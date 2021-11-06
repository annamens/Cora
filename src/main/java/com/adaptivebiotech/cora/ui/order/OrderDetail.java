package com.adaptivebiotech.cora.ui.order;

import static com.adaptivebiotech.test.utils.PageHelper.ChargeType.Medicare;
import static com.adaptivebiotech.test.utils.PageHelper.PatientStatus.getPatientStatus;
import static com.adaptivebiotech.test.utils.TestHelper.formatDt1;
import static com.adaptivebiotech.test.utils.TestHelper.formatDt2;
import static java.lang.ClassLoader.getSystemResource;
import static java.util.Arrays.asList;
import static java.util.EnumSet.allOf;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.BooleanUtils.toBoolean;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.WebElement;
import com.adaptivebiotech.cora.dto.Insurance;
import com.adaptivebiotech.cora.dto.Orders.Order;
import com.adaptivebiotech.cora.dto.Orders.OrderProperties;
import com.adaptivebiotech.cora.dto.Orders.OrderTest;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.dto.Physician;
import com.adaptivebiotech.cora.dto.Specimen;
import com.adaptivebiotech.test.utils.Logging;
import com.adaptivebiotech.test.utils.PageHelper.AbnStatus;
import com.adaptivebiotech.test.utils.PageHelper.Anticoagulant;
import com.adaptivebiotech.test.utils.PageHelper.Assay;
import com.adaptivebiotech.test.utils.PageHelper.ChargeType;
import com.adaptivebiotech.test.utils.PageHelper.ContainerType;
import com.adaptivebiotech.test.utils.PageHelper.DeliveryType;
import com.adaptivebiotech.test.utils.PageHelper.OrderStatus;
import com.adaptivebiotech.test.utils.PageHelper.PatientRelationship;
import com.adaptivebiotech.test.utils.PageHelper.PatientStatus;
import com.adaptivebiotech.test.utils.PageHelper.SpecimenSource;
import com.adaptivebiotech.test.utils.PageHelper.SpecimenType;
import com.seleniumfy.test.utils.Timeout;

public class OrderDetail extends OrderHeader {

    private final String patientMrdStatus = ".patient-status";
    private final String specimenNumber   = "//*[text()='Adaptive Specimen ID']/..//span";

    public OrderDetail () {
        staticNavBarHeight = 200;
    }

    @Override
    public void isCorrectPage () {
        assertTrue (isTextInElement ("[role='tablist'] .active a", "ORDER DETAILS"));
        pageLoading ();
    }

    public String getPatientMRDStatus () {
        return getText (patientMrdStatus);
    }

    public void clickPatientOrderHistory () {
        assertTrue (click ("//a[text()='Patient Order History']"));
        pageLoading ();
        assertEquals (waitForElementVisible ("[uisref=\"main.patient.orders\"]").getText (), "PATIENT ORDER HISTORY");
    }

