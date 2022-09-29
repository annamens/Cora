/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.order;

import static com.adaptivebiotech.cora.test.CoraEnvironment.coraCSAdminTestPass;
import static com.adaptivebiotech.cora.test.CoraEnvironment.coraCSAdminTestUser;
import static com.adaptivebiotech.cora.test.CoraEnvironment.coraNonPHITestPass;
import static com.adaptivebiotech.cora.test.CoraEnvironment.coraNonPHITestUser;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
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

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod () {
        login.doLogin ();
        ordersList.isCorrectPage ();
        orderTestsList.doOrderTestSearch (PhysicianType.big_shot.accountName);
    }

    /**
     * NOTE: SR-T4319
     * 
     * @sdlc.requirements SR-8336:R3, SR-8336:R13, SR-8336:R26
     */
    public void orderTestsListReservationUI () {
        assertTrue (reservationModule.manageReservationsButtonDisplayed ());
        assertFalse (reservationModule.reserveButtonDisplayed ());
        assertFalse (reservationModule.doneButtonDisplayed ());
        assertFalse (reservationModule.reserveCheckboxesDisplayed ());
        testLog ("Reservation ui was hidden by default");
        reservationModule.clickManageReservations ();
        assertFalse (reservationModule.manageReservationsButtonDisplayed ());
        assertTrue (reservationModule.reserveButtonDisplayed ());
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
        reservationModule.clickDone ();
        assertTrue (reservationModule.manageReservationsButtonDisplayed ());
        assertFalse (reservationModule.reserveButtonDisplayed ());
        assertFalse (reservationModule.doneButtonDisplayed ());
        assertFalse (reservationModule.reserveCheckboxesDisplayed ());
        testLog ("Reservation ui was hidden after clicking done");
        reservationModule.clickManageReservations ();
        assertFalse (reservationModule.rowIsSelected (1));
        assertFalse (reservationModule.rowIsSelected (2));
        testLog ("Closing and opening reservation ui deselected order tests");
    }

    /**
     * NOTE: SR-T4337
     * 
     * @sdlc.requirements SR-8336:R2
     */
    public void orderTestsListPermissions () {
        assertTrue (reservationModule.manageReservationsButtonDisplayed ());
        testLog ("Reservation ui was available for Clinical Laboratory Directors");
        orderTestsList.clickSignOut ();
        login.doLogin (coraCSAdminTestUser, coraCSAdminTestPass);
        ordersList.isCorrectPage ();
        ordersList.goToOrderTests ();
        orderTestsList.doOrderTestSearch (PhysicianType.big_shot.accountName);
        assertTrue (reservationModule.manageReservationsButtonDisplayed ());
        testLog ("Reservation ui was available for CS Admins");
        orderTestsList.clickSignOut ();
        login.doLogin (coraNonPHITestUser, coraNonPHITestPass);
        ordersList.isCorrectPage ();
        ordersList.goToOrderTests ();
        orderTestsList.doOrderTestSearch (PhysicianType.big_shot.accountName);
        assertFalse (reservationModule.manageReservationsButtonDisplayed ());
        testLog ("Reservation ui was not available for other user groups");
    }

}
