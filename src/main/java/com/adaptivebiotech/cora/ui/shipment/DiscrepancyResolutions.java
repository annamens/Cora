package com.adaptivebiotech.cora.ui.shipment;

import static org.testng.Assert.assertTrue;
import java.util.List;
import org.openqa.selenium.WebElement;
import com.adaptivebiotech.cora.ui.CoraPage;

public class DiscrepancyResolutions extends CoraPage {

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
            waitForAjaxCalls ();
        }

    }

    public void clickSave () {
        String css = "[data-ng-click=\"ctrl.$scope.$broadcast('discrepancy-save')\"]";
        assertTrue (click (css));
        pageLoading ();
    }

}
