package com.adaptivebiotech.cora.ui.order;

import static org.testng.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.WebElement;
import com.adaptivebiotech.test.utils.PageHelper.StageName;
import com.adaptivebiotech.test.utils.PageHelper.StageStatus;
import com.adaptivebiotech.test.utils.PageHelper.StageSubstatus;

public class OrderStatus extends OrderHeader {

    private final String historyLink          = ".history-link";
    private final String stageActionDots      = "#stageActionsDropdown";
    private final String stageActionsDropdown = "[aria-labelledby='stageActionsDropdown']";
    private final String failworkflowAction   = "//*[@aria-labelledby='stageActionsDropdown']//a[text()='Fail workflow']";
    private final String subStatusMsg         = "[ng-model='ctrl.subStatusMessage']";
    private final String submit               = "//button[text()='Submit']";
    private final String actionConfirm        = ".action-confirm";
    private final String confirmYes           = "//button[text()='Yes']";

    @Override
    public void isCorrectPage () {
        assertTrue (isTextInElement ("[role='tablist'] .active a", "ORDER STATUS"));
        pageLoading ();
    }

    public List <String> getCancelOrderMessages () {
        String css = "[ng-if='ctrl.orderEntry.order | orderIsCancelled']";
        List <String> cancellationMsgs = new ArrayList <String> ();
        if (isElementPresent (css)) {
            cancellationMsgs.add (getText (css + " h2"));
            cancellationMsgs.add (getText (css + " p"));
        }
        return cancellationMsgs;
    }

    public String getOrderNum () {
        return getText ("[ng-bind='ctrl.orderEntry.order.orderNumber']");
    }

    public String getOrderName () {
        return getText ("[ng-bind='ctrl.orderEntry.order.name']");
    }

    public String getSpecimenNumber () {
        return getText ("[ng-bind='::orderTest.specimenNumber']");
    }

    public String getTestName () {
        return getText ("[ng-bind='::orderTest.testName']");
    }

    // aka sample name
    public String getWorkflowId () {
        return getText ("[ng-bind=\"::orderTest.workflowName\"]");
    }

    public String getLastActivity () {
        return getText ("//*[contains(@ng-bind,'::orderTest.lastActivity')]/..");
    }

    public boolean kitClonoSEQReportStageDisplayed () {

        return waitForElement ("[class='ordertest-list-stage KitClonoSEQReport']").isDisplayed ();
    }

    public boolean kitReportDeliveryStageDisplayed () {
        return waitForElement ("[class='ordertest-list-stage KitReportDelivery']").isDisplayed ();
    }

    public void clickPatientNotesIcon () {
        String css = "[ng-click=\"ctrl.showPatientNotesDialog()\"]";
        assertTrue (click (css));
        waitForElementVisible (".patient-notes-modal");
        assertTrue (getText (popupTitle).contains ("Patient Note for Patient "));
    }

    // patient notes popup
    public String getPatientNotes () {
        String css = "[ng-bind=\"ctrl.patient.notes\"]";
        String text = readInput (css);
        return text;
    }

    public int getClarityStageRequeueCount () {
        String css = ".ordertest-list-stage.Clarity .requeue-count";
        return Integer.valueOf (getText (css));
    }

    public StageName getCurrentWorkflowStage () {
        String css = ".is-current";
        WebElement webElement = waitForElementVisible (css);
        String text = webElement.getAttribute ("title");
        return StageName.valueOf (text);
    }

    public void expandWorkflowHistory () {
        String css = ".ordertest-list-stage.Clarity";
        assertTrue (click (css));
        pageLoading ();
        assertTrue (waitUntilVisible (".table.table-bordered.history"));
    }

    public boolean isWorkflowHistoryPresent (String stage, String status, String subStatus) {
        String css = ".table.table-bordered.history tbody tr";
        List <WebElement> workflowHistories = waitForElementsVisible (css);
        for (WebElement row : workflowHistories) {
            String rowStage = getText (row, "td:nth-child(1)");
            String rowStatus = getText (row, "td:nth-child(2)");
            String rowSubStatus = getText (row, "td:nth-child(3)");
            if (stage.equals (rowStage) && status.equals (rowStatus) && subStatus.equals (rowSubStatus)) {
                return true;
            }
        }
        return false;
    }

    public String getOrderStatusText () {
        String status = "[ng-bind='ctrl.orderEntry.order.status']";
        return getText (status);
    }

    public StageSubstatus getStageSubstatus () {
        String css = "span.ng-binding.ng-scope";
        String subStatusName = getText (css);
        String trimmedName = subStatusName.substring (1, subStatusName.length () - 1);
        return StageSubstatus.valueOf (trimmedName);
    }

    public StageStatus getStageStatus () {
        String xpath = "//td[@class='stage-status']/div/div[1]";
        String statusText = getText (xpath);
        String trimmedStatusText = statusText.split (" ")[0];
        return StageStatus.valueOf (trimmedStatusText);
    }

    public List <String> getStageStatusUrls () {
        return getAttributeList (".details-url", "href");
    }

    public boolean isStageSubstatusVisible () {
        String css = "span.ng-binding.ng-scope";
        return waitUntilVisible (css);
    }

    public String getOrderTestIdFromUrl () {
        String orderTestId = null;
        String url = getCurrentUrl ();
        if (url.contains ("ordertestid")) {
            orderTestId = url.split ("ordertestid=")[1];
        }
        return orderTestId;
    }

    public void failWorkflow (String message) {
        assertTrue (isTextInElement (historyLink, "History"));
        assertTrue (click (historyLink));
        assertTrue (isTextInElement (historyLink, "Hide"));
        assertTrue (click (stageActionDots));
        assertTrue (waitUntilVisible (stageActionsDropdown));
        assertTrue (click (failworkflowAction));
        assertTrue (setText (subStatusMsg, message));
        assertTrue (click (submit));
        assertTrue (isTextInElement (actionConfirm, "Are you sure you want to fail the workflow?"));
        assertTrue (click (confirmYes));
    }

    public String getSpecimenId () {
        String css = "span.order-specimen-id";
        return getText (css);
    }
}
