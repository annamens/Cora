/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.shipment;

import static com.adaptivebiotech.cora.dto.Containers.ContainerType.Slide;
import static com.adaptivebiotech.cora.dto.Containers.ContainerType.SlideBox5;
import static com.adaptivebiotech.cora.dto.Orders.Assay.COVID19_DX_IVD;
import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.non_CLEP_clonoseq;
import static com.adaptivebiotech.cora.utils.TestHelper.bloodSpecimen;
import static com.adaptivebiotech.cora.utils.TestHelper.newClientPatient;
import static com.adaptivebiotech.cora.utils.TestHelper.newNoChargePatient;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import java.time.LocalDateTime;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Containers;
import com.adaptivebiotech.cora.dto.Containers.ContainerType;
import com.adaptivebiotech.cora.dto.Orders.Order;
import com.adaptivebiotech.cora.dto.Shipment;
import com.adaptivebiotech.cora.dto.Specimen.SpecimenStatus;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.NewOrderClonoSeq;
import com.adaptivebiotech.cora.ui.order.NewOrderTDetect;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.shipment.Accession;
import com.adaptivebiotech.cora.ui.shipment.NewShipment;
import com.adaptivebiotech.cora.ui.shipment.ShipmentDetail;

/**
 * @author jpatel
 *
 */
@Test (groups = "regression")
public class ShipmentLinkTestSuite extends CoraBaseBrowser {

    private Login            login            = new Login ();
    private OrdersList       ordersList       = new OrdersList ();
    private NewOrderClonoSeq newOrderClonoSeq = new NewOrderClonoSeq ();
    private NewOrderTDetect  newOrderTDetect  = new NewOrderTDetect ();
    private NewShipment      shipment         = new NewShipment ();
    private ShipmentDetail   shipmentDetail   = new ShipmentDetail ();
    private Accession        accession        = new Accession ();

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod () {
        login.doLogin ();
        ordersList.isCorrectPage ();
    }

    /**
     * NOTE: SR-T3144
     */
    public void verifyClonoSeqShipmentLink () {
        // create clonoSEQ diagnostic order
        Order order = newOrderClonoSeq.createClonoSeqOrder (coraApi.getPhysician (non_CLEP_clonoseq),
                                                            newNoChargePatient (),
                                                            new String[] { "C90.00" },
                                                            ID_BCell2_CLIA,
                                                            bloodSpecimen ());

        // add diagnostic shipment
        shipment.createShipment (order.orderNumber, SlideBox5);

        // accession complete
        accession.isCorrectPage ();
        accession.clickIntakeComplete ();
        accession.clickLabelingComplete ();
        accession.clickLabelVerificationComplete ();
        accession.clickPass ();
        LocalDateTime expIntakeComplete = accession.getIntakeCompleteDate ();

        accession.clickShipmentTab ();
        shipmentDetail.isCorrectPage ();

        Shipment shipment = shipmentDetail.getShipmentDetails ();
        String expSpecimenId = shipmentDetail.getSpecimenId ();
        ContainerType expContainerType = shipmentDetail.getContainerType ();
        int expContainerQuantity = shipmentDetail.getContainerQuantity ();
        SpecimenStatus expSpecimenApprovalStatus = shipmentDetail.getSpecimenApprovalStatus ();
        LocalDateTime expSpecimenApprovalDateTime = shipmentDetail.getSpecimenApprovalDateTime ();
        Containers expContainers = shipmentDetail.getPrimaryContainers (SlideBox5);
        shipmentDetail.clickOrderNumber ();
        newOrderClonoSeq.isCorrectPage ();
        assertEquals (newOrderClonoSeq.getShipmentArrivalDate (), shipment.arrivalDate);
        assertEquals (newOrderClonoSeq.getSpecimenId (), expSpecimenId);
        assertNotEquals (newOrderClonoSeq.getSpecimenContainerType (), expContainerType);
        assertEquals (newOrderClonoSeq.getSpecimenContainerType (), Slide);
        assertEquals (newOrderClonoSeq.getSpecimenContainerQuantity ().intValue (), expContainerQuantity);
        assertEquals (newOrderClonoSeq.getIntakeCompleteDate (), expIntakeComplete);
        assertEquals (newOrderClonoSeq.getSpecimenApprovalStatus (), expSpecimenApprovalStatus);
        assertEquals (newOrderClonoSeq.getSpecimenApprovalDate (), expSpecimenApprovalDateTime);

        Containers actualContainers = newOrderClonoSeq.getContainers ();
        assertEquals (actualContainers.list.size (), expContainers.list.size ());
        assertEquals (actualContainers.list.get (0).containerNumber, expContainers.list.get (0).containerNumber);
    }

    /**
     * NOTE: SR-T3144
     */
    public void verifyTDetectShipmentLink () {
        // create T-Detect diagnostic order
        Order order = newOrderTDetect.createTDetectOrder (coraApi.getPhysician (non_CLEP_clonoseq),
                                                          newClientPatient (),
                                                          null,
                                                          COVID19_DX_IVD,
                                                          bloodSpecimen ());

        // add diagnostic shipment
        shipment.createShipment (order.orderNumber, SlideBox5);

        // accession complete
        accession.isCorrectPage ();
        accession.clickIntakeComplete ();
        accession.clickLabelingComplete ();
        accession.clickLabelVerificationComplete ();
        accession.clickPass ();
        LocalDateTime expIntakeComplete = accession.getIntakeCompleteDate ();

        accession.clickShipmentTab ();
        shipmentDetail.isCorrectPage ();

        Shipment shipment = shipmentDetail.getShipmentDetails ();
        String expSpecimenId = shipmentDetail.getSpecimenId ();
        ContainerType expContainerType = shipmentDetail.getContainerType ();
        int expContainerQuantity = shipmentDetail.getContainerQuantity ();
        SpecimenStatus expSpecimenApprovalStatus = shipmentDetail.getSpecimenApprovalStatus ();
        LocalDateTime expSpecimenApprovalDateTime = shipmentDetail.getSpecimenApprovalDateTime ();
        Containers expContainers = shipmentDetail.getPrimaryContainers (SlideBox5);
        shipmentDetail.clickOrderNumber ();
        newOrderTDetect.isCorrectPage ();
        assertEquals (newOrderTDetect.getShipmentArrivalDate (), shipment.arrivalDate);
        assertEquals (newOrderTDetect.getSpecimenId (), expSpecimenId);
        assertNotEquals (newOrderTDetect.getSpecimenContainerType (), expContainerType);
        assertEquals (newOrderTDetect.getSpecimenContainerType (), Slide);
        assertEquals (newOrderTDetect.getSpecimenContainerQuantity ().intValue (), expContainerQuantity);
        assertEquals (newOrderTDetect.getIntakeCompleteDate (), expIntakeComplete);
        assertEquals (newOrderTDetect.getSpecimenApprovalStatus (), expSpecimenApprovalStatus);
        assertEquals (newOrderTDetect.getSpecimenApprovalDate (), expSpecimenApprovalDateTime);

        Containers actualContainers = newOrderTDetect.getContainers ();
        assertEquals (actualContainers.list.size (), expContainers.list.size ());
        assertEquals (actualContainers.list.get (0).containerNumber, expContainers.list.get (0).containerNumber);
    }
}
