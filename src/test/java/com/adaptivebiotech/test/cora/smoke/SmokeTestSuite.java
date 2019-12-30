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
        testLog ("Created new diagnostic order, diagnostic Order page displayed");
        diagnostic.selectPhysician (physician);
        testLog ("Searched and selected physician");
        verifyPhysicianName ("Matt UVT-Physician");
        testLog ("Verified physician name displayed in the Diagnostice Order page's Ordering Physician section");
        diagnostic.clickSave ();
        testLog ("Saved the diagnostic order");
        verifyOrderNumber ();
        testLog ("Diagnostic Order page displayed an order number, D-######, in the order header");
    }

}
