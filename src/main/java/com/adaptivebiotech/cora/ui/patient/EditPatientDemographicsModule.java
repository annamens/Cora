package com.adaptivebiotech.cora.ui.patient;

import static org.testng.Assert.assertTrue;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.ui.CoraPage;
import com.adaptivebiotech.cora.utils.PageHelper.Ethnicity;
import com.adaptivebiotech.cora.utils.PageHelper.Race;


public class EditPatientDemographicsModule extends CoraPage {

    private final String firstName   = "#firstName";
    private final String lastName    = "#lastName";
    private final String dateOfBirth = "#dateOfBirth";
    
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
    
    public String getFirstName () {
        return getText (firstName);
    }

    public String getLastName () {
        return getText (lastName);
    }

    public String getDateOfBirth () {
        return getText (dateOfBirth);
    }

    // when this modal appears the fields initially are empty, we need to wait until there is data
    // in the fields
    public void waitForDataFieldsToLoad (Patient patient) {

        DateFormat inputFormat = new SimpleDateFormat ("MM/dd/yyyy");
        DateFormat outputFormat = new SimpleDateFormat ("yyyy-MM-dd");
        String expectedDateOfBirth = "";
        try {
            Date dateOfBirthDate = inputFormat.parse (patient.dateOfBirth);
            expectedDateOfBirth = outputFormat.format (dateOfBirthDate);
        } catch (ParseException e) {
            e.printStackTrace ();
        }

        waitForAttrContains (waitForElement (firstName), "value", patient.firstName);
        waitForAttrContains (waitForElement (lastName), "value", patient.lastName);
        waitForAttrContains (waitForElement (dateOfBirth), "value", expectedDateOfBirth);
    }

}
