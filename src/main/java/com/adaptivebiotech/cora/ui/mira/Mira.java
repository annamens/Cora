package com.adaptivebiotech.cora.ui.mira;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.openqa.selenium.support.ui.Select;
import com.adaptivebiotech.cora.ui.CoraPage;
import com.adaptivebiotech.cora.utils.PageHelper.MiraExpansionMethod;
import com.adaptivebiotech.cora.utils.PageHelper.MiraLab;
import com.adaptivebiotech.cora.utils.PageHelper.MiraPanel;
import com.adaptivebiotech.cora.utils.PageHelper.MiraType;

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

    public void selectLab (MiraLab lab) {
        String labSelector = "[name='labType']";
        Select dropdown = new Select (scrollTo (waitForElementClickable (locateBy (labSelector))));
        dropdown.selectByVisibleText (lab.text);
        pageLoading ();
        assertEquals (dropdown.getFirstSelectedOption ().getText (), lab.text);
    }

    public void selectType (MiraType type) {
        String typeSelector = "[name='miraType']";
        Select dropdown = new Select (scrollTo (waitForElementClickable (locateBy (typeSelector))));
        dropdown.selectByVisibleText (type.text);
        assertEquals (dropdown.getFirstSelectedOption ().getText (), type.text);
    }

    public void selectExpansionMethod (MiraExpansionMethod expansionMethod) {
        String emSelector = "[name='expansionMethod']";
        Select dropdown = new Select (scrollTo (waitForElementClickable (locateBy (emSelector))));
        dropdown.selectByVisibleText (expansionMethod.text);
        assertEquals (dropdown.getFirstSelectedOption ().getText (), expansionMethod.text);
    }

    public void enterSpecimenAndFind (String specimenId) {
        String specimenInput = "[ng-model='ctrl.specimenNumber']";
        String findSpecimenButton = "[ng-click='ctrl.loadSpecimen(ctrl.specimenNumber)']";
        String removeSpecimenButton = "[ng-click='ctrl.removeSpecimen()']";
        assertTrue (setText (specimenInput, specimenId));
        assertTrue (click (findSpecimenButton));
        assertTrue (hasPageLoaded ());
        assertTrue (isElementVisible (removeSpecimenButton));
    }
}
