package com.adaptivebiotech.cora.ui.mira;

import static com.seleniumfy.test.utils.Logging.info;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.util.Strings;
import com.adaptivebiotech.cora.dto.PoolDetails;
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

    private final int    durationSeconds         = 120;
    private final int    pollingSeconds          = 10;

    private final String ownerSelector           = "select[name='ownerUsername']";
    private final String SFDCOrderLabelParent    = "//label[text()='immunoSEQ SFDC Order']/..";
    private final String PairSEQOrderLabelParent = "//label[text()='pairSEQ SFDC Order']/..";
    private final String panelInput              = "input[ng-model='ctrl.panelSearchText']";

    public Mira () {
        staticNavBarHeight = 90;
    }

    @Override
    public boolean refresh () {
        assertTrue (super.refresh ());
        if (!isMiraDetailsPage ()) {
            assertTrue (super.refresh ());
        }
        return isMiraDetailsPage ();
    }

    @Override
    public void isCorrectPage () {
        assertTrue (isTextInElement (".container .mira-heading", "New MIRA"));
    }

    public void isCorrectPage (String miraId) {
        assertTrue (waitUntilVisible (".mira-header"));
        if (!isTextInElement ("[data-ng-bind='ctrl.mira.miraId']", miraId)) {
            // try again - sometimes getting a blank page here
            assertTrue (refresh ());
            assertTrue (waitUntilVisible (".mira-header"));
            assertTrue (isTextInElement ("[data-ng-bind='ctrl.mira.miraId']", miraId));
        }
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
        assertNotNull (waitForElementClickable (inputField));
        assertTrue (setText (inputField, containerId));
        assertTrue (click (verifyButton));
        assertTrue (waitUntilVisible (checkmark));
    }

    public void uploadBatchRecord (String batchRecordFile) {
        waitForElement ("input[data-ngf-select*='ctrl.fileHandler']").sendKeys (batchRecordFile);
        pageLoading ();
    }

    public void clickUploadAndSave () {
        String uploadAndSave = "button[ng-click='ctrl.uploadPoolsFile()']";

        assertTrue (click (uploadAndSave));
        moduleLoading ();

        String containerName = "span[data-ng-bind='poolDetail.containerName']";
        assertTrue (waitUntilVisible (containerName));
        List <String> containerNames = getTextList (containerName);
        assertNotNull (containerNames.get (0));
    }

    public void clickMiraPrepComplete () {
        String miraPrepComplete = ".btn-activate";
        assertTrue (click (miraPrepComplete));
        waitForNotification ();
        waitUntilVisible (popupTitle);
        clickPopupOK ();
        waitForNotification ();
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
        pageLoading ();
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
                info ("waiting for stage : " + stage);
                try {
                    MiraStage currentStage = getCurrentStage ();
                    if (currentStage == stage) {
                        return true;
                    }
                } catch (Throwable t) {
                    // just return false
                    info (t.toString ());
                }
                assertTrue (refresh ());
                return false;
            }
        };
        return waitForBooleanCondition (durationSeconds, pollingSeconds, func);
    }

    public Boolean waitForStatus (MiraStatus status, int durationSeconds, int pollingSeconds) {
        Function <WebDriver, Boolean> func = new Function <WebDriver, Boolean> () {
            public Boolean apply (WebDriver driver) {
                info ("waiting for status: " + status);
                MiraStatus currentStatus = null;
                try {
                    currentStatus = getCurrentStatus ();
                    if (currentStatus == MiraStatus.Stuck) {
                        throw new RuntimeException ("workflow is stuck");
                    }
                    if (currentStatus == status) {
                        return true;
                    }
                } catch (Throwable t) {
                    if (currentStatus == MiraStatus.Stuck) {
                        throw t;
                    }
                    info (t.toString ());
                }
                assertTrue (refresh ());
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

    public void waitForSubstatusTextContains (String text) {
        String currentSubstatusText = "//table[contains(@class,'history')]/tbody/tr[1]/td[3]";

        assertTrue (isTextInElement (currentSubstatusText, text));
    }

    @Override
    public List <String> getTextList (String target) {
        try {
            List <String> rv = super.getTextList (target);
            return rv;
        } catch (StaleElementReferenceException e) {
            // try again
            info ("caught stale element reference exception in getTextList");
            doWait (1000);
            List <String> rv = super.getTextList (target);
            return rv;
        }
    }

    public boolean isNewMiraPage () {
        return waitUntilVisible (".container .mira-heading");
    }

    public boolean isMiraDetailsPage () {
        return waitUntilVisible (".mira-header");
    }

    public String getPopupTextAndWait () {
        String popup = "span[ng-bind-html='notification.msg']";
        String popupText = getText (popup);
        assertTrue (waitForElementInvisible (popup));
        return popupText;
    }

    // owner is always truncated to 20 chars
    public void verifyOwner (String ownerName) {
        String selectedText = waitForSelectedText (ownerSelector);
        assertEquals (selectedText, truncateOwnerName (ownerName));
    }

    public void setOwner (String ownerName) {
        String truncatedOwnerName = truncateOwnerName (ownerName);
        CoraSelect owner = new CoraSelect (waitForElementClickable (ownerSelector));
        owner.selectByVisibleText (truncatedOwnerName);

        verifyOwner (truncatedOwnerName);
    }

    /**
     * post MPC
     * this is the truncated owner name
     */
    public String getExperimentOwner () {
        String owner = "div[ng-bind='ctrl.mira.ownerUsername']";
        return getText (owner);
    }

    public void verifyPanelTypeAhead (String panelName) {
        String dropdownEntries = "//a[contains(@class, 'dropdown-item')]";
        assertTrue (click (panelInput));
        List <String> initialDropdownText = getTextList (dropdownEntries);
        assertTrue (setText (panelInput, panelName.substring (0, 1)));
        List <String> secondDropdownText = getTextList (dropdownEntries);
        for (String dropdownText : secondDropdownText) {
            assertTrue (initialDropdownText.contains (dropdownText));
            assertEquals (panelName.substring (0, 1), dropdownText.substring (0, 1));
        }
        assertTrue (clear (panelInput));
        assertTrue (click (panelInput)); // need to click again

    }

    public void choosePanelByName (String panelName) {
        String dropdownEntryBase = "//a[contains(@class, 'dropdown-item') and text()='%s']";
        String dropdownEntry = String.format (dropdownEntryBase, panelName);
        String chosenPanel = "//div[contains(@class, 'panel-section')]/div/div[1]/span";
        assertTrue (click (panelInput));
        assertTrue (setText (panelInput, panelName));
        assertTrue (click (dropdownEntry));
        assertTrue (waitUntilVisible (chosenPanel));
        assertEquals (getText (chosenPanel), panelName);
        assertTrue (isPanelInputInvisible ());
    }

    public void enterNotes (String text) {
        String notesField = "textarea[ng-model='ctrl.miraEntry.notes']";
        assertTrue (setText (notesField, text));
    }

    public MiraLab getMiraLab () {
        String labField = "div[ng-bind='ctrl.mira.labTypeDisplay']";
        String labText = getText (labField);
        for (MiraLab miraLab : MiraLab.values ()) {
            if (miraLab.text.equals (labText)) {
                return miraLab;
            }
        }
        return null;
    }

    public MiraType getMiraType () {
        String typeField = "div[ng-bind='ctrl.mira.miraType']";
        String typeSelector = "select[name='miraType']";
        String text;
        if (waitUntilVisible (typeSelector, 1)) {
            text = waitForSelectedText (typeSelector);
        } else {
            text = getText (typeField);
        }
        for (MiraType miraType : MiraType.values ()) {
            if (miraType.name ().equals (text)) {
                return miraType;
            }
        }
        info ("found unknown mira type text: " + text);
        return null;
    }

    public boolean isPanelInputVisible () {
        return waitUntilVisible (panelInput);
    }

    public boolean isPanelInputInvisible () {
        return waitForElementInvisible (panelInput);
    }

    public String getMiraPanelText () {
        String chosenPanel = "//div[contains(@class, 'panel-section')]/div/div[1]/span";
        return getText (chosenPanel);
    }

    public MiraCostCenter getCostCenter (boolean expectSelector) {
        String costCenterSelector = "select[name='costCenter']";
        String costCenterField = "div[ng-bind='ctrl.mira.costCenter']";
        String costCenterText;
        if (expectSelector) {
            costCenterText = waitForSelectedText (costCenterSelector);
        } else {
            costCenterText = getText (costCenterField);
        }

        for (MiraCostCenter costCenter : MiraCostCenter.values ()) {
            if (costCenter.text.equals (costCenterText)) {
                return costCenter;
            }
        }
        return null;
    }

    public MiraExpansionMethod getExpansionMethod () {
        String expansionMethodSelector = "select[name='expansionMethod']";
        String expansionMethodField = "//label[text()='Expansion Method' and not(contains(@class, 'control-label'))]/../div";
        String text;
        if (isElementVisible (expansionMethodSelector)) {
            text = waitForSelectedText (expansionMethodSelector);
        } else {
            text = getText (expansionMethodField);
        }
        for (MiraExpansionMethod em : MiraExpansionMethod.values ()) {
            if (em.text.equals (text)) {
                return em;
            }
        }
        return null;
    }

    public MiraSortType getSortType () {
        String selector = "select[name='sortType']";
        String field = "//label[text()='Sort Type' and not(contains(@class, 'control-label'))]/../div";
        String text;
        if (isElementVisible (selector)) {
            text = waitForSelectedText (selector);
        } else {
            text = getText (field);
        }
        for (MiraSortType mst : MiraSortType.values ()) {
            if (mst.text.equals (text)) {
                return mst;
            }
        }
        return null;
    }

    public MiraInputCellType getInputCellType () {
        String selector = "select[name='inputCellType']";
        String field = "//label[text()='Input Cell Type' and not(contains(@class, 'control-label'))]/../div";
        String text;
        if (isElementVisible (selector)) {
            text = waitForSelectedText (selector);
        } else {
            text = getText (field);
        }
        for (MiraInputCellType ict : MiraInputCellType.values ()) {
            if (ict.text.equals (text)) {
                return ict;
            }
        }
        return null;
    }

    public String getNotes () {
        String notesField = "textarea[ng-model='ctrl.mira.notes']";
        return readInput (notesField);
    }

    public String getName () {
        String nameField = "input[ng-model='ctrl.mira.name']";
        return readInput (nameField);
    }

    public String getExperimentName () {
        String field = "div[ng-bind='ctrl.mira.name']";
        return getText (field);
    }

    public void enterExpansionIdAndFind (String expansionId) {
        String input = "input[ng-model='ctrl.expansionNumber']";
        String findExpansion = "button[ng-click='ctrl.loadExpansion(ctrl.expansionNumber)']";
        String displayedExpansionId = "span[ng-bind='ctrl.miraEntry.expansion.number']";
        assertTrue (setText (input, expansionId));
        assertTrue (click (findExpansion));
        assertTrue (hasPageLoaded ());
        assertTrue (waitUntilVisible ("button[ng-click='ctrl.removeSpecimen()']"));
        assertEquals (getText (displayedExpansionId), expansionId);
    }

    public void clickRemoveSpecimen () {
        String removeSpecimen = "button[ng-click='ctrl.removeSpecimen()']";
        String specimenInput = "[ng-model='ctrl.specimenNumber']";
        assertTrue (click (removeSpecimen));
        clickPopupOK ();
        pageLoading ();
        assertTrue (waitUntilVisible (specimenInput));
    }

    public void clickRemovePanel () {
        String trashIcon = "span[data-ng-click='ctrl.removePanel($index)']";
        String panelInput = "input[ng-model='ctrl.panelSearchText']";
        assertTrue (click (trashIcon));
        assertTrue (waitUntilVisible (panelInput));
    }

    public void clickRemovePanelMiraDetails () {
        String editIcon = "span[data-ng-click='ctrl.removePanel()']";
        String panelInput = "input[ng-model='ctrl.panelSearchText']";
        assertTrue (click (editIcon));
        assertTrue (waitUntilVisible (panelInput));
    }

    public void verifyErrorMessagesAreVisible (Map <String, String> requiredFieldMessages) {
        String locatorBase = "span[ng-bind='ctrl.errors.%s']";
        for (String fieldName : requiredFieldMessages.keySet ()) {
            String expectedMessage = requiredFieldMessages.get (fieldName);
            String locator = String.format (locatorBase, fieldName);
            String errorMessage = getText (locator);
            info ("found error message: " + errorMessage);
            assertEquals (errorMessage, expectedMessage);
        }
    }

    public boolean isLabelInExperimentSection (String text, boolean isRequired) {
        String optionalLocatorBase = "//h2[text()='Experiment(s)']/..//label[text()='%s' and not(contains(@class,'required'))]";
        String requiredLocatorBase = "//h2[text()='Experiment(s)']/..//label[text()='%s' and contains(@class,'required')]";

        String locator = "";
        if (isRequired) {
            locator = String.format (requiredLocatorBase, text);
        } else {
            locator = String.format (optionalLocatorBase, text);
        }

        return waitUntilVisible (locator);
    }

    public int countLabelsInExperimentSection () {
        String labelLocator = "//h2[text()='Experiment(s)']/..//label";
        List <WebElement> labels = waitForElements (labelLocator);
        return labels.size ();
    }

    public void verifyFieldInSpecimenDetailsSection (String label, String text) {
        String specimenDetailLabelBase = "//div[contains(@class, 'specimen-details-section')]//label[text()='%s']";
        String locator = String.format (specimenDetailLabelBase, label);
        String textLocator = locator + "/../div";
        assertTrue (waitUntilVisible (locator));
        if (text != null) {
            assertTrue (isTextInElement (textLocator, text));
        }
    }

    public boolean isLabelInMiraSection (String text, boolean isRequired) {
        String optionalLocatorBase = "//h2[text()='MIRA']/../..//label[text()='%s' and not(contains(@class,'required'))]";
        String requiredLocatorBase = "//h2[text()='MIRA']/../..//label[text()='%s' and contains(@class,'required')]";

        String locator = "";
        if (isRequired) {
            locator = String.format (requiredLocatorBase, text);
        } else {
            locator = String.format (optionalLocatorBase, text);
        }
        List <WebElement> labels = waitForElements (locator);
        // for some of these are 2 labels, one invisible one visible
        for (WebElement webElement : labels) {
            if (webElement.isDisplayed ()) {
                return true;
            }
        }

        return false;

    }

    public void verifySpecimenDetailsVisible () {
        String specimenDetails = "//div[contains(@class, 'specimen-details-section')]";
        assertTrue (waitUntilVisible (specimenDetails));
    }

    public void verifySpecimenDetailsInvisible () {
        String specimenDetails = "//div[contains(@class, 'specimen-details-section')]";
        assertTrue (waitForElementInvisible (specimenDetails));
    }

    public boolean isSpecimenDetailsTableVisible () {
        String table = "//div[@class='mira-specimen-list-details']/table";
        return waitUntilVisible (table);
    }

    public void waitForStageOnDetailsPage (MiraStage miraStage) {
        Function <WebDriver, Boolean> func = new Function <WebDriver, Boolean> () {
            public Boolean apply (WebDriver driver) {
                try {
                    if (getStageText ().equals (miraStage.name ())) {
                        return true;
                    }
                } catch (Throwable t) {
                    // just return false
                }
                assertTrue (refresh ());
                return false;
            }
        };
        waitForBooleanCondition (300, 30, func);
    }

    public List <String> getExpansionMethodTexts () {
        String expansionMethodField = "select[name='expansionMethod']";
        return getOptionTexts (expansionMethodField);
    }

    public List <String> getCostCenterTexts () {
        String costCenterField = "select[name='costCenter']";
        return getOptionTexts (costCenterField);
    }

    public String getMIRAOccupancy () {
        String locator = "span[data-ng-bind='ctrl.mira.panelOccupancy']";
        return getText (locator);
    }

    public void clickSaveNewMira () {
        String saveButton = "button[ng-click='ctrl.save()']";
        assertTrue (click (saveButton));
        pageLoading ();
    }

    public void clickSaveMiraDetails (boolean expectPopup) {
        String saveButton = "//button[text()='Save']";
        assertTrue (click (saveButton));
        if (expectPopup) {
            clickPopupOK ();
        }
        waitForNotification ();
    }

    public void enterExperimentName (String name) {
        String nameField = "input[name='name']";
        scrollTo (waitForElementClickable (nameField));
        assertTrue (clear (nameField));
        assertTrue (setText (nameField, name));
    }

    public void clearExperimentName () {
        String experimentName = "input[name='name']";
        WebElement inputField = waitForElementVisible (experimentName);

        scrollTo (inputField);
        assertTrue (click (inputField));

        inputField.clear (); // this gives the intended behavior
    }

    public void clickMiraPrepCompleteExpectFailure () {
        String miraPrepComplete = ".btn-activate";
        assertTrue (click (miraPrepComplete));
        waitForNotification ();
    }

    public String truncateOwnerName (String ownerName) {
        String truncatedOwnerName = ownerName;
        if (truncatedOwnerName.length () > 20) {
            truncatedOwnerName = truncatedOwnerName.substring (0, 20);
        }
        return truncatedOwnerName;
    }

    public boolean isSFDCPresent () {
        return waitUntilVisible (SFDCOrderLabelParent);
    }

    public boolean isSFDCNotPresent () {
        return waitForElementInvisible (SFDCOrderLabelParent);
    }

    public boolean isPairSEQPresent () {
        return waitUntilVisible (PairSEQOrderLabelParent);
    }

    public boolean isPairSEQNotPresent () {
        return waitForElementInvisible (PairSEQOrderLabelParent);
    }

    public String getSalesforceOrderUrl () {
        String locator = "//div[@ng-if='ctrl.mira.salesforceOrderUrl']/a";
        String url = getAttribute (locator, "href");
        return url;
    }

    public String getImmunoSEQSFDCOrderName () {
        String locator = "//div[@ng-if='ctrl.mira.salesforceOrderUrl']/a";
        return getText (locator);
    }

    public String getSalesforceOrderNumber () {
        String locator = "div[ng-bind='ctrl.mira.salesforceOrderNumber']";
        return getText (locator);
    }

    public String getPairSEQOrderUrl () {
        String locator = "//div[@ng-if='ctrl.mira.salesforcePairSeqOrderUrl']/a";
        String url = getAttribute (locator, "href");
        return url;
    }

    public String getPairSEQOrderName () {
        String locator = "//div[@ng-if='ctrl.mira.salesforcePairSeqOrderUrl']/a";
        return getText (locator);
    }

    public String getPairSEQOrderNumber () {
        String locator = "div[ng-bind='ctrl.mira.pairSeqSalesforceOrderNumber']";
        return getText (locator);
    }

    public List <String> getAttachmentNames () {
        String locator = "span[ng-bind='attachment.name']";
        List <String> texts = getTextList (locator);
        return texts;
    }

    public List <String> getWorkflowNames () {
        return getTextList ("td[ng-bind='::miraTest.workflowName']");
    }

    public void uploadBatchRecordExpectFailure (String batchRecordFile) {
        waitForElement ("input[data-ngf-select*='ctrl.fileHandler']").sendKeys (batchRecordFile);
        pageLoading ();
        waitForDangerNotification ();

    }

    /**
     * go to the miras list from an edited MIRA
     */
    public void leaveEditedMIRA () {
        clickMiras ();
        clickPopupOK ();
        new MirasList ().isCorrectPage ();
    }

    public String getDrilldownURLBySubstatusMessage (String msg) {
        String base = "//span[contains(@class, 'substatus-message') and text()='%s']/../a[contains(@class, 'details-url')]";
        String locator = String.format (base, msg);
        return getAttribute (locator, "href");
    }

    public String getFirstDrilldownUrlByStage (MiraStage stage) {
        String base = "//td[text()='%s']/../td[3]/a";
        String locator = String.format (base, stage.name ());
        return getAttribute (locator, "href");
    }

    public String getOrderNumberFromTests () {
        String locator = "span[ng-bind='::miraTest.orderNumber']";
        return getText (locator);
    }

    public List <PoolDetails> getPoolDetailsPreview () {
        List <PoolDetails> rv = new ArrayList <> ();
        String poolDetailRows = "//div[contains(@class, 'poolDetailsDialog')]/div[contains(@class, 'modal-body')]/div[2]/div[contains(@class,'ng-scope')]";
        List <WebElement> rows = waitForElementsVisible (poolDetailRows);
        for (WebElement row : rows) {
            List <WebElement> cols = waitForElementsVisible (row, "div");
            PoolDetails poolDetails = getPoolDetailsFromTableRow (cols);
            rv.add (poolDetails);
        }
        return rv;
    }

    public List <PoolDetails> getPoolDetails () {
        List <PoolDetails> rv = new ArrayList <> ();
        String poolDetailRows = "//div[contains(@class, 'mira-pools')]/div/div[@class='specimen-summary-table']/table/tbody/tr";
        List <WebElement> rows = waitForElementsVisible (poolDetailRows);
        for (WebElement row : rows) {
            List <WebElement> cols = waitForElementsVisible (row, "td");
            PoolDetails poolDetails = getPoolDetailsFromTableRow (cols);
            rv.add (poolDetails);
        }
        return rv;
    }

    public String getQCCommentsFromSubstatus () {
        String locator = "//table[contains(@class, 'history')]/tbody/tr/td[text()='MIRAQC']/../td[text()[contains(.,'MIRA_QC_COMPLETE')]]/span[contains(@class, 'substatus-message')]";
        return getText (locator);
    }

    public String getQCComments () {
        String comment = "div[ng-bind='ctrl.mira.qcComments']";
        return getText (comment);
    }

    private PoolDetails getPoolDetailsFromTableRow (List <WebElement> cols) {
        PoolDetails poolDetails = new PoolDetails ();

        poolDetails.setSampleName (getText (cols.get (0)));
        poolDetails.setHoldingContainerName (getText (cols.get (1)));
        poolDetails.setContainerName (getText (cols.get (2)));
        poolDetails.setPool (getText (cols.get (3)));
        poolDetails.setCellCount (Double.parseDouble (getText (cols.get (4))));
        poolDetails.setNotes (getText (cols.get (5)));
        return poolDetails;
    }

    private void waitForDangerNotification () {
        String popup = "//div[contains(@class, 'alert-danger')]";
        WaitForPopupFunction func = new WaitForPopupFunction (popup);
        Wait <WebDriver> wait = new FluentWait <> (this.getDriver ())
                                                                     .withTimeout (Duration.ofSeconds (120))
                                                                     .pollingEvery (Duration.ofMillis (100));
        wait.until (func);
    }

    private void waitForNotification () {
        String popup = "span[ng-bind-html='notification.msg']";
        WaitForPopupFunction func = new WaitForPopupFunction (popup);

        Wait <WebDriver> wait = new FluentWait <> (this.getDriver ())
                                                                     .withTimeout (Duration.ofSeconds (120))
                                                                     .pollingEvery (Duration.ofMillis (100));
        wait.until (func);
    }

    private List <String> getOptionTexts (String field) {
        CoraSelect emSelector = new CoraSelect (waitForElementVisible (field));
        List <WebElement> options = emSelector.getOptions ();
        int count = 0;
        while (count < 5 && options.size () == 1) {
            doWait (5000);
            options = emSelector.getOptions ();
            count++;
        }
        List <String> rv = new ArrayList <> ();
        for (WebElement webElement : options) {
            String optionText = getText (webElement);
            rv.add (optionText);
        }
        return rv;
    }

    private String waitForSelectedText (String locator) {

        Function <WebDriver, Boolean> func = new Function <WebDriver, Boolean> () {
            public Boolean apply (WebDriver driver) {
                String selectedText = getSelectedText (locator);
                // info ("selected text is: " + selectedText);
                return selectedText != null && !selectedText.equals ("");
            }
        };

        try {
            waitForBooleanCondition (10, 2, func);
        } catch (Throwable t) {
            // field is not set
        }

        return getSelectedText (locator);
    }

    private String getSelectedText (String locator) {
        CoraSelect selector = new CoraSelect (waitForElementVisible (locator));
        WebElement selectedOption = selector.getFirstSelectedOption ();
        String selectedText = getText (selectedOption);
        return selectedText;
    }

    private String getStageText () {
        String stage = "span[data-ng-bind='ctrl.mira.stageName']";
        return getText (stage);
    }

    private boolean waitUntilVisible (String target, int timeoutInSeconds) {
        waitForAjaxCalls ();
        int sleepInMillis = 100;
        By by = locateBy (target);
        try {
            WebDriverWait webDriverWait = new WebDriverWait (getDriver (), timeoutInSeconds, sleepInMillis);
            WebElement webElement = webDriverWait.until (ExpectedConditions.visibilityOfElementLocated (by));
            return webElement.isDisplayed ();
        } catch (Exception e) {
            return false;
        }
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

    private class WaitForPopupFunction implements Function <WebDriver, Boolean> {

        private String locator;

        public WaitForPopupFunction (String locator) {
            this.locator = locator;
        }

        public Boolean apply (WebDriver webDriver) {
            try {
                if (isElementPresent (locator)) {
                    assertTrue (waitForElementInvisible (locator));
                    return true;
                }
            } catch (NoSuchElementException e) {
                info ("waiting for notification...");
            }
            return false;
        }

    }

}
