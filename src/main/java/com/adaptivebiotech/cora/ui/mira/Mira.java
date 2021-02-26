package com.adaptivebiotech.cora.ui.mira;

import static com.seleniumfy.test.utils.Logging.info;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import java.util.List;
import org.openqa.selenium.support.ui.Select;
import com.adaptivebiotech.cora.ui.CoraPage;
import com.adaptivebiotech.cora.utils.CoraSelect;
import com.adaptivebiotech.cora.utils.PageHelper.MiraExpansionMethod;
import com.adaptivebiotech.cora.utils.PageHelper.MiraLab;
import com.adaptivebiotech.cora.utils.PageHelper.MiraPanel;
import com.adaptivebiotech.cora.utils.PageHelper.MiraStage;
import com.adaptivebiotech.cora.utils.PageHelper.MiraStatus;
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

    public void clickSave (boolean isActive) {
        String saveButton = isActive ? "//button[text()='Save']" : "button[ng-click='ctrl.save()']";
        assertTrue (click (saveButton));
        pageLoading ();
        if (isActive) {
            clickPopupOK ();
            // pageLoading() does not work here
            assertTrue (waitUntilVisible (".loading-overlay"));
            assertTrue (waitForElementInvisible (".loading-overlay"));
        }
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

    public void verifyContainerId (String containerId) {
        String inputField = "input[ng-model='ctrl.containerNumber']";
        String verifyButton = "button[ng-click='ctrl.verify()']";
        String checkmark = "img[data-ng-if='::containerDetail.verified']";
        assertTrue (setText (inputField, containerId));
        assertTrue (click (verifyButton));
        assertTrue (waitUntilVisible (checkmark));
    }

    public void uploadBatchRecord (String batchRecordFile) {
        waitForElement ("input[data-ngf-select*='ctrl.fileHandler']").sendKeys (batchRecordFile);
        pageLoading ();
    }

    public void clickUploadAndSave (String miraId) {
        String uploadAndSave = "button[ng-click='ctrl.uploadPoolsFile()']";
        String poolDetail = "span[data-ng-bind='poolDetail.miraId']";

        assertTrue (click (uploadAndSave));
        moduleLoading ();

        assertTrue (waitUntilVisible (poolDetail));
        List <String> poolDetailsMiraIds = getTextList (poolDetail);
        assertEquals (poolDetailsMiraIds.get (0), miraId);
    }

    public void clickMiraPrepComplete () {
        String miraPrepComplete = ".btn-activate";
        assertTrue (click (miraPrepComplete));
        waitUntilVisible (".modal-title");
        clickPopupOK ();
        pageLoading ();
    }

    public void clickStatusTab () {
        String statusTab = "a[data-ng-click='ctrl.setTab(\\'status\\')']";
        assertTrue (click (statusTab));
        pageLoading ();
        // need to make sure that the table is loaded
        MiraStage currentStage = getCurrentStage ();
        int count = 0;
        while (count < 20 && currentStage == null) {
            count++;
            info ("waiting for status table to load");
            doWait (10000);
            currentStage = getCurrentStage ();
        }
        assertNotNull (currentStage);
    }

    public MiraStage getCurrentStage () {
        String currentStageCell = "//table[contains(@class,'history')]/tbody/tr[1]/td[1]";
        String currentStageCellText = getText (currentStageCell);
        return currentStageCellText == null ? null : MiraStage.valueOf (currentStageCellText);
    }

    public MiraStatus getCurrentStatus () {
        String currentStatusCell = "//table[contains(@class,'history')]/tbody/tr[1]/td[2]";
        String currentStatusCellText = getText (currentStatusCell);
        return currentStatusCellText == null ? null : MiraStatus.valueOf (currentStatusCellText);
    }
    
    private List <String> getPanelNamesText () {
        String panelNamesField = "[data-ng-bind='panel.name']";
        List <String> panelNamesText = getTextList (panelNamesField);
        return panelNamesText;
    }

}
