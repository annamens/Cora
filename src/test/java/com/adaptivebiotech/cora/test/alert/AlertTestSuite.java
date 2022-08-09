package com.adaptivebiotech.cora.test.alert;

import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.clonoSEQ_client;
import static com.adaptivebiotech.cora.utils.TestHelper.scenarioBuilderPatient;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.buildDiagnosticOrder;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.stage;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestUrl;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.SecondaryAnalysis;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Ready;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.adaptivebiotech.cora.api.CoraApi;
import com.adaptivebiotech.cora.dto.Diagnostic;
import com.adaptivebiotech.cora.dto.HttpResponse;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.dto.Physician;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.ui.CoraPage;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.OrderAlert;

@Test (groups = "wparsons")
public class AlertTestSuite extends CoraBaseBrowser {
    private Login        login      = new Login ();
    private CoraApi      coraApi    = new CoraApi ();
    private Physician    physician;
    private final String bcellIdTsv = azTsvPath + "/above-loq.id.tsv.gz";
    private CoraPage     coraPage   = new CoraPage ();
    private OrderAlert   orderAlert = new OrderAlert ();

    public void verifyEmailNotificationsUnchecked () throws InterruptedException {
        login.doLogin ();
        // Find order or make new one
        coraApi.getAuthToken ();
        coraApi.addTokenAndUsername ();
        physician = coraApi.getPhysician (clonoSEQ_client);
        Patient patient = scenarioBuilderPatient ();
        Diagnostic diagnostic = buildDiagnosticOrder (physician,
                                                      patient,
                                                      stage (SecondaryAnalysis, Ready),
                                                      genCDxTest (ID_BCell2_CLIA, bcellIdTsv));
        HttpResponse response = coraApi.newBcellOrder (diagnostic);
        assertEquals (response.patientId, patient.id);
        testLog ("submitted new BCell ID order");
        coraPage.gotoOrderEntry (response.orderId);
        // Open Alerts Box
        assertTrue (orderAlert.click (".new-alert"));
        // Add Letter of Medical Necessity
        assertTrue (orderAlert.click ("//select[@class='form-control ng-untouched ng-pristine ng-valid']"));
        assertTrue (orderAlert.click ("//option[@value='2: Object']"));
        // Check boxes are marked as unchecked
        while (!orderAlert.isElementPresent ("//input[@class='recipient-email ng-untouched ng-pristine ng-valid']")) {}
        assertTrue (!orderAlert.isElementPresent ("//input[@ng-reflect-model='true']"));
        assertTrue (orderAlert.isElementPresent ("//input[@ng-reflect-model='false']"));
        // Save
        assertTrue (orderAlert.click ("//button[@class='btn btn-primary mar-right-10 mar-top-10']"));
        // Expand Letter of medical necessity
        assertTrue (orderAlert.click ("//span[@class='alert-expand glyphicon glyphicon-triangle-right']"));
        // Expand Send Email Notification
        assertTrue (orderAlert.click ("//span[@class='btn glyphicon alert-expand glyphicon-triangle-right']"));
        // Check boxes are marked as unchecked
        while (!orderAlert.isElementPresent ("//input[@class='recipient-email ng-untouched ng-pristine ng-valid']")) {}
        assertTrue (!orderAlert.isElementPresent ("//input[@ng-reflect-model='true']"));
        assertTrue (orderAlert.isElementPresent ("//input[@ng-reflect-model='false']"));
        assertTrue (orderAlert.click ("//button[@class='btn btn-secondary pull-right']"));
        // Add Pathology Report
        assertTrue (orderAlert.click (".new-alert"));
        assertTrue (orderAlert.click ("//button[@class='btn btn-primary resolve-alert-button']"));
        orderAlert.clickClose ();
        assertTrue (orderAlert.click (".new-alert"));
        assertTrue (orderAlert.click ("//select[@class='form-control ng-untouched ng-pristine ng-valid']"));
        assertTrue (orderAlert.click ("//option[@value='4: Object']"));
        // Check boxes are marked as unchecked
        while (!orderAlert.isElementPresent ("//input[@class='recipient-email ng-untouched ng-pristine ng-valid']")) {}
        assertTrue (!orderAlert.isElementPresent ("//input[@ng-reflect-model='true']"));
        assertTrue (orderAlert.isElementPresent ("//input[@ng-reflect-model='false']"));
        // Save
        assertTrue (orderAlert.click ("//button[@class='btn btn-primary mar-right-10 mar-top-10']"));
        // Expand Pathology Report needed
        assertTrue (orderAlert.click ("//span[@class='alert-expand glyphicon glyphicon-triangle-right']"));
        // Expand Send Email Notification section
        assertTrue (orderAlert.click ("//span[@class='btn glyphicon alert-expand glyphicon-triangle-right']"));
        // Check boxes are marked as unchecked
        while (!orderAlert.isElementPresent ("//input[@class='recipient-email ng-untouched ng-pristine ng-valid']")) {}
        assertTrue (!orderAlert.isElementPresent ("//input[@ng-reflect-model='true']"));
        assertTrue (orderAlert.isElementPresent ("//input[@ng-reflect-model='false']"));
        assertTrue (orderAlert.click ("//button[@class='btn btn-secondary pull-right']"));
        // Add Corrected Report
        assertTrue (orderAlert.click (".new-alert"));
        assertTrue (orderAlert.click ("//button[@class='btn btn-primary resolve-alert-button']"));
        orderAlert.clickClose ();
        assertTrue (orderAlert.click (".new-alert"));
        assertTrue (orderAlert.click ("//select[@class='form-control ng-untouched ng-pristine ng-valid']"));
        assertTrue (orderAlert.click ("//option[@value='3: Object']"));
        // Check boxes are marked as unchecked
        while (!orderAlert.isElementPresent ("//input[@class='recipient-email ng-untouched ng-pristine ng-valid']")) {}
        assertTrue (!orderAlert.isElementPresent ("//input[@ng-reflect-model='true']"));
        assertTrue (orderAlert.isElementPresent ("//input[@ng-reflect-model='false']"));
        // Save
        assertTrue (orderAlert.click ("//button[@class='btn btn-primary mar-right-10 mar-top-10']"));
        // Expand Corrected Report
        assertTrue (orderAlert.click ("//span[@class='alert-expand glyphicon glyphicon-triangle-right']"));
        // Expand Send Email Notification section
        assertTrue (orderAlert.click ("//span[@class='btn glyphicon alert-expand glyphicon-triangle-right']"));
        // Check boxes are marked as unchecked
        while (!orderAlert.isElementPresent ("//input[@class='recipient-email ng-untouched ng-pristine ng-valid']")) {}
        assertTrue (!orderAlert.isElementPresent ("//input[@ng-reflect-model='true']"));
        assertTrue (orderAlert.isElementPresent ("//input[@ng-reflect-model='false']"));
        assertTrue (orderAlert.click ("//button[@class='btn btn-secondary pull-right']"));
        // Add Clinical Consultation
        assertTrue (orderAlert.click (".new-alert"));
        assertTrue (orderAlert.click ("//button[@class='btn btn-primary resolve-alert-button']"));
        orderAlert.clickClose ();
        assertTrue (orderAlert.click (".new-alert"));
        assertTrue (orderAlert.click ("//select[@class='form-control ng-untouched ng-pristine ng-valid']"));
        assertTrue (orderAlert.click ("//option[@value='7: Object']"));
        // Check boxes are marked as unchecked
        while (!orderAlert.isElementPresent ("//input[@class='recipient-email ng-untouched ng-pristine ng-valid']")) {}
        assertTrue (!orderAlert.isElementPresent ("//input[@ng-reflect-model='true']"));
        assertTrue (orderAlert.isElementPresent ("//input[@ng-reflect-model='false']"));
        // Save
        assertTrue (orderAlert.click ("//button[@class='btn btn-primary mar-right-10 mar-top-10']"));
        // Expand Clinical Consultation
        assertTrue (orderAlert.click ("//span[@class='alert-expand glyphicon glyphicon-triangle-right']"));
        // Expand Send Email Notification section
        assertTrue (orderAlert.click ("//span[@class='btn glyphicon alert-expand glyphicon-triangle-right']"));
        // Check boxes are marked as unchecked
        while (!orderAlert.isElementPresent ("//input[@class='recipient-email ng-untouched ng-pristine ng-valid']")) {}
        assertTrue (!orderAlert.isElementPresent ("//input[@ng-reflect-model='true']"));
        assertTrue (orderAlert.isElementPresent ("//input[@ng-reflect-model='false']"));
        assertTrue (orderAlert.click ("//button[@class='btn btn-secondary pull-right']"));
    }
}
