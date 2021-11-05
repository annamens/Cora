package com.adaptivebiotech.cora.ui.patient;

import static org.testng.Assert.assertTrue;
import com.adaptivebiotech.cora.ui.CoraPage;

/**
 * @author jpatel
 *
 */
public class PatientHeader extends CoraPage {

    public PatientHeader () {
        staticNavBarHeight = 200;
    }

    public void clickClose () {
        String cssForCloseButton = "go-back";
        assertTrue (click (cssForCloseButton));
        moduleLoading ();
    }

}
