/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.ui.order;

import static java.lang.String.format;
import static org.testng.Assert.assertTrue;
import com.seleniumfy.test.utils.BasePage;

/**
 * @author Spencer Fisco
 *         <a href="mailto:sfisco@adaptivebiotech.com">sfisco@adaptivebiotech.com</a>
 */

// sounds like a DTO, should probably be renamed
public class ReservationModule extends BasePage {

    private final String manageReservationsButton = "//button[text()='Manage Reservations']";
    private final String reserveAndOpenButton     = "//button[text()='Reserve and Open']";
    private final String reserveButton            = "//button[text()='Reserve']";
    private final String removeReservationButton  = "//button[text()='Remove Reservation']";
    private final String doneButton               = "//button[text()='Open']";

    public boolean manageReservationsButtonDisplayed () {
        return isElementPresent (manageReservationsButton);
    }

    public boolean reserveAndOpenButtonDisplayed () {
        return isElementPresent (reserveAndOpenButton);
    }

    public boolean reserveButtonDisplayed () {
        return isElementPresent (reserveButton);
    }

    public boolean removeReservationButtonDisplayed () {
        return isElementPresent (removeReservationButton);
    }

    public boolean doneButtonDisplayed () {
        return isElementPresent (doneButton);
    }

    public void clickManageReservations () {
        assertTrue (click (manageReservationsButton));
    }

    public boolean reserveCheckboxesDisplayed () {
        return isElementPresent (".table.list-section input[type='checkbox']");
    }

    public void selectCheckbox (int row) {
        assertTrue (click (format (".table.list-section tr:nth-of-type(%s) input[type='checkbox']", row)));
    }

    public boolean rowIsSelected (int row) {
        // TBD
        return true;
    }

    public void clickDone () {
        assertTrue (click (doneButton));
    }

}
