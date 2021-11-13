package com.adaptivebiotech.cora.test.order.tdetect;

import static com.adaptivebiotech.cora.utils.TestHelper.scenarioBuilderPatient;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.newCovidOrder;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.stage;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.PageHelper.Assay.COVID19_DX_IVD;
import static com.adaptivebiotech.test.utils.PageHelper.QC.Pass;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.DxReport;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.ReportDelivery;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Awaiting;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Ready;
import static com.adaptivebiotech.test.utils.PageHelper.StageSubstatus.CLINICAL_QC;
import static com.adaptivebiotech.test.utils.PageHelper.StageSubstatus.SENDING_REPORT_NOTIFICATION;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.AssayResponse;
import com.adaptivebiotech.cora.dto.Diagnostic;
import com.adaptivebiotech.cora.dto.Orders.OrderTest;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.dto.Workflow.WorkflowProperties;
import com.adaptivebiotech.cora.test.order.OrderTestBase;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.debug.OrcaHistory;
import com.adaptivebiotech.cora.ui.order.ReportClonoSeq;

@Test (groups = { "akita", "regression" })
public class GatewayNotificationTestSuite extends OrderTestBase {

    private final String   covidTsv           = "https://adaptiveivdpipeline.blob.core.windows.net/pipeline-results/210209_NB551550_0241_AHTT33BGXG/v3.1/20210211_0758/packaged/rd.Human.TCRB-v4b.nextseq.156x12x0.vblocks.ultralight.rev3/HTT33BGXG_0_CLINICAL-CLINICAL_95268-SN-2205.adap.txt.results.tsv.gz";
    private final String   covidWorkspaceName = "CLINICAL-CLINICAL";
    private final String   covidSampleName    = "95268-SN-2205";
    private final String   gatewayJson        = "gatewayMessage.json";
    private OrcaHistory    history            = new OrcaHistory ();
    private ReportClonoSeq report             = new ReportClonoSeq ();
    private Patient        patient;

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod () {
        patient = scenarioBuilderPatient ();
        doCoraLogin ();
        new Login ().doLogin ();
    }

    /**
     * @sdlc_requirements SR-7370
     */
    public void verifyCovidGatewayMessageUpdate () {
        AssayResponse.CoraTest test = genTDxTest (COVID19_DX_IVD);
        test.tsvPath = covidTsv;
        test.workflowProperties = new WorkflowProperties ();
        test.workflowProperties.flowcell = "H752HBGXH";
        test.workflowProperties.workspaceName = covidWorkspaceName;
        test.workflowProperties.sampleName = covidSampleName;

        Diagnostic diagnostic = buildCovidOrder (patient, stage (DxReport, Ready), test);
        assertEquals (newCovidOrder (diagnostic).patientId, patient.id);
        testLog ("submitted a new Covid19 order in Cora");

        OrderTest orderTest = diagnostic.findOrderTest (COVID19_DX_IVD);
        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (DxReport, Awaiting, CLINICAL_QC);
        history.clickOrderTest ();
        report.clickReportTab (COVID19_DX_IVD);
        report.releaseReport (COVID19_DX_IVD, Pass);
        testLog ("released the Covid report");

        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (ReportDelivery, Awaiting, SENDING_REPORT_NOTIFICATION);
        history.isFilePresent (gatewayJson);
        testLog ("gateway message sent");
    }
}
