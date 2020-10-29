package com.adaptivebiotech.cora.ui.patient;

import static org.testng.Assert.assertTrue;
import com.adaptivebiotech.cora.ui.CoraPage;

public class EditPatientDemographics extends CoraPage {

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

}
