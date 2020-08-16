package com.adaptivebiotech.cora.ui.task;

import static org.testng.Assert.assertTrue;
import com.adaptivebiotech.ui.cora.CoraPage;

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
}
