package com.adaptivebiotech.cora.test.shipment;

import static com.adaptivebiotech.test.utils.PageHelper.DeliveryType.CustomerShipment;
import static com.adaptivebiotech.test.utils.PageHelper.ShippingCondition.Ambient;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Containers;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.Billing;
import com.adaptivebiotech.cora.ui.order.Diagnostic;
import com.adaptivebiotech.cora.ui.order.OrderDetailTDetect;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.order.Specimen;
import com.adaptivebiotech.cora.ui.shipment.Accession;
import com.adaptivebiotech.cora.ui.shipment.Shipment;
import com.adaptivebiotech.cora.ui.shipment.ShipmentDetail;
import com.adaptivebiotech.cora.utils.DateUtils;
import com.adaptivebiotech.cora.utils.TestHelper;
import com.adaptivebiotech.test.utils.Logging;
import com.adaptivebiotech.test.utils.PageHelper.Anticoagulant;
import com.adaptivebiotech.test.utils.PageHelper.Assay;
import com.adaptivebiotech.test.utils.PageHelper.ChargeType;
import com.adaptivebiotech.test.utils.PageHelper.ContainerType;
import com.adaptivebiotech.test.utils.PageHelper.SpecimenType;

/**
 * @author jpatel
 *
 */
@Test (groups = "regression")
public class ShipmentLinkTestSuite extends CoraBaseBrowser {

    private OrdersList         ordersList         = new OrdersList ();
    private Diagnostic         diagnostic         = new Diagnostic ();
    private Billing            billing            = new Billing ();
    private Specimen           specimen           = new Specimen ();
    private Shipment           shipment           = new Shipment ();
    private ShipmentDetail     shipmentDetail     = new ShipmentDetail ();
    private Accession          accession          = new Accession ();
    private OrderDetailTDetect orderDetailTDetect = new OrderDetailTDetect ();

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod () {
        new Login ().doLogin ();
        ordersList.isCorrectPage ();
    }

    /**
     * NOTE: SR-T3144
     */
    public void verifyClonoSeqShipmentLink () {
        // create clonoSEQ diagnostic order
        billing.selectNewClonoSEQDiagnosticOrder ();
        billing.isCorrectPage ();

        billing.selectPhysician (TestHelper.physicianTRF ());
        billing.createNewPatient (TestHelper.newPatient ());
        billing.enterPatientICD_Codes ("C90.00");
        billing.clickAssayTest (Assay.ID_BCell2_CLIA);
        billing.selectBilling (ChargeType.NoCharge);
        billing.clickSave ();

        // add specimen details for order
        specimen.enterSpecimenDelivery (CustomerShipment);
        specimen.clickEnterSpecimenDetails ();
        specimen.enterSpecimenType (SpecimenType.Blood);
        specimen.enterAntiCoagulant (Anticoagulant.EDTA);
        specimen.enterCollectionDate (DateUtils.getPastFutureDate (-1));
        specimen.enterOrderNotes ("SR-T3144 Order Notes");
        specimen.clickSave ();

        String orderNum = specimen.getOrderNum ();
        Logging.info ("Order Number: " + orderNum);

        // add diagnostic shipment
        shipment.selectNewDiagnosticShipment ();
        shipment.isDiagnostic ();
        shipment.enterShippingCondition (Ambient);
        shipment.enterOrderNumber (orderNum);
        shipment.selectDiagnosticSpecimenContainerType (ContainerType.SlideBox5);
        shipment.clickSave ();
        shipment.gotoAccession ();
        accession.isCorrectPage ();

        // accession complete
        accession.clickIntakeComplete ();
        accession.labelingComplete ();
        accession.labelVerificationComplete ();
        accession.clickPass ();
        String expIntakeComplete = accession.getIntakeCompleteDate ();

        accession.gotoShipment ();
        shipmentDetail.isCorrectPage ();

        com.adaptivebiotech.cora.dto.Shipment shipment = shipmentDetail.getShipmentDetails ();
        String expSpecimenId = shipmentDetail.getSpecimenId ();
        String expContainerType = shipmentDetail.getContainerType ();
        String expContainerQuantity = shipmentDetail.getContainerQuantity ();
        String expSpecimenApprovalStatus = shipmentDetail.getSpecimenApprovalStatus ();
        String expSpecimenApprovalDateTime = shipmentDetail.getSpecimenApprovalDateTime ();
        Containers expContainers = shipmentDetail.getPrimaryContainers (ContainerType.SlideBox5);
        shipmentDetail.clickOrderNo ();
        diagnostic.isCorrectPage ();

        assertEquals (diagnostic.getShipmentArrivalDate (), shipment.arrivalDate);
        assertEquals (diagnostic.getSpecimenId (), expSpecimenId);
        // TODO make changes as per https://sdlc.dna.corp.adaptivebiotech.com:8443/browse/SR-7887
        // assertEquals (diagnostic.getSpecimenContainerType ().label, expContainerType);
        assertEquals (diagnostic.getSpecimenContainerQuantity (), expContainerQuantity);
        assertEquals (diagnostic.getIntakeCompleteDate (), expIntakeComplete.split (",")[0]);
        assertEquals (diagnostic.getSpecimenApprovalStatus (), expSpecimenApprovalStatus.toUpperCase ());
        assertEquals (diagnostic.getSpecimenApprovalDate (), expSpecimenApprovalDateTime);

        diagnostic.clickShowContainers ();
        Containers actualContainers = diagnostic.getContainers ();
        assertEquals (actualContainers.list.size (), expContainers.list.size ());
        assertEquals (actualContainers.list.get (0).containerNumber, expContainers.list.get (0).containerNumber);

    }

