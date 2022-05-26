/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.order.clonoseq;

import static com.adaptivebiotech.cora.dto.Containers.ContainerType.Tube;
import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Orders.ChargeType.CommercialInsurance;
import static com.adaptivebiotech.cora.dto.Orders.ChargeType.Medicare;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.clonoSEQ_client;
import static com.adaptivebiotech.cora.utils.PageHelper.Discrepancy.SpecimenType;
import static com.adaptivebiotech.cora.utils.PageHelper.DiscrepancyAssignee.CLINICAL_TRIALS;
import static com.adaptivebiotech.cora.utils.TestHelper.bloodSpecimen;
import static com.adaptivebiotech.cora.utils.TestHelper.newClientPatient;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static java.lang.String.format;
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
import com.adaptivebiotech.cora.ui.order.NewOrderClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrderDetailClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.shipment.Accession;
import com.adaptivebiotech.cora.ui.shipment.DiscrepancyResolutions;
import com.adaptivebiotech.cora.ui.shipment.NewShipment;

@Test (groups = "regression")
public class NewOrderTestSuite extends NewOrderTestBase {

    private Login                  login                  = new Login ();
    private OrdersList             ordersList             = new OrdersList ();
    private NewOrderClonoSeq       newOrderClonoSeq       = new NewOrderClonoSeq ();
    private OrderDetailClonoSeq    orderDetailClonoSeq    = new OrderDetailClonoSeq ();
    private NewShipment            shipment               = new NewShipment ();
    private Accession              accession              = new Accession ();
    private DiscrepancyResolutions discrepancyResolutions = new DiscrepancyResolutions ();

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod () {
        login.doLogin ();
        ordersList.isCorrectPage ();
    }

    public void sections_order () {
        newOrderClonoSeq.selectNewClonoSEQDiagnosticOrder ();
        newOrderClonoSeq.isCorrectPage ();
        assertEquals (newOrderClonoSeq.getSectionHeaders (), headers);
        testLog ("found the Order Authorization section below the Billing section of the clonoSEQ order form");
    }

    /**
     * @sdlc.requirements SR-8396:R2
     */
    @Test (groups = "dingo")
    public void billing_questions () {
        String log = "the qualifying questionnaires input fields are hidden for '%s'";

        newOrderClonoSeq.selectNewClonoSEQDiagnosticOrder ();
        newOrderClonoSeq.isCorrectPage ();
        newOrderClonoSeq.billing.selectBilling (CommercialInsurance);
        assertFalse (newOrderClonoSeq.billing.isBillingQuestionsVisible ());
        testLog (format (log, CommercialInsurance.label));

        newOrderClonoSeq.billing.selectBilling (Medicare);
        assertFalse (newOrderClonoSeq.billing.isBillingQuestionsVisible ());
        testLog (format (log, Medicare.label));
    }

    /**
     * NOTE: SR-T4182
     * 
     * @sdlc.requirements SR-10524:R1
     */
    @Test (groups = "fox-terrier")
    public void validateOrderTabsWithDiscrepancy () {
        Order order = newOrderClonoSeq.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_client),
                                                            newClientPatient (),
                                                            new String[] { "C90.00" },
                                                            ID_BCell2_CLIA,
                                                            bloodSpecimen ());
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

        newOrderClonoSeq.activateOrder ();
        orderDetailClonoSeq.isCorrectPage ();
        assertEquals (orderDetailClonoSeq.getTabList (), asList (orderStatusTab, orderDetailsTab));
        testLog ("activate Order");

    }

    /**
     * NOTE: SR-T4182
     * 
     * @sdlc.requirements SR-10524:R1
     */
    @Test (groups = "fox-terrier")
    public void validateOrderTabsWithoutDiscrepancy () {
        Order order = newOrderClonoSeq.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_client),
                                                            newClientPatient (),
                                                            new String[] { "C90.00" },
                                                            ID_BCell2_CLIA,
                                                            bloodSpecimen ());
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

        newOrderClonoSeq.activateOrder ();
        orderDetailClonoSeq.isCorrectPage ();
        assertEquals (orderDetailClonoSeq.getTabList (), asList (orderStatusTab, orderDetailsTab));
        testLog ("activate Order");

    }

    private void validateTabsShipmentPage (String shipmentId, List <String> expTabs) {
        accession.gotoAccession (shipmentId);
        assertEquals (accession.getTabList (), expTabs);
        testLog ("Validate Tabs on Shipment Page");
    }

    private void validateTabsOrderPage (Order order, List <String> expTabs) {
        newOrderClonoSeq.gotoOrderEntry (order.id);
        assertEquals (newOrderClonoSeq.getTabList (), expTabs);
        testLog ("Validate Tabs on Orders Page");
    }
}
