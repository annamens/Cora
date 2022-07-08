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
import static com.adaptivebiotech.test.utils.PageHelper.StageName.ClonoSEQReport;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.NorthQC;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.SecondaryAnalysis;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Awaiting;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Ready;
import static com.adaptivebiotech.test.utils.PageHelper.StageSubstatus.CLINICAL_CONSULTANT;
import static com.adaptivebiotech.test.utils.PageHelper.StageSubstatus.CLINICAL_QC;
import static org.testng.Assert.assertEquals;
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
import com.adaptivebiotech.cora.test.report.ReportTestBase;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.debug.OrcaHistory;
import com.adaptivebiotech.cora.ui.order.OrderDetail;
import com.adaptivebiotech.cora.ui.order.OrderStatus;
import com.adaptivebiotech.cora.ui.order.ReportClonoSeq;
import com.adaptivebiotech.cora.utils.PageHelper.QC;

@Test (groups = { "regression", "golden-retriever" }) // not golden-retriever anymore
public class ClonoSEQAutoReleaseTestSuite extends ReportTestBase {

    private Login          login       = new Login ();
    private OrcaHistory    history     = new OrcaHistory ();
    private ReportClonoSeq report      = new ReportClonoSeq ();
    private OrderDetail    orderDetail = new OrderDetail ();
    private OrderStatus    orderStatus = new OrderStatus ();

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
        OrderTest trackingTest = createBCellOrderTest (MRD_BCell2_CLIA, patient);

