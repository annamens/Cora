package com.adaptivebiotech.cora.ui.order;

import static com.adaptivebiotech.test.utils.PageHelper.OrderStatus.Pending;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import java.util.List;
import org.openqa.selenium.StaleElementReferenceException;
import com.adaptivebiotech.cora.dto.Containers;
import com.adaptivebiotech.cora.dto.Containers.Container;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.utils.PageHelper.Ethnicity;
import com.adaptivebiotech.cora.utils.PageHelper.Race;
import com.adaptivebiotech.test.utils.PageHelper.Assay;
import com.adaptivebiotech.test.utils.PageHelper.OrderStatus;
import com.seleniumfy.test.utils.Timeout;

public class OrderDetailTDetect extends Diagnostic {

    private final String dateSignedPending     = "[formcontrolname='dateSigned']";
    private final String dateSignedActive      = "[ng-bind='ctrl.originalDate']";
    private final String orderNotesPending     = "#order-notes";
    private final String orderNotesActive      = "[ng-bind='ctrl.originalNotes']";
    private final String collectionDatePending = "[formcontrolname='collectionDate']";
    private final String collectionDateActive  = "[ng-bind^='ctrl.orderEntry.specimen.collectionDate']";

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
    public void enterDateSigned (String date) {
        assertTrue (setText (dateSignedPending, date));
    }

    @Override
    public String getDateSigned (OrderStatus state) {
        String css = Pending.equals (state) ? dateSignedPending : dateSignedActive;
        return isElementPresent (css) && isElementVisible (css) ? (Pending.equals (state) ? readInput (css) : getText (css)) : null;
    }

    @Override
    public void enterOrderNotes (String notes) {
        assertTrue (setText (orderNotesPending, notes));
    }

    @Override
    public String getOrderNotes (OrderStatus state) {
        String css = Pending.equals (state) ? orderNotesPending : orderNotesActive;
        return isElementPresent (css) && isElementVisible (css) ? (Pending.equals (state) ? readInput (css) : getText (css)) : null;
    }

