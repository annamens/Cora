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
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Physician.PhysicianType;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.order.reservation.ReservationModule;

/**
 * @author Spencer Fisco
 *         <a href="mailto:sfisco@adaptivebiotech.com">sfisco@adaptivebiotech.com</a>
 */
@Test (groups = { "regression", "irish-wolfhound" })
public class OrderTestReservationTestSuite extends OrderTestBase {
    private ReservationModule reservationModule = new ReservationModule ();
    private OrdersList        ordersList        = new OrdersList ();
    private Login             login             = new Login ();

    /**
     * NOTE: SR-T4319
     * 
     * @sdlc.requirements SR-8336:R3, SR-8336:R4, SR-8336:R26
     */
    public void orderTestsListReservationUI () {
        login.doLogin ();
        ordersList.isCorrectPage ();
        ordersList.doOrderTestSearch (PhysicianType.big_shot.accountName);
        assertTrue (reservationModule.isManageReservationsButtonVisible ());
        assertFalse (reservationModule.isReserveButtonVisible ());
        assertFalse (reservationModule.isRemoveReservationButtonVisible ());
        assertFalse (reservationModule.isDoneButtonVisible ());
        assertFalse (reservationModule.isReserveCheckboxVisible ());
        testLog ("Reservation ui was hidden by default");
        reservationModule.clickManageReservations ();
        assertFalse (reservationModule.isManageReservationsButtonVisible ());
        assertTrue (reservationModule.isReserveButtonVisible ());
        assertTrue (reservationModule.isRemoveReservationButtonVisible ());
        assertTrue (reservationModule.isDoneButtonVisible ());
        assertTrue (reservationModule.isReserveCheckboxVisible ());
        testLog ("Reservation ui was displayed after clicking button");
        reservationModule.selectCheckbox (1);
        reservationModule.selectCheckbox (2);
        assertTrue (reservationModule.isRowSelected (1));
        assertTrue (reservationModule.isRowSelected (2));
        reservationModule.selectCheckbox (1);
        assertFalse (reservationModule.isRowSelected (1));
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
        String sampleName = "104730-SN-4454";
        clearReservationProperties (sampleName);
        login.doLogin ();
        ordersList.isCorrectPage ();
        ordersList.doOrderTestSearch (sampleName);
        reservationModule.clickManageReservations ();
        reservationModule.selectCheckbox (1);
        reservationModule.clickDone ();
        assertFalse (orderTestsReserved (sampleName, coraTestUser));
        testLog ("Clicking Done with order test selected did not reserve");
        assertTrue (reservationModule.isManageReservationsButtonVisible ());
        assertFalse (reservationModule.isReserveButtonVisible ());
        assertFalse (reservationModule.isRemoveReservationButtonVisible ());
        assertFalse (reservationModule.isDoneButtonVisible ());
        assertFalse (reservationModule.isReserveCheckboxVisible ());
        testLog ("Reservation ui was hidden after clicking done");
        reservationModule.clickManageReservations ();
        assertFalse (reservationModule.isRowSelected (1));
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
        assertEquals (reservationModule.getToastMessage (), "2 reservation(s) removed.");
        testLog ("User was notified of the number of order tests reservations removed");
    }

    /**
     * NOTE: SR-T4358
     * 
     * @sdlc.requirements SR-8336:R10
     */
    public void reserveUnreserved () {
        String sampleName = "104204-SN-4145";
        clearReservationProperties (sampleName);
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
        String sampleName = "114744-SN-8680";
        setReservationProperties (sampleName, coraCSAdminTestUser);
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
        String sampleName = "128977-SN-14854";
        setReservationProperties (sampleName, coraTestUser);
        login.doLogin ();
        ordersList.isCorrectPage ();
        ordersList.doOrderTestSearch (sampleName);
        reservationModule.clickManageReservations ();
        reservationModule.selectCheckbox (1);
        reservationModule.clickRemoveReservation ();
        reservationModule.waitForToastMessage ();
        assertFalse (orderTestsReserved (sampleName, coraTestUser));
        testLog ("User's own order test reservation was removed");
    }

    /**
     * NOTE: SR-T4359
     * 
     * @sdlc.requirements SR-8336:R12
     */
    public void removeOtherReservation () {
        String sampleName = "115475-SN-8988";
        setReservationProperties (sampleName, coraCSAdminTestUser);
        login.doLogin ();
        ordersList.isCorrectPage ();
        ordersList.doOrderTestSearch (sampleName);
        reservationModule.clickManageReservations ();
        reservationModule.selectCheckbox (1);
        reservationModule.clickRemoveReservation ();
        reservationModule.waitForToastMessage ();
        assertFalse (orderTestsReserved (sampleName, coraTestUser));
        testLog ("Other user's order test reservation was removed");
    }

    private boolean userSeesReservationUI (String user, String pass) {
        login.doLogin (user, pass);
        ordersList.isCorrectPage ();
        ordersList.doOrderTestSearch (PhysicianType.big_shot.accountName);
        return reservationModule.isManageReservationsButtonVisible ();
    }

    private void clearReservationProperties (String sampleName) {
        String query = format ("update cora.order_tests set properties = properties - 'ReservedBy' - 'ReservedOn' where sample_name = '%s';",
                               sampleName);
        coraDb.executeUpdate (query);
    }

    private void setReservationProperties (String sampleName, String userName) {
        String query = format ("update cora.order_tests set properties = jsonb_set(jsonb_set(properties, '{ReservedBy}', '\"%s\"'), '{ReservedOn}', '\"2022-10-04T22:14:19.594\"') where sample_name = '%s';",
                               userName,
                               sampleName);
        coraDb.executeUpdate (query);
    }

    // will be replaced when reservation visible on front end
    private boolean orderTestsReserved (String sampleName, String user) {
        String query = "SELECT properties->>'ReservedBy' AS reserved_by FROM cora.order_tests WHERE sample_name = '%s';";
        List <Map <String, Object>> queryResult = coraDb.executeSelect (format (query, sampleName));
        assertFalse (queryResult.isEmpty (), "Order test not found");
        return String.valueOf (queryResult.get (0).get ("reserved_by")).equals (user) ? true : false;
    }
}
