package com.adaptivebiotech.cora.ui.container;

import static com.adaptivebiotech.cora.dto.Containers.ContainerType.Plate;
import static com.adaptivebiotech.cora.dto.Containers.ContainerType.getContainerType;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestUser;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import java.util.List;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.testng.util.Strings;
import com.adaptivebiotech.cora.dto.Containers;
import com.adaptivebiotech.cora.dto.Containers.Container;
import com.adaptivebiotech.cora.dto.Containers.ContainerType;
import com.adaptivebiotech.cora.ui.CoraPage;
import com.seleniumfy.test.utils.Timeout;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class ContainerList extends CoraPage {

    private final String   depleted                 = ".select-depletion";
    private final String   comments                 = "[ng-model='ctrl.comments']";
    private final String   pass                     = ".alert-success";
    private final String   fail                     = ".alert-danger";
    private final String   holdingBtn               = "[ng-click*='holdingContainer']";
    private final String   moveBtn                  = "[ng-click='ctrl.moveHere()']";
    private final String   locked                   = "//*[contains (@class, 'alert-danger') and contains (text(), 'All containers are locked by another process.')]";
    protected final String scan                     = "#container-scan-input";
    private final String   containerSearch          = "[placeholder='CO-000000 or Container Name']";
    private final String   freezerDropdownInput     = "[placeholder = 'Select Freezer'] input";
    private final String   freezerDropdownSelection = "//*[@placeholder='Select Freezer']/descendant::div[@role='option']/span[text()='%s']";
    private final String   bulkMoveBtn              = "//button[text()='Bulk Move']";
    private final String   bulkComment              = "input[placeholder='Add Comment']";
    private final String   selectAllCheckbox        = ".containers-list th [type='checkbox']";
    private final String   bulkMoveActionDropdown   = ".bulk-move-container select";
    private final String   bulkMoveSuccess          = ".toast-success";
    private final String   bulkMoveError            = ".toast-error";

    public ContainerList () {
        staticNavBarHeight = 90;
    }

    @Override
    public void isCorrectPage () {
        assertTrue (waitUntilVisible ("[uisref='main.containers.list']"));
        assertTrue (waitUntilVisible ("[uisref='main.containers.custody']"));
    }

    public static enum Category {
        Any, Diagnostic, Batch
    }

    public static enum GroupBy {
        None, HoldingContainer
    }

    public static enum BulkMoveAction {
        BulkMoveToMyCustody ("Bulk Move to My Custody"),
        BulkMoveToFreezer ("Bulk Move to Freezer");

        public final String text;

        private BulkMoveAction (String text) {
            this.text = text;
        }
    }

    public int getMyCustodySize () {
        return Integer.valueOf (getText ("[uisref='main.containers.custody'] span").replaceAll (",", ""));
    }

    public void searchContainerIdOrName (String containerIdOrName) {
        assertTrue (clear (containerSearch));
        assertTrue (setText (containerSearch, containerIdOrName));
        assertTrue (click (".search-btn"));
        assertNotNull (waitForElementClickable ("//button[text()='Filter list']"));
    }

    public void searchContainerIdsOrNames (List <String> containerIdsOrNames) {
        searchContainerIdOrName (String.join (",", containerIdsOrNames));
    }

    public void setCategory (Category category) {
        assertTrue (click ("//*[contains (p,'Category')]//button"));
        assertTrue (click (format ("//*[contains (p,'Category')]//a[text()='%s']", category.name ())));
    }

    public void setCurrentLocationFilter (String freezer) {
        assertTrue (click ("//*[contains (p,'Current Location')]//button"));
        assertTrue (click (format ("//*[contains (p,'Current Location')]//a[text()='%s']", freezer)));
    }

    public void setContainerType (ContainerType type) {
        assertTrue (click ("//*[contains (p,'Container Type')]//button"));
        assertTrue (click (format ("//*[contains (p,'Container Type')]//a[text()='%s']", type.label)));
    }

    public void setGroupBy (GroupBy groupBy) {
        assertTrue (click ("//*[contains (p,'Group By')]//button"));
        assertTrue (click (format ("//*[contains (p,'Group By')]//a[text()='%s']", groupBy.name ())));
    }

    public Containers getContainers () {
        return new Containers (waitForElements (".containers-list > tbody > tr").stream ().map (el -> {
            List <WebElement> columns = findElements (el, "td");
            Container c = new Container ();
            c.id = getConId (getAttribute (columns.get (2), "a", "href"));
            c.containerNumber = getText (columns.get (2));
            String containerType = getText (columns.get (3));
            c.containerType = containerType != null && !containerType.equals ("Unsupported") ? getContainerType (containerType) : null;
            c.specimenId = getText (columns.get (4));
            c.name = getText (columns.get (5));
            c.location = getText (columns.get (6));
            String capacity = getText (columns.get (7));
            c.capacity = Integer.parseInt (Strings.isNotNullAndNotEmpty (capacity) ? capacity : "0");
            return c;
        }).collect (toList ()));
    }

    public void scan (Container container) {
        scan (container.containerNumber);
    }

    public void scan (String containerNumber) {
        assertTrue (clear (scan));
        assertTrue (setText (scan, containerNumber));
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
        String success = format ("Moved container %s to ", container.containerNumber);
        if (comment != null)
            assertTrue (setText (comments, comment));

        assertTrue (click (format ("//*[@class='modal-content']/descendant::span[text()='%s']", freezer.name)));
        clickMove ();
        transactionInProgress ();

        // SR-8459: sometimes we're getting a container locked error msg
        Timeout timer = new Timeout (millisRetry * 10, waitRetry);
        boolean isLocked = false;
        while (!timer.Timedout () && (isLocked = isElementPresent (locked))) {
            clickMove ();
            transactionInProgress ();
        }
        if (isLocked)
            fail ("unable to move to freezer");

        assertTrue (isTextInElement (pass, success + freezer.name));
        assertTrue (noSuchElementPresent (depleted));
        assertTrue (noSuchElementPresent (comments));

        String regex = "(.*)(Position.*$)";
        String loc = getText (pass).replace (success, "");
        container.location = loc;
        container.root = null;
        if (container.containerType.isHolding && !container.containerType.equals (Plate) && container.children != null)
            container.children.forEach (c -> {
                c.location = String.join (" : ", loc, c.containerType.label, c.location.replaceFirst (regex, "$2"));
            });

        clickClose ();
        moduleLoading ();
        pageLoading ();
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

        // SR-8459: sometimes we're getting a container locked error msg
        Timeout timer = new Timeout (millisRetry * 20, waitRetry);
        boolean isSuccessful = true;
        while (!timer.Timedout () && ! (isSuccessful = finalizeHoldingContainer (child, holding))) {
            scanAndClickHoldingContainer (child);
        }
        if (!isSuccessful)
            fail ("unable to set holding container");
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

    public boolean finalizeHoldingContainer (Container child, Container holding) {
        chooseHoldingContainer (holding);
        if (holding.containerType.isHolding) {
            assertTrue (waitUntilVisible ("[containers='[ctrl.holdingContainer]']"));
            clickMove ();
            transactionInProgress ();
            if (isElementPresent (locked)) {
                child.root = null;
                closePopup ();
                return false;
            }

            if (holding.containerType.type.equals (child.containerType.type)) {
                String root = child.root != null ? " from " + child.root.containerNumber : "";
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
        return true;
    }

    public void chooseHoldingContainer (Container holding) {
        chooseHoldingContainer (holding.containerNumber);
    }

    public void chooseHoldingContainer (String containerNumber) {
        String target = ".modal-body [ng-model='ctrl.containerNumber']";
        assertTrue (clear (target));
        assertTrue (setText (target, containerNumber));
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
        assertTrue (clear (comments));
        assertTrue (setText (comments, format ("verifying - %s", child.containerNumber)));
        scanToVerify (child);
        isVerified (child);
        clickClose ();
    }

    public void scanToVerify (Container child) {
        scanToVerify (child.containerNumber);
    }

    public void scanToVerify (String containerNumber) {
        String scanField = "#scan_input";
        assertTrue (clear (scanField));
        assertTrue (setText (scanField, containerNumber));
        assertTrue (pressKey (Keys.ENTER));
    }

    public void isVerified (Container child) {
        assertTrue (waitUntilVisible (format ("#containerNumber_%s .container-verified img", child.containerNumber)));
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

    public void bulkMoveAllToFreezer (Container freezer) {
        bulkMoveAllToFreezer (freezer, null);
    }

    public void bulkMoveAllToFreezer (Container freezer, String comment) {
        clickBulkMoveContainers ();
        selectBulkMoveAction (BulkMoveAction.BulkMoveToFreezer);
        assertTrue (click (freezerDropdownInput));
        assertTrue (clear (freezerDropdownInput));
        assertTrue (setText (freezerDropdownInput, freezer.name));
        assertTrue (click (format (freezerDropdownSelection,
                                   freezer.name)));
        if (comment != null) {
            assertTrue (clear (bulkComment));
            assertTrue (setText (bulkComment, comment));
        }
        assertTrue (click (selectAllCheckbox));
        assertTrue (click (bulkMoveBtn));
        transactionInProgress ();
    }

    public void bulkMoveAllToCustody () {
        bulkMoveAllToCustody (null);
    }

    public void bulkMoveAllToCustody (String comment) {
        clickBulkMoveContainers ();
        selectBulkMoveAction (BulkMoveAction.BulkMoveToMyCustody);
        if (comment != null) {
            assertTrue (clear (bulkComment));
            assertTrue (setText (bulkComment, comment));
        }
        assertTrue (click (selectAllCheckbox));
        assertTrue (click (bulkMoveBtn));
        transactionInProgress ();
    }

    public void clickBulkMoveContainers () {
        assertTrue (click ("//button[text()='Bulk Move Containers']"));
        waitForElement (".bulk-move-blue");
    }

    public void selectBulkMoveAction (BulkMoveAction action) {
        clickAndSelectText (bulkMoveActionDropdown, action.text);
    }

    public boolean scanFieldDisplayed () {
        return isElementPresent (scan);
    }

    public boolean isBulkMoveSuccessMessageDisplayed () {
        return waitUntilVisible (bulkMoveSuccess);
    }

    public boolean isBulkMoveErrorMessageDisplayed () {
        return waitUntilVisible (bulkMoveError);
    }

    public String getBulkMoveErrorMessage () {
        return getText (bulkMoveError);
    }

    public void clickSuccessMessageLink () {
        assertTrue (click (bulkMoveSuccess + " a"));
    }

    public boolean isFreezerDropdownEnabled () {
        String freezerDropdownContainer = "//*[@placeholder='Select Freezer']/parent::div";
        return !getAttribute (freezerDropdownContainer, "class").contains ("div-disabled");
    }

    public void selectContainerToBulkMove (String containerName) {
        String checkbox = format ("//*[@title='%s']/ancestor::tr/descendant::*[@type='checkbox']", containerName);
        assertTrue (click (checkbox));
    }

    public boolean rowIsSelected (String containerName) {
        String row = format ("//*[@title='%s']/ancestor::tr", containerName);
        return getAttribute (row, "class").contains ("highlighted-blue");
    }

    public List <String> getBulkMoveActions () {
        return getTextList (bulkMoveActionDropdown + " option");
    }
}
