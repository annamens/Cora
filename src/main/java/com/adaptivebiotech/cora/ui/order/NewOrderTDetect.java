package com.adaptivebiotech.cora.ui.order;

import static com.adaptivebiotech.test.utils.PageHelper.ChargeType.Medicare;
import static java.util.EnumSet.allOf;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.BooleanUtils.toBoolean;
import static org.testng.Assert.assertTrue;
import java.util.List;
import com.adaptivebiotech.cora.dto.Containers;
import com.adaptivebiotech.cora.dto.Containers.Container;
import com.adaptivebiotech.cora.dto.Insurance;
import com.adaptivebiotech.cora.dto.Orders.Order;
import com.adaptivebiotech.cora.dto.Orders.OrderProperties;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.dto.Patient.Address;
import com.adaptivebiotech.cora.dto.Physician;
import com.adaptivebiotech.cora.dto.Specimen;
import com.adaptivebiotech.cora.ui.shipment.Accession;
import com.adaptivebiotech.cora.ui.shipment.Shipment;
import com.adaptivebiotech.test.utils.Logging;
import com.adaptivebiotech.test.utils.PageHelper.Assay;
import com.adaptivebiotech.test.utils.PageHelper.ChargeType;
import com.adaptivebiotech.test.utils.PageHelper.ContainerType;
import com.adaptivebiotech.test.utils.PageHelper.DeliveryType;
import com.adaptivebiotech.test.utils.PageHelper.OrderStatus;

/**
 * @author jpatel
 *
 */
public class NewOrderTDetect extends NewOrder {

    public BillingNewOrderTDetect billing        = new BillingNewOrderTDetect ();

    private final String          dateSigned     = "[formcontrolname='dateSigned']";
    private final String          orderNotes     = "#order-notes";
    private final String          collectionDate = "[formcontrolname='collectionDate']";

    public String getPhysicianOrderCode () {
        String xpath = "input[formcontrolname='externalOrderCode']";
        return readInput (xpath);
    }

    public void activateOrder () {
        clickSaveAndActivate ();
        moduleLoading ();
        pageLoading ();
        waitUntilActivated ();
    }

    public void createNewPatient (Patient patient) {
        clickPickPatient ();
        assertTrue (click ("#new-patient"));
        assertTrue (waitForElementInvisible (".ab-panel.matches"));
        assertTrue (isTextInElement (popupTitle, "Create New Patient"));
        assertTrue (setText ("#firstName", patient.firstName));
        assertTrue (setText ("#middleName", patient.middleName));
        assertTrue (setText ("#lastName", patient.lastName));
        assertTrue (setText ("#dateOfBirth", patient.dateOfBirth));
        assertTrue (clickAndSelectText ("#gender", patient.gender));
        if (patient.race != null) {
            assertTrue (clickAndSelectText ("#race", patient.race.text));
        }
        if (patient.ethnicity != null) {
            assertTrue (clickAndSelectText ("#ethnicity", patient.ethnicity.text));
        }
        assertTrue (click ("//button[text()='Save']"));
        assertTrue (setText ("[formcontrolname='mrn']", patient.mrn));
    }

    public void clickAssayTest (Assay assay) {
        waitForElements (".test-type-selection .panel-label").forEach (el -> {
            if (el.getText ().equals (assay.test)) {
                click (el, "input");
            }
        });
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
        ChargeType chargeType = billing.getBillingType ();
        order.patient.billingType = chargeType;
        order.patient.abnStatusType = Medicare.equals (chargeType) ? billing.getAbnStatus () : null;
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
        order.patient.insurance1.provider = billing.getInsurance1Provider ();
        order.patient.insurance1.groupNumber = billing.getInsurance1GroupNumber ();
        order.patient.insurance1.policyNumber = billing.getInsurance1Policy ();
        order.patient.insurance1.insuredRelationship = billing.getInsurance1Relationship ();
        order.patient.insurance1.policyholder = billing.getInsurance1PolicyHolder ();
        order.patient.insurance1.hospitalizationStatus = billing.getInsurance1PatientStatus ();
        order.patient.insurance1.billingInstitution = billing.getInsurance1Hospital ();
        order.patient.insurance1.dischargeDate = billing.getInsurance1DischargeDate ();
        order.patient.insurance2 = new Insurance ();
        order.patient.insurance2.provider = billing.getInsurance2Provider ();
        order.patient.insurance2.groupNumber = billing.getInsurance2GroupNumber ();
        order.patient.insurance2.policyNumber = billing.getInsurance2Policy ();
        order.patient.insurance2.insuredRelationship = billing.getInsurance2Relationship ();
        order.patient.insurance2.policyholder = billing.getInsurance2PolicyHolder ();
        order.patient.address = billing.getPatientAddress1 ();
        order.patient.phone = billing.getPatientPhone ();
        order.patient.locality = billing.getPatientCity ();
        order.patient.region = billing.getPatientState ();
        order.patient.postCode = billing.getPatientZipcode ();
        order.notes = getOrderNotes ();
        return order;
    }

