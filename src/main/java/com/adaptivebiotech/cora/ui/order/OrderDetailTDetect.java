package com.adaptivebiotech.cora.ui.order;

import static org.junit.Assert.fail;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import java.util.List;
import org.openqa.selenium.StaleElementReferenceException;
import com.adaptivebiotech.cora.utils.PageHelper.Ethnicity;
import com.adaptivebiotech.cora.utils.PageHelper.Race;
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
        String xpath = "//button[text()='Edit Patient Demographics']";
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
        String text = getTopmostICDMenuItem ();
        Timeout timer = new Timeout (millisRetry, waitRetry);
        while (!timer.Timedout () && ! (text.contains (code))) {
            timer.Wait ();
            text = getTopmostICDMenuItem ();
        }

        assertTrue (click (topmostListItem));

        verifyICDCodeAdded (code);
    }

    public List <String> getPatientICDCodes () {
        String xpath = "//label[text()='ICD Codes']/../div";
        Timeout timer = new Timeout (millisRetry, waitRetry);

        while (!timer.Timedout ()) {
            List <String> rv = null;
            try {
                rv = getTextList (xpath);
                return rv;
            } catch (Exception e) {
                // sometimes there is a stale reference here b/c the page is somehow not finished
                // loading
                // so try again
                e.printStackTrace ();
                timer.Wait ();
            }
        }

        fail ("can't get Patient ICD Codes");
        return null;
    }

    public String getCollectionDate () {
        String css = "[formcontrolname=\"collectionDate\"]";
        return readInput (css);
    }
    
    public Race getPatientRace () {
        String xpath = "//label[text()='Race']/../div[1]";
        String raceText = getText (xpath);
        return Race.getRace (raceText);
    }

    public Ethnicity getPatientEthnicity () {
        String xpath = "//label[text()='Ethnicity']/../div[1]";
        String ethnicityText = getText (xpath);
        return Ethnicity.getEthnicity (ethnicityText);
    }
    
    public String getToastText () {
        String css = ".toast-message";
        return getText (css);
    }
    
    private String getTopmostICDMenuItem () {
        String topmostListItemCode = "//label[text()='ICD Codes']/../ul/li[2]/a/span[1]";

        try {
            String text = getText (topmostListItemCode);
            return text;
        } catch (StaleElementReferenceException sere) {
            doWait (10000);
            String text = getText (topmostListItemCode);
            return text;
        }
    }

    private void verifyICDCodeAdded (String code) {
        Timeout timer = new Timeout (millisRetry, waitRetry);

        while (!timer.Timedout ()) {
            List <String> icdCodes = getPatientICDCodes ();

            for (String icdText : icdCodes) {
                if (icdText.contains (code)) { // code is just the code # not the full text
                    return;
                }
            }
            timer.Wait ();
        }

        fail ("can't find icd code " + code);
    }

}
