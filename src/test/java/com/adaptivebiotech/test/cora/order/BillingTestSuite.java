package com.adaptivebiotech.test.cora.order;

import static com.adaptivebiotech.utils.PageHelper.Anticoagulant.EDTA;
import static com.adaptivebiotech.utils.PageHelper.Assay.Clonality_BCell_2;
import static com.adaptivebiotech.utils.PageHelper.ChargeType.Client;
import static com.adaptivebiotech.utils.PageHelper.ChargeType.InternalPharmaBilling;
import static com.adaptivebiotech.utils.PageHelper.ChargeType.NoCharge;
import static com.adaptivebiotech.utils.PageHelper.ChargeType.PatientSelfPay;
import static com.adaptivebiotech.utils.PageHelper.ChargeType.TrialProtocol;
import static com.adaptivebiotech.utils.PageHelper.ContainerType.Tube;
import static com.adaptivebiotech.utils.PageHelper.DeliveryType.CustomerShipment;
import static com.adaptivebiotech.utils.PageHelper.ShippingCondition.Ambient;
import static com.adaptivebiotech.utils.PageHelper.SpecimenType.Blood;
import static com.adaptivebiotech.utils.TestHelper.newInsurancePatient;
import static com.adaptivebiotech.utils.TestHelper.newMedicarePatient;
import static com.adaptivebiotech.utils.TestHelper.newPatient;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.dto.Patient;
import com.adaptivebiotech.ui.cora.CoraPage;
import com.adaptivebiotech.ui.cora.order.Billing;
import com.adaptivebiotech.ui.cora.order.Diagnostic;
import com.adaptivebiotech.ui.cora.order.Specimen;
import com.adaptivebiotech.ui.cora.shipment.Accession;
import com.adaptivebiotech.ui.cora.shipment.Shipment;

@Test (groups = { "cora" })
public class BillingTestSuite extends OrderTestBase {

    private Diagnostic diagnostic;
    private Billing    billing;

    @BeforeMethod
    public void beforeMethod () {
        new CoraPage ().clickNewDiagnosticOrder ();
        diagnostic = new Diagnostic ();
        diagnostic.isCorrectPage ();
        diagnostic.selectPhysician (physicianTRF);
        diagnostic.enterPatientICD_Codes (icdCode);
        billing = new Billing ();
    }

    public void insurance () {
        Patient patient = newInsurancePatient ();
        patient.address = null; // not required for cora

        billing.createNewPatient (patient);
        billing.enterInsuranceInfo (patient);
        complete_order_and_activate ();

        // test: remove patient and confirm billing is reset
        billing.removePatient ();
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
        patient.address = null; // not required for cora

        billing.createNewPatient (patient);
        billing.enterMedicareInfo (patient);
        complete_order_and_activate ();

        // test: remove patient and confirm billing is reset
        billing.removePatient ();
        assertNull (billing.getBilling ());

        // test: confirm we're able to Activate
        billing.selectPatient (patient);
        billing.enterMedicareInfo (patient);
        activate_and_cancel ();
    }

    public void patientSelfPay () {
        Patient patient = newPatient ();
        patient.billingType = PatientSelfPay;

        billing.createNewPatient (patient);
        billing.selectBilling (patient.billingType);
        complete_order_and_activate ();

        // test: remove patient and confirm billing is reset
        billing.removePatient ();
        assertNull (billing.getBilling ());

        // test: confirm we're able to Activate
        billing.selectPatient (patient);
        billing.selectBilling (patient.billingType);
        activate_and_cancel ();
    }

    public void billClient () {
        Patient patient = newPatient ();
        patient.billingType = Client;

        billing.createNewPatient (patient);
        billing.selectBilling (patient.billingType);
        complete_order_and_activate ();

        // test: remove patient and confirm billing is reset
        billing.removePatient ();
        assertNull (billing.getBilling ());

        // test: confirm we're able to Activate
        billing.selectPatient (patient);
        billing.selectBilling (patient.billingType);
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
        assertEquals (billing.getBilling (), patient.billingType);

        // test: confirm we're able to Activate
        billing.selectPatient (patient);
        activate_and_cancel ();
    }

    private void activate_and_cancel () {
        diagnostic.clickActivateOrder ();
        diagnostic.clickCancel ();
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
        shipment.clickNewDiagnosticShipment ();
        shipment.isCorrectPage ();
        shipment.enterShippingCondition (Ambient);
        shipment.enterOrderNumber (orderNum);
        shipment.enterDiagnosticSpecimenContainerType (Tube);
        shipment.clickSave ();
        shipment.gotoAccession ();

        // test: accession complete
        Accession accession = new Accession ();
        accession.isCorrectPage ();
        accession.clickIntakeComplete ();
        accession.clickPass ();
        accession.gotoOrderDetail ();

        // test: add a test, confirm we're able to Activate and then cancel it
        diagnostic.isCorrectPage ();
        diagnostic.clickAssayTest (Clonality_BCell_2);
        diagnostic.clickActivateOrder ();
        diagnostic.clickCancel ();
    }
}
