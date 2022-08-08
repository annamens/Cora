/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.ui.order;

import static com.adaptivebiotech.cora.dto.Containers.ContainerType.getContainerType;
import static com.adaptivebiotech.test.utils.DateHelper.formatDt1;
import static com.adaptivebiotech.test.utils.DateHelper.formatDt2;
import static com.adaptivebiotech.test.utils.DateHelper.formatDt7;
import static java.lang.ClassLoader.getSystemResource;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.EnumSet.allOf;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isNoneBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.substringBetween;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import com.adaptivebiotech.cora.dto.Containers;
import com.adaptivebiotech.cora.dto.Containers.Container;
import com.adaptivebiotech.cora.dto.Containers.ContainerType;
import com.adaptivebiotech.cora.dto.Orders.Assay;
import com.adaptivebiotech.cora.dto.Orders.DeliveryType;
import com.adaptivebiotech.cora.dto.Orders.OrderAuthorization;
import com.adaptivebiotech.cora.dto.Orders.OrderTest;
import com.adaptivebiotech.cora.dto.Physician;
import com.adaptivebiotech.cora.dto.Specimen.Anticoagulant;
import com.adaptivebiotech.cora.dto.Specimen.SpecimenStatus;
import com.adaptivebiotech.cora.dto.UploadFile;
import com.adaptivebiotech.cora.utils.PageHelper.Ethnicity;
import com.adaptivebiotech.cora.utils.PageHelper.Race;
import com.adaptivebiotech.test.utils.PageHelper.SpecimenSource;
import com.adaptivebiotech.test.utils.PageHelper.SpecimenType;
import com.seleniumfy.test.utils.Timeout;

/**
 * @author jpatel
 *
 */
public abstract class NewOrder extends OrderHeader {

    private final String   orderType           = "//*[@label='Order Type']//span";
    private final String   orderNumber         = "//*[@label='Order #']//span";
    private final String   dateSigned          = "[formcontrolname='dateSigned']";
    private final String   dueDate             = "labeled-value[label='Due Date'] span";
    private final String   instructions        = "[formcontrolname='specialInstructions']";
    private final String   patientMrdStatus    = ".patient-status";
    private final String   orderNotes          = "#order-notes";
    private final String   patientMrn          = "[formcontrolname='mrn']";
    private final String   patientNotes        = ".patient-note";
    private final String   orderAuth           = "order-documentation .row span";
    private final String   attachmentC         = "//h3[contains(text(),'%s')]/ancestor::div[@class='row']";
    private final String   attachments         = attachmentC + "//attachments//*[contains(@class,'row')]";
    private final String   fileLoc             = "//a[contains(text(),'%s')]";
    private final String   fileLocInC          = attachmentC + fileLoc;
    private final String   fileUpload          = ".attachment-upload-input";
    private final String   specimenDelivery    = "[formcontrolname='specimenDeliveryType']";
    private final String   collectionDate      = "[formcontrolname='collectionDate']";
    private final String   collectionDateLabel = "//label[contains(text(),'Collection Date')]";
    private final String   shipmentArrivalLink = "[ng-reflect-state='main.shipment.entry']";
    protected final String specimenType        = "[formcontrolname='sampleType']";
    protected final String specimenSource      = "[formcontrolname='source']";
    protected final String anticoagulant       = "[formcontrolname='anticoagulant']";
    protected final String specimenNumber      = "//*[text()='Adaptive Specimen ID']/..//div";
    protected final String toastContainer      = "#toast-container";
    protected final String toastError          = ".toast-error";
    protected final String toastSuccess        = ".toast-success";
    protected final String toastMessage        = ".toast-message";
    protected final String textDanger          = ".text-danger";

    public NewOrder () {
        staticNavBarHeight = 200;
    }

    @Override
    public void isCorrectPage () {
        assertTrue (isTextInElement ("[role='tablist'] .active a", "ORDER DETAILS"));
        pageLoading ();
    }

    @Override
    public void gotoOrderEntry (String orderId) {
        super.gotoOrderEntry (orderId);
        isCorrectPage ();
    }

