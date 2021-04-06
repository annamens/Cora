package com.adaptivebiotech.cora.ui.mira;

import static com.seleniumfy.test.utils.Logging.info;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import java.util.List;
import java.util.function.Function;
import org.openqa.selenium.WebDriver;
import org.testng.util.Strings;
import com.adaptivebiotech.cora.ui.CoraPage;
import com.adaptivebiotech.cora.utils.CoraSelect;
import com.adaptivebiotech.cora.utils.PageHelper.MiraCostCenter;
import com.adaptivebiotech.cora.utils.PageHelper.MiraExpansionMethod;
import com.adaptivebiotech.cora.utils.PageHelper.MiraInputCellType;
import com.adaptivebiotech.cora.utils.PageHelper.MiraLab;
import com.adaptivebiotech.cora.utils.PageHelper.MiraPanel;
import com.adaptivebiotech.cora.utils.PageHelper.MiraQCStatus;
import com.adaptivebiotech.cora.utils.PageHelper.MiraSortType;
import com.adaptivebiotech.cora.utils.PageHelper.MiraStage;
import com.adaptivebiotech.cora.utils.PageHelper.MiraStatus;
import com.adaptivebiotech.cora.utils.PageHelper.MiraType;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class Mira extends CoraPage {

    private final int durationSeconds = 120;
    private final int pollingSeconds  = 10;

    public Mira () {
        staticNavBarHeight = 90;
    }

    @Override
    public void isCorrectPage () {
        assertTrue (isTextInElement (".container .mira-heading", "New MIRA"));
    }

    public void isCorrectPage (String miraId) {
        assertTrue (waitUntilVisible (".mira-header"));
        assertTrue (isTextInElement ("[data-ng-bind='ctrl.mira.miraId']", miraId));
    }

    public void selectPanel (MiraPanel panel) {
        // after selection, the getFirstSelectedOption() stays at "Select..."
        String selector = "[name='panelType']";
        CoraSelect dropdown = new CoraSelect (waitForElementClickable (selector));
        dropdown.selectByVisibleText (panel.name ());
        // wait until the panel is visible below
        waitForPanelText (panel);
        assertTrue (getPanelNamesText ().contains (panel.name ()));
    }

    public void selectLab (MiraLab lab) {
        String labSelector = "[name='labType']";
        selectAndVerifySelection (labSelector, lab.text);
    }

    public void selectType (MiraType type) {
        String typeSelector = "[name='miraType']";
        selectAndVerifySelection (typeSelector, type.text);
    }

    public void selectSortType (MiraSortType miraSortType) {
        String selector = "select[name='sortType']";
        selectAndVerifySelection (selector, miraSortType.text);
    }

    public void selectExpansionMethod (MiraExpansionMethod expansionMethod) {
        String emSelector = "select[name='expansionMethod']";
        selectAndVerifySelection (emSelector, expansionMethod.text);
    }

    public void selectInputCellType (MiraInputCellType inputCellType) {
        String selector = "select[name='inputCellType']";
        selectAndVerifySelection (selector, inputCellType.text);
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

    public String getSpecimenId () {
        String specimenId = "span[ng-bind='::ctrl.mira.expansion.miraSpecimen.number']";
        String specimenIdText = getText (specimenId);
        return specimenIdText;
    }

    public String getExpansionId () {
        String expansionId = "span[ng-bind='::ctrl.mira.expansion.number']";
        String expansionIdText = getText (expansionId);
        return expansionIdText;
    }

    public List <String> getContainerIds () {
        String containerIdField = "span[data-ng-bind='::containerDetail.container.containerNumber']";
        List <String> containerIds = getTextList (containerIdField);
        return containerIds;
    }

    public void selectFirstContainer () {
        String containerCheckboxField = "input[data-ng-model='containerDetail.selected'";
        assertTrue (click (waitForElementsVisible (containerCheckboxField).get (0)));
    }

    public void clickVerifyAllSelectedContainers () {
        String button = "button[ng-click='ctrl.verifyAll()']";
        String checkmark = "img[data-ng-if='::containerDetail.verified']";

        assertTrue (click (button));
        assertTrue (waitUntilVisible (checkmark));
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
        clickUploadAndSave (miraId, MiraLab.AntigenMapProduction);
    }

    public void clickUploadAndSave (String miraId, MiraLab miraLab) {
        String uploadAndSave = "button[ng-click='ctrl.uploadPoolsFile()']";

        assertTrue (click (uploadAndSave));
        moduleLoading ();

        if (miraLab.equals (MiraLab.AntigenMapProduction)) {
            String poolDetail = "span[data-ng-bind='poolDetail.miraId']";
            assertTrue (waitUntilVisible (poolDetail));
            List <String> poolDetailsMiraIds = getTextList (poolDetail);
            assertEquals (poolDetailsMiraIds.get (0), miraId);
        } else if (miraLab.equals (MiraLab.TCRDiscovery)) {
            String containerName = "span[data-ng-bind='poolDetail.containerName']";
            assertTrue (waitUntilVisible (containerName));
            List <String> containerNames = getTextList (containerName);
            assertNotNull (containerNames.get (0));
        }
    }

    public void clickMiraPrepComplete () {
        String miraPrepComplete = ".btn-activate";
        assertTrue (click (miraPrepComplete));
        waitUntilVisible (popupTitle);
        clickPopupOK ();
        pageLoading ();
    }

    public void clickStatusTab () {
        String statusTab = "a[data-ng-click='ctrl.setTab(\\'status\\')']";
        assertTrue (click (statusTab));
        pageLoading ();
        // need to make sure that the table is loaded
        assertTrue (waitUntilVisible ("//table[contains(@class,'history')]"));
        MiraStage currentStage = waitForStatusTable (120, 10);
        assertNotNull (currentStage);
    }

    public void clickTestTab (boolean expectTests) {
        String testTab = "a[data-ng-click='ctrl.setTab(\\'test\\')']";
        assertTrue (click (testTab));
        pageLoading ();

        // if the tests are there then wait until the page has loaded
        if (expectTests) {
            assertTrue (waitUntilVisible ("//div[contains(@class, 'Genologics')]"));
        }
    }

    public void clickDetailsTab (String miraId) {
        String detailsTab = "a[data-ng-click='ctrl.setTab(\\'detail\\')']";
        assertTrue (click (detailsTab));
        pageLoading ();
        String miraIdField = "div[ng-bind='ctrl.mira.miraId']";
        assertTrue (waitUntilVisible (miraIdField));
        assertEquals (getText (miraIdField), miraId);
    }

    public void clickReadyToShip () {
        String readyToShipButton = "button[data-ng-click='ctrl.$scope.$broadcast(\\'mira-ship\\')']";
        assertTrue (click (readyToShipButton));
        clickPopupOK ();
        pageLoading ();
    }

    public void ignorePairseqResult () {
        String ignorePairseqResultButton = "button[data-ng-click='ctrl.ignorePairSeqResult()']";
        assertTrue (click (ignorePairseqResultButton));
        clickPopupOK ();
        pageLoading (); // maybe doing something else weird here
        assertTrue (waitUntilVisible ("div.label-ignored"));
    }

    public boolean waitForStage (MiraStage stage) {
        return waitForStage (stage, durationSeconds, pollingSeconds);
    }

    public boolean waitForStatus (MiraStatus status) {
        return waitForStatus (status, durationSeconds, pollingSeconds);
    }

    public Boolean waitForStage (MiraStage stage, int durationSeconds, int pollingSeconds) {
        Function <WebDriver, Boolean> func = new Function <WebDriver, Boolean> () {
            public Boolean apply (WebDriver driver) {
                MiraStage currentStage = getCurrentStage ();
                if (currentStage == stage) {
                    return true;
                }
                info ("waiting for stage : " + stage);
                refresh ();
                return false;
            }
        };
        return waitForBooleanCondition (durationSeconds, pollingSeconds, func);
    }

    public Boolean waitForStatus (MiraStatus status, int durationSeconds, int pollingSeconds) {
        Function <WebDriver, Boolean> func = new Function <WebDriver, Boolean> () {
            public Boolean apply (WebDriver driver) {
                MiraStatus currentStatus = getCurrentStatus ();
                if (currentStatus == MiraStatus.Stuck) {
                    throw new RuntimeException ("workflow is stuck");
                }
                if (currentStatus == status) {
                    return true;
                }
                info ("waiting for status : " + status);
                refresh ();
                return false;
            }
        };
        return waitForBooleanCondition (durationSeconds, pollingSeconds, func);
    }

    public void setQCStatus (MiraQCStatus status) {
        String dropdown = "select[name='qcStatus']";
        String button = "button[data-ng-click='ctrl.qcComplete(ctrl.mira.qcStatus)']";

        assertTrue (clickAndSelectText (dropdown, status.toString ()));
        assertTrue (click (button));
        clickPopupOK (); // page reloads after you click ok
        pageLoading ();
    }

    public void enterName (String name) {
        String nameField = "input[name='name']";
        assertTrue (setText (nameField, name));
    }

    public void setCostCenter (MiraCostCenter costCenter) {
        String costCenterField = "select[name='costCenter']";
        selectAndVerifySelection (costCenterField, costCenter.text);
    }

    private void waitForPanelText (MiraPanel panel) {
        Function <WebDriver, Boolean> func = new Function <WebDriver, Boolean> () {
            public Boolean apply (WebDriver driver) {
                if (getPanelNamesText ().contains (panel.name ())) {
                    return true;
                }
                info ("waiting for panel to be added: " + panel.name ());
                return false;
            }
        };
        assertTrue (waitForBooleanCondition (120, 10, func));
    }

    private MiraStage waitForStatusTable (int durationSeconds, int pollingSeconds) {
        Function <WebDriver, Boolean> func = new Function <WebDriver, Boolean> () {
            public Boolean apply (WebDriver driver) {
                MiraStage currentStage = getCurrentStage ();
                if (currentStage != null) {
                    return true;
                }
                return false;
            }
        };
        assertTrue (waitForBooleanCondition (durationSeconds, pollingSeconds, func));
        return getCurrentStage ();
    }

    private MiraStage getCurrentStage () {
        String currentStageCell = "//table[contains(@class,'history')]/tbody/tr[1]/td[1]";
        String currentStageCellText = getText (currentStageCell);
        if (Strings.isNullOrEmpty (currentStageCellText)) {
            return null;
        }
        return MiraStage.valueOf (currentStageCellText);
    }

    private MiraStatus getCurrentStatus () {
        String currentStatusCell = "//table[contains(@class,'history')]/tbody/tr[1]/td[2]";
        String currentStatusCellText = getText (currentStatusCell);
        if (Strings.isNullOrEmpty (currentStatusCellText)) {
            return null;
        }
        return MiraStatus.valueOf (currentStatusCellText);
    }

    private List <String> getPanelNamesText () {
        String panelNamesField = "[data-ng-bind='panel.name']";
        List <String> panelNamesText = getTextList (panelNamesField);
        return panelNamesText;
    }

    private void selectAndVerifySelection (String selector, String text) {
        CoraSelect dropdown = new CoraSelect (waitForElementClickable (selector));
        dropdown.selectByVisibleText (text);
        assertEquals (dropdown.getFirstSelectedOption ().getText (), text);
    }

}
