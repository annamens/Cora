/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.report.clonoseq;

import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_BCell2_IUO_CLIA;
import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_BCell2_IUO_FLEX;
import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_BCell2_IUO_IVD;
import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_BCell2_IVD;
import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_TCRB;
import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_TCRG;
import static com.adaptivebiotech.cora.dto.Orders.Assay.MRD_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Orders.OrderStatus.Cancelled;
import static com.adaptivebiotech.cora.utils.TestHelper.scenarioBuilderPatient;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.stage;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.ClonoSEQReport;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.NorthQC;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.SecondaryAnalysis;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Awaiting;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Ready;
import static com.adaptivebiotech.test.utils.PageHelper.StageSubstatus.CLINICAL_QC;
import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Alerts.Alert;
import com.adaptivebiotech.cora.dto.AssayResponse.CoraTest;
import com.adaptivebiotech.cora.dto.Diagnostic;
import com.adaptivebiotech.cora.dto.Orders.Assay;
import com.adaptivebiotech.cora.dto.Orders.OrderTest;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.test.report.ReportTestBase;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.debug.OrcaHistory;
import com.adaptivebiotech.cora.ui.order.OrderStatus;
import com.adaptivebiotech.cora.ui.order.ReportClonoSeq;
import com.adaptivebiotech.cora.utils.PageHelper.QC;
import com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty;

/**
 * @author Spencer Fisco
 *         <a href="mailto:sfisco@adaptivebiotech.com">sfisco@adaptivebiotech.com</a>
 */
@Test (groups = { "regression", "havanese" })
public class ClonoSEQAutoReleaseTestSuite extends ReportTestBase {

    private Login             login                      = new Login ();
    private OrcaHistory       history                    = new OrcaHistory ();
    private ReportClonoSeq    report                     = new ReportClonoSeq ();
    private OrderStatus       orderStatus                = new OrderStatus ();
    private final String      validICD                   = "C90.00";
    private final String      bCellIDTsv                 = azTsvPath + "/above-loq.id.tsv.gz";
    private final String      bCellMRDTsv                = azTsvPath + "/above-loq.mrd.tsv.gz";
    private final String      polyclonalTsv              = azTsvPath + "/polyclonal/polyclonal.id.tsv.gz";
    private final String      highIGHtsv                 = azTsvPath + "/many-clones/highIGH.id.tsv.gz";
    private final String      highIGKtsv                 = azTsvPath + "/many-clones/highIGK.id.tsv.gz";
    private final String      highIGLtsv                 = azTsvPath + "/many-clones/highIGL.id.tsv.gz";
    private final UUID        priorBcellPatient          = UUID.fromString ("85869a51-f272-42d4-9002-759eb0bebecc");
    private final UUID        priorCancelledBcellPatient = UUID.fromString ("8525cad4-52e6-411c-a9cc-50370d28a650");
    private final UUID        priorNoResultBcellPatient  = UUID.fromString ("bf31b885-0c53-425d-8f29-1f9f5c25161f");
    private final UUID        priorTcellPatient          = UUID.fromString ("f9a04e2c-e3fc-4c20-8574-f2fcd9d1f10b");
    private final List <UUID> patientsToCleanUp          = Arrays.asList (priorBcellPatient,
                                                                          priorCancelledBcellPatient,
                                                                          priorNoResultBcellPatient,
                                                                          priorTcellPatient);

    @BeforeClass (alwaysRun = true)
    public void beforeClass () {
        coraApi.addTokenAndUsername ();
        for (UUID id : patientsToCleanUp) {
            Arrays.stream (coraApi.getOrdersForPatient (id))
                  .filter (order -> !order.orderTestStatusType.equals (Cancelled))
                  .filter (order -> order.accountName.contains (SEAaccount))
                  .forEach (order -> coraApi.cancelWorkflow (order.workflowId));
        }
    }

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod () {
        login.doLogin ();
    }