    private DeliveryType getSpecimenDelivery () {
        String css = "[formcontrolname='specimenDeliveryType']";
        return DeliveryType.getDeliveryType (getFirstSelectedText (css));
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
                List <Container> children = row.findElements (locateBy (css)).stream ().map (childRow -> {
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

    public String getSpecimenDeliverySelectedOption () {
        String css = "[formcontrolname='specimenDeliveryType']";
        if (isElementVisible (css)) {
            return getFirstSelectedText (css);
        }
        return null;
    }

    public void enterOrderNotes (String notes) {
        assertTrue (setText (orderNotes, notes));
    }

    public String getOrderNotes () {
        return isElementPresent (orderNotes) && isElementVisible (orderNotes) ? readInput (orderNotes) : null;
    }

    public void enterDateSigned (String date) {
        assertTrue (setText (dateSigned, date));
    }

    public String getDateSigned () {
        return isElementPresent (dateSigned) && isElementVisible (dateSigned) ? readInput (dateSigned) : null;
    }

    public String getCollectionDt () {
        return isElementPresent (collectionDate) && isElementVisible (collectionDate) ? readInput (collectionDate) : null;
    }

    public String getPatientGender () {
        return getText ("//label[text()='Gender']/../div[1]");
    }

    public String getOrderNum () {
        return getText ("//label[@id='order-number-text']/../div[1]/span");
    }

    public String getOrderName () {
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
     * NOTE: Keep updating this method, and try to always use these methods to create TDetect Order
     * 
     * @param physician
     * @param patient
     * @param icdCodes
     * @param collectionDate
     * @param assayTest
     * @param chargeType
     * @param patientAddress
     * @return
     */
    public String createTDetectOrder (Physician physician,
                                      Patient patient,
                                      String[] icdCodes,
                                      String collectionDate,
                                      Assay assayTest,
                                      ChargeType chargeType,
                                      Address patientAddress) {
        selectNewTDetectDiagnosticOrder ();
        isCorrectPage ();

        selectPhysician (physician);
        createNewPatient (patient);
        for (String icdCode : icdCodes) {
            enterPatientICD_Codes (icdCode);
        }
        clickSave ();

        enterCollectionDate (collectionDate);

        clickAssayTest (assayTest);
        billing.selectBilling (chargeType);
        billing.enterPatientAddress (patientAddress);
        clickSave ();

        String orderNum = getOrderNum ();
        Logging.info ("T-Detect Order Number: " + orderNum);
        return orderNum;
    }

    /**
     * Create TDetect Pending (differs from above method as this method creates shipment as well)
     * or Active Order by passing orderStatus argument. Returns order no.
     * NOTE: Keep updating this method, and try to always use these methods to create TDetect
     * Pending Or Active Order
     * 
     * @param physician
     * @param patient
     * @param icdCodes
     * @param collectionDate
     * @param assayTest
     * @param chargeType
     * @param patientAddress
     * @param orderStatus
     * @param containerType
     * @return
     */
    public String createTDetectOrder (Physician physician,
                                      Patient patient,
                                      String[] icdCodes,
                                      String collectionDate,
                                      Assay assayTest,
                                      ChargeType chargeType,
                                      Address patientAddress,
                                      OrderStatus orderStatus,
                                      ContainerType containerType) {
        // create T-Detect order
        String orderNum = createTDetectOrder (physician,
                                              patient,
                                              icdCodes,
                                              collectionDate,
                                              assayTest,
                                              chargeType,
                                              patientAddress);

        // add diagnostic shipment
        new Shipment ().createShipment (orderNum, containerType);

        // accession complete
        if (orderStatus.equals (com.adaptivebiotech.test.utils.PageHelper.OrderStatus.Active)) {
            new Accession ().completeAccession ();

            // activate order
            isCorrectPage ();
            activateOrder ();
        }

        navigateToOrderDetailsPage (getOrderId ());
        isCorrectPage ();

        return orderNum;
    }
}
