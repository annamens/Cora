/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.ui.order;

import static com.adaptivebiotech.cora.dto.Orders.NoChargeReason.NoReportIssued;
import static com.adaptivebiotech.cora.dto.Orders.OrderStatus.Active;
import static com.adaptivebiotech.test.utils.DateHelper.formatDt7;
import static com.seleniumfy.test.utils.Logging.info;
import static java.lang.String.format;
import static org.apache.commons.lang3.BooleanUtils.toBoolean;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import com.adaptivebiotech.cora.dto.Containers.ContainerType;
import com.adaptivebiotech.cora.dto.Orders.Assay;
import com.adaptivebiotech.cora.dto.Orders.Order;
import com.adaptivebiotech.cora.dto.Orders.OrderProperties;
import com.adaptivebiotech.cora.dto.Orders.OrderStatus;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.dto.Physician;
import com.adaptivebiotech.cora.dto.Specimen;
import com.adaptivebiotech.cora.dto.Specimen.Anticoagulant;
import com.adaptivebiotech.cora.dto.UploadFile;
import com.adaptivebiotech.cora.ui.patient.PickPatientModule;
import com.adaptivebiotech.cora.ui.shipment.Accession;
import com.adaptivebiotech.cora.ui.shipment.NewShipment;
import com.adaptivebiotech.test.utils.PageHelper.Compartment;
import com.adaptivebiotech.test.utils.PageHelper.SpecimenSource;
import com.adaptivebiotech.test.utils.PageHelper.SpecimenType;

/**
 * @author jpatel
 *
 */
public class NewOrderClonoSeq extends NewOrder {

    public BillingNewOrderClonoSeq billing             = new BillingNewOrderClonoSeq (staticNavBarHeight);
    public PickPatientModule       pickPatient         = new PickPatientModule ();
    private Accession              accession           = new Accession ();
    private final String           orderNotes          = "#order-notes";
    private final String           specimenDetails     = "#specimen-details";
    private final String           specimenType        = "#specimen-entry-specimen-type";
    private final String           specimenTypeOther   = "#specimen-entry-other-specimen-type";
    private final String           compartment         = "#specimen-entry-compartment";
    private final String           anticoagulant       = "#specimen-entry-anticoagulant-tube";
    private final String           anticoagulantOther  = "#specimen-entry-anticoagulant-tube-other";
    private final String           specimenSource      = "[formcontrolname='source']";
    private final String           specimenSourceOther = "#specimen-entry-specimen-source-other";
    private final String           retrievalDate       = "#specimen-entry-retrieval-date";

    public void activateOrder () {
        clickSaveAndActivate ();
        List <String> errors = getRequiredFieldMsgs ();
        assertEquals (errors.size (), 0, "Order No: " + getOrderNumber () + " failed to activate, Errors: " + errors);
        confirmActivate ();
        hasPageLoaded ();
        pageLoading ();
        waitUntilActivated ();
    }

    public void clickSaveAndActivate () {
        assertTrue (click ("#order-entry-save-and-activate"));
    }

    public void confirmActivate () {
        assertTrue (isTextInElement (popupTitle, "Confirm Order"));
        assertTrue (click ("//*[text()='Activate the Order']"));
        WebElement toastEle = checkOrderActivateCancelError ();
        assertTrue (toastEle.isDisplayed ());
    }

    public void clickAssayTest (Assay assay) {
        String type = format ("//*[@class='test-type-selection']//*[text()='%s']/ancestor::label//input", assay.type);
        if (!waitForElement (type).isSelected ())
            assertTrue (click (type));

        String showTestMenu = "//*[@class='test-selection']//a[text()='Show Test Menu']";
        if (isElementPresent (showTestMenu))
            assertTrue (click (showTestMenu));

        clickSave ();
        String test = format ("//*[text()='%s']/ancestor::li//input", assay.test);
        if (!waitForElement (test).isSelected ())
            assertTrue (click (test));
    }

