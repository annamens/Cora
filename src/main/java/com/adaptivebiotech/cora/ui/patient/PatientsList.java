/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.ui.patient;

import static java.util.stream.Collectors.toList;
import static org.openqa.selenium.Keys.ENTER;
import static org.testng.Assert.assertTrue;
import java.util.List;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.ui.CoraPage;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class PatientsList extends CoraPage {

    public PatientsList () {
        staticNavBarHeight = 90;
    }

    @Override
    public void isCorrectPage () {
        assertTrue (waitUntilVisible (".active[title='Patients']"));
        pageLoading ();
    }

    public void searchPatient (String term) {
        assertTrue (setText ("[type='search']", term));
        assertTrue (pressKey (ENTER));
        pageLoading ();
    }

    public List <Patient> getPatients () {
        return waitForElements (".list-section tbody tr").stream ().map (tr -> {
            String[] tmp = getText (tr, "td:nth-child(2)").split (" ");
            Patient p = new Patient ();
            p.patientCode = Integer.valueOf (getText (tr, "td:nth-child(1)"));
            p.firstName = tmp[0];
            p.lastName = tmp[1];
            p.dateOfBirth = getText (tr, "td:nth-child(3)");
            return p;
        }).collect (toList ());
    }

    public void clickPatient (int idx) {
        String css = "patient-list tbody tr:nth-child(" + idx + ") td:nth-child(1) a";
        assertTrue (click (css));
        pageLoading ();
    }

    /**
     * @param term
     *            patient name (first last) or patient code
     */
    public void clickPatient (String term) {
        String css = "//*[contains (@class, 'list-section')]//*[text()='" + term + "']";
        assertTrue (click (css));
        pageLoading ();
    }

    /**
     * @param term
     *            patient name (first last) or patient code
     */
    public void clickPatientDetails (String term) {
        String details = "//*[*[*[text()='" + term + "']]]/following-sibling::td//*[contains (@class, 'history-link')]";
        assertTrue (click (details));
        pageLoading ();
    }

    public String getPatientMRDStatus () {
        String css = ".patient-status";
        return getText (css);
    }
}
