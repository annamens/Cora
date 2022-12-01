/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.order.clonoseq;

import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Orders.Assay.MRD_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Orders.ChargeType.CommercialInsurance;
import static com.adaptivebiotech.cora.dto.Orders.ChargeType.Medicare;
import static com.adaptivebiotech.cora.dto.Orders.OrderStatus.Cancelled;
import static com.adaptivebiotech.cora.dto.Orders.OrderStatus.Pending;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.clonoSEQ_client;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.clonoSEQ_trial;
import static com.adaptivebiotech.cora.utils.TestHelper.bloodSpecimen;
import static com.adaptivebiotech.cora.utils.TestHelper.newClientPatient;
import static com.adaptivebiotech.cora.utils.TestHelper.newTrialProtocolPatient;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.TestHelper.mapper;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import java.util.List;
import java.util.Map;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Orders.Assay;
import com.adaptivebiotech.cora.dto.Orders.Order;
import com.adaptivebiotech.cora.dto.Orders.OrderProperties;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.dto.Specimen;
import com.adaptivebiotech.cora.test.order.NewOrderTestBase;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.NewOrderClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrderDetail;
import com.adaptivebiotech.cora.ui.order.OrderStatus;
import com.adaptivebiotech.cora.ui.order.OrdersList;

@Test (groups = "regression")
public class NewOrderTestSuite extends NewOrderTestBase {

    private Login            login            = new Login ();
    private OrdersList       ordersList       = new OrdersList ();
    private OrderStatus      orderStatus      = new OrderStatus ();
    private NewOrderClonoSeq newOrderClonoSeq = new NewOrderClonoSeq ();
    private OrderDetail      orderDetail      = new OrderDetail ();

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
     * NOTE: SR-T4291, SR-T4372
     * 
     * @sdlc.requirements SR-11341, SR-13576
     */
    @Test (groups = { "irish-wolfhound", "jack-russell", "smoke" })
    public void verifyCancelOrder () {
        String[] icdCodes = { "V00.218S" };
        Assay assayTest = MRD_BCell2_CLIA;
        Patient patient = newTrialProtocolPatient ();

        // Order 1: Non-Streck, Without Fastlane
        Specimen specimenNonStreck = bloodSpecimen ();
        Order orderOne = newOrderClonoSeq.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_trial),
                                                               patient,
                                                               icdCodes,
                                                               assayTest,
                                                               specimenNonStreck);
        testLog ("Order 1, Non-Streck, Without Fastlane: " + orderOne.orderNumber);
        newOrderClonoSeq.clickCancelOrder ();
        assertFalse (newOrderClonoSeq.isCancelActionDropdownVisible ());
        testLog ("Order 1: Cancel Action dropdown not visible");
        newOrderClonoSeq.cancelOrder ();
        assertEquals (newOrderClonoSeq.getOrderStatus (), Cancelled);
        assertEquals (orderStatus.getOrderTestStatus (), Cancelled);
        testLog ("Order 1: Order Status is Cancelled");
    }

    /**
     * @sdlc.requirements SR-4383:R1
     */
    @Test (groups = "jack-russell")
    public void verifyUndoCancellationMultipleTimes () {
        int repetition = 3;
        Order order = newOrderClonoSeq.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_client),
                                                            newClientPatient (),
                                                            new String[] { "B17.2" },
                                                            null,
                                                            bloodSpecimen ());
        testLog (format ("ClonoSeq Order number without Order Tests is: %s", order.orderNumber));
        for (int i = 1; i <= repetition; i++) {
            cancelAndRestartOrder (order.orderNumber);
        }
        testLog ("ClonoSEQ order cancelled and restarted to pending status " + repetition + " times");
    }

    /**
     * @sdlc.requirements SR-4383:R1
     */
    @Test (groups = "jack-russell")
    public void verifyUndoCancellationAbsentForOrdersWithOrderTests () {
        Order order = newOrderClonoSeq.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_client),
                                                            newClientPatient (),
                                                            new String[] { "B17.2" },
                                                            ID_BCell2_CLIA,
                                                            bloodSpecimen ());
        testLog (format ("ClonoSEQ Order number with Order Tests is: %s", order.orderNumber));
        newOrderClonoSeq.clickAndCancelOrder ();
        orderStatus.isCorrectPage ();
        assertFalse (orderStatus.isOrderActionDotsPresent ());
        testLog ("Restart order button not visible for ClonoSEQ order with order tests");
    }

    private void cancelAndRestartOrder (String orderNumber) {
        newOrderClonoSeq.clickAndCancelOrder ();
        orderStatus.isCorrectPage ();
        orderStatus.clickRestartOrder ();
        orderDetail.isCorrectPage ();
        assertEquals (orderDetail.getOrderStatus (), Pending);
        List <Map <String, Object>> properties = coraDb.executeSelect (format ("select properties from cora.orders where order_number= '%s';",
                                                                               orderNumber));
        OrderProperties cancellationProperties = mapper.readValue (properties.get (0).get ("properties").toString (),
                                                                   OrderProperties.class);
        asList (cancellationProperties).forEach (r -> {
            assertNull (r.CancellationNotes);
            assertNull (r.CancellationReason);
            assertNull (r.CancellationReason2);
            assertNull (r.CancellationReason3);
            assertNull (r.CancellationDateTime);
        });
    }
}
