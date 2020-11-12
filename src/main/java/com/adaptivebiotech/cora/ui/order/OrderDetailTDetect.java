package com.adaptivebiotech.cora.ui.order;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import java.util.List;
import org.openqa.selenium.WebElement;
import com.adaptivebiotech.cora.ui.CoraPage;
import com.seleniumfy.test.utils.Timeout;

public class OrderDetailTDetect extends CoraPage {


    private final String dateSignedField = "[formcontrolname=\"dateSigned\"]";
    private final String orderNotesField = "#order-notes";

    @Override
    public void isCorrectPage () {
        assertTrue (isTextInElement ("[role='tablist'] .active a", "ORDER DETAILS"));
        pageLoading ();
    }

    public String getStatusText () {
        String xpath = "//order-header//label[text()='Status']/../div/span";
        return getText (xpath);
    }

    public void clickEditPatientDemographic () {
        String expectedText = "Edit Patient Demographic"; // https://sdlc.dna.corp.adaptivebiotech.com:8443/browse/SR-5743
        List <WebElement> buttons = waitForElementsVisible ("button");
        for (WebElement button : buttons) {
            if (getText (button).equals (expectedText)) {
                assertTrue (click (button));
                pageLoading ();
                String expectedTitle = "Edit Patient Demographics";
                assertEquals (getText (".modal-title"), expectedTitle);
                return;
            }
        }

        fail ("Can't find button with text " + expectedText);
    }

    public List <String> getTextDangerText () {
        String css = ".text-danger";
        List <String> text = getTextList (css);
        return text;
    }

    public void activateOrder () {
        clickSaveAndActivate ();
        waitUntilActivated ();
    }

    public void clickSaveAndActivate () {
        String css = "#order-entry-save-and-activate";
        assertTrue (click (css));
        moduleLoading ();
        pageLoading ();
    }

    public void waitUntilActivated () {
        Timeout timer = new Timeout (millisRetry, waitRetry);
        while (!timer.Timedout () && ! (this.getStatusText ().equals ("Active"))) {
            refresh ();
            timer.Wait ();
        }
        assertEquals (this.getStatusText (), "Active");
    }

    public void enterDateSigned (String date) {
        assertTrue (setText (dateSignedField, date));
    }

    public String getDateSigned () {
        return readInput (dateSignedField);
    }

    public void enterOrderNotes (String notes) {
        assertTrue (setText (orderNotesField, notes));
    }

    public String getOrderNotes () {
        return readInput (orderNotesField);
    }

    public void addPatientICDCode (String code) {
        String addButton = "//label[text()='ICD Codes']/../button";
        String icdInput = "//label[text()='ICD Codes']/../input";
        String topmostListItem = "//label[text()='ICD Codes']/../ul/li[2]/a";
        String topmostListItemCode = "//label[text()='ICD Codes']/../ul/li[2]/a/span[1]";
        
        assertTrue (click (addButton));
        waitForElementVisible (icdInput);
        assertTrue (setText (icdInput, code));
        pageLoading ();
        String text = getText (topmostListItemCode);

        assertEquals (text, code); // verify that not only the dropdown appears but that the text in
                                   // it matches the code
        assertTrue (click (topmostListItem));
    }

    public List <String> getPatientICDCodes () {
        String xpath = "//label[text()='ICD Codes']/../div";

        return getTextList (xpath);
    }

    public void clickSave () {
        String css = "#order-entry-save";
        assertTrue (click (css));
        pageLoading ();
    }

    public String getCollectionDate () {
        String css = "[formcontrolname=\"collectionDate\"]";
        return readInput (css);
    }

    
}
