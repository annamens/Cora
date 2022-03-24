package com.adaptivebiotech.cora.ui.order;

import com.adaptivebiotech.cora.dto.Patient;

import static org.testng.Assert.assertTrue;

public class PatientNewOrderClonoSeq extends PatientNewOrder{

    public void fillPatientInfo (Patient patient) {
        clear("[name='firstName']");
        assertTrue (setText ("[name='firstName']", patient.firstName));
        clear("[name='middleName']");
        assertTrue (setText ("[name='middleName']", patient.middleName));
        clear("[name='lastName']");
        assertTrue (setText ("[name='lastName']", patient.lastName));
        assertTrue (setText ("[name='dateOfBirth']", patient.dateOfBirth));
        assertTrue (clickAndSelectValue ("[name='gender']", "string:" + patient.gender));
    }
}
