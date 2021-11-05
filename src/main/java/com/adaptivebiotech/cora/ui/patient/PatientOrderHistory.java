package com.adaptivebiotech.cora.ui.patient;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author jpatel
 *
 */
public class PatientOrderHistory extends PatientHeader {

    @Override
    public void isCorrectPage () {
        assertTrue (isTextInElement ("[role='tablist'] .active a", "PATIENT ORDER HISTORY"));
    }

    public void closeReportPreview () {
        String css = ".modal-header button.close";
        assertTrue (click (css));
        waitForAjaxCalls ();
    }

    public void clickReportPreviewLink () {
        String css = "img[src='/assets/images/ReportPDF.png']";
        assertTrue (click (css));
        String headerText = waitForElementVisible (".modal-header").getText ();
        assertEquals (headerText, "Preview");
    }

    public void clickReportNotesIcon () {
        String css = "img[src='/assets/images/ReportNotes.png']";
        assertTrue (click (css));
        waitForAjaxCalls ();
    }

    public String getTooltipText () {
        String css = ".selectable-tooltip-content";
        return getText (css);
    }

    public void clickAdditionalCommentsIcon () {
        String additionalCommentsButton = "img[src='/assets/images/ReportPDFAdditionalComments.png']";
        assertTrue (click (additionalCommentsButton));
        waitForAjaxCalls ();
    }
}
