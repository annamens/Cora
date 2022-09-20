/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.ui.order.reservation;

import static java.lang.String.format;
import static org.testng.Assert.assertTrue;
import com.seleniumfy.test.utils.BasePage;

/**
 * @author Spencer Fisco
 *         <a href="mailto:sfisco@adaptivebiotech.com">sfisco@adaptivebiotech.com</a>
 */
public class ReservationModuleBase extends BasePage {

    private final String manageReservationsButton = "//button[text()='Manage Reservations']";
    private final String reserveButton            = ".btn-reservation-action";
    private final String doneButton               = ".btn-reservation-done";
    private final String checkbox                 = "tr:nth-of-type(%s) td input[type='checkbox']";

    public boolean manageReservationsButtonDisplayed () {
        return isElementPresent (manageReservationsButton);
    }

    public boolean reserveButtonDisplayed () {
        return isElementPresent (reserveButton);
    }

    public boolean doneButtonDisplayed () {
        return isElementPresent (doneButton);
    }

    public void clickManageReservations () {
        assertTrue (click (manageReservationsButton));
    }

    public boolean reserveCheckboxesDisplayed () {
        return isElementPresent (format (checkbox, 1));
    }

    public void selectCheckbox (int row) {
        assertTrue (click (format (checkbox, row)));
    }

    public boolean rowIsSelected (int row) {
        return (findElement (format (checkbox, row)).isSelected ());
    }

    public void clickDone () {
        assertTrue (click (doneButton));
    }
}
