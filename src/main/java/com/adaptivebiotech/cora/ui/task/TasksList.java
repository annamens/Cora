package com.adaptivebiotech.cora.ui.task;

import static org.testng.Assert.assertTrue;
import com.adaptivebiotech.cora.ui.CoraPage;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class TasksList extends CoraPage {

    public TasksList () {
        staticNavBarHeight = 90;
    }

    @Override
    public void isCorrectPage () {
        assertTrue (waitUntilVisible (".active[title='Tasks']"));
        pageLoading ();
    }

    public void searchAndClickFirstTask (String text) {
        this.doTaskSearch (text);
        String firstTaskName = "tr[ng-repeat-start='task in ctrl.tasks']:nth-child(1) a[ui-sref*='main.task'] span";
        assertTrue (click (firstTaskName));
        pageLoading ();
    }
}
