package com.adaptivebiotech.test.cora.smoke;

import static com.adaptivebiotech.test.utils.Logging.testLog;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.common.dto.Physician;
import com.adaptivebiotech.ui.cora.CoraPage;
import com.adaptivebiotech.ui.cora.order.Diagnostic;

@Test (groups = { "smoke" })
public class SmokeTestSuite extends SmokeTestBase {
    private CoraPage main;

    @BeforeMethod
    public void beforeMethod () {
        main = new CoraPage ();
        testLog ("Login Cora");
    }

    // SR-T1960
    public void SaveNewDiagnosticOrder () {
        diagnostic = new Diagnostic ();
        Physician physician = new Physician ();
        physician.firstName = "";
        physician.lastName = "UVT-Physician";
        physician.accountName = "";
        physician.providerFullName = "";
        physician.allowInternalOrderUpload = true;

        main.clickNewDiagnosticOrder ();
        testLog ("Create new diagnostic order");
        diagnostic.selectPhysician (physician);
        testLog ("Search and select physician");
        verifyPhysicianName ("Matt UVT-Physician");
        diagnostic.clickSave ();
        testLog ("Save the diagnostic order");
        verifyOrderNumber ();
    }

}
