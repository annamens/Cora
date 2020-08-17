package com.adaptivebiotech.cora.ui.container;

import static com.adaptivebiotech.test.utils.PageHelper.ContainerType.getContainerType;
import static java.lang.String.format;
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

    @Override
    public void isCorrectPage () {
        assertTrue (isTextInElement (".container h2", "Add Container(s)"));
    }

    public boolean isGenerateContainerLabelsVisible () {
        return isElementPresent ("[ng-click='ctrl.generateLabels()']");
    }

    public boolean isAddContainersVisible () {
        boolean visible = isElementVisible (".container h2");
        visible &= isElementVisible ("[ng-model='selectedContainerId']");
        visible &= isElementVisible ("[ng-click*='ctrl.addContainer']");
        return visible;
    }

    public void clickAdd () {
        assertTrue (click ("[ng-click*='ctrl.addContainer']"));
    }

    public void clickSave () {
        assertTrue (click ("[data-ng-click='ctrl.save()']"));
        assertTrue (isTextInElement (popupTitle, "New Container Confirmation"));
        clickPopupOK ();
        assertTrue (waitUntilVisible ("[ng-click='ctrl.generateLabels()']"));
        closeNotification ("Container(s) updated");
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

    public void clearQuantity () {
        assertTrue (clear ("#containerQty"));
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
}
