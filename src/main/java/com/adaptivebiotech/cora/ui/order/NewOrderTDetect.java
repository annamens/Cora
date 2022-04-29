/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.ui.order;

import static com.adaptivebiotech.cora.dto.Orders.NoChargeReason.CustomerService;
import static com.adaptivebiotech.cora.dto.Orders.OrderStatus.Active;
import static com.adaptivebiotech.test.utils.DateHelper.formatDt7;
import static com.seleniumfy.test.utils.Logging.info;
import static java.lang.String.format;
import static org.apache.commons.lang3.BooleanUtils.toBoolean;
import static org.testng.Assert.assertTrue;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.WebElement;
import com.adaptivebiotech.cora.dto.Containers.ContainerType;
import com.adaptivebiotech.cora.dto.Orders.Assay;
import com.adaptivebiotech.cora.dto.Orders.Order;
import com.adaptivebiotech.cora.dto.Orders.OrderProperties;
import com.adaptivebiotech.cora.dto.Orders.OrderStatus;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.dto.Physician;
import com.adaptivebiotech.cora.dto.Specimen;
import com.adaptivebiotech.cora.dto.UploadFile;
import com.adaptivebiotech.cora.ui.patient.PickPatientModule;
import com.adaptivebiotech.cora.ui.shipment.Accession;
import com.adaptivebiotech.cora.ui.shipment.NewShipment;

/**
 * @author jpatel
 *
 */
public class NewOrderTDetect extends NewOrder {

    public BillingNewOrderTDetect billing        = new BillingNewOrderTDetect (staticNavBarHeight);
    public PickPatientModule      pickPatient    = new PickPatientModule ();
    private Accession             accession      = new Accession ();
    private final String          dateSigned     = "[formcontrolname='dateSigned']";
    private final String          collectionDate = "[formcontrolname='collectionDate']";

    public String getPhysicianOrderCode () {
        String xpath = "input[formcontrolname='externalOrderCode']";
        return readInput (xpath);
    }

