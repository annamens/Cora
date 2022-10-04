/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.order;

import static com.adaptivebiotech.cora.test.CoraEnvironment.coraCSAdminTestPass;
import static com.adaptivebiotech.cora.test.CoraEnvironment.coraCSAdminTestUser;
import static com.adaptivebiotech.cora.test.CoraEnvironment.coraNonPHITestPass;
import static com.adaptivebiotech.cora.test.CoraEnvironment.coraNonPHITestUser;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestPass;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestUser;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import java.util.List;
import java.util.Map;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Orders.Assay;
import com.adaptivebiotech.cora.dto.Physician.PhysicianType;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.OrderTestsList;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.order.reservation.ReservationModule;

/**
 * @author Spencer Fisco
 *         <a href="mailto:sfisco@adaptivebiotech.com">sfisco@adaptivebiotech.com</a>
 */
@Test (groups = { "regression", "irish-wolfhound" })
public class OrderTestReservationTestSuite extends CoraBaseBrowser {
    private ReservationModule reservationModule = new ReservationModule ();
    private OrdersList        ordersList        = new OrdersList ();
    private OrderTestsList    orderTestsList    = new OrderTestsList ();
    private Login             login             = new Login ();

    @BeforeSuite (alwaysRun = true)
    public void setupReservations () {
        String test = Assay.COVID19_DX_IVD.test;
        login.doLogin ();
        ordersList.isCorrectPage ();
        ordersList.doOrderTestSearch (test);
        reservationModule.clickManageReservations ();
        reservationModule.selectCheckbox (1);
        reservationModule.selectCheckbox (2);
        reservationModule.clickReserve ();
        orderTestsList.clickSignOut ();
        login.doLogin (coraCSAdminTestUser, coraCSAdminTestPass);
        ordersList.isCorrectPage ();
        ordersList.doOrderTestSearch (test);
        reservationModule.clickManageReservations ();
        reservationModule.selectCheckbox (3);
        reservationModule.selectCheckbox (4);
        reservationModule.clickReserve ();
    }

    /**
     * NOTE: SR-T4319
     * 
     * @sdlc.requirements SR-8336:R3, SR-8336:R4, SR-8336:R26
     */
    public void orderTestsListReservationUI () {
        login.doLogin ();
        ordersList.isCorrectPage ();
        ordersList.doOrderTestSearch (PhysicianType.big_shot.accountName);
        assertTrue (reservationModule.manageReservationsButtonDisplayed ());
        assertFalse (reservationModule.reserveButtonDisplayed ());
        assertFalse (reservationModule.removeReservationButtonDisplayed ());
        assertFalse (reservationModule.doneButtonDisplayed ());
        assertFalse (reservationModule.reserveCheckboxesDisplayed ());
        testLog ("Reservation ui was hidden by default");
        reservationModule.clickManageReservations ();
        assertFalse (reservationModule.manageReservationsButtonDisplayed ());
        assertTrue (reservationModule.reserveButtonDisplayed ());
        assertTrue (reservationModule.removeReservationButtonDisplayed ());
        assertTrue (reservationModule.doneButtonDisplayed ());
        assertTrue (reservationModule.reserveCheckboxesDisplayed ());
        testLog ("Reservation ui was displayed after clicking button");
        reservationModule.selectCheckbox (1);
        reservationModule.selectCheckbox (2);
        assertTrue (reservationModule.rowIsSelected (1));
        assertTrue (reservationModule.rowIsSelected (2));
        reservationModule.selectCheckbox (1);
        assertFalse (reservationModule.rowIsSelected (1));
        testLog ("Multiple order tests could be selected and deselected");
    }

    /**
     * NOTE: SR-T4337
     * 
     * @sdlc.requirements SR-8336:R2
     */
    public void cldHasUI () {
        assertTrue (userSeesReservationUI (coraTestUser, coraTestPass));
        testLog ("Reservation ui was available for Clinical Laboratory Directors");
    }

