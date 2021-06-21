package com.adaptivebiotech.cora.ui.mira;

import static com.seleniumfy.test.utils.Logging.info;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.util.Strings;
import com.adaptivebiotech.cora.ui.CoraPage;
import com.adaptivebiotech.cora.utils.CoraSelect;
import com.adaptivebiotech.cora.utils.PageHelper.MiraCostCenter;
import com.adaptivebiotech.cora.utils.PageHelper.MiraExpansionMethod;
import com.adaptivebiotech.cora.utils.PageHelper.MiraInputCellType;
import com.adaptivebiotech.cora.utils.PageHelper.MiraPanel;
import com.adaptivebiotech.cora.utils.PageHelper.MiraSortType;
import com.adaptivebiotech.cora.utils.PageHelper.MiraType;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public abstract class Mira extends CoraPage {

    private final String ownerSelector = "select[name='ownerUsername']";
    private final String panelInput    = "input[ng-model='ctrl.panelSearchText']";

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

    public void selectPanel (MiraPanel panel) {
        // after selection, the getFirstSelectedOption() stays at "Select..."
        String selector = "[name='panelType']";
        CoraSelect dropdown = new CoraSelect (waitForElementClickable (selector));
        dropdown.selectByVisibleText (panel.name ());
        // wait until the panel is visible below
        waitForPanelText (panel);
        assertTrue (getPanelNamesText ().contains (panel.name ()));
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

    public void enterName (String name) {
        String nameField = "input[name='name']";
        assertTrue (setText (nameField, name));
    }

    public void setCostCenter (MiraCostCenter costCenter) {
        String costCenterField = "select[name='costCenter']";
        selectAndVerifySelection (costCenterField, costCenter.text);
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

    // TODO - split
    public MiraType getMiraType () {
        String typeField = "div[ng-bind='ctrl.mira.miraType']";
        String typeSelector = "select[name='miraType']";
        String name;
        if (waitUntilVisible (typeSelector, 1)) {
            name = waitForSelectedText (typeSelector);
        } else {
            name = getText (typeField);
        }
        if (Strings.isNullOrEmpty (name)) {
            return null;
        }
        return MiraType.valueOf (name);
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
        if (Strings.isNullOrEmpty (costCenterText)) {
            return null;
        }
        return MiraCostCenter.getMiraCostCenter (costCenterText);
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
        if (Strings.isNullOrEmpty (text)) {
            return null;
        }
        return MiraExpansionMethod.getMiraExpansionMethod (text);
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
        if (Strings.isNullOrEmpty (text)) {
            return null;
        }
        return MiraSortType.getMiraSortType (text);
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
        if (Strings.isNullOrEmpty (text)) {
            return null;
        }
        return MiraInputCellType.getMiraInputCellType (text);
    }

    public void clickRemovePanel () {
        String trashIcon = "span[data-ng-click='ctrl.removePanel($index)']";
        String panelInput = "input[ng-model='ctrl.panelSearchText']";
        assertTrue (click (trashIcon));
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

    public boolean isSpecimenDetailsTableVisible () {
        String table = "//div[@class='mira-specimen-list-details']/table";
        return waitUntilVisible (table);
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

    public String truncateOwnerName (String ownerName) {
        String truncatedOwnerName = ownerName;
        if (truncatedOwnerName.length () > 20) {
            truncatedOwnerName = truncatedOwnerName.substring (0, 20);
        }
        return truncatedOwnerName;
    }

    /**
     * go to the miras list from an edited MIRA
     */
    public void leaveEditedMIRA () {
        clickMiras ();
        clickPopupOK ();
        new MirasList ().isCorrectPage ();
    }
    
    protected void selectAndVerifySelection (String selector, String text) {
        CoraSelect dropdown = new CoraSelect (waitForElementClickable (selector));
        dropdown.selectByVisibleText (text);
        assertEquals (dropdown.getFirstSelectedOption ().getText (), text);
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

    private List <String> getPanelNamesText () {
        String panelNamesField = "[data-ng-bind='panel.name']";
        List <String> panelNamesText = getTextList (panelNamesField);
        return panelNamesText;
    }



}
