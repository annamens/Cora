package com.adaptivebiotech.cora.test.order.tdetect;

import static com.adaptivebiotech.cora.dto.Containers.ContainerType.Tube;
import static com.adaptivebiotech.cora.dto.Orders.Assay.COVID19_DX_IVD;
import static com.adaptivebiotech.cora.dto.Orders.OrderStatus.Active;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.TDetect_client;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.TDetect_insurance;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.TDetect_medicare;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.TDetect_selfpay;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.TDetect_trial;
import static com.adaptivebiotech.cora.utils.TestHelper.bloodSpecimen;
import static com.adaptivebiotech.cora.utils.TestHelper.newClientPatient;
import static com.adaptivebiotech.cora.utils.TestHelper.newInsurancePatient;
import static com.adaptivebiotech.cora.utils.TestHelper.newMedicarePatient;
import static com.adaptivebiotech.cora.utils.TestHelper.newSelfPayPatient;
import static com.adaptivebiotech.cora.utils.TestHelper.newTrialProtocolPatient;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static java.lang.String.format;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.dto.Specimen;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.NewOrderTDetect;
import com.adaptivebiotech.cora.ui.order.OrdersList;

/**
 * Note:
 * - ICD codes and patient billing address are not required
 */
@Test (groups = "regression")
public class BillingTestSuite extends CoraBaseBrowser {

    private final String    log        = "created an order with billing: %s";
    private Login           login      = new Login ();
    private OrdersList      ordersList = new OrdersList ();
    private NewOrderTDetect diagnostic = new NewOrderTDetect ();
    private Specimen        specimen   = bloodSpecimen ();

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod () {
        login.doLogin ();
        ordersList.isCorrectPage ();
    }

    /**
     * @sdlc_requirements SR-7907:R1
     */
    @Test (groups = "corgi")
    public void insurance () {
        Patient patient = newInsurancePatient ();
        diagnostic.createTDetectOrder (coraApi.getPhysician (TDetect_insurance),
                                       patient,
                                       null,
                                       specimen.collectionDate.toString (),
                                       COVID19_DX_IVD,
                                       Active,
                                       Tube);
        testLog (format (log, patient.billingType.label));
    }

    /**
     * @sdlc_requirements SR-7907:R1
     */
    @Test (groups = "corgi")
    public void medicare () {
        Patient patient = newMedicarePatient ();
        patient.abnStatusType = null;
        diagnostic.createTDetectOrder (coraApi.getPhysician (TDetect_medicare),
                                       patient,
                                       null,
                                       specimen.collectionDate.toString (),
                                       COVID19_DX_IVD,
                                       Active,
                                       Tube);
        testLog (format (log, patient.billingType.label));
    }

    /**
     * @sdlc_requirements SR-7907:R1
     */
    @Test (groups = "corgi")
    public void patient_self_pay () {
        Patient patient = newSelfPayPatient ();
        diagnostic.createTDetectOrder (coraApi.getPhysician (TDetect_selfpay),
                                       patient,
                                       null,
                                       specimen.collectionDate.toString (),
                                       COVID19_DX_IVD,
                                       Active,
                                       Tube);
        testLog (format (log, patient.billingType.label));
    }

    /**
     * @sdlc_requirements SR-7907:R1
     */
    @Test (groups = "corgi")
    public void client_bill () {
        Patient patient = newClientPatient ();
        diagnostic.createTDetectOrder (coraApi.getPhysician (TDetect_client),
                                       patient,
                                       null,
                                       specimen.collectionDate.toString (),
                                       COVID19_DX_IVD,
                                       Active,
                                       Tube);
        testLog (format (log, patient.billingType.label));
    }

    /**
     * @sdlc_requirements SR-7907:R20
     */
    @Test (groups = "corgi")
    public void bill_per_study_protocol () {
        Patient patient = newTrialProtocolPatient ();
        diagnostic.createTDetectOrder (coraApi.getPhysician (TDetect_trial),
                                       patient,
                                       null,
                                       specimen.collectionDate.toString (),
                                       COVID19_DX_IVD,
                                       Active,
                                       Tube);
        testLog (format (log, patient.billingType.label));
    }
}