    public void findSpecimenId (String id) {
        assertTrue (setText ("[ng-model='ctrl.specimenNumber']", id));
        assertTrue (click ("[ng-click='ctrl.reuseSpecimen(ctrl.specimenNumber)']"));
        assertTrue (isTextInElement (popupTitle, "Patient Warning"));
        assertTrue (click ("[ng-click='ctrl.ok()']"));
        moduleLoading ();
        assertTrue (isTextInElement (specimenNumber, id));
    }

    public void deselectAllTests () {
        String tCellCheckbox = "#order-test-type-t-cell";
        String bCellCheckbox = "#order-test-type-b-cell";
        String trackingCheckbox = "#order-test-type-tracking";
        String[] checkboxes = { tCellCheckbox, bCellCheckbox, trackingCheckbox };
        for (String checkbox : checkboxes)
            if (waitForElementVisible (checkbox).isSelected ())
                assertTrue (click (checkbox));

        assertTrue (isElementPresent ("//*[@class='test-selection-header']//*[text()='No tests selected']"));
    }

    public Order parseOrder () {
        Order order = new Order ();
        order.id = getOrderId ();
        order.orderEntryType = getOrderType ();
        order.name = getOrderName ();
        order.status = getOrderStatus ();
        order.orderNumber = getOrderNumber ();
        order.data_analysis_group = null;
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
        order.patient = billing.getPatientBilling (order.patient);
        order.icdcodes = getPatientICD_Codes ();
        order.properties = new OrderProperties (order.patient.billingType, getSpecimenDelivery ());
        order.specimenDto = new Specimen ();
        order.specimenDto.specimenNumber = getSpecimenId ();
        order.specimenDto.sampleType = getSpecimenType ();
        order.specimenDto.sampleSource = getSpecimenSource ();
        order.specimenDto.anticoagulant = getAnticoagulant ();
        order.specimenDto.collectionDate = getCollectionDate ();
        order.specimenDto.reconciliationDate = getReconciliationDate ();
        order.specimenDto.arrivalDate = getShipmentArrivalDate ();
        order.tests = getSelectedTests ();
        order.orderAttachments = getCoraAttachments ();
        order.shipmentAttachments = getShipmentAttachments ();
        order.trf = getDoraTrf ();
        order.doraAttachments = getDoraAttachments ();
        order.notes = getOrderNotes ();
        return order;
    }

    public String getOrderName () {
        // sometimes it's taking a while for the order detail page to load
        String css = oEntry + " [ng-bind='ctrl.orderEntry.order.name']";
        assertTrue (waitUntil (millisDuration, millisPoll, new Function <WebDriver, Boolean> () {
            public Boolean apply (WebDriver driver) {
                return isTextInElement (css, "Clinical");
            }
        }));
        return getText (css);
    }

    private String isTrfAttached () {
        return getText ("[ng-bind*='ExternalTrf']");
    }

    public String getProviderName () {
        return getText ("[ng-bind$='providerFullName']");
    }

    protected String getProviderAccount () {
        return getText ("[ng-bind='ctrl.orderEntry.order.authorizingProvider.account.name']");
    }

    public String getCollectionDate () {
        String css = "[ng-model^='ctrl.orderEntry.specimen.collectionDate']";
        return isElementPresent (css) && isElementVisible (css) ? readInput (css) : null;
    }

    public List <UploadFile> getCoraAttachments () {
        List <UploadFile> coraAttachments = new ArrayList <> ();
        String files = "[attachments='ctrl.orderEntry.attachments'][filter='ctrl.isOrderAttachment'] .attachments-table-row";
        if (isElementPresent (files))
            for (WebElement element : waitForElements (files)) {
                UploadFile attachment = new UploadFile ();
                attachment.fileName = getText (element, "a [ng-bind='attachment.name']");
                attachment.fileUrl = getAttribute (element, "a[href]", "href");
                String createdDateTime = getText (element, "[ng-bind$='localDateTime']");
                attachment.createdDateTime = LocalDateTime.parse (createdDateTime, formatDt7);
                attachment.createdBy = getText (element, "[ng-bind='attachment.createdBy']");
                coraAttachments.add (attachment);
            }
        return coraAttachments;
    }

