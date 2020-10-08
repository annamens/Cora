package com.adaptivebiotech.cora.ui.task;

import static com.adaptivebiotech.test.utils.PageHelper.StageName.AzCoraScript;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.CoraScript;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Finished;
import java.util.Map;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class TaskScript extends Task {

    public void doScriptAws (String image, String args, String... files) {
        selectNewTask ();
        isCorrectPage ();
        selectTask ("Script (AWS)");
        if (image == null)
            enterTaskName ("Selenium Run Script");
        else {
            enterTaskName ("Selenium Run Script - " + image);
            enterImage (image);
        }
        enterCPU ("4");
        enterRAM ("30");
        if (args != null)
            enterArgs (args);

        if (files.length > 0)
            attachedFiles (files);

        clickRun ();
        clickTaskStatus ();
    }

    public Map <String, String> doScriptAwsAndWait (String image, String args, String... files) {
        doScriptAws (image, args, files);

        TaskStatus status = new TaskStatus ();
        status.isCorrectPage ();
        status.waitFor (CoraScript, Finished);
        status.clickTaskDetail ();

        TaskDetail detail = new TaskDetail ();
        detail.isCorrectPage ();
        return detail.taskFiles ();
    }

    public void doScriptAz (String image, String args, String... files) {
        selectNewTask ();
        isCorrectPage ();
        selectTask ("Script (Azure)");
        if (image == null)
            enterTaskName ("Selenium Run Script");
        else {
            enterTaskName ("Selenium Run Script - " + image);
            enterImage (image);
        }
        enterCPU ("4");
        enterRAM ("30");
        if (args != null)
            enterArgs (args);

        if (files.length > 0)
            attachedFiles (files);

        clickRun ();
        clickTaskStatus ();
    }

    public Map <String, String> doScriptAzAndWait (String image, String args, String... files) {
        doScriptAz (image, args, files);

        TaskStatus status = new TaskStatus ();
        status.isCorrectPage ();
        status.waitFor (AzCoraScript, Finished);
        status.clickTaskDetail ();

        TaskDetail detail = new TaskDetail ();
        detail.isCorrectPage ();
        return detail.taskFiles ();
    }
}
