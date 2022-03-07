package com.adaptivebiotech.cora.ui.mira;

import static com.seleniumfy.test.utils.Logging.info;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class MiraSpecimens extends MirasListBase {

    @Override
    public void isCorrectPage () {
        assertTrue (waitUntilVisible (".specimen-table"));
    }

    public void clickFilterList () {
        String button = "//button[text()='Filter list']";
        String firstResult = "//table[contains(@class, 'specimen-table')]/tbody/tr[1]/td[1]/a";

        assertTrue (click (button));
        pageLoading ();
        waitForTableToRefresh (firstResult);
    }

    /**
     * it takes a while for the specimen to appear on this page after the shipment is created
     * 
     * @param specimenId
     */
    public void waitUntilSpecimenIdPresent (String specimenId) {
        Function <WebDriver, Boolean> func = new Function <WebDriver, Boolean> () {
            public Boolean apply (WebDriver driver) {
                if (specimenIdIsPresent (specimenId)) {
                    return true;
                }
                info ("waiting for specimenId " + specimenId);
                clickFilterList ();
                return false;
            }
        };

        waitUntil (600000, 60000, func);
        assertTrue (specimenIdIsPresent (specimenId));
    }

    public void waitUntilSpecimenIdNotPresent (String specimenId) {
        Function <WebDriver, Boolean> func = new Function <WebDriver, Boolean> () {
            public Boolean apply (WebDriver driver) {
                if (!specimenIdIsPresent (specimenId)) {
                    return true;
                }
                info ("waiting for specimenId " + specimenId + " to not be present");
                clickFilterList ();
                return false;
            }
        };

        waitUntil (600000, 60000, func);
        assertFalse (specimenIdIsPresent (specimenId));
    }

    /**
     * no wait
     * 
     * @param specimenId
     * @return
     */
    public boolean specimenIdIsPresent (String specimenId) {
        String xpath = "//td[contains(., '%s')]";
        return isElementPresent (String.format (xpath, specimenId));
    }

    public int countMiraSpecimens () {
        String xpath = "//table[contains(@class, 'specimen-table')]/tbody/tr";
        List <WebElement> specimenRows = waitForElements (xpath);
        return specimenRows.size ();
    }

    /**
     * there is no select all button on mira specimens
     */
    public void clickSelect () {
        String select = "//button[text()='Select']";
        String selectASpecimen = "//div[text()='Select a specimen']";
        assertTrue (click (select));
        assertTrue (waitUntilVisible (selectASpecimen));
    }

    public void selectSpecimenById (String specimenId) {
        String inputBase = "//a[contains(text(),'%s')]/../../td[1]/input";
        String input = String.format (inputBase, specimenId);
        assertTrue (click (input));
    }

    public void clickCreateNewMIRA () {
        String createNewMIRA = "//button[text()='Create New MIRA']";
        assertTrue (click (createNewMIRA));
    }

    public void createNewMIRAFromSpecimenId (String specimenId) {
        clickSelect ();
        selectSpecimenById (specimenId);
        clickCreateNewMIRA ();
    }

    public List <String> getSpecimenTableHeaderText () {
        String headers = "//table[contains(@class, 'specimen-table')]/thead/tr/th";
        List <String> headerText = getTextList (headers);
        return headerText;
    }

    public void clickTableHeader (String headerText) {
        String locatorBase = "//table[contains(@class, 'specimen-table')]/thead/tr/th/div/span[text()='%s']";
        String locator = String.format (locatorBase, headerText);
        assertTrue (click (locator));
        pageLoading ();
        String firstResult = "//table[contains(@class, 'specimen-table')]/tbody/tr[1]/td[1]/a";
        waitForTableToRefresh (firstResult);
    }

    public List <String> getCellCountsText () {
        String locator = "//table[contains(@class, 'specimen-table')]/tbody/tr/td[5]";
        info ("getting cell counts");
        List <String> rv = getTextList (locator);
        info ("got cell counts");
        return rv;
    }

    public String getFirstSpecimenId () {
        String locator = "//table[contains(@class, 'specimen-table')]/tbody/tr[1]/td[1]";
        return getText (locator);
    }

    // this takes < 2 minutes locally, > 15 mins on saucelabs
    // not usable
    public List <List <String>> getSpecimenData () {
        String rowLocator = "//table[contains(@class, 'specimen-table')]/tbody/tr";
        info ("getting specimen data...");
        List <WebElement> rows = waitForElementsVisible (rowLocator);
        List <List <String>> rv = new ArrayList <> (rows.size ());

        for (WebElement row : rows) {
            List <WebElement> cols = findElements (row, "td");
            List <String> fields = new ArrayList <> ();
            for (WebElement col : cols) {
                String text = col.getText ();
                fields.add (text);
            }
            rv.add (fields);
        }

        info ("got specimen data");
        return rv;
    }

    public List <String> getSpecimenIds () {
        String specimenIdField = "//table[contains(@class, 'specimen-table')]/tbody/tr/td[1]/a";
        String noResultsMessage = "p.no-results-message";
        List <String> rv = new ArrayList <> ();
        if (waitUntilVisible (specimenIdField, 10, 100)) {
            rv = getTextList (specimenIdField);
            assertEquals (rv.size (), getSpecimenCount ());
        } else {
            assertTrue (isElementVisible (noResultsMessage));
        }
        return rv;
    }

    public int getSpecimenCount () {
        String countDiv = "table-select-header .info-msg";
        int count = Integer.parseInt ( (getText (countDiv).split (" ")[0]));
        return count;
    }

}
