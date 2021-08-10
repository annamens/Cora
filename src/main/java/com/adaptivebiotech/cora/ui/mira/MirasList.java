package com.adaptivebiotech.cora.ui.mira;

import static com.seleniumfy.test.utils.Logging.info;
import static org.openqa.selenium.Keys.RETURN;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import com.adaptivebiotech.cora.dto.Miras;
import com.adaptivebiotech.cora.dto.Miras.Mira;
import com.adaptivebiotech.cora.ui.CoraPage;
import com.adaptivebiotech.cora.ui.mira.MirasListHelper.Filter;
import com.adaptivebiotech.cora.utils.PageHelper.MiraCostCenter;
import com.adaptivebiotech.cora.utils.PageHelper.MiraLab;
import com.adaptivebiotech.cora.utils.PageHelper.MiraPanel;
import com.adaptivebiotech.cora.utils.PageHelper.MiraStage;
import com.adaptivebiotech.cora.utils.PageHelper.MiraStatus;
import com.adaptivebiotech.test.utils.PageHelper.OrderStatus;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class MirasList extends CoraPage {

    private Set <String>    knownPanels              = new HashSet <String> ();
    private MirasListHelper mirasListHelper          = new MirasListHelper ();
    private final String    searchTypeDropdown       = "//span[contains(@class, 'list-search-type-container')]/dropdown-filter/div[contains(@class, 'dropdown')]";
    private final String    searchTypeDropdownButton = searchTypeDropdown + "/button";
    private final String    searchField              = "input[type='search']";

    public MirasList () {
        staticNavBarHeight = 90;
        for (MiraPanel miraPanel : MiraPanel.values ()) {
            knownPanels.add (miraPanel.name ());
        }
    }

    @Override
    public void isCorrectPage () {
        assertTrue (waitUntilVisible (".active[title='MIRAs']"));
        pageLoading ();
    }

    public void searchMira (String miraId) {
        assertTrue (setText (searchField, miraId));
        assertTrue (pressKey (RETURN));
    }

    public void searchAndClickMira (String miraId) {
        searchForMira (miraId);
        clickMira (miraId);
    }

    public void searchAndClickMira (String miraId, MiraLab miraLab) {
        selectLab (miraLab);
        searchAndClickMira (miraId);
    }

    public void searchForMira (String miraId, MiraLab miraLab) {
        selectLab (miraLab);
        searchForMira (miraId);
    }

    public void searchForMira (String miraId) {
        String firstResult = "//table[contains(@class, 'mira-table')]/tbody/tr[1]/td[1]/a/span";

        assertTrue (setText (searchField, miraId));
        assertTrue (pressKey (RETURN));
        pageLoading ();

        waitForFirstMiraId (miraId, firstResult);
        assertEquals (waitAndGetText (firstResult), miraId);
    }

    public void selectLab (MiraLab miraLab) {
        mirasListHelper.selectLab (miraLab);
    }

    // VERY SLOW
    public Miras getMiras () {
        return getMirasFromMiraListPage ();
    }

    public void clickSelect () {
        String selectButton = "//button[text()='Select']";
        String selectAllCheckbox = "//table[contains(@class,'mira-table')]/thead/tr/th/input[contains(@type, 'checkbox')]";
        assertTrue (click (selectButton));
        assertTrue (waitUntilVisible (selectAllCheckbox));
    }

    public void selectMiraInList (String miraId) {
        String miraCheckBox = "//td[contains(@class, 'mira-name-description')]/a/span[text()='%s']/../../../td[1]/input[contains(@type, 'checkbox')]";
        assertTrue (click (String.format (miraCheckBox, miraId)));
    }

    public void clickMira (String miraId) {
        String miraLink = "//td[contains(@class, 'mira-name-description')]/a/span[text()='%s']";

        if (click (String.format (miraLink, miraId)) == false) {
            // sometimes get a stale element exception here, really retry the click
            assertTrue (click (String.format (miraLink, miraId)));
        }

        pageLoading ();
        assertTrue (waitUntilVisible (".mira-header"));
    }

    public String clickCreateSampleManifest () {
        String createSampleManifestButton = "//button[text()='Create Sample Manifest']";
        assertTrue (click (createSampleManifestButton));
        assertTrue (waitUntilVisible (".mira-manifest-dialog"));
        assertTrue (click ("//button[text()='Yes, Create Sample Manifest']"));
        pageLoading ();
        return "Can't verify file download on saucelabs";
    }

    public void selectAllLabs () {
        mirasListHelper.selectAllLabs ();
    }

    public void selectPanelByName (String panelName) {
        mirasListHelper.selectFilter (Filter.Panel, panelName);
    }

    public void selectCostCenter (MiraCostCenter costCenter) {
        mirasListHelper.selectFilter (Filter.CostCenter, costCenter.text);
    }

    public void selectExperimentOwner (String ownerName) {
        mirasListHelper.selectFilter (Filter.ExperimentOwner, ownerName);
    }

    public void selectStatus (String status) {
        mirasListHelper.selectFilter (Filter.Status, status);
    }

    public void selectWorkflowStage (MiraStage workflowStage) {
        mirasListHelper.selectFilter (Filter.WorkflowStage, workflowStage.name ());
    }

    public void selectStageStatus (MiraStatus stageStatus) {
        mirasListHelper.selectFilter (Filter.StageStatus, stageStatus.name ());
    }

    public void enterStageSubstatus (String stageSubStatus) {
        String stageSubstatusFilter = "input#subStatusSearch";
        assertTrue (setText (stageSubstatusFilter, stageSubStatus));
    }

    public void enterSearchBoxText (String text) {
        mirasListHelper.enterSearchBoxText (text);
    }

    public String getLabText () {
        return mirasListHelper.getFilterText (Filter.Lab);
    }

    public String getPanelText () {
        return mirasListHelper.getFilterText (Filter.Panel);
    }

    public String getCostCenterText () {
        return mirasListHelper.getFilterText (Filter.CostCenter);
    }

    public String getExperimentOwnerText () {
        return mirasListHelper.getFilterText (Filter.ExperimentOwner);
    }

    public String getStatusText () {
        return mirasListHelper.getFilterText (Filter.Status);
    }

    public String getWorkflowStageText () {
        return mirasListHelper.getFilterText (Filter.WorkflowStage);
    }

    public String getStageStatusText () {
        return mirasListHelper.getFilterText (Filter.StageStatus);
    }

    public String getStageSubstatusText () {
        return readInput ("input#subStatusSearch");
    }

    public String getSearchBoxText () {
        return mirasListHelper.getSearchBoxText ();
    }

    /**
     * after you click the filter button and the page loads, need to wait until the mira table
     * refreshes
     * so loop on checking the first mira Id until:
     * 1) it changes
     * 2) we get a stale element reference exception
     * 3) the wait times out
     * if there is no first mira id, we just wait until timeout
     */
    public void clickFilterList () {
        String filterButton = "//button[text()='Filter list']";
        assertTrue (click (filterButton));
        pageLoading ();
        String firstResult = "//table[contains(@class, 'mira-table')]/tbody/tr[1]/td[1]/a/span";

        mirasListHelper.waitForTableToRefresh (firstResult);
    }

    public int countMiras () {
        List <WebElement> miraRows = waitForElements (".mira-table > tbody > tr");
        return miraRows.size ();
    }

    public List <String> getMiraIds () {
        String miraIdField = "//td[contains(@class, 'mira-name-description')]/a/span";
        List <String> miraIds = getTextList (miraIdField);
        return miraIds;
    }

    public List <String> getMiraPanelTexts () {
        String miraPanelField = "//table[contains(@class, 'mira-table')]/tbody/tr/td[3]";
        List <String> miraPanelTexts = getTextList (miraPanelField);
        return miraPanelTexts;
    }

    public Mira getMira (String miraId) {
        String miraLink = "//span[text()='%s']/../../..";
        WebElement miraRow = waitForElement (String.format (miraLink, miraId));
        Mira mira = getMiraFromRow (miraRow);

        return mira;
    }

    public void clickMIRASpecimens () {
        String miraSpecimensButton = "//a[text()='MIRA Specimens']";
        click (miraSpecimensButton);
        pageLoading ();
    }

    public void selectSearchType (SearchType searchType) {
        String dropdownItemBase = searchTypeDropdown + "/ul[contains(@class, 'dropdown-menu')]/li/a[text()='%s']";
        String dropdownItem = String.format (dropdownItemBase, searchType.text);

        assertTrue (click (searchTypeDropdownButton));
        assertTrue (click (dropdownItem));
        assertEquals (getSelectedSearchType (), searchType);
    }

    public SearchType getSelectedSearchType () {
        String buttonText = searchTypeDropdownButton + "/span";
        return SearchType.getByText (getText (buttonText));
    }

    public void reprocessMIRAs () {
        String reprocessMIRAsButton = "//button[text()='Reprocess MIRA(s)']";
        String dialog = ".mira-reprocess-dialog";
        String yesButton = "//button[text()='Yes, Reprocess MIRA(s)']";
        String toast = ".toast-message";

        assertTrue (click (reprocessMIRAsButton));
        assertTrue (waitUntilVisible (dialog));
        assertTrue (click (yesButton));
        assertTrue (waitUntilVisible (toast));
        assertTrue (waitForElementInvisible (toast));
        pageLoading ();
    }

    public void clickAcceptMIRAQC () {
        String button = "//button[text()='Accept MIRA QC']";
        String modal = "//modal-content/div[contains(@class,'accept-mira-qc-dialog')]";
        assertTrue (click (button));
        assertTrue (waitUntilVisible (modal));
    }

    public String clickAcceptMIRAQCExpectFailure () {
        String button = "//button[text()='Accept MIRA QC']";
        String toast = "//div[contains(@class, 'toast-message')]";

        assertTrue (click (button));
        assertTrue (waitUntilVisible (toast));
        String toastText = getAttribute (toast, "aria-label");
        assertTrue (waitForElementInvisible (toast));
        return toastText;
    }

    public boolean isMIRASelected (String miraId) {
        String miraCheckBoxBase = "//td[contains(@class, 'mira-name-description')]/a/span[text()='%s']/../../../td[1]/input[contains(@type, 'checkbox')]";
        String miraCheckBox = String.format (miraCheckBoxBase, miraId);
        return Boolean.parseBoolean (getAttribute (miraCheckBox, "checked"));
    }

    public void acceptMIRAQCWithComments (String comments) {
        String commentsField = "//modal-content/div[contains(@class,'accept-mira-qc-dialog')]/div[contains(@class,'modal-body')]/div[3]/div/div/textarea";
        String saveButton = "//modal-content/div[contains(@class,'accept-mira-qc-dialog')]/div[contains(@class,'modal-footer')]/button[text()='Save']";
        String toast = "//div[contains(@class, 'toast-message')]";

        assertTrue (setText (commentsField, comments));
        assertTrue (click (saveButton));
        pageLoading ();
        assertTrue (waitUntilVisible (toast));
        assertTrue (waitForElementInvisible (toast));
    }

    public void cancelAcceptMIRAQC () {
        String button = "//modal-content/div[contains(@class,'accept-mira-qc-dialog')]/div[contains(@class,'modal-footer')]/button[text()='Cancel']";
        String modal = "//modal-content/div[contains(@class,'accept-mira-qc-dialog')]";
        assertTrue (click (button));
        assertTrue (waitForElementInvisible (modal));
    }

    public boolean isMirasListInSelectMode () {
        return !isSelectButtonVisible ();
    }

    public boolean isSelectButtonVisible () {
        String selectButton = "//button[text()='Select']";
        return isElementPresent (selectButton);
    }

    private Mira getMiraFromRow (WebElement miraRow) {
        List <WebElement> columns = miraRow.findElements (By.xpath (".//td"));
        assertEquals (columns.size (), 10);
        Mira mira = new Mira ();
        mira.id = getMiraGuid (getAttribute (columns.get (0), "a", "href"));
        mira.miraId = getText (columns.get (0));
        String panel = getText (columns.get (2));
        if (panel == null || panel.equals ("")) {
            mira.panel = null;
        } else if (knownPanels.contains (panel)) {
            mira.panel = MiraPanel.valueOf (panel);
        }
        mira.numPools = Integer.valueOf (getText (columns.get (3)));
        mira.asid = getText (columns.get (4));
        mira.lastActivity = getText (columns.get (5));
        mira.status = OrderStatus.valueOf (getText (columns.get (6)));
        mira.stages = getAttributeList (columns.get (7), ".ordertest-list-stage-back", "title");
        mira.stageStatus = getText (columns.get (8));

        return mira;
    }

    private Miras getMirasFromMiraListPage () {

        List <WebElement> miraRows = waitForElements (".mira-table > tbody > tr");
        info ("found " + miraRows.size () + " miras");
        int count = 0;

        Miras miras = new Miras (new ArrayList <Mira> (500));

        for (WebElement row : miraRows) {
            Mira m = getMiraFromRow (row);
            miras.list.add (m);
            count++;
            if (count % 100 == 0) {
                info ("found " + count + " miras");
            }
        }

        return miras;

    }

    private void waitForFirstMiraId (String miraId, String firstResult) {
        Function <WebDriver, Boolean> func = new Function <WebDriver, Boolean> () {
            public Boolean apply (WebDriver driver) {
                info ("waiting for miraId " + miraId + " in search result");
                return waitAndGetText (firstResult).equals (miraId);
            }
        };
        assertTrue (waitForBooleanCondition (240, 10, func));
    }

    private String getMiraGuid (String href) {
        return href.replaceFirst (".*mira/details/", "");
    }

    // avoid stale element reference
    private String waitAndGetText (String by) {
        try {
            return waitForElement (by).getText ();
        } catch (StaleElementReferenceException e) {
            info (e.getMessage ());
            return waitForElement (by).getText ();
        }

    }

    public static enum SearchType {
        MiraID ("MIRA ID"),
        ExperimentName ("Experiment Name"),
        SpecimenID ("Specimen ID"),
        immunoSEQOrder ("immunoSEQ order"),
        pairSEQOrder ("pairSEQ order"),
        ExpansionID ("Expansion ID"),
        ContainerID ("Container ID");

        public final String text;

        private SearchType (String s) {
            text = s;
        }

        public static SearchType getByText (String s) {
            for (SearchType searchType : SearchType.values ()) {
                if (searchType.text.equals (s)) {
                    return searchType;
                }
            }
            return null;
        }

    }
}