    public Order parseOrder () {
        Order order = new Order ();
        order.id = getOrderId ();
        order.orderEntryType = getOrderType ();
        order.name = getOrderName ();
        order.status = getOrderStatus ();
        order.order_number = getOrderNum ();
        order.data_analysis_group = getDataAnalysisGroup ();
        order.isTrfAttached = toBoolean (isTrfAttached ());
        order.date_signed = getDateSigned ();
        order.customerInstructions = getInstructions ();
        order.physician = new Physician ();
        order.physician.providerFullName = getProviderName ();
        order.physician.accountName = getProviderAccount ();
        order.patient = new Patient ();
        order.patient.fullname = getPatientName ();
        order.patient.dateOfBirth = getPatientDOB ();
        order.patient.gender = getPatientGender ();
        order.patient.patientCode = Integer.valueOf (getPatientCode ());
        order.patient.mrn = getPatientMRN ();
        order.patient.notes = getPatientNotes ();
        ChargeType chargeType = getBillingType ();
        order.patient.billingType = chargeType;
        order.patient.abnStatusType = Medicare.equals (chargeType) ? getAbnStatus () : null;
        order.icdcodes = getPatientICD_Codes ();
        order.properties = new OrderProperties (chargeType, getSpecimenDelivery ());
        order.specimenDto = new Specimen ();
        order.specimenDto.specimenNumber = getSpecimenId ();
        order.specimenDto.sampleType = getSpecimenType ();
        order.specimenDto.sourceType = getSpecimenSource ();
        order.specimenDto.anticoagulant = getAnticoagulant ();
        order.specimenDto.collectionDate = getCollectionDt ();
        order.specimenDto.reconciliationDate = getReconciliationDt ();
        order.specimenDto.arrivalDate = getShipmentArrivalDate ();
        order.expectedTestType = getExpectedTest ();
        order.tests = allOf (Assay.class).stream ().map (a -> getTestState (a)).collect (toList ()).parallelStream ()
                                         .filter (t -> t.selected).collect (toList ());
        order.orderAttachments = getCoraAttachments ();
        order.doraAttachments = getDoraAttachments ();
        order.patient.insurance1 = new Insurance ();
        order.patient.insurance1.provider = getInsurance1Provider ();
        order.patient.insurance1.groupNumber = getInsurance1GroupNumber ();
        order.patient.insurance1.policyNumber = getInsurance1Policy ();
        order.patient.insurance1.insuredRelationship = getInsurance1Relationship ();
        order.patient.insurance1.policyholder = getInsurance1PolicyHolder ();
        order.patient.insurance1.hospitalizationStatus = getInsurance1PatientStatus ();
        order.patient.insurance1.billingInstitution = getInsurance1Hospital ();
        order.patient.insurance1.dischargeDate = getInsurance1DischargeDate ();
        order.patient.insurance2 = new Insurance ();
        order.patient.insurance2.provider = getInsurance2Provider ();
        order.patient.insurance2.groupNumber = getInsurance2GroupNumber ();
        order.patient.insurance2.policyNumber = getInsurance2Policy ();
        order.patient.insurance2.insuredRelationship = getInsurance2Relationship ();
        order.patient.insurance2.policyholder = getInsurance2PolicyHolder ();
        order.patient.address = getPatientAddress1 ();
        order.patient.phone = getPatientPhone ();
        order.patient.locality = getPatientCity ();
        order.patient.region = getPatientState ();
        order.patient.postCode = getPatientZipcode ();
        order.notes = getOrderNotes ();
        return order;
    }

    private String getOrderType () {
        String css = "[ng-bind='ctrl.orderEntry.order.category.name']";
        return isElementPresent (css) ? getText (css) : null;
    }

    public String getOrderName () {
        // sometimes it's taking a while for the order detail page to load
        String css = oDetail + " [ng-bind='ctrl.orderEntry.order.name']";
        Timeout timer = new Timeout (millisRetry, waitRetry);
        while (!timer.Timedout () && ! (isTextInElement (css, "Clinical")))
            timer.Wait ();
        pageLoading ();
        return getText (css);
    }

    public OrderStatus getOrderStatus () {
        return OrderStatus.valueOf (getText ("[ng-bind='ctrl.orderEntry.order.status']"));
    }

    public String getOrderNum () {
        String css = oDetail + " .ab-panel-first" + " [ng-bind='ctrl.orderEntry.order.orderNumber']";
        return getText (css);
    }

    public String getDataAnalysisGroup () {
        return getText ("[ng-bind='ctrl.orderEntry.order.dataAnalysisGroup']");
    }

    private String isTrfAttached () {
        return getText ("[ng-bind^='ctrl.orderEntry.order.documentedByType']");
    }

    public String getDateSigned () {
        String css = "[ng-bind^='ctrl.orderEntry.order.dateSigned']";
        return isElementPresent (css) && isElementVisible (css) ? readInput (css) : null;
    }

