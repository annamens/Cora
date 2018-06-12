package com.adaptivebiotech.test.cora.order;

import static com.adaptivebiotech.utils.PageHelper.OrderStatus.Active;
import static com.adaptivebiotech.utils.PageHelper.OrderStatus.Pending;
import static com.adaptivebiotech.utils.TestHelper.randomWords;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.dto.Orders.Order;
import com.adaptivebiotech.ui.cora.order.Diagnostic;
import com.adaptivebiotech.ui.cora.order.OrderList;

@Test (groups = { "regression" })
public class TransferTrfTestSuite extends OrderTestBase {

    private OrderList  list;
    private Diagnostic diagnostic;

    @BeforeMethod
    public void beforeMethod () {
        list = new OrderList ();
        diagnostic = new Diagnostic ();
    }

    public void pendingOrder () {
        // search for pending orders
        Order expected = list.getAllPendingOrders ().getOriginals ().list.get (0);
        list.doOrderSearch (expected.order_number);
        String postfix = String.valueOf ((char) ('a' + list.getOrders ().list.size () - 1));
        list.clickOrder (expected.order_number);

        // test: transfer a pending TRF order
        diagnostic.isCorrectPage ();
        diagnostic.enterPatientNotes (randomWords (30));
        diagnostic.enterOrderNotes (randomWords (25));
        diagnostic.clickSave ();
        expected = diagnostic.parseOrder (Pending);
        diagnostic.transferTrf ();
        diagnostic.isCorrectPage ();

        // test: verify the newly cloned order
        Order actual = diagnostic.parseOrder (Pending);
        assertEquals (actual.order_number, String.join ("-", expected.order_number, postfix));
        assertEquals (actual.name, String.join ("-", expected.name, postfix));
        verifyTrfCopied (actual, expected);

        // cleanup
        diagnostic.clickCancelOrder ();
        list.searchAndClickOrder (expected.order_number);
        diagnostic.isCorrectPage ();
        diagnostic.clickCancelOrder ();
    }

    public void activeOrder () {
        // search for active orders
        Order expected = list.getAllActiveOrders ().getOriginals ().list.get (0);
        list.doOrderSearch (expected.order_number);
        String postfix = String.valueOf ((char) ('a' + list.getOrders ().list.size () - 1));
        list.clickOrder (expected.order_number);

        // test: transfer a active TRF order
        diagnostic.isActiveOrderStatusPage ();
        diagnostic.clickOrderDetails ();
        diagnostic.editPatientNotes (randomWords (30));
        diagnostic.editOrderNotes (randomWords (25));
        expected = diagnostic.parseOrder (Active);
        diagnostic.transferTrf ();
        diagnostic.isCorrectPage ();

        // test: verify the newly cloned order
        Order actual = diagnostic.parseOrder (Pending);
        assertEquals (actual.order_number, String.join ("-", expected.order_number, postfix));
        assertEquals (actual.name, String.join ("-", expected.name, postfix));
        verifyTrfCopied (actual, expected);

        // cleanup
        diagnostic.clickCancelOrder ();
    }

    public void doubleTransfers () {
        // search for pending orders
        Order expected = list.getAllPendingOrders ().getOriginals ().list.get (0);
        list.doOrderSearch (expected.order_number);
        String postfix1 = String.valueOf ((char) ('a' + list.getOrders ().list.size () - 1));
        list.clickOrder (expected.order_number);

        // test: transfer a pending TRF order
        diagnostic.isCorrectPage ();
        expected = diagnostic.parseOrder (Pending);
        diagnostic.transferTrf ();
        diagnostic.isCorrectPage ();

        // test: first cloned order
        diagnostic.enterPatientNotes (randomWords (30));
        diagnostic.enterOrderNotes (randomWords (25));
        diagnostic.clickSave ();
        Order actual1 = diagnostic.parseOrder (Pending);
        assertEquals (actual1.order_number, String.join ("-", expected.order_number, postfix1));
        assertEquals (actual1.name, String.join ("-", expected.name, postfix1));
        diagnostic.transferTrf ();
        diagnostic.isCorrectPage ();

        // test: second cloned order
        actual1.order_number = actual1.order_number.replace ("-" + postfix1, "");
        actual1.name = actual1.name.replace ("-" + postfix1, "");

        String postfix2 = String.valueOf ((char) (postfix1.charAt (0) + 1));
        Order actual2 = diagnostic.parseOrder (Pending);
        assertEquals (actual2.order_number, String.join ("-", expected.order_number, postfix2));
        assertEquals (actual2.name, String.join ("-", expected.name, postfix2));
        verifyTrfCopied (actual2, actual1);

        // cleanup
        diagnostic.clickCancelOrder ();
        list.searchAndClickOrder (String.join ("-", actual1.order_number, postfix1));
        diagnostic.isCorrectPage ();
        diagnostic.clickCancelOrder ();

        list.searchAndClickOrder (expected.order_number);
        diagnostic.isCorrectPage ();
        diagnostic.clickCancelOrder ();
    }
}
