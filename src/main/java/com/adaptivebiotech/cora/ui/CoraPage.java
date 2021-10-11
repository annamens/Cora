package com.adaptivebiotech.cora.ui;

import static com.adaptivebiotech.test.BaseEnvironment.coraTestUrl;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.substringBetween;
import static org.testng.Assert.assertTrue;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.util.Strings;
import com.adaptivebiotech.cora.dto.Containers.Container;
import com.seleniumfy.test.utils.BasePage;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class CoraPage extends BasePage {

    private final String   newmenu    = "li:nth-child(1) .new-order #navNewDropdown";
    private final String   utilities  = "li:nth-child(9) .new-order #navNewDropdown";
    protected final String popupTitle = ".modal-header .modal-title";

    public CoraPage () {
        staticNavBarHeight = 35;
    }

    public void isCorrectPage () {
        assertTrue (waitUntilVisible (".navbar"));
        assertTrue (waitUntilVisible (".content"));
        assertTrue (waitUntilVisible ("[type='search']"));
        pageLoading ();
    }

    public boolean isHeaderNavHighlighted (String header) {
        return isElementPresent (format (".active[title='%s']", header));
    }

    public void clickSignOut () {
        assertTrue (click ("#sign-out"));
    }

    public String getMailTo () {
        return substringBetween (getAttribute (".cora-support", "href"), "mailto:", "?subject");
    }

    public void clickNew () {
        assertTrue (click (newmenu));
    }

    public List <String> getNewPopupMenu () {
        return getTextList ("li:nth-child(1) ul li").stream ().filter (li -> !Strings.isNullOrEmpty (li))
                                                    .collect (toList ());
    }

    public void selectNewClonoSEQDiagnosticOrder () {
        assertTrue (click (newmenu));
        assertTrue (waitUntilVisible (".dropdown.new-order.open"));
        assertTrue (click ("//a[text()='clonoSEQ Diagnostic Order']"));
    }

    public void selectNewTDetectDiagnosticOrder () {
        assertTrue (click (newmenu));
        assertTrue (waitUntilVisible (".dropdown.new-order.open"));
        assertTrue (click ("//a[text()='T-Detect Diagnostic Order']"));
    }

    public void selectNewDiagnosticShipment () {
        assertTrue (click (newmenu));
        assertTrue (waitUntilVisible (".dropdown.new-order.open"));
        assertTrue (click ("//a[text()='Diagnostic Shipment']"));
    }

    public void selectNewBatchOrder () {
        assertTrue (click (newmenu));
        assertTrue (waitUntilVisible (".dropdown.new-order.open"));
        assertTrue (click ("//a[text()='Batch Order']"));
    }

    public void selectNewBatchShipment () {
        assertTrue (click (newmenu));
        assertTrue (waitUntilVisible (".dropdown.new-order.open"));
        assertTrue (click ("//a[text()='Batch Shipment']"));
    }

    public void selectNewGeneralShipment () {
        assertTrue (click (newmenu));
        assertTrue (waitUntilVisible (".dropdown.new-order.open"));
        assertTrue (click ("//a[text()='General Shipment']"));
    }

    public void selectNewContainer () {
        assertTrue (click (newmenu));
        assertTrue (waitUntilVisible (".dropdown.new-order.open"));
        assertTrue (click ("//a[text()='Container']"));
    }

    public void selectNewMira () {
        assertTrue (click (newmenu));
        assertTrue (waitUntilVisible (".dropdown.new-order.open"));
        assertTrue (click ("//a[text()='MIRA']"));
    }

    public void selectNewTask () {
        assertTrue (click (newmenu));
        assertTrue (waitUntilVisible (".dropdown.new-order.open"));
        assertTrue (click ("//a[text()='Task']"));
    }

    public void clickCora () {
        assertTrue (click (".navbar-brand"));
    }

    public void clickOrders () {
        assertTrue (click ("#orders-tab"));
    }

    public void clickOrderTests () {
        assertTrue (click ("#order-tests-tab"));
    }

    public void clickShipments () {
        assertTrue (click ("#shipments-tab"));
    }

    public void clickContainers () {
        assertTrue (click ("#containers-tab"));
    }

    public void clickTasks () {
        assertTrue (click ("#tasks-tab"));
    }

    public void clickMiras () {
        assertTrue (click ("#miras-tab"));
    }

    public void clickPatients () {
        assertTrue (click ("#patients-tab"));
    }

    public void clickUtilities () {
        assertTrue (click (utilities));
    }

    public List <String> getUtilitiesMenu () {
        return getTextList ("li:nth-child(9) ul li");
    }

    public void selectAuditTool () {
        assertTrue (click (utilities));
        assertTrue (waitUntilVisible (".dropdown.new-order.open"));
        assertTrue (click ("//a[text()='Audit Tool']"));
    }

    public void selectBarcodeComparisonTool () {
        assertTrue (click (utilities));
        assertTrue (waitUntilVisible (".dropdown.new-order.open"));
        assertTrue (click ("//a[text()='Barcode Comparison Tool']"));
    }

    public void gotoOrderEntry (String orderId) {
        assertTrue (navigateTo (coraTestUrl + "/cora/order/entry/diagnostic/" + orderId));
        pageLoading ();
    }

    public void searchAndClickOrder (String orderNum) {
        doOrderSearch (orderNum);
        clickOrder (orderNum);
    }

    public void doOrderSearch (String searchTerm) {
        // return setText ("[type='search']", searchTerm) && pressKey (Keys.ENTER);
        // we have an issue with navbar hiding the "Search" field
        String search = "/cora/orders?creationdate=all&sort=created&ascending=true&search=" + searchTerm;
        assertTrue (navigateTo (coraTestUrl + search));
        pageLoading ();
    }

    public void doOrderTestSearch (String searchTerm) {
        String search = "/cora/ordertests?status=all&sort=duedate&ascending=false&search=" + searchTerm;
        assertTrue (navigateTo (coraTestUrl + search));
        pageLoading ();
    }

    public void doTaskSearch (String searchTerm) {
        String search = "/cora/tasks?sort=created&ascending=false&status=all&stageStatus=all&search=" + searchTerm;
        assertTrue (navigateTo (coraTestUrl + search));
        pageLoading ();
    }

    public void clickOrder (String orderNum) {
        assertTrue (click ("//table//*[text()='" + orderNum + "']"));
        pageLoading ();
    }

    public void searchContainer (Container container) {
        assertTrue (navigateTo (coraTestUrl + "/cora/containers/list?arrivalDate=all&search=" + container.containerNumber));
    }

    public void searchContainerByContainerId (Container container) {
        assertTrue (navigateTo (coraTestUrl + "/cora/containers/list?searchText=" + container.containerNumber));
    }

    public void showTodayFreezerContents (Container freezer) {
        assertTrue (navigateTo (coraTestUrl + "/cora/containers/list?arrivalDate=today&rootContainerId=" + freezer.id));
    }

    public void gotoMyCustody () {
        assertTrue (navigateTo (coraTestUrl + "/cora/containers/custody"));
    }

    public void gotoContainerDetail (Container container) {
        assertTrue (navigateTo (coraTestUrl + "/cora/container/details/" + container.id));
        pageLoading ();
    }

    public void gotoContainerHistory (Container container) {
        assertTrue (navigateTo (coraTestUrl + "/cora/container/details/" + container.id + "/history"));
        pageLoading ();
    }

    // click on (X) icon
    public void closePopup () {
        assertTrue (click ("[ng-click='ctrl.cancel()'] .glyphicon-remove"));
        moduleLoading ();
    }

    protected void pageLoading () {
        assertTrue (waitForElementInvisible (".loading-overlay")); // page overlay
        assertTrue (waitForElementInvisible ("[ng-show='loading.show']")); // yello bar: Loading...
    }

    protected void moduleLoading () {
        assertTrue (waitForElementInvisible (".message.ng-hide[ng-show='loading.overlay']"));
        assertTrue (noSuchElementPresent (".modal-dialog"));
        assertTrue (noSuchElementPresent (".modal-backdrop"));
    }

    protected String getConId (String href) {
        return href.replaceFirst (".*container/details/", "");
    }

    protected void clickPopupOK () {
        assertTrue (click ("[data-ng-click='ctrl.ok();']"));
        moduleLoading ();
    }

    public void ignoredUnsavedChanges () {
        assertTrue (isTextInElement (popupTitle, "Unsaved Changes"));
        clickPopupOK ();
    }

    public void clickFilter () {
        assertTrue (click ("[ng-click='ctrl.search()']"));
    }

    public Boolean waitForBooleanCondition (int secondsDuration, int pollSeconds, Function <WebDriver, Boolean> func) {
        Wait <WebDriver> wait = new FluentWait <> (this.getDriver ())
                                                                     .withTimeout (Duration.ofSeconds (secondsDuration))
                                                                     .pollingEvery (Duration.ofSeconds (pollSeconds));
        return wait.until (func);
    }

    public void gotoTaskById (String id) {
        String url = coraTestUrl + "/cora/task/" + id + "?p=status";
        assertTrue (navigateTo (url));
    }

    public boolean waitUntilVisible (String target, int timeoutInSeconds, int sleepInMillis) {
        waitForAjaxCalls ();
        By by = locateBy (target);
        try {
            WebDriverWait webDriverWait = new WebDriverWait (getDriver (), timeoutInSeconds, sleepInMillis);
            WebElement webElement = webDriverWait.until (ExpectedConditions.visibilityOfElementLocated (by));
            return webElement.isDisplayed ();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Wrapper for {@link Select}.getOptions()
     * 
     * @param select
     *            &lt;select&gt; element (css or xpath)
     * @return drop down option values
     */
    public List <String> getDropdownOptions (String select) {
        List <String> dropDownOptions = new LinkedList <String> ();
        try {
            Select dropdown = new Select (scrollTo (waitForElementClickable (locateBy (select))));

            for (WebElement element : dropdown.getOptions ()) {
                dropDownOptions.add (element.getText ());
            }
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
        return dropDownOptions;
    }
}
