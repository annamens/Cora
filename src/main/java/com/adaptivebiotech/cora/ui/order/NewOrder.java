package com.adaptivebiotech.cora.ui.order;

import static com.adaptivebiotech.cora.dto.Orders.OrderStatus.Active;
import static java.lang.ClassLoader.getSystemResource;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.EnumSet.allOf;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.StaleElementReferenceException;
import com.adaptivebiotech.cora.dto.Orders.Assay;
import com.adaptivebiotech.cora.dto.Orders.OrderTest;
import com.adaptivebiotech.cora.dto.Physician;
import com.adaptivebiotech.cora.dto.Specimen.Anticoagulant;
import com.adaptivebiotech.test.utils.Logging;
import com.adaptivebiotech.test.utils.PageHelper.SpecimenSource;
import com.adaptivebiotech.test.utils.PageHelper.SpecimenType;
import com.seleniumfy.test.utils.Timeout;

/**
 * @author jpatel
 *
 */
public abstract class NewOrder extends OrderHeader {

    private final String   patientMrdStatus = ".patient-status";
    protected final String specimenNumber   = "//*[text()='Adaptive Specimen ID']/..//div";

    public NewOrder () {
        staticNavBarHeight = 200;
    }

    @Override
    public void isCorrectPage () {
        assertTrue (isTextInElement ("[role='tablist'] .active a", "ORDER DETAILS"));
        pageLoading ();
    }

    public List <String> getSectionHeaders () {
        return getTextList (".order-entry h2");
    }

    public String getPatientMRDStatus () {
        return getText (patientMrdStatus);
    }

