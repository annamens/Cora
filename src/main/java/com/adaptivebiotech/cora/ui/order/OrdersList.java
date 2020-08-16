package com.adaptivebiotech.cora.ui.order;

import static com.adaptivebiotech.test.BaseEnvironment.coraTestUrl;
import static com.adaptivebiotech.test.utils.PageHelper.Assay.getAssay;
import static com.adaptivebiotech.test.utils.PageHelper.DateRange.Last30;
import static com.adaptivebiotech.test.utils.PageHelper.OrderCategory.Diagnostic;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import java.util.List;
import com.adaptivebiotech.common.dto.Orders;
import com.adaptivebiotech.common.dto.Orders.Order;
import com.adaptivebiotech.common.dto.Orders.OrderTest;
import com.adaptivebiotech.common.dto.Patient;
import com.adaptivebiotech.cora.dto.Workflow;
import com.adaptivebiotech.test.utils.PageHelper.DateRange;
import com.adaptivebiotech.test.utils.PageHelper.OrderCategory;
import com.adaptivebiotech.test.utils.PageHelper.OrderStatus;
import com.adaptivebiotech.ui.cora.CoraPage;
import com.seleniumfy.test.utils.Timeout;

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
        pageLoading ();
        return getOrders ();
    }

    public Orders getOrders () {
        return new Orders (waitForElements ("[ng-repeat-start='order in ctrl.orders']").stream ().map (el -> {
            Order o = new Order ();
            o.order_number = getText (el, "[ng-bind='::order.displayOrderNumber']");
            o.name = getText (el, "[ng-bind='::order.name']");
            return o;
        }).filter (o -> o.name.length () > 1).collect (toList ()));
    }

    public Orders getOrderTests () {
        Timeout timer = new Timeout (millisRetry, waitRetry);
        Orders orders = tryGettingOrderTests ();
        do {
            assertTrue (refresh ());
            pageLoading ();
            timer.Wait ();
            orders = tryGettingOrderTests ();
        } while (!timer.Timedout () && orders.list.parallelStream ().anyMatch (o -> o.workflow.sampleName == null));
        if (orders.list.parallelStream ().anyMatch (o -> o.workflow.sampleName == null))
            fail ("orderTest workflowName is null");

        return orders;
    }

    public void goToOrderTests () {
        String url = "/cora/ordertests?status=all&sort=duedate&ascending=false&search=";
        assertTrue (navigateTo (coraTestUrl + url));
        pageLoading ();
    }

    public List <String> getStageDropDownMenuItemLabelList () {
        return getTextList ("[name='stage']");
    }

    private Orders tryGettingOrderTests () {
        return new Orders (waitForElements ("[ng-repeat-start='orderTest in ctrl.orderTests']").stream ().map (el -> {
            Order o = new Order ();
            o.name = getText (el, "[ng-bind='::orderTest.orderName']");
            o.id = getAttribute (el, "[ng-bind='::orderTest.orderName']", "href").replaceFirst (".*ordertestid=", "");
            o.tests.add (new OrderTest (null, getAssay (getText (el, "[ng-bind='::orderTest.testName']")), true));

            Patient patient = new Patient ();
            patient.patientCode = getText (el, "[ng-bind='::orderTest.patientCode']");
            o.patient = patient;

            Workflow workflow = new Workflow ();
            workflow.sampleName = getText (el, "[ng-bind='::orderTest.workflowName']");
            o.workflow = workflow;
            o.order_number = o.name.replaceFirst (".*-D-", "D-");
            return o;
        }).filter (o -> o.name.length () > 1).collect (toList ()));
    }
}
