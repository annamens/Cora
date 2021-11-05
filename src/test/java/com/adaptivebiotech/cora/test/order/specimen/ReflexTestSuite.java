package com.adaptivebiotech.cora.test.order.specimen;

import static com.adaptivebiotech.cora.utils.TestHelper.newPatient;
import static com.adaptivebiotech.test.utils.PageHelper.Anticoagulant.EDTA;
import static com.adaptivebiotech.test.utils.PageHelper.Assay.ID_BCell2_CLIA;
import static com.adaptivebiotech.test.utils.PageHelper.ChargeType.NoCharge;
import static com.adaptivebiotech.test.utils.PageHelper.ContainerType.Tube;
import static com.adaptivebiotech.test.utils.PageHelper.DeliveryType.CustomerShipment;
import static com.adaptivebiotech.test.utils.PageHelper.DeliveryType.Reflex;
import static com.adaptivebiotech.test.utils.PageHelper.ShippingCondition.Ambient;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.Blood;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.debug.OrcaHistory;
import com.adaptivebiotech.cora.ui.order.NewOrderClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.shipment.Accession;
import com.adaptivebiotech.cora.ui.shipment.Shipment;
import com.adaptivebiotech.cora.utils.DateUtils;
import com.adaptivebiotech.cora.utils.TestHelper;

/**
 * Note:
 * - to reuse the same specimenId, it can't be the same ID and MRD test
 * - it must pass LIMS extraction point
 * - the old test can't be active
 */
@Test (enabled = false, groups = "regression")
public class ReflexTestSuite extends CoraBaseBrowser {

    private OrdersList       oList            = new OrdersList ();
    private NewOrderClonoSeq newOrderClonoSeq = new NewOrderClonoSeq ();

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod () {
        new Login ().doLogin ();
        oList.isCorrectPage ();
        oList.selectNewClonoSEQDiagnosticOrder ();

        newOrderClonoSeq.isCorrectPage ();
        newOrderClonoSeq.selectPhysician (TestHelper.physicianTRF ());
        newOrderClonoSeq.createNewPatient (newPatient ());
        newOrderClonoSeq.enterPatientICD_Codes ("A01.02");
        newOrderClonoSeq.clickSave (); // have to Save first before we can set Specimen info
    }

    public void withSpecimenId () {
        newOrderClonoSeq.enterSpecimenDelivery (CustomerShipment);
        newOrderClonoSeq.clickEnterSpecimenDetails ();
        newOrderClonoSeq.enterSpecimenType (Blood);
        newOrderClonoSeq.enterAntiCoagulant (EDTA);
        newOrderClonoSeq.enterCollectionDate (DateUtils.getPastFutureDate (3));
        newOrderClonoSeq.clickSave ();
        String specimenId = newOrderClonoSeq.getSpecimenId ();

        OrcaHistory history = new OrcaHistory ();
        history.gotoOrderDebug (addDiagnosticShipment_and_Activate ());
        history.cancelOrder ();

        newOrderClonoSeq.selectNewClonoSEQDiagnosticOrder ();
        newOrderClonoSeq.isCorrectPage ();
        newOrderClonoSeq.selectPhysician (TestHelper.physicianTRF ());
        newOrderClonoSeq.clickSave ();
        newOrderClonoSeq.enterSpecimenDelivery (Reflex);
        newOrderClonoSeq.findSpecimenId (specimenId);
        newOrderClonoSeq.clickSave ();
        addDiagnosticShipment_and_Activate ();
    }

    private String addDiagnosticShipment_and_Activate () {
        newOrderClonoSeq.billing.selectBilling (NoCharge);
        newOrderClonoSeq.clickSave ();
        String orderNum = newOrderClonoSeq.getOrderNum ();

        Shipment shipment = new Shipment ();
        shipment.selectNewDiagnosticShipment ();
        shipment.isDiagnostic ();
        shipment.enterShippingCondition (Ambient);
        shipment.enterOrderNumber (orderNum);
        shipment.selectDiagnosticSpecimenContainerType (Tube);
        shipment.clickSave ();
        shipment.gotoAccession ();

        Accession accession = new Accession ();
        accession.isCorrectPage ();
        accession.clickIntakeComplete ();
        accession.labelingComplete ();
        accession.labelVerificationComplete ();
        accession.clickPass ();
        accession.gotoOrderDetail ();

        newOrderClonoSeq.isCorrectPage ();
        newOrderClonoSeq.clickAssayTest (ID_BCell2_CLIA);
        newOrderClonoSeq.activateOrder ();
        return newOrderClonoSeq.getSampleName ();
    }
}
