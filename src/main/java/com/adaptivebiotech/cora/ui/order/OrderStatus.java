package com.adaptivebiotech.cora.ui.order;

import static java.lang.String.format;
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

    private final long   millisRetry          = 3000000l;                                                                 // 50mins
    private final long   waitRetry            = 60000l;                                                                   // 60sec
    private final String historyLink          = ".history-link";
    private final String stageActionDots      = "#stageActionsDropdown";
    private final String stageActionsDropdown = "[aria-labelledby='stageActionsDropdown']";
    private final String failworkflowAction   = "//*[@aria-labelledby='stageActionsDropdown']//a[text()='Fail workflow']";
    private final String subStatusMsg         = "[ng-model='ctrl.subStatusMessage']";
    private final String submit               = "//button[text()='Submit']";
    private final String actionConfirm        = ".action-confirm";
    private final String confirmYes           = "//button[text()='Yes']";
    private final String hideShow             = "//*[contains (@class, 'history-link') and text()='%s']";
    private final String workflowTable        = "table.history";

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

    public void waitFor (StageName stage, StageStatus status, StageSubstatus substatus, String message) {
        String fail = "unable to locate Stage: %s, Status: %s, Substatus: %s, Message: %s";
        String xpath = "//table[contains (@class, 'history')]//tr[td='%s']/following-sibling::td[contains(.,'%s')]/following-sibling::td[contains(.,'%s')]/*[contains (text(), '%s')]";
        String check = format (xpath, stage, status, substatus == null ? "" : substatus, message);
        Timeout timer = new Timeout (millisRetry, waitRetry);
        boolean found = false;
        clickHistory ();
        while (!timer.Timedout () && ! (found = isElementPresent (check))) {
            clickHide ();
            timer.Wait ();
            clickHistory ();
        }
        if (!found)
            fail (format (fail, stage, status, substatus, message));
    }

    public void waitFor (StageName stage, StageStatus status, StageSubstatus substatus) {
        String fail = "unable to locate Stage: %s, Status: %s, Substatus: %s";
        Timeout timer = new Timeout (millisRetry, waitRetry);
        boolean found = false;
        clickHistory ();
        while (!timer.Timedout () && ! (found = isStagePresent (stage, status, substatus))) {
            clickHide ();
            timer.Wait ();
            clickHistory ();
        }
        if (!found)
            fail (format (fail, stage, status, substatus));
    }

    public void waitFor (StageName stage, StageStatus status) {
        String fail = "unable to locate Stage: %s, Status: %s";
        Timeout timer = new Timeout (millisRetry, waitRetry);
        boolean found = false;
        clickHistory ();
        while (!timer.Timedout () && ! (found = isStagePresent (stage, status))) {
            clickHide ();
            timer.Wait ();
            clickHistory ();
        }
        if (!found)
            fail (format (fail, stage, status));
    }

    public boolean isStagePresent (StageName stage, StageStatus status, StageSubstatus substatus) {
        String xpath = "//table[contains (@class, 'history')]//tr[td='%s']/following-sibling::td[contains(.,'%s')]/following-sibling::td[contains(.,'%s')]";
        return isElementPresent (format (xpath, stage.name (), status.name (), substatus.name ()));
    }

    public boolean isStagePresent (StageName stage, StageStatus status) {
        String xpath = "//table[contains (@class, 'history')]//tr[td='%s']/following-sibling::td[contains(.,'%s')]";
        return isElementPresent (format (xpath, stage.name (), status.name ()));
    }

    public void clickHistory () {
        assertTrue (click (format (hideShow, "History")));
        pageLoading ();
        assertTrue (waitUntilVisible (workflowTable));
    }

    public void clickHide () {
        assertTrue (click (format (hideShow, "Hide")));
        assertTrue (waitForElementInvisible (workflowTable));
    }
}
