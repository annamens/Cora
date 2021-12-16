package com.adaptivebiotech.cora.test.order.clonoseq;

import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.clonoSEQ_client;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.clonoSEQ_insurance;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.clonoSEQ_medicare;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.clonoSEQ_selfpay;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.clonoSEQ_trial;
import static com.adaptivebiotech.cora.utils.TestHelper.newClientPatient;
import static com.adaptivebiotech.cora.utils.TestHelper.newInsurancePatient;
import static com.adaptivebiotech.cora.utils.TestHelper.newMedicarePatient;
import static com.adaptivebiotech.cora.utils.TestHelper.newSelfPayPatient;
import static com.adaptivebiotech.cora.utils.TestHelper.newTrialProtocolPatient;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.PageHelper.Anticoagulant.EDTA;
import static com.adaptivebiotech.test.utils.PageHelper.Assay.ID_BCell2_CLIA;
import static com.adaptivebiotech.test.utils.PageHelper.ContainerType.Tube;
import static com.adaptivebiotech.test.utils.PageHelper.OrderStatus.Active;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.Blood;
import static java.lang.String.format;
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

    private final String     log        = "created an order with billing: %s";
    private final String[]   icdCodes   = { "V95.43" };
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
                                        patient.billingType,
                                        specimen.sampleType,
                                        specimen.sampleSource,
                                        specimen.anticoagulant,
                                        Active,
                                        Tube);
        testLog (format (log, patient.billingType.label));
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
                                        patient.billingType,
                                        specimen.sampleType,
                                        specimen.sampleSource,
                                        specimen.anticoagulant,
                                        Active,
                                        Tube);
        testLog (format (log, patient.billingType.label));
    }

    public void patientSelfPay () {
        Patient patient = newSelfPayPatient ();
        diagnostic.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_selfpay),
                                        patient,
                                        icdCodes,
                                        ID_BCell2_CLIA,
                                        patient.billingType,
                                        specimen.sampleType,
                                        specimen.sampleSource,
                                        specimen.anticoagulant,
                                        Active,
                                        Tube);
        testLog (format (log, patient.billingType.label));
    }

    public void billClient () {
        Patient patient = newClientPatient ();
        diagnostic.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_client),
                                        patient,
                                        icdCodes,
                                        ID_BCell2_CLIA,
                                        patient.billingType,
                                        specimen.sampleType,
                                        specimen.sampleSource,
                                        specimen.anticoagulant,
                                        Active,
                                        Tube);
        testLog (format (log, patient.billingType.label));
    }

    public void billPerStudyProtocol () {
        Patient patient = newTrialProtocolPatient ();
        diagnostic.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_trial),
                                        patient,
                                        icdCodes,
                                        ID_BCell2_CLIA,
                                        patient.billingType,
                                        specimen.sampleType,
                                        specimen.sampleSource,
                                        specimen.anticoagulant,
                                        Active,
                                        Tube);
        testLog (format (log, patient.billingType.label));
    }
}