    public List <String> getPatientICDCodes () {
        String xpath = "//label[text()='ICD Codes']/../div";
        Timeout timer = new Timeout (millisDuration, millisPoll);
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

    public void clickPatientOrderHistory () {
        assertTrue (click ("//a[text()='Patient Order History']"));
        pageLoading ();
        assertEquals (waitForElementVisible ("[uisref=\"main.patient.orders\"]").getText (), "PATIENT ORDER HISTORY");
    }

    public String getPatientId () {
        String patientUrl = getAttribute ("//a[contains(text(),'Edit Patient')]", "href");
        return patientUrl.substring (patientUrl.lastIndexOf ("/") + 1);
    }

    public void clickSave () {
        assertTrue (click ("#order-entry-save"));
        pageLoading ();
        waitForPageLoading ();
    }

    public void waitForPageLoading () {
        final String pageLoadingBar = "div.loading";
        waitUntilVisible (pageLoadingBar, 10, 100);
        assertTrue (waitForElementInvisible (pageLoadingBar));
    }

    public void waitUntilActivated () {
        Timeout timer = new Timeout (millisDuration * 10, millisPoll * 2);
        while (!timer.Timedout () && ! (getOrderStatus ().equals (Active))) {
            refresh ();
            timer.Wait ();
        }
        assertEquals (getOrderStatus (), Active, "Order did not activated successfully");
    }

    public void clickSaveAndActivate () {
        String css = "#order-entry-save-and-activate";
        assertTrue (click (css));
    }

    public void clickCancel () {
        assertTrue (click ("[ng-click='ctrl.cancel();']"));
        moduleLoading ();
    }

    public void clickCancelOrder () {
        assertTrue (click ("//button[contains(text(),'Cancel Order')]"));
        assertTrue (isTextInElement (popupTitle, "Cancel Order"));
        assertTrue (clickAndSelectText ("#cancellationReason", "Other - Internal"));
        assertTrue (clickAndSelectText ("#cancellationReason2", "Specimen - Not Rejected"));
        assertTrue (clickAndSelectText ("#cancellationReason3", "Other"));
        assertTrue (setText ("#cancellationNotes", "this is a test"));
        assertTrue (click ("//button[contains(text(),'Yes. Cancel Order')]"));
        pageLoading ();
        moduleLoading ();
        assertTrue (isTextInElement ("[ng-bind='ctrl.orderEntry.order.status']", "Cancelled"));
    }

    public void clickSeeOriginal () {
        assertTrue (click ("[ng-if='ctrl.orderEntry.order.parentReference.sourceOrderId'] .data-value-link"));
        pageLoading ();
    }

    protected String getOrderType () {
        String css = "[ng-bind='ctrl.orderEntry.order.category.name']";
        return isElementPresent (css) ? getText (css) : null;
    }

    public String getOrderNumber () {
        String css = oEntry + " .ab-panel-first [ng-bind='ctrl.orderEntry.order.orderNumber']";
        return getText (css);
    }

    public void enterDateSigned (String date) {
        assertTrue (setText ("[ng-model='ctrl.orderEntry.order.dateSigned']", date));
    }

    public String getDateSigned () {
        String css = "[ng-model^='ctrl.orderEntry.order.dateSigned']";
        return isElementVisible (css) ? readInput (css) : null;
    }

    public void enterInstruction (String instruction) {
        assertTrue (setText ("[ng-model='ctrl.orderEntry.order.specialInstructions']", instruction));
    }

    protected String getInstructions () {
        String css = "[ng-model='ctrl.orderEntry.order.specialInstructions']";
        return isElementVisible (css) ? readInput (css) : null;
    }

    public void clickPickPhysician () {
        assertTrue (click ("//button[text()='Pick Physician...']"));
        assertTrue (isTextInElement (popupTitle, "Pick Physician"));
    }

    public void enterPhysicianFirstname (String firstName) {
        assertTrue (setText ("#provider-firstname", firstName));
    }

    public void enterPhysicianLastname (String lastName) {
        assertTrue (setText ("[name='lastName']", lastName));
    }

    public void enterAccount (String accountName) {
        assertTrue (setText ("[name='accountName']", accountName));
    }

    public void clickSearch () {
        assertTrue (click ("#provider-search"));
    }

    public List <Physician> getPhysicianResults () {
        List <Physician> physicians = new ArrayList <> ();
        getTextList (".ab-panel.matches .row").forEach (l -> {
            String[] tmp = l.split ("\n");
            Physician p = new Physician ();
            p.firstName = tmp[0];
            p.lastName = tmp[1];
            p.accountName = tmp[2];
            physicians.add (p);
        });
        return physicians;
    }

    public void selectPhysician (int idx) {
        assertTrue (click (format (".ab-panel.matches .row:nth-child(%s) > div", idx)));
    }

    public void clickSelectPhysician () {
        assertTrue (click ("#select-provider"));
        assertTrue (waitForElementInvisible (".ab-panel.matches"));
    }

    public void selectPhysician (Physician physician) {
        clickPickPhysician ();
        enterPhysicianFirstname (physician.firstName);
        enterPhysicianLastname (physician.lastName);
        enterAccount (physician.accountName);
        clickSearch ();
        selectPhysician (1);
        clickSelectPhysician ();
    }

    public void clickPickPatient () {
        assertTrue (click ("//button[text()='Pick Patient...']"));
        assertTrue (isTextInElement (popupTitle, "Pick Patient"));
    }

    public void removePatient () {
        clickRemovePatient ();
        assertTrue (isTextInElement (popupTitle, "Order Billing Warning"));
        assertTrue (click ("[data-ng-click='ctrl.ok();']"));
    }

    public void removePatientTest () {
        assertTrue (isTextInElement (popupTitle, "Test Selection Warning"));
        clickPopupOK ();
    }

    public void clickRemovePatient () {
        assertTrue (click ("[ng-click='ctrl.removePatient()']"));
    }

    public void clickPatientCode () {
        String css = "//*[text()='Patient Code']/parent::div//a";
        assertTrue (click (css));
        assertTrue (waitForChildWindows (2));
        navigateToTab (1);
    }

    public String getPatientCode () {
        String xpath = "//*[text()='Patient Code']/..//a[1]/span";
        return getText (xpath);
    }

    public abstract void setPatientMRN (String mrn);

    public String getPatientMRN () {
        String css = "[ng-model='ctrl.orderEntry.order.mrn']";
        return isElementVisible (css) ? readInput (css) : null;
    }

    public void enterPatientNotes (String notes) {
        assertTrue (setText ("[ng-model='ctrl.orderEntry.order.patient.notes']", notes));
    }

    public String getPatientNotes () {
        String css = "[ng-model='ctrl.orderEntry.order.patient.notes']";
        return isElementPresent (css) ? readInput (css) : null;
    }

    public void enterPatientICD_Codes (String... codes) {
        String dropdown = "//*[*[text()='ICD Codes']]//ul";
        String css = "//button[text()='Add Code']";
        for (String code : codes) {
            if (isElementVisible (css))
                assertTrue (click (css));

            assertTrue (setText ("//*[*[text()='ICD Codes']]//input", code));
            assertTrue (waitUntilVisible (dropdown));
            assertTrue (click ("//*[contains(text(),'" + code + "')]"));
            assertTrue (waitForElementInvisible (dropdown));
        }
    }

    public List <String> getPatientICD_Codes () {
        String searchBox = "[ng-show='ctrl.searchBoxVisible'] input";
        String css = "[ng-model*='ctrl.orderEntry.icdCodes'] span";
        return isElementVisible (searchBox) ? null : isElementPresent (css) ? getTextList (css) : null;
    }

    public String getSpecimenId () {
        return isElementVisible (specimenNumber) ? getText (specimenNumber) : null;
    }

    public SpecimenType getSpecimenType () {
        String css = "[ng-model^='ctrl.orderEntry.specimen.sampleType']";
        return isElementVisible (css) ? SpecimenType.getSpecimenType (getFirstSelectedText (css)) : null;
    }

    public SpecimenSource getSpecimenSource () {
        String css = "[ng-model^='ctrl.orderEntry.specimen.sourceType']";
        return isElementVisible (css) ? SpecimenSource.getSpecimenSource (getFirstSelectedText (css)) : null;
    }

    public Anticoagulant getAnticoagulant () {
        String css = "[ng-model^='ctrl.orderEntry.specimen | specimenAnticoagulant']";
        return isElementVisible (css) ? Anticoagulant.valueOf (getFirstSelectedText (css)) : null;
    }

    protected String getReconciliationDate () {
        String rDate = "[ng-bind*='ctrl.orderEntry.specimen.reconciliationDate']";
        return isElementVisible (rDate) ? getText (rDate) : null;
    }

    public String getShipmentArrivalDate () {
        String xpath = "//*[text()='Shipment Arrival']/..//span";
        String arrivalDate = isElementVisible (xpath) ? getText (xpath) : null;
        Logging.testLog ("Shipment Arrival Date from UI: " + arrivalDate);
        return arrivalDate;
    }

    public String getSpecimenContainerQuantity () {
        return getText ("//*[text()='Quantity']/..//div");
    }

    public List <OrderTest> getSelectedTests () {
        return allOf (Assay.class).stream ().map (a -> getTestState (a)).collect (toList ())
                                  .parallelStream ().filter (t -> t.selected).collect (toList ());
    }

    private OrderTest getTestState (Assay assay) {
        OrderTest orderTest = new OrderTest (assay);
        orderTest.selected = false;

        String labelPath = String.format ("//*[text()='%s']", assay.test);
        if (isElementPresent (labelPath))
            orderTest.selected = findElement (labelPath + "/../input").isSelected ();
        return orderTest;
    }

    public String getSampleName () {
        return getText ("[ng-bind='orderTest.sampleName']");
    }

    public void enterCollectionDate (String date) {
        assertTrue (setText ("//*[text()='Collection Date']/..//input", date));
    }

    public void uploadAttachments (String... files) {
        String attachments = asList (files).parallelStream ().map (f -> getSystemResource (f).getPath ())
                                           .collect (joining ("\n"));
        waitForElement ("input[ngf-select*='ctrl.onUpload']").sendKeys (attachments);
        pageLoading ();
    }

    public List <String> getHistory () {
        return getTextList ("//*[text()='History']/..//li");
    }

    public String getIntakeCompleteDate () {
        return getText ("//*[text()='Intake Complete']/..//div");
    }

    public String getSpecimenApprovalDate () {
        return getText ("//*[text()='Specimen Approval']/..//span[2]");
    }

    public String getSpecimenApprovalStatus () {
        return getText ("//*[text()='Specimen Approval']/..//span[1]");
    }
}
