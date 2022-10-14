/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.ui.order;

import static org.testng.Assert.assertTrue;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.openqa.selenium.WebElement;
import com.adaptivebiotech.cora.dto.Orders.SkuProperties;
import com.adaptivebiotech.cora.ui.CoraPage;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class OrderTestsList extends CoraPage {

    private final String confirmRequeueButton = "[data-ng-click='ctrl.confirm()']";
    private final String downloadCSVbutton    = ".download-list .glyphicon-save";
    private final String skuColumnNames       = "//th//span";

    public OrderTestsList () {
        staticNavBarHeight = 50;
    }

    @Override
    public void isCorrectPage () {
        assertTrue (waitUntilVisible (".active[title='Order Tests']"));
        pageLoading ();
    }

    public void clickQueriesButton () {
        String css = "[ng-click='ctrl.queryClick()']";
        assertTrue (click (css));
    }

    public void clickSKUproperties () {
        String sku = "//a[text()='SKU Properties']";
        assertTrue (click (sku));
        pageLoading ();
        assertTrue (waitUntilVisible (".container-fluid"));
        assertTrue (waitUntilVisible (downloadCSVbutton));
    }

    public void querySamplesPendingRequeue () {
        clickQueriesButton ();
        String css = "[href=\"/cora/requeues\"]";
        assertTrue (click (css));
        pageLoading ();
        waitForElementVisible (confirmRequeueButton);
        waitForElementVisible ("[data-ng-click=\"ctrl.fail()\"]");
    }

    public void clickCSVdownloadButton () {
        assertTrue (click (downloadCSVbutton));
    }

    public List <String> listOfSkuColumnNames () {
        List <WebElement> skuColumns = waitForElements (skuColumnNames);
        List <String> skuColumnNames = new ArrayList <> ();
        for (WebElement skuColumn : skuColumns) {
            skuColumnNames.add (getText (skuColumn));
        }
        return skuColumnNames;
    }


    public boolean verifySkuNames (List <String> skuNames) {
        HashSet <String> skuNameSet = new HashSet <> ();
        for (SkuProperties skuProperty : SkuProperties.values ()) {
            skuNameSet.add (skuProperty.skupropertyName);
        }
        for (String skuName : skuNames) {
            assertTrue (skuNameSet.contains (skuName));;
        }

        return true;
    }

}
