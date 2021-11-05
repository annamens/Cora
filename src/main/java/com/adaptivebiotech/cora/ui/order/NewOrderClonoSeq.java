package com.adaptivebiotech.cora.ui.order;

import static com.adaptivebiotech.test.utils.PageHelper.ChargeType.Medicare;
import static com.adaptivebiotech.test.utils.PageHelper.DeliveryType.CustomerShipment;
import static java.lang.String.format;
import static java.util.EnumSet.allOf;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.BooleanUtils.toBoolean;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import java.util.List;
import com.adaptivebiotech.cora.dto.Containers;
import com.adaptivebiotech.cora.dto.Containers.Container;
import com.adaptivebiotech.cora.dto.Insurance;
import com.adaptivebiotech.cora.dto.Orders.Order;
import com.adaptivebiotech.cora.dto.Orders.OrderProperties;
import com.adaptivebiotech.cora.dto.Orders.OrderTest;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.dto.Physician;
import com.adaptivebiotech.cora.dto.Specimen;
import com.adaptivebiotech.cora.ui.shipment.Accession;
import com.adaptivebiotech.cora.ui.shipment.Shipment;
import com.adaptivebiotech.cora.utils.DateUtils;
import com.adaptivebiotech.test.utils.Logging;
import com.adaptivebiotech.test.utils.PageHelper.Anticoagulant;
import com.adaptivebiotech.test.utils.PageHelper.Assay;
import com.adaptivebiotech.test.utils.PageHelper.ChargeType;
import com.adaptivebiotech.test.utils.PageHelper.Compartment;
import com.adaptivebiotech.test.utils.PageHelper.ContainerType;
import com.adaptivebiotech.test.utils.PageHelper.DeliveryType;
import com.adaptivebiotech.test.utils.PageHelper.OrderStatus;
import com.adaptivebiotech.test.utils.PageHelper.SpecimenSource;
import com.adaptivebiotech.test.utils.PageHelper.SpecimenType;

/**
 * @author jpatel
 *
 */
public class NewOrderClonoSeq extends NewOrder {

    public BillingClonoSeq billing          = new BillingClonoSeq ();

    private final String   assayEl          = "//span[@ng-bind='test.name' and text()='%s']";
    private final String   specimenDelivery = "[name='specimenType']";

    public void clickAssayTest (Assay assay) {
        String type = "[ng-click*='" + assay.type + "']";
        if (!waitForElement (type).isSelected ())
            assertTrue (click (type));

        if (isElementPresent (".ng-hide[ng-show='ctrl.showTestMenu']"))
            assertTrue (click (".clickable[ng-bind*='showTestMenu']"));

        assertTrue (click (format (assayEl, assay.test)));
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
        for (String checkbox : checkboxes) {
            if (waitForElementVisible (checkbox).isSelected ()) {
                assertTrue (click (checkbox));
                waitForAjaxCalls ();
            }
        }
        String testSelection = "[ng-bind=\"ctrl.getTestSelectionSummary()\"]";
        String testSelectionText = waitForElementVisible (testSelection).getText ();
        assertEquals (testSelectionText, "No tests selected");
        waitForAjaxCalls ();
    }