    private String getInstructions () {
        String css = "[ng-bind='ctrl.orderEntry.order.specialInstructions']";
        return isElementVisible (css) ? getText (css) : null;
    }

    public String getProviderName () {
        return getText ("[ng-bind$='providerFullName']");
    }

    private String getProviderAccount () {
        return getText ("[ng-bind='ctrl.orderEntry.order.authorizingProvider.account.name']");
    }

    public String getPatientName () {
        return getText ("[ng-bind$='patientFullName']");
    }

    public String getPatientDOB () {
        return getText ("[ng-bind^='ctrl.orderEntry.order.patient.dateOfBirth']");
    }

    public String getPatientGender () {
        return getText ("[ng-bind='ctrl.orderEntry.order.patient.gender']");
    }

    public void clickPatientCode () {
        String css = "//*[text()='Patient Code']/parent::div//a";
        assertTrue (click (css));
        assertTrue (waitForChildWindows (2));
        List <String> windows = new ArrayList <> (getDriver ().getWindowHandles ());
        getDriver ().switchTo ().window (windows.get (1));
    }

    public String getPatientCode () {
        String xpath = "//label[text()='Patient Code']/../div/a[1]/span";
        return getText (xpath);
    }

    public void enterPatientNotes (String notes) {
        assertTrue (setText ("[ng-model='ctrl.orderEntry.order.patient.notes']", notes));
    }

    public String getPatientNotes () {
        String css = "[notes='ctrl.orderEntry.order.patient.notes']";
        return isElementPresent (css) ? getText (css) : null;
    }

    public void editPatientNotes (String notes) {
        String css = "[notes='ctrl.orderEntry.order.patient.notes']";
        assertTrue (click (css + " [ng-click='ctrl.editNotes()']"));
        assertTrue (setText (css + " textarea", notes));
        assertTrue (click (css + " [ng-click='ctrl.save()']"));
    }

    public List <String> getPatientICD_Codes () {
        String searchBox = "[ng-show='ctrl.searchBoxVisible'] input";
        String css = "[ng-repeat*='ctrl.orderEntry.icdCodes']";
        return isElementPresent (searchBox) && isElementVisible (searchBox) ? null : isElementPresent (css) ? getTextList (css) : null;
    }

    private DeliveryType getSpecimenDelivery () {
        String css = "[ng-bind^='ctrl.orderEntry.order.specimenDeliveryType']";
        return DeliveryType.getDeliveryType (getText (css));
    }

    public String getSpecimenId () {
        return isElementVisible (specimenNumber) ? getText (specimenNumber) : null;
    }

    public String getSpecimenIdUrlAttribute (String attribute) {
        String xpath = "//*[text()='Adaptive Specimen ID']/..//a";
        return isElementPresent (xpath) && isElementVisible (xpath) ? getAttribute (xpath, attribute) : null;
    }

    private String getPatientMRN () {
        String css = "[ng-bind='ctrl.orderEntry.order.mrn']";
        return isElementVisible (css) ? getText (css) : null;
    }

    public SpecimenType getSpecimenType () {
        String css = "[ng-bind^='ctrl.orderEntry.specimen.sampleType']";
        return isElementPresent (css) && isElementVisible (css) ? SpecimenType.getSpecimenType (getText (css)) : null;
    }

    public SpecimenSource getSpecimenSource () {
        String css = "[ng-bind^='ctrl.orderEntry.specimen.sourceType']";
        return isElementPresent (css) && isElementVisible (css) ? SpecimenSource.valueOf (getText (css)) : null;
    }

    public Anticoagulant getAnticoagulant () {
        String css = "[ng-bind^='ctrl.orderEntry.specimen | specimenAnticoagulant']";
        return isElementPresent (css) && isElementVisible (css) ? Anticoagulant.valueOf (getText (css)) : null;
    }

