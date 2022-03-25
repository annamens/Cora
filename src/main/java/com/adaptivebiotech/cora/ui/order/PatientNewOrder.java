package com.adaptivebiotech.cora.ui.order;

import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.ui.CoraPage;

import java.util.List;

import static org.testng.Assert.assertTrue;

public class PatientNewOrder extends CoraPage {

    public void clickCreateNewPatient () {
        assertTrue (click ("#new-patient"));
        assertTrue (waitForElementInvisible (".ab-panel.matches"));
        assertTrue (isTextInElement (popupTitle, "Create New Patient"));
    }

    public void clickSave () {
        assertTrue (click ("//button[text()='Save']"));
    }

    public void clickCancel () {
        assertTrue (click ("//button[text()='Cancel']"));
    }

    public String getErrorMessage () {
        try {
            List<String> error = getTextList ("//span[@class='text-danger']");
            return String.join (" ", error);
        } catch (Exception e) {
            return "";
        }
    }

    public void fillPatientInfo (Patient patient) {
        clear("#firstName");
        assertTrue (setText ("#firstName", patient.firstName));
        clear("#middleName");
        assertTrue (setText ("#middleName", patient.middleName));
        clear("#lastName");
        assertTrue (setText ("#lastName", patient.lastName));
        assertTrue (setText ("#dateOfBirth", patient.dateOfBirth));
        assertTrue (clickAndSelectText ("#gender", patient.gender));
        if (patient.race != null) {
            assertTrue (clickAndSelectText ("#race", patient.race.text));
        }
        if (patient.ethnicity != null) {
            assertTrue (clickAndSelectText ("#ethnicity", patient.ethnicity.text));
        }
    }

    public void createNewPatient (Patient patient) {
        clickCreateNewPatient();
        fillPatientInfo(patient);
        clickSave();
        assertTrue (setText ("[name='mrn']", patient.mrn));
    }

    public void searchPatient (Patient patient) {
        assertTrue (setText ("#patient-firstname", patient.firstName));
        assertTrue (setText ("#patient-lastname", patient.lastName));
        assertTrue (setText ("#patient-dateofbirth", patient.dateOfBirth));
        assertTrue (setText ("#patient-mrn", patient.mrn));
        assertTrue (click ("#patient-search"));
        pageLoading ();
    }

    public boolean searchOrCreatePatient (Patient patient) {
        searchPatient (patient);

        boolean matchFound = false;
        String firstrow = ".ab-panel.matches .row:nth-child(1)";
        if (getText (firstrow).matches ("No patient(s)? found\\."))
            createNewPatient (patient);
        else {
            assertTrue (click (firstrow));
            assertTrue (click ("#select-patient"));
            moduleLoading ();
            matchFound = true;
        }
        return matchFound;
    }
}
