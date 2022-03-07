package com.adaptivebiotech.cora.ui.order;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.WebElement;
import com.adaptivebiotech.test.utils.PageHelper.StageName;
import com.adaptivebiotech.test.utils.PageHelper.StageStatus;
import com.adaptivebiotech.test.utils.PageHelper.StageSubstatus;
import com.seleniumfy.test.utils.Timeout;

public class OrderStatus extends OrderHeader {

    private final long   millisRetry          = 3000000l;                                                                     // 50mins
    private final long   waitRetry            = 30000l;                                                                       // 30sec
    private final String stageActionDots      = "#stageActionsDropdown";
    private final String stageActionsDropdown = "[aria-labelledby='stageActionsDropdown']";
    private final String dropdownItem         = "//*[@aria-labelledby='stageActionsDropdown']//a[text()='%s']";
    private final String subStatusMsg         = "[ng-model='ctrl.subStatusMessage']";
    private final String submit               = "//button[text()='Submit']";
    private final String actionConfirm        = ".action-confirm";
    private final String confirmYes           = "//button[text()='Yes']";
    private final String hideShow             = "//tr[td[text()='%s']]//*[contains (@class, 'history-link') and text()='%s']";
    private final String workflowTable        = "//tr[td[text()='%s']]/following-sibling::tr[1]//tbody/tr[1]";

    public OrderStatus () {
        staticNavBarHeight = 200;
    }

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
        return substringAfterLast (getCurrentUrl (), "ordertestid=");
    }

    public void failWorkflow (String sampleName, String message) {
        clickHistory (sampleName);
        assertTrue (click (stageActionDots));
        assertTrue (waitUntilVisible (stageActionsDropdown));
        assertTrue (click (format (dropdownItem, "Fail workflow")));
        assertTrue (setText (subStatusMsg, message));
        assertTrue (click (submit));
        assertTrue (isTextInElement (actionConfirm, "Are you sure you want to fail the workflow?"));
        assertTrue (click (confirmYes));
        clickHide (sampleName);
    }

    public String getSpecimenId () {
        String css = "span.order-specimen-id";
        return getText (css);
    }

    public void nudgeWorkflow () {
        assertTrue (click (stageActionDots));
        assertTrue (waitUntilVisible (stageActionsDropdown));
        assertTrue (click (format (dropdownItem, "Nudge workflow")));
        assertTrue (click (confirmYes));
    }

    public void waitFor (String sampleName, StageName stage, StageStatus status, StageSubstatus substatus,
                         String message) {
        String fail = "unable to locate Stage: %s, Status: %s, Substatus: %s, Message: %s";
        String xpath = "//tr[td[text()='%s']]/following-sibling::tr[1]//table[contains (@class, 'history')]//td[text()='%s']/following-sibling::td[text()='%s']/following-sibling::td[contains(.,'%s')]/*[contains (text(), '%s')]";
        String check = format (xpath, sampleName, stage, status, substatus == null ? "" : substatus, message);
        boolean found = false;
        Timeout timer = new Timeout (millisRetry, waitRetry);
        while (!timer.Timedout ()) {
            clickHistory (sampleName);
            if (found = isElementPresent (check)) {
                clickHide (sampleName);
                break;
            } else {
                nudgeWorkflow ();
                clickHide (sampleName);
                timer.Wait ();
            }
        }
        if (!found)
            fail (format (fail, stage, status, substatus, message));
    }

    public void waitFor (String sampleName, StageName stage, StageStatus status, StageSubstatus substatus) {
        String fail = "unable to locate Stage: %s, Status: %s, Substatus: %s";
        boolean found = false;
        Timeout timer = new Timeout (millisRetry, waitRetry);
        while (!timer.Timedout () && !found) {
            clickHistory (sampleName);
            if (found = isStagePresent (sampleName, stage, status, substatus)) {
                clickHide (sampleName);
                break;
            } else {
                nudgeWorkflow ();
                clickHide (sampleName);
                timer.Wait ();
            }
        }
        if (!found)
            fail (format (fail, stage, status, substatus));
    }

    public void waitFor (String sampleName, StageName stage, StageStatus status) {
        String fail = "unable to locate Stage: %s, Status: %s";
        boolean found = false;
        Timeout timer = new Timeout (millisRetry, waitRetry);
        while (!timer.Timedout ()) {
            clickHistory (sampleName);
            if (found = isStagePresent (sampleName, stage, status)) {
                clickHide (sampleName);
                break;
            } else {
                nudgeWorkflow ();
                clickHide (sampleName);
                timer.Wait ();
            }
        }
        if (!found)
            fail (format (fail, stage, status));
    }

    public boolean isStagePresent (String sampleName, StageName stage, StageStatus status, StageSubstatus substatus) {
        String xpath = "//tr[td[text()='%s']]/following-sibling::tr[1]//table[contains (@class, 'history')]//td[text()='%s']/following-sibling::td[text()='%s']/following-sibling::td[contains(.,'%s')]";
        return isElementPresent (format (xpath, sampleName, stage.name (), status.name (), substatus.name ()));
    }

    public boolean isStagePresent (String sampleName, StageName stage, StageStatus status) {
        String xpath = "//tr[td[text()='%s']]/following-sibling::tr[1]//table[contains (@class, 'history')]//td[text()='%s']/following-sibling::td[text()='%s']";
        return isElementPresent (format (xpath, sampleName, stage.name (), status.name ()));
    }

    public void clickHistory (String sampleName) {
        assertTrue (click (format (hideShow, sampleName, "History")));
        assertTrue (waitUntilVisible (format (workflowTable, sampleName)));
    }

    public void clickHide (String sampleName) {
        assertTrue (click (format (hideShow, sampleName, "Hide")));
        assertTrue (waitForElementInvisible (format (workflowTable, sampleName)));
    }
}