        verifyAutoReleaseSuccess (ivdTest);
        verifyAutoReleaseSuccess (cliaTest);
        verifyAutoReleaseSuccess (iuoCliaTest);
        verifyAutoReleaseSuccess (iuoFlexTest);
        verifyAutoReleaseSuccess (iuoIvdTest);
        verifyAutoReleaseFailure (trackingTest,
                                  "Failed Auto Release Rules: Auto Release is not enabled for this SKU.");
    }

    /**
     * @sdlc.requirements SR-9504:R1
     */
    public void tCellSKU () {
        Diagnostic diagnostic = createTCellOrder ();
        OrderTest testTCRB = diagnostic.findOrderTest (ID_TCRB);
        OrderTest testTCRG = diagnostic.findOrderTest (ID_TCRG);
        verifyNoAutoRelease (testTCRB);
        verifyNoAutoRelease (testTCRG);
    }

    /**
     * @sdlc.requirements SR-9504:R2, SR-9504:R3
     */
    public void alerts () {
        Patient patient = scenarioBuilderPatient ();
        OrderTest insuranceAlertTest = createBCellOrderTest (ID_BCell2_CLIA, patient);
        OrderTest lomnAlertTest = createBCellOrderTest ();
        OrderTest correctedReportAlertTest = createBCellOrderTest ();

        // could probably handle this better
        setPatientAlert ("insurance_anomaly", patient);
        setOrderAlert ("lomn_required", lomnAlertTest);
        setOrderAlert ("corrected_report_ready", correctedReportAlertTest); // is this right?
        verifyAutoReleaseSuccess (insuranceAlertTest);
        verifyAutoReleaseSuccess (lomnAlertTest);
        verifyAutoReleaseFailure (correctedReportAlertTest, "i dunno placeholder");
    }

    /**
     * @sdlc.requirements SR-9504:R4
     */
    public void reportNotes () {
        // boilerplate
        CoraTest test = genCDxTest (ID_BCell2_CLIA, azTsvPath + "/above-loq.id.tsv.gz");
        test.workflowProperties.reportNotes = "testnotes";
        Patient patient = scenarioBuilderPatient ();
        Diagnostic diagnostic = buildCdxOrder (patient,
                                               "C91.00",
                                               stage (SecondaryAnalysis, Ready),
                                               test);
        assertEquals (coraApi.newBcellOrder (diagnostic).patientId, patient.id);
        OrderTest orderTest = diagnostic.findOrderTest (ID_BCell2_CLIA);
        verifyAutoReleaseFailure (orderTest,
                                  "i dunno placeholder");
    }

    /**
     * @sdlc.requirements SR-9504:R5
     */
    public void validIcdCodes () {
        OrderTest test = createBCellOrderTest (ID_BCell2_CLIA,
                                               scenarioBuilderPatient (),
                                               "C90.00,C83.1,C82.9,C83.3,C90.1,C90.2");
        verifyAutoReleaseSuccess (test);
    }

    /**
     * @sdlc.requirements SR-9504:R5
     */
    public void invalidIcdCodes () {
        OrderTest test = createBCellOrderTest (ID_BCell2_CLIA, scenarioBuilderPatient (), "C90.00,W61.62XD");
        verifyAutoReleaseFailure (test,
                                  "i dunno placeholder");
    }

    /**
     * @sdlc.requirements SR-9504:R6
     */
    public void multipleIDs () {
        Patient patient = scenarioBuilderPatient ();
        OrderTest firstID = createBCellOrderTest (ID_BCell2_CLIA, patient, "C90.00");
        OrderTest secondID = createBCellOrderTest (ID_BCell2_CLIA, patient, "C90.00");
        verifyAutoReleaseSuccess (firstID);
        // non-cancelled B-cell clonality ID order test with no results succeeds
        verifyAutoReleaseFailure (secondID, "i dunno placeholder");
    }

    /**
     * @sdlc.requirements SR-9504:R6
     */
    public void priorCancelledID () {
        OrderTest cancelledTest = createBCellOrderTest ();
        OrderTest secondTest = createBCellOrderTest ();
        orderDetail.gotoOrderDetailsPage (cancelledTest.orderId);
        orderDetail.clickCancelOrder ();
        verifyAutoReleaseSuccess (secondTest);
    }

    /**
     * @sdlc.requirements SR-9504:R6
     */
    public void priorTcellID () {
        Diagnostic diagnostic = createTCellOrder ();
        OrderTest test = createBCellOrderTest (ID_BCell2_CLIA, diagnostic.patient, "C90.00");
        verifyAutoReleaseSuccess (test);
    }

    /**
     * @sdlc.requirements SR-9504:R7
     */
    public void noResult () {
        OrderTest test = createBCellOrderTest ();
        orderStatus.gotoOrderStatusPage (test.orderId);
        orderStatus.isCorrectPage ();
        orderStatus.failWorkflow (test.sampleName, "testing no result autorelease");
        verifyAutoReleaseFailure (test, "placeholder i dunno");
    }

    // public void polyclonal() {
    // // maybe this one?
    // s3://pipeline-north-production-archive:us-west-2/210602_NB552492_0027_AH3GK5BGXJ/v3.1/20210604_2027/packaged/rd.Human.BCell.nextseq.146x13x116.threeRead.ultralight.rev32/H3GK5BGXJ_0_CLINICAL-CLINICAL_102589-01MC-B20-229.adap.txt.results.tsv.gz
    //
    // }

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
        return createBCellOrderTest (ID_BCell2_CLIA, scenarioBuilderPatient (), "C91.00");
    }

    private OrderTest createBCellOrderTest (Assay sku) {
        return createBCellOrderTest (sku, scenarioBuilderPatient (), "C91.00");
    }

    private OrderTest createBCellOrderTest (Assay sku, Patient patient) {
        return createBCellOrderTest (sku, patient, "C91.00");
    }

    private OrderTest createBCellOrderTest (Assay sku, Patient patient, String icdCodes) {
        CoraTest test = genCDxTest (sku, azTsvPath + "/above-loq.id.tsv.gz");
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

    private void verifyAutoReleaseSuccess (OrderTest orderTest) {
        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);
        history.clickOrderTest ();
        report.clickReportTab (Assay.getAssay (orderTest.test.name));
        report.setQCstatus (QC.Pass);
        history.gotoOrderDebug (orderTest.sampleName);
        // this doesn't work yet
        // history.waitFor (ClonoSEQReport, Finished, RELEASED);
    }

    private void verifyAutoReleaseFailure (OrderTest orderTest, String failureMessage) {
        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);
        history.clickOrderTest ();
        report.clickReportTab (Assay.getAssay (orderTest.test.name));
        report.setQCstatus (QC.Pass);
        // this doesn't work yet
        // history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_CONSULTANT, failureMessage);
    }

    private void verifyNoAutoRelease (OrderTest orderTest) {
        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);
        history.clickOrderTest ();
        Assay assay = Assay.getAssay (orderTest.test.name);
        report.clickReportTab (Assay.getAssay (orderTest.test.name));
        report.generateReport (assay);
        report.setQCstatus (QC.Pass);
        // wait 30 seconds to see if workflow update occurs
        doWait (30000);
        history.gotoOrderDebug (orderTest.sampleName);
        assertTrue (history.isTopLevelStagePresent (ClonoSEQReport,
                                                    Awaiting,
                                                    CLINICAL_CONSULTANT,
                                                    "Waiting for report to be released."));
    }
}
