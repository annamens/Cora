package com.adaptivebiotech.cora.test.order;

import static com.adaptivebiotech.test.utils.Logging.error;
import static com.adaptivebiotech.test.utils.PageHelper.OrderStatus.Active;
import static com.adaptivebiotech.test.utils.PageHelper.OrderStatus.Cancelled;
import static com.adaptivebiotech.test.utils.PageHelper.OrderStatus.Completed;
import static com.adaptivebiotech.test.utils.PageHelper.OrderStatus.Pending;
import static com.adaptivebiotech.test.utils.TestHelper.mapper;
import static com.adaptivebiotech.test.utils.TestHelper.randomWords;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Orders.Order;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.ui.order.Diagnostic;
import com.adaptivebiotech.cora.ui.order.OrdersList;

//TODO This test can mess with other peoples orders
@Test (groups = "regression", enabled = false)
public class TransferTrfTestSuite extends CoraBaseBrowser {

    private OrdersList list;
    private Diagnostic diagnostic;

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod () {
        list = new OrdersList ();
        diagnostic = new Diagnostic ();
    }

    public void pendingOrder () {
        // search for pending orders
        list.isCorrectPage ();
        Order expected = list.getAllDiagnosticOrders (Pending).findOriginals ().list.get (0);
        list.doOrderSearch (expected.order_number);
        String postfix = String.valueOf ((char) ('a' + list.getOrders ().list.size () - 1));
        list.clickOrder (expected.order_number);

        // test: transfer a pending TRF order
        diagnostic.isCorrectPage ();
        expected = diagnostic.parseOrder (Pending);
        expected.patient.notes = randomWords (30);
        expected.notes = randomWords (25);

        diagnostic.enterPatientNotes (expected.patient.notes);
        diagnostic.enterOrderNotes (expected.notes);
        diagnostic.transferTrf (Pending);
        diagnostic.isCorrectPage ();

        // test: verify the newly cloned order
        Order actual = diagnostic.parseOrder (Pending);
        assertEquals (actual.order_number, String.join ("-", expected.order_number, postfix));
        assertEquals (actual.name, String.join ("-", expected.name, postfix));
        verifyTrfCopied (actual, expected);

        // cleanup
        diagnostic.clickCancelOrder ();

        // trying "See original" link
        diagnostic.clickSeeOriginal ();
        assertEquals (diagnostic.getOrderNum (Active), expected.order_number);
        assertEquals (diagnostic.getOrderName (Active), expected.name);

        // cleanup
        list.searchAndClickOrder (expected.order_number);
        diagnostic.isCorrectPage ();
        diagnostic.clickCancelOrder ();
    }

    public void activeOrder () {
        // search for active orders
        list.isCorrectPage ();
        Order expected = list.getAllDiagnosticOrders (Active).findOriginals ().list.get (0);
        list.doOrderSearch (expected.order_number);
        String postfix = String.valueOf ((char) ('a' + list.getOrders ().list.size () - 1));
        list.clickOrder (expected.order_number);

        // test: transfer a active TRF order
        diagnostic.isOrderStatusPage ();
        diagnostic.clickOrderDetails ();
        expected = diagnostic.parseOrder (Active);
        expected.patient.notes = randomWords (30);
        expected.notes = randomWords (25);

        diagnostic.editPatientNotes (expected.patient.notes);
        diagnostic.editOrderNotes (expected.notes);
        diagnostic.transferTrf (Active);
        diagnostic.isCorrectPage ();

        // test: verify the newly cloned order
        Order actual = diagnostic.parseOrder (Pending);
        assertEquals (actual.order_number, String.join ("-", expected.order_number, postfix));
        assertEquals (actual.name, String.join ("-", expected.name, postfix));
        verifyTrfCopied (actual, expected);

        // cleanup
        diagnostic.clickCancelOrder ();

        // trying "See original" link
        diagnostic.clickSeeOriginal ();
        assertEquals (diagnostic.getOrderNum (Active), expected.order_number);
        assertEquals (diagnostic.getOrderName (Active), expected.name);
    }

