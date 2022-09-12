/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.ui.order;

import static org.testng.Assert.assertTrue;
import java.util.List;
import org.openqa.selenium.WebElement;
import com.adaptivebiotech.cora.ui.CoraPage;

/**
 * @author Harry Soehalim <a href=
 *         "mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class OrderTestsList extends CoraPage {

    private final String confirmRequeueButton = "[data-ng-click=\"ctrl.confirm()\"]";

    public OrderTestsList () {
        staticNavBarHeight = 90;
    }

    @Override
    public void isCorrectPage () {
        assertTrue (waitUntilVisible (".active[title='Order Tests']"));
        pageLoading ();
    }

    public void clickQueriesButton () {
        String css = "[ng-click=\"ctrl.queryClick()\"]";
        assertTrue (click (css));
    }

    public void clickSkuProperties () {
        String sku = "//a[@href='/cora/testproperties']";
        assertTrue (click (sku));
        pageLoading ();
        waitForElementsVisible ("h4[class='ng-binding']");
    }

    public void querySamplesPendingRequeue () {
        clickQueriesButton ();
        String css = "[href=\"/cora/requeues\"]";
        assertTrue (click (css));
        pageLoading ();
        waitForElementVisible (confirmRequeueButton);
        waitForElementVisible ("[data-ng-click=\"ctrl.fail()\"]");
    }

    public void clickCsvDownloadButton () {
        String csv = "//a[@href='/cora/api/v1/tests/downloadSearch?sort=name&ascending=true']";
        assertTrue (click (csv));
    }

    public void requeueSample (String flowcellId) {
        List <WebElement> requeueItems = waitForElementsVisible ("[ng-repeat-start=\"detail in ctrl.requeueDetails\"]");
        for (WebElement row : requeueItems) {
            String rowFlowcellId = getText (row, "[ng-bind=\"::detail.flowcell\"]");
            if (flowcellId.equals (rowFlowcellId)) {
                assertTrue (click (row, "input"));
            }
        }
        assertTrue (click (confirmRequeueButton));
        waitForElementVisible (".confirm-requeue-modal-dialog");
        assertTrue (click ("[ng-click=\"ctrl.ok()\"]"));
        pageLoading ();
    }
}
