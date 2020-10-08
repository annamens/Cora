package com.adaptivebiotech.cora.ui.task;

import static com.adaptivebiotech.test.utils.PageHelper.StageName.AzCoraScript;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Finished;
import java.util.Map;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class TaskRuncomparison extends Task {

    public void doRuncomparison (String fcid, String args, String... files) {
        selectNewTask ();
        isCorrectPage ();
        selectTask ("Script (Azure)");
        enterTaskName ("Selenium Run Comparison - " + fcid);
        enterImage ("runcomparison");
        enterCPU ("4");
        enterRAM ("30");
        enterArgs (args);

        if (files.length > 0)
            attachedFiles (files);

        clickRun ();
        clickTaskStatus ();
    }

    public Map <String, String> doRuncomparisonAndWait (String fcid, String args, String... files) {
        doRuncomparison (fcid, args, files);

        TaskStatus status = new TaskStatus ();
        status.isCorrectPage ();
        status.waitFor (AzCoraScript, Finished);
        status.clickTaskDetail ();

        TaskDetail detail = new TaskDetail ();
        detail.isCorrectPage ();
        return detail.taskFiles ();
    }
}
