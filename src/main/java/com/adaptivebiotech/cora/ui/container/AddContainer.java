/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.ui.container;

import static com.adaptivebiotech.cora.dto.Containers.ContainerType.getContainerType;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertTrue;
import java.util.List;
import org.openqa.selenium.Keys;
import com.adaptivebiotech.cora.dto.Containers;
import com.adaptivebiotech.cora.dto.Containers.Container;
import com.adaptivebiotech.cora.dto.Containers.ContainerType;
import com.adaptivebiotech.cora.ui.CoraPage;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class AddContainer extends CoraPage {

    private final String lines              = "[data-ng-repeat='container in ctrl.containers']";
    private final String addContainerHeader = ".container h2";
    private final String containerType      = "[ng-model='selectedContainerId']";
    private final String quantity           = "#containerQty";
    private final String add                = "[ng-click*='ctrl.addContainer']";
    private final String notificationMsg    = "[ng-bind-html='notification.msg']";
    private final String generateLabels     = "[ng-click='ctrl.generateLabels()']";

    public AddContainer () {
        staticNavBarHeight = 90;
    }

    @Override
    public void isCorrectPage () {
        assertTrue (isTextInElement (".container h2", "Add Container(s)"));
    }

    public boolean isGenerateContainerLabelsVisible () {
        return isElementPresent (generateLabels);
    }

    public void clickGenerateContainerLabels () {
        assertTrue (click (generateLabels));
    }

    public boolean isAddContainerHeaderVisible () {
        return isElementVisible (addContainerHeader);
    }

    public void clickAdd () {
        assertTrue (click (add));
    }

    public void clickSave () {
        assertTrue (click ("[data-ng-click='ctrl.save()']"));
        assertTrue (isTextInElement (popupTitle, "New Container Confirmation"));
        clickPopupOK ();
    }

    public void isFailedValidation (String error) {
        assertTrue (isTextInElement ("[ng-show='ctrl.inputError']", error));
    }

    public String getContainerMoveError () {
        return getText ("[ng-bind-html='ctrl.scanResult.msg']");
    }

    public boolean isContainerTypeVisible () {
        return isElementVisible (containerType);
    }

    public void pickContainerType (ContainerType type) {
        assertTrue (clickAndSelectValue (containerType, "string:" + type));
    }

    public boolean isQuantityVisible () {
        return isElementVisible (quantity);
    }

    public void enterQuantity (int num) {
        assertTrue (setText (quantity, String.valueOf (num)));
    }

    public void clearQuantity () {
        assertTrue (clear (quantity));
    }

    public boolean isAddBtnVisible () {
        return isElementVisible (add);
    }

    public void addContainer (ContainerType type, int num) {
        pickContainerType (type);
        clearQuantity ();
        enterQuantity (num);
        assertTrue (pressKey (Keys.ENTER));
    }

    public void setContainerName (int idx, String name) {
        String row = format (".research-container-entry:nth-child(%s) [data-ng-model='container.barcode']", idx + 2);
        assertTrue (clear (row));
        assertTrue (setText (row, name));
    }

    public void setContainerLocation (int idx, String freezer) {
        assertTrue (clickAndSelectText (waitForElements (lines).get (idx - 1), "select", freezer));
    }

    public List <String> getNameValErrors () {
        return getTextList ("div[data-ng-bind*='ctrl.errors']");
    }

    public List <String> getLocationValErrors () {
        return getTextList ("[data-ng-bind='ctrl.errors[container.id]']");
    }

    public void setContainersLocation (String freezer) {
        waitForElements (lines).forEach (el -> assertTrue (clickAndSelectText (el, "select", freezer)));
    }

    public void removeContainers () {
        assertTrue (click ("[data-ng-click='ctrl.save()']"));
    }

    public Containers getContainers () {
        return new Containers (waitForElements (lines).stream ().map (el -> {
            Container c = new Container ();
            String href = getAttribute (el, "[data-ng-bind*='containerNumber']", "href");
            c.id = href != null ? getConId (href) : null;
            c.containerNumber = getText (el, "td:nth-child(1)");
            c.containerType = getContainerType (getText (el, ".container-type"));
            c.name = readInput (el, "[data-ng-model='container.barcode']");
            c.location = getText (el, "[data-ng-bind='container.location']");
            return c;
        }).collect (toList ()));
    }

    public String getContainerSavedMsg () {
        return getText (notificationMsg);
    }

    public void clickContainerNo (String containerNo) {
        assertTrue (click (format ("//*[text()='%s']", containerNo)));
    }

}
