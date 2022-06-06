/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.order.tdetect;

import static com.adaptivebiotech.cora.dto.Containers.ContainerType.Tube;
import static com.adaptivebiotech.cora.dto.Orders.Assay.COVID19_DX_IVD;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.TDetect_client;
import static com.adaptivebiotech.cora.utils.PageHelper.Discrepancy.SpecimenType;
import static com.adaptivebiotech.cora.utils.PageHelper.DiscrepancyAssignee.CLINICAL_TRIALS;
import static com.adaptivebiotech.cora.utils.TestHelper.bloodSpecimen;
import static com.adaptivebiotech.cora.utils.TestHelper.newClientPatient;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import java.util.List;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Orders.Order;
import com.adaptivebiotech.cora.test.order.NewOrderTestBase;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.NewOrderTDetect;
import com.adaptivebiotech.cora.ui.order.OrderDetailTDetect;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.shipment.Accession;
import com.adaptivebiotech.cora.ui.shipment.DiscrepancyResolutions;
import com.adaptivebiotech.cora.ui.shipment.NewShipment;

/**
 * @author jpatel
 *
 */
@Test (groups = { "regression", "tDetect" })
public class OrderLinkTestSuite extends NewOrderTestBase {

    private Login                  login                  = new Login ();
    private OrdersList             ordersList             = new OrdersList ();
    private NewOrderTDetect        newOrderTDetect        = new NewOrderTDetect ();
    private OrderDetailTDetect     orderDetailTDetect     = new OrderDetailTDetect ();
    private NewShipment            shipment               = new NewShipment ();
    private Accession              accession              = new Accession ();
    private DiscrepancyResolutions discrepancyResolutions = new DiscrepancyResolutions ();

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod () {
        login.doLogin ();
        ordersList.isCorrectPage ();
    }

    /**
     * NOTE: SR-T4182
     * 
     * @sdlc.requirements SR-10524:R1
     */
    @Test (groups = "fox-terrier")
    public void validateOrderTabsWithDiscrepancy () {
        Order order = newOrderTDetect.createTDetectOrder (coraApi.getPhysician (TDetect_client),
                                                          newClientPatient (),
                                                          null,
                                                          bloodSpecimen ().collectionDate.toString (),
                                                          COVID19_DX_IVD);
        assertEquals (newOrderTDetect.getTabList (), asList (orderDetailsTab));

        shipment.createShipment (order.orderNumber, Tube);
        String shipmentId = accession.getShipmentId ();
        assertEquals (accession.getTabList (), accessionTabList);
        testLog ("Shipment Created");

        validateTabsOrderPage (order, asList (orderDetailsTab));

        accession.gotoAccession (shipmentId);
        accession.clickAddContainerSpecimenDiscrepancy ();
        accession.addDiscrepancy (SpecimenType, "This is a specimen/container discrepancy", CLINICAL_TRIALS);
        accession.clickDiscrepancySave ();
        assertEquals (accession.getTabList (), discrepancyTabList);
        testLog ("Discrepancy created");

        validateTabsOrderPage (order, asList (orderDetailsTab));

        accession.gotoAccession (shipmentId);
        accession.clickIntakeComplete ();
        accession.clickLabelingComplete ();
        accession.clickLabelVerificationComplete ();
        accession.gotoDiscrepancyResolutions ();
        discrepancyResolutions.resolveAllDiscrepancies ();
        discrepancyResolutions.clickSave ();
        testLog ("Resolve discrepancy");

        discrepancyResolutions.clickAccessionTab ();
        accession.clickPass ();
        assertEquals (accession.getTabList (), discrepancyTabList);
        testLog ("Specimen approval pass");

        validateTabsOrderPage (order, orderDiscrepTabList);
        newOrderTDetect.clickAccessionTab ();
        accession.isCorrectPage ();
        newOrderTDetect.clickDiscrepancyResolutionsTab ();
        discrepancyResolutions.isCorrectPage ();
        testLog ("Validate Accession and Discrepancy tabs open in same window");

        newOrderTDetect.clickOrderDetailsTab ();
        newOrderTDetect.isCorrectPage ();
        newOrderTDetect.activateOrder ();
        orderDetailTDetect.gotoOrderDetailsPage (order.id);
        assertEquals (orderDetailTDetect.getTabList (), asList (orderStatusTab, orderDetailsTab));
        testLog ("activate Order");

    }

    /**
     * NOTE: SR-T4182
     * 
     * @sdlc.requirements SR-10524:R1
     */
    @Test (groups = "fox-terrier")
    public void validateOrderTabsWithoutDiscrepancy () {
        Order order = newOrderTDetect.createTDetectOrder (coraApi.getPhysician (TDetect_client),
                                                          newClientPatient (),
                                                          null,
                                                          bloodSpecimen ().collectionDate.toString (),
                                                          COVID19_DX_IVD);
        assertEquals (newOrderTDetect.getTabList (), asList (orderDetailsTab));

        shipment.createShipment (order.orderNumber, Tube);
        String shipmentId = accession.getShipmentId ();
        assertEquals (accession.getTabList (), accessionTabList);
        testLog ("Shipment Created");

        validateTabsOrderPage (order, asList (orderDetailsTab));

        accession.gotoAccession (shipmentId);
        accession.clickIntakeComplete ();
        accession.clickLabelingComplete ();
        accession.clickLabelVerificationComplete ();
        accession.clickPass ();
        assertEquals (accession.getTabList (), accessionTabList);
        testLog ("Specimen approval pass");

        validateTabsOrderPage (order, orderDetailsTabList);
        newOrderTDetect.clickAccessionTab ();
        accession.isCorrectPage ();
        testLog ("Validate Accession tab opens in same window");

        newOrderTDetect.clickOrderDetailsTab ();
        newOrderTDetect.isCorrectPage ();
        newOrderTDetect.activateOrder ();
        orderDetailTDetect.gotoOrderDetailsPage (order.id);
        assertEquals (orderDetailTDetect.getTabList (), asList (orderStatusTab, orderDetailsTab));
        testLog ("activate Order");

    }

    private void validateTabsOrderPage (Order order, List <String> expTabs) {
        newOrderTDetect.gotoOrderEntry (order.id);
        assertEquals (newOrderTDetect.getTabList (), expTabs);
        testLog ("Validate Tabs on Orders Page");
    }
}
