package com.adaptivebiotech.cora.ui.container;

import static com.adaptivebiotech.test.utils.PageHelper.ContainerType.getContainerType;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertTrue;
import java.util.List;
import org.openqa.selenium.Keys;
import com.adaptivebiotech.cora.dto.Containers;
import com.adaptivebiotech.cora.dto.Containers.Container;
import com.adaptivebiotech.test.utils.PageHelper.ContainerType;
import com.adaptivebiotech.ui.cora.CoraPage;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class AddContainer extends CoraPage {

    private final String lines = "[data-ng-repeat='container in ctrl.containers']";

    public AddContainer () {
        staticNavBarHeight = 90;
    }

    public void clickAdd () {
        assertTrue (click ("[ng-click*='ctrl.addContainer']"));
    }

    public void clickSave () {
        assertTrue (click ("[data-ng-click='ctrl.save()']"));
        assertTrue (isTextInElement (popupTitle, "New Container Confirmation"));
        clickPopupOK ();
        closeNotification ("Container(s) saved");
        assertTrue (waitUntilVisible ("[data-ng-click='ctrl.generateLabels()']"));
    }

    public void isFailedValidation (String error) {
        assertTrue (isTextInElement ("[ng-show='ctrl.inputError']", error));
    }

    public String getContainerMoveError () {
        return getText ("[ng-bind-html='ctrl.scanResult.msg']");
    }

    public void pickContainerType (ContainerType type) {
        assertTrue (clickAndSelectValue ("[ng-model='selectedContainerId']", "string:" + type));
    }

    public void enterQuantity (int num) {
        assertTrue (setText ("#containerQty", String.valueOf (num)));
    }

    public void addContainer (ContainerType type, int num) {
        pickContainerType (type);
        enterQuantity (num);
        assertTrue (pressKey (Keys.ENTER));
    }

    public void setContainerName (int idx, String name) {
        String row = ".research-container-entry:nth-child(" + (idx + 2) + ") [data-ng-model='container.barcode']";
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
        waitForElements (lines).forEach (el -> assertTrue (click (el, "[data-ng-click*='ctrl.removeContainer']")));
        assertTrue (click ("[data-ng-click='ctrl.save()']"));
        closeNotification ("Container(s) updated");
    }

    public Containers getContainers () {
        return new Containers (waitForElements (lines).stream ().map (el -> {
            Container c = new Container ();
            c.id = getConId (getAttribute (el, "[data-ng-bind*='containerNumber']", "href"));
            c.containerNumber = getText (el, "td:nth-child(1)");
            c.containerType = getContainerType (getText (el, ".container-type"));
            c.name = readInput (el, "[data-ng-model='container.barcode']");
            c.location = getText (el, "[data-ng-bind='container.location']");
            return c;
        }).collect (toList ()));
    }
}
