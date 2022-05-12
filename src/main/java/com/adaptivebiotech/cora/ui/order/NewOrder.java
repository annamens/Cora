/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.ui.order;

import static com.adaptivebiotech.cora.dto.Containers.ContainerType.getContainerType;
import static java.lang.ClassLoader.getSystemResource;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Arrays.asList;
import static java.util.EnumSet.allOf;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
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
import com.adaptivebiotech.cora.dto.Orders.OrderTest;
import com.adaptivebiotech.cora.dto.Physician;
import com.adaptivebiotech.cora.dto.Specimen.Anticoagulant;
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

    private final String   orderNumber      = "//*[@label='Order #']//span";
    private final String   patientMrdStatus = ".patient-status";
    private final String   specimenDelivery = "[formcontrolname='specimenDeliveryType']";
    private final String   orderNotes       = "#order-notes";
    protected final String specimenNumber   = "//*[text()='Adaptive Specimen ID']/..//div";
    protected final String toastContainer   = "#toast-container";
    protected final String toastError       = ".toast-error";
    protected final String toastSuccess     = ".toast-error";
    protected final String toastMessage     = ".toast-message";

    public NewOrder () {
        staticNavBarHeight = 200;
    }

    @Override
    public void isCorrectPage () {
        assertTrue (isTextInElement ("[role='tablist'] .active a", "ORDER DETAILS"));
        pageLoading ();
    }

    public List <String> getSectionHeaders () {
        return getTextList (".order-entry h2");
    }

    public abstract void activateOrder ();

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
        assertTrue (click ("#order-entry-save"));
        hasPageLoaded ();
        pageLoading ();
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
        checkOrderActivateCancelError ();
        assertTrue (isTextInElement ("[ng-bind='ctrl.orderEntry.order.status']", "Cancelled"));
    }

    protected WebElement checkOrderActivateCancelError () {
        WebElement toastEle = waitForElementVisible (toastContainer);
        if (isElementPresent (toastEle, toastError)) {
            fail (getText (toastEle, join (" ", toastError, toastMessage)));
        }
        return toastEle;
    }

    public void clickSeeOriginal () {
        assertTrue (click ("[ng-if='ctrl.orderEntry.order.parentReference.sourceOrderId'] .data-value-link"));
        pageLoading ();
    }

    protected String getOrderType () {
        String css = "[ng-bind='ctrl.orderEntry.order.category.name']";
        return isElementPresent (css) ? getText (css) : null;
    }

    public String getOrderNumber () {
        return getText (orderNumber);
    }

    public void enterDateSigned (String date) {
        assertTrue (setText ("[ng-model='ctrl.orderEntry.order.dateSigned']", date));
    }

    public String getDateSigned () {
        String css = "[ng-model^='ctrl.orderEntry.order.dateSigned']";
        return isElementVisible (css) ? readInput (css) : null;
    }

    public void enterOrderNotes (String notes) {
        assertTrue (setText (orderNotes, notes));
    }

    public String getOrderNotes () {
        return readInput (orderNotes);
    }

    public void enterInstruction (String instruction) {
        assertTrue (setText ("[ng-model='ctrl.orderEntry.order.specialInstructions']", instruction));
    }

    protected String getInstructions () {
        String css = "[ng-model='ctrl.orderEntry.order.specialInstructions']";
        return isElementVisible (css) ? readInput (css) : null;
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

    public String getPatientCode () {
        String xpath = "//*[text()='Patient Code']/..//a[1]/span";
        return getText (xpath);
    }

    public String getPatientMRN () {
        String css = "[ng-model='ctrl.orderEntry.order.mrn']";
        return isElementVisible (css) ? readInput (css) : null;
    }

    public void enterPatientNotes (String notes) {
        assertTrue (setText ("[ng-model='ctrl.orderEntry.order.patient.notes']", notes));
    }

    public String getPatientNotes () {
        String css = "[ng-model='ctrl.orderEntry.order.patient.notes']";
        return isElementPresent (css) ? readInput (css) : null;
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
        String searchBox = "[ng-show='ctrl.searchBoxVisible'] input";
        String css = "[ng-model*='ctrl.orderEntry.icdCodes'] span";
        return isElementVisible (searchBox) ? null : isElementPresent (css) ? getTextList (css) : null;
    }

    public String getSpecimenId () {
        return isElementVisible (specimenNumber) ? getText (specimenNumber) : null;
    }

    public SpecimenType getSpecimenType () {
        String css = "[ng-model^='ctrl.orderEntry.specimen.sampleType']";
        return isElementVisible (css) ? SpecimenType.getSpecimenType (getFirstSelectedText (css)) : null;
    }

    public SpecimenSource getSpecimenSource () {
        String css = "[ng-model^='ctrl.orderEntry.specimen.sourceType']";
        return isElementVisible (css) ? SpecimenSource.getSpecimenSource (getFirstSelectedText (css)) : null;
    }

    public Anticoagulant getAnticoagulant () {
        String css = "[ng-model^='ctrl.orderEntry.specimen | specimenAnticoagulant']";
        return isElementVisible (css) ? Anticoagulant.valueOf (getFirstSelectedText (css)) : null;
    }

    protected String getReconciliationDate () {
        String rDate = "[ng-bind*='ctrl.orderEntry.specimen.reconciliationDate']";
        return isElementVisible (rDate) ? getText (rDate) : null;
    }

    public String getShipmentArrivalDate () {
        String shipmentArrival = "//*[*[text()='Shipment Arrival']]//span";
        return isElementVisible (shipmentArrival) ? getText (shipmentArrival) : null;
    }

    public ContainerType getSpecimenContainerType () {
        return getContainerType (getText ("//*[*[text()='Specimen Container Type']]/div"));
    }

    public String getSpecimenContainerQuantity () {
        return getText ("//*[*[text()='Quantity']]/div");
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

    public void enterCollectionDate (String date) {
        assertTrue (setText ("//*[text()='Collection Date']/..//input", date));
    }

    public String getPhlebotomySelection () {
        return getText ("//*[text()='Phlebotomy Selection']/../div");
    }

    public void uploadAttachments (String... files) {
        String attachments = asList (files).parallelStream ().map (f -> getSystemResource (f).getPath ())
                                           .collect (joining ("\n"));
        waitForElement ("input[ngf-select*='ctrl.onUpload']").sendKeys (attachments);
        pageLoading ();
    }

    public List <String> getHistory () {
        return getTextList ("//*[text()='History']/..//li");
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

    public void expandShipment () {
        assertTrue (click ("//*[text()='Shipment']"));
    }

    public void expandContainers () {
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
}
