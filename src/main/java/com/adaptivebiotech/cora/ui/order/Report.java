package com.adaptivebiotech.cora.ui.order;

import static com.adaptivebiotech.cora.dto.Orders.Assay.COVID19_DX_IVD;
import static com.adaptivebiotech.cora.dto.Orders.Assay.LYME_DX_IVD;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestPass;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestUser;
import static org.apache.commons.lang3.StringUtils.substringBetween;
import static org.testng.Assert.assertTrue;
import com.adaptivebiotech.cora.dto.Orders.Assay;
import com.adaptivebiotech.cora.utils.PageHelper.CorrectionType;
import com.adaptivebiotech.cora.utils.PageHelper.QC;

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

    public Report () {
        staticNavBarHeight = 200;
    }

    @Override
    public void isCorrectPage () {
        assertTrue (isTextInElement ("[role='tablist'] .active a", "REPORT"));
        pageLoading ();
    }

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

    public void clickSaveAndUpdate () {
        String css = "//*[text()='Save & Update']";
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
        selectQCStatus (qc);
        clickQCComplete ();
    }

    public void clickReleaseReport () {
        assertTrue (click ("//*[text()='Release Report']"));
        assertTrue (isTextInElement (popupTitle, "Release Report"));
        clickPopupOK ();
    }

    public void releaseReport (Assay assay, QC qc) {
        // for TCell
        if (!COVID19_DX_IVD.equals (assay) && !LYME_DX_IVD.equals (assay) && isElementVisible (".report-blocked-msg")) {
            assertTrue (click ("//*[text()='Generate Report']"));
            pageLoading ();
        }

        setQCstatus (qc);
        clickReleaseReport ();
    }

    public void releaseReportWithSignatureRequired () {
        // click release report, wait for popup, enter username and pw, then click release
        // button in popup
        assertTrue (click ("//*[text()='Release Report']"));
        assertTrue (isTextInElement (popupTitle, "Sign & Release Report"));
        assertTrue (setText ("#userName", coraTestUser));
        assertTrue (setText ("#userPassword", coraTestPass));
        assertTrue (click ("//*[text()='Sign & release Report']"));
        moduleLoading ();
        pageLoading ();
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
        reportLoading ();
    }

    public void selectCorrectionType (CorrectionType correctionType) {
        assertTrue (click ("#" + correctionType.name ().toLowerCase () + "[name='correctionType']"));
    }

    public void enterReasonForCorrection (String text) {
        String reasonTextArea = "[ng-model='ctrl.reportEntry.report.commentInfo.correctionReason']";
        assertTrue (setText (reasonTextArea, text));
    }

    public String getCorrectedReportTaskId () {
        String url = getAttribute ("//*[contains (@download, 'CORRECTED')]", "href");
        return substringBetween (url, "cora-data/orca/", "/");
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
