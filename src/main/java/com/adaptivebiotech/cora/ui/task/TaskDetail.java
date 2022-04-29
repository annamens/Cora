/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.ui.task;

import static org.testng.Assert.assertTrue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openqa.selenium.WebElement;
import com.adaptivebiotech.cora.dto.TaskOutput;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class TaskDetail extends Task {

    public TaskDetail () {
        staticNavBarHeight = 195;
    }

    @Override
    public void isCorrectPage () {
        assertTrue (isTextInElement ("[role='tablist'] .active a", "TASK DETAIL"));
    }

    public String getHeaderTaskName () {
        return getText ("[data-ng-bind='ctrl.task.name']");
    }

    public Map <String, String> taskFiles () {
        Map <String, String> files = new HashMap <> ();
        waitForElements ("[ng-if='ctrl.task.files.length'] .row[ng-repeat]").forEach (el -> {
            files.put (getText (el, "[ng-bind='file']"), getAttribute (el, "a", "href"));
        });
        return files;
    }

    public List <TaskOutput> getTaskOutputs () {
        List <TaskOutput> outputs = new ArrayList <> ();
        for (WebElement tr : waitForElements ("[ng-repeat='output in ctrl.outputProperties']")) {
            TaskOutput output = new TaskOutput ();
            output.label = getText (tr, ".prop-label");
            output.value = getText (tr, "[ng-bind='ctrl.task.properties[output]']");
            outputs.add (output);
        }
        return outputs;
    }
}
