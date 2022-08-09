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
import static com.adaptivebiotech.cora.utils.TestHelper.scenarioBuilderPatient;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.stage;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.ClonoSEQReport;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.NorthQC;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.SecondaryAnalysis;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Awaiting;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Finished;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Ready;
import static com.adaptivebiotech.test.utils.PageHelper.StageSubstatus.CLINICAL_CONSULTANT;
import static com.adaptivebiotech.test.utils.PageHelper.StageSubstatus.CLINICAL_QC;
import static com.adaptivebiotech.test.utils.PageHelper.StageSubstatus.RELEASED;
import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import java.util.Arrays;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Alerts.Alert;
import com.adaptivebiotech.cora.dto.AssayResponse.CoraTest;
import com.adaptivebiotech.cora.dto.Diagnostic;
import com.adaptivebiotech.cora.dto.Orders.Assay;
import com.adaptivebiotech.cora.dto.Orders.OrderTest;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.dto.Workflow.Stage;
import com.adaptivebiotech.cora.test.report.ReportTestBase;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.debug.OrcaHistory;
import com.adaptivebiotech.cora.ui.order.OrderDetail;
import com.adaptivebiotech.cora.ui.order.OrderStatus;
import com.adaptivebiotech.cora.ui.order.ReportClonoSeq;
import com.adaptivebiotech.cora.utils.PageHelper.QC;

/**
 * @author Spencer Fisco
 *         <a href="mailto:<sfisco@adaptivebiotech.com">sfisco@adaptivebiotech.com</a>
 */
@Test (groups = { "regression", "havanese" })
public class ClonoSEQAutoReleaseTestSuite extends ReportTestBase {

    private Login          login         = new Login ();
    private OrcaHistory    history       = new OrcaHistory ();
    private ReportClonoSeq report        = new ReportClonoSeq ();
    private OrderDetail    orderDetail   = new OrderDetail ();
    private OrderStatus    orderStatus   = new OrderStatus ();
    private final String   validICD      = "C90.00";
    private final String   bCellIDTsv    = azTsvPath + "/above-loq.id.tsv.gz";
    private final String   bCellMRDTsv   = azTsvPath + "/above-loq.mrd.tsv.gz";
    private final String   polyclonalTsv = azTsvPath + "/polyclonal/polyclonal.id.tsv.gz";
    private final String   highIGHtsv    = azTsvPath + "/many-clones/highIGH.id.tsv.gz";
    private final String   highIGKtsv    = azTsvPath + "/many-clones/highIGK.id.tsv.gz";
    private final String   highIGLtsv    = azTsvPath + "/many-clones/highIGL.id.tsv.gz";

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod () {
        login.doLogin ();
    }

    /**
     * @sdlc.requirements SR-9504:R1
     */
    public void bCellSKUs () {
        Patient patient = scenarioBuilderPatient ();
        OrderTest ivdTest = createBCellOrderTest (ID_BCell2_IVD);
        OrderTest cliaTest = createBCellOrderTest (ID_BCell2_CLIA, patient);
        OrderTest iuoCliaTest = createBCellOrderTest (ID_BCell2_IUO_CLIA);
        OrderTest iuoFlexTest = createBCellOrderTest (ID_BCell2_IUO_FLEX);
        OrderTest iuoIvdTest = createBCellOrderTest (ID_BCell2_IUO_IVD);
        OrderTest trackingTest = createBCellOrderTest (MRD_BCell2_CLIA, patient, validICD, bCellMRDTsv);

        verifyAutoReleaseSuccess (ivdTest);
        testLog ("SR-9504:R1: B-cell 2.0 Clonality (IVD) autoreleased");
        verifyAutoReleaseSuccess (cliaTest);
        testLog ("SR-9504:R1: B-cell 2.0 Clonality (CLIA) autoreleased");
        verifyAutoReleaseSuccess (iuoCliaTest);
        testLog ("SR-9504:R1: B-cell 2.0 Clonality (IUO CLIA-extract) autoreleased");
        verifyAutoReleaseSuccess (iuoFlexTest);
        testLog ("SR-9504:R1: B-cell 2.0 Clonality (IUO flex-extract) autoreleased");
        verifyAutoReleaseSuccess (iuoIvdTest);
        testLog ("SR-9504:R1: B-cell 2.0 Clonality (IUO IVD-extract) autoreleased");
        verifyAutoReleaseFailure (trackingTest,
                                  "Failed Auto Release Rules: Auto Release is not enabled for this SKU.");
        testLog ("SR-9504:R1: B-cell 2.0 Tracking (CLIA) failed autorelease");
    }