    public String getCollectionDt () {
        String css = "[ng-bind^='ctrl.orderEntry.specimen.collectionDate']";
        return isElementPresent (css) && isElementVisible (css) ? getText (css) : null;
    }

    private String getReconciliationDt () {
        String rDate = "[ng-bind*='ctrl.orderEntry.specimen.reconciliationDate']";
        return isElementPresent (rDate) && isElementVisible (rDate) ? getText (rDate) : null;
    }

    public String getShipmentArrivalDate () {
        String xpath = "//*[text()='Shipment Arrival']/..//span";
        String arrivalDate = isElementPresent (xpath) && isElementVisible (xpath) ? getText (xpath) : null;
        Logging.testLog ("Shipment Arrival Date from UI: " + arrivalDate);
        return arrivalDate;
    }

    public void clickShipmentArrivalDate () {
        assertTrue (click ("//*[text()='Shipment Arrival']/..//span"));
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

    public ContainerType getSpecimenContainerType () {
        return ContainerType.getContainerType (getText ("//*[text()='Specimen Container Type']/..//div"));
    }

    private String getExpectedTest () {
        return isElementPresent ("[ng-if='ctrl.orderEntry.order.expectedTestType']") ? getText ("[ng-bind*='order.expectedTestType']") : null;
    }

    private OrderTest getTestState (Assay assay) {
        String xpath = "//*[@ng-bind='orderTest.test.name' and text()='" + assay.test + "']";
        boolean selected = isElementPresent (xpath);
        String sampleName = selected ? getText (xpath + "/..//*[@ng-bind='orderTest.sampleName']") : null;
        OrderTest orderTest = new OrderTest (assay, selected);
        orderTest.sampleName = sampleName;
        return orderTest;
    }

    public String getSampleName () {
        return getText ("[ng-bind='orderTest.sampleName']");
    }

    private ChargeType getBillingType () {
        String css = "[ng-model^='ctrl.orderEntry.order.billingType']";
        return ChargeType.getChargeType (getText (css));
    }

    private AbnStatus getAbnStatus () {
        String css = "[ng-model^='ctrl.orderEntry.order.abnStatusType']";
        return AbnStatus.getAbnStatus (getText (css));
    }

    public String getInsurance1Provider () {
        String css = "[ng-model*='ctrl.orderEntry.orderBilling.insurance.insuranceProvider']";
        return isElementPresent (css) ? getText (css) : null;
    }

    public String getInsurance1GroupNumber () {
        String css = "[ng-model*='ctrl.orderEntry.orderBilling.insurance.groupNumber']";
        return isElementPresent (css) ? getText (css) : null;
    }

    public String getInsurance1Policy () {
        String css = "[ng-model*='ctrl.orderEntry.orderBilling.insurance.policyNumber']";
        return isElementPresent (css) ? getText (css) : null;
    }

    public PatientRelationship getInsurance1Relationship () {
        String css = "[ng-model*='ctrl.orderEntry.orderBilling.insurance.insuredRelationship']";
        String value = isElementPresent (css) ? getText (css) : null;
        return value != null ? PatientRelationship.valueOf (value) : null;
    }

    public String getInsurance1PolicyHolder () {
        String css = "[ng-model*='ctrl.orderEntry.orderBilling.insurance.policyholder']";
        return isElementPresent (css) ? getText (css) : null;
    }

    public PatientStatus getInsurance1PatientStatus () {
        String css = "[ng-model*='ctrl.orderEntry.orderBilling.insurance.hospitalizationStatus']";
        return isElementPresent (css) ? getPatientStatus (getText (css)) : null;
    }

    public String getInsurance1Hospital () {
        String css = "[ng-model*='ctrl.orderEntry.orderBilling.insurance.institution']";
        return isElementPresent (css) ? getText (css) : null;
    }

    public String getInsurance1DischargeDate () {
        String css = "[ng-model*='ctrl.orderEntry.orderBilling.insurance.dischargeDate']";
        String dt = isElementPresent (css) ? getText (css) : null;
        return dt != null ? formatDt1.format (formatDt2.parse (dt)) : dt;
    }

    public String getInsurance2Provider () {
        String css = "[ng-model*='ctrl.orderEntry.orderBilling.secondaryInsurance.insuranceProvider']";
        return isElementPresent (css) ? getText (css) : null;
    }

    public String getInsurance2GroupNumber () {
        String css = "[ng-model*='ctrl.orderEntry.orderBilling.secondaryInsurance.groupNumber']";
        return isElementPresent (css) ? getText (css) : null;
    }

    public String getInsurance2Policy () {
        String css = "[ng-model*='ctrl.orderEntry.orderBilling.secondaryInsurance.policyNumber']";
        return isElementPresent (css) ? getText (css) : null;
    }

    public PatientRelationship getInsurance2Relationship () {
        String css = "[ng-model*='ctrl.orderEntry.orderBilling.secondaryInsurance.insuredRelationship']";
        return isElementPresent (css) ? PatientRelationship.valueOf (getText (css)) : null;
    }

    public String getInsurance2PolicyHolder () {
        String css = "[ng-model*='ctrl.orderEntry.orderBilling.secondaryInsurance.policyholder']";
        return isElementPresent (css) ? getText (css) : null;
    }

    private String getPatientAddress1 () {
        String css = "[ng-model*='ctrl.orderEntry.orderBilling.guarantor.address1']";
        return isElementPresent (css) ? getText (css) : null;
    }

    private String getPatientPhone () {
        String css = "[ng-model*='ctrl.orderEntry.orderBilling.guarantor.phone']";
        return isElementPresent (css) ? getText (css) : null;
    }

    private String getPatientCity () {
        String css = "[ng-model*='ctrl.orderEntry.orderBilling.guarantor.locality']";
        return isElementPresent (css) ? getText (css) : null;
    }

    private String getPatientState () {
        String css = "[ng-model*='ctrl.orderEntry.orderBilling.guarantor.region']";
        return isElementPresent (css) ? getText (css) : null;
    }

    private String getPatientZipcode () {
        String css = "[ng-model*='ctrl.orderEntry.orderBilling.guarantor.postCode']";
        return isElementPresent (css) ? getText (css) : null;
    }

    public List <String> getCoraAttachments () {
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

    public String getOrderNotes () {
        return getText ("[notes='ctrl.orderEntry.order.notes']");
    }

    public void editOrderNotes (String notes) {
        assertTrue (click ("[notes='ctrl.orderEntry.order.notes'] [ng-click='ctrl.editNotes()'] span"));
        assertTrue (setText ("[notes='ctrl.orderEntry.order.notes'] textarea", notes));
        assertTrue (click ("[notes='ctrl.orderEntry.order.notes'] [ng-click='ctrl.save()']"));
    }

    public void uploadAttachments (String... files) {
        String attachments = asList (files).parallelStream ()
                                           .map (f -> getSystemResource (f).getPath ())
                                           .collect (joining ("\n"));
        waitForElement ("input[ngf-select*='ctrl.onUpload']").sendKeys (attachments);
        pageLoading ();
    }

    public String getDAGText () {
        String DAG = "[ng-bind*='dataAnalysisGroup']";
        return getText (DAG);
    }

    public int getMessageTableRowCount () {
        String messages = "//h2[text()='Messages']";
        assertTrue (click (messages));
        String messagesTableRows = "[ng-repeat*='ctrl.orderEntry.orderMessages']";
        List <WebElement> rows = waitForElementsVisible (messagesTableRows);
        return rows.size ();
    }

    public boolean isMessagesTableVisible () {
        String messages = "//h2[text()='Messages']";
        return waitUntilVisible (messages);
    }

    public List <String> getHistory () {
        return getTextList ("//*[text()='History']/ancestor::div[@class='row']//li");
    }
}