    public void clickAssayTest (Assay assay) {
        assertTrue (click (format ("//*[@class='test-type-selection']//*[text()='%s']", assay.test)));
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

    private String isTrfAttached () {
        return getText ("labeled-value[label='Internal TRF Attached'] span");
    }

    public String getProviderName () {
        return getText ("//*[@formcontrolname='providerForm']//label[text()='Name']/parent::div//span");
    }

    private String getProviderAccount () {
        return getText ("//*[@formcontrolname='providerForm']//label[text()='Account']/parent::div//span");
    }

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

    private void verifyICDCodeAdded (String code) {
        String xpath = "//label[text()='ICD Codes']/..";
        assertTrue (isTextInElement (xpath, code));
    }

    @Override
    public void enterDateSigned (String date) {
        assertTrue (setText (dateSigned, date));
    }

    @Override
    public String getDateSigned () {
        return isElementVisible (dateSigned) ? readInput (dateSigned) : null;
    }

    public String getCollectionDate () {
        return isElementVisible (collectionDate) ? readInput (collectionDate) : null;
    }

    private void clickAttachments () {
        assertTrue (click (".order-attachments h2"));
        pageLoading ();
    }

    private void expandAttachmentsIfNot () {
        if (getAttribute ("order-attachments i", "class").endsWith ("right")) {
            clickAttachments ();
        }
    }

    private List <UploadFile> getCoraAttachments () {
        expandAttachmentsIfNot ();
        List <UploadFile> coraAttachments = new ArrayList <> ();
        String files = "//h3[text()='Orders']/parent::div//div[contains(@class,'attachments-table-row')]";
        if (isElementPresent (files))
            for (WebElement element : waitForElements (files)) {
                UploadFile attachment = new UploadFile ();
                attachment.fileName = getText (element, "./div[1]//a");
                attachment.fileUrl = getAttribute (element, "./div[2]//a", "href");
                String createdDateTime = getText (element, "./div[3]");
                attachment.createdDateTime = LocalDateTime.parse (createdDateTime, formatDt7);
                attachment.createdBy = getText (element, "./div[4]");
                coraAttachments.add (attachment);
            }
        return coraAttachments;
    }

    private List <UploadFile> getShipmentAttachments () {
        expandAttachmentsIfNot ();
        List <UploadFile> shipmentAttachments = new ArrayList <> ();
        String files = "//h3[text()='Shipments']/parent::div//div[contains(@class,'attachments-table-row')]";
        if (isElementPresent (files))
            for (WebElement element : waitForElements (files)) {
                UploadFile attachment = new UploadFile ();
                attachment.fileName = getText (element, "./div[1]//a");
                attachment.fileUrl = getAttribute (element, "./div[2]//a", "href");
                String createdDateTime = getText (element, "./div[3]");
                attachment.createdDateTime = LocalDateTime.parse (createdDateTime, formatDt7);
                attachment.createdBy = getText (element, "./div[4]");
                shipmentAttachments.add (attachment);
            }
        return shipmentAttachments;
    }

    private void clickDoraAttachmentsExpand () {
        assertTrue (click ("//h3[contains(text(),'Dora')]//a"));
        pageLoading ();
    }

    private void expandDoraAttachmentsIfNot () {
        expandAttachmentsIfNot ();
        if (getText ("//h3[contains(text(),'Dora')]//span").contains ("Expand")) {
            clickDoraAttachmentsExpand ();
        }
    }

    private UploadFile getDoraTrf () {
        expandDoraAttachmentsIfNot ();
        UploadFile doraTrFile = new UploadFile ();
        String doraTrf = "//h3[contains(text(),'Dora')]/parent::div//a[contains(text(),'Original Dora Trf')]";
        if (isElementPresent (doraTrf)) {
            WebElement row = findElement (waitForElement (doraTrf),
                                          "./ancestor::div[contains(@class,'attachments-table-row')]");
            doraTrFile.fileName = getText (row, "./div[1]//a");
            doraTrFile.fileUrl = getAttribute (row, "./div[2]//a", "href");
        }
        return doraTrFile;
    }

    private List <UploadFile> getDoraAttachments () {
        expandDoraAttachmentsIfNot ();
        List <UploadFile> doraAttachments = new ArrayList <> ();
        String files = "//h3[contains(text(),'Dora')]/parent::div//div[contains(@class,'attachments-table-row')]";
        if (isElementPresent (files))
            for (WebElement element : waitForElements (files)) {
                String fileName = getText (element, "./div[1]//a");
                if (fileName.contains ("Original Dora Trf")) {
                    continue;
                }
                UploadFile attachment = new UploadFile ();
                attachment.fileName = fileName;
                attachment.fileUrl = getAttribute (element, "./div[2]//a", "href");
                String createdDateTime = getText (element, "./div[3]");
                attachment.createdDateTime = LocalDateTime.parse (createdDateTime, formatDt7);
                attachment.createdBy = getText (element, "./div[4]");
                doraAttachments.add (attachment);
            }
        return doraAttachments;
    }

    public String getOrderName () {
        return getText ("//labeled-value[@label='Order Name']/div/div[2]/span");
    }

    /**
     * Create TDetect Pending Order by filling out all the required fields and passed arguments on
     * New Order TDetect page, and returns order no.
     * 
     * NOTE: Keep updating this method, and try to always use these methods to create TDetect Order
     * 
     * @param physician
     * @param patient
     * @param icdCodes
     * @param collectionDate
     * @param assayTest
     * @return Cora order number
     */
    public String createTDetectOrder (Physician physician,
                                      Patient patient,
                                      String[] icdCodes,
                                      String collectionDate,
                                      Assay assayTest) {
        selectNewTDetectDiagnosticOrder ();
        isCorrectPage ();

        selectPhysician (physician);
        clickPickPatient ();
        boolean matchFound = pickPatient.searchOrCreatePatient (patient);
        if (icdCodes != null)
            enterPatientICD_Codes (icdCodes);
        enterCollectionDate (collectionDate);
        clickAssayTest (assayTest);

        switch (patient.billingType) {
        case CommercialInsurance:
            billing.enterInsuranceInfo (patient);
            break;
        case Medicare:
            billing.enterMedicareInfo (patient);
            break;
        case NoCharge:
            billing.selectBilling (patient.billingType);
            billing.selectReason (CustomerService);
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

        String orderNum = getOrderNumber ();
        info ("T-Detect Order Number: " + orderNum);
        return orderNum;
    }

    /**
     * Create TDetect Pending (differs from above method as this method creates shipment as well)
     * or Active Order by passing orderStatus argument. Returns order no.
     * 
     * NOTE: Keep updating this method, and try to always use these methods to create TDetect
     * Pending Or Active Order
     * 
     * @param physician
     * @param patient
     * @param icdCodes
     * @param collectionDate
     * @param assayTest
     * @param orderStatus
     * @param containerType
     * @return Cora order number
     */
    public String createTDetectOrder (Physician physician,
                                      Patient patient,
                                      String[] icdCodes,
                                      String collectionDate,
                                      Assay assayTest,
                                      OrderStatus orderStatus,
                                      ContainerType containerType) {
        // create T-Detect order
        String orderNum = createTDetectOrder (physician, patient, icdCodes, collectionDate, assayTest);

        // add diagnostic shipment
        new NewShipment ().createShipment (orderNum, containerType);

        // accession complete
        if (orderStatus.equals (Active)) {
            accession.completeAccession ();

            // activate order
            isCorrectPage ();
            activateOrder ();

            // for T-Detect, refreshing the page doesn't automatically take you to order detail
            gotoOrderDetailsPage (getOrderId ());
            isCorrectPage ();
        } else {
            accession.clickOrderNumber ();
            isCorrectPage ();
        }

        return orderNum;
    }
}
