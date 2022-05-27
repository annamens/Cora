/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.ui.hl7;

import static java.lang.String.format;
import static org.testng.Assert.fail;
import com.adaptivebiotech.cora.ui.CoraPage;
import com.seleniumfy.test.utils.Timeout;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class MirthGateway extends CoraPage {

    public void waitFor (String orderId) {
        String hl7 = format ("a[href*='%s-gatewayMessage.hl7']", orderId);
        Timeout timer = new Timeout (millisDuration, millisPoll);
        boolean found = false;
        while (!timer.Timedout () && ! (found = isElementPresent (hl7))) {
            timer.Wait ();
            refresh ();
        }
        if (!found)
            fail ("unable to located hl7 file for orderId: " + orderId);
    }
}