    /**
     * NOTE: SR-T3144
     */
    public void verifyTDetectShipmentLink () {
        // create T-Detect diagnostic order
        orderDetailTDetect.selectNewTDetectDiagnosticOrder ();
        orderDetailTDetect.isCorrectPage ();

        orderDetailTDetect.selectPhysician (TestHelper.physicianTRF ());
        orderDetailTDetect.createNewPatient (TestHelper.newPatient ());
        orderDetailTDetect.clickSave ();

        specimen.enterCollectionDate (DateUtils.getPastFutureDate (-3));
        orderDetailTDetect.clickAssayTest (Assay.COVID19_DX_IVD);
        billing.selectBilling (ChargeType.Client);
        billing.enterPatientAddress (TestHelper.address ());
        billing.clickSave ();

        String orderNum = orderDetailTDetect.getOrderNum ();
        Logging.info ("Order Number: " + orderNum);

        // add diagnostic shipment
        shipment.selectNewDiagnosticShipment ();
        shipment.isDiagnostic ();
        shipment.enterShippingCondition (Ambient);
        shipment.enterOrderNumber (orderNum);
        shipment.selectDiagnosticSpecimenContainerType (ContainerType.SlideBox5);
        shipment.clickSave ();
        shipment.gotoAccession ();
        accession.isCorrectPage ();

        // accession complete
        accession.clickIntakeComplete ();
        accession.labelingComplete ();
        accession.labelVerificationComplete ();
        accession.clickPass ();
        String expIntakeComplete = accession.getIntakeCompleteDate ();

        accession.gotoShipment ();
        shipmentDetail.isCorrectPage ();

        com.adaptivebiotech.cora.dto.Shipment shipment = shipmentDetail.getShipmentDetails ();
        String expSpecimenId = shipmentDetail.getSpecimenId ();
        String expContainerType = shipmentDetail.getContainerType ();
        String expContainerQuantity = shipmentDetail.getContainerQuantity ();
        String expSpecimenApprovalStatus = shipmentDetail.getSpecimenApprovalStatus ();
        String expSpecimenApprovalDateTime = shipmentDetail.getSpecimenApprovalDateTime ();
        Containers expContainers = shipmentDetail.getPrimaryContainers (ContainerType.SlideBox5);
        shipmentDetail.clickOrderNo ();
        diagnostic.isCorrectPage ();

        orderDetailTDetect.clickShipment ();
        orderDetailTDetect.clickShowContainers ();
        assertEquals (diagnostic.getShipmentArrivalDate (), shipment.arrivalDate);
        assertEquals (diagnostic.getSpecimenId (), expSpecimenId);
        // TODO make changes as per https://sdlc.dna.corp.adaptivebiotech.com:8443/browse/SR-7887
        // assertEquals (diagnostic.getSpecimenContainerType ().label, expContainerType);
        assertEquals (diagnostic.getSpecimenContainerQuantity (), expContainerQuantity);
        assertEquals (diagnostic.getIntakeCompleteDate (), expIntakeComplete.split (",")[0]);
        assertEquals (diagnostic.getSpecimenApprovalStatus (), expSpecimenApprovalStatus.toUpperCase ());
        assertEquals (diagnostic.getSpecimenApprovalDate (), expSpecimenApprovalDateTime);

        Containers actualContainers = orderDetailTDetect.getContainers ();
        assertEquals (actualContainers.list.size (), expContainers.list.size ());
        assertEquals (actualContainers.list.get (0).containerNumber, expContainers.list.get (0).containerNumber);

    }

}
