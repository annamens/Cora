package com.adaptivebiotech.cora.ui.patient;

import static org.testng.Assert.assertTrue;
import com.adaptivebiotech.ui.cora.CoraPage;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class PatientDetail extends CoraPage {

    public PatientDetail () {
        staticNavBarHeight = 200;
    }

    @Override
    public void isCorrectPage () {
        assertTrue (isTextInElement ("[role='tablist'] .active a", "PATIENT DETAILS"));
    }
    
    public String getFirstName () {
        return getText ("[label='First Name']").replace ("First Name", "").trim ();
    }
    
    public String getLastName () {
        return getText ("[label='Last Name']").replace ("Last Name", "").trim ();
    }
}
