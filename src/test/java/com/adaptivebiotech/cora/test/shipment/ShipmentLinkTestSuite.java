package com.adaptivebiotech.cora.test.shipment;

import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.non_CLEP_clonoseq;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Containers;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.NewOrderClonoSeq;
import com.adaptivebiotech.cora.ui.order.NewOrderTDetect;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.shipment.Accession;
import com.adaptivebiotech.cora.ui.shipment.NewShipment;
import com.adaptivebiotech.cora.ui.shipment.ShipmentDetail;
import com.adaptivebiotech.cora.utils.DateUtils;
import com.adaptivebiotech.cora.utils.TestHelper;
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

    private Login            login           = new Login ();
    private OrdersList       ordersList      = new OrdersList ();
    private NewOrderClonoSeq diagnostic      = new NewOrderClonoSeq ();
    private NewShipment      shipment        = new NewShipment ();
    private ShipmentDetail   shipmentDetail  = new ShipmentDetail ();
    private Accession        accession       = new Accession ();
    private NewOrderTDetect  newOrderTDetect = new NewOrderTDetect ();

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod () {
        login.doLogin ();
        ordersList.isCorrectPage ();
        coraApi.login ();
    }

    /**
     * NOTE: SR-T3144
     */
    public void verifyClonoSeqShipmentLink () {
        // create clonoSEQ diagnostic order
        String orderNum = diagnostic.createClonoSeqOrder (coraApi.getPhysician (non_CLEP_clonoseq),
                                                          TestHelper.newPatient (),
                                                          new String[] { "C90.00" },
                                                          Assay.ID_BCell2_CLIA,
                                                          ChargeType.NoCharge,
                                                          SpecimenType.Blood,
                                                          null,
                                                          Anticoagulant.EDTA);

        // add diagnostic shipment
        shipment.createShipment (orderNum, ContainerType.SlideBox5);

        // accession complete
        accession.isCorrectPage ();
        accession.clickIntakeComplete ();
        accession.clickLabelingComplete ();
        accession.clickLabelVerificationComplete ();
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
        String orderNum = newOrderTDetect.createTDetectOrder (coraApi.getPhysician (non_CLEP_clonoseq),
                                                              TestHelper.newPatient (),
                                                              new String[] {},
                                                              DateUtils.getPastFutureDate (-1),
                                                              Assay.COVID19_DX_IVD,
                                                              ChargeType.Client,
                                                              TestHelper.getRandomAddress ());

        // add diagnostic shipment
        shipment.createShipment (orderNum, ContainerType.SlideBox5);

        // accession complete
        accession.isCorrectPage ();
        accession.clickIntakeComplete ();
        accession.clickLabelingComplete ();
        accession.clickLabelVerificationComplete ();
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
        newOrderTDetect.isCorrectPage ();

        newOrderTDetect.clickShipment ();
        newOrderTDetect.clickShowContainers ();
        assertEquals (newOrderTDetect.getShipmentArrivalDate (), shipment.arrivalDate);
        assertEquals (newOrderTDetect.getSpecimenId (), expSpecimenId);
        // TODO make changes as per https://sdlc.dna.corp.adaptivebiotech.com:8443/browse/SR-7887
        // assertEquals (newOrderTDetect.getSpecimenContainerType ().label, expContainerType);
        assertEquals (newOrderTDetect.getSpecimenContainerQuantity (), expContainerQuantity);
        assertEquals (newOrderTDetect.getIntakeCompleteDate (), expIntakeComplete.split (",")[0]);
        assertEquals (newOrderTDetect.getSpecimenApprovalStatus (), expSpecimenApprovalStatus.toUpperCase ());
        assertEquals (newOrderTDetect.getSpecimenApprovalDate (), expSpecimenApprovalDateTime);

        Containers actualContainers = newOrderTDetect.getContainers ();
        assertEquals (actualContainers.list.size (), expContainers.list.size ());
        assertEquals (actualContainers.list.get (0).containerNumber, expContainers.list.get (0).containerNumber);

    }

}
