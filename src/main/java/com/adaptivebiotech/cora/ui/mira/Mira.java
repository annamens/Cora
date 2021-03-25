package com.adaptivebiotech.cora.ui.mira;

import static com.seleniumfy.test.utils.Logging.info;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.function.Function;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.openqa.selenium.WebDriver;
import com.adaptivebiotech.cora.ui.CoraPage;
import com.adaptivebiotech.cora.utils.CoraSelect;
import com.adaptivebiotech.cora.utils.PageHelper.MiraExpansionMethod;
import com.adaptivebiotech.cora.utils.PageHelper.MiraLab;
import com.adaptivebiotech.cora.utils.PageHelper.MiraPanel;
import com.adaptivebiotech.cora.utils.PageHelper.MiraQCStatus;
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
        CoraSelect dropdown = new CoraSelect (waitForElementClickable (labSelector));
        dropdown.selectByVisibleText (lab.text);
        assertEquals (dropdown.getFirstSelectedOption ().getText (), lab.text);
    }

    public void selectType (MiraType type) {
        String typeSelector = "[name='miraType']";
        CoraSelect dropdown = new CoraSelect (waitForElementClickable (typeSelector));
        dropdown.selectByVisibleText (type.text);
        assertEquals (dropdown.getFirstSelectedOption ().getText (), type.text);
    }

    public void selectExpansionMethod (MiraExpansionMethod expansionMethod) {
        String emSelector = "[name='expansionMethod']";
        CoraSelect dropdown = new CoraSelect (waitForElementClickable (emSelector));
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

    public String createNewBatchRecord (String miraId) {
        String xlFolder = "MIRA/";
        String basePath = ClassLoader.getSystemResource (xlFolder).getPath ();
        String originalBatchRecord = basePath + "M-xx_Batch_Record.xlsx";
        String newBatchRecord = basePath + miraId + "_Batch_Record.xlsx";
        String worksheetName = "Experiment Request";

        try {
            FileInputStream inputStream = new FileInputStream (new File (originalBatchRecord));
            Workbook workbook = WorkbookFactory.create (inputStream);
            FileOutputStream outputStream = FileUtils.openOutputStream (new File (newBatchRecord));
            Sheet sheet = workbook.getSheet (worksheetName);
            sheet.protectSheet (null);
            Cell cell = sheet.getRow (2).getCell (0);
            cell.setBlank ();
            cell.setCellValue (miraId);
            workbook.getCreationHelper ().createFormulaEvaluator ().evaluateAll ();
            workbook.write (outputStream);
            outputStream.close ();
            inputStream.close ();
            info ("created new mira batch record file " + newBatchRecord);
        } catch (Exception e) {
            throw new RuntimeException (e);
        }

        return newBatchRecord;
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
        return currentStageCellText == null ? null : MiraStage.valueOf (currentStageCellText);
    }

    private MiraStatus getCurrentStatus () {
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
