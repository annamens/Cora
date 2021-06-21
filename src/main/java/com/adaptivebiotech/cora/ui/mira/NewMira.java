package com.adaptivebiotech.cora.ui.mira;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import java.util.List;
import org.openqa.selenium.WebElement;
import org.testng.util.Strings;
import com.adaptivebiotech.cora.utils.PageHelper.MiraLab;
import com.adaptivebiotech.cora.utils.PageHelper.MiraType;

public class NewMira extends Mira {

    private final String typeSelector = "select[name='miraType']";
    
    @Override
    public void isCorrectPage () {
        assertTrue (isTextInElement (".container .mira-heading", "New MIRA"));
    }
    
    public boolean isNewMiraPage () {
        return waitUntilVisible (".container .mira-heading");
    }

    public void selectLab (MiraLab lab) {
        String labSelector = "[name='labType']";
        selectAndVerifySelection (labSelector, lab.text);
    }

    public void selectType (MiraType type) {
        selectAndVerifySelection (typeSelector, type.text);
    }
    
    public MiraType getMiraType () {
        String name = waitForSelectedText (typeSelector);
       
        if (Strings.isNullOrEmpty (name)) {
            return null;
        }
        return MiraType.valueOf (name);
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

    public void verifySpecimenDetailsVisible () {
        String specimenDetails = "//div[contains(@class, 'specimen-details-section')]";
        assertTrue (waitUntilVisible (specimenDetails));
    }

    public void verifySpecimenDetailsInvisible () {
        String specimenDetails = "//div[contains(@class, 'specimen-details-section')]";
        assertTrue (waitForElementInvisible (specimenDetails));
    }

    public void clickSave () {
        String saveButton = "button[ng-click='ctrl.save()']";
        assertTrue (click (saveButton));
        pageLoading ();
    }

}