    public String getOrderId () {
        return substringBetween (getCurrentUrl (), "cora/order/dx/", "/details");
    }

    public List <String> getSectionHeaders () {
        return getTextList (".order-entry h2");
    }

    public abstract void activateOrder ();

    public String getOrderName () {
        return getText ("//labeled-value[@label='Order Name']/div/div[2]/span");
    }

    public String isTrfAttached () {
        return getText ("labeled-value[label='Internal TRF Attached'] span");
    }

    public void enterDateSigned (LocalDate date) {
        assertTrue (setText (dateSigned, formatDt1.format (date)));
    }

    public LocalDate getDateSigned () {
        String data = isElementVisible (dateSigned) ? readInput (dateSigned) : null;
        return isNoneBlank (data) ? LocalDate.parse (data, formatDt2) : null;
    }

    public LocalDate getDueDate () {
        return isElementVisible (dueDate) ? LocalDate.parse (readInput (dueDate), formatDt1) : null;
    }

    public String getPatientName () {
        return getText ("//label[text()='Patient']/../div[1]");
    }

    public String getPatientDOB () {
        return getText ("//label[text()='Birth Date']/../div[1]");
    }

    public String getPatientGender () {
        return getText ("//label[text()='Gender']/../div[1]");
    }

    public Race getPatientRace () {
        return Race.getRace (getText ("//label[text()='Race']/../div[1]"));
    }

    public Ethnicity getPatientEthnicity () {
        return Ethnicity.getEthnicity (getText ("//label[text()='Ethnicity']/../div[1]"));
    }

    public String getPatientMRDStatus () {
        return getText (patientMrdStatus);
    }

    public List <String> getPatientICDCodes () {
        String xpath = "//label[text()='ICD Codes']/../div";
        Timeout timer = new Timeout (millisDuration, millisPoll);
        while (!timer.Timedout ()) {
            List <String> rv = null;
            try {
                rv = getTextList (xpath);
                return rv;
            } catch (StaleElementReferenceException e) {
                timer.Wait ();
            }
        }

        fail ("can't get Patient ICD Codes");
        return null;
    }

    public void clickPatientOrderHistory () {
        assertTrue (click ("//a[text()='Patient Order History']"));
        pageLoading ();
        assertEquals (waitForElementVisible ("[uisref='main.patient.orders']").getText (), "Patient Order History");
    }

    public String getPatientId () {
        String patientUrl = getAttribute ("//*[contains(text(),'Patient Order History')]", "href");
        return StringUtils.substringBetween (patientUrl, "patient/", "/orders");
    }

    public void clickSave () {
        clickOrderSave ();
        hasPageLoaded ();
        pageLoading ();
    }

    public void clickOrderSave () {
        assertTrue (click ("#order-entry-save"));
    }

    public List <String> getRequiredFieldMsgs () {
        return isElementVisible (requiredMsg) ? getTextList (requiredMsg) : new ArrayList <> ();
    }

    public abstract void clickSaveAndActivate ();

    public void clickCancel () {
        assertTrue (click ("[ng-click='ctrl.cancel();']"));
        moduleLoading ();
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
        checkOrderForErrors ();
        assertTrue (isTextInElement ("[ng-bind='ctrl.orderEntry.order.status']", "Cancelled"));
    }

    protected void checkOrderForErrors () {
        if (isElementVisible (toastContainer)) {
            WebElement toastEle = findElement (toastContainer);
            if (isElementPresent (toastEle, toastError)) {
                fail (getText (toastEle, join (" ", toastError, toastMessage)));
            }
        }
    }

    public void closeToast () {
        assertTrue (click (toastContainer));
        assertTrue (waitForElementInvisible (toastContainer));
    }

    public String getToastError () {
        String message = getText (toastError);
        assertTrue (waitForElementInvisible (toastContainer));
        return message;
    }

    public String getToastSuccess () {
        String message = getText (toastSuccess);
        assertTrue (waitForElementInvisible (toastContainer));
        return message;
    }

