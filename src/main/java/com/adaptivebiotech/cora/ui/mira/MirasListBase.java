package com.adaptivebiotech.cora.ui.mira;

import static com.seleniumfy.test.utils.Logging.info;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import java.time.Duration;
import java.util.function.Function;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import com.adaptivebiotech.cora.ui.CoraPage;
import com.adaptivebiotech.cora.utils.PageHelper.MiraLab;

public class MirasListBase extends CoraPage {

    protected final String searchBox = "input[type='search']";

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

    protected void waitForTableToRefresh (String firstResult) {
        int durationSeconds = 10;
        int pollingSeconds = 1;
        HasTableRefreshedFunction func = new HasTableRefreshedFunction (firstResult);
        try {
            waitForBooleanCondition (durationSeconds, pollingSeconds, func);
        } catch (TimeoutException te) {
            info ("caught timeout exception");
        }
    }

    // the pageLoading method doesn't catch the yellow bar
    protected void waitForPageLoading () {
        final String pageLoadingBar = "div.loading";

        Function <WebDriver, Boolean> waitForPageloadingBar = new Function <WebDriver, Boolean> () {
            public Boolean apply (WebDriver driver) {
                return isPageLoadingBarVisible ();
            }
        };
        Wait <WebDriver> wait = new FluentWait <> (this.getDriver ()).withTimeout (Duration.ofSeconds (10))
                                                                     .pollingEvery (Duration.ofMillis (100));

        try {
            wait.until (waitForPageloadingBar);
        } catch (TimeoutException e) {
            // sometimes we miss it if it comes and goes really fast
            info (e.toString ());
        }

        assertTrue (waitForElementInvisible (pageLoadingBar));
    }

    protected boolean isPageLoadingBarVisible () {
        String pageLoadingBar = "div.loading";
        try {
            if (isElementVisible (pageLoadingBar)) {
                return true;
            }
        } catch (NoSuchElementException e) {
            // do nothing
        }
        return false;
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

}
