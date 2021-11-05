package com.adaptivebiotech.cora.test.order;

import static com.adaptivebiotech.cora.utils.TestHelper.insurance1;
import static com.adaptivebiotech.cora.utils.TestHelper.newInsurancePatient;
import static com.adaptivebiotech.cora.utils.TestHelper.newMedicarePatient;
import static com.adaptivebiotech.cora.utils.TestHelper.newPatient;
import static com.adaptivebiotech.cora.utils.TestHelper.physicianTRF;
import static com.adaptivebiotech.test.utils.PageHelper.Anticoagulant.EDTA;
import static com.adaptivebiotech.test.utils.PageHelper.Assay.ID_BCell2_CLIA;
import static com.adaptivebiotech.test.utils.PageHelper.ChargeType.Client;
import static com.adaptivebiotech.test.utils.PageHelper.ChargeType.InternalPharmaBilling;
import static com.adaptivebiotech.test.utils.PageHelper.ChargeType.NoCharge;
import static com.adaptivebiotech.test.utils.PageHelper.ChargeType.PatientSelfPay;
import static com.adaptivebiotech.test.utils.PageHelper.ChargeType.TrialProtocol;
import static com.adaptivebiotech.test.utils.PageHelper.ContainerType.Tube;
import static com.adaptivebiotech.test.utils.PageHelper.DeliveryType.CustomerShipment;
import static com.adaptivebiotech.test.utils.PageHelper.PatientStatus.NonHospital;
import static com.adaptivebiotech.test.utils.PageHelper.ShippingCondition.Ambient;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.Blood;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.NewOrderClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.shipment.Accession;
import com.adaptivebiotech.cora.ui.shipment.Shipment;
import com.adaptivebiotech.cora.utils.DateUtils;

@Test (groups = "regression", enabled = false)
public class BillingTestSuite extends CoraBaseBrowser {

    private OrdersList       oList            = new OrdersList ();
    private NewOrderClonoSeq diagnostic = new NewOrderClonoSeq ();

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod () {
        new Login ().doLogin ();
        oList.isCorrectPage ();
        oList.selectNewClonoSEQDiagnosticOrder ();

        diagnostic.isCorrectPage ();
        diagnostic.selectPhysician (physicianTRF ());
        diagnostic.enterPatientICD_Codes ("A01.02");
    }

    public void insurance () {
        Patient patient = newInsurancePatient ();

        diagnostic.createNewPatient (patient);
        diagnostic.billing.enterInsuranceInfo (patient);
        complete_order_and_activate ();

        // test: remove patient and confirm billing is reset
        diagnostic.removePatient ();
        diagnostic.removePatientTest ();
        assertNull (diagnostic.billing.getBilling ());

        // test: confirm we're able to Activate
        diagnostic.selectPatient (patient);
        diagnostic.billing.enterInsuranceInfo (patient);
        activate_and_cancel ();
    }

    /**
     * @sdlc_requirements 173.Medicare.required
     */
    public void medicare () {
        Patient patient = newMedicarePatient ();

        diagnostic.createNewPatient (patient);
        diagnostic.billing.enterMedicareInfo (patient);
        complete_order_and_activate ();

        // test: remove patient and confirm billing is reset
        diagnostic.removePatient ();
        diagnostic.removePatientTest ();
        assertNull (diagnostic.billing.getBilling ());

        // test: confirm we're able to Activate
        diagnostic.selectPatient (patient);
        diagnostic.billing.enterMedicareInfo (patient);
        activate_and_cancel ();
    }

    public void patientSelfPayNonHospital () {
        Patient patient = newPatient ();
        patient.billingType = PatientSelfPay;
        patient.insurance1.hospitalizationStatus = NonHospital;

        diagnostic.createNewPatient (patient);
        diagnostic.billing.enterBill (patient);
        complete_order_and_activate ();

        // test: remove patient and confirm billing is reset
        diagnostic.removePatient ();
        diagnostic.removePatientTest ();
        assertNull (diagnostic.billing.getBilling ());

        // test: confirm we're able to Activate
        diagnostic.selectPatient (patient);
        diagnostic.billing.enterBill (patient);
        activate_and_cancel ();
    }

    public void patientSelfPayInpatient () {
        Patient patient = newPatient ();
        patient.billingType = PatientSelfPay;
        patient.insurance1 = insurance1 ();

        diagnostic.createNewPatient (patient);
        diagnostic.billing.enterBill (patient);
        complete_order_and_activate ();

        // test: remove patient and confirm billing is reset
        diagnostic.removePatient ();
        diagnostic.removePatientTest ();
        assertNull (diagnostic.billing.getBilling ());

        // test: confirm we're able to Activate
        diagnostic.selectPatient (patient);
        diagnostic.billing.enterBill (patient);
        activate_and_cancel ();
    }

