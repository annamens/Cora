package com.adaptivebiotech.cora.ui.container;

import static com.adaptivebiotech.test.BaseEnvironment.coraTestUser;
import static com.adaptivebiotech.test.utils.PageHelper.ContainerType.Plate;
import static com.adaptivebiotech.test.utils.PageHelper.ContainerType.getContainerType;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertTrue;
import org.openqa.selenium.Keys;
import com.adaptivebiotech.cora.dto.Containers;
import com.adaptivebiotech.cora.dto.Containers.Container;
import com.adaptivebiotech.ui.cora.CoraPage;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class ContainerList extends CoraPage {

    private final String depleted   = ".select-depletion";
    private final String comments   = "[ng-model='ctrl.comments']";
    private final String pass       = ".alert-success";
    private final String fail       = ".alert-danger";
    private final String holdingBtn = "[ng-click*='holdingContainer']";
    private final String moveBtn    = "[ng-click='ctrl.moveHere()']";

    public ContainerList () {
        staticNavBarHeight = 90;
    }

    public void setCurrentLocationFilter (String freezer) {
        assertTrue (click (".filters-sm:nth-child(2) .dropdown-toggle"));
        assertTrue (click ("//a[text()='" + freezer + "']"));
    }

    public Containers getContainers () {
        return new Containers (waitForElements (".containers-list tr.ng-scope").stream ().map (el -> {
            Container c = new Container ();
            c.id = getConId (getAttribute (el, "[ng-bind*='containerNumber']", "href"));
            c.containerNumber = getText (el, "[ng-bind*='containerNumber']");
            c.containerType = getContainerType (getText (el, "[ng-bind*='containerTypeDisplayName']"));
            c.contents = getText (el, "[ng-bind*='containerContentSummary']");
            c.location = getText (el, "[ng-bind*='container.location']");
            c.name = getText (el, "[ng-bind*='container.displayName']");
            c.arrivalDate = getText (el, "[ng-bind*='arrivalDate']");
            c.orderId = getText (el, "[ng-bind*='shipmentDetail.recordType.name']");
            return c;
        }).collect (toList ()));
    }

    public void scan (Container container) {
        scan (container.containerNumber);
    }

    public void scan (String containerNumber) {
        assertTrue (setText ("#container-scan-input", containerNumber));
        assertTrue (pressKey (Keys.ENTER));
    }

    public String getScanError () {
        return getText (".scan-msg.text-danger");
    }

    public String getScanError2 () {
        return getText (fail);
    }

    public void clickGoBack () {
        assertTrue (click ("[ng-click='ctrl.goBack()']"));
        moveModal ();
    }

    public void takeCustody (Container container) {
        String msg = format ("%s %s is in your custody", container.containerType.label, container.containerNumber);
        scan (container);
        assertTrue (isTextInElement ("[ng-bind-html='ctrl.scanResult.msg']", msg));
    }

    public void moveToFreezer (Container container, Container freezer) {
        scanAndclickFreezer (container);
        selectFreezer (container, freezer, null);
    }

    public void scanAndclickFreezer (Container container) {
        scan (container);
        moveModal ();
        setDepletion (container);
        if (container.comment != null)
            assertTrue (setText (comments, container.comment));
        clickFreezer ();
    }

    public void clickFreezer () {
        assertTrue (click ("[ng-click*='freezer']"));
    }

    public void selectFreezer (Container container, Container freezer, String comment) {
        String success = "Moved container " + container.containerNumber + " to ";
        if (comment != null)
            assertTrue (setText (comments, comment));

        selectFreezerTest (freezer);
        assertTrue (isTextInElement (pass, success + freezer.name));
        assertTrue (noSuchElementPresent (depleted));
        assertTrue (noSuchElementPresent (comments));

        String regex = "(.*)(Position.*$)";
        String loc = getText (pass).replace (success, "");
        container.location = loc;
        container.root = null;
        if (container.containerType.isHolding && !container.containerType.equals (Plate))
            container.children.forEach (c -> {
                c.location = String.join (" : ", loc, c.containerType.label, c.location.replaceFirst (regex, "$2"));
            });

        clickClose ();
        pageLoading ();
    }

    public void selectFreezerTest (Container freezer) {
        assertTrue (click ("//span[text()='" + freezer.name + "']"));
        clickMove ();
    }

    public String getFreezerError () {
        return getText (fail);
    }

    public String getFreezerSuccess () {
        return getText (pass);
    }

    public void isHoldingContainer (Container target) {
        scan (target);
        moveModal ();
        assertTrue (noSuchElementPresent ("div[ng-if*='ctrl.enableDepletion']"));
        assertTrue (noSuchElementPresent ("[containers='[ctrl.containerDetail]'] " + depleted));
        assertTrue (noSuchElementPresent (holdingBtn));
        assertTrue (noSuchElementPresent (moveBtn));
        closePopup ();
    }

    public void setHoldingContainer (Container child, Container holding) {
        scanAndClickHoldingContainer (child);
        finalizeHoldingContainer (child, holding);
    }

    public void setHoldingContainerTest (Container child, Container holding) {
        scanAndClickHoldingContainer (child);
        chooseHoldingContainer (holding);
        assertTrue (waitUntilVisible ("[containers='[ctrl.holdingContainer]']"));
        clickMove ();
    }

    public void scanAndClickHoldingContainer (Container child) {
        scan (child);
        moveModal ();
        setDepletion (child);
        if (child.comment != null)
            assertTrue (setText (comments, child.comment));
        assertTrue (click (holdingBtn));
        assertTrue (isTextInElement (".container h5", "Choose Holding Container"));
    }

    public void finalizeHoldingContainer (Container child, Container holding) {
        String root = child.root != null ? " from " + child.root.containerNumber : "";
        chooseHoldingContainer (holding);
        if (holding.containerType.isHolding) {
            assertTrue (waitUntilVisible ("[containers='[ctrl.holdingContainer]']"));
            clickMove ();

            if (holding.containerType.type.equals (child.containerType.type)) {
                String success = "Moved container " + child.containerNumber + root + " to " + holding.containerNumber;
                assertTrue (isTextInElement (pass, success));
                assertTrue (noSuchElementPresent (depleted));
                assertTrue (noSuchElementPresent (comments));
                child.location = String.join (" : ", coraTestUser, holding.containerNumber);
                String location = getText (pass).replace (success, "").replace (" at ", "Position ");
                if (location.length () > 0)
                    child.location += " : " + location;
                child.root = holding;
                holding.children.add (child);
            } else {
                String tType = child.containerType.typeError;
                String hType = holding.containerType.typeError;
                String err = "Cannot add container type %s to destination container type %s.";
                assertTrue (isTextInElement (fail, format (err, tType, hType)));
            }
        } else {
            String err = "Container " + holding.containerNumber + " is not a holding container. Choose another container.";
            assertTrue (isTextInElement (".text-danger", err));
        }
        closePopup ();
    }

    public void chooseHoldingContainer (Container holding) {
        chooseHoldingContainer (holding.containerNumber);
    }

    public void chooseHoldingContainer (String containerNumber) {
        assertTrue (setText (".modal-body [ng-model='ctrl.containerNumber']", containerNumber));
        assertTrue (pressKey (Keys.ENTER));
    }

    public void removeFromHoldingContainer (Container child, Container holding) {
        String success = "Removed " + child.containerNumber + " from " + holding.containerNumber;
        scanAndClickHoldingContainer (child);
        assertTrue (click ("[ng-click*='removeFromHoldingContainer']"));
        assertTrue (isTextInElement (pass, success));
        assertTrue (noSuchElementPresent (depleted));
        assertTrue (noSuchElementPresent (comments));
        clickClose ();
        child.location = coraTestUser;
    }

    public String getScanVerifyError () {
        return getText (".container-move-verify-error");
    }

    public String getScanVerifyText () {
        return getText (".verify-scan-text");
    }

    public void scanToVerify (Container parent, Container child) {
        scan (parent);
        moveModal ();
        assertTrue (setText (comments, "verifying - " + child.containerNumber));
        scanToVerify (child);
        isVerified (child);
        clickClose ();
    }

    public void scanToVerify (Container child) {
        scanToVerify (child.containerNumber);
    }

    public void scanToVerify (String containerNumber) {
        assertTrue (setText ("#scan_input", containerNumber));
        assertTrue (pressKey (Keys.ENTER));
    }

    public void isVerified (Container child) {
        assertTrue (waitUntilVisible ("#containerNumber_" + child.containerNumber + " .container-verified img"));
    }

    public void setChildDepletion (Container child) {
        String select = format ("#containerNumber_%s " + depleted, child.containerNumber);
        assertTrue (clickAndSelectValue (select, "boolean:" + child.depleted));
    }

    private void clickMove () {
        assertTrue (click (moveBtn));
    }

    public void clickClose () {
        assertTrue (click ("[ng-click='ctrl.ok()']"));
        moduleLoading ();
    }

    private void moveModal () {
        assertTrue (isTextInElement (popupTitle, "Move Container"));
    }

    private void setDepletion (Container container) {
        if (container.containerType.isHolding) {
            assertTrue (noSuchElementPresent ("div[ng-if*='ctrl.enableDepletion']"));
            assertTrue (noSuchElementPresent ("[containers='[ctrl.containerDetail]'] " + depleted));
        } else if (container.depleted != null)
            assertTrue (clickAndSelectValue (depleted, "boolean:" + container.depleted));
    }
}