    /**
     * @sdlc.requirements SR-9504:R2, SR-9504:R3
     */
    public void alerts () {
        Patient patient = scenarioBuilderPatient ();
        OrderTest insuranceAlertTest = createBCellOrderTest (ID_BCell2_CLIA, patient);
        OrderTest lomnAlertTest = createBCellOrderTest ();
        OrderTest correctedReportAlertTest = createBCellOrderTest ();

        setPatientAlert ("insurance_anomaly", patient);
        setOrderAlert ("lomn_required", lomnAlertTest);
        setOrderAlert ("corrected_report_ready", correctedReportAlertTest);
        verifyAutoReleaseSuccess (insuranceAlertTest);
        testLog ("SR-9504:R3: Report with insurance anomaly patient alert autoreleased");
        verifyAutoReleaseSuccess (lomnAlertTest);
        testLog ("SR-9504:R3: Report with LOMN required order alert autoreleased");
        verifyAutoReleaseFailure (correctedReportAlertTest,
                                  "Failed Auto Release Rules: HasNoAlerts expected AlertType to not contain one of [Risk Acknowledgement Letter,Corrected Report,Clinical Consultation,High Sharing,cfDNA report with prior gDNA,censored low uniqueness clones,Clonality (ID) Order Needed,Pathology report needed].");
        testLog ("SR-9504:R2,R3: Report with corrected report order alert failed autoreleased");
    }

    /**
     * @sdlc.requirements SR-9504:R4
     */
    public void reportNotes () {
        String reportNote = "testing report notes autorelease";
        OrderTest test = createBCellOrderTest ();
        verifyAutoReleaseFailure (test,
                                  "Failed Auto Release Rules: HasNoClinicalReportNotes expected clinicalReportNote to be blank.",
                                  reportNote);
        testLog ("SR-9504:R4: Report with report notes failed autorelease");
    }

    /**
     * @sdlc.requirements SR-9504:R5
     */
    public void validIcdCodes () {
        String codes = "C90.00,C83.1,C82.9,C83.3,C90.1,C90.2";
        OrderTest test = createBCellOrderTest (ID_BCell2_CLIA,
                                               scenarioBuilderPatient (),
                                               codes,
                                               bCellIDTsv);
        verifyAutoReleaseSuccess (test);
        testLog ("SR-9504:R5: Report autoreleased for order containing the following ICD codes: " + codes);
    }

    /**
     * @sdlc.requirements SR-9504:R5
     */
    public void invalidIcdCodes () {
        String icdErrorMessage = "Failed Auto Release Rules: Rule IsAcceptedICDCodes expected Icd10Codes to include one of the following codes (Autorelease will fail if the list of idc codes contain a code starting with C that is not on the list): C90.0, C83.1, C82.9, C83.3, C90.1, C90.2.";
        String cPrefixCodes = "C90.0,C91.0";
        OrderTest cPrefixCodesTest = createBCellOrderTest (ID_BCell2_CLIA,
                                                           scenarioBuilderPatient (),
                                                           cPrefixCodes,
                                                           bCellIDTsv);
        verifyAutoReleaseFailure (cPrefixCodesTest, icdErrorMessage);
        testLog ("SR-9504:R5: Report failed autorelease for order containing the following ICD codes: " + cPrefixCodes);
    }

    /**
     * @sdlc.requirements SR-9504:R6
     */
    public void priorBCellID () {
        Patient patient = scenarioBuilderPatient ();
        OrderTest firstID = createBCellOrderTest (ID_BCell2_CLIA, patient, validICD, bCellIDTsv);
        verifyAutoReleaseSuccess (firstID);
        OrderTest secondID = createBCellOrderTest (ID_BCell2_CLIA, patient, validICD, bCellIDTsv);
        verifyAutoReleaseFailure (secondID,
                                  "Failed Auto Release Rules: HasNoCompletedClonalityTests expects BCell to be false.");
        testLog ("SR-9504:R6: Report failed autorelease for Bcell ID with prior completed Bcell ID");
    }

