/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.ui.order;

import static com.adaptivebiotech.cora.dto.Orders.NoChargeReason.NoReportIssued;
import static com.adaptivebiotech.cora.dto.Orders.OrderStatus.Active;
import static com.adaptivebiotech.cora.dto.Specimen.SpecimenActivation.FAILED;
import static com.adaptivebiotech.cora.dto.Specimen.SpecimenActivation.FAILED_ACTIVATION;
import static com.adaptivebiotech.cora.dto.Specimen.SpecimenActivation.PENDING;
import static com.adaptivebiotech.test.utils.DateHelper.formatDt1;
import static com.adaptivebiotech.test.utils.DateHelper.formatDt7;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.CellPellet;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.CellSuspension;
import static com.seleniumfy.test.utils.Logging.info;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.BooleanUtils.toBoolean;
import static org.apache.commons.lang3.EnumUtils.getEnum;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNoneBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import com.adaptivebiotech.cora.dto.Containers.ContainerType;
import com.adaptivebiotech.cora.dto.Element;
import com.adaptivebiotech.cora.dto.Orders.Assay;
import com.adaptivebiotech.cora.dto.Orders.Order;
import com.adaptivebiotech.cora.dto.Orders.OrderProperties;
import com.adaptivebiotech.cora.dto.Orders.OrderStatus;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.dto.Physician;
import com.adaptivebiotech.cora.dto.Specimen;
import com.adaptivebiotech.cora.dto.Specimen.Anticoagulant;
import com.adaptivebiotech.cora.ui.patient.PickPatientModule;
import com.adaptivebiotech.cora.ui.shipment.Accession;
import com.adaptivebiotech.cora.ui.shipment.NewShipment;
import com.adaptivebiotech.cora.utils.PageHelper.AbnStatus;
import com.adaptivebiotech.test.utils.PageHelper.Compartment;
import com.adaptivebiotech.test.utils.PageHelper.SpecimenSource;
import com.adaptivebiotech.test.utils.PageHelper.SpecimenType;
import com.seleniumfy.test.utils.Timeout;

/**
 * @author jpatel
 *
 */
public class NewOrderClonoSeq extends NewOrder {

    public BillingNewOrderClonoSeq billing              = new BillingNewOrderClonoSeq (staticNavBarHeight);
    public PickPatientModule       pickPatient          = new PickPatientModule ();
    private Accession              accession            = new Accession ();
    private final String           specimenDetails      = "#specimen-details";
    private final String           specimenCoordination = "[formcontrolname='specimenCoordination']";
    private final String           specimenTypeOther    = "#specimen-entry-other-specimen-type";
    private final String           specimenSourceOther  = "#specimen-entry-specimen-source-other";
    private final String           uniqueSpecimenId     = "[formcontrolname='uniqueSpecimenId']";
    private final String           retrievalDate        = "#specimen-entry-retrieval-date";
    private final String           compartment          = "[formcontrolname='compartment']";
    private final String           anticoagulantOther   = "[formcontrolname='anticoagulantOther']";
    private final String           abnStatus            = "#abn-status-type";

    public void activateOrder () {
        String orderNumber = getOrderNumber ();
        clickSaveAndActivate ();
        List <String> errors = getRequiredFieldMsgs ();
        assertEquals (errors.size (), 0, "Order No: " + orderNumber + " failed to activate, Errors: " + errors);
        checkOrderForErrors ();
        confirmActivate ();
        checkOrderForErrors ();
        moduleLoading ();
        waitUntilActivated ();
    }

    public void clickSaveAndActivate () {
        assertTrue (click ("#order-entry-save-and-activate"));
    }

    public void confirmActivate () {
        assertTrue (isTextInElement (popupTitle, "Confirm Order"));
        assertTrue (click ("//*[text()='Activate the Order']"));
    }

    public void cancelActivate () {
        assertTrue (isTextInElement (popupTitle, "Confirm Order"));
        assertTrue (click ("//*[text()='Cancel']"));
    }

    public void clickAssayTest (Assay assay) {
        String type = format ("//*[@class='test-type-selection']//*[text()='%s']/ancestor::label//input", assay.type);
        if (!waitForElement (type).isSelected ())
            assertTrue (click (type));

        String showTestMenu = "//*[@class='test-selection']//a[text()='Show Test Menu']";
        if (isElementPresent (showTestMenu))
            assertTrue (click (showTestMenu));

        assertTrue (click (format ("//*[text()='%s']/ancestor::li//input", assay.test)));
    }

    public void findSpecimenId (String id) {
        assertTrue (setText ("[ng-model='ctrl.specimenNumber']", id));
        assertTrue (click ("[ng-click='ctrl.reuseSpecimen(ctrl.specimenNumber)']"));
        assertTrue (isTextInElement (popupTitle, "Patient Warning"));
        assertTrue (click ("[ng-click='ctrl.ok()']"));
        moduleLoading ();
        assertTrue (isTextInElement (specimenNumber, id));
    }

