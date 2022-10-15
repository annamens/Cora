/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.order.clonoseq;

import static com.adaptivebiotech.cora.dto.Orders.Assay.MRD_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Orders.ChargeType.CommercialInsurance;
import static com.adaptivebiotech.cora.dto.Orders.ChargeType.Medicare;
import static com.adaptivebiotech.cora.dto.Orders.OrderStatus.Cancelled;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.clonoSEQ_trial;
import static com.adaptivebiotech.cora.utils.TestHelper.bloodSpecimen;
import static com.adaptivebiotech.cora.utils.TestHelper.newTrialProtocolPatient;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Orders.Assay;
import com.adaptivebiotech.cora.dto.Orders.Order;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.dto.Specimen;
import com.adaptivebiotech.cora.test.order.NewOrderTestBase;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.NewOrderClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrdersList;

@Test (groups = "regression")
public class NewOrderTestSuite extends NewOrderTestBase {

    private Login            login            = new Login ();
    private OrdersList       ordersList       = new OrdersList ();
    private NewOrderClonoSeq newOrderClonoSeq = new NewOrderClonoSeq ();

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
     * NOTE: SR-T4291
     * 
     * @sdlc.requirements SR-11341
     */
    @Test (groups = "irish-wolfhound")
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
        testLog ("Order 1: Order Status is Cancelled");
    }
}
