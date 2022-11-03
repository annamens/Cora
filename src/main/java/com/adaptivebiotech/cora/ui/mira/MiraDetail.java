/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.ui.mira;

import static com.seleniumfy.test.utils.Logging.info;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.testng.util.Strings;
import com.adaptivebiotech.cora.dto.PoolDetails;
import com.adaptivebiotech.cora.utils.PageHelper.MiraLab;
import com.adaptivebiotech.cora.utils.PageHelper.MiraStage;
import com.adaptivebiotech.cora.utils.PageHelper.MiraStatus;
import com.adaptivebiotech.cora.utils.PageHelper.MiraType;

public class MiraDetail extends Mira {

    private final int      durationSeconds         = 120;
    private final int      pollingSeconds          = 10;
    private final int      pollingMillis           = 25;
    protected final String SFDCOrderLabelParent    = "//label[text()='immunoSEQ SFDC Order']/..";
    protected final String PairSEQOrderLabelParent = "//label[text()='pairSEQ SFDC Order']/..";

    public void isCorrectPage (String miraId) {
        assertTrue (waitUntilVisible (".mira-header"));
        if (!isTextInElement ("[data-ng-bind='ctrl.mira.miraId']", miraId)) {
            // try again - sometimes getting a blank page here
            assertTrue (refresh ());
            assertTrue (waitUntilVisible (".mira-header"));
            assertTrue (isTextInElement ("[data-ng-bind='ctrl.mira.miraId']", miraId));
        }
    }

    @Override
    public boolean refresh () {
        assertTrue (super.refresh ());
        if (!isMiraDetailsPage ()) {
            assertTrue (super.refresh ());
        }
        return isMiraDetailsPage ();
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
        return waitUntil (durationSeconds * 1000, pollingSeconds * 1000, func);
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
        return waitUntil (durationSeconds * 1000, pollingSeconds * 1000, func);
    }

    public void waitForSubstatusTextContains (String text) {
        String currentSubstatusText = "//table[contains(@class,'history')]/tbody/tr[1]/td[3]";

        assertTrue (isTextInElement (currentSubstatusText, text));
    }

    /**
     * post MPC
     * this is the truncated owner name
     */
    public String getExperimentOwner () {
        String owner = "div[ng-bind='ctrl.mira.ownerUsername']";
        return getText (owner);
    }

    public MiraLab getMiraLab () {
        String labField = "div[ng-bind='ctrl.mira.labTypeDisplay']";
        String labText = getText (labField);
        if (Strings.isNullOrEmpty (labText)) {
            return null;
        }
        return MiraLab.getMiraLab (labText);
    }

    public MiraType getMiraType () {
        String typeField = "div[ng-bind='ctrl.mira.miraType']";
        String name = getText (typeField);

        return MiraType.valueOf (name);
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

    public void clickRemovePanel () {
        String editIcon = "span[data-ng-click='ctrl.removePanel()']";
        String panelInput = "input[ng-model='ctrl.panelSearchText']";
        assertTrue (click (editIcon));
        assertTrue (waitUntilVisible (panelInput));
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
        waitUntil (300000, 30000, func);
    }

    public void clickMiraPrepCompleteExpectFailure () {
        String miraPrepComplete = ".btn-activate";
        assertTrue (click (miraPrepComplete));
        waitForNotification ();
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

    public List <String> getWorkflowNames () {
        return getTextList ("td[ng-bind='::miraTest.workflowName']");
    }

    public void uploadBatchRecordExpectFailure (String batchRecordFile) {
        waitForElement ("input[data-ngf-select*='ctrl.fileHandler']").sendKeys (batchRecordFile);
        pageLoading ();
        waitForDangerNotification ();

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

    public List <String> getAttachmentNames () {
        String locator = "span[ng-bind='attachment.name']";
        List <String> texts = getTextList (locator);
        return texts;
    }

    protected void waitForDangerNotification () {
        String popup = "//div[contains(@class, 'alert-danger')]";
        WaitForPopupFunction func = new WaitForPopupFunction (popup);
        Wait <WebDriver> wait = new FluentWait <> (this.getDriver ())
                                                                     .withTimeout (Duration.ofSeconds (durationSeconds))
                                                                     .pollingEvery (Duration.ofMillis (pollingMillis));
        wait.until (func);
    }

    protected void waitForNotification () {
        String popup = "span[ng-bind-html='notification.msg']";
        WaitForPopupFunction func = new WaitForPopupFunction (popup);

        Wait <WebDriver> wait = new FluentWait <> (this.getDriver ())
                                                                     .withTimeout (Duration.ofSeconds (durationSeconds))
                                                                     .pollingEvery (Duration.ofMillis (pollingMillis));
        wait.until (func);
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
        assertTrue (waitUntil (durationSeconds * 1000, pollingSeconds * 1000, func));
        return getCurrentStage ();
    }

    protected MiraStage getCurrentStage () {
        String currentStageCell = "//table[contains(@class,'history')]/tbody/tr[1]/td[1]";
        String currentStageCellText = getText (currentStageCell);
        if (Strings.isNullOrEmpty (currentStageCellText)) {
            return null;
        }
        return MiraStage.valueOf (currentStageCellText);
    }

    protected MiraStatus getCurrentStatus () {
        String currentStatusCell = "//table[contains(@class,'history')]/tbody/tr[1]/td[2]";
        String currentStatusCellText = getText (currentStatusCell);
        if (Strings.isNullOrEmpty (currentStatusCellText)) {
            return null;
        }
        return MiraStatus.valueOf (currentStatusCellText);
    }

    private boolean isMiraDetailsPage () {
        return waitUntilVisible (".mira-header");
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

    private String getStageText () {
        String stage = "span[data-ng-bind='ctrl.mira.stageName']";
        return getText (stage);
    }

    protected class WaitForPopupFunction implements Function <WebDriver, Boolean> {

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