    public void billClientNonHospital () {
        Patient patient = newPatient ();
        patient.billingType = Client;
        patient.insurance1.hospitalizationStatus = NonHospital;

        diagnostic.createNewPatient (patient);
        diagnostic.billing.enterBill (patient);
        complete_order_and_activate ();

        // test: remove patient and confirm billing is reset
        diagnostic.removePatient ();
        diagnostic.removePatientTest ();
        assertNull (diagnostic.billing.getBilling ());

        // test: confirm we're able to Activate
        diagnostic.selectPatient (patient);
        diagnostic.billing.enterBill (patient);
        activate_and_cancel ();
    }

    public void billClientInpatient () {
        Patient patient = newPatient ();
        patient.billingType = Client;
        patient.insurance1 = insurance1 ();

        diagnostic.createNewPatient (patient);
        diagnostic.billing.enterBill (patient);
        complete_order_and_activate ();

        // test: remove patient and confirm billing is reset
        diagnostic.removePatient ();
        diagnostic.removePatientTest ();
        assertNull (diagnostic.billing.getBilling ());

        // test: confirm we're able to Activate
        diagnostic.selectPatient (patient);
        diagnostic.billing.enterBill (patient);
        activate_and_cancel ();
    }

    public void billPerStudyProtocol () {
        Patient patient = newPatient ();
        patient.billingType = TrialProtocol;

        diagnostic.createNewPatient (patient);
        diagnostic.billing.selectBilling (patient.billingType);
        complete_order_and_activate ();

        // test: remove patient and confirm billing stays the same
        diagnostic.clickRemovePatient ();
        diagnostic.removePatientTest ();
        assertEquals (diagnostic.billing.getBilling (), patient.billingType);

        // test: confirm we're able to Activate
        diagnostic.selectPatient (patient);
        activate_and_cancel ();
    }

    public void noCharge () {
        Patient patient = newPatient ();
        patient.billingType = NoCharge;

        diagnostic.createNewPatient (patient);
        diagnostic.billing.selectBilling (patient.billingType);
        complete_order_and_activate ();

        // test: remove patient and confirm billing stays the same
        diagnostic.clickRemovePatient ();
        diagnostic.removePatientTest ();
        assertEquals (diagnostic.billing.getBilling (), patient.billingType);

        // test: confirm we're able to Activate
        diagnostic.selectPatient (patient);
        activate_and_cancel ();
    }

    public void internalPharmaBilling () {
        Patient patient = newPatient ();
        patient.billingType = InternalPharmaBilling;

        diagnostic.createNewPatient (patient);
        diagnostic.billing.selectBilling (patient.billingType);
        complete_order_and_activate ();

        // test: remove patient and confirm billing stays the same
        diagnostic.clickRemovePatient ();
        diagnostic.removePatientTest ();
        assertEquals (diagnostic.billing.getBilling (), patient.billingType);

        // test: confirm we're able to Activate
        diagnostic.selectPatient (patient);
        activate_and_cancel ();
    }

    private void activate_and_cancel () {
        diagnostic.clickAssayTest (ID_BCell2_CLIA);
        diagnostic.activateOrder ();
        diagnostic.clickCancelOrder ();
    }

    private void complete_order_and_activate () {
        diagnostic.clickSave ();
        diagnostic.enterSpecimenDelivery (CustomerShipment);
        diagnostic.clickEnterSpecimenDetails ();
        diagnostic.enterSpecimenType (Blood);
        diagnostic.enterAntiCoagulant (EDTA);
        diagnostic.enterCollectionDate (DateUtils.getPastFutureDate (-3));
        diagnostic.clickSave ();
        String orderNum = diagnostic.getOrderNum ();

        // test: add diagnostic shipment
        Shipment shipment = new Shipment ();
        shipment.selectNewDiagnosticShipment ();
        shipment.isDiagnostic ();
        shipment.enterShippingCondition (Ambient);
        shipment.enterOrderNumber (orderNum);
        shipment.selectDiagnosticSpecimenContainerType (Tube);
        shipment.clickSave ();
        shipment.gotoAccession ();

        // test: accession complete
        Accession accession = new Accession ();
        accession.isCorrectPage ();
        accession.clickIntakeComplete ();
        accession.labelingComplete ();
        accession.labelVerificationComplete ();
        accession.clickPass ();
        accession.gotoOrderDetail ();

        // test: add a test, confirm we're able to Activate and then cancel it
        diagnostic.isCorrectPage ();
        diagnostic.clickAssayTest (ID_BCell2_CLIA);
        diagnostic.clickActivateOrder ();
        diagnostic.clickCancel ();
    }
}
