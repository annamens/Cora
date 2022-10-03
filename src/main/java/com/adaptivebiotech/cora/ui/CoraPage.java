/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.ui;

import static com.adaptivebiotech.test.BaseEnvironment.coraTestUrl;
import static java.lang.String.format;
import static java.util.UUID.fromString;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.substringBetween;
import static org.testng.Assert.assertTrue;
import static org.testng.util.Strings.isNullOrEmpty;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import com.adaptivebiotech.cora.dto.Containers.Container;
import com.seleniumfy.test.utils.BasePage;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class CoraPage extends BasePage {

    private final String   newmenu     = "li:nth-child(1) .new-order #navNewDropdown";
    private final String   utilities   = "li:nth-child(9) .new-order #navNewDropdown";
    protected final String popupTitle  = ".modal-header .modal-title";
    protected final String tabBase     = "//ul[contains(@class, 'nav-tabs')]//*[text()='%s']";
    protected final String requiredMsg = ".text-danger";

    public CoraPage () {
        staticNavBarHeight = 35;
    }

    @Override
    public String getText (WebElement el) {
        String text = el.getText ();
        return isNullOrEmpty (text) ? null : isNullOrEmpty (text.trim ()) ? null : text.trim ();
    }

    @Override
    public String getAttribute (WebElement el, String attribute) {
        String attr = el.getAttribute (attribute);
        return isNullOrEmpty (attr) ? null : isNullOrEmpty (attr.trim ()) ? null : attr.trim ();
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
        return getTextList ("li:nth-child(1) ul li").stream ().filter (li -> !isNullOrEmpty (li)).collect (toList ());
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

    public void gotoOrderEntry (UUID orderId) {
        assertTrue (navigateTo (coraTestUrl + "/cora/order/auto?id=" + orderId));
        pageLoading ();
    }

    public void searchAndClickOrder (String orderNum) {
        doOrderSearch (orderNum);
        clickOrder (orderNum);
    }

    public void doOrderSearch (String searchTerm) {
        // return setText ("[type='search']", searchTerm) && pressKey (Keys.ENTER);
        // we have an issue with navbar hiding the "Search" field
        String search = "/cora/orders?creationDate=all&sort=Created&ascending=true&search=" + searchTerm;
        assertTrue (navigateTo (coraTestUrl + search));
        pageLoading ();
    }

    public void doOrderTestSearch (String searchTerm) {
        String search = "/cora/ordertests?dueDate=all&sort=DueDate&ascending=false&search=" + searchTerm;
        assertTrue (navigateTo (coraTestUrl + search));
        pageLoading ();
    }

    public void doTaskSearch (String searchTerm) {
        String search = "/cora/tasks?sort=Created&ascending=false&status=Active&stageStatus=All&search=" + searchTerm;
        assertTrue (navigateTo (coraTestUrl + search));
        pageLoading ();
    }

    public void clickOrder (String orderNum) {
        assertTrue (click ("//table//*[text()='" + orderNum + "']"));
        pageLoading ();
    }

    public void clickOrderName (String orderName) {
        assertTrue (click ("//table//*[text()='" + orderName + "']"));
        pageLoading ();
    }

    public void searchContainer (Container container) {
        assertTrue (navigateTo (coraTestUrl + "/cora/containers/list?searchText=" + container.containerNumber + "&sort=HoldingContainer&ascending=true&searchType=Container&groupByHoldingContainer=false&includeChildSpecimen=false&offset=0"));
    }

    public void gotoMyCustody () {
        assertTrue (navigateTo (coraTestUrl + "/cora/containers/custody"));
    }

    public void gotoShipmentEntry (UUID shipmentId) {
        assertTrue (navigateTo (coraTestUrl + "/cora/shipment/entry/" + shipmentId));
        pageLoading ();
    }

    public void gotoContainerDetail (Container container) {
        assertTrue (navigateTo (coraTestUrl + "/cora/container/details/" + container.id));
        pageLoading ();
    }

    public void gotoContainerHistory (Container container) {
        assertTrue (navigateTo (coraTestUrl + "/cora/container/details/" + container.id + "/history"));
        pageLoading ();
    }

    public void gotoOrderDetailsPage (UUID orderId) {
        assertTrue (navigateTo (coraTestUrl + "/cora/order/details/" + orderId));
        assertTrue (hasPageLoaded ());
        pageLoading ();
    }

    public void gotoOrderStatusPage (UUID orderId) {
        assertTrue (navigateTo (coraTestUrl + "/cora/order/status/" + orderId));
        assertTrue (hasPageLoaded ());
        pageLoading ();
    }

    public void gotoTaskDetail (UUID taskId) {
        assertTrue (navigateTo (coraTestUrl + "/cora/task/" + taskId));
        assertTrue (hasPageLoaded ());
        pageLoading ();
    }

    public void gotoTaskStatus (UUID taskId) {
        assertTrue (navigateTo (coraTestUrl + "/cora/task/" + taskId + "?p=status"));
        assertTrue (hasPageLoaded ());
        pageLoading ();
    }

    public void gotoAccession (UUID shipmentId) {
        assertTrue (navigateTo (coraTestUrl + "/cora/shipment/entry/" + shipmentId + "?p=accession"));
        assertTrue (hasPageLoaded ());
        pageLoading ();
    }

    // click on (X) icon
    public void closePopup () {
        assertTrue (click ("[ng-click='ctrl.cancel()'] .glyphicon-remove"));
        moduleLoading ();
    }

    protected void pageLoading () {
        assertTrue (waitForElementInvisible (".loading"));
    }

    protected void moduleLoading () {
        transactionInProgress ();
        assertTrue (noSuchElementPresent (".modal-dialog"));
        assertTrue (noSuchElementPresent (".modal-backdrop"));
    }

    // Transaction in progress: Do not leave this page.
    protected void transactionInProgress () {
        assertTrue (waitForElementInvisible (".message[ng-show='loading.overlay']"));
        hasPageLoaded ();
    }

    protected UUID getConId (String href) {
        return fromString (href.replaceFirst (".*container/details/", ""));
    }

    protected void clickPopupOK () {
        assertTrue (click ("//*[text()='Ok']"));
        moduleLoading ();
    }

    public void ignoredUnsavedChanges () {
        assertTrue (isTextInElement (popupTitle, "Unsaved Changes"));
        clickPopupOK ();
    }

    public void clickFilter () {
        assertTrue (click ("//*[text()='Filter list']"));
        pageLoading ();
    }

    public void gotoTaskById (UUID id) {
        String url = coraTestUrl + "/cora/task/" + id + "?p=status";
        assertTrue (navigateTo (url));
    }

    protected boolean waitUntilVisible (String target, int timeoutInSeconds, int sleepInMillis) {
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

    public void navigateToTab (int tabIndex) {
        getDriver ().switchTo ().window (new ArrayList <> (getDriver ().getWindowHandles ()).get (tabIndex));
    }

    public List <String> getUnauthorizedMsgs () {
        waitForElementVisible ("#unauthorized");
        List <String> elements = new LinkedList <> ();
        elements.add (getText (".unauthorized h2"));
        elements.add (getText (".unauthorized p"));
        return elements;
    }

    public List <String> getTabList () {
        return getTextList (".nav-tabs li:not([class*='ng-hide']) a:not([target='_blank'])");
    }

    public void closeFilePreview () {
        assertTrue (click (".modal-header button.close"));
        moduleLoading ();
    }
}
