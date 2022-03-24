package com.adaptivebiotech.cora.ui.order;

import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.ui.CoraPage;

import java.util.List;

import static org.testng.Assert.assertTrue;

public abstract class PatientNewOrder extends CoraPage {

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

    public abstract void fillPatientInfo(Patient patient);
}
