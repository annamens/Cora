package com.adaptivebiotech.cora.ui.order;

import static java.lang.String.format;
import static org.apache.commons.lang3.EnumUtils.getEnum;
import static org.testng.Assert.assertTrue;
import org.apache.commons.lang3.StringUtils;
import com.adaptivebiotech.cora.dto.Orders.Assay;
import com.adaptivebiotech.cora.dto.Orders.OrderStatus;
import com.adaptivebiotech.cora.ui.CoraPage;

/**
 * @author jpatel
 *
 */
public class OrderHeader extends CoraPage {

    protected final String oEntry              = ".order-entry";
    protected final String oDetail             = ".detail-sections";
    private final String   newAlert            = ".new-alert";
    private final String   alertDashboard      = ".alert-dashboard-main";
    private final String   activeAlertCount    = ".alert-count";
    private final String   orderAlerts         = ".alert-dashboard-modal .alert-category-name";
    private final String   alertDashboardClose = ".alert-dashboard-modal .alert-close";
    private final String   activeAlert         = ".alert-dashboard-modal .alert-status a:nth-of-type(1)";
    private final String   resolvedAlert       = ".alert-dashboard-modal .alert-status a:nth-of-type(2)";

    protected void reportLoading () {
        assertTrue (waitForElementInvisible (".report-loading"));
        assertTrue (waitForElementInvisible ("[ng-show='ctrl.isLoadingPDF']"));
    }

    public void clickOrderStatusTab () {
        assertTrue (click (format (tabBase, "Order Status")));
        pageLoading ();
    }

    public void clickOrderDetailsTab () {
        assertTrue (click (format (tabBase, "Order Details")));
        pageLoading ();
    }

    public void clickReportTab (Assay assay) {
        assertTrue (click ("//a[text()='REPORT | " + assay.test + "']"));
        pageLoading ();
        reportLoading ();
        assertTrue (waitUntilVisible (".order-test-report"));
    }

    public String getheaderOrderNumber () {
        return getText ("[ng-bind='ctrl.orderEntry.order.orderNumber']");
    }

    public OrderStatus getheaderOrderStatus () {
        return getEnum (OrderStatus.class, getText ("[ng-bind='ctrl.orderEntry.order.status']"));
    }

    public boolean isActiveAlertCountPresent () {
        return isElementPresent (alertDashboard + " " + activeAlertCount);
    }

    public void clickAlertDashboard () {
        assertTrue (click (alertDashboard));
    }

    public void clickCloseAlertDashboard () {
        assertTrue (click (alertDashboardClose));
    }

    public void clickOrderAlerts () {
        assertTrue (click (orderAlerts));
    }

    public String getActiveAlert () {
        return StringUtils.substringBetween (getText (activeAlert), "(", ")");
    }

    public String getResolvedAlert () {
        return StringUtils.substringBetween (getText (resolvedAlert), "(", ")");
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

    public void clickPatientNotesIcon () {
        String css = "[ng-click='ctrl.showPatientNotesDialog()']";
        assertTrue (click (css));
        assertTrue (isTextInElement (popupTitle, "Patient Note for Patient "));
    }

    // patient notes popup
    public String getPatientNotesPopup () {
        String css = "[ng-bind=\"ctrl.patient.notes\"]";
        String text = readInput (css);
        return text;
    }
}
