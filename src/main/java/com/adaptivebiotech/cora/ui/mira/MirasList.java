package com.adaptivebiotech.cora.ui.mira;

import static org.testng.Assert.assertTrue;
import com.adaptivebiotech.ui.cora.CoraPage;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class MirasList extends CoraPage {

    public MirasList () {
        staticNavBarHeight = 90;
    }

    @Override
    public void isCorrectPage () {
        assertTrue (waitUntilVisible (".active[title='MIRAs']"));
        pageLoading ();
    }
}
