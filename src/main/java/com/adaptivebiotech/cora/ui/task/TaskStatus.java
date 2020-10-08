package com.adaptivebiotech.cora.ui.task;

import static org.testng.Assert.assertTrue;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class TaskStatus extends Task {

    public TaskStatus () {
        staticNavBarHeight = 195;
    }

    @Override
    public void isCorrectPage () {
        assertTrue (isTextInElement ("[role='tablist'] .active a", "TASK STATUS"));
    }
}