    /**
     * @sdlc.requirements SR-9504:R1
     */
    public void ivdSKU () {
        OrderTest test = createBCellDiagnostic (scenarioBuilderPatient (),
                                                validICD,
                                                genCDxTest (ID_BCell2_IVD, bCellIDTsv)).findOrderTest (ID_BCell2_IVD);
        verifyAutoReleaseSuccess (test);
        testLog ("SR-9504:R1: B-cell 2.0 Clonality (IVD) autoreleased");
    }

    /**
     * @sdlc.requirements SR-9504:R1
     */
    public void cliaSKUs () {
        Diagnostic diagnostic = createBCellDiagnostic (scenarioBuilderPatient (),
                                                       validICD,
                                                       genCDxTest (ID_BCell2_CLIA, bCellIDTsv),
                                                       genCDxTest (MRD_BCell2_CLIA, bCellMRDTsv));
        verifyAutoReleaseSuccess (diagnostic.findOrderTest (ID_BCell2_CLIA));
        testLog ("SR-9504:R1: B-cell 2.0 Clonality (CLIA) autoreleased");
        verifyAutoReleaseFailure (diagnostic.findOrderTest (MRD_BCell2_CLIA),
                                  "Failed Auto Release Rules: Auto Release is not enabled for this SKU.");
        testLog ("SR-9504:R1: B-cell 2.0 Tracking (CLIA) failed autorelease");
    }

    /**
     * @sdlc.requirements SR-9504:R1
     */
    public void iuoCLIASKU () {
        OrderTest test = createBCellDiagnostic (scenarioBuilderPatient (),
                                                validICD,
                                                genCDxTest (ID_BCell2_IUO_CLIA,
                                                            bCellIDTsv)).findOrderTest (ID_BCell2_IUO_CLIA);
        verifyAutoReleaseSuccess (test);
        testLog ("SR-9504:R1: B-cell 2.0 Clonality (IUO CLIA-extract) autoreleased");
    }

    /**
     * @sdlc.requirements SR-9504:R1
     */
    public void iuoFlexSKU () {
        OrderTest test = createBCellDiagnostic (scenarioBuilderPatient (),
                                                validICD,
                                                genCDxTest (ID_BCell2_IUO_FLEX,
                                                            bCellIDTsv)).findOrderTest (ID_BCell2_IUO_FLEX);
        verifyAutoReleaseSuccess (test);
        testLog ("SR-9504:R1: B-cell 2.0 Clonality (IUO flex-extract) autoreleased");
    }

    /**
     * @sdlc.requirements SR-9504:R1
     */
    public void iuoIVDSKU () {
        OrderTest test = createBCellDiagnostic (scenarioBuilderPatient (),
                                                validICD,
                                                genCDxTest (ID_BCell2_IUO_IVD,
                                                            bCellIDTsv)).findOrderTest (ID_BCell2_IUO_IVD);
        verifyAutoReleaseSuccess (test);
        testLog ("SR-9504:R1: B-cell 2.0 Clonality (IUO IVD-extract) autoreleased");
    }

    /**
     * @sdlc.requirements SR-9504:R1
     */
    public void tCellSKU () {
        Diagnostic diagnostic = createTCellOrder ();
        verifyAutoReleaseFailure (diagnostic.findOrderTest (ID_TCRB),
                                  "Failed Auto Release Rules: Auto Release is not enabled for this SKU.");
        testLog ("SR-9504:R1: TCRB Clonality (CLIA) failed autorelease");
        verifyAutoReleaseFailure (diagnostic.findOrderTest (ID_TCRG),
                                  "Failed Auto Release Rules: Auto Release is not enabled for this SKU.");
        testLog ("SR-9504:R1: TCRG Clonality (CLIA) failed autorelease");
    }

    /**
     * @sdlc.requirements SR-9504:R3
     */
    public void insuranceAlert () {
        Patient patient = scenarioBuilderPatient ();
        OrderTest test = createBCellDiagnostic (patient,
                                                validICD,
                                                genCDxTest (ID_BCell2_CLIA, bCellIDTsv)).findOrderTest (ID_BCell2_CLIA);
        setPatientAlert ("insurance_anomaly", patient);
        verifyAutoReleaseSuccess (test);
        testLog ("SR-9504:R3: Report with insurance anomaly patient alert autoreleased");
    }

