/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.ui.patient;

import static java.lang.String.format;
import static org.testng.Assert.assertTrue;
import com.adaptivebiotech.cora.dto.Element;
import com.adaptivebiotech.cora.dto.Orders.Order;

/**
 * @author jpatel
 *
 */
public class PatientOrderHistory extends PatientHeader {

    private final String icon = "//tr[td='%s']/td//*[contains (@src, '%s')]";

    public PatientOrderHistory () {
        staticNavBarHeight = 200;
    }

    @Override
    public void isCorrectPage () {
        assertTrue (isTextInElement ("[role='tablist'] .active a", "PATIENT ORDER HISTORY"));
    }

    public void closeReportPreview () {
        assertTrue (click (".modal-header button.close"));
        moduleLoading ();
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

    public String getTooltipText () {
        String css = ".selectable-tooltip-content";
        return getText (css);
    }

    public Element getStabilizationWindow (Order order) {
        Element el = new Element ();
        String xpath = "//*[*[text()='" + order.orderNumber + "']]/following-sibling::td//specimen-stabilization-window//div[*[*[contains (@title, 'Stabilization')]]]";
        el.text = getText (xpath + "//strong");
        el.color = getCssValue (xpath, "background-color");
        return el;
    }
}
