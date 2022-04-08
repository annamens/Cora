package com.adaptivebiotech.cora.ui.order;

import static com.adaptivebiotech.cora.dto.Orders.ChargeType.Medicare;
import static com.adaptivebiotech.cora.dto.Orders.OrderStatus.Active;
import static com.adaptivebiotech.test.utils.TestHelper.formatDt7;
import static com.seleniumfy.test.utils.Logging.info;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.BooleanUtils.toBoolean;
import static org.testng.Assert.assertTrue;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.WebElement;
import com.adaptivebiotech.cora.dto.Containers;
import com.adaptivebiotech.cora.dto.Containers.Container;
import com.adaptivebiotech.cora.dto.Containers.ContainerType;
import com.adaptivebiotech.cora.dto.Insurance;
import com.adaptivebiotech.cora.dto.Orders.Assay;
import com.adaptivebiotech.cora.dto.Orders.ChargeType;
import com.adaptivebiotech.cora.dto.Orders.Order;
import com.adaptivebiotech.cora.dto.Orders.OrderProperties;
import com.adaptivebiotech.cora.dto.Orders.OrderStatus;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.dto.Physician;
import com.adaptivebiotech.cora.dto.Specimen;
import com.adaptivebiotech.cora.dto.UploadFile;
import com.adaptivebiotech.cora.ui.shipment.Accession;
import com.adaptivebiotech.cora.ui.shipment.NewShipment;

/**
 * @author jpatel
 *
 */
public class NewOrderTDetect extends NewOrder {

    public BillingNewOrderTDetect billing         = new BillingNewOrderTDetect (staticNavBarHeight);
    public PatientNewOrder        patientNewOrder = new PatientNewOrder ();
    private Accession             accession       = new Accession ();
    private final String          dateSigned      = "[formcontrolname='dateSigned']";
    private final String          collectionDate  = "[formcontrolname='collectionDate']";

    public String getPhysicianOrderCode () {
        String xpath = "input[formcontrolname='externalOrderCode']";
        return readInput (xpath);
    }

    public void clickAssayTest (Assay assay) {
        assertTrue (click (format ("//*[@class='test-type-selection']//*[text()='%s']", assay.test)));
    }

    public void clickShipment () {
        assertTrue (click ("//*[text()='Shipment']"));
    }

    public Order parseOrder () {
        Order order = new Order ();
        order.id = getOrderId ();
        order.orderEntryType = getOrderType ();
        order.name = getOrderName ();
        order.status = getOrderStatus ();
        order.order_number = getOrderNumber ();
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
        ChargeType chargeType = billing.getBilling ();
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
        order.tests = getSelectedTests ();
        order.orderAttachments = getCoraAttachments ();
        order.shipmentAttachments = getShipmentAttachments ();
        order.trf = getDoraTrf ();
        order.doraAttachments = getDoraAttachments ();

        switch (chargeType) {
        case CommercialInsurance:
        case Medicare:
            order.patient.insurance1 = new Insurance ();
            order.patient.insurance1.provider = billing.getInsurance1Provider ();
            order.patient.insurance1.groupNumber = billing.getInsurance1GroupNumber ();
            order.patient.insurance1.policyNumber = billing.getInsurance1Policy ();
            order.patient.insurance1.insuredRelationship = billing.getInsurance1Relationship ();
            order.patient.insurance1.policyholder = billing.getInsurance1PolicyHolder ();
            order.patient.insurance1.hospitalizationStatus = billing.getInsurance1PatientStatus ();
            order.patient.insurance1.billingInstitution = billing.getInsurance1Hospital ();
            order.patient.insurance1.dischargeDate = billing.getInsurance1DischargeDate ();

            if (billing.hasSecondaryInsurance ()) {
                order.patient.insurance2 = new Insurance ();
                order.patient.insurance2.provider = billing.getInsurance2Provider ();
                order.patient.insurance2.groupNumber = billing.getInsurance2GroupNumber ();
                order.patient.insurance2.policyNumber = billing.getInsurance2Policy ();
                order.patient.insurance2.insuredRelationship = billing.getInsurance2Relationship ();
                order.patient.insurance2.policyholder = billing.getInsurance2PolicyHolder ();
            }

            if (billing.hasTertiaryInsurance ()) {
                order.patient.insurance3 = new Insurance ();
                order.patient.insurance3.provider = billing.getInsurance3Provider ();
                order.patient.insurance3.groupNumber = billing.getInsurance3GroupNumber ();
                order.patient.insurance3.policyNumber = billing.getInsurance3Policy ();
                order.patient.insurance3.insuredRelationship = billing.getInsurance3Relationship ();
                order.patient.insurance3.policyholder = billing.getInsurance3PolicyHolder ();
            }
            break;
        default:
            break;
        }

        order.patient.address = billing.getPatientAddress1 ();
        order.patient.phone = billing.getPatientPhone ();
        order.patient.locality = billing.getPatientCity ();
        order.patient.region = billing.getPatientState ();
        order.patient.postCode = billing.getPatientZipcode ();
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

    public void clickShowContainers () {
        assertTrue (click ("//*[@class='row']//*[contains(text(),'Containers')]"));
    }

    public Containers getContainers () {
        String rows = "//specimen-containers//*[@class='row']/..";
        return new Containers (waitForElements (rows).stream ().map (row -> {
            Container c = new Container ();
            c.id = getConId (getAttribute (row, "//*[text()='Adaptive Container ID']/..//a", "href"));
            c.containerNumber = getText (row, "//*[text()='Adaptive Container ID']/..//a");
            c.location = getText (row, "//*[text()='Current Storage Location']/..//div");

            if (isElementPresent (row, ".container-table")) {
                String css = "tbody tr";
                List <Container> children = findElements (row, css).stream ().map (childRow -> {
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

    public String getPatientGender () {
        return getText ("//label[text()='Gender']/../div[1]");
    }

    private String getOrderName () {
        return getText ("//labeled-value[@label='Order Name']/div/div[2]/span");
    }

    public String getPatientName () {
        return getText ("//label[text()='Patient']/../div[1]");
    }

    public String getPatientDOB () {
        return getText ("//label[text()='Birth Date']/../div[1]");
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
        boolean matchFound = patientNewOrder.searchOrCreatePatient (patient);
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
            waitForSpecimenDelivery ();
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