    /**
     * NOTE: SR-T4337
     * 
     * @sdlc.requirements SR-8336:R2
     */
    public void csAdminHasUI () {
        assertTrue (userSeesReservationUI (coraCSAdminTestUser, coraCSAdminTestPass));
        testLog ("Reservation ui was available for CS Admins");
    }

    /**
     * NOTE: SR-T4337
     * 
     * @sdlc.requirements SR-8336:R2
     */
    public void otherUserHasNoUI () {
        assertFalse (userSeesReservationUI (coraNonPHITestUser, coraNonPHITestPass));
        testLog ("Reservation ui was not available for other user groups");
    }

    /**
     * NOTE: SR-T4360
     * 
     * @sdlc.requirements SR-8336:R13
     */
    public void exitReservation () {
        String sampleName = getUnreservedSampleName ();
        login.doLogin ();
        ordersList.isCorrectPage ();
        ordersList.doOrderTestSearch (sampleName);
        reservationModule.clickManageReservations ();
        reservationModule.selectCheckbox (1);
        reservationModule.clickDone ();
        assertFalse (orderTestsReserved (sampleName, coraTestUser));
        testLog ("Clicking Done with order test selected did not reserve");
        assertTrue (reservationModule.manageReservationsButtonDisplayed ());
        assertFalse (reservationModule.reserveButtonDisplayed ());
        assertFalse (reservationModule.removeReservationButtonDisplayed ());
        assertFalse (reservationModule.doneButtonDisplayed ());
        assertFalse (reservationModule.reserveCheckboxesDisplayed ());
        testLog ("Reservation ui was hidden after clicking done");
        reservationModule.clickManageReservations ();
        assertFalse (reservationModule.rowIsSelected (1));
        testLog ("Closing and opening reservation ui deselected order tests");
    }

    /**
     * NOTE: SR-T4356
     * 
     * @sdlc.requirements SR-8336:R19
     */
    public void reserveSuccessMessage () {
        login.doLogin ();
        ordersList.isCorrectPage ();
        ordersList.doOrderTestSearch (PhysicianType.big_shot.accountName);
        reservationModule.clickManageReservations ();
        reservationModule.selectCheckbox (1);
        reservationModule.selectCheckbox (2);
        reservationModule.clickReserve ();
        reservationModule.waitForToastMessage ();
        assertTrue (reservationModule.toastSuccessDisplayed ());
        assertEquals (reservationModule.getToastMessage (), "Reserved 2 tests.");
        testLog ("User was notified of the number of order tests reserved");
    }

    /**
     * NOTE: SR-T4357
     * 
     * @sdlc.requirements SR-8336:R18
     */
    public void removeReservationSuccessMessage () {
        login.doLogin ();
        ordersList.isCorrectPage ();
        ordersList.doOrderTestSearch (PhysicianType.big_shot.accountName);
        reservationModule.clickManageReservations ();
        reservationModule.selectCheckbox (1);
        reservationModule.selectCheckbox (2);
        reservationModule.clickRemoveReservation ();
        reservationModule.waitForToastMessage ();
        assertTrue (reservationModule.toastSuccessDisplayed ());
        assertEquals (reservationModule.getToastMessage (), "2 reservation(s) removed.");
        testLog ("User was notified of the number of order tests reservations removed");
    }

    /**
     * NOTE: SR-T4358
     * 
     * @sdlc.requirements SR-8336:R10
     */
    public void reserveUnreserved () {
        String sampleName = getUnreservedSampleName ();
        login.doLogin ();
        ordersList.isCorrectPage ();
        ordersList.doOrderTestSearch (sampleName);
        reservationModule.clickManageReservations ();
        reservationModule.selectCheckbox (1);
        reservationModule.clickReserve ();
        reservationModule.waitForToastMessage ();
        assertTrue (orderTestsReserved (sampleName, coraTestUser));
        testLog ("Unreserved order test was reserved");
    }