    public Order parseOrder () {
        Order order = new Order ();
        order.id = getOrderId ();
        order.orderEntryType = getOrderType ();
        order.name = getOrderName ();
        order.status = getOrderStatus ();
        order.order_number = getOrderNum ();
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
        Logging.testLog ("DTO Shipment Arrival Date: " + order.specimenDto.arrivalDate);
        order.expectedTestType = getExpectedTest ();
        order.tests = allOf (Assay.class).stream ().map (a -> getTestState (a)).collect (toList ())
                                         .parallelStream ().filter (t -> t.selected).collect (toList ());
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

    private DeliveryType getSpecimenDelivery () {
        String css = "[ng-model^='ctrl.orderEntry.order.specimenDeliveryType']";
        return DeliveryType.getDeliveryType (getFirstSelectedText (css));
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
        pageLoading ();
        String editPatientLink = "a[ui-sref^='main.patient.details']";
        assertTrue (click (editPatientLink));
        pageLoading ();
    }

    public List <OrderTest> getSelectedTests () {
        return allOf (Assay.class).stream ().map (a -> getTestState (a)).collect (toList ())
                                  .parallelStream ().filter (t -> t.selected).collect (toList ());
    }

    public void verifyTests (List <Assay> assays) {
        List <OrderTest> orderTests = this.getSelectedTests ();
        assertEquals (orderTests.size (), assays.size ());
        for (OrderTest test : orderTests) {
            assertTrue (assays.contains (test.assay));
        }
    }

    public boolean isAbnStatusNotRequired () {
        return (isTextInElement ("div[ng-if^='ctrl.orderEntry.order.abnStatusType']", "Not Required"));
    }

    public void activateOrder () {
        clickActivateOrder ();
        assertTrue (click ("[ng-click='ctrl.ok()']"));
        moduleLoading ();
        pageLoading ();
        assertTrue (isTextInElement ("[ng-bind='ctrl.orderEntry.order.status']", "PendingActivation"));
        waitUntilActivated ();
    }

    public void transferTrf () {
        assertTrue (click ("[ng-click='ctrl.showTrfTransferModal()']"));
        assertTrue (isTextInElement (popupTitle, "Transfer TRF to New Order"));
        assertTrue (click ("[data-ng-click='ctrl.ok()']"));
        moduleLoading ();
        pageLoading ();
    }

    public void createNewPatient (Patient patient) {
        clickPickPatient ();
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
                List <Container> children = row.findElements (locateBy (css)).stream ().map (childRow -> {
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

    public void enterSpecimenDelivery (DeliveryType type) {
        assertTrue (clickAndSelectValue (specimenDelivery, "string:" + type));
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

    public String getSpecimenDeliverySelectedOption () {
        String css = "[ng-model^='ctrl.orderEntry.order.specimenDeliveryType']";
        if (isElementVisible (css)) {
            return getFirstSelectedText (css);
        }
        return null;
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
        assertTrue (setText ("[ng-model='ctrl.orderEntry.order.notes']", notes));
    }

    public String createClonoSeqOrder (Physician physician,
                                       Patient patient,
                                       String[] icdCodes,
                                       Assay assayTest,
                                       ChargeType chargeType,
                                       SpecimenType specimenType,
                                       SpecimenSource specimenSource,
                                       Anticoagulant anticoagulant) {

        selectNewClonoSEQDiagnosticOrder ();
        isCorrectPage ();

        selectPhysician (physician);
        createNewPatient (patient);
        for (String icdCode : icdCodes) {
            enterPatientICD_Codes (icdCode);
        }

        billing.selectBilling (chargeType);
        clickSave ();

        clickAssayTest (assayTest);
        enterSpecimenDelivery (CustomerShipment);
        clickEnterSpecimenDetails ();
        enterSpecimenType (specimenType);

        if (specimenSource != null)
            enterSpecimenSource (specimenSource);
        if (anticoagulant != null)
            enterAntiCoagulant (Anticoagulant.EDTA);

        enterCollectionDate (DateUtils.getPastFutureDate (-3));
        enterOrderNotes ("Creating Order in Cora");
        clickSave ();

        String orderNum = getOrderNum ();
        Logging.info ("ClonoSeq Order Number: " + orderNum);
        return orderNum;
    }

    public String createClonoSeqOrder (Physician physician,
                                       Patient patient,
                                       String[] icdCodes,
                                       Assay assayTest,
                                       ChargeType chargeType,
                                       SpecimenType specimenType,
                                       SpecimenSource specimenSource,
                                       Anticoagulant anticoagulant,
                                       OrderStatus orderStatus,
                                       ContainerType containerType) {
        // create clonoSEQ diagnostic order
        String orderNum = createClonoSeqOrder (physician,
                                               patient,
                                               icdCodes,
                                               assayTest,
                                               chargeType,
                                               specimenType,
                                               specimenSource,
                                               anticoagulant);

        // add diagnostic shipment
        new Shipment ().createShipment (orderNum, containerType);

        // accession complete
        if (orderStatus.equals (com.adaptivebiotech.test.utils.PageHelper.OrderStatus.Active)) {
            new Accession ().completeAccession ();

            // activate order
            isCorrectPage ();
            activateOrder ();
        }

        // URL path still contains dx, change it to details page
        // though both page looks similar, locators are different for dx and details in URL
        // when order is activated
        navigateToOrderDetailsPage (getOrderId ());
        isCorrectPage ();

        return orderNum;
    }
}
