package com.adaptivebiotech.cora.ui.order;

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
import com.adaptivebiotech.cora.dto.Orders.OrderStatus;
import com.adaptivebiotech.cora.dto.Orders.OrderTest;
import com.adaptivebiotech.cora.dto.Patient;
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

    public abstract void createNewPatient (Patient patient);

    public String getPatientMRDStatus () {
        return getText (patientMrdStatus);
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

    public String getStatusText () {
        String xpath = "//*[text()='Status']/..//span";
        return getText (xpath);
    }

    public void waitUntilActivated () {
        Timeout timer = new Timeout (millisRetry, waitRetry);
        while (!timer.Timedout () && ! (getStatusText ().equals ("Active"))) {
            refresh ();
            timer.Wait ();
        }
        assertEquals (getStatusText (), "Active", "Order did not activated successfully");
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

    public OrderStatus getOrderStatus () {
        return OrderStatus.valueOf (getText ("[ng-bind='ctrl.orderEntry.order.status']"));
    }

    public void clickSeeOriginal () {
        assertTrue (click ("[ng-if='ctrl.orderEntry.order.parentReference.sourceOrderId'] .data-value-link"));
        pageLoading ();
    }

    protected String getOrderType () {
        String css = "[ng-bind='ctrl.orderEntry.order.category.name']";
        return isElementPresent (css) ? getText (css) : null;
    }

    public String getOrderName () {
        // sometimes it's taking a while for the order detail page to load
        String css = oEntry + " [ng-bind='ctrl.orderEntry.order.name']";
        Timeout timer = new Timeout (millisRetry, waitRetry);
        while (!timer.Timedout () && ! (isTextInElement (css, "Clinical")))
            timer.Wait ();
        pageLoading ();
        return getText (css);
    }

    public String getOrderNumber () {
        String css = oEntry + " .ab-panel-first [ng-bind='ctrl.orderEntry.order.orderNumber']";
        return getText (css);
    }

    protected String isTrfAttached () {
        return getText ("[ng-bind^='ctrl.orderEntry.order.documentedByType']");
    }

    public void enterDateSigned (String date) {
        assertTrue (setText ("[ng-model='ctrl.orderEntry.order.dateSigned']", date));
    }

    public String getDateSigned () {
        String css = "[ng-model^='ctrl.orderEntry.order.dateSigned']";
        return isElementPresent (css) && isElementVisible (css) ? readInput (css) : null;
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

    public String getProviderName () {
        return getText ("[ng-bind$='providerFullName']");
    }

    protected String getProviderAccount () {
        return getText ("[ng-bind='ctrl.orderEntry.order.authorizingProvider.account.name']");
    }

    public boolean searchOrCreatePatient (Patient patient) {
        clickPickPatient ();
        searchPatient (patient);

        boolean matchFound = false;
        String firstrow = ".ab-panel.matches .row:nth-child(1)";
        if (getText (firstrow).matches ("No patient(s)? found\\."))
            createNewPatient (patient);
        else {
            assertTrue (click (firstrow));
            assertTrue (click ("#select-patient"));
            moduleLoading ();
            setPatientMRN (patient.mrn);
            matchFound = true;
        }
        return matchFound;

    }

    public void clickPickPatient () {
        assertTrue (click ("//button[text()='Pick Patient...']"));
        assertTrue (isTextInElement (popupTitle, "Pick Patient"));
    }

    public void searchPatient (Patient patient) {
        assertTrue (setText ("#patient-firstname", patient.firstName));
        assertTrue (setText ("#patient-lastname", patient.lastName));
        assertTrue (setText ("#patient-dateofbirth", patient.dateOfBirth));
        assertTrue (setText ("#patient-mrn", patient.mrn));
        assertTrue (click ("#patient-search"));
        pageLoading ();
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

    public String getPatientName () {
        return getText ("[ng-bind$='patientFullName']");
    }

    public String getPatientDOB () {
        return getText ("[ng-bind^='ctrl.orderEntry.order.patient.dateOfBirth']");
    }

    public void clickPatientCode () {
        String css = "//*[text()='Patient Code']/parent::div//a";
        assertTrue (click (css));
        assertTrue (waitForChildWindows (2));
        List <String> windows = new ArrayList <> (getDriver ().getWindowHandles ());
        getDriver ().switchTo ().window (windows.get (1));
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

    public void enterPatientICD_Codes (String codes) {
        String css = "//button[text()='Add Code']";
        if (isElementPresent (css) && isElementVisible (css))
            assertTrue (click (css));
        assertTrue (setText ("//*[text()='ICD Codes']/..//input", codes));
        assertTrue (waitUntilVisible ("//*[text()='ICD Codes']/..//ul"));
        assertTrue (click ("//*[contains(text(),'" + codes + "')]"));
        assertTrue (waitForElementInvisible ("//*[text()='ICD Codes']/..//ul"));
    }

    public List <String> getPatientICD_Codes () {
        String searchBox = "[ng-show='ctrl.searchBoxVisible'] input";
        String css = "[ng-model*='ctrl.orderEntry.icdCodes'] span";
        return isElementPresent (searchBox) && isElementVisible (searchBox) ? null : isElementPresent (css) ? getTextList (css) : null;
    }

    public String getSpecimenId () {
        return isElementVisible (specimenNumber) ? getText (specimenNumber) : null;
    }

    public SpecimenType getSpecimenType () {
        String css = "[ng-model^='ctrl.orderEntry.specimen.sampleType']";
        return isElementPresent (css) && isElementVisible (css) ? SpecimenType.getSpecimenType (getFirstSelectedText (css)) : null;
    }

    public SpecimenSource getSpecimenSource () {
        String css = "[ng-model^='ctrl.orderEntry.specimen.sourceType']";
        return isElementPresent (css) && isElementVisible (css) ? SpecimenSource.getSpecimenSource (getFirstSelectedText (css)) : null;
    }

    public Anticoagulant getAnticoagulant () {
        String css = "[ng-model^='ctrl.orderEntry.specimen | specimenAnticoagulant']";
        return isElementPresent (css) && isElementVisible (css) ? Anticoagulant.valueOf (getFirstSelectedText (css)) : null;
    }

    protected String getReconciliationDate () {
        String rDate = "[ng-bind*='ctrl.orderEntry.specimen.reconciliationDate']";
        return isElementPresent (rDate) && isElementVisible (rDate) ? getText (rDate) : null;
    }

    public String getShipmentArrivalDate () {
        String xpath = "//*[text()='Shipment Arrival']/..//span";
        String arrivalDate = isElementPresent (xpath) && isElementVisible (xpath) ? getText (xpath) : null;
        Logging.testLog ("Shipment Arrival Date from UI: " + arrivalDate);
        return arrivalDate;
    }

    public String getSpecimenContainerQuantity () {
        return getText ("//*[text()='Quantity']/..//div");
    }

    protected String getExpectedTest () {
        return isElementPresent ("[ng-if='ctrl.orderEntry.order.expectedTestType']") ? getText ("[ng-bind*='order.expectedTestType']") : null;
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

    public List <String> getCoraAttachments () {
        String files = "[attachments='ctrl.orderEntry.attachments'][filter='ctrl.isOrderAttachment']";
        return isElementPresent (files + " .attachments-table-row") ? getTextList (files + " a [ng-bind='attachment.name']") : null;
    }

    protected List <String> getDoraAttachments () {
        List <String> result = new ArrayList <> ();
        String doraTrf = "[ng-if='ctrl.orderEntry.hasDoraTrf']";
        if (isElementPresent (doraTrf))
            result.add (getText (doraTrf));

        String files = "[attachments='ctrl.orderEntry.attachments'][filter='ctrl.isDoraAttachment']";
        if (isElementPresent (files + " .attachments-table-row"))
            result.addAll (getTextList (files + " a [ng-bind='attachment.name']"));
        return result;
    }

    public String getOrderNotes () {
        return readInput ("[ng-model='ctrl.orderEntry.order.notes']");
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