    /**
     * NOTE: SR-T4358
     * 
     * @sdlc.requirements SR-8336:R10
     */
    public void overrideReserve () {
        String sampleName = getReservedSampleName (coraCSAdminTestUser);
        login.doLogin ();
        ordersList.isCorrectPage ();
        ordersList.doOrderTestSearch (sampleName);
        reservationModule.clickManageReservations ();
        reservationModule.selectCheckbox (1);
        reservationModule.clickReserve ();
        reservationModule.waitForToastMessage ();
        assertTrue (orderTestsReserved (sampleName, coraTestUser));
        testLog ("Order test reservation was overridden by user");
    }

    /**
     * NOTE: SR-T4359
     * 
     * @sdlc.requirements SR-8336:R12
     */
    public void removeOwnReservation () {
        String sampleName = getReservedSampleName (coraTestUser);
        login.doLogin ();
        ordersList.isCorrectPage ();
        ordersList.doOrderTestSearch (sampleName);
        reservationModule.clickManageReservations ();
        reservationModule.selectCheckbox (1);
        reservationModule.clickRemoveReservation ();
        reservationModule.waitForToastMessage ();
        assertTrue (reservationModule.toastSuccessDisplayed ());
        assertFalse (orderTestsReserved (sampleName, coraTestUser));
        testLog ("User's own order test reservation was removed");
    }

    /**
     * NOTE: SR-T4359
     * 
     * @sdlc.requirements SR-8336:R12
     */
    public void removeOtherReservation () {
        String sampleName = getReservedSampleName (coraCSAdminTestUser);
        login.doLogin ();
        ordersList.isCorrectPage ();
        ordersList.doOrderTestSearch (sampleName);
        reservationModule.clickManageReservations ();
        reservationModule.selectCheckbox (1);
        reservationModule.clickRemoveReservation ();
        reservationModule.waitForToastMessage ();
        assertTrue (reservationModule.toastSuccessDisplayed ());
        assertFalse (orderTestsReserved (sampleName, coraTestUser));
        testLog ("Other user's order test reservation was removed");
    }

    private boolean userSeesReservationUI (String user, String pass) {
        login.doLogin (user, pass);
        ordersList.isCorrectPage ();
        ordersList.doOrderTestSearch (PhysicianType.big_shot.accountName);
        return reservationModule.manageReservationsButtonDisplayed ();
    }

    private String getUnreservedSampleName () {
        String query = "SELECT sample_name FROM cora.order_tests WHERE test_id = '1d298f94-74ed-474f-b12c-89e07295b1b4' AND properties->>'ReservedBy' IS NULL AND properties IS NOT NULL ORDER BY RANDOM() LIMIT 1;";
        List <Map <String, Object>> queryResult = coraDb.executeSelect (query);
        assertEquals (queryResult.size (), 1, "Unable to find unreserved order test");
        return String.valueOf (queryResult.get (0).get ("sample_name"));
    }

    private String getReservedSampleName (String reservedBy) {
        String query = format ("SELECT sample_name FROM cora.order_tests WHERE test_id = '1d298f94-74ed-474f-b12c-89e07295b1b4' AND properties->>'ReservedBy' != '%s' ORDER BY RANDOM() limit 1;",
                               reservedBy);
        List <Map <String, Object>> queryResult = coraDb.executeSelect (query);
        assertEquals (queryResult.size (), 1, "Unable to order test reserved by " + reservedBy);
        return String.valueOf (queryResult.get (0).get ("sample_name"));
    }

    // will be replaced when reservation visible on front end
    private boolean orderTestsReserved (String sampleName, String user) {
        String query = "SELECT properties->>'ReservedBy' AS reserved_by FROM cora.order_tests WHERE sample_name = '%s';";
        List <Map <String, Object>> queryResult = coraDb.executeSelect (format (query,
                                                                                sampleName));
        assertFalse (queryResult.isEmpty (), "Order test not found");
        return String.valueOf (queryResult.get (0).get ("reserved_by")).equals (user) ? true : false;
    }
}
