/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.ui.patient;

import static java.lang.String.format;
import static org.testng.Assert.assertTrue;
import com.adaptivebiotech.cora.dto.Element;
import com.adaptivebiotech.cora.dto.Orders.Order;
import com.adaptivebiotech.cora.dto.Orders.OrderStatus;

/**
 * @author jpatel
 *
 */
public class PatientOrderHistory extends PatientHeader {

    private final String icon           = "//tr[td='%s']/td//*[contains (@src, '%s')]";
    private final String orderRowStatus = "//*[*[text()='%s']]/following-sibling::td";

    public PatientOrderHistory () {
        staticNavBarHeight = 200;
    }

    @Override
    public void isCorrectPage () {
        assertTrue (isTextInElement ("[role='tablist'] .active a", "PATIENT ORDER HISTORY"));
    }

    public void clickReportPreviewLink (Order order) {
        assertTrue (click (format (icon, order.orderNumber, "ReportPDF.png")));
        assertTrue (isTextInElement (popupTitle, "Preview"));
    }

    public void clickReportNotesIcon (Order order) {
        assertTrue (click (format (icon, order.orderNumber, "ReportNotes.png")));
    }

    public void clickAdditionalCommentsIcon (Order order) {
        assertTrue (click (format (icon, order.orderNumber, "ReportPDFAdditionalComments.png")));
    }

    public boolean statusHeadersPresent () {
        return isTextInElement ("thead th:nth-child(9)", "Test Status") && isTextInElement ("thead th:nth-child(10)",
                                                                                            "Order Status");
    }

    public OrderStatus getTestStatus (Order order) {
        return OrderStatus.valueOf (getText (format (orderRowStatus + "[8]//span", order.orderNumber)));
    }

    public OrderStatus getOrderStatus (Order order) {
        return OrderStatus.valueOf (getText (format (orderRowStatus + "[9]", order.orderNumber)));
    }

    public String getTooltipText () {
        String css = ".selectable-tooltip-content";
        return getText (css);
    }

    public Element getStabilizationWindow (Order order) {
        Element el = new Element ();
        String xpath = format ("//*[*[text()='%s']]/following-sibling::td//specimen-stabilization-window//div[*[*[contains (@title, 'Stabilization')]]]",
                               order.orderNumber);
        el.text = getText (xpath + "//strong");
        el.color = getCssValue (xpath, "background-color");
        return el;
    }
}
