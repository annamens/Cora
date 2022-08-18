/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.alert;

import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.clonoSEQ_client;
import static com.adaptivebiotech.cora.utils.TestHelper.scenarioBuilderPatient;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.buildDiagnosticOrder;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.stage;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Ready;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Diagnostic;
import com.adaptivebiotech.cora.dto.HttpResponse;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.dto.Physician;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.ui.CoraPage;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.OrderAlert;

/**
 * @author Mitch Parsons
 *         <a href="mailto:<wparsons@adaptivebiotech.com">wparsons@adaptivebiotech.com</a>
 */
@Test (groups = "regression")
public class AlertTestSuite extends CoraBaseBrowser {
    private Login      login      = new Login ();
    private Physician  physician;
    private CoraPage   coraPage   = new CoraPage ();
    private OrderAlert orderAlert = new OrderAlert ();

    public void verifyEmailNotificationsUnchecked () {
        login.doLogin ();
        // Find order or make new one
        physician = coraApi.getPhysician (clonoSEQ_client);
        Patient patient = scenarioBuilderPatient ();
        Diagnostic diagnostic = buildDiagnosticOrder (physician,
                                                      patient,
                                                      stage (null, null),
                                                      genCDxTest (ID_BCell2_CLIA, null));
        HttpResponse response = coraApi.newBcellOrder (diagnostic);
        assertEquals (response.patientId, patient.id);
        testLog ("submitted new BCell ID order");
        coraPage.gotoOrderEntry (response.orderId);
        // Open Alerts Box
        // Add Letter of Medical Necessity
        orderAlert.addLetterOfMedicalNecessity ();
        // Check boxes are marked as unchecked
        assertTrue (orderAlert.noBoxesChecked ());
        // Save
        orderAlert.clickSaveNewAlert ();
        // Expand Letter of medical necessity
        orderAlert.expandTopAlert ();
        // Expand Send Email Notification
        orderAlert.expandEmailsFromTopAlert ();
        // Check boxes are marked as unchecked
        assertTrue (orderAlert.noBoxesChecked ());
        orderAlert.closeExpandedAlert ();
        // Add Pathology Report
        orderAlert.resolveTopAlert ();
        orderAlert.addPathologyReport ();
        // Check boxes are marked as unchecked
        assertTrue (orderAlert.noBoxesChecked ());
        // Save
        orderAlert.clickSaveNewAlert ();
        // Expand Pathology Report needed
        orderAlert.expandTopAlert ();
        // Expand Send Email Notification section
        orderAlert.expandEmailsFromTopAlert ();
        // Check boxes are marked as unchecked
        assertTrue (orderAlert.noBoxesChecked ());
        orderAlert.closeExpandedAlert ();
        // Add Corrected Report
        orderAlert.resolveTopAlert ();
        orderAlert.addCorrectedReport ();
        // Check boxes are marked as unchecked
        assertTrue (orderAlert.noBoxesChecked ());
        // Save
        orderAlert.clickSaveNewAlert ();
        // Expand Corrected Report
        orderAlert.expandTopAlert ();
        // Expand Send Email Notification section
        orderAlert.expandEmailsFromTopAlert ();
        // Check boxes are marked as unchecked
        assertTrue (orderAlert.noBoxesChecked ());
        orderAlert.closeExpandedAlert ();
        // Add Clinical Consultation
        orderAlert.resolveTopAlert ();
        orderAlert.addClinicalConsultationOption ();
        // Check boxes are marked as unchecked
        assertTrue (orderAlert.noBoxesChecked ());
        // Save
        orderAlert.clickSaveNewAlert ();
        // Expand Clinical Consultation
        orderAlert.expandTopAlert ();
        // Expand Send Email Notification section
        orderAlert.expandEmailsFromTopAlert ();
        // Check boxes are marked as unchecked
        assertTrue (orderAlert.noBoxesChecked ());
        orderAlert.closeExpandedAlert ();
        // Resolve Alert
        orderAlert.resolveTopAlert ();
    }
}
