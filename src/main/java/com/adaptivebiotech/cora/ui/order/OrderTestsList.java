package com.adaptivebiotech.cora.ui.order;

import static org.testng.Assert.assertTrue;
import com.adaptivebiotech.cora.ui.CoraPage;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class OrderTestsList extends CoraPage {

    public OrderTestsList () {
        staticNavBarHeight = 90;
    }

    @Override
    public void isCorrectPage () {
        assertTrue (waitUntilVisible (".active[title='Order Tests']"));
        pageLoading ();
    }
}
