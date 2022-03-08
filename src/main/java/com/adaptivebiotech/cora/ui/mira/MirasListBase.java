package com.adaptivebiotech.cora.ui.mira;

import static com.seleniumfy.test.utils.Logging.info;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import java.util.function.Function;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import com.adaptivebiotech.cora.ui.CoraPage;
import com.adaptivebiotech.cora.utils.PageHelper.MiraLab;

public class MirasListBase extends CoraPage {

    private final String   searchTypeDropdown       = "//span[contains(@class, 'list-search-type-container')]/dropdown-filter/div[contains(@class, 'dropdown')]";
    private final String   searchTypeDropdownButton = searchTypeDropdown + "/button";
    protected final String searchBox                = "input[type='search']";

    public void selectAllLabs () {
        selectFilter (Filter.Lab, "All");
        pageLoading ();
    }

    public void selectLab (MiraLab miraLab) {
        selectFilter (Filter.Lab, miraLab.text);
        pageLoading ();
    }

    public void selectFilter (Filter filter, String selection) {
        String dropdownBase = "//dropdown-filter[@ng-reflect-label='%s']/div[@class='dropdown']/button";
        String dropdown = String.format (dropdownBase, filter.text);
        String menuBase = "//dropdown-filter[@ng-reflect-label='%s']/div[@class='dropdown open']/ul[contains(@class,'dropdown-menu')]";
        String menu = String.format (menuBase, filter.text);
        String itemToClick = menu + "/li/a[text()='" + selection + "']";
        String selectedOption = dropdown + "/span";
        assertTrue (click (dropdown));
        if (!click (itemToClick)) {
            assertTrue (click (itemToClick)); // try again
        }
        assertEquals (getText (selectedOption), selection);
    }

    public String getFilterText (Filter filter) {
        String xpathBase = "//dropdown-filter[@ng-reflect-label='%s']/div[@class='dropdown']/button/span[contains(@class, 'title')]";
        String xpath = String.format (xpathBase, filter.text);
        return getText (xpath);
    }

    public void enterSearchBoxText (String text) {
        waitForElementClickable ("input[type='search']");
        clearSearchBoxText ();
        assertTrue (setText (searchBox, text));
        assertEquals (readInput (searchBox), text);
    }

    public String getSearchBoxText () {
        return readInput (searchBox);
    }

    public void clearSearchBoxText () {
        String searchBox = "input[type='search']";
        assertTrue (clear (searchBox));
    }

    public void clickSearchButton () {
        String searchButton = "//div[contains(@class, 'input-group')]/span[1]/button";
        assertTrue (click (searchButton));
        waitForPageLoading ();
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

    protected void waitForTableToRefresh (String firstResult) {
        int durationSeconds = 10000;
        int pollingSeconds = 1000;
        HasTableRefreshedFunction func = new HasTableRefreshedFunction (firstResult);
        try {
            waitUntil (durationSeconds, pollingSeconds, func);
        } catch (TimeoutException te) {
            info ("caught timeout exception");
        }
    }

    // the pageLoading method doesn't catch the yellow bar
    protected void waitForPageLoading () {
        final String pageLoadingBar = "div.loading";

        waitUntilVisible (pageLoadingBar, 10, 100);
        assertTrue (waitForElementInvisible (pageLoadingBar));
    }

    protected class HasTableRefreshedFunction implements Function <WebDriver, Boolean> {
        private String firstEntry = "";
        private String firstResult;

        public HasTableRefreshedFunction (String firstResult) {
            this.firstResult = firstResult;
        }

        public Boolean apply (WebDriver webDriver) {
            try {
                if (waitUntilVisible (firstResult)) {
                    String firstResultText = getText (firstResult);
                    info ("first result is: " + firstResultText);

                    if (firstEntry.equals ("")) {
                        firstEntry = firstResultText;
                        info ("setting firstEntry to : " + firstEntry);
                    } else if (!firstEntry.equals (firstResultText)) { // page has reloaded
                        info (firstEntry + " does not match " + firstResultText);
                        return true;
                    }
                }
                return false;

            } catch (StaleElementReferenceException e) {
                info ("caught StaleElementReferenceException");
                return true;
            }
        }
    }

    protected static enum Filter {
        Lab ("Lab"),
        Panel ("Panel"),
        CostCenter ("Cost Center"),
        ExperimentOwner ("Experiment Owner"),
        Status ("Status"),
        WorkflowStage ("Workflow Stage"),
        StageStatus ("Stage Status");

        public final String text;

        private Filter (String text) {
            this.text = text;
        }
    }

    public static enum SearchType {
        MiraID ("MIRA ID"),
        ExperimentName ("Experiment Name"),
        SpecimenID ("Specimen ID"),
        immunoSEQOrder ("immunoSEQ order"),
        pairSEQOrder ("pairSEQ order"),
        ExpansionID ("Expansion ID"),
        ContainerID ("Container ID"),
        SpecimenName ("Specimen Name"),
        SubjectID ("Subject ID"),
        SampleType ("Sample type"),
        Vendor ("Vendor"),
        Comments ("Comments"),
        RelatedExperiments ("Related Experiments");

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
