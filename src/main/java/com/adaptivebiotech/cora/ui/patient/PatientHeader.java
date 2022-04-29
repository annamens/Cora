/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.ui.patient;

import static java.lang.String.format;
import static org.testng.Assert.assertTrue;
import com.adaptivebiotech.cora.ui.CoraPage;

/**
 * @author jpatel
 *
 */
public class PatientHeader extends CoraPage {

    public void clickClose () {
        assertTrue (click ("go-back"));
        pageLoading ();
    }

    public void clickPatientDetailsTab () {
        assertTrue (click (format (tabBase, "Patient Details")));
    }

    public void clickPatientOrderHistoryTab () {
        assertTrue (click (format (tabBase, "Patient Order History")));
    }
}
