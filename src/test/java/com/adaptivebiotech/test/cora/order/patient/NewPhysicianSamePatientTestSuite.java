package com.adaptivebiotech.test.cora.order.patient;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.test.cora.order.OrderTestBase;
import com.adaptivebiotech.ui.cora.CoraPage;
import com.adaptivebiotech.ui.cora.order.Diagnostic;

@Test (groups = { "cora-seeding" })
public class NewPhysicianSamePatientTestSuite extends OrderTestBase {

    private Diagnostic diagnostic;

    @BeforeMethod
    public void beforeMethod () {
        new CoraPage ().clickNewDiagnosticOrder ();
        diagnostic = new Diagnostic ();
        diagnostic.isCorrectPage ();
        diagnostic.selectPhysician (physician1);
    }

    @AfterMethod
    public void afterMethod () {
        diagnostic.clickSave ();
    }

    public void medicare () {
        diagnostic.selectPatient (patientMedicare);
    }
}
