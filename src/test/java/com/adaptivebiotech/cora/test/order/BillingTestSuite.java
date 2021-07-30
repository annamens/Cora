package com.adaptivebiotech.cora.test.order;

import static com.adaptivebiotech.cora.utils.TestHelper.insurance1;
import static com.adaptivebiotech.cora.utils.TestHelper.newInsurancePatient;
import static com.adaptivebiotech.cora.utils.TestHelper.newMedicarePatient;
import static com.adaptivebiotech.cora.utils.TestHelper.newPatient;
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
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.Billing;
import com.adaptivebiotech.cora.ui.order.Diagnostic;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.order.Specimen;
import com.adaptivebiotech.cora.ui.shipment.Accession;
import com.adaptivebiotech.cora.ui.shipment.Shipment;

@Test (groups = "regression", enabled = false)
public class BillingTestSuite extends OrderTestBase {

    private OrdersList oList;
    private Diagnostic diagnostic;
    private Billing    billing;

    @BeforeMethod
    public void beforeMethod () {
        new Login ().doLogin ();
        oList = new OrdersList ();
        oList.isCorrectPage ();
        oList.selectNewClonoSEQDiagnosticOrder ();

        diagnostic = new Diagnostic ();
        diagnostic.isCorrectPage ();
        diagnostic.selectPhysician (physicianTRF);
        diagnostic.enterPatientICD_Codes (icdCode);
        billing = new Billing ();
    }

    public void insurance () {
        Patient patient = newInsurancePatient ();

        billing.createNewPatient (patient);
        billing.enterInsuranceInfo (patient);
        complete_order_and_activate ();

        // test: remove patient and confirm billing is reset
        billing.removePatient ();
        billing.removePatientTest ();
        assertNull (billing.getBilling ());

        // test: confirm we're able to Activate
        billing.selectPatient (patient);
        billing.enterInsuranceInfo (patient);
        activate_and_cancel ();
    }

    /**
     * @sdlc_requirements 173.Medicare.required
     */
    public void medicare () {
        Patient patient = newMedicarePatient ();

        billing.createNewPatient (patient);
        billing.enterMedicareInfo (patient);
        complete_order_and_activate ();

        // test: remove patient and confirm billing is reset
        billing.removePatient ();
        billing.removePatientTest ();
        assertNull (billing.getBilling ());

        // test: confirm we're able to Activate
        billing.selectPatient (patient);
        billing.enterMedicareInfo (patient);
        activate_and_cancel ();
    }

    public void patientSelfPayNonHospital () {
        Patient patient = newPatient ();
        patient.billingType = PatientSelfPay;
        patient.insurance1.hospitalizationStatus = NonHospital;

        billing.createNewPatient (patient);
        billing.enterBill (patient);
        complete_order_and_activate ();

        // test: remove patient and confirm billing is reset
        billing.removePatient ();
        billing.removePatientTest ();
        assertNull (billing.getBilling ());

        // test: confirm we're able to Activate
        billing.selectPatient (patient);
        billing.enterBill (patient);
        activate_and_cancel ();
    }

    public void patientSelfPayInpatient () {
        Patient patient = newPatient ();
        patient.billingType = PatientSelfPay;
        patient.insurance1 = insurance1 ();

        billing.createNewPatient (patient);
        billing.enterBill (patient);
        complete_order_and_activate ();

        // test: remove patient and confirm billing is reset
        billing.removePatient ();
        billing.removePatientTest ();
        assertNull (billing.getBilling ());

        // test: confirm we're able to Activate
        billing.selectPatient (patient);
        billing.enterBill (patient);
        activate_and_cancel ();
    }

    public void billClientNonHospital () {
        Patient patient = newPatient ();
        patient.billingType = Client;
        patient.insurance1.hospitalizationStatus = NonHospital;

        billing.createNewPatient (patient);
        billing.enterBill (patient);
        complete_order_and_activate ();

        // test: remove patient and confirm billing is reset
        billing.removePatient ();
        billing.removePatientTest ();
        assertNull (billing.getBilling ());

        // test: confirm we're able to Activate
        billing.selectPatient (patient);
        billing.enterBill (patient);
        activate_and_cancel ();
    }

    public void billClientInpatient () {
        Patient patient = newPatient ();
        patient.billingType = Client;
        patient.insurance1 = insurance1 ();

        billing.createNewPatient (patient);
        billing.enterBill (patient);
        complete_order_and_activate ();

        // test: remove patient and confirm billing is reset
        billing.removePatient ();
        billing.removePatientTest ();
        assertNull (billing.getBilling ());

        // test: confirm we're able to Activate
        billing.selectPatient (patient);
        billing.enterBill (patient);
        activate_and_cancel ();
    }

    public void billPerStudyProtocol () {
        Patient patient = newPatient ();
        patient.billingType = TrialProtocol;

        billing.createNewPatient (patient);
        billing.selectBilling (patient.billingType);
        complete_order_and_activate ();

        // test: remove patient and confirm billing stays the same
        billing.clickRemovePatient ();
        billing.removePatientTest ();
        assertEquals (billing.getBilling (), patient.billingType);

        // test: confirm we're able to Activate
        billing.selectPatient (patient);
        activate_and_cancel ();
    }

    public void noCharge () {
        Patient patient = newPatient ();
        patient.billingType = NoCharge;

        billing.createNewPatient (patient);
        billing.selectBilling (patient.billingType);
        complete_order_and_activate ();

        // test: remove patient and confirm billing stays the same
        billing.clickRemovePatient ();
        billing.removePatientTest ();
        assertEquals (billing.getBilling (), patient.billingType);

        // test: confirm we're able to Activate
        billing.selectPatient (patient);
        activate_and_cancel ();
    }

    public void internalPharmaBilling () {
        Patient patient = newPatient ();
        patient.billingType = InternalPharmaBilling;

        billing.createNewPatient (patient);
        billing.selectBilling (patient.billingType);
        complete_order_and_activate ();

        // test: remove patient and confirm billing stays the same
        billing.clickRemovePatient ();
        billing.removePatientTest ();
        assertEquals (billing.getBilling (), patient.billingType);

        // test: confirm we're able to Activate
        billing.selectPatient (patient);
        activate_and_cancel ();
    }

    private void activate_and_cancel () {
        diagnostic.clickAssayTest (ID_BCell2_CLIA);
        diagnostic.activateOrder ();
        diagnostic.clickCancelOrder ();
    }

    private void complete_order_and_activate () {
        Specimen specimen = new Specimen ();
        specimen.clickSave ();
        specimen.enterSpecimenDelivery (CustomerShipment);
        specimen.clickEnterSpecimenDetails ();
        specimen.enterSpecimenType (Blood);
        specimen.enterAntiCoagulant (EDTA);
        specimen.enterCollectionDate (collectionDt);
        specimen.clickSave ();
        String orderNum = specimen.getOrderNum ();

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