    public void clickSeeOriginal () {
        assertTrue (click ("[ng-if='ctrl.orderEntry.order.parentReference.sourceOrderId'] .data-value-link"));
        pageLoading ();
    }

    protected String getOrderType () {
        return isElementPresent (orderType) ? getText (orderType) : null;
    }

    public String getOrderNumber () {
        return getText (orderNumber);
    }

    public void enterOrderNotes (String notes) {
        assertTrue (setText (orderNotes, notes));
    }

    public String getOrderNotes () {
        return readInput (orderNotes);
    }

    public void enterInstruction (String instruction) {
        assertTrue (setText (instructions, instruction));
    }

    protected String getInstructions () {
        return isElementVisible (instructions) ? readInput (instructions) : null;
    }

    public void clickPickPhysician () {
        assertTrue (click ("//button[text()='Pick Physician...']"));
        assertTrue (isTextInElement (popupTitle, "Pick Physician"));
    }

    public void enterPhysicianFirstname (String firstName) {
        assertTrue (setText ("#provider-firstname", firstName));
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
        return getText ("//*[@formcontrolname='providerForm']//label[text()='Name']/parent::div//span");
    }

    public String getProviderAccount () {
        return getText ("//*[@formcontrolname='providerForm']//label[text()='Account']/parent::div//span");
    }

    public Boolean getProviderMedicareEnrolled () {
        String medicareEnrolled = getText ("//*[@formcontrolname='providerForm']//label[text()='Medicare Enrolled']/parent::div//span");
        return isNotBlank (medicareEnrolled) && medicareEnrolled.equals ("Yes") ? true : false;
    }

