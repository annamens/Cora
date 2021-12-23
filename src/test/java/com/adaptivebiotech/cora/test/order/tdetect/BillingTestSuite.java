package com.adaptivebiotech.cora.test.order.tdetect;

import static com.adaptivebiotech.cora.dto.Containers.ContainerType.Tube;
import static com.adaptivebiotech.cora.dto.Orders.Assay.COVID19_DX_IVD;
import static com.adaptivebiotech.cora.dto.Orders.OrderStatus.Active;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.TDetect_client;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.TDetect_selfpay;
import static com.adaptivebiotech.cora.dto.Specimen.Anticoagulant.EDTA;
import static com.adaptivebiotech.cora.utils.TestHelper.getRandomAddress;
import static com.adaptivebiotech.cora.utils.TestHelper.newClientPatient;
import static com.adaptivebiotech.cora.utils.TestHelper.newSelfPayPatient;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.Blood;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.dto.Specimen;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.NewOrderTDetect;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.utils.DateUtils;

@Test (groups = "regression")
public class BillingTestSuite extends CoraBaseBrowser {

    private final String[]  icdCodes   = { "A01.02" };
    private Login           login      = new Login ();
    private OrdersList      ordersList = new OrdersList ();
    private NewOrderTDetect diagnostic = new NewOrderTDetect ();
    private Specimen        specimen;

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod () {
        login.doLogin ();
        ordersList.isCorrectPage ();

        specimen = new Specimen ();
        specimen.sampleType = Blood;
        specimen.anticoagulant = EDTA;
        specimen.collectionDate = DateUtils.getPastFutureDate (-3);
        coraApi.login ();
    }

    public void patientSelfPay () {
        Patient patient = newSelfPayPatient ();
        diagnostic.createTDetectOrder (coraApi.getPhysician (TDetect_selfpay),
                                       patient,
                                       icdCodes,
                                       specimen.collectionDate.toString (),
                                       COVID19_DX_IVD,
                                       patient.billingType,
                                       getRandomAddress (),
                                       Active,
                                       Tube);
        testLog ("created an order with billing: Patient Self-Pay");
    }

    public void billClient () {
        Patient patient = newClientPatient ();
        diagnostic.createTDetectOrder (coraApi.getPhysician (TDetect_client),
                                       patient,
                                       icdCodes,
                                       specimen.collectionDate.toString (),
                                       COVID19_DX_IVD,
                                       patient.billingType,
                                       getRandomAddress (),
                                       Active,
                                       Tube);
        testLog ("created an order with billing: Client Bill");
    }
}
