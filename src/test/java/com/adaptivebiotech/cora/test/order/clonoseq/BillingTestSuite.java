package com.adaptivebiotech.cora.test.order.clonoseq;

import static com.adaptivebiotech.cora.dto.Containers.ContainerType.Tube;
import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Orders.OrderStatus.Active;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.clonoSEQ_client;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.clonoSEQ_insurance;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.clonoSEQ_medicare;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.clonoSEQ_selfpay;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.clonoSEQ_trial;
import static com.adaptivebiotech.cora.utils.PageHelper.AbnStatus.NotRequired;
import static com.adaptivebiotech.cora.utils.PageHelper.AbnStatus.RequiredIncludedBillMedicare;
import static com.adaptivebiotech.cora.utils.TestHelper.bloodSpecimen;
import static com.adaptivebiotech.cora.utils.TestHelper.newClientPatient;
import static com.adaptivebiotech.cora.utils.TestHelper.newInsurancePatient;
import static com.adaptivebiotech.cora.utils.TestHelper.newMedicarePatient;
import static com.adaptivebiotech.cora.utils.TestHelper.newSelfPayPatient;
import static com.adaptivebiotech.cora.utils.TestHelper.newTrialProtocolPatient;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.dto.Specimen;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.NewOrderClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrderDetailClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrdersList;

@Test (groups = "regression")
public class BillingTestSuite extends CoraBaseBrowser {

    private final String        log         = "created an order with billing: %s";
    private final String[]      icdCodes    = { "V95.43" };
    private Login               login       = new Login ();
    private OrdersList          ordersList  = new OrdersList ();
    private NewOrderClonoSeq    diagnostic  = new NewOrderClonoSeq ();
    private OrderDetailClonoSeq orderDetail = new OrderDetailClonoSeq ();
    private Specimen            specimen    = bloodSpecimen ();

    @BeforeMethod
    public void beforeMethod () {
        login.doLogin ();
        ordersList.isCorrectPage ();
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
     * @sdlc_requirements 173.Medicare.required, SR-1516
     */
    public void medicare_abn_required () {
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

        assertEquals (orderDetail.billing.getAbnStatus (), RequiredIncludedBillMedicare);
        testLog ("ABN Status dropdown was visible and was able to make a selection");
    }

    /**
     * @sdlc_requirements 173.Medicare.required, SR-1516
     */
    public void medicare_abn_not_required () {
        Patient patient = newMedicarePatient ();
        diagnostic.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_medicare),
                                        patient,
                                        new String[] { icdCodes[0], "C91.10" },
                                        ID_BCell2_CLIA,
                                        patient.billingType,
                                        specimen.sampleType,
                                        specimen.sampleSource,
                                        specimen.anticoagulant,
                                        Active,
                                        Tube);
        testLog (format (log, patient.billingType.label));

        assertEquals (orderDetail.billing.getAbnStatus (), NotRequired);
        testLog ("ABN Status field value was 'Not required' and was not editable");
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
