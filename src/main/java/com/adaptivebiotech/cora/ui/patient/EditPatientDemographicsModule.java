package com.adaptivebiotech.cora.ui.patient;

import static org.testng.Assert.assertTrue;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.ui.CoraPage;
import com.adaptivebiotech.cora.utils.PageHelper.Ethnicity;
import com.adaptivebiotech.cora.utils.PageHelper.Race;
import com.adaptivebiotech.test.utils.TestHelper;

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

    // when this modal appears the fields initially are empty, we need to wait until there is data
    // in the fields
    public void waitForDataFieldsToLoad (Patient patient) {

        DateTimeFormatter inputFormatter = TestHelper.formatDt1;
        DateTimeFormatter outputFormatter = TestHelper.formatDt2;

        LocalDate dateOfBirthDate = LocalDate.parse (patient.dateOfBirth, inputFormatter);
        String expectedDateOfBirth = dateOfBirthDate.format (outputFormatter);

        waitForAttrContains (waitForElement (firstName), "value", patient.firstName);
        waitForAttrContains (waitForElement (lastName), "value", patient.lastName);
        waitForAttrContains (waitForElement (dateOfBirth), "value", expectedDateOfBirth);
    }

}
