package com.adaptivebiotech.cora.ui.mira;

import static org.testng.Assert.assertTrue;
import org.openqa.selenium.support.ui.Select;
import com.adaptivebiotech.cora.utils.PageHelper.MiraPanel;
import com.adaptivebiotech.ui.cora.CoraPage;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class Mira extends CoraPage {

    public Mira () {
        staticNavBarHeight = 90;
    }

    @Override
    public void isCorrectPage () {
        assertTrue (isTextInElement (".container .mira-heading", "New MIRA"));
    }

    public void selectPanel (MiraPanel panel) {
        // after selection, the getFirstSelectedOption() stays at "Select..."
        Select dropdown = new Select (scrollTo (waitForElementClickable (locateBy ("[name='panelType']"))));
        dropdown.selectByVisibleText (panel.name ());
        assertTrue (waitUntilVisible (".mira-panel-entry"));
    }
}
