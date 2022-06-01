/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.ui.order;

import static com.adaptivebiotech.cora.dto.Orders.Assay.getAssay;
import static com.adaptivebiotech.cora.dto.Orders.ChargeType.Medicare;
import static com.adaptivebiotech.test.utils.DateHelper.formatDt7;
import static java.lang.ClassLoader.getSystemResource;
import static java.lang.String.format;
import static java.time.LocalDateTime.parse;
import static java.util.EnumSet.allOf;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.BooleanUtils.toBoolean;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static org.testng.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import com.adaptivebiotech.cora.dto.Containers.ContainerType;
import com.adaptivebiotech.cora.dto.Insurance;
import com.adaptivebiotech.cora.dto.Orders.Assay;
import com.adaptivebiotech.cora.dto.Orders.ChargeType;
import com.adaptivebiotech.cora.dto.Orders.DeliveryType;
import com.adaptivebiotech.cora.dto.Orders.Order;
import com.adaptivebiotech.cora.dto.Orders.OrderAuthorization;
import com.adaptivebiotech.cora.dto.Orders.OrderProperties;
import com.adaptivebiotech.cora.dto.Orders.OrderStatus;
import com.adaptivebiotech.cora.dto.Orders.OrderTest;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.dto.Physician;
import com.adaptivebiotech.cora.dto.Specimen;
import com.adaptivebiotech.cora.dto.Specimen.Anticoagulant;
import com.adaptivebiotech.cora.dto.UploadFile;
import com.adaptivebiotech.test.utils.PageHelper.SpecimenSource;
import com.adaptivebiotech.test.utils.PageHelper.SpecimenType;

/**
 * @author jpatel
 */
public class OrderDetail extends OrderHeader {

    public BillingOrderDetail billing             = new BillingOrderDetail ();

    private final String      patientMrdStatus    = ".patient-status";
    private final String      specimenNumber      = "[ng-bind='ctrl.orderEntry.specimen.specimenNumber']";
    private final String      specimenArrivalDate = "[ng-bind^='ctrl.orderEntry.specimenDisplayArrivalDate']";
    private final String      messagesLabel       = "//h2[text()='Messages']";
    private final String      attachmentName      = "[ng-show='!ctrl.showPreview(attachment.name)'] [ng-bind='attachment.name']";
    private final String      attachmentUrl       = "a[href]";
    private final String      attachmentDate      = "[ng-bind$='localDateTime']";
    private final String      attachmentCreatedBy = "[ng-bind='attachment.createdBy']";
    private final String      orderAuth           = "//*[*[text()='Order Authorization']]/following-sibling::div";
    private final String      attachments         = "[attachments='ctrl.orderEntry.attachments']%s .attachments-table-row";

    public OrderDetail () {
        staticNavBarHeight = 200;
    }

    @Override
    public void isCorrectPage () {
        assertTrue (isTextInElement ("[role='tablist'] .active a", "ORDER DETAILS"));
        pageLoading ();
    }

    @Override
    public void gotoOrderDetailsPage (String orderId) {
        super.gotoOrderDetailsPage (orderId);
        isCorrectPage ();
    }

