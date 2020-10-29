package com.adaptivebiotech.cora.ui.patient;

import com.adaptivebiotech.cora.ui.CoraPage;

public class PatientNotesModal extends CoraPage {

    public String getNotes () {
        String css = "[ng-bind=\"ctrl.patient.notes\"]";
        String text = readInput (css);
        return text;
    }
}