    private List <UploadFile> getShipmentAttachments () {
        List <UploadFile> shipmentAttachments = new ArrayList <> ();
        String files = "[attachments='ctrl.orderEntry.attachments'][filter-by='CORA.SHIPMENTS'] .attachments-table-row";
        if (isElementPresent (files))
            for (WebElement element : waitForElements (files)) {
                UploadFile attachment = new UploadFile ();
                attachment.fileName = getText (element, "a [ng-bind='attachment.name']");
                attachment.fileUrl = getAttribute (element, "a[href]", "href");
                String createdDateTime = getText (element, "[ng-bind$='localDateTime']");
                attachment.createdDateTime = LocalDateTime.parse (createdDateTime, formatDt7);
                attachment.createdBy = getText (element, "[ng-bind='attachment.createdBy']");
                shipmentAttachments.add (attachment);
            }
        return shipmentAttachments;
    }

    private UploadFile getDoraTrf () {
        UploadFile doraTrFile = new UploadFile ();
        String doraTrf = "[ng-if='ctrl.orderEntry.hasDoraTrf']";
        if (isElementPresent (doraTrf)) {
            doraTrFile.fileName = getText (String.join (" ", doraTrf, ".btn-link"));
            doraTrFile.fileUrl = getAttribute (String.join (" ", doraTrf, "a[href]"), "href");
        }
        return doraTrFile;
    }

    private List <UploadFile> getDoraAttachments () {
        List <UploadFile> doraAttachments = new ArrayList <> ();
        String files = "[attachments='ctrl.orderEntry.attachments'][filter='ctrl.isDoraAttachment'] .attachments-table-row";
        if (isElementPresent (files))
            for (WebElement element : waitForElements (files)) {
                UploadFile attachment = new UploadFile ();
                attachment.fileName = getText (element, "a [ng-bind='attachment.name']");
                attachment.fileUrl = getAttribute (element, "a[href]", "href");
                String createdDateTime = getText (element, "[ng-bind$='localDateTime']");
                attachment.createdDateTime = LocalDateTime.parse (createdDateTime, formatDt7);
                attachment.createdBy = getText (element, "[ng-bind='attachment.createdBy']");
                doraAttachments.add (attachment);
            }
        return doraAttachments;
    }

    public void addPatientICDCode (String icdCode) {
        String expectedModalTitle = "Test Selection Warning";
        this.enterPatientICD_Codes (icdCode);
        String actualText = waitForElementVisible ("[ng-bind-html=\"ctrl.dialogOptions.headerText\"]").getText ();
        assertEquals (actualText, expectedModalTitle);
        assertTrue (click ("[data-ng-click='ctrl.ok();']"));
    }

    public void clickEditPatient () {
        String editPatientLink = "a[ui-sref^='main.patient.details']";
        assertTrue (click (editPatientLink));
        pageLoading ();
    }

    public void clickEnterSpecimenDetails () {
        assertTrue (click (specimenDetails));
    }

    public void enterSpecimenType (SpecimenType type) {
        assertTrue (clickAndSelectValue (specimenType, type.name ()));
    }

    public void enterSpecimenTypeOther (String type) {
        assertTrue (setText (specimenTypeOther, type));
    }

    public void enterCompartment (Compartment compartmentEnum) {
        assertTrue (clickAndSelectValue (compartment, compartmentEnum.name ()));
    }

    public void enterAntiCoagulant (Anticoagulant anticoagulantEnum) {
        assertTrue (clickAndSelectValue (anticoagulant, anticoagulantEnum.name ()));
    }

    public void enterAntiCoagulantOther (String anticoagulant) {
        assertTrue (setText (anticoagulantOther, anticoagulant));
    }

    public void enterSpecimenSource (SpecimenSource source) {
        assertTrue (clickAndSelectValue (specimenSource, source.name ()));
    }

    public void enterSpecimenSourceOther (String source) {
        assertTrue (setText (specimenSourceOther, source));
    }