    /**
     * @sdlc.requirements SR-9504:R3
     */
    public void lomnAlert () {
        OrderTest test = createBCellOrderTest ();
        setOrderAlert ("lomn_required", test);
        verifyAutoReleaseSuccess (test);
        testLog ("SR-9504:R3: Report with LOMN required order alert autoreleased");
    }

    /**
     * @sdlc.requirements SR-9504:R2, SR-9504:R3
     */
    public void correctedReportAlert () {
        OrderTest test = createBCellOrderTest ();
        setOrderAlert ("corrected_report_ready", test);
        verifyAutoReleaseFailure (test,
                                  "Failed Auto Release Rules: HasNoAlerts expected AlertType to not contain one of [Risk Acknowledgement Letter,Corrected Report,Clinical Consultation,High Sharing,cfDNA report with prior gDNA,censored low uniqueness clones,Clonality (ID) Order Needed,Pathology report needed].");
        testLog ("SR-9504:R2,R3: Report with corrected report order alert failed autoreleased");
    }

    /**
     * @sdlc.requirements SR-9504:R4
     */
    public void reportNotes () {
        String reportNote = "testing report notes autorelease";
        OrderTest test = createBCellOrderTest ();
        history.gotoOrderDebug (test.sampleName);
        history.setWorkflowProperty (WorkflowProperty.clinicalReportNote, reportNote);
        verifyAutoReleaseFailure (test,
                                  "Failed Auto Release Rules: HasNoClinicalReportNotes expected clinicalReportNote to be blank.");
        testLog ("SR-9504:R4: Report with report notes failed autorelease");
    }

    /**
     * @sdlc.requirements SR-9504:R5
     */
    public void validIcdCodes () {
        String codes = "C90.00,C83.1,C82.9,C83.3,C90.1,C90.2,W61.62XD";
        OrderTest test = createBCellDiagnostic (scenarioBuilderPatient (),
                                                codes,
                                                genCDxTest (ID_BCell2_CLIA, bCellIDTsv)).findOrderTest (ID_BCell2_CLIA);
        verifyAutoReleaseSuccess (test);
        testLog ("SR-9504:R5: Report autoreleased for order containing the following ICD codes: " + codes);
    }

    /**
     * @sdlc.requirements SR-9504:R5
     */
    public void invalidIcdCodes () {
        String icdErrorMessage = "Failed Auto Release Rules: Rule IsAcceptedICDCodes expected Icd10Codes to include one of the following codes (Autorelease will fail if the list of idc codes contain a code starting with C that is not on the list): C90.0, C83.1, C82.9, C83.3, C90.1, C90.2.";
        String codes = "C90.0,C91.0";
        OrderTest test = createBCellDiagnostic (scenarioBuilderPatient (),
                                                codes,
                                                genCDxTest (ID_BCell2_CLIA, bCellIDTsv)).findOrderTest (ID_BCell2_CLIA);
        verifyAutoReleaseFailure (test, icdErrorMessage);
        testLog ("SR-9504:R5: Report failed autorelease for order containing the following ICD codes: " + codes);
    }

    /**
     * @sdlc.requirements SR-9504:R6
     */
    public void priorBCellID () {
        Patient patient = coraApi.getPatient (priorBcellPatient);
        OrderTest test = createBCellDiagnostic (patient,
                                                validICD,
                                                genCDxTest (ID_BCell2_CLIA, bCellIDTsv)).findOrderTest (ID_BCell2_CLIA);
        verifyAutoReleaseFailure (test,
                                  "Failed Auto Release Rules: HasNoCompletedClonalityTests expects BCell to be false.");
        testLog ("SR-9504:R6: Report failed autorelease for Bcell ID with prior completed Bcell ID");
    }

    /**
     * @sdlc.requirements SR-9504:R7
     */
    public void noResultBcellID () {
        OrderTest test = createBCellDiagnostic (scenarioBuilderPatient (),
                                                validICD,
                                                genCDxTest (ID_BCell2_CLIA, bCellIDTsv)).findOrderTest (ID_BCell2_CLIA);
        orderStatus.gotoOrderStatusPage (test.orderId);
        orderStatus.isCorrectPage ();
        orderStatus.failWorkflow (test.sampleName, "testing prior no result autorelease");
        verifyAutoReleaseFailure (test,
                                  "Failed Auto Release Rules: IsSentToFailureTarget expected wasSentToFailureTarget not to be true.");
        testLog ("SR-9504:R7: Report with no result failed autorelease");
    }

