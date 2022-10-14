/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.ui.shipment;

import static java.lang.String.format;
import static org.testng.Assert.assertTrue;
import java.util.List;
import java.util.UUID;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.Color;
import com.adaptivebiotech.cora.dto.Element;
import com.adaptivebiotech.cora.utils.PageHelper.Discrepancy;

public class DiscrepancyResolutions extends ShipmentHeader {

    public DiscrepancyResolutions () {
        staticNavBarHeight = 195;
    }

    @Override
    public void isCorrectPage () {
        assertTrue (isTextInElement ("[role='tablist'] .active:nth-child(3)", "DISCREPANCY RESOLUTIONS"));
        pageLoading ();
    }

    @Override
    public void gotoDiscrepancy (UUID shipmentId) {
        super.gotoDiscrepancy (shipmentId);
        isCorrectPage ();
    }

    public int getNumberOfDiscrepancies () {
        String css = "[ng-bind=\"ctrl.entry.discrepancies.length\"]";
        String text = getText (css);
        return Integer.parseInt (text);
    }

    public void resolveDiscrepancy (Discrepancy discrepancy) {
        String css = "//*[text()='%s']//ancestor::div[contains(@class,'discrepancies-row')]//*[@ng-model='discrepancy.status']";
        assertTrue (clickAndSelectText (format (css, discrepancy.text), "Resolved - Yes"));
    }

    public Element getDiscrepancyHoldType (Discrepancy discrepancy) {
        Element el = new Element ();
        String xpath = format ("//*[text()='%s']/following-sibling::discrepancy-severity", discrepancy.text);
        el.text = getText (xpath);
        el.color = Color.fromString (getCssValue (xpath + "//*[@ng-class='ctrl.discrepancyTypeClass']",
                                                  "background-color"))
                        .asHex ();
        return el;
    }

    public void resolveAllDiscrepancies () {
        String css = "[ng-model=\"discrepancy.status\"]";

        List <WebElement> statusDropdowns = waitForElementsVisible (css);

        for (WebElement statusDropdown : statusDropdowns) {
            assertTrue (clickAndSelectText (statusDropdown, "Resolved - Yes"));
        }
        clickSave ();
    }

    public void clickSave () {
        String css = "[data-ng-click=\"ctrl.$scope.$broadcast('discrepancy-save')\"]";
        assertTrue (click (css));
        transactionInProgress ();
    }

}