    public void enterRetrievalDate (String date) {
        assertTrue (setText (retrievalDate, date));
    }

    public String getRetrievalDate () {
        return isElementVisible (retrievalDate) ? readInput (retrievalDate) : null;
    }

    public void closeTestSelectionWarningModal () {
        String expectedModalTitle = "Test Selection Warning";
        String modalHeader = "[ng-bind-html=\"ctrl.dialogOptions.headerText\"]";
        assertTrue (isTextInElement (modalHeader, expectedModalTitle));
        clickPopupOK ();
    }

    public void enterOrderNotes (String notes) {
        assertTrue (setText (orderNotes, notes));
    }

    public String getOrderNotes () {
        return readInput (orderNotes);
    }

    /**
     * Create ClonoSeq Pending Order by filling out all the required fields and passed arguments on
     * New Order ClonoSeq page, and returns order no.
     * NOTE: Keep updating this method, and try to always use these methods to create ClonoSeq Order
     * 
     * @param physician
     * @param patient
     * @param icdCodes
     * @param assayTest
     * @param specimen
     * @return
     */
    public Order createClonoSeqOrder (Physician physician,
                                      Patient patient,
                                      String[] icdCodes,
                                      Assay assayTest,
                                      Specimen specimen) {
        selectNewClonoSEQDiagnosticOrder ();
        isCorrectPage ();

        selectPhysician (physician);
        clickPickPatient ();
        boolean matchFound = pickPatient.searchOrCreatePatient (patient);
        enterPatientICD_Codes (icdCodes);

        switch (patient.billingType) {
        case CommercialInsurance:
            billing.enterInsuranceInfo (patient);
            break;
        case Medicare:
            billing.enterMedicareInfo (patient);
            break;
        case NoCharge:
            billing.selectBilling (patient.billingType);
            billing.selectReason (NoReportIssued);
            break;
        case Client:
        case PatientSelfPay:
        default:
            billing.selectBilling (patient.billingType);
            break;
        }

        // Patient Billing Address is not required regardless of billing type.
        if (!matchFound && patient.hasAddress ())
            billing.enterPatientAddress (patient);

        clickSave ();
        clickEnterSpecimenDetails ();
        enterSpecimenType (specimen.sampleType);

        if (specimen.sampleSource != null)
            enterSpecimenSource (specimen.sampleSource);
        if (specimen.anticoagulant != null)
            enterAntiCoagulant (specimen.anticoagulant);

        enterCollectionDate (specimen.collectionDate.toString ());
        clickAssayTest (assayTest);
        clickSave ();

        Order order = new Order ();
        order.orderNumber = getOrderNumber ();
        order.id = getOrderId ();
        info ("ClonoSeq Order Number: " + order.orderNumber);
        return order;
    }

    /**
     * Create ClonoSeq Pending (differs from above method as this method creates shipment as well)
     * or Active Order by passing orderStatus argument. Returns order no.
     * NOTE: Keep updating this method, and try to always use these methods to create ClonoSeq
     * Pending Or Active Order
     * 
     * @param physician
     * @param patient
     * @param icdCodes
     * @param assayTest
     * @param orderStatus
     * @param containerType
     * @return
     */
    public Order createClonoSeqOrder (Physician physician,
                                      Patient patient,
                                      String[] icdCodes,
                                      Assay assayTest,
                                      Specimen specimen,
                                      OrderStatus orderStatus,
                                      ContainerType containerType) {
        // create clonoSEQ diagnostic order
        Order order = createClonoSeqOrder (physician, patient, icdCodes, assayTest, specimen);

        // add diagnostic shipment
        new NewShipment ().createShipment (order.orderNumber, containerType);

        // accession complete
        if (orderStatus.equals (Active)) {
            accession.completeAccession ();

            // activate order
            isCorrectPage ();
            activateOrder ();

            // refreshing the page doesn't automatically take you to order detail
            gotoOrderDetailsPage (order.id);
            isCorrectPage ();
        } else {
            accession.clickOrderNumber ();
            isCorrectPage ();
        }

        return order;
    }
}
