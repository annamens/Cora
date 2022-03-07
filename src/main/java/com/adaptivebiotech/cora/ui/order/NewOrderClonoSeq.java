package com.adaptivebiotech.cora.ui.order;

import static com.adaptivebiotech.cora.dto.Orders.ChargeType.Medicare;
import static com.adaptivebiotech.cora.dto.Orders.OrderStatus.Active;
import static com.adaptivebiotech.test.utils.Logging.info;
import static com.adaptivebiotech.test.utils.TestHelper.formatDt7;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.BooleanUtils.toBoolean;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import com.adaptivebiotech.cora.dto.Containers;
import com.adaptivebiotech.cora.dto.Containers.Container;
import com.adaptivebiotech.cora.dto.Containers.ContainerType;
import com.adaptivebiotech.cora.dto.Insurance;
import com.adaptivebiotech.cora.dto.Orders.Assay;
import com.adaptivebiotech.cora.dto.Orders.ChargeType;
import com.adaptivebiotech.cora.dto.Orders.DeliveryType;
import com.adaptivebiotech.cora.dto.Orders.Order;
import com.adaptivebiotech.cora.dto.Orders.OrderProperties;
import com.adaptivebiotech.cora.dto.Orders.OrderStatus;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.dto.Physician;
import com.adaptivebiotech.cora.dto.Specimen;
import com.adaptivebiotech.cora.dto.Specimen.Anticoagulant;
import com.adaptivebiotech.cora.dto.UploadFile;
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

    public BillingNewOrderClonoSeq billing          = new BillingNewOrderClonoSeq (staticNavBarHeight);
    private Accession              accession        = new Accession ();
    private final String           orderNotes       = "[ng-model='ctrl.orderEntry.order.notes']";
    private final String           specimenDelivery = "[ng-model='ctrl.orderEntry.order.specimenDeliveryType']";

    public void clickAssayTest (Assay assay) {
        String type = "[ng-click*='" + assay.type + "']";
        if (!waitForElement (type).isSelected ())
            assertTrue (click (type));

        if (isElementPresent (".ng-hide[ng-show='ctrl.showTestMenu']"))
            assertTrue (click (".clickable[ng-bind*='showTestMenu']"));

        assertTrue (click (format ("//*[@ng-bind='test.name' and text()='%s']", assay.test)));
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

    private String getOrderName () {
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

    public String getPatientName () {
        return getText ("[ng-bind$='patientFullName']");
    }

    public String getPatientDOB () {
        return getText ("[ng-bind^='ctrl.orderEntry.order.patient.dateOfBirth']");
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

    public String getPatientGender () {
        return getText ("[ng-bind='ctrl.orderEntry.order.patient.gender']");
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

    public void activateOrder () {
        clickSaveAndActivate ();
        assertTrue (isTextInElement (popupTitle, "Confirm Order"));
        assertTrue (click ("[ng-click='ctrl.ok()']"));
        moduleLoading ();
        pageLoading ();
        waitUntilActivated ();
    }

    @Override
    public void createNewPatient (Patient patient) {
        assertTrue (click ("#new-patient"));
        assertTrue (waitForElementInvisible (".ab-panel.matches"));
        assertTrue (isTextInElement (popupTitle, "Create New Patient"));
        assertTrue (setText ("[name='firstName']", patient.firstName));
        assertTrue (setText ("[name='middleName']", patient.middleName));
        assertTrue (setText ("[name='lastName']", patient.lastName));
        assertTrue (setText ("[name='dateOfBirth']", patient.dateOfBirth));
        assertTrue (clickAndSelectValue ("[name='gender']", "string:" + patient.gender));
        assertTrue (click ("//button[text()='Save']"));
        assertTrue (setText ("[name='mrn']", patient.mrn));
    }

    public void setPatientMRN (String mrn) {
        assertTrue (setText ("#mrn-input", mrn));
    }

    public void clickShowContainers () {
        assertTrue (click ("[ng-click^='ctrl.showContainers']"));
    }

    public Containers getContainers () {
        String rows = "[ng-repeat='group in ctrl.groupContainers()']";
        return new Containers (waitForElements (rows).stream ().map (row -> {
            Container c = new Container ();
            c.id = getConId (getAttribute (row, "[data-ng-bind='group.holdingContainer.containerNumber']", "href"));
            c.containerNumber = getText (row, "[data-ng-bind='group.holdingContainer.containerNumber']");
            c.location = getText (row, "[ng-bind='group.holdingContainer.location']");

            if (isElementPresent (row, ".container-table")) {
                String css = "[ng-repeat='child in group.containers']";
                List <Container> children = findElements (row, css).stream ().map (childRow -> {
                    Container childContainer = new Container ();
                    childContainer.id = getConId (getAttribute (childRow,
                                                                "[data-ng-bind='child.container.containerNumber']",
                                                                "href"));
                    childContainer.containerNumber = getText (childRow,
                                                              "[data-ng-bind='child.container.containerNumber']");
                    childContainer.name = getText (childRow, "[ng-bind^='child.container.displayName']");
                    childContainer.integrity = getText (childRow, "[ng-bind='child.container.integrity']");
                    childContainer.root = c;
                    return childContainer;
                }).collect (toList ());
                c.children = children;
            }
            return c;
        }).collect (toList ()));
    }

    public void waitForSpecimenDelivery () {
        assertTrue (waitUntil (millisDuration, millisPoll, new Function <WebDriver, Boolean> () {
            public Boolean apply (WebDriver driver) {
                return getDropdownOptions (specimenDelivery).size () > 0;
            }
        }));
    }

    public void enterSpecimenDelivery (DeliveryType type) {
        assertTrue (clickAndSelectValue (specimenDelivery, "string:" + type));
    }

    public DeliveryType getSpecimenDelivery () {
        return DeliveryType.getDeliveryType (getFirstSelectedText (specimenDelivery));
    }

    public List <String> getSpecimenDeliveryOptions () {
        return getDropdownOptions (specimenDelivery);
    }

    public void clickEnterSpecimenDetails () {
        assertTrue (click ("[ng-click='ctrl.showSpecimen=!ctrl.showSpecimen']"));
    }

    public void enterSpecimenType (SpecimenType type) {
        assertTrue (clickAndSelectValue ("[ng-model='ctrl.orderEntry.specimen.sampleType']", "string:" + type));
    }

    public void enterSpecimenTypeOther (String type) {
        assertTrue (setText ("[name='otherSampleType']", type));
    }

    public void enterCompartment (Compartment compartment) {
        assertTrue (clickAndSelectValue ("[ng-model='ctrl.orderEntry.specimen.compartment']", "string:" + compartment));
    }

    public void enterAntiCoagulant (Anticoagulant anticoagulant) {
        assertTrue (clickAndSelectValue ("[name='anticoagulant']", "string:" + anticoagulant));
    }

    public void enterAntiCoagulantOther (String anticoagulant) {
        assertTrue (setText ("[name='otherAnticoagulant']", anticoagulant));
    }

    public void enterSpecimenSource (SpecimenSource source) {
        assertTrue (clickAndSelectValue ("[name='specimenSource']", "string:" + source));
    }

    public void enterSpecimenSourceOther (String source) {
        assertTrue (setText ("[name='otherSpecimenSource']", source));
    }

    public void enterRetrievalDate (String date) {
        String cssRetrievalDate = "#specimen-entry-retrieval-date";
        assertTrue (setText (cssRetrievalDate, date));
    }

    public String getRetrievalDate () {
        String css = "[ng-model^='ctrl.orderEntry.specimen.retrievalDate']";
        if (isElementVisible (css)) {
            return readInput (css);
        }
        return null;
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
    public String createClonoSeqOrder (Physician physician,
                                       Patient patient,
                                       String[] icdCodes,
                                       Assay assayTest,
                                       Specimen specimen) {

        selectNewClonoSEQDiagnosticOrder ();
        isCorrectPage ();

        selectPhysician (physician);
        boolean matchFound = searchOrCreatePatient (patient);
        enterPatientICD_Codes (icdCodes);

        switch (patient.billingType) {
        case CommercialInsurance:
            billing.enterInsuranceInfo (patient);
            break;
        case Medicare:
            billing.enterMedicareInfo (patient);
            break;
        case Client:
        case PatientSelfPay:
            billing.enterBill (patient);
            break;
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

        String orderNum = getOrderNumber ();
        info ("ClonoSeq Order Number: " + orderNum);
        return orderNum;
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
    public String createClonoSeqOrder (Physician physician,
                                       Patient patient,
                                       String[] icdCodes,
                                       Assay assayTest,
                                       Specimen specimen,
                                       OrderStatus orderStatus,
                                       ContainerType containerType) {
        // create clonoSEQ diagnostic order
        String orderNum = createClonoSeqOrder (physician, patient, icdCodes, assayTest, specimen);

        // add diagnostic shipment
        new NewShipment ().createShipment (orderNum, containerType);

        // accession complete
        if (orderStatus.equals (Active)) {
            accession.completeAccession ();

            // activate order
            isCorrectPage ();
            waitForSpecimenDelivery ();
            activateOrder ();
        } else {
            accession.clickOrderNumber ();
            isCorrectPage ();
        }

        return orderNum;
    }
}
