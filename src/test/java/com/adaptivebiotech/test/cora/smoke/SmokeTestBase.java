package com.adaptivebiotech.test.cora.smoke;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.ui.cora.order.Diagnostic;

public class SmokeTestBase extends CoraBaseBrowser {
    protected Diagnostic diagnostic;

    protected void verifyPhysicianName (String ExpectedName) {
        assertEquals (diagnostic.getProviderName (), ExpectedName);
    }

    // Verify Diagnostic Order page displays an order number, D-######, in the order
    // header.
    protected void verifyOrderNumber () {

        String orderNumber = diagnostic.getOrderNum ();
        assertTrue (orderNumber.matches ("D-\\d{6}"));
    }

}
