package com.adaptivebiotech.cora.test.order.clonoseq;

import static com.adaptivebiotech.cora.utils.TestHelper.newClientPatient;
import static com.adaptivebiotech.cora.utils.TestHelper.newInsurancePatient;
import static com.adaptivebiotech.cora.utils.TestHelper.newMedicarePatient;
import static com.adaptivebiotech.cora.utils.TestHelper.newSelfPayPatient;
import static com.adaptivebiotech.cora.utils.TestHelper.newTrialProtocolPatient;
import static com.adaptivebiotech.cora.utils.TestHelper.physicianClonoSEQClientBill;
import static com.adaptivebiotech.cora.utils.TestHelper.physicianClonoSEQInsurance;
import static com.adaptivebiotech.cora.utils.TestHelper.physicianClonoSEQMedicare;
import static com.adaptivebiotech.cora.utils.TestHelper.physicianClonoSEQSelfPay;
import static com.adaptivebiotech.cora.utils.TestHelper.physicianClonoSEQTrialProtocol;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.PageHelper.Anticoagulant.EDTA;
import static com.adaptivebiotech.test.utils.PageHelper.Assay.ID_BCell2_CLIA;
import static com.adaptivebiotech.test.utils.PageHelper.ContainerType.Tube;
import static com.adaptivebiotech.test.utils.PageHelper.OrderStatus.Active;
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
    private OrdersList       oList      = new OrdersList ();
    private NewOrderClonoSeq diagnostic = new NewOrderClonoSeq ();

    private Specimen         specimen;

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod () {
        new Login ().doLogin ();
        oList.isCorrectPage ();

        specimen = new Specimen ();
        specimen.sampleType = Blood;
        specimen.anticoagulant = EDTA;
    }

    public void insurance () {
        doCoraLogin ();
        Patient patient = newInsurancePatient ();
        diagnostic.createClonoSeqOrder (physicianClonoSEQInsurance (),
                                        patient,
                                        icdCodes,
                                        ID_BCell2_CLIA,
                                        patient.billingType,
                                        specimen.sampleType,
                                        specimen.sampleSource,
                                        specimen.anticoagulant,
                                        Active,
                                        Tube);
        testLog ("created an order with billing: Insurance (Including Medicare Advantage)");
    }

    /**
     * @sdlc_requirements 173.Medicare.required
     */
    public void medicare () {
        doCoraLogin ();
        Patient patient = newMedicarePatient ();
        diagnostic.createClonoSeqOrder (physicianClonoSEQMedicare (),
                                        patient,
                                        icdCodes,
                                        ID_BCell2_CLIA,
                                        patient.billingType,
                                        specimen.sampleType,
                                        specimen.sampleSource,
                                        specimen.anticoagulant,
                                        Active,
                                        Tube);
        testLog ("created an order with billing: Medicare");
    }

    public void patientSelfPay () {
        doCoraLogin ();
        Patient patient = newSelfPayPatient ();
        diagnostic.createClonoSeqOrder (physicianClonoSEQSelfPay (),
                                        patient,
                                        icdCodes,
                                        ID_BCell2_CLIA,
                                        patient.billingType,
                                        specimen.sampleType,
                                        specimen.sampleSource,
                                        specimen.anticoagulant,
                                        Active,
                                        Tube);
        testLog ("created an order with billing: Patient Self-Pay");
    }

    public void billClient () {
        doCoraLogin ();
        Patient patient = newClientPatient ();
        diagnostic.createClonoSeqOrder (physicianClonoSEQClientBill (),
                                        patient,
                                        icdCodes,
                                        ID_BCell2_CLIA,
                                        patient.billingType,
                                        specimen.sampleType,
                                        specimen.sampleSource,
                                        specimen.anticoagulant,
                                        Active,
                                        Tube);
        testLog ("created an order with billing: Client Bill");
    }

    public void billPerStudyProtocol () {
        doCoraLogin ();
        Patient patient = newTrialProtocolPatient ();
        diagnostic.createClonoSeqOrder (physicianClonoSEQTrialProtocol (),
                                        patient,
                                        icdCodes,
                                        ID_BCell2_CLIA,
                                        patient.billingType,
                                        specimen.sampleType,
                                        specimen.sampleSource,
                                        specimen.anticoagulant,
                                        Active,
                                        Tube);
        testLog ("created an order with billing: Client Bill");
    }
}