    /**
     * @sdlc.requirements SR-9504:R6
     */
    public void priorNoResultBCellID () {
        Patient patient = coraApi.getPatient (priorNoResultBcellPatient);
        OrderTest test = createBCellDiagnostic (patient,
                                                validICD,
                                                genCDxTest (ID_BCell2_CLIA, bCellIDTsv)).findOrderTest (ID_BCell2_CLIA);
        verifyAutoReleaseSuccess (test);
        testLog ("SR-9504:R6: Report autoreleased for Bcell ID with prior Bcell ID that had no result");
    }

    /**
     * @sdlc.requirements SR-9504:R6
     */
    public void priorCancelledBCellID () {
        Patient patient = coraApi.getPatient (priorCancelledBcellPatient);
        OrderTest test = createBCellDiagnostic (patient,
                                                validICD,
                                                genCDxTest (ID_BCell2_CLIA, bCellIDTsv)).findOrderTest (ID_BCell2_CLIA);
        verifyAutoReleaseSuccess (test);
        testLog ("SR-9504:R6: Report autoreleased for Bcell ID with prior cancelled Bcell ID");
    }

    /**
     * @sdlc.requirements SR-9504:R6
     */
    public void priorTcellID () {
        Patient patient = coraApi.getPatient (priorTcellPatient);
        OrderTest bCellTest = createBCellDiagnostic (patient,
                                                     validICD,
                                                     genCDxTest (ID_BCell2_CLIA,
                                                                 bCellIDTsv)).findOrderTest (ID_BCell2_CLIA);
        verifyAutoReleaseSuccess (bCellTest);
        testLog ("SR-9504:R6: Report autoreleased for Bcell ID with prior Tcell ID");
    }

    /**
     * @sdlc.requirements SR-9504:R7
     */
    public void polyclonal () {
        OrderTest polyclonalTest = createBCellDiagnostic (scenarioBuilderPatient (),
                                                          validICD,
                                                          genCDxTest (ID_BCell2_CLIA,
                                                                      polyclonalTsv)).findOrderTest (ID_BCell2_CLIA);
        verifyAutoReleaseFailure (polyclonalTest,
                                  "Failed Auto Release Rules: IsNotPolyClonal expects PolyClonal to be false.");
        testLog ("SR-9504:R7: Polyclonal report failed autorelease");
    }

    /**
     * @sdlc.requirements SR-9504:R8
     */
    public void ighClones () {
        String threshold = "4";
        OrderTest test = createBCellDiagnostic (scenarioBuilderPatient (),
                                                validICD,
                                                genCDxTest (ID_BCell2_CLIA, highIGHtsv)).findOrderTest (ID_BCell2_CLIA);
        verifyAutoReleaseFailure (test,
                                  format ("Failed Auto Release Rules: CheckIghIsBelowThreshold expected IGH to be less than %s.",
                                          threshold));
        testLog (format ("SR-9504:R8: Report with %s dominant IGH sequences failed autorelease",
                         threshold));
    }

    /**
     * @sdlc.requirements SR-9504:R8
     */
    public void igkClones () {
        String threshold = "5";
        OrderTest test = createBCellDiagnostic (scenarioBuilderPatient (),
                                                validICD,
                                                genCDxTest (ID_BCell2_CLIA, highIGKtsv)).findOrderTest (ID_BCell2_CLIA);
        verifyAutoReleaseFailure (test,
                                  format ("Failed Auto Release Rules: CheckIgkIsBelowThreshold expected IGK to be less than %s.",
                                          threshold));
        testLog (format ("SR-9504:R8: Report with %s dominant IGK sequences failed autorelease",
                         threshold));
    }

