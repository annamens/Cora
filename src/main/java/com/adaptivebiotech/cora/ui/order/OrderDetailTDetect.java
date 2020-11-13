package com.adaptivebiotech.cora.ui.order;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import java.util.List;
import com.seleniumfy.test.utils.Timeout;

public class OrderDetailTDetect extends Diagnostic {

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
        // https://sdlc.dna.corp.adaptivebiotech.com:8443/browse/SR-5743
        String xpath = "//button[text()='Edit Patient Demographic']";
        assertTrue (click (xpath));
        String expectedTitle = "Edit Patient Demographics";
        assertTrue (isTextInElement (popupTitle, expectedTitle));
    }

    public List <String> getTextDangerText () {
        String css = ".text-danger";
        List <String> text = getTextList (css);
        return text;
    }

    @Override
    public void activateOrder () {
        clickActivateOrder ();
        waitUntilActivated ();
    }

    @Override
    public void clickActivateOrder () {
        clickSaveAndActivate ();
        moduleLoading ();
        pageLoading ();
    }

    @Override
    public void waitUntilActivated () {
        Timeout timer = new Timeout (millisRetry, waitRetry);
        while (!timer.Timedout () && ! (this.getStatusText ().equals ("Active"))) {
            refresh ();
            timer.Wait ();
        }
        assertEquals (this.getStatusText (), "Active");
    }

    @Override
    public void enterDateSigned (String date) {
        assertTrue (setText (dateSignedField, date));
    }

    public String getDateSigned () {
        return readInput (dateSignedField);
    }

    @Override
    public void enterOrderNotes (String notes) {
        assertTrue (setText (orderNotesField, notes));
    }

    public String getOrderNotes () {
        return readInput (orderNotesField);
    }

    @Override
    public void addPatientICDCode (String code) {
        // String addButton = "//label[text()='ICD Codes']/../button";
        String addButton = "//button[text()='Add Code']";
        String icdInput = "//label[text()='ICD Codes']/../input";
        String topmostListItem = "//label[text()='ICD Codes']/../ul/li[2]/a";
        String topmostListItemCode = "//label[text()='ICD Codes']/../ul/li[2]/a/span[1]";

        assertTrue (click (addButton));
        waitForElementVisible (icdInput);
        assertTrue (setText (icdInput, code));
        pageLoading ();
        waitForElementVisible (topmostListItemCode);
        waitForAjaxCalls (); // wait for the menu to finish shuffling
        String text = getText (topmostListItemCode);

        Timeout timer = new Timeout (millisRetry, waitRetry);
        while (!timer.Timedout () && ! (text.equals (code))) {
            timer.Wait ();
            text = getText (topmostListItemCode);
        }
        assertTrue (click (topmostListItem));
    }

    public List <String> getPatientICDCodes () {
        String xpath = "//label[text()='ICD Codes']/../div";

        return getTextList (xpath);
    }

    public String getCollectionDate () {
        String css = "[formcontrolname=\"collectionDate\"]";
        return readInput (css);
    }

}
