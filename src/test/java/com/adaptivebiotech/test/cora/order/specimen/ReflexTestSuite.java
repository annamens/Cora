package com.adaptivebiotech.test.cora.order.specimen;

import static com.adaptivebiotech.test.utils.PageHelper.Anticoagulant.EDTA;
import static com.adaptivebiotech.test.utils.PageHelper.Assay.ID_BCell2_CLIA;
import static com.adaptivebiotech.test.utils.PageHelper.ChargeType.NoCharge;
import static com.adaptivebiotech.test.utils.PageHelper.ContainerType.Tube;
import static com.adaptivebiotech.test.utils.PageHelper.DeliveryType.CustomerShipment;
import static com.adaptivebiotech.test.utils.PageHelper.DeliveryType.Reflex;
import static com.adaptivebiotech.test.utils.PageHelper.ShippingCondition.Ambient;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.Blood;
import static com.adaptivebiotech.test.utils.TestHelper.newPatient;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.test.cora.order.OrderTestBase;
import com.adaptivebiotech.ui.cora.CoraPage;
import com.adaptivebiotech.ui.cora.order.Billing;
import com.adaptivebiotech.ui.cora.order.Diagnostic;
import com.adaptivebiotech.cora.ui.order.Specimen;
import com.adaptivebiotech.cora.ui.shipment.Accession;
import com.adaptivebiotech.cora.ui.shipment.Shipment;
import com.adaptivebiotech.ui.cora.workflow.History;

/**
 * Note:
 * - to reuse the same specimenId, it can't be the same ID and MRD test
 * - it must pass LIMS extraction point
 * - the old test can't be active
 */
@Test (enabled = false, groups = { "regression" })
public class ReflexTestSuite extends OrderTestBase {

    @BeforeMethod
    public void beforeMethod () {
        new CoraPage ().clickNewDiagnosticOrder ();
        Diagnostic diagnostic = new Diagnostic ();
        diagnostic.isCorrectPage ();
        diagnostic.selectPhysician (physicianTRF);
        diagnostic.createNewPatient (newPatient ());
        diagnostic.enterPatientICD_Codes (icdCode);
        diagnostic.clickSave (); // have to Save first before we can set Specimen info
    }

    public void withSpecimenId () {
        Specimen specimen = new Specimen ();
        specimen.enterSpecimenDelivery (CustomerShipment);
        specimen.clickEnterSpecimenDetails ();
        specimen.enterSpecimenType (Blood);
        specimen.enterAntiCoagulant (EDTA);
        specimen.enterCollectionDate (collectionDt);
        specimen.clickSave ();
        String specimenId = specimen.getSpecimenId ();

        History history = new History ();
        history.gotoOrderDebug (addDiagnosticShipment_and_Activate ());
        history.cancelOrder ();

        specimen.clickNewDiagnosticOrder ();
        specimen.isCorrectPage ();
        specimen.selectPhysician (physicianTRF);
        specimen.clickSave ();
        specimen.enterSpecimenDelivery (Reflex);
        specimen.findSpecimenId (specimenId);
        specimen.clickSave ();
        addDiagnosticShipment_and_Activate ();
    }

    private String addDiagnosticShipment_and_Activate () {
        Billing billing = new Billing ();
        billing.selectBilling (NoCharge);
        billing.clickSave ();
        String orderNum = billing.getOrderNum ();

        Shipment shipment = new Shipment ();
        shipment.clickNewDiagnosticShipment ();
        shipment.isCorrectPage ();
        shipment.enterShippingCondition (Ambient);
        shipment.enterOrderNumber (orderNum);
        shipment.enterDiagnosticSpecimenContainerType (Tube);
        shipment.clickSave ();
        shipment.gotoAccession ();

        Accession accession = new Accession ();
        accession.isCorrectPage ();
        accession.clickIntakeComplete ();
        accession.labelingComplete ();
        accession.labelVerificationComplete ();
        accession.clickPass ();
        accession.gotoOrderDetail ();

        Diagnostic diagnostic = new Diagnostic ();
        diagnostic.isCorrectPage ();
        diagnostic.clickAssayTest (ID_BCell2_CLIA);
        diagnostic.activateOrder ();
        return diagnostic.getSampleName ();
    }
}
