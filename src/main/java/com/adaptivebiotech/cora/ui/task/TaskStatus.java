package com.adaptivebiotech.cora.ui.task;

import static java.lang.String.format;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.WebElement;
import com.adaptivebiotech.cora.dto.TaskHistory;
import com.adaptivebiotech.test.utils.PageHelper.StageName;
import com.adaptivebiotech.test.utils.PageHelper.StageStatus;
import com.adaptivebiotech.test.utils.PageHelper.StageSubstatus;
import com.seleniumfy.test.utils.Timeout;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class TaskStatus extends Task {

    private final long millisRetry = 9000000l; // 2.5hrs
    private final long waitRetry   = 10000l;   // 10sec

    public TaskStatus () {
        staticNavBarHeight = 195;
    }

    @Override
    public void isCorrectPage () {
        assertTrue (isTextInElement ("[role='tablist'] .active a", "TASK STATUS"));
    }

    public void clickTaskDetail () {
        assertTrue (click ("//a[text()='Task Detail']"));
    }

    public void waitFor (StageName stage, StageStatus status, StageSubstatus substatus, String message) {
        String fail = "unable to locate Stage: %s, Status: %s, Substatus: %s, Message: %s";
        String xpath = "//td[text()='%s']/../td[text()='%s']/../td[text()[contains (.,'%s')]]/span[text()='%s']";
        Timeout timer = new Timeout (millisRetry, waitRetry);
        boolean found = false;
        while (!timer.Timedout () && ! (found = isElementPresent (format (xpath, stage, status, substatus, message)))) {
            nudge ();
            timer.Wait ();
            refresh ();
            pageLoading ();
        }
        if (!found)
            fail (format (fail, stage, status, substatus, message));
    }

    public void waitFor (StageName stage, StageStatus status, StageSubstatus substatus) {
        String fail = "unable to locate Stage: %s, Status: %s, Substatus: %s";
        String xpath = "//td[text()='%s']/../td[text()='%s']/../td[text()[contains (.,'%s')]]";
        Timeout timer = new Timeout (millisRetry, waitRetry);
        boolean found = false;
        while (!timer.Timedout () && ! (found = isElementPresent (format (xpath, stage, status, substatus)))) {
            nudge ();
            timer.Wait ();
            refresh ();
            pageLoading ();
        }
        if (!found)
            fail (format (fail, stage, status, substatus));
    }

    public void waitFor (StageName stage, StageStatus status) {
        String fail = "unable to locate Stage: %s, Status: %s";
        String xpath = "//td[text()='%s']/../td[text()='%s']";
        Timeout timer = new Timeout (millisRetry, waitRetry);
        boolean found = false;
        while (!timer.Timedout () && ! (found = isElementPresent (format (xpath, stage, status)))) {
            nudge ();
            timer.Wait ();
            refresh ();
            pageLoading ();
        }
        if (!found)
            fail (format (fail, stage, status));
    }

    public void nudge () {
        String threedots = "#stageActionsDropdown";
        if (isElementVisible (threedots)) {
            assertTrue (click (threedots));
            assertTrue (waitUntilVisible ("[aria-labelledby='stageActionsDropdown']"));
            assertTrue (click ("[ng-click*='nudgeCurrentWorkflowConfirm']"));
            assertTrue (click ("[ng-click='ctrl.nudgeCurrentWorkflow()']"));
            pageLoading ();
        }
    }

    public List <TaskHistory> parseTaskHistory () {
        List <TaskHistory> histories = new ArrayList <> ();
        for (WebElement el : waitForElements ("[ng-repeat*='ctrl.taskHistory']")) {
            TaskHistory history = new TaskHistory ();
            history.stage = StageName.valueOf (getText (el, "td:nth-child(1)"));
            history.status = StageStatus.valueOf (getText (el, "td:nth-child(2)"));
            history.message = getText (el, ".substatus-message");

            String substatus = getText (el, "td:nth-child(3)");
            if (substatus != null) {
                String code = substatus.replace (history.message, "").trim ();
                if (code.length () > 0)
                    history.substatus = StageSubstatus.valueOf (code);
            }

            history.actor = getText (el, "td:nth-child(4)");
            history.timestamp = getText (el, "td:nth-child(5)");
            histories.add (history);
        }
        return histories;
    }
}
