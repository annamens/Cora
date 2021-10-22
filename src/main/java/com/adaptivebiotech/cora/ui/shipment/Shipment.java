package com.adaptivebiotech.cora.ui.shipment;

import static com.adaptivebiotech.test.BaseEnvironment.coraTestUser;
import static java.lang.ClassLoader.getSystemResource;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.openqa.selenium.Keys.ENTER;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import java.util.List;
import org.openqa.selenium.WebElement;
import org.testng.util.Strings;
import com.adaptivebiotech.cora.dto.Containers;
import com.adaptivebiotech.cora.dto.Containers.Container;
import com.adaptivebiotech.cora.ui.CoraPage;
import com.adaptivebiotech.cora.utils.PageHelper.Carrier;
import com.adaptivebiotech.cora.utils.PageHelper.LinkShipment;
import com.adaptivebiotech.test.utils.PageHelper.ContainerType;
import com.adaptivebiotech.test.utils.PageHelper.ShippingCondition;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class Shipment extends CoraPage {

    private final String cssCarrier        = "#carrierType";
    private final String cssTrackingNumber = "#trackingNumber";
    private final String cssNotes          = "#shipment-notes";

    public Shipment () {
        staticNavBarHeight = 195;
    }

    public void isDiagnostic () {
        assertTrue (isTextInElement ("[role='tablist'] .active:nth-child(1)", "SHIPMENT"));
        assertTrue (waitUntilVisible ("#orderNumber"));
        assertTrue (waitUntilVisible ("#boxId"));
        assertTrue (waitUntilVisible ("#containerType"));
    }

    public void isBatchOrGeneral () {
        assertTrue (isTextInElement ("[role='tablist'] .active:nth-child(1)", "SHIPMENT"));
        assertTrue (waitUntilVisible ("#expectedRecordType"));
        assertTrue (waitUntilVisible ("#expectedRecordType"));
    }

    public void gotoAccession () {
        assertTrue (click ("#shipment-accession-tab-link"));
        pageLoading ();
    }

    public void clickSave () {
        assertTrue (click ("[data-ng-click*='shipment-save']"));
        pageLoading ();
    }

    public String getArrivalDate () {
        return getAttribute ("#arrivalDate", "value");
    }

    public String getArrivalTime () {
        return getAttribute ("#arrivalTime", "value");
    }

    public void enterShippingCondition (ShippingCondition condition) {
        assertTrue (clickAndSelectValue ("[ng-model='ctrl.entry.shipment.condition']", "string:" + condition));
    }

    public void enterOrderNumber (String orderNum) {
        assertTrue (setText ("#orderNumber", orderNum));
    }

    public void selectDiagnosticSpecimenContainerType (ContainerType type) {
        assertTrue (clickAndSelectValue ("#containerType", "string:" + type.name ()));
    }

    public void selectBatchSpecimenContainerType (ContainerType type) {
        assertTrue (click (".add-container-dropdown button"));
        assertTrue (click (format ("//*[contains(@class,'add-container-dropdown')]//*[text()='%s']", type.label)));
    }

    public void clickAddContainer () {
        assertTrue (click ("[ng-click='ctrl.addContainer()']"));
    }

    public void clickAddSlide () {
        assertTrue (click ("[ng-click='ctrl.addChildContainer(container)']"));
    }

    public void setContainerName (int idx, String containerName) {
        assertTrue (setText ("[name='trackingNumber" + (idx - 1) + "']", containerName));
    }

    public String getHeaderShipmentNum () {
        return getText (".shipment").replace ("\n", " ");
    }

    public String getShipmentNum () {
        return getText ("[ng-bind='ctrl.entry.shipment.shipmentNumber']");
    }

    public Containers getPrimaryContainers (ContainerType type) {
        String lines = "[class='container-section'] [ng-repeat='container in ctrl.entry.containers']";
        String shipmentNum = getText ("[ng-bind='ctrl.entry.shipment.shipmentNumber']");
        return new Containers (waitForElements (lines).stream ().map (el -> {
            Container c = new Container ();
            c.id = getConId (getAttribute (el, "[data-ng-bind='container.containerNumber']", "href"));
            c.containerNumber = getText (el, "[data-ng-bind='container.containerNumber']");
            c.containerType = type;
            c.location = getText (el, "[ng-bind='container.location']");
            c.name = readInput (el, "[ng-model='container.externalId']");
            c.shipmentNumber = shipmentNum;

            if (isElementPresent (el, ".container-table")) {
                String css = "[ng-repeat='child in container.children']";
                List <Container> children = el.findElements (locateBy (css)).stream ().map (childRow -> {
                    Container childContainer = new Container ();
                    childContainer.id = getConId (getAttribute (childRow,
                                                                "[data-ng-bind='child.containerNumber']",
                                                                "href"));
                    childContainer.containerNumber = getText (childRow, "[data-ng-bind='child.containerNumber']");
                    childContainer.name = getAttribute (childRow, " [name*='trackingNumber']", "value");
                    childContainer.root = c;
                    return childContainer;
                }).collect (toList ());
                c.children = children;
            }
            return c;
        }).collect (toList ()));
    }

    public Containers getBatchContainers () {
        String lines = ".shipment-details-research [data-ng-repeat='container in ctrl.entry.containers']";
        String shipmentNum = getText ("div[data-ng-bind='ctrl.entry.shipment.shipmentNumber']");
        return new Containers (waitForElements (lines).stream ().map (el -> {
            Container container = new Container ();
            container.id = getConId (getAttribute (el, "[data-ng-bind='container.containerNumber']", "href"));
            container.containerNumber = getText (el, "[data-ng-bind='container.containerNumber']");
            container.containerType = ContainerType.getContainerType (getText (el, ".container-type-header"));
            container.name = getText (el, "[data-ng-bind='container.displayName']");
            container.shipmentNumber = shipmentNum;

            if (isElementPresent (el, ".container-specimen-summary-table")) {
                String css = ".middle-container [data-ng-repeat='specimen in container.specimens']";
                List <Container> children = el.findElements (locateBy (css)).stream ().map (el1 -> {
                    Container c = new Container ();
                    c.id = getConId (getAttribute (el1, "[data-ng-bind*='containerNumber']", "href"));
                    c.containerNumber = getText (el1, "td:nth-child(1)");
                    c.name = getText (el1, "[data-ng-bind*='specimen.customerId']");
                    c.barcode = getText (el1, "[data-ng-bind*='specimen.barcode']");
                    c.specimenId = getText (el1, "[data-ng-bind*='specimen.specimen.specimenNumber']");
                    c.specimenName = getText (el1, "[data-ng-bind*='specimen.specimen.name']");
                    if (Strings.isNotNullAndNotEmpty (c.specimenName)) {
                        c.containerType = ContainerType.getContainerType (c.specimenName.split ("-")[0]);
                    }
                    c.root = container;
                    c.location = String.join (" : ", coraTestUser, container.containerNumber);
                    return c;
                }).collect (toList ());
                css = "[data-ng-bind*='specimen.location']";
                List <String> locs = el.findElements (locateBy (css)).stream ().map (el1 -> {
                    String loc = getText (el1);
                    if (loc != null) {
                        return loc.length () > 0 ? " : Position " + loc : loc;
                    } else {
                        return "";
                    }
                }).collect (toList ());
                for (int i = 0; i < children.size (); ++i)
                    children.get (i).location += locs.get (i);
                container.children = children;
            }
            return container;
        }).collect (toList ()));
    }

    public void linkShipmentTo (LinkShipment link, String value) {
        assertTrue (clickAndSelectValue ("#expectedRecordType", link.name ()));
        String css = null;
        switch (link) {
        case Account:
            css = "[data-ng-model='ctrl.entry.account']";
            break;
        case SalesforceOrder:
            css = "[data-ng-model='ctrl.entry.sfdcOrder']";
            break;
        case Project:
            css = "[data-ng-model='ctrl.entry.project']";
            break;
        }
        assertTrue (waitUntilVisible (css));
        assertTrue (setText (css + " input", value));
        assertTrue (pressKey (ENTER));

        assertTrue (waitUntilVisible (css + " .matches"));
        waitForElements (css + " li a div:nth-child(1)").forEach (el -> {
            if (value.equals (getText (el)))
                assertTrue (click (el));
        });
        assertTrue (waitUntilVisible (format (".expected-record-summary[data-ng-show*='%s']", link)));
    }

    public void uploadAttachments (String... files) {
        String attachments = asList (files).parallelStream ()
                                           .map (f -> getSystemResource (f).getPath ())
                                           .collect (joining ("\n"));
        waitForElement ("input[ngf-select*='ctrl.onUpload']").sendKeys (attachments);
        pageLoading ();
    }

    public void doubleClickSave () {
        WebElement saveButton = waitForElement (locateBy ("[data-ng-click*='shipment-save']"));
        saveButton.click ();
        saveButton.click ();
        pageLoading ();
    }

    public void enterCarrier (Carrier coraCarrier) {
        assertTrue (clickAndSelectValue (cssCarrier, "string:" + coraCarrier.text));
    }

    public Carrier getCarrier () {
        String carrierText = getFirstSelectedText (cssCarrier);
        return Carrier.valueOf (carrierText);
    }

    public void enterTrackingNumber (String trackingNumber) {
        assertTrue (setText (cssTrackingNumber, trackingNumber));
    }

    public String getTrackingNumber () {
        return readInput (cssTrackingNumber);
    }

    public void clickAddShipmentDiscrepancy () {
        String cssShipmentDiscrepancy = "[title='Add Shipment Discrepancy']";
        assertTrue (click (cssShipmentDiscrepancy));
        pageLoading ();
        String expectedTitle = "Discrepancy";
        String title = waitForElementVisible (popupTitle).getText ();
        assertEquals (title, expectedTitle);
    }

    public void enterNotes (String notes) {
        assertTrue (setText (cssNotes, notes));
    }

    public String getNotes () {
        return readInput (cssNotes);
    }

    public void enterInitialStorageLocation (String freezerName) {
        String cssInitialStorageLocation = "[ng-model='ctrl.storageLocation']";
        assertTrue (clickAndSelectText (cssInitialStorageLocation, freezerName));
    }

    public boolean discrepancyResolutionsTabVisible () {
        String cssDRT = "#shipment-discrepancy-tab-link";
        String expectedText = "DISCREPANCY RESOLUTIONS";
        String text = waitForElementVisible (cssDRT).getText ();
        return expectedText.equals (text);
    }

    public String getShippingCondition () {
        String css = "[ng-model='ctrl.entry.shipment.condition']";
        return getFirstSelectedText (css);
    }

    public String getOrderNumber () {
        return readInput ("#orderNumber");
    }

    public ContainerType getContainerType () {
        String text = getFirstSelectedText ("#containerType");
        return ContainerType.getContainerType (text);
    }

    public String getInitialStorageLocation () {
        String cssInitialStorageLocation = "[ng-model='ctrl.storageLocation']";
        return getFirstSelectedText (cssInitialStorageLocation);
    }

    public List <String> getAttachmentNames () {
        return getTextList ("[ng-bind='attachment.name']");
    }

    public void clickGenerateContainerLabels () {
        String css = "[ng-click='ctrl.generateLabels()']";
        assertTrue (click (css));
        pageLoading ();
    }

    public String getShipmentStatus () {
        String css = "[ng-bind=\"ctrl.entry | shipmentEntryStatus\"]";
        String status = getText (css);
        return status;
    }

    public void gotoDiscrepancyResolutions () {
        String css = "#shipment-discrepancy-tab-link";
        assertTrue (click (css));
    }

    public void clickDiscontinueShipment () {
        String button = "//button[contains(., 'Discontinue Shipment')]";
        assertTrue (click (button));
        assertTrue (waitUntilVisible (".modal-dialog"));
        assertTrue (click ("button[ng-click='ctrl.save()']"));
        moduleLoading ();
        String status = "div[ng-bind='ctrl.entry | shipmentEntryStatus']";
        assertTrue (isTextInElement (status, "Discontinued"));
    }

    public void clickContainerNo (String containerNo) {
        assertTrue (click ("//table//a[text()='" + containerNo + "']"));
    }

}