    public void clickPickPatient () {
        assertTrue (click ("//button[text()='Pick Patient...']"));
        assertTrue (isTextInElement (popupTitle, "Pick Patient"));
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

    public void clickPatientCode () {
        String css = "//*[text()='Patient Code']/parent::div//a";
        assertTrue (click (css));
        assertTrue (waitForChildWindows (2));
        navigateToTab (1);
    }

    public Integer getPatientCode () {
        String xpath = "//*[text()='Patient Code']/..//a[1]/span";
        return isElementVisible (xpath) ? Integer.valueOf (getText (xpath)) : null;
    }

    public Integer getBillingPatientCode () {
        String xpath = "//*[text()='Billing Patient Code']/../div[1]";
        return isElementVisible (xpath) ? Integer.valueOf (getText (xpath)) : null;
    }

    public String getPatientMRDStatusCode () {
        String xpath = "//*[text()='Patient MRD Status']/..//span";
        return getText (xpath);
    }

    public String getPatientMRN () {
        return isElementVisible (patientMrn) ? readInput (patientMrn) : null;
    }

    public void enterPatientNotes (String notes) {
        assertTrue (setText ("[ng-model='ctrl.orderEntry.order.patient.notes']", notes));
    }

    public String getPatientNotes () {
        return isElementPresent (patientNotes) ? readInput (patientNotes) : null;
    }

    public void enterPatientICD_Codes (String... codes) {
        String dropdown = ".icd-code-list-item .dropdown-item";
        String css = "//button[text()='Add Code']";
        for (String code : codes) {
            if (isElementVisible (css))
                assertTrue (click (css));

            assertTrue (setText ("//*[*[text()='ICD Codes']]//input", code));
            assertTrue (waitUntilVisible (dropdown));
            assertTrue (click ("//*[contains(text(),'" + code + "')]"));
            assertTrue (waitForElementInvisible (dropdown));
        }
    }

    public List <String> getPatientICD_Codes () {
        String css = "[formcontrolname='mrnIcdForm'] .row";
        return isElementVisible (css) ? getTextList (css) : null;
    }

    public void deleteICDCode (String code) {
        assertTrue (click ("//*[contains(text(),'" + code + "')]/following-sibling::div//a"));
    }

    public String getSpecimenId () {
        return isElementVisible (specimenNumber) ? getText (specimenNumber) : null;
    }

    public SpecimenType getSpecimenType () {
        return isElementVisible (specimenType) ? SpecimenType.getSpecimenType (getFirstSelectedText (specimenType)) : null;
    }

    public SpecimenSource getSpecimenSource () {
        return isElementVisible (specimenSource) ? SpecimenSource.valueOf (readInput (specimenSource)) : null;
    }

    public Anticoagulant getAnticoagulant () {
        return isElementVisible (anticoagulant) ? Anticoagulant.valueOf (getFirstSelectedText (anticoagulant)) : null;
    }

    protected String getReconciliationDate () {
        String rDate = "[ng-bind*='ctrl.orderEntry.specimen.reconciliationDate']";
        return isElementVisible (rDate) ? getText (rDate) : null;
    }

    public LocalDateTime getShipmentArrivalDate () {
        String shipmentArrival = "//*[*[text()='Shipment Arrival']]//span";
        return isElementVisible (shipmentArrival) ? LocalDateTime.parse (getText (shipmentArrival),
                                                                         formatDt7) : null;
    }

    public String getSpecimenStabilizationWindow () {
        String stabilizationWindow = "specimen-stabilization-window";
        return isElementVisible (stabilizationWindow) ? getText (stabilizationWindow).split (":")[1] : null;
    }

    public ContainerType getSpecimenContainerType () {
        String containerType = "//*[*[text()='Specimen Container Type']]/div";
        return isElementVisible (containerType) ? getContainerType (getText (containerType)) : null;
    }

    public Integer getSpecimenContainerQuantity () {
        String quantity = "//*[*[text()='Quantity']]/div";
        return isElementVisible (quantity) ? Integer.valueOf (getText (quantity)) : null;
    }

    public List <OrderTest> getSelectedTests () {
        return allOf (Assay.class).stream ().map (a -> getTestState (a)).collect (toList ())
                                  .parallelStream ().filter (t -> t.selected).collect (toList ());
    }

    public OrderTest getTestState (Assay assay) {
        OrderTest orderTest = new OrderTest (assay);
        orderTest.selected = false;

        String labelPath = String.format ("//*[text()='%s']", assay.test);
        if (isElementPresent (labelPath))
            orderTest.selected = findElement (labelPath + "/../input").isSelected ();
        return orderTest;
    }

    public void clearCollectionDate () {
        assertTrue (clear (collectionDate));
        // after entering date, it keeps year selected, so next sendKeys enter date in year part,
        // below click will remove year selection
        assertTrue (click (collectionDateLabel));
    }

    public void enterCollectionDate (LocalDate date) {
        assertTrue (setText (collectionDate, formatDt1.format (date)));
    }

    public LocalDate getCollectionDate () {
        return isElementVisible (collectionDate) ? LocalDate.parse (readInput (collectionDate), formatDt2) : null;
    }

    public String getCollectionDateErrorMsg () {
        String locator = join (" + ", collectionDate, textDanger);
        return isElementVisible (locator) ? getText (locator) : null;
    }

    public String getPhlebotomySelection () {
        return getText ("//*[text()='Phlebotomy Selection']/../div");
    }

    public void uploadAttachments (List <String> files) {
        for (String file : files) {
            waitForElement (fileUpload).sendKeys (getSystemResource (file).getPath ());
            transactionInProgress ();
            waitForElement (fileUpload).clear ();
        }
    }

    public List <String> getHistory () {
        return getTextList ("//*[text()='History']/..//li");
    }

    public LocalDateTime getIntakeCompleteDate () {
        String css = "//*[text()='Intake Complete']/..//div";
        return isElementVisible (css) ? LocalDateTime.parse (getText (css), formatDt7) : null;
    }

    public LocalDateTime getSpecimenApprovalDate () {
        String css = "//*[text()='Specimen Approval']/..//span[2]";
        return isElementVisible (css) ? LocalDateTime.parse (getText (css), formatDt7) : null;
    }

    public SpecimenStatus getSpecimenApprovalStatus () {
        String specimenApprovalStatus = "//*[text()='Specimen Approval']/..//span[1]";
        return isElementVisible (specimenApprovalStatus) ? SpecimenStatus.valueOf (getText (specimenApprovalStatus)) : null;
    }

    public void waitForSpecimenDelivery () {
        assertTrue (waitUntil (millisDuration, millisPoll, new Function <WebDriver, Boolean> () {
            public Boolean apply (WebDriver driver) {
                return getDropdownOptions (specimenDelivery).size () > 0;
            }
        }));
    }

    public void enterSpecimenDelivery (DeliveryType type) {
        assertTrue (clickAndSelectText (specimenDelivery, type.label));
    }

    public DeliveryType getSpecimenDelivery () {
        return DeliveryType.getDeliveryType (getFirstSelectedText (specimenDelivery));
    }

    public List <String> getSpecimenDeliveryOptions () {
        return getDropdownOptions (specimenDelivery);
    }

    public void clickShipmentArrivalLink () {
        assertTrue (click (shipmentArrivalLink));
    }

    public void expandShipment () {
        if (isElementPresent ("//*[*[text()='Shipment']]//*[contains (@class, 'glyphicon-triangle-right')]"))
            assertTrue (click ("//*[text()='Shipment']"));

        if (!isElementPresent ("specimen-containers"))
            assertTrue (click ("//order-specimen-shipment//*[contains(text(),'Containers')]"));
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

    public OrderAuthorization getOrderAuthorization () {
        return isElementVisible (orderAuth) ? OrderAuthorization.getOrderAuthorization (getText (orderAuth)) : null;
    }

    private List <UploadFile> getOrderAttachments (String attachmentLoc) {
        List <UploadFile> attachments = new ArrayList <> ();
        if (isElementPresent (attachmentLoc))
            for (WebElement element : waitForElements (attachmentLoc)) {
                UploadFile attachment = new UploadFile ();
                if (isElementPresent (element, ".//div[1]//span")) {
                    attachment.fileName = getText (element, ".//div[1]//span");
                    attachment.canFilePreview = false;
                } else if (isElementPresent (element, "a[title]")) {
                    attachment.fileName = getText (element, "a[title]");
                    attachment.fileNameTitle = getAttribute (element, "a[title]", "title");
                    attachment.canFilePreview = getAttribute (element, "a[title]", "class").contains ("btn-link");
                }
                attachment.fileUrl = getAttribute (element, "a[href]", "href");
                String createdDateTime = getText (element, ".//div[3]");
                attachment.createdDateTime = LocalDateTime.parse (createdDateTime, formatDt7);
                attachment.createdBy = getText (element, ".//div[4]");
                attachments.add (attachment);
            }
        return attachments;
    }

    public List <UploadFile> getCoraAttachments () {
        return getOrderAttachments (format (attachments, "Orders"));
    }

    public List <UploadFile> getShipmentAttachments () {
        return getOrderAttachments (format (attachments, "Shipments"));
    }

    public UploadFile getDoraTrf () {
        UploadFile doraTrFile = new UploadFile ();
        String doraTrf = "//h3[contains(text(),'Dora')]/ancestor::div[@class='row']";
        if (isElementPresent (doraTrf)) {
            WebElement element = findElement (doraTrf);
            doraTrFile.fileName = getText (element, ".//*[contains(text(),'Original Dora Trf')]");
            doraTrFile.fileUrl = getAttribute (element, ".//a[contains(@href,'downloadDoraTrf')]", "href");
        }
        return doraTrFile;
    }

    public List <UploadFile> getDoraAttachments () {
        return getOrderAttachments (format (attachments, "Dora"));
    }

    public void clickFilePreviewLink (String fileName) {
        assertTrue (click (format (fileLoc, fileName)));
        assertTrue (isTextInElement (popupTitle, fileName));
    }

    public void clickFilePreviewLink (String containerName, String fileName) {
        assertTrue (click (format (fileLocInC, containerName, fileName)));
        assertTrue (isTextInElement (popupTitle, fileName));
    }
}