    public void cancelledOrder () {
        // search for cancelled orders
        list.isCorrectPage ();
        Order expected = list.getAllDiagnosticOrders (Cancelled).findOriginals ().list.get (0);
        list.doOrderSearch (expected.order_number);
        String postfix = String.valueOf ((char) ('a' + list.getOrders ().list.size () - 1));
        list.clickOrder (expected.order_number);

        // test: transfer a pending TRF order
        diagnostic.isOrderStatusPage ();
        diagnostic.clickOrderDetails ();
        expected = diagnostic.parseOrder (Cancelled);
        expected.patient.notes = randomWords (30);
        expected.notes = randomWords (25);

        diagnostic.editPatientNotes (expected.patient.notes);
        diagnostic.editOrderNotes (expected.notes);
        diagnostic.transferTrf (Cancelled);
        diagnostic.isCorrectPage ();

        // test: verify the newly cloned order
        Order actual = diagnostic.parseOrder (Pending);
        assertEquals (actual.order_number, String.join ("-", expected.order_number, postfix));
        assertEquals (actual.name, String.join ("-", expected.name, postfix));
        verifyTrfCopied (actual, expected);

        // cleanup
        diagnostic.clickCancelOrder ();

        // trying "See original" link
        diagnostic.clickSeeOriginal ();
        assertEquals (diagnostic.getOrderNum (Cancelled), expected.order_number);
        assertEquals (diagnostic.getOrderName (Cancelled), expected.name);
    }

    public void completedOrder () {
        // search for completed orders
        list.isCorrectPage ();
        Order expected = list.getAllDiagnosticOrders (Completed).findOriginals ().list.get (0);
        list.doOrderSearch (expected.order_number);
        String postfix = String.valueOf ((char) ('a' + list.getOrders ().list.size () - 1));
        list.clickOrder (expected.order_number);

        // test: transfer a pending TRF order
        diagnostic.isOrderStatusPage ();
        diagnostic.clickOrderDetails ();
        expected = diagnostic.parseOrder (Completed);
        expected.patient.notes = randomWords (30);
        expected.notes = randomWords (25);

        diagnostic.editPatientNotes (expected.patient.notes);
        diagnostic.editOrderNotes (expected.notes);
        diagnostic.transferTrf (Completed);
        diagnostic.isCorrectPage ();

        // test: verify the newly cloned order
        Order actual = diagnostic.parseOrder (Pending);
        assertEquals (actual.order_number, String.join ("-", expected.order_number, postfix));
        actual.name = expected.name;
        verifyTrfCopied (actual, expected);

        // cleanup
        diagnostic.clickCancelOrder ();

        // trying "See original" link
        diagnostic.clickSeeOriginal ();
        assertEquals (diagnostic.getOrderNum (Completed), expected.order_number);
        assertEquals (diagnostic.getOrderName (Completed), expected.name);
    }

