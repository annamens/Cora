package com.adaptivebiotech.cora.ui.order;

import static org.testng.Assert.assertTrue;
import com.adaptivebiotech.cora.ui.CoraPage;
import com.adaptivebiotech.test.utils.PageHelper.Assay;

/**
 * @author jpatel
 *
 */
public class OrderHeader extends CoraPage {

    protected final String oEntry   = ".order-entry";
    protected final String oDetail  = ".detail-sections";
    protected final String tabBase  = "//ul[contains(@class, 'nav-tabs')]//*[text()='%s']";
    private final String   newAlert = ".new-alert";

    protected void reportLoading () {
        assertTrue (waitForElementInvisible (".report-loading"));
        assertTrue (waitForElementInvisible ("[ng-show='ctrl.isLoadingPDF']"));
    }

    public void clickOrderStatusTab () {
        String tab = String.format (tabBase, "Order Status");
        assertTrue (click (tab));
        pageLoading ();
    }

    public void clickOrderDetailsTab () {
        String tab = String.format (tabBase, "Order Details");
        assertTrue (click (tab));
        pageLoading ();
    }

    public void clickReportTab (Assay assay) {
        assertTrue (click ("//a[text()='REPORT | " + assay.test + "']"));
        pageLoading ();
        reportLoading ();
        assertTrue (waitUntilVisible (".order-test-report"));
    }

    public void createNewAlert () {
        assertTrue (click (newAlert));
    }

    public String getOrderId () {
        String[] splitUrl = getCurrentUrl ().split ("/");
        return splitUrl[splitUrl.length - 1];
    }

    public String getDueDate () {
        return getText ("[ng-bind^='ctrl.orderEntry.orderTests[0].dueDate']");
    }
}
