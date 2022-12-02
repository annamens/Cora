/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.ui.order;

import static com.adaptivebiotech.cora.dto.Orders.OrderCategory.Diagnostic;
import static com.adaptivebiotech.cora.utils.PageHelper.DateRange.Last30;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestUrl;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertTrue;
import java.util.List;
import com.adaptivebiotech.cora.dto.Orders;
import com.adaptivebiotech.cora.dto.Orders.Order;
import com.adaptivebiotech.cora.dto.Orders.OrderCategory;
import com.adaptivebiotech.cora.dto.Orders.OrderStatus;
import com.adaptivebiotech.cora.ui.CoraPage;
import com.adaptivebiotech.cora.utils.PageHelper.DateRange;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class OrdersList extends CoraPage {

    public OrdersList () {
        staticNavBarHeight = 90;
    }

    @Override
    public void isCorrectPage () {
        assertTrue (waitUntilVisible (".active[title='Orders']"));
        pageLoading ();
    }

    public void isCorrectTopNavRow1 (String user) {
        assertTrue (isTextInElement (".navbar-brand", "CORA"));
        assertTrue (isElementPresent ("#navNewDropdown"));
        assertTrue (waitUntilVisible (".active[title='Orders']"));
        assertTrue (waitUntilVisible ("[title='Order Tests']"));
        assertTrue (waitUntilVisible ("[title='Shipments']"));
        assertTrue (waitUntilVisible ("[title='Containers']"));
        assertTrue (waitUntilVisible ("[title='Tasks']"));
        assertTrue (waitUntilVisible ("[title='MIRAs']"));
        assertTrue (waitUntilVisible ("[title='Patients']"));
        assertTrue (waitUntilVisible (".glyphicon-wrench"));
        if (isElementVisible (".user-name"))
            assertTrue (isTextInElement (".user-name", user));
        else
            assertTrue (waitUntilVisible (format ("[title='%s']", user)));
        assertTrue (isTextInElement ("#sign-out", "Sign Out"));
        assertTrue (waitUntilVisible (".glyphicon-question-sign"));
    }

    public void isCorrectTopNavRow2 () {
        assertTrue (isTextInElement (".header-title", "Order List"));
    }

    public void selectCategory (OrderCategory category) {
        assertTrue (clickAndSelectText ("[ng-model='ctrl.selectedCategory']", category.name ()));
    }

    public void selectOrderStatus (OrderStatus status) {
        assertTrue (clickAndSelectText ("[ng-model='ctrl.selectedStatus']", status.name ()));
    }

    public void selectCreationDate (DateRange range) {
        assertTrue (clickAndSelectText ("[ng-model='ctrl.selectedDateRange']", range.label));
    }

    public void selectCreatedBy (String user) {
        assertTrue (clickAndSelectText ("[ng-model='ctrl.selectedCreator']", user));
    }

    public Orders getAllDiagnosticOrders (OrderStatus status) {
        selectCategory (Diagnostic);
        selectOrderStatus (status);
        selectCreationDate (Last30);
        clickFilter ();
        return getOrders ();
    }

    public Orders getOrders () {
        return new Orders (waitForElements ("[ng-repeat-start='order in ctrl.orders']").stream ().map (el -> {
            Order o = new Order ();
            o.orderNumber = getText (el, "[ng-bind='::order.displayOrderNumber']");
            o.name = getText (el, "[ng-bind='::order.name']");
            return o;
        }).filter (o -> o.name.length () > 1).collect (toList ()));
    }

    public void goToOrderTests () {
        String url = "/cora/ordertests?status=all&sort=duedate&ascending=false&search=";
        assertTrue (navigateTo (coraTestUrl + url));
        pageLoading ();
    }

    public List <String> getStageDropDownMenuItemLabelList () {
        return getTextList ("[name='stage']");
    }
}
