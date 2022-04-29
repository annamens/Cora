/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.ui.shipment;

import static org.testng.Assert.assertTrue;
import java.util.List;
import org.openqa.selenium.WebElement;

public class DiscrepancyResolutions extends ShipmentHeader {

    public int getNumberOfDiscrepancies () {
        String css = "[ng-bind=\"ctrl.entry.discrepancies.length\"]";
        String text = getText (css);
        return Integer.parseInt (text);
    }

    public void resolveAllDiscrepancies () {
        String css = "[ng-model=\"discrepancy.status\"]";

        List <WebElement> statusDropdowns = waitForElementsVisible (css);

        for (WebElement statusDropdown : statusDropdowns) {
            assertTrue (clickAndSelectText (statusDropdown, "Resolved - Yes"));
        }

    }

    public void clickSave () {
        String css = "[data-ng-click=\"ctrl.$scope.$broadcast('discrepancy-save')\"]";
        assertTrue (click (css));
        pageLoading ();
    }

}
