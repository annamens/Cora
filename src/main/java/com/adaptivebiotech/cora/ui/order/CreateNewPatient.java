package com.adaptivebiotech.cora.ui.order;

import static org.testng.Assert.assertTrue;
import java.util.List;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.ui.CoraPage;

public class CreateNewPatient extends CoraPage {

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

    public String getErrorMessage () {
        try {
            List <String> error = getTextList ("//span[@class='text-danger']");
            return String.join (" ", error);
        } catch (Exception e) {
            return "";
        }
    }
}
