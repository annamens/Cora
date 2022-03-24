package com.adaptivebiotech.cora.ui.order;

import com.adaptivebiotech.cora.dto.Patient;

import static org.testng.Assert.assertTrue;

public class PatientNewOrderTDetect extends PatientNewOrder{

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
}
