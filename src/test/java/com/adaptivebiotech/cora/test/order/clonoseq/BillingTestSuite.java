package com.adaptivebiotech.cora.test.order.clonoseq;

import static com.adaptivebiotech.cora.dto.Containers.ContainerType.Tube;
import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Orders.OrderStatus.Active;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.clonoSEQ_client;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.clonoSEQ_insurance;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.clonoSEQ_medicare;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.clonoSEQ_selfpay;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.clonoSEQ_trial;
import static com.adaptivebiotech.cora.dto.Specimen.Anticoagulant.EDTA;
import static com.adaptivebiotech.cora.utils.TestHelper.newClientPatient;
import static com.adaptivebiotech.cora.utils.TestHelper.newInsurancePatient;
import static com.adaptivebiotech.cora.utils.TestHelper.newMedicarePatient;
import static com.adaptivebiotech.cora.utils.TestHelper.newSelfPayPatient;
import static com.adaptivebiotech.cora.utils.TestHelper.newTrialProtocolPatient;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.Blood;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.dto.Specimen;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.NewOrderClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrdersList;

@Test (groups = "regression")
public class BillingTestSuite extends CoraBaseBrowser {

    private final String[]   icdCodes   = { "A01.02" };
    private Login            login      = new Login ();
    private OrdersList       ordersList = new OrdersList ();
    private NewOrderClonoSeq diagnostic = new NewOrderClonoSeq ();
    private Specimen         specimen;

    @BeforeMethod
    public void beforeMethod () {
        login.doLogin ();
        ordersList.isCorrectPage ();

        specimen = new Specimen ();
        specimen.sampleType = Blood;
        specimen.anticoagulant = EDTA;
        coraApi.login ();
    }

    public void insurance () {
        Patient patient = newInsurancePatient ();
        diagnostic.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_insurance),
                                        patient,
                                        icdCodes,
                                        ID_BCell2_CLIA,
                                        specimen,
                                        Active,
                                        Tube);
        testLog ("created an order with billing: Insurance (Including Medicare Advantage)");
    }

    /**
     * @sdlc_requirements 173.Medicare.required
     */
    public void medicare () {
        Patient patient = newMedicarePatient ();
        diagnostic.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_medicare),
                                        patient,
                                        icdCodes,
                                        ID_BCell2_CLIA,
                                        specimen,
                                        Active,
                                        Tube);
        testLog ("created an order with billing: Medicare");
    }

    public void patientSelfPay () {
        Patient patient = newSelfPayPatient ();
        diagnostic.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_selfpay),
                                        patient,
                                        icdCodes,
                                        ID_BCell2_CLIA,
                                        specimen,
                                        Active,
                                        Tube);
        testLog ("created an order with billing: Patient Self-Pay");
    }

    public void billClient () {
        Patient patient = newClientPatient ();
        diagnostic.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_client),
                                        patient,
                                        icdCodes,
                                        ID_BCell2_CLIA,
                                        specimen,
                                        Active,
                                        Tube);
        testLog ("created an order with billing: Client Bill");
    }

    public void billPerStudyProtocol () {
        Patient patient = newTrialProtocolPatient ();
        diagnostic.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_trial),
                                        patient,
                                        icdCodes,
                                        ID_BCell2_CLIA,
                                        specimen,
                                        Active,
                                        Tube);
        testLog ("created an order with billing: Bill per Study Protocol");
    }
}
