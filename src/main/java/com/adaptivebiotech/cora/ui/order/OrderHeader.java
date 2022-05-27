/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.ui.order;

import static com.adaptivebiotech.cora.dto.Orders.OrderStatus.Active;
import static com.adaptivebiotech.cora.dto.Orders.OrderStatus.FailedActivation;
import static java.lang.String.format;
import static org.apache.commons.lang3.EnumUtils.getEnum;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static org.apache.commons.lang3.StringUtils.substringBetween;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import com.adaptivebiotech.cora.dto.Orders.Assay;
import com.adaptivebiotech.cora.dto.Orders.OrderStatus;
import com.adaptivebiotech.cora.ui.CoraPage;
import com.seleniumfy.test.utils.Timeout;

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
        return getText ("//*[label[@id='order-number-text']]//span");
    }

    public OrderStatus getOrderStatus () {
        return getEnum (OrderStatus.class, getText ("//*[text()='Status']/..//span"));
    }

    public void waitUntilActivated () {
        Timeout timer = new Timeout (millisDuration * 10, millisPoll * 2);
        OrderStatus orderStatus = getOrderStatus ();
        while (!timer.Timedout () && !orderStatus.equals (Active)) {
            if (orderStatus.equals (FailedActivation))
                fail (format ("the order is '%s'", FailedActivation));

            timer.Wait ();
            refresh ();
            orderStatus = getOrderStatus ();
        }
        assertEquals (orderStatus, Active, "Order did not activated successfully");
    }

    public String getPatientId () {
        String css = "//*[@class='summary']//a[*[@ng-bind='ctrl.orderEntry.order.patient.patientCode']]";
        return substringAfterLast (getAttribute (css, "href"), "patient/");
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
        return substringBetween (getText (activeAlert), "(", ")");
    }

    public String getResolvedAlert () {
        return substringBetween (getText (resolvedAlert), "(", ")");
    }

    public void createNewAlert () {
        assertTrue (click (newAlert));
    }

    public String getHeaderDueDate () {
        return getText (".header-alert-dashboard [ng-bind^='ctrl.orderEntry.orderTests[0].dueDate']");
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
