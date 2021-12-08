package com.adaptivebiotech.cora.ui.patient;

import static java.lang.String.format;
import static org.testng.Assert.assertTrue;
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
        assertTrue (click (format (icon, "ReportPDF.png", order.order_number)));
        assertTrue (isTextInElement (popupTitle, "Preview"));
    }

    public void clickReportNotesIcon (Order order) {
        assertTrue (click (format (icon, "ReportNotes.png", order.order_number)));
    }

    public void clickAdditionalCommentsIcon (Order order) {
        assertTrue (click (format (icon, "ReportPDFAdditionalComments.png", order.order_number)));
    }

    public String getTooltipText () {
        String css = ".selectable-tooltip-content";
        return getText (css);
    }
}
