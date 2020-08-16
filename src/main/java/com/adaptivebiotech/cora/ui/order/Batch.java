package com.adaptivebiotech.cora.ui.order;

import static org.openqa.selenium.Keys.ENTER;
import static org.testng.Assert.assertTrue;
import com.adaptivebiotech.ui.cora.CoraPage;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class Batch extends CoraPage {

    public Batch () {
        staticNavBarHeight = 90;
    }

    @Override
    public void isCorrectPage () {
        assertTrue (waitUntilVisible (".salesforce-container"));
        assertTrue (waitUntilVisible (".shipments"));
        assertTrue (waitUntilVisible ("[name='projectType']"));
    }

    public void searchOrder (String ordernum) {
        assertTrue (setText ("[ng-model='ctrl.salesforceId']", ordernum));
        assertTrue (pressKey (ENTER));
        pageLoading ();
    }
}
