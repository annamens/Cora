package com.adaptivebiotech.cora.ui.shipment;

import com.adaptivebiotech.cora.dto.Containers;
import com.adaptivebiotech.cora.dto.Containers.Container;
import com.adaptivebiotech.test.utils.PageHelper.ContainerType;
import com.adaptivebiotech.test.utils.PageHelper.ShippingCondition;
import com.adaptivebiotech.ui.cora.CoraPage;
import org.openqa.selenium.WebElement;

import java.util.List;

import static com.adaptivebiotech.test.BaseEnvironment.coraTestUser;
import static com.adaptivebiotech.test.utils.PageHelper.ContainerType.getContainerType;
import static java.lang.ClassLoader.getSystemResource;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertTrue;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class Shipment extends CoraPage {

    public Shipment () {
        staticNavBarHeight = 195;
    }

    @Override
    public void isCorrectPage () {
        assertTrue (waitUntilVisible (".navbar"));
        assertTrue (waitUntilVisible ("[role='tablist']"));
        assertTrue (isTextInElement ("[role='tablist'] .active:nth-child(1)", "SHIPMENT"));
    }

    public void gotoAccession () {
        assertTrue (click ("[role='presentation'] [data-ng-click*='accession']"));
    }

    public void clickSave () {
        assertTrue (click ("[data-ng-click*='shipment-save']"));
        pageLoading ();
        closeNotification ("Shipment saved");
    }

    public void enterShippingCondition (ShippingCondition condition) {
        assertTrue (clickAndSelectValue ("[ng-model='ctrl.entry.shipment.condition']", "string:" + condition));
    }

    public void enterOrderNumber (String orderNum) {
        assertTrue (setText ("#orderNumber", orderNum));
    }

    public void enterDiagnosticSpecimenContainerType (ContainerType type) {
        assertTrue (clickAndSelectValue ("#containerType", "string:" + type.name ()));
    }

    public void enterBatchSpecimenContainerType (ContainerType type) {
        assertTrue (click (".add-container-dropdown button"));
        assertTrue (click ("[data-ng-bind*='\\'" + type + "\\'']"));
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

    public Containers getPrimaryContainers (ContainerType type) {
        String lines = ".container-table [ng-repeat='container in ctrl.entry.containers']";
        String shipmentNum = getText ("[ng-bind='ctrl.entry.shipment.shipmentNumber']");
        return new Containers (waitForElements (lines).stream ().map (el -> {
            Container c = new Container ();
            c.id = getConId (getAttribute (el, "[data-ng-bind='container.containerNumber']", "href"));
            c.containerNumber = getText (el, "[data-ng-bind='container.containerNumber']");
            c.containerType = type;
            c.location = getText (el, "[ng-bind='container.location']");
            c.name = readInput (el, "[ng-model='container.externalId']");
            c.shipmentNumber = shipmentNum;
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
            container.containerType = getContainerType (getText (el, ".container-type-header"));
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
                    if (c.specimenName != null) {
                        c.containerType = getContainerType (c.specimenName.split("-")[0]);
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

    public void uploadAttachments (String... files) {
        String attachments = asList (files).parallelStream ()
                                           .map (f -> getSystemResource (f).getPath ())
                                           .collect (joining ("\n"));
        waitForElement ("input[ngf-select*='ctrl.onUpload']").sendKeys (attachments);
        pageLoading ();
    }

    public void doubleClickSave () {
        WebElement saveButton =  waitForElement(locateBy("[data-ng-click*='shipment-save']"));
        saveButton.click();
        saveButton.click();
        pageLoading ();
    }
}
