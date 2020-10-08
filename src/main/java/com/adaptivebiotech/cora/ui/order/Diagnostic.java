package com.adaptivebiotech.cora.ui.order;

import static com.adaptivebiotech.test.utils.PageHelper.formatDt1;
import static com.adaptivebiotech.test.utils.PageHelper.formatDt2;
import static com.adaptivebiotech.test.utils.PageHelper.ChargeType.Medicare;
import static com.adaptivebiotech.test.utils.PageHelper.OrderStatus.Pending;
import static com.adaptivebiotech.test.utils.PageHelper.PatientStatus.getPatientStatus;
import static java.lang.ClassLoader.getSystemResource;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.EnumSet.allOf;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.BooleanUtils.toBoolean;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import com.adaptivebiotech.cora.dto.Insurance;
import com.adaptivebiotech.cora.dto.Orders.Order;
import com.adaptivebiotech.cora.dto.Orders.OrderProperties;
import com.adaptivebiotech.cora.dto.Orders.OrderTest;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.dto.Patient.Address;
import com.adaptivebiotech.cora.dto.Physician;
import com.adaptivebiotech.cora.dto.Specimen;
import com.adaptivebiotech.cora.ui.CoraPage;
import com.adaptivebiotech.test.utils.PageHelper.AbnStatus;
import com.adaptivebiotech.test.utils.PageHelper.Assay;
import com.adaptivebiotech.test.utils.PageHelper.ChargeType;
import com.adaptivebiotech.test.utils.PageHelper.DeliveryType;
import com.adaptivebiotech.test.utils.PageHelper.OrderStatus;
import com.adaptivebiotech.test.utils.PageHelper.PatientRelationship;
import com.adaptivebiotech.test.utils.PageHelper.PatientStatus;
import com.adaptivebiotech.test.utils.PageHelper.QC;
import com.adaptivebiotech.test.utils.PageHelper.SpecimenType;
import com.seleniumfy.test.utils.Timeout;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class Diagnostic extends CoraPage {

    private final String   oEntry         = ".order-entry";
    private final String   oDetail        = ".detail-sections";
    private final String   assayEl        = "//span[@ng-bind='test.name' and text()='%s']";
    protected final String specimenNumber = "div[ng-bind='ctrl.orderEntry.specimen.specimenNumber']";

    public Diagnostic () {
        staticNavBarHeight = 200;
    }

    @Override
    public void isCorrectPage () {
        assertTrue (isTextInElement ("[role='tablist'] .active a", "ORDER DETAILS"));
        pageLoading ();
    }

    public void isOrderStatusPage () {
        assertTrue (isTextInElement ("[role='tablist'] .active a", "ORDER STATUS"));
        pageLoading ();
    }

    public void clickOrderDetails () {
        assertTrue (click ("//a[text()='Order Details']"));
        pageLoading ();
    }

    public void clickReportTab (Assay assay) {
        assertTrue (click ("//a[text()='Report | " + assay.test + "']"));
        assertTrue (waitForElementInvisible ("[ng-show='ctrl.isLoadingPDF']"));
        assertTrue (waitUntilVisible (".order-test-report"));
    }

    public void clickSave () {
        assertTrue (click ("[ng-click='ctrl.save()']"));
        pageLoading ();
    }

    public void waitUntilActivated () {
        String status = "[ng-bind='ctrl.orderEntry.order.status']";
        Timeout timer = new Timeout (millisRetry, waitRetry);
        while (!timer.Timedout () && ! (isTextInElement (status, "Active"))) {
            refresh ();
            timer.Wait ();
        }
    }

    public void activateOrder () {
        clickActivateOrder ();
        assertTrue (click ("[ng-click='ctrl.ok()']"));
        moduleLoading ();
        pageLoading ();
        assertTrue (isTextInElement ("[ng-bind='ctrl.orderEntry.order.status']", "PendingActivation"));
        waitUntilActivated ();
    }

    public void clickActivateOrder () {
        assertTrue (click ("[ng-click='ctrl.activateOrder()']"));
        assertTrue (isTextInElement (popupTitle, "Confirm Order"));
    }

    public void clickCancel () {
        assertTrue (click ("[ng-click='ctrl.cancel();']"));
        moduleLoading ();
    }

    public void clickCancelOrder () {
        assertTrue (click ("[ng-click='ctrl.cancelOrder()']"));
        assertTrue (isTextInElement (popupTitle, "Cancel Order"));
        assertTrue (clickAndSelectValue ("#cancellationReason", "string:Other"));
        assertTrue (clickAndSelectValue ("#cancellationReason2", "string:Specimen - Not Rejected"));
        assertTrue (clickAndSelectValue ("#cancellationReason3", "string:Other"));
        assertTrue (setText ("#cancellationNotes", "this is a test"));
        assertTrue (click ("[ng-click='ctrl.ok()']"));
        pageLoading ();
        moduleLoading ();
        assertTrue (isTextInElement ("[ng-bind='ctrl.orderEntry.order.status']", "Cancelled"));
    }

    public void clickSeeOriginal () {
        assertTrue (click ("[ng-if='ctrl.orderEntry.order.parentReference.sourceOrderId'] .data-value-link"));
        pageLoading ();
    }

    public void setQCstatus (QC qc) {
        assertTrue (clickAndSelectValue ("#qcStatus", qc.name ()));
        assertTrue (click ("[data-ng-click='ctrl.qcComplete()']"));
        pageLoading ();
    }

    public void releaseReport () {
        assertTrue (click ("[ng-click='ctrl.releaseReport()']"));
        assertTrue (isTextInElement (popupTitle, "Release Report"));
        clickPopupOK ();
    }

    public void releaseReport (Assay assay, QC qc) {
        isOrderStatusPage ();
        clickReportTab (assay);
        setQCstatus (qc);
        releaseReport ();
    }

    public String getReportReleaseDate () {
        return getText ("[ng-bind*='reportEntry.releasedDate']");
    }

    public String getReportUrl () {
        return getAttribute (".released-report-table a[download]", "href");
    }

    public void transferTrf (OrderStatus state) {
        assertTrue (click (Pending.equals (state) ? "[ng-click='ctrl.showTrfTransferModal()']" : ".transfer-trf-btn"));
        assertTrue (isTextInElement (popupTitle, "Transfer TRF to New Order"));
        assertTrue (click ("[data-ng-click='ctrl.ok()']"));
        moduleLoading ();
        pageLoading ();
    }

    public void setAlerts (int idx) {
        assertTrue (click ("#lomn-alert-checkbox"));
        assertTrue (isTextInElement (popupTitle, "Letter of Medical Necessity Alert"));
        assertTrue (isTextInElement (".lomn-alert-info", "Are you sure you want to set this alert?"));
        assertTrue (click ("#lomn-alert-recipient-" + (idx - 1)));
        assertTrue (click ("[data-ng-click='ctrl.ok()']"));
        moduleLoading ();
        pageLoading ();
    }

    public Order parseOrder (OrderStatus state) {
        Order order = new Order ();
        order.id = getOrderId ();
        order.orderEntryType = getOrderType ();
        order.name = getOrderName (state);
        order.order_number = getOrderNum (state);
        order.isTrfAttached = toBoolean (isTrfAttached ());
        order.date_signed = getDateSigned (state);
        order.customerInstructions = getInstructions (state);
        order.physician = new Physician ();
        order.physician.providerFullName = getProviderName ();
        order.physician.accountName = getProviderAccount ();
        order.patient = new Patient ();
        order.patient.fullname = getPatientName ();
        order.patient.dateOfBirth = getPatientDOB ();
        order.patient.gender = getPatientGender ();
        order.patient.patientCode = getPatientCode (state);
        order.patient.mrn = getPatientMRN (state);
        order.patient.notes = getPatientNotes (state);
        ChargeType chargeType = getBillingType (state);
        order.patient.billingType = chargeType;
        order.patient.abnStatusType = Medicare.equals (chargeType) ? getAbnStatus (state) : null;
        order.icdcodes = getPatientICD_Codes (state);
        order.properties = new OrderProperties (chargeType, getSpecimenDelivery (state));
        order.specimen = new Specimen ();
        order.specimen.specimenNumber = getSpecimenId (state);
        order.specimen.sampleType = getSpecimenType (state);
        order.specimen.collectionDate = getCollectionDt (state);
        order.specimen.reconciliationDate = getReconciliationDt ();
        order.expected_test_type = getExpectedTest ();
        order.tests = allOf (Assay.class).stream ().map (a -> getTestState (state, a)).collect (toList ())
                                         .parallelStream ().filter (t -> t.selected).collect (toList ());
        order.orderAttachments = getCoraAttachments ();
        order.doraAttachments = getDoraAttachments ();
        order.patient.insurance1 = new Insurance ();
        order.patient.insurance1.provider = getInsurance1Provider (state);
        order.patient.insurance1.groupNumber = getInsurance1GroupNumber (state);
        order.patient.insurance1.policyNumber = getInsurance1Policy (state);
        order.patient.insurance1.insuredRelationship = getInsurance1Relationship (state);
        order.patient.insurance1.policyholder = getInsurance1PolicyHolder (state);
        order.patient.insurance1.hospitalizationStatus = getInsurance1PatientStatus (state);
        order.patient.insurance1.billingInstitution = getInsurance1Hospital (state);
        order.patient.insurance1.dischargeDate = getInsurance1DischargeDate (state);
        order.patient.insurance2 = new Insurance ();
        order.patient.insurance2.provider = getInsurance2Provider (state);
        order.patient.insurance2.groupNumber = getInsurance2GroupNumber (state);
        order.patient.insurance2.policyNumber = getInsurance2Policy (state);
        order.patient.insurance2.insuredRelationship = getInsurance2Relationship (state);
        order.patient.insurance2.policyholder = getInsurance2PolicyHolder (state);

        Address address = new Address ();
        address.line1 = getPatientAddress1 (state);
        address.phone = getPatientPhone (state);
        address.city = getPatientCity (state);
        address.state = getPatientState (state);
        address.postalCode = getPatientZipcode (state);
        order.patient.address = address.nullOrElse ();

        order.notes = getOrderNotes (state);
        return order;
    }

    public String getOrderId () {
        return getCurrentUrl ().replaceFirst (".*order/entry/diagnostic/", "").replaceFirst (".*order/details/", "");
    }

    private String getOrderType () {
        return getText ("[ng-bind='ctrl.orderEntry.order.category.name']");
    }

    public String getOrderName (OrderStatus state) {
        // sometimes it's taking a while for the order detail page to load
        String css = (Pending.equals (state) ? oEntry : oDetail) + " [ng-bind='ctrl.orderEntry.order.name']";
        Timeout timer = new Timeout (millisRetry, waitRetry);
        while (!timer.Timedout () && ! (isTextInElement (css, "Clinical")))
            timer.Wait ();
        pageLoading ();
        return getText (css);
    }

    public String getOrderNum () {
        return getOrderNum (Pending);
    }

    public String getOrderNum (OrderStatus state) {
        String css = (Pending.equals (state) ? oEntry + " .ab-panel-first" : oDetail) + " [ng-bind='ctrl.orderEntry.order.orderNumber']";
        return getText (css);
    }

    private String isTrfAttached () {
        return getText ("[ng-bind^='ctrl.orderEntry.order.documentedByType']");
    }

    public void enterDateSigned (String date) {
        assertTrue (setText ("[ng-model='ctrl.orderEntry.order.dateSigned']", date));
    }

    private String getDateSigned (OrderStatus state) {
        String css = "[ng-" + (Pending.equals (state) ? "model" : "bind") + "^='ctrl.orderEntry.order.dateSigned']";
        return isElementVisible (css) ? Pending.equals (state) ? readInput (css) : getText (css) : null;
    }

    public void enterInstruction (String instruction) {
        assertTrue (setText ("[ng-model='ctrl.orderEntry.order.specialInstructions']", instruction));
    }

    private String getInstructions (OrderStatus state) {
        String css = "[ng-" + (Pending.equals (state) ? "model" : "bind") + "='ctrl.orderEntry.order.specialInstructions']";
        return isElementVisible (css) ? Pending.equals (state) ? readInput (css) : getText (css) : null;
    }

    public void clickPickPhysician () {
        assertTrue (click ("#pick-physician"));
        assertTrue (waitUntilVisible (".modal-open"));
    }

    public void enterPhysicianFirstname (String firstName) {
        assertTrue (setText ("[name='firstName']", firstName));
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

    private String getProviderAccount () {
        return getText ("[ng-bind='ctrl.orderEntry.order.authorizingProvider.account.name']");
    }

    public void selectPatient (Patient patient) {
        clickPickPatient ();
        searchPatient (patient);

        // if we don't found a match, one more try ..
        String noMatch = ".ab-panel.matches .no-items";
        if (isElementPresent (noMatch)) {
            assertTrue (click ("[ng-click='ctrl.cancel(orderEntryForm)']"));
            moduleLoading ();
            clickPickPatient ();
            searchPatient (patient);
            assertFalse (isElementPresent (noMatch), "patient lastname = " + patient.lastName);
        }

        assertTrue (click (".ab-panel.matches .row:nth-child(1) > div"));
        assertTrue (click ("[ng-click='ctrl.save(orderEntryForm)']"));
        moduleLoading ();
        assertTrue (waitUntilVisible ("[ng-click='ctrl.removePatient()']"));
        assertTrue (setText ("[name='mrn']", patient.mrn));

    }

    public void createNewPatient (Patient patient) {
        clickPickPatient ();
        assertTrue (click ("[ng-click='ctrl.createNewPatient()']"));
        assertTrue (waitForElementInvisible (".ab-panel.matches"));
        assertTrue (isTextInElement (popupTitle, "Create New Patient"));
        assertTrue (setText ("[name='firstName']", patient.firstName));
        assertTrue (setText ("[name='middleName']", patient.middleName));
        assertTrue (setText ("[name='lastName']", patient.lastName));
        assertTrue (setText ("[name='dateOfBirth']", patient.dateOfBirth));
        assertTrue (clickAndSelectValue ("[name='gender']", "string:" + patient.gender));
        assertTrue (click ("[ng-click='ctrl.save(orderEntryForm)']"));
        closeNotification ("Patient saved");
        assertTrue (setText ("[name='mrn']", patient.mrn));
    }

    public void clickPickPatient () {
        assertTrue (click ("[ng-click='ctrl.findPatient()']"));
        assertTrue (isTextInElement (popupTitle, "Pick Patient"));
    }

    private void searchPatient (Patient patient) {
        assertTrue (setText ("[name='firstName']", patient.firstName));
        assertTrue (setText ("[name='lastName']", patient.lastName));

        // less is actually more accurate ...
        // assertTrue (setText ("[name='dateOfBirth']", patient.dateofbirth));
        // assertTrue (setText ("[ng-model='ctrl.mrn']", patient.mrn));

        assertTrue (click ("[ng-click='ctrl.search()']"));
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

    private String getPatientDOB () {
        return getText ("[ng-bind^='ctrl.orderEntry.order.patient.dateOfBirth']");
    }

    private String getPatientGender () {
        return getText ("[ng-bind='ctrl.orderEntry.order.patient.gender']");
    }

    public void clickPatientCode (OrderStatus state) {
        String css = (Pending.equals (state) ? oEntry : oDetail) + " [ng-bind='ctrl.orderEntry.order.patient.patientCode']";
        assertTrue (click (css));
        assertTrue (waitForChildWindows (2));
        List <String> windows = new ArrayList <> (getDriver ().getWindowHandles ());
        getDriver ().switchTo ().window (windows.get (1));
    }

    public String getPatientCode (OrderStatus state) {
        String css = (Pending.equals (state) ? oEntry : oDetail) + " [ng-bind='ctrl.orderEntry.order.patient.patientCode']";
        return getText (css);
    }

    private String getPatientMRN (OrderStatus state) {
        String css = "[ng-" + (Pending.equals (state) ? "model" : "bind") + "='ctrl.orderEntry.order.mrn']";
        return isElementVisible (css) ? Pending.equals (state) ? readInput (css) : getText (css) : null;
    }

    public void editPatientNotes (String notes) {
        String css = "[notes='ctrl.orderEntry.order.patient.notes']";
        assertTrue (click (css + " [ng-click='ctrl.editNotes()']"));
        assertTrue (setText (css + " textarea", notes));
        assertTrue (click (css + " [ng-click='ctrl.save()']"));
    }

    public void enterPatientNotes (String notes) {
        assertTrue (setText ("[ng-model='ctrl.orderEntry.order.patient.notes']", notes));
    }

    private String getPatientNotes (OrderStatus state) {
        String css = "[" + (Pending.equals (state) ? "ng-model" : "notes") + "='ctrl.orderEntry.order.patient.notes']";
        return Pending.equals (state) ? readInput (css) : getText (css);
    }

    public void enterPatientICD_Codes (String codes) {
        String css = "[ng-click='ctrl.showSearchBox()']";
        if (isElementVisible (css))
            assertTrue (click (css));
        assertTrue (setText ("[ng-show='ctrl.searchBoxVisible'] input", codes));
        assertTrue (waitUntilVisible ("[name='icdcodes'] .matches-icdcode"));
        assertTrue (click ("//*[contains(text(),'" + codes + "')]"));
    }

    private List <String> getPatientICD_Codes (OrderStatus state) {
        String searchBox = "[ng-show='ctrl.searchBoxVisible'] input";
        String css = "[ng-" + (Pending.equals (state) ? "model" : "repeat") + "*='ctrl.orderEntry.icdCodes']";
        if (Pending.equals (state))
            css += " span";
        return isElementPresent (searchBox) && isElementVisible (searchBox) ? null : isElementPresent (css) ? getTextList (css) : null;
    }

    private DeliveryType getSpecimenDelivery (OrderStatus state) {
        String css = "[ng-" + (Pending.equals (state) ? "model" : "bind") + "^='ctrl.orderEntry.order.specimenDeliveryType']";
        return DeliveryType.getDeliveryType (Pending.equals (state) ? getFirstSelectedText (css) : getText (css));
    }

    public String getSpecimenId () {
        return getSpecimenId (Pending);
    }

    private String getSpecimenId (OrderStatus state) {
        String css = Pending.equals (state) ? specimenNumber : specimenNumber.replaceFirst ("div", "span");
        return isElementVisible (css) ? getText (css) : null;
    }

    private SpecimenType getSpecimenType (OrderStatus state) {
        String css = "[ng-" + (Pending.equals (state) ? "model" : "bind") + "^='ctrl.orderEntry.specimen.sampleType']";
        return isElementVisible (css) ? SpecimenType.getSpecimenType (Pending.equals (state) ? getFirstSelectedText (css) : getText (css)) : null;
    }

    private String getCollectionDt (OrderStatus state) {
        String css = "[ng-" + (Pending.equals (state) ? "model" : "bind") + "^='ctrl.orderEntry.specimen.collectionDate']";
        return isElementVisible (css) ? Pending.equals (state) ? readInput (css) : getText (css) : null;
    }

    private String getReconciliationDt () {
        String rDate = "[ng-bind*='ctrl.orderEntry.specimen.reconciliationDate']";
        return isElementPresent (rDate) && isElementVisible (rDate) ? getText (rDate) : null;
    }

    private String getExpectedTest () {
        return isElementPresent ("[ng-if='ctrl.orderEntry.order.expectedTestType']") ? getText ("[ng-bind*='order.expectedTestType']") : null;
    }

    private OrderTest getTestState (OrderStatus state, Assay assay) {
        boolean selected;
        if (Pending.equals (state))
            selected = waitForElement (format (assayEl, assay.test) + "/../input").isSelected ();
        else
            selected = isElementPresent ("//*[@ng-bind='orderTest.test.name' and text()='" + assay.test + "']");
        return new OrderTest (null, assay, selected);
    }

    public String getSampleName () {
        return getText ("[ng-bind='orderTest.sampleName']");
    }

    private ChargeType getBillingType (OrderStatus state) {
        String css = "[ng-" + (Pending.equals (state) ? "model" : "bind") + "^='ctrl.orderEntry.order.billingType']";
        return ChargeType.getChargeType (Pending.equals (state) ? getFirstSelectedText (css) : getText (css));
    }

    private AbnStatus getAbnStatus (OrderStatus state) {
        String css = "[ng-" + (Pending.equals (state) ? "model" : "bind") + "^='ctrl.orderEntry.order.abnStatusType']";
        return AbnStatus.getAbnStatus (Pending.equals (state) ? getFirstSelectedText (css) : getText (css));
    }

    public String getInsurance1Provider (OrderStatus state) {
        String css = "[ng-" + (Pending.equals (state) ? "model" : "bind") + "*='ctrl.orderEntry.orderBilling.insurance.insuranceProvider']";
        return isElementPresent (css) ? Pending.equals (state) ? readInput (css) : getText (css) : null;
    }

    public String getInsurance1GroupNumber (OrderStatus state) {
        String css = "[ng-" + (Pending.equals (state) ? "model" : "bind") + "*='ctrl.orderEntry.orderBilling.insurance.groupNumber']";
        return isElementPresent (css) ? Pending.equals (state) ? readInput (css) : getText (css) : null;
    }

    public String getInsurance1Policy (OrderStatus state) {
        String css = "[ng-" + (Pending.equals (state) ? "model" : "bind") + "*='ctrl.orderEntry.orderBilling.insurance.policyNumber']";
        return isElementPresent (css) ? Pending.equals (state) ? readInput (css) : getText (css) : null;
    }

    public PatientRelationship getInsurance1Relationship (OrderStatus state) {
        String css = "[ng-" + (Pending.equals (state) ? "model" : "bind") + "*='ctrl.orderEntry.orderBilling.insurance.insuredRelationship']";
        String value = isElementPresent (css) ? Pending.equals (state) ? getFirstSelectedText (css) : getText (css) : null;
        return value != null ? PatientRelationship.valueOf (value) : null;
    }

    public String getInsurance1PolicyHolder (OrderStatus state) {
        String css = "[ng-" + (Pending.equals (state) ? "model" : "bind") + "*='ctrl.orderEntry.orderBilling.insurance.policyholder']";
        return isElementPresent (css) ? Pending.equals (state) ? readInput (css) : getText (css) : null;
    }

    public PatientStatus getInsurance1PatientStatus (OrderStatus state) {
        String css = "[ng-" + (Pending.equals (state) ? "model" : "bind") + "*='ctrl.orderEntry.orderBilling.insurance.hospitalizationStatus']";
        return isElementPresent (css) ? getPatientStatus (Pending.equals (state) ? getFirstSelectedText (css) : getText (css)) : null;
    }

    public String getInsurance1Hospital (OrderStatus state) {
        String css = "[ng-" + (Pending.equals (state) ? "model" : "bind") + "*='ctrl.orderEntry.orderBilling.insurance.institution']";
        return isElementPresent (css) ? Pending.equals (state) ? readInput (css) : getText (css) : null;
    }

    public String getInsurance1DischargeDate (OrderStatus state) {
        String css = "[ng-" + (Pending.equals (state) ? "model" : "bind") + "*='ctrl.orderEntry.orderBilling.insurance.dischargeDate']";
        String dt = isElementPresent (css) ? Pending.equals (state) ? readInput (css) : getText (css) : null;
        return dt != null ? formatDt1.format (formatDt2.parse (dt)) : dt;
    }

    public String getInsurance2Provider (OrderStatus state) {
        String css = "[ng-" + (Pending.equals (state) ? "model" : "bind") + "*='ctrl.orderEntry.orderBilling.secondaryInsurance.insuranceProvider']";
        return isElementPresent (css) ? Pending.equals (state) ? readInput (css) : getText (css) : null;
    }

    public String getInsurance2GroupNumber (OrderStatus state) {
        String css = "[ng-" + (Pending.equals (state) ? "model" : "bind") + "*='ctrl.orderEntry.orderBilling.secondaryInsurance.groupNumber']";
        return isElementPresent (css) ? Pending.equals (state) ? readInput (css) : getText (css) : null;
    }

    public String getInsurance2Policy (OrderStatus state) {
        String css = "[ng-" + (Pending.equals (state) ? "model" : "bind") + "*='ctrl.orderEntry.orderBilling.secondaryInsurance.policyNumber']";
        return isElementPresent (css) ? Pending.equals (state) ? readInput (css) : getText (css) : null;
    }

    public PatientRelationship getInsurance2Relationship (OrderStatus state) {
        String css = "[ng-" + (Pending.equals (state) ? "model" : "bind") + "*='ctrl.orderEntry.orderBilling.secondaryInsurance.insuredRelationship']";
        return isElementPresent (css) ? PatientRelationship.valueOf (Pending.equals (state) ? getFirstSelectedText (css) : getText (css)) : null;
    }

    public String getInsurance2PolicyHolder (OrderStatus state) {
        String css = "[ng-" + (Pending.equals (state) ? "model" : "bind") + "*='ctrl.orderEntry.orderBilling.secondaryInsurance.policyholder']";
        return isElementPresent (css) ? Pending.equals (state) ? readInput (css) : getText (css) : null;
    }

    private String getPatientAddress1 (OrderStatus state) {
        String css = "[ng-" + (Pending.equals (state) ? "model" : "bind") + "*='ctrl.orderEntry.orderBilling.guarantor.address1']";
        return isElementPresent (css) ? Pending.equals (state) ? readInput (css) : getText (css) : null;
    }

    private String getPatientPhone (OrderStatus state) {
        String css = "[ng-" + (Pending.equals (state) ? "model" : "bind") + "*='ctrl.orderEntry.orderBilling.guarantor.phone']";
        return isElementPresent (css) ? Pending.equals (state) ? readInput (css) : getText (css) : null;
    }

    private String getPatientCity (OrderStatus state) {
        String css = "[ng-" + (Pending.equals (state) ? "model" : "bind") + "*='ctrl.orderEntry.orderBilling.guarantor.locality']";
        return isElementPresent (css) ? Pending.equals (state) ? readInput (css) : getText (css) : null;
    }

    private String getPatientState (OrderStatus state) {
        String css = "[ng-" + (Pending.equals (state) ? "model" : "bind") + "*='ctrl.orderEntry.orderBilling.guarantor.region']";
        return isElementPresent (css) ? Pending.equals (state) ? getFirstSelectedText (css) : getText (css) : null;
    }

    private String getPatientZipcode (OrderStatus state) {
        String css = "[ng-" + (Pending.equals (state) ? "model" : "bind") + "*='ctrl.orderEntry.orderBilling.guarantor.postCode']";
        return isElementPresent (css) ? Pending.equals (state) ? readInput (css) : getText (css) : null;
    }

    public void clickAssayTest (Assay assay) {
        String type = "[ng-click*='" + assay.type + "']";
        if (!waitForElement (type).isSelected ())
            assertTrue (click (type));

        if (isElementPresent (".ng-hide[ng-show='ctrl.showTestMenu']"))
            assertTrue (click (".clickable[ng-bind*='showTestMenu']"));

        assertTrue (click (format (assayEl, assay.test)));
    }

    private List <String> getCoraAttachments () {
        String files = "[attachments='ctrl.orderEntry.attachments'][filter='ctrl.isOrderAttachment']";
        return isElementPresent (files + " .attachments-table-row") ? getTextList (files + " a [ng-bind='attachment.name']") : null;
    }

    private List <String> getDoraAttachments () {
        List <String> result = new ArrayList <> ();
        String doraTrf = "[ng-if='ctrl.orderEntry.hasDoraTrf']";
        if (isElementPresent (doraTrf))
            result.add (getText (doraTrf));

        String files = "[attachments='ctrl.orderEntry.attachments'][filter='ctrl.isDoraAttachment']";
        if (isElementPresent (files + " .attachments-table-row"))
            result.addAll (getTextList (files + " a [ng-bind='attachment.name']"));
        return result;
    }

    public void uploadAttachments (String... files) {
        String attachments = asList (files).parallelStream ()
                                           .map (f -> getSystemResource (f).getPath ())
                                           .collect (joining ("\n"));
        waitForElement ("input[ngf-select*='ctrl.onUpload']").sendKeys (attachments);
        pageLoading ();
    }

    public void editOrderNotes (String notes) {
        assertTrue (click ("[notes='ctrl.orderEntry.order.notes'] [ng-click='ctrl.editNotes()'] span"));
        assertTrue (setText ("[notes='ctrl.orderEntry.order.notes'] textarea", notes));
        assertTrue (click ("[notes='ctrl.orderEntry.order.notes'] [ng-click='ctrl.save()']"));
    }

    public void enterOrderNotes (String notes) {
        assertTrue (setText ("[ng-model='ctrl.orderEntry.order.notes']", notes));
    }

    private String getOrderNotes (OrderStatus state) {
        String css = "[" + (Pending.equals (state) ? "ng-model" : "notes") + "='ctrl.orderEntry.order.notes']";
        return Pending.equals (state) ? readInput (css) : getText (css);
    }
}
