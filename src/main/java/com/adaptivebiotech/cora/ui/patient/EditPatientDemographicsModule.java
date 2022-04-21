package com.adaptivebiotech.cora.ui.patient;

import static org.testng.Assert.assertTrue;
import com.adaptivebiotech.cora.ui.CoraPage;
import com.adaptivebiotech.cora.utils.PageHelper.Ethnicity;
import com.adaptivebiotech.cora.utils.PageHelper.Race;

public class EditPatientDemographicsModule extends CoraPage {

    private final String firstName   = "#firstName";
    private final String lastName    = "#lastName";
    private final String dateOfBirth = "#dateOfBirth";

    @Override
    public void isCorrectPage () {
        assertTrue (isTextInElement (popupTitle, "Edit Patient Demographics"));
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

    public String getFirstName () {
        return getText (firstName);
    }

    public String getLastName () {
        return getText (lastName);
    }

    public String getDateOfBirth () {
        return getText (dateOfBirth);
    }
}
