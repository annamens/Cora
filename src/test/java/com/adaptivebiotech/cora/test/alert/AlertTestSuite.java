/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.alert;

import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.clonoSEQ_client;
import static com.adaptivebiotech.cora.utils.TestHelper.scenarioBuilderPatient;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.buildDiagnosticOrder;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Alerts.AlertOptions;
import com.adaptivebiotech.cora.dto.Diagnostic;
import com.adaptivebiotech.cora.dto.HttpResponse;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.dto.Physician;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.OrderAlert;

/**
 * @author Mitch Parsons
 *         <a href="mailto:wparsons@adaptivebiotech.com">wparsons@adaptivebiotech.com</a>
 */
@Test (groups = "regression")
public class AlertTestSuite extends CoraBaseBrowser {
    private Login      login      = new Login ();
    private OrderAlert orderAlert = new OrderAlert ();

    public void verifyEmailNotificationsUnchecked () {
        Physician physician = coraApi.getPhysician (clonoSEQ_client);
        Patient patient = scenarioBuilderPatient ();
        Diagnostic diagnostic = buildDiagnosticOrder (physician, patient, null, genCDxTest (ID_BCell2_CLIA, null));
        HttpResponse response = coraApi.newBcellOrder (diagnostic);
        assertEquals (response.patientId, patient.id);
        testLog ("submitted new BCell ID order");
        login.doLogin ();
        orderAlert.gotoOrderEntry (response.orderId);
        orderAlert.addAlert (AlertOptions.LetterOfMedicalNecessity);
        fullyCheckBoxes ();
        orderAlert.addAlert (AlertOptions.PathologyReport);
        fullyCheckBoxes ();
        orderAlert.addAlert (AlertOptions.CorrectedReport);
        fullyCheckBoxes ();
        orderAlert.addAlert (AlertOptions.ClinicalConsultation);
        fullyCheckBoxes ();
    }

    private void fullyCheckBoxes () {
        assertFalse (orderAlert.isAnyEmailBoxChecked ());
        orderAlert.clickSaveNewAlert ();
        orderAlert.expandTopAlert ();
        orderAlert.expandEmailsFromTopAlert ();
        assertFalse (orderAlert.isAnyEmailBoxChecked ());
        orderAlert.clickCloseButton ();
        orderAlert.resolveTopAlert ();
    }
}
