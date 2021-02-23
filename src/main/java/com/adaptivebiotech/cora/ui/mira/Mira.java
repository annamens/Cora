package com.adaptivebiotech.cora.ui.mira;

import static com.seleniumfy.test.utils.Logging.info;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import java.util.List;
import org.openqa.selenium.support.ui.Select;
import com.adaptivebiotech.cora.ui.CoraPage;
import com.adaptivebiotech.cora.utils.CoraSelect;
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
        // wait until the panel is visible below
        int count = 0;
        while (count < 10 && !getPanelNamesText ().contains (panel.name ())) {
            info ("waiting for panel to be added: " + panel.name ());
            count++;
            doWait (10000);
        }
        assertTrue (getPanelNamesText ().contains (panel.name ()));
    }

    public void selectLab (MiraLab lab) {
        String labSelector = "[name='labType']";
        CoraSelect dropdown = new CoraSelect (scrollTo (waitForElementClickable (locateBy (labSelector))));
        dropdown.selectByVisibleText (lab.text);
        assertEquals (dropdown.getFirstSelectedOption ().getText (), lab.text);
    }

    public void selectType (MiraType type) {
        String typeSelector = "[name='miraType']";
        CoraSelect dropdown = new CoraSelect (scrollTo (waitForElementClickable (locateBy (typeSelector))));
        dropdown.selectByVisibleText (type.text);
        assertEquals (dropdown.getFirstSelectedOption ().getText (), type.text);
    }

    public void selectExpansionMethod (MiraExpansionMethod expansionMethod) {
        String emSelector = "[name='expansionMethod']";
        CoraSelect dropdown = new CoraSelect (scrollTo (waitForElementClickable (locateBy (emSelector))));
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
        assertTrue (waitUntilVisible (removeSpecimenButton));
    }

    public void clickSave () {
        String saveButton = "button[ng-click='ctrl.save()']";
        assertTrue (click (saveButton));
        assertTrue (hasPageLoaded ());
    }

    public String getMiraId () {
        String idText = "span[data-ng-bind='ctrl.mira.miraId']";
        String miraId = getText (idText);
        return miraId;
    }

    public List <String> getContainerIds () {
        String containerIdField = "span[data-ng-bind='::containerDetail.container.containerNumber']";
        List <String> containerIds = getTextList (containerIdField);
        return containerIds;
    }

    public List <String> getPanelNamesText () {
        String panelNamesField = "[data-ng-bind='panel.name']";
        List <String> panelNamesText = getTextList (panelNamesField);
        return panelNamesText;
    }

    public void verifyContainerId (String containerId) {
        String inputField = "input[ng-model='ctrl.containerNumber']";
        String verifyButton = "button[ng-click='ctrl.verify()']";
        String checkmark = "img[data-ng-if='::containerDetail.verified']";
        assertTrue (setText (inputField, containerId));
        assertTrue (click (verifyButton));
        assertTrue (waitUntilVisible (checkmark));
    }
    
    public void uploadBatchRecord (String path) {
        String uploadButton = "button[data-ng-model='ctrl.poolsFiles']";
        assertTrue(click(uploadButton));
        
    }

}
