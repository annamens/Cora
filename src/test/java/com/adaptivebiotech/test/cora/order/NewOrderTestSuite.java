package com.adaptivebiotech.test.cora.order;

import com.adaptivebiotech.common.dto.Patient;
import com.adaptivebiotech.cora.ui.order.Specimen;
import com.adaptivebiotech.cora.ui.shipment.Accession;
import com.adaptivebiotech.cora.ui.shipment.Shipment;
import com.adaptivebiotech.ui.cora.order.Billing;
import com.adaptivebiotech.ui.cora.order.Diagnostic;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.adaptivebiotech.test.utils.PageHelper.Assay.ID_BCell2_IVD;
import static com.adaptivebiotech.test.utils.PageHelper.Assay.MRD_BCell2_IVD;
import static com.adaptivebiotech.test.utils.PageHelper.ChargeType.Client;
import static com.adaptivebiotech.test.utils.PageHelper.ContainerType.Tube;
import static com.adaptivebiotech.test.utils.PageHelper.DeliveryType.CustomerShipment;
import static com.adaptivebiotech.test.utils.PageHelper.PatientStatus.NonHospital;
import static com.adaptivebiotech.test.utils.PageHelper.PatientStatus.Outpatient;
import static com.adaptivebiotech.test.utils.PageHelper.ShippingCondition.Ambient;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.BoneMarrowAspirateSlide;
import static com.adaptivebiotech.test.utils.TestHelper.insurance1;
import static com.adaptivebiotech.test.utils.TestHelper.newPatient;

@Test (groups = { "order", "regression" })
public class NewOrderTestSuite extends OrderTestBase {

    private Diagnostic diagnostic;

    @BeforeMethod
    public void beforeMethod () {
        diagnostic = new Diagnostic ();
    }

    @Test(enabled=false)
    public void clonality_eos_ivd () {
        Patient patient = newPatient ();
        patient.billingType = Client;
        patient.insurance1.hospitalizationStatus = NonHospital;

        // test: new order for Clonality EOD IVD
        prep_new_order (patient);
        diagnostic.isCorrectPage ();
        diagnostic.clickAssayTest (ID_BCell2_IVD);
        diagnostic.activateOrder ();
    }

    @Test(enabled=false)
    public void tracking_eos_ivd () {
        Patient patient = newPatient ();
        patient.billingType = Client;
        patient.insurance1 = insurance1 ();
        patient.insurance1.hospitalizationStatus = Outpatient;

        // test: new order for Tracking EOD IVD
        prep_new_order (patient);
        diagnostic.isCorrectPage ();
        diagnostic.clickAssayTest (MRD_BCell2_IVD);
        diagnostic.activateOrder ();
    }

    private void prep_new_order (Patient patient) {
        Billing billing = new Billing ();
        billing.clickNewDiagnosticOrder ();
        billing.isCorrectPage ();
        billing.selectPhysician (physicianTRF);
        billing.createNewPatient (patient);
        billing.enterPatientICD_Codes (icdCode);
        billing.enterBill (patient);
        billing.clickSave (); // have to Save first before we can set Specimen info

        Specimen specimen = new Specimen ();
        specimen.enterSpecimenDelivery (CustomerShipment);
        specimen.clickEnterSpecimenDetails ();
        specimen.enterSpecimenType (BoneMarrowAspirateSlide);
        specimen.enterCollectionDate (collectionDt);
        specimen.clickSave ();

        add_shipment_and_accession (specimen.getOrderNum ());
    }

    private void add_shipment_and_accession (String orderNum) {
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
        accession.labelingComplete ();
        accession.labelVerificationComplete ();
        accession.clickPass ();
        accession.gotoOrderDetail ();
    }
}