    public String getOrderId () {
        return substringAfterLast (getCurrentUrl (), "cora/order/details/");
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

    public String getPatientMRDStatus () {
        return getText (patientMrdStatus);
    }

    public void clickPatientOrderHistory () {
        assertTrue (click ("//a[text()='Patient Order History']"));
        pageLoading ();
    }

    public Order parseOrder () {
        Order order = new Order ();
        order.id = getOrderId ();
        order.orderEntryType = getOrderType ();
        order.name = getOrderName ();
        order.status = getOrderStatus ();
        order.orderNumber = getOrderNumber ();
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
        ChargeType chargeType = billing.getBillingType ();
        order.patient.billingType = chargeType;
        order.patient.abnStatusType = Medicare.equals (chargeType) ? billing.getAbnStatus () : null;
        order.icdcodes = getPatientICD_Codes ();
        order.properties = new OrderProperties (chargeType, getSpecimenDelivery ());
        order.specimenDto = new Specimen ();
        order.specimenDto.specimenNumber = getSpecimenId ();
        order.specimenDto.sampleType = getSpecimenType ();
        order.specimenDto.sampleSource = getSpecimenSource ();
        order.specimenDto.anticoagulant = getAnticoagulant ();
        order.specimenDto.collectionDate = getCollectionDate ();
        order.specimenDto.reconciliationDate = getReconciliationDate ();
        order.specimenDto.arrivalDate = getShipmentArrivalDate ();
        order.tests = allOf (Assay.class).stream ().map (a -> getTestState (a)).collect (toList ()).parallelStream ()
                                         .filter (t -> t.selected).collect (toList ());
        order.orderAttachments = getCoraAttachments ();
        order.shipmentAttachments = getShipmentAttachments ();
        order.trf = getDoraTrf ();
        order.doraAttachments = getDoraAttachments ();

        if (billing.isPrimaryInsurancePresent ()) {
            order.patient.insurance1 = new Insurance ();
            order.patient.insurance1.provider = billing.getInsurance1Provider ();
            order.patient.insurance1.priorAuthorizationNumber = billing.getInsurance1AuthorizationNumber ();
            order.patient.insurance1.groupNumber = billing.getInsurance1GroupNumber ();
            order.patient.insurance1.policyNumber = billing.getInsurance1Policy ();
            order.patient.insurance1.insuredRelationship = billing.getInsurance1Relationship ();
            order.patient.insurance1.policyholder = billing.getInsurance1PolicyHolder ();
            order.patient.insurance1.hospitalizationStatus = billing.getInsurance1PatientStatus ();
            order.patient.insurance1.billingInstitution = billing.getInsurance1Hospital ();
            order.patient.insurance1.dischargeDate = billing.getInsurance1DischargeDate ();
        }

        if (billing.isSecondaryInsurancePresent ()) {
            order.patient.insurance2 = new Insurance ();
            order.patient.insurance2.provider = billing.getInsurance2Provider ();
            order.patient.insurance2.priorAuthorizationNumber = billing.getInsurance2AuthorizationNumber ();
            order.patient.insurance2.groupNumber = billing.getInsurance2GroupNumber ();
            order.patient.insurance2.policyNumber = billing.getInsurance2Policy ();
            order.patient.insurance2.insuredRelationship = billing.getInsurance2Relationship ();
            order.patient.insurance2.policyholder = billing.getInsurance2PolicyHolder ();
        }

        if (billing.isTertiaryInsurancePresent ()) {
            order.patient.insurance3 = new Insurance ();
            order.patient.insurance3.provider = billing.getInsurance3Provider ();
            order.patient.insurance3.priorAuthorizationNumber = billing.getInsurance3AuthorizationNumber ();
            order.patient.insurance3.groupNumber = billing.getInsurance3GroupNumber ();
            order.patient.insurance3.policyNumber = billing.getInsurance3Policy ();
            order.patient.insurance3.insuredRelationship = billing.getInsurance3Relationship ();
            order.patient.insurance3.policyholder = billing.getInsurance3PolicyHolder ();
        }

        order.patient.address = billing.getPatientAddress1 ();
        order.patient.phone = billing.getPatientPhone ();
        order.patient.locality = billing.getPatientCity ();
        order.patient.region = billing.getPatientState ();
        order.patient.postCode = billing.getPatientZipcode ();
        order.documentedByType = getOrderAuthorization ();
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
        assertTrue (waitUntil (millisDuration, millisPoll, new Function <WebDriver, Boolean> () {
            public Boolean apply (WebDriver driver) {
                return isTextInElement (css, "Clinical");
            }
        }));
        return getText (css);
    }

    public OrderStatus getOrderStatus () {
        return OrderStatus.valueOf (getText ("[ng-bind='ctrl.orderEntry.order.status']"));
    }

    public String getOrderNumber () {
        String css = oDetail + " [ng-bind='ctrl.orderEntry.order.orderNumber']";
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
        return isElementVisible (css) ? readInput (css) : null;
    }

    private String getInstructions () {
        String css = "[ng-bind='ctrl.orderEntry.order.specialInstructions']";
        return isElementVisible (css) ? getText (css) : null;
    }

    public String getProviderName () {
        return getText ("[ng-bind$='providerFullName']");
    }

    public String getProviderAccount () {
        return getText ("[ng-bind='ctrl.orderEntry.order.authorizingProvider.account.name']");
    }

    public String getPhysicianOrderCode () {
        return getText ("(//*[*[text()='Order Code']]//div)[last()]");
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
        String css = "[ng-bind='ctrl.orderEntry.order.patient.patientCode']";
        assertTrue (click (css));
        assertTrue (waitForChildWindows (2));
        navigateToTab (1);
    }

    public String getPatientCode () {
        String xpath = "[ng-bind='ctrl.orderEntry.order.patient.patientCode']";
        return getText (xpath);
    }

    public String getPatientNotes () {
        String css = "[data-ng-bind='ctrl.orderEntry.order.patient.notes']";
        return isElementPresent (css) ? getText (css) : null;
    }

    public List <String> getPatientICD_Codes () {
        String searchBox = "[ng-show='ctrl.searchBoxVisible'] input";
        String css = "[ng-repeat*='ctrl.orderEntry.icdCodes']";
        return isElementVisible (searchBox) ? null : isElementPresent (css) ? getTextList (css) : null;
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
        return isElementVisible (xpath) ? getAttribute (xpath, attribute) : null;
    }

    public String getPatientMRN () {
        String css = "[ng-bind='ctrl.orderEntry.order.mrn']";
        return isElementVisible (css) ? getText (css) : null;
    }

    public SpecimenType getSpecimenType () {
        String css = "[ng-bind^='ctrl.orderEntry.specimen.sampleType']";
        return isElementVisible (css) ? SpecimenType.getSpecimenType (getText (css)) : null;
    }

    public SpecimenSource getSpecimenSource () {
        String css = "[ng-bind^='ctrl.orderEntry.specimen.sourceType']";
        return isElementVisible (css) ? SpecimenSource.getSpecimenSource (getText (css)) : null;
    }

    public Anticoagulant getAnticoagulant () {
        String css = "[ng-bind^='ctrl.orderEntry.specimen | specimenAnticoagulant']";
        return isElementVisible (css) ? Anticoagulant.valueOf (getText (css)) : null;
    }

    public String getCollectionDate () {
        String css = "[ng-bind^='ctrl.orderEntry.specimen.collectionDate']";
        return isElementVisible (css) ? getText (css) : null;
    }

    private String getReconciliationDate () {
        String rDate = "[ng-bind*='ctrl.orderEntry.specimen.reconciliationDate']";
        return isElementVisible (rDate) ? getText (rDate) : null;
    }

    public String getShipmentArrivalDate () {
        return isElementVisible (specimenArrivalDate) ? getText (specimenArrivalDate) : null;
    }

    public void clickShipmentArrivalDate () {
        assertTrue (click (specimenArrivalDate));
    }

    public String getIntakeCompleteDate () {
        return getText ("[ng-bind^='ctrl.orderEntry.intakeCompletedDate']");
    }

    public String getSpecimenApprovalDate () {
        return getText ("[ng-bind^='ctrl.orderEntry.specimen.approvedDate']");
    }

    public String getSpecimenApprovalStatus () {
        return getText ("[ng-bind='ctrl.orderEntry.specimen.approvalStatus']");
    }

    public ContainerType getSpecimenContainerType () {
        return ContainerType.getContainerType (getText ("[ng-bind='ctrl.orderEntry.specimenDisplayContainerType']"));
    }

    public OrderTest getTestState (Assay assay) {
        String xpath = format ("//*[@ng-bind='orderTest.test.name' and text()='%s']", assay.test);
        boolean selected = isElementPresent (xpath);
        OrderTest orderTest = new OrderTest (assay);
        orderTest.selected = selected;
        orderTest.sampleName = selected ? getText (xpath + "/..//*[@ng-bind='orderTest.sampleName']") : null;
        return orderTest;
    }

    public List <OrderTest> getOrderTests () {
        List <OrderTest> orderTests = new ArrayList <> ();
        for (WebElement element : waitForElements ("[ng-repeat='orderTest in ctrl.orderEntry.orderTests']")) {
            OrderTest orderTest = new OrderTest ();
            orderTest.assay = getAssay (getText (element, "[ng-bind='orderTest.test.name']"));
            orderTest.sampleName = getText (element, "[ng-bind='orderTest.sampleName']");
            orderTests.add (orderTest);
        }
        return orderTests;
    }

    public String getSampleName (Assay assay) {
        return getText (format ("//*[text()='%s']/parent::div//*[@ng-bind='orderTest.sampleName']", assay.test));
    }

    private List <UploadFile> getOrderAttachments (String attachmentLoc) {
        List <UploadFile> attachments = new ArrayList <> ();
        if (isElementPresent (attachmentLoc))
            for (WebElement element : waitForElements (attachmentLoc)) {
                UploadFile attachment = new UploadFile ();
                attachment.fileName = getText (element, attachmentName);
                attachment.fileUrl = getAttribute (element, attachmentUrl, "href");
                attachment.createdDateTime = parse (getText (element, attachmentDate), formatDt7);
                attachment.createdBy = getText (element, attachmentCreatedBy);
                attachments.add (attachment);
            }
        return attachments;
    }

    private List <UploadFile> getCoraAttachments () {
        return getOrderAttachments (format (attachments, "[filter='ctrl.isOrderAttachment']"));
    }

    private List <UploadFile> getShipmentAttachments () {
        return getOrderAttachments (format (attachments, "[filter-by='CORA.SHIPMENTS']"));
    }

    private UploadFile getDoraTrf () {
        UploadFile doraTrFile = new UploadFile ();
        String doraTrf = "[ng-if='ctrl.orderEntry.hasDoraTrf']";
        if (isElementPresent (doraTrf)) {
            doraTrFile.fileName = getText (String.join (" ", doraTrf, ".btn-link"));
            doraTrFile.fileUrl = getAttribute (String.join (" ", doraTrf, attachmentUrl), "href");
        }
        return doraTrFile;
    }

    private List <UploadFile> getDoraAttachments () {
        return getOrderAttachments (format (attachments, "[filter='ctrl.isDoraAttachment']"));
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
        for (String file : files) {
            waitForElement ("input[ngf-select*='ctrl.onUpload']").sendKeys (getSystemResource (file).getPath ());
            transactionInProgress ();
        }
    }

    public String getDAGText () {
        String DAG = "[ng-bind*='dataAnalysisGroup']";
        return getText (DAG);
    }

    public int getMessageTableRowCount () {
        assertTrue (click (messagesLabel));
        String messagesTableRows = "[ng-repeat*='ctrl.orderEntry.orderMessages']";
        List <WebElement> rows = waitForElementsVisible (messagesTableRows);
        return rows.size ();
    }

    public boolean isMessagesTableVisible () {
        return isElementVisible (messagesLabel);
    }

    public List <String> getHistory () {
        return getTextList ("//*[text()='History']/ancestor::div[@class='row']//li");
    }

    public OrderAuthorization getOrderAuthorization () {
        return isElementVisible (orderAuth) ? OrderAuthorization.getOrderAuthorization (getText (orderAuth)) : null;
    }
}
