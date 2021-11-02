package com.adaptivebiotech.cora.ui.order;

import static com.adaptivebiotech.test.BaseEnvironment.coraTestPass;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestUser;
import static com.adaptivebiotech.test.utils.PageHelper.Assay.COVID19_DX_IVD;
import static com.adaptivebiotech.test.utils.PageHelper.Assay.LYME_DX_IVD;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import com.adaptivebiotech.cora.utils.PageHelper.CorrectionType;
import com.adaptivebiotech.test.utils.PageHelper.Assay;
import com.adaptivebiotech.test.utils.PageHelper.QC;

/**
 * @author jpatel
 *
 */
public class Report extends OrderHeader {

    private final String reportNotes        = "[ng-model=\"ctrl.reportEntry.notes\"]";
    private final String additionalComments = "[ng-model=\"ctrl.reportEntry.report.commentInfo.comments\"]";
    private final String previewReportPdf   = ".report-preview ng-pdf";
    private final String releasedReportPdf  = ".released-report-table tr td:nth-child(3) a";
    private final String editJson           = "[ng-click='ctrl.editReportData()']";
    private final String reportDataJson     = "[ng-model='ctrl.reportDataJSON']";
    private final String modalOk            = "[data-ng-click='ctrl.ok();']";

    public String getReportNotes () {
        return readInput (reportNotes);
    }

    public void enterReportNotes (String notes) {
        assertTrue (setText (reportNotes, notes));
    }

    public String getAdditionalComments () {
        return readInput (additionalComments);
    }

    public void enterAdditionalComments (String comments) {
        String button = "[ng-click=\"ctrl.toggleComments()\"]";
        assertTrue (click (button));
        assertTrue (setText (additionalComments, comments));
    }

    public String getPreviewReportPdfUrl () {
        return getAttribute (previewReportPdf, "pdf-url");
    }

    public String getReleasedReportPdfUrl () {
        return getAttribute (releasedReportPdf, "href");
    }

    public void clickEditJson () {
        assertTrue (click (editJson));
        assertTrue (isTextInElement (popupTitle, "Edit JSON"));
    }

    public String getReportDataJson () {
        return getAttribute (reportDataJson, "value");
    }

    public void setReportDataJson (String jsonData) {
        assertTrue (clear (reportDataJson));
        assertTrue (setText (reportDataJson, jsonData));
        assertTrue (click (modalOk));
        pageLoading ();
    }

    public void clickReportNotesIcon () {
        String css = "img[src=\"/assets/images/ReportNotes.png\"]";
        assertTrue (click (css));
        waitForAjaxCalls ();
    }

    public String getTooltipText () {
        String css = ".selectable-tooltip-content";
        return getText (css);
    }

    public void clickAdditionalCommentsIcon () {
        String additionalCommentsButton = "img[src=\"/assets/images/ReportPDFAdditionalComments.png\"]";
        assertTrue (click (additionalCommentsButton));
        waitForAjaxCalls ();
    }

    public void releaseReportWithSignatureRequired () {
        String releaseReport = "[ng-click=\"ctrl.releaseReport()\"]";
        String usernameField = "#userName";
        String passwordField = "#userPassword";
        String button = "[ng-click=\"ctrl.ok();\"]";
        // click release report, wait for popup, enter username and pw, then click release
        // button in popup
        assertTrue (click (releaseReport));
        assertTrue (isTextInElement (popupTitle, "Release Report"));
        assertTrue (setText (usernameField, coraTestUser));
        assertTrue (setText (passwordField, coraTestPass));
        assertTrue (click (button));
        pageLoading ();

    }

    public void closeReportPreview () {
        String css = ".modal-header button.close";
        assertTrue (click (css));
        waitForAjaxCalls ();
    }

    public void clickReportPreviewLink () {
        String css = "img[src=\"/assets/images/ReportPDF.png\"]";
        assertTrue (click (css));
        String headerText = waitForElementVisible (".modal-header").getText ();
        assertEquals (headerText, "Preview");
    }

    public void clickSaveAndUpdate () {
        String css = "//button[text()='Save & Update']";
        assertTrue (click (css));
        pageLoading ();
    }

    public void selectQCStatus (QC qc) {
        String css = "#qcStatus";
        assertTrue (clickAndSelectValue (css, qc.name ()));
    }

    public void enterPredefinedComment (String textToEnter) {
        String textField = "[ng-enter=\"ctrl.onKeyEnter()\"]";
        String firstElementInDropdown = "[ng-show='ctrl.hasResults'] li:nth-child(1) a";
        assertTrue (setText (textField, textToEnter));
        // now a dropdown appears and you have to select the same text
        assertTrue (click (waitForElementClickable (firstElementInDropdown)));
        waitForElementVisible ("[ng-bind=\"ctrl.reportEntry.qcInfo.comments\"]");
    }

    public void clickQCComplete () {
        String css = "[data-ng-click='ctrl.qcComplete()']";
        assertTrue (click (css));
        pageLoading ();
    }

    public void setQCstatus (QC qc) {
        assertTrue (clickAndSelectValue ("#qcStatus", qc.name ()));
        assertTrue (click ("[data-ng-click='ctrl.qcComplete()']"));
        pageLoading ();
    }

    public void releaseReport () {
        assertTrue (click ("//*[text()='Release Report']"));
        assertTrue (isTextInElement (popupTitle, "Release Report"));
        clickPopupOK ();
    }

    public void releaseReport (Assay assay, QC qc) {
        new OrderStatus ().isCorrectPage ();
        clickReportTab (assay);

        // for TCell
        if (!COVID19_DX_IVD.equals (assay) && !LYME_DX_IVD.equals (assay) && isElementVisible (".report-blocked-msg")) {
            assertTrue (click ("//*[text()='Generate Report']"));
            pageLoading ();
        }

        setQCstatus (qc);
        releaseReport ();
    }

    public String getReportReleaseDate () {
        return getText ("[ng-bind*='reportEntry.releasedDate']");
    }

    public String getReportUrl () {
        return getAttribute (".released-report-table a[download]", "href");
    }

    public void clickCorrectReport () {
        String button = "//button[text()='Correct Report']";
        assertTrue (click (button));
        pageLoading ();
    }

    public void selectCorrectionType (CorrectionType correctionType) {
        assertTrue (click ("#" + correctionType.name ().toLowerCase () + "[name='correctionType']"));
    }

    public void enterReasonForCorrection (String text) {
        String reasonTextArea = "[ng-model='ctrl.reportEntry.report.commentInfo.correctionReason']";
        assertTrue (setText (reasonTextArea, text));
    }

    public void clickCorrectReportSaveAndUpdate () {
        String button = "[ng-click='ctrl.update()']";
        assertTrue (click (button));
        pageLoading ();
    }

    public boolean waitForCorrectedReportStatusFinished () {
        String reportDeliveryStatus = "//tr[contains (@ng-repeat-start, 'releasedReports')][1]/td[contains (@ng-bind, 'deliveryStatus')]";
        int count = 0;

        String status = getText (reportDeliveryStatus);
        while (count < 10 && !status.equals ("Finished")) {
            doWait (10000);
            refresh ();
            status = getText (reportDeliveryStatus);
            count++;
        }
        return status.equals ("Finished");
    }

}