    public String getAbnStatus () {
        if (isElementVisible (abnStatus)) {
            return getFirstSelectedValue (abnStatus);
        } else {
            return null;
        }
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
        order.dateSigned = getDateSigned ();
        order.customerInstructions = getInstructions ();
        order.physician = new Physician ();
        order.physician.providerFullName = getProviderName ();
        order.physician.accountName = getProviderAccount ();
        order.physician.medicareEnrolled = getProviderMedicareEnrolled ();
        order.patient = new Patient ();
        order.patient.fullname = getPatientName ();
        order.patient.dateOfBirth = getPatientDOB ();
        order.patient.gender = getPatientGender ();
        order.patient.patientCode = getPatientCode ();
        order.patient.externalPatientCode = getBillingPatientCode ();
        order.patient.testStatus = getPatientMRDStatus ();
        order.patient.race = getPatientRace ();
        order.patient.ethnicity = getPatientEthnicity ();
        order.patient.mrn = getPatientMRN ();
        order.patient.notes = getPatientNotes ();
        order.patient = billing.getPatientBilling (order.patient);
        order.icdcodes = getPatientICD_Codes ();
        order.properties = new OrderProperties (order.patient.billingType, getSpecimenDelivery ());
        order.specimenDto = new Specimen ();
        order.specimenDto.specimenNumber = getSpecimenId ();
        order.specimenDto.sampleType = getSpecimenType ();
        order.specimenDto.sampleSource = getSpecimenSource ();
        order.specimenDto.compartment = getCompartment ();
        order.specimenDto.anticoagulant = getAnticoagulant ();
        order.specimenDto.collectionDate = getCollectionDate ();
        order.specimenDto.reconciliationDate = getReconciliationDate ();
        order.specimenDto.retrievalDate = getRetrievalDate ();
        order.specimenDto.approvedDate = getSpecimenApprovalDate ();
        order.specimenDto.approvalStatus = getSpecimenApprovalStatus ();
        order.specimenDto.activationDate = getSpecimenActivationDate ();
        order.specimenDisplayArrivalDate = getShipmentArrivalDate ();
        order.intakeCompletedDate = getIntakeCompleteDate ();
        order.specimenDisplayContainerType = getSpecimenContainerType ();
        order.specimenDisplayContainerCount = getSpecimenContainerQuantity ();
        order.tests = getSelectedTests ();
        LocalDate dueDate = getDueDate ();
        for (int i = 0; i < order.tests.size (); i++) {
            order.tests.get (i).dueDate = dueDate;
        }
        order.documentedByType = getOrderAuthorization ();
        order.orderAttachments = getCoraAttachments ();
        order.shipmentAttachments = getShipmentAttachments ();
        order.trf = getDoraTrf ();
        order.doraAttachments = getDoraAttachments ();
        order.notes = getOrderNotes ();
        return order;
    }

    public void addPatientICDCode (String icdCode) {
        String expectedModalTitle = "Test Selection Warning";
        this.enterPatientICD_Codes (icdCode);
        String actualText = waitForElementVisible ("[ng-bind-html=\"ctrl.dialogOptions.headerText\"]").getText ();
        assertEquals (actualText, expectedModalTitle);
        assertTrue (click ("[data-ng-click='ctrl.ok();']"));
    }

    public void clickEditPatient () {
        String editPatientLink = "//*[text()='Edit Patient Demographics']";
        assertTrue (click (editPatientLink));
        pageLoading ();
    }

    public void clickEnterSpecimenDetails () {
        assertTrue (click (specimenDetails));
    }