    /**
     * @sdlc.requirements SR-9504:R8
     */
    public void iglClones () {
        String threshold = "3";
        OrderTest test = createBCellDiagnostic (scenarioBuilderPatient (),
                                                validICD,
                                                genCDxTest (ID_BCell2_CLIA, highIGLtsv)).findOrderTest (ID_BCell2_CLIA);
        verifyAutoReleaseFailure (test,
                                  format ("Failed Auto Release Rules: CheckIglIsBelowThreshold expected IGL to be less than %s.",
                                          threshold));
        testLog (format ("SR-9504:R8: Report with %s dominant IGL sequences failed autorelease",
                         threshold));

    }

    private void waitForReportGeneration (OrderTest orderTest) {
        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);
        history.clickOrderTest ();
        Assay assay = Assay.getAssay (orderTest.test.name);
        report.clickReportTab (assay);
        if (orderTest.test.receptorFamily.equals ("TCell")) {
            report.generateReport (assay);
        }
    }

    private void verifyAutoReleaseSuccess (OrderTest orderTest) {
        waitForReportGeneration (orderTest);
        report.setQCstatus (QC.Pass);
        history.gotoOrderDebug (orderTest.sampleName);
        assertTrue (history.waitForWorkflowPropertySet (WorkflowProperty.AutoReleasedReport).AutoReleasedReport,
                    "Autorelease unexpectedly failed");
    }

    private void verifyAutoReleaseFailure (OrderTest orderTest, String failureMessage) {
        waitForReportGeneration (orderTest);
        report.setQCstatus (QC.Pass);
        history.gotoOrderDebug (orderTest.sampleName);
        assertFalse (history.waitForWorkflowPropertySet (WorkflowProperty.AutoReleasedReport).AutoReleasedReport,
                     "Autorelease unexpectedly succeeded");
        assertEquals (history.parseStatusHistory ().get (0).subStatusMessage,
                      failureMessage);
    }

    private void setPatientAlert (String alertTypeName, Patient patient) {
        Alert alert = new Alert ();
        alert.alertTypeName = alertTypeName;
        alert.referencedEntityId = patient.id;
        coraApi.setAlerts (alert);
        assertTrue (Arrays.asList (coraApi.getAlertsById (patient.id)).stream ()
                          .anyMatch (actualAlert -> actualAlert.alertType.name.equals (alertTypeName)));
    }

    private void setOrderAlert (String alertTypeName, OrderTest orderTest) {
        Alert alert = new Alert ();
        alert.alertTypeName = alertTypeName;
        alert.referencedEntityId = orderTest.orderId;
        coraApi.setAlerts (alert);
        assertTrue (Arrays.asList (coraApi.getAlertsById (orderTest.orderId)).stream ()
                          .anyMatch (actualAlert -> actualAlert.alertType.name.equals (alertTypeName)));
    }

    private OrderTest createBCellOrderTest () {
        return createBCellDiagnostic (scenarioBuilderPatient (),
                                      validICD,
                                      genCDxTest (ID_BCell2_CLIA, bCellIDTsv)).findOrderTest (ID_BCell2_CLIA);
    }

    private Diagnostic createBCellDiagnostic (Patient patient, String icdCodes, CoraTest... tests) {
        Diagnostic diagnostic = buildCdxOrder (patient,
                                               icdCodes,
                                               stage (SecondaryAnalysis, Ready),
                                               tests);
        assertEquals (coraApi.newBcellOrder (diagnostic).patientId, patient.id);
        return diagnostic;
    }

    private Diagnostic createTCellOrder () {
        String tsvPath = azTsvPath + "/HKJVGBGXC_0_CLINICAL-CLINICAL_68353-01MB.adap.txt.results.tsv.gz";
        String lastFlowcellId = "HKJVGBGXC";
        Patient patient = scenarioBuilderPatient ();
        Diagnostic diagnostic = buildCdxOrder (patient,
                                               stage (NorthQC, Ready),
                                               genTcrTest (ID_TCRB, lastFlowcellId, tsvPath),
                                               genTcrTest (ID_TCRG, lastFlowcellId, tsvPath));
        diagnostic.order.postToImmunoSEQ = true;
        assertEquals (coraApi.newTcellOrder (diagnostic).patientId, patient.id);
        return diagnostic;
    }

}