    public void doubleTransfers () {
        // search for pending orders
        list.isCorrectPage ();
        Order expected = list.getAllDiagnosticOrders (Pending).findOriginals ().list.get (0);
        list.doOrderSearch (expected.order_number);
        String postfix1 = String.valueOf ((char) ('a' + list.getOrders ().list.size () - 1));
        list.clickOrder (expected.order_number);

        // test: transfer a pending TRF order
        diagnostic.isCorrectPage ();
        expected = diagnostic.parseOrder (Pending);
        diagnostic.transferTrf (Pending);
        diagnostic.isCorrectPage ();

        // test: first cloned order
        Order actual1 = diagnostic.parseOrder (Pending);
        actual1.patient.notes = randomWords (30);
        actual1.notes = randomWords (25);
        assertEquals (actual1.order_number, String.join ("-", expected.order_number, postfix1));
        assertEquals (actual1.name, String.join ("-", expected.name, postfix1));
        diagnostic.enterPatientNotes (actual1.patient.notes);
        diagnostic.enterOrderNotes (actual1.notes);
        diagnostic.transferTrf (Pending);
        diagnostic.isCorrectPage ();

        // test: second cloned order
        String postfix2 = String.valueOf ((char) (postfix1.charAt (0) + 1));
        Order actual2 = diagnostic.parseOrder (Pending);
        assertEquals (actual2.order_number, String.join ("-", expected.order_number, postfix2));
        assertEquals (actual2.name, String.join ("-", expected.name, postfix2));
        actual1.order_number = expected.order_number;
        actual1.name = expected.name;
        verifyTrfCopied (actual2, actual1);

        // cleanup
        diagnostic.clickCancelOrder ();

        // go to the parent order
        diagnostic.clickSeeOriginal ();
        assertEquals (diagnostic.getOrderNum (Active), String.join ("-", expected.order_number, postfix1));
        assertEquals (diagnostic.getOrderName (Active), String.join ("-", expected.name, postfix1));

        // cleanup
        list.searchAndClickOrder (String.join ("-", expected.order_number, postfix1));
        diagnostic.isCorrectPage ();
        diagnostic.clickCancelOrder ();

        // one more time, go to the parent order
        diagnostic.clickSeeOriginal ();
        assertEquals (diagnostic.getOrderNum (Active), expected.order_number);
        assertEquals (diagnostic.getOrderName (Active), expected.name);

        // cleanup
        list.searchAndClickOrder (expected.order_number);
        diagnostic.isCorrectPage ();
        diagnostic.clickCancelOrder ();
    }

    private void verifyTrfCopied (Order actual, Order expected) {
        try {
            assertEquals (actual.orderEntryType, expected.orderEntryType);
            assertTrue (actual.order_number.startsWith (expected.order_number));
            assertTrue (actual.name.startsWith (expected.name));
            assertEquals (actual.isTrfAttached, expected.isTrfAttached);
            assertEquals (actual.date_signed, expected.date_signed);
            assertEquals (actual.customerInstructions, expected.customerInstructions);
            assertEquals (actual.physician.providerFullName, expected.physician.providerFullName);
            assertEquals (actual.physician.accountName, expected.physician.accountName);
            assertEquals (actual.patient.fullname, expected.patient.fullname);
            assertEquals (actual.patient.dateOfBirth, expected.patient.dateOfBirth);
            assertEquals (actual.patient.gender, expected.patient.gender);
            assertEquals (actual.patient.patientCode, expected.patient.patientCode);
            assertEquals (actual.patient.mrn, expected.patient.mrn);
            assertEquals (actual.icdcodes, expected.icdcodes);
            assertEquals (actual.properties.SpecimenDeliveryType, expected.properties.SpecimenDeliveryType);
            assertNull (actual.specimenDto.specimenNumber);
            assertNull (actual.specimenDto.sourceType);
            assertNull (actual.specimenDto.collectionDate);
            assertNull (actual.specimenDto.reconciliationDate);
            assertEquals (actual.expectedTestType, expected.expectedTestType);
            assertEquals (actual.tests.size (), 0);
            assertEquals (actual.properties.BillingType, expected.properties.BillingType);
            assertEquals (actual.patient.abnStatusType, expected.patient.abnStatusType);

            if (expected.patient.billingType != null)
                assertEquals (mapper.writeValueAsString (actual.patient.address),
                              mapper.writeValueAsString (expected.patient.address));
            else
                assertNull (actual.patient.billingType);

            assertEquals (mapper.writeValueAsString (actual.patient.insurance1),
                          mapper.writeValueAsString (expected.patient.insurance1));
            assertEquals (mapper.writeValueAsString (actual.patient.insurance2),
                          mapper.writeValueAsString (expected.patient.insurance2));

            assertEquals (actual.orderAttachments, expected.orderAttachments);
            assertEquals (actual.doraAttachments, expected.doraAttachments);
            assertNull (actual.notes);
        } catch (Exception e) {
            error ("expected order=" + expected.order_number + ", actual order=" + actual.order_number);
            throw new RuntimeException (e);
        }
    }
}
