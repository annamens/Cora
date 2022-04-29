/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.ui.order;

import static java.util.stream.Collectors.toList;
import static org.openqa.selenium.Keys.ENTER;
import static org.testng.Assert.assertTrue;
import java.util.List;
import com.adaptivebiotech.cora.dto.Shipment;
import com.adaptivebiotech.cora.ui.CoraPage;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class Batch extends CoraPage {

    public Batch () {
        staticNavBarHeight = 90;
    }

    @Override
    public void isCorrectPage () {
        assertTrue (waitUntilVisible (".salesforce-container"));
        assertTrue (waitUntilVisible (".shipments"));
        assertTrue (waitUntilVisible ("[name='projectType']"));
    }

    public void searchOrder (String ordernum) {
        assertTrue (setText ("[ng-model='ctrl.salesforceId']", ordernum));
        assertTrue (pressKey (ENTER));
        pageLoading ();
    }

    public List <Shipment> getShipments () {
        return waitForElements ("[batch-shipment='shipmentEntry']").stream ().map (el -> {
            Shipment s = new Shipment ();
            s.shipmentNumber = getText (el, "[ng-bind='ctrl.entry.shipment.shipmentNumber']");
            return s;
        }).collect (toList ());
    }
}
