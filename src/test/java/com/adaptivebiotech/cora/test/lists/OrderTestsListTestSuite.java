/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.lists;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.order.ReservationModule;

/**
 * @author Spencer Fisco
 *         <a href="mailto:sfisco@adaptivebiotech.com">sfisco@adaptivebiotech.com</a>
 */
@Test (groups = { "regression", "jack-russell" })
public class OrderTestsListTestSuite extends CoraBaseBrowser {

    private ReservationModule reservationModule = new ReservationModule ();
    private OrdersList        ordersList        = new OrdersList ();

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod () {
        new Login ().doLogin ();
        ordersList.isCorrectPage ();
        ordersList.goToOrderTests ();
    }

    /**
     * NOTE: TEST PLACEHOLDER
     * 
     * @sdlc.requirements REQ PLACEHOLDER
     */
    // cannot assume there will be order tests by default
    // what search can assure we'll always get order tests?
    // can search for an existing patient
    public void reservationUI () {
        assertTrue (reservationModule.manageReservationsButtonDisplayed ());
        reservationModule.clickManageReservations ();
        assertTrue (reservationModule.reserveAndOpenButtonDisplayed ());
        assertTrue (reservationModule.reserveButtonDisplayed ());
        assertTrue (reservationModule.removeReservationButtonDisplayed ());
        assertTrue (reservationModule.doneButtonDisplayed ());
        assertTrue (reservationModule.reserveCheckboxesDisplayed ());
        reservationModule.selectCheckbox (0);
        reservationModule.selectCheckbox (1);
        assertTrue (reservationModule.rowIsSelected (0));
        assertTrue (reservationModule.rowIsSelected (1));
        reservationModule.clickDone ();
        assertTrue (reservationModule.manageReservationsButtonDisplayed ());
        assertFalse (reservationModule.reserveAndOpenButtonDisplayed ());
        assertFalse (reservationModule.reserveButtonDisplayed ());
        assertFalse (reservationModule.removeReservationButtonDisplayed ());
        assertFalse (reservationModule.doneButtonDisplayed ());
        assertFalse (reservationModule.reserveCheckboxesDisplayed ());
        reservationModule.clickManageReservations ();
        assertFalse (reservationModule.rowIsSelected (0));
        assertFalse (reservationModule.rowIsSelected (1));
    }
}