    /**
     * @sdlc.requirements SR-9504:R6, SR-9504:R7
     */
    public void priorNoResultBCellID () {
        Patient patient = scenarioBuilderPatient ();
        OrderTest priorFailedID = createBCellOrderTest (ID_BCell2_CLIA, patient, validICD, bCellIDTsv);
        orderStatus.gotoOrderStatusPage (priorFailedID.orderId);
        orderStatus.isCorrectPage ();
        orderStatus.failWorkflow (priorFailedID.sampleName, "testing prior no result autorelease");
        waitForReportGeneration (priorFailedID);
        verifyAutoReleaseFailure (priorFailedID,
                                  "Failed Auto Release Rules: IsSentToFailureTarget expected wasSentToFailureTarget not to be true,IsNotPolyClonal expects PolyClonal to be false.");
        testLog ("SR-9504:R7: Report with no result failed autorelease");
        history.clickOrderTest ();
        report.clickReportTab (Assay.getAssay (priorFailedID.test.name));
        report.clickReleaseReport ();
        OrderTest secondID = createBCellOrderTest (ID_BCell2_CLIA, patient, validICD, bCellIDTsv);
        verifyAutoReleaseSuccess (secondID);
        testLog ("SR-9504:R6: Report autoreleased for Bcell ID with prior Bcell ID that had no result");
    }

    /**
     * @sdlc.requirements SR-9504:R6
     */
    public void priorCancelledBCellID () {
        Patient patient = scenarioBuilderPatient ();
        OrderTest priorCancelledID = createBCellOrderTest (ID_BCell2_CLIA, patient, validICD, bCellIDTsv);
        orderDetail.gotoOrderDetailsPage (priorCancelledID.orderId);
        orderDetail.clickCancelOrder ();
        OrderTest secondTest = createBCellOrderTest (ID_BCell2_CLIA, patient, validICD, bCellIDTsv);
        verifyAutoReleaseSuccess (secondTest);
        testLog ("SR-9504:R6: Report autoreleased for Bcell ID with prior cancelled Bcell ID");
    }

    /**
     * @sdlc.requirements SR-9504:R1, SR-9504:R6
     */
    public void priorTcellID () {
        Diagnostic diagnostic = createTCellOrder ();
        verifyAutoReleaseFailure (diagnostic.findOrderTest (ID_TCRB),
                                  "Failed Auto Release Rules: Auto Release is not enabled for this SKU.");
        testLog ("SR-9504:R1: TCRB Clonality (CLIA) failed autorelease");
        verifyAutoReleaseFailure (diagnostic.findOrderTest (ID_TCRG),
                                  "Failed Auto Release Rules: Auto Release is not enabled for this SKU.");
        testLog ("SR-9504:R1: TCRG Clonality (CLIA) failed autorelease");
        OrderTest bCellTest = createBCellOrderTest (ID_BCell2_CLIA, diagnostic.patient, validICD, bCellIDTsv);
        verifyAutoReleaseSuccess (bCellTest);
        testLog ("SR-9504:R6: Report autoreleased for Bcell ID with prior Tcell ID");
    }

    /**
     * @sdlc.requirements SR-9504:R7
     */
    public void polyclonal () {
        OrderTest polyclonalTest = createBCellOrderTest (ID_BCell2_CLIA,
                                                         scenarioBuilderPatient (),
                                                         validICD,
                                                         polyclonalTsv);
        verifyAutoReleaseFailure (polyclonalTest,
                                  "Failed Auto Release Rules: IsNotPolyClonal expects PolyClonal to be false.");
        testLog ("SR-9504:R7: Polyclonal report failed autorelease");
    }

