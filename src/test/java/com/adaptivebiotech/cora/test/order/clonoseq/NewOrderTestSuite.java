/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.order.clonoseq;

import static com.adaptivebiotech.cora.dto.Orders.ChargeType.CommercialInsurance;
import static com.adaptivebiotech.cora.dto.Orders.ChargeType.Medicare;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
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

}