    @Override
    public void addPatientICDCode (String code) {
        String addButton = "//button[text()='Add Code']";
        String icdInput = "//label[text()='ICD Codes']/../input";
        String topmostListItem = "//label[text()='ICD Codes']/../ul/li[2]/a";
        String topmostListItemCode = "//label[text()='ICD Codes']/../ul/li[2]/a/span[1]";

        assertTrue (click (addButton));
        assertTrue (setText (icdInput, code));
        pageLoading ();
        waitForElementVisible (topmostListItemCode);
        waitForAjaxCalls (); // wait for the menu to finish shuffling
        assertTrue (isTextInElement (topmostListItemCode, code));
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
            } catch (StaleElementReferenceException e) {
                timer.Wait ();
            }
        }

        fail ("can't get Patient ICD Codes");
        return null;
    }

    @Override
    public void createNewPatient (Patient patient) {
        clickPickPatient ();
        assertTrue (click ("#new-patient"));
        assertTrue (waitForElementInvisible (".ab-panel.matches"));
        assertTrue (isTextInElement (popupTitle, "Create New Patient"));
        assertTrue (setText ("#firstName", patient.firstName));
        assertTrue (setText ("#middleName", patient.middleName));
        assertTrue (setText ("#lastName", patient.lastName));
        assertTrue (setText ("#dateOfBirth", patient.dateOfBirth));
        assertTrue (clickAndSelectText ("#gender", patient.gender));
        if (patient.race != null) {
            assertTrue (clickAndSelectText ("#race", patient.race.text));
        }
        if (patient.ethnicity != null) {
            assertTrue (clickAndSelectText ("#ethnicity", patient.ethnicity.text));
        }
        assertTrue (click ("//button[text()='Save']"));
        assertTrue (setText ("[formcontrolname='mrn']", patient.mrn));
    }

    @Override
    public void clickAssayTest (Assay assay) {
        waitForElements (".test-type-selection .panel-label").forEach (el -> {
            if (el.getText ().equals (assay.test)) {
                click (el, "input");
            }
        });
    }

    @Override
    public String getCollectionDt (OrderStatus state) {
        String css = Pending.equals (state) ? collectionDatePending : collectionDateActive;
        return isElementPresent (css) && isElementVisible (css) ? (Pending.equals (state) ? readInput (css) : getText (css)) : null;
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

    private void verifyICDCodeAdded (String code) {
        String xpath = "//label[text()='ICD Codes']/..";
        assertTrue (isTextInElement (xpath, code));
    }

    @Override
    public String getPatientGender (OrderStatus state) {
        String css = Pending.equals (state) ? "//label[text()='Gender']/../div[1]" : "[ng-bind='ctrl.orderEntry.order.patient.gender']";
        return getText (css);
    }

    public String getOrderCode () {
        String xpath = "input[formcontrolname='externalOrderCode']";
        return readInput (xpath);
    }

    @Override
    public String getOrderName (OrderStatus state) {
        String css = Pending.equals (state) ? "//labeled-value[@label='Order Name']/div/div[2]/span" : "[ng-bind='ctrl.orderEntry.order.name']";;
        return getText (css);
    }

    @Override
    public String getOrderNum () {
        String xpath = "//label[@id='order-number-text']/../div[1]/span";
        return getText (xpath);
    }

    public String getBillingAddressLine1 () {
        String xpath = "input[formcontrolname='address1']";
        return readInput (xpath);
    }

    public String getBillingAddressLine2 () {
        String xpath = "input[formcontrolname='address2']";
        return readInput (xpath);
    }

    public String getBillingAddressCity () {
        String xpath = "input[formcontrolname='locality']";
        return readInput (xpath);
    }

    public String getBillingAddressState () {
        String xpath = "select[formcontrolname='region']";
        return getFirstSelectedText (xpath);
    }

    public String getBillingZipcode () {
        String xpath = "input[formcontrolname='postCode']";
        return readInput (xpath);
    }

    public String getBillingType () {
        String css = "#billing-type";
        return getFirstSelectedText (css);
    }

    public String getPhlebotomySelection () {
        String xpath = "//h3[text()='Phlebotomy Selection']/../div";
        return getText (xpath);
    }

    public String getBillingPhone () {
        String xpath = "input[formcontrolname='phone']";
        return readInput (xpath);
    }

    public String getBillingEmail () {
        String xpath = "input[formcontrolname='email']";
        return readInput (xpath);
    }

    @Override
    public String getPatientName (OrderStatus state) {
        String locator = Pending.equals (state) ? "//label[text()='Patient']/../div[1]" : "[ng-bind$='patientFullName']";
        return getText (locator);
    }

    @Override
    public String getPatientDOB (OrderStatus state) {
        String locator = Pending.equals (state) ? "//label[text()='Birth Date']/../div[1]" : "[ng-bind^='ctrl.orderEntry.order.patient.dateOfBirth']";
        return getText (locator);
    }
    
    public void clickShipment () {
        assertTrue (click ("//*[text()='Shipment']"));
    }

    @Override
    public void clickShowContainers () {
        assertTrue (click ("//*[@class='row']//*[contains(text(),'Containers')]"));
    }

    @Override
    public Containers getContainers () {
        String rows = "//specimen-containers//*[@class='row']/..";
        return new Containers (waitForElements (rows).stream ().map (row -> {
            Container c = new Container ();
            c.id = getConId (getAttribute (row, "//*[text()='Adaptive Container ID']/..//a", "href"));
            c.containerNumber = getText (row, "//*[text()='Adaptive Container ID']/..//a");
            c.location = getText (row, "//*[text()='Current Storage Location']/..//div");

            if (isElementPresent (row, ".container-table")) {
                String css = "tbody tr";
                List <Container> children = row.findElements (locateBy (css)).stream ().map (childRow -> {
                    Container childContainer = new Container ();
                    childContainer.id = getConId (getAttribute (childRow,
                                                                "td:nth-child(1) a",
                                                                "href"));
                    childContainer.containerNumber = getText (childRow,
                                                              "td:nth-child(1) a");
                    childContainer.name = getText (childRow, "td:nth-child(2)");
                    childContainer.integrity = getText (childRow, "td:nth-child(3)");
                    childContainer.root = c;
                    return childContainer;
                }).collect (toList ());
                c.children = children;
            }
            return c;
        }).collect (toList ()));
    }

}