    /**
     * @sdlc.requirements SR-9504:R8
     */
    public void manyClones () {
        int ighThreshold = 4;
        int igkThreshold = 5;
        int iglThreshold = 3;
        OrderTest highIGHtest = createBCellOrderTest (ID_BCell2_CLIA,
                                                      scenarioBuilderPatient (),
                                                      validICD,
                                                      highIGHtsv);
        OrderTest highIGKtest = createBCellOrderTest (ID_BCell2_CLIA,
                                                      scenarioBuilderPatient (),
                                                      validICD,
                                                      highIGKtsv);
        OrderTest highIGLtest = createBCellOrderTest (ID_BCell2_CLIA,
                                                      scenarioBuilderPatient (),
                                                      validICD,
                                                      highIGLtsv);
        verifyAutoReleaseFailure (highIGHtest,
                                  format ("Failed Auto Release Rules: CheckIghIsBelowThreshold expected IGH to be less than %s.",
                                          Integer.toString (ighThreshold)));
        testLog (format ("SR-9504:R8: Report with %s dominant IGH sequences failed autorelease",
                         Integer.toString (ighThreshold)));
        verifyAutoReleaseFailure (highIGKtest,
                                  format ("Failed Auto Release Rules: CheckIgkIsBelowThreshold expected IGK to be less than %s.",
                                          Integer.toString (igkThreshold)));
        testLog (format ("SR-9504:R8: Report with %s dominant IGK sequences failed autorelease",
                         Integer.toString (igkThreshold)));
        verifyAutoReleaseFailure (highIGLtest,
                                  format ("Failed Auto Release Rules: CheckIglIsBelowThreshold expected IGL to be less than %s.",
                                          Integer.toString (iglThreshold)));
        testLog (format ("SR-9504:R8: Report with %s dominant IGL sequences failed autorelease",
                         Integer.toString (iglThreshold)));

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
        history.waitForActorPickup (ClonoSEQReport, "svc_test_orca");
        Stage autoreleasedStage = history.parseStatusHistory ().stream ()
                                         .filter (stage -> stage.stageName == ClonoSEQReport && stage.stageStatus == Finished && stage.stageSubstatus == RELEASED && stage.actor.contains ("svc_test_orca"))
                                         .findFirst ().orElse (null);
        assertNotNull (autoreleasedStage, "Autorelease success not found");
    }

    private void verifyAutoReleaseFailure (OrderTest orderTest, String failureMessage) {
        verifyAutoReleaseFailure (orderTest, failureMessage, null);
    }

    private void verifyAutoReleaseFailure (OrderTest orderTest, String failureMessage, String reportNote) {
        waitForReportGeneration (orderTest);
        if (reportNote != null) {
            report.enterReportNotes (reportNote);
            report.setQCstatus (QC.Pass);
            assertEquals (report.getReportNotes (), reportNote);
        } else {
            report.setQCstatus (QC.Pass);
        }
        history.gotoOrderDebug (orderTest.sampleName);
        history.waitForActorPickup (ClonoSEQReport, "svc_test_orca");
        Stage failureStage = history.parseStatusHistory ().stream ()
                                    .filter (stage -> stage.stageName == ClonoSEQReport && stage.stageStatus == Awaiting && stage.stageSubstatus == CLINICAL_CONSULTANT && stage.actor.contains ("svc_test_orca"))
                                    .findFirst ().orElse (null);
        assertNotNull (failureStage, "Autorelease failure not found");
        assertEquals (failureStage.subStatusMessage,
                      failureMessage);
    }

    private void setPatientAlert (String alertTypeName, Patient patient) {
        Alert alert = new Alert ();
        alert.alertTypeName = alertTypeName;
        alert.referencedEntityId = patient.id;
        coraApi.setAlerts (alert);
        assertTrue (Arrays.asList (coraApi.getAlertsForPatientId (patient.id)).stream ()
                          .anyMatch (actualAlert -> actualAlert.alertType.name.equals (alertTypeName)));
    }

    private void setOrderAlert (String alertTypeName, OrderTest orderTest) {
        Alert alert = new Alert ();
        alert.alertTypeName = alertTypeName;
        alert.referencedEntityId = orderTest.orderId;
        coraApi.setAlerts (alert);
        assertTrue (Arrays.asList (coraApi.getAlertsForOrderId (orderTest.orderId)).stream ()
                          .anyMatch (actualAlert -> actualAlert.alertType.name.equals (alertTypeName)));
    }

    private OrderTest createBCellOrderTest () {
        return createBCellOrderTest (ID_BCell2_CLIA, scenarioBuilderPatient (), validICD, bCellIDTsv);
    }

    private OrderTest createBCellOrderTest (Assay sku) {
        return createBCellOrderTest (sku, scenarioBuilderPatient (), validICD, bCellIDTsv);
    }

    private OrderTest createBCellOrderTest (Assay sku, Patient patient) {
        return createBCellOrderTest (sku, patient, validICD, bCellIDTsv);
    }

    private OrderTest createBCellOrderTest (Assay sku, Patient patient, String icdCodes, String tsvPath) {
        CoraTest test = genCDxTest (sku, tsvPath);
        Diagnostic diagnostic = buildCdxOrder (patient,
                                               icdCodes,
                                               stage (SecondaryAnalysis, Ready),
                                               test);
        assertEquals (coraApi.newBcellOrder (diagnostic).patientId, patient.id);
        return diagnostic.findOrderTest (sku);
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