    public void clickPathologyRetrieval () {
        assertTrue (click (specimenCoordination));
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

    public Compartment getCompartment () {
        String compartmentVal = isElementVisible (compartment) ? getFirstSelectedText (compartment) : null;
        return isNotBlank (compartmentVal) ? Compartment.getCompartment (compartmentVal) : null;
    }

    public boolean isCompartmentEnabled () {
        return waitForElement (compartment).isEnabled ();
    }

    public void enterAntiCoagulant (Anticoagulant anticoagulantEnum) {
        assertTrue (clickAndSelectValue (anticoagulant, anticoagulantEnum.name ()));
    }

    public List <Anticoagulant> getAntiCoagulantTypeList () {
        return getDropdownOptions (anticoagulant).stream ()
                .filter (optionText -> optionText.length () > 0 && !optionText.contains ("Select..."))
                .map (optionText -> {
                    return Anticoagulant.valueOf (optionText);
                }).collect (toList ());
    }

    public void enterAntiCoagulantOther (String anticoagulant) {
        assertTrue (setText (anticoagulantOther, anticoagulant));
    }

    public void enterSpecimenSource (SpecimenSource source) {
        assertTrue (clickAndSelectValue (specimenSource, source.name ()));
    }

    public boolean isSpecimenSourceEnabled () {
        return waitForElement (specimenSource).isEnabled ();
    }

    public void enterSpecimenSourceOther (String source) {
        assertTrue (setText (specimenSourceOther, source));
    }

    public void enterCellCount (int count) {
        assertTrue (setText ("#specimen-entry-cell-count", String.valueOf (count)));
    }

    public void enterUniqueSpecimenId (String specimenId) {
        assertTrue (setText (uniqueSpecimenId, specimenId));
    }

    public String getUniqueSpecimenId () {
        return isElementVisible (uniqueSpecimenId) ? readInput (uniqueSpecimenId) : null;
    }

    public boolean isUniqueSpecimenIdEnabled () {
        return waitForElement (uniqueSpecimenId).isEnabled ();
    }

    public void enterRetrievalDate (String date) {
        assertTrue (setText (retrievalDate, date));
    }

    public LocalDateTime getRetrievalDate () {
        String data = isElementVisible (retrievalDate) ? readInput (retrievalDate) : null;
        return isNoneBlank (data) ? LocalDateTime.parse (data, formatDt1) : null;
    }

    public boolean isRetrievalDateEnabled () {
        return waitForElement (retrievalDate).isEnabled ();
    }

    public LocalDateTime waitUntilSpecimenActivated () {
        Timeout timer = new Timeout (millisDuration * 12, millisPoll * 30);
        while (!timer.Timedout ()) {
            String specimenActivationDate = getSpecimenActivationDate ();
            if (isBlank (specimenActivationDate) || specimenActivationDate.equals (PENDING.label)) {
                timer.Wait ();
                refresh ();
            } else if (specimenActivationDate.equals (FAILED_ACTIVATION.label) || specimenActivationDate.equals (FAILED.label)) {
                fail (format ("Specimen activation failed , Order No: %s, Specimen Activation: %s",
                              getOrderNumber (),
                              specimenActivationDate));
            } else {
                return LocalDateTime.parse (specimenActivationDate, formatDt7);
            }
        }
        fail (format ("Specimen did not activate in time, Order No: %s, Specimen Activation: %s",
                      getOrderNumber (),
                      getSpecimenActivationDate ()));
        return null;
    }

    public boolean isPathologyRetrievalVisible () {
        return isElementVisible (specimenCoordination);
    }

    public boolean isPathologyRetrievalSelected () {
        return findElement (specimenCoordination).isSelected ();
    }

    public void closeTestSelectionWarningModal () {
        String expectedModalTitle = "Test Selection Warning";
        String modalHeader = "[ng-bind-html=\"ctrl.dialogOptions.headerText\"]";
        assertTrue (isTextInElement (modalHeader, expectedModalTitle));
        clickPopupOK ();
    }

    public Element getStabilizationWindow () {
        Element el = new Element ();
        String xpath = "//specimen-stabilization-window//div";
        el.text = getText (xpath + "//strong");
        el.color = getCssValue (xpath, "background-color");
        return el;
    }

    /**
     * Create ClonoSeq Pending Order by filling out all the required fields and passed arguments on
     * New Order ClonoSeq page, and returns order no.
     * 
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
            billing.selectBilling (patient.billingType);
            billing.enterInsurance1PatientStatus (patient.insurance1.hospitalizationStatus);
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
        if (specimen.compartment != null)
            enterCompartment (specimen.compartment);
        if (specimen.anticoagulant != null)
            enterAntiCoagulant (specimen.anticoagulant);
        if (asList (CellPellet, CellSuspension).contains (specimen.sampleType))
            enterCellCount (1000000);

        enterCollectionDate ((LocalDate) specimen.collectionDate);
        clickAssayTest (assayTest);
        clickSave ();

        Order order = new Order ();
        order.orderNumber = getOrderNumber ();
        order.id = getOrderId ();
        info (format ("ClonoSeq Order Number: %s (%s)", order.orderNumber, order.id));
        return order;
    }

    /**
     * Create ClonoSeq Pending (differs from above method as this method creates shipment as well)
     * or Active Order by passing orderStatus argument. Returns order no.
     * 
     * NOTE: Keep updating this method, and try to always use these methods to create ClonoSeq
     * Pending Or Active Order
     * 
     * @param physician
     * @param patient
     * @param icdCodes
     * @param assayTest
     * @param specimen
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

    /**
     * @param requiredincludedbillmedicare
     */
    public void changeABNStatus (AbnStatus requiredIncludedBillMedicare) {
        // TODO Auto-generated method stub
        billing.enterABNstatus (requiredIncludedBillMedicare);

    }
}
