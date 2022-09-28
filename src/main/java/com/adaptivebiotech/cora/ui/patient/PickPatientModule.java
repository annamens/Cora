/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.ui.patient;

import static java.lang.String.join;
import static org.testng.Assert.assertTrue;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.ui.CoraPage;

public class PickPatientModule extends CoraPage {

    private final String birthDate = "#dateOfBirth";
    private final String firstrow  = ".ab-panel.matches .row:nth-child(1)";

    public void clickCreateNewPatient () {
        assertTrue (click ("#new-patient"));
        assertTrue (waitForElementInvisible (".ab-panel.matches"));
        assertTrue (isTextInElement (popupTitle, "Create New Patient"));
    }

    public void clickSave () {
        assertTrue (click ("//button[text()='Save']"));
        moduleLoading ();
    }

    public void clickCancel () {
        assertTrue (click ("//button[text()='Cancel']"));
    }

    public String getBirthDateErrorMessage () {
        return getText (join (" + ", birthDate, requiredMsg));
    }

    public boolean isBirthDateErrorVisible () {
        return isElementVisible (join (" + ", birthDate, requiredMsg));
    }

    public void fillPatientInfo (Patient patient) {
        String firstName = "#firstName";
        String middleName = "#middleName";
        String lastName = "#lastName";

        clear (firstName);
        assertTrue (setText (firstName, patient.firstName));
        clear (middleName);
        assertTrue (setText (middleName, patient.middleName));
        clear (lastName);
        assertTrue (setText (lastName, patient.lastName));
        assertTrue (setText (birthDate, patient.dateOfBirth));
        assertTrue (clickAndSelectText ("#gender", patient.gender));
        if (patient.race != null) {
            assertTrue (clickAndSelectText ("#race", patient.race.text));
        }
        if (patient.ethnicity != null) {
            assertTrue (clickAndSelectText ("#ethnicity", patient.ethnicity.text));
        }
    }

    public void createNewPatient (Patient patient) {
        clickCreateNewPatient ();
        fillPatientInfo (patient);
        clickSave ();
        enterPatientMrn (patient.mrn);
    }

    public void searchPatient (Patient patient) {
        assertTrue (setText ("#patient-firstname", patient.firstName));
        assertTrue (setText ("#patient-lastname", patient.lastName));
        assertTrue (setText ("#patient-dateofbirth", patient.dateOfBirth));
        assertTrue (click ("#patient-search"));
        pageLoading ();
    }

    public void searchPatientWithLastName (Patient patient) {
        assertTrue (setText ("#patient-lastname", patient.lastName));
        assertTrue (click ("#patient-search"));
        pageLoading ();
    }

    public boolean isPickPatientRowPresent () {
        return isElementVisible (".pick-patient-row");
    }

    public void clickRemovePatient () {
        assertTrue (click ("//*[text()='Remove Patient']"));
    }

    public boolean isNoPatientsFound () {
        return isElementVisible ("//*[text()='No patients found.']");
    }

    public void clickSelectPatient () {
        assertTrue (click (firstrow));
        assertTrue (click ("#select-patient"));
        pageLoading ();

    }

    public String getFirstRowPatient () {
        return getText (".ab-panel.matches .row:nth-child(1)");
    }

    public boolean searchOrCreatePatient (Patient patient) {
        searchPatient (patient);

        boolean matchFound = false;
        if (getText (firstrow).matches ("No patients found."))
            createNewPatient (patient);
        else {
            assertTrue (click (firstrow));
            assertTrue (click ("#select-patient"));
            pageLoading ();
            matchFound = true;
            enterPatientMrn (patient.mrn);
        }
        return matchFound;
    }

    public void enterPatientMrn (String mrn) {
        assertTrue (setText ("[formcontrolname='mrn']", mrn));
    }

    public boolean isDuplicateFoundPopUpVisible () {
        return isElementVisible (join (" ", ".findDialog", popupTitle));
    }

    public void clickContinueAndSaveAnyway () {
        assertTrue (click ("//button[text()='Continue and Save Anyway']"));
    }

}
