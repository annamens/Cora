package com.adaptivebiotech.cora.ui.patient;

import static org.testng.Assert.assertTrue;
import com.adaptivebiotech.cora.ui.CoraPage;
import com.adaptivebiotech.cora.utils.PageHelper.Ethnicity;
import com.adaptivebiotech.cora.utils.PageHelper.Race;


public class EditPatientDemographicsModule extends CoraPage {

    @Override
    public void isCorrectPage () {
        String modalTitle = ".modal-title";
        assertTrue (isTextInElement (modalTitle, "Edit Patient Demographics"));
    }

    public void enterDateOfBirth (String dateOfBirth) {
        String dateOfBirthFieldId = "#dateOfBirth";
        assertTrue (setText (dateOfBirthFieldId, dateOfBirth));
    }

    public void clickSave () {
        assertTrue (click ("[type='submit']"));
        pageLoading ();
    }
    
    public void selectRace (Race race) {
        String css = "#race";
        assertTrue (clickAndSelectText (css, race.text));
    }

    public void selectEthnicity (Ethnicity ethnicity) {
        String css = "#ethnicity";
        assertTrue (clickAndSelectText (css, ethnicity.text));
    }

}
