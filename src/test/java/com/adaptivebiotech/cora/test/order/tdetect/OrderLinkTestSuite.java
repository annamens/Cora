/**
* Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
*/
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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
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
        validateTabsOrderPage (order, asList (orderDetailsTab));

        shipment.createShipment (order.orderNumber, Tube);
        testLog ("Shipment Created");

        String shipmentId = accession.getShipmentId ();
        validateTabsShipmentPage (shipmentId, accessionTabList);

        validateTabsOrderPage (order, asList (orderDetailsTab));

        validateTabsShipmentPage (shipmentId, accessionTabList);
        accession.clickAddContainerSpecimenDiscrepancy ();
        accession.addDiscrepancy (SpecimenType, "This is a specimen/container discrepancy", CLINICAL_TRIALS);
        accession.clickDiscrepancySave ();
        validateTabsShipmentPage (shipmentId, discrepancyTabList);
        testLog ("Discrepancy created");

        validateTabsOrderPage (order, asList (orderDetailsTab));

        validateTabsShipmentPage (shipmentId, discrepancyTabList);
        accession.clickIntakeComplete ();
        testLog ("Accession - Intake complete");

        validateTabsOrderPage (order, orderDiscrepTabList);

        validateTabsShipmentPage (shipmentId, discrepancyTabList);
        accession.clickLabelingComplete ();
        testLog ("Labelling complete");

        validateTabsOrderPage (order, orderDiscrepTabList);

        validateTabsShipmentPage (shipmentId, discrepancyTabList);
        accession.clickLabelVerificationComplete ();
        testLog ("Label verification complete");

        validateTabsOrderPage (order, orderDiscrepTabList);

        validateTabsShipmentPage (shipmentId, discrepancyTabList);
        assertFalse (accession.isApproveSpecimenEnabled ());
        testLog ("Specimen approval is disabled");

        accession.gotoDiscrepancyResolutions ();
        discrepancyResolutions.resolveAllDiscrepancies ();
        discrepancyResolutions.clickSave ();
        validateTabsShipmentPage (shipmentId, discrepancyTabList);
        testLog ("Resolve discrepancy");

        validateTabsOrderPage (order, orderDiscrepTabList);

        validateTabsShipmentPage (shipmentId, discrepancyTabList);
        accession.clickPass ();
        testLog ("Specimen approval pass");

        validateTabsOrderPage (order, orderDiscrepTabList);

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
        validateTabsOrderPage (order, asList (orderDetailsTab));

        shipment.createShipment (order.orderNumber, Tube);
        testLog ("Shipment Created");

        String shipmentId = accession.getShipmentId ();
        validateTabsShipmentPage (shipmentId, accessionTabList);

        validateTabsOrderPage (order, asList (orderDetailsTab));

        validateTabsShipmentPage (shipmentId, accessionTabList);
        accession.clickIntakeComplete ();
        testLog ("Accession - Intake complete");

        validateTabsOrderPage (order, orderDetailsTabList);

        validateTabsShipmentPage (shipmentId, accessionTabList);
        accession.clickLabelingComplete ();
        testLog ("Labelling complete");

        validateTabsOrderPage (order, orderDetailsTabList);

        validateTabsShipmentPage (shipmentId, accessionTabList);
        accession.clickLabelVerificationComplete ();
        testLog ("Label verification complete");

        validateTabsOrderPage (order, orderDetailsTabList);

        validateTabsShipmentPage (shipmentId, accessionTabList);
        assertTrue (accession.isApproveSpecimenEnabled ());
        testLog ("Specimen approval is enabled");

        validateTabsShipmentPage (shipmentId, accessionTabList);

        validateTabsOrderPage (order, orderDetailsTabList);

        validateTabsShipmentPage (shipmentId, accessionTabList);
        accession.clickPass ();
        testLog ("Specimen approval pass");

        validateTabsOrderPage (order, orderDetailsTabList);

        newOrderTDetect.activateOrder ();
        orderDetailTDetect.gotoOrderDetailsPage (order.id);
        assertEquals (orderDetailTDetect.getTabList (), asList (orderStatusTab, orderDetailsTab));
        testLog ("activate Order");

    }

    private void validateTabsShipmentPage (String shipmentId, List <String> expTabs) {
        accession.gotoAccession (shipmentId);
        assertEquals (accession.getTabList (), expTabs);
        testLog ("Validate Tabs on Shipment Page");
    }

    private void validateTabsOrderPage (Order order, List <String> expTabs) {
        newOrderTDetect.gotoOrderEntry (order.id);
        assertEquals (newOrderTDetect.getTabList (), expTabs);
        testLog ("Validate Tabs on Orders Page");
    }
}
