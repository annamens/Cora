package com.adaptivebiotech.cora.ui.task;

import static org.testng.Assert.assertTrue;
import com.adaptivebiotech.cora.ui.CoraPage;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class TaskList extends CoraPage {

    public TaskList () {
        staticNavBarHeight = 90;
    }

    @Override
    public void isCorrectPage () {
        assertTrue (waitUntilVisible (".active[title='Tasks']"));
        pageLoading ();
    }
    
    public void searchAndClickFirstTask (String text) {
        this.doTaskSearch (text);
        String firstTaskName = "table > tbody > tr:nth-child(1) > td.task-name-description > a > span";
        assertTrue (click (firstTaskName));
        pageLoading ();
    }
}
