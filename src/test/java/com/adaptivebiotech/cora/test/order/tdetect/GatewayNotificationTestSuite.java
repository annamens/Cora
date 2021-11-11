package com.adaptivebiotech.cora.test.order.tdetect;

import static com.adaptivebiotech.cora.utils.TestHelper.scenarioBuilderPatient;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.newCovidOrder;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.stage;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.PageHelper.Assay.COVID19_DX_IVD;
import static com.adaptivebiotech.test.utils.PageHelper.QC.Pass;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.DxAnalysis;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.DxContamination;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.DxReport;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.ReportDelivery;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Awaiting;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Finished;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Ready;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Stuck;
import static com.adaptivebiotech.test.utils.PageHelper.StageSubstatus.CLINICAL_QC;
import static com.adaptivebiotech.test.utils.PageHelper.StageSubstatus.SENDING_REPORT_NOTIFICATION;
import static com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty.sampleName;
import static com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty.workspaceName;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.AssayResponse;
import com.adaptivebiotech.cora.dto.Diagnostic;
import com.adaptivebiotech.cora.dto.Orders;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.test.order.OrderTestBase;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.debug.OrcaHistory;
import com.adaptivebiotech.cora.ui.order.ReportClonoSeq;

@Test (groups = { "akita", "regression" })
public class GatewayNotificationTestSuite extends OrderTestBase {

    private final String     covidTsv           = "https://adaptiveruopipeline.blob.core.windows.net/pipeline-results/200613_NB551725_0151_AHM7N7BGXF/v3.1/20200615_1438/packaged/rd.Human.TCRB-v4b.nextseq.156x12x0.vblocks.ultralight.rev1/HM7N7BGXF_0_Hospital12deOctubre-MartinezLopez_860011348.adap.txt.results.tsv.gz";
    private final String     covidWorkspaceName = "Hospital12deOctubre-MartinezLopez";
    private final String     covidSampleName    = "860011348";

    private Diagnostic       diagnostic;
    private Orders.OrderTest orderTest;
    private OrcaHistory      history;
    private ReportClonoSeq   report;

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod () {
        doCoraLogin ();
        new Login ().doLogin ();
        history = new OrcaHistory ();
        report = new ReportClonoSeq ();
    }

    /**
     * @sdlc_requirements SR-7370
     */
    public void verifyCovidGatewayMessageUpdate () {
        Patient patient = scenarioBuilderPatient ();
        AssayResponse.CoraTest test = genTDxTest (COVID19_DX_IVD);
        test.tsvPath = covidTsv;
        diagnostic = buildCovidOrder (patient, stage (DxAnalysis, Ready), test);
        assertEquals (newCovidOrder (diagnostic).patientId, patient.id);
        testLog ("submitted a new Covid19 order in Cora");

        orderTest = diagnostic.findOrderTest (COVID19_DX_IVD);
        history.gotoOrderDebug (orderTest.sampleName);
        history.setWorkflowProperty (workspaceName, covidWorkspaceName);
        history.setWorkflowProperty (sampleName, covidSampleName);
        history.waitFor (DxContamination, Stuck);
        history.forceStatusUpdate (DxContamination, Finished);
        history.waitFor (DxReport, Awaiting, CLINICAL_QC);
        history.clickOrderTest ();
        report.releaseReport (COVID19_DX_IVD, Pass);
        testLog ("released the Covid report");

        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (ReportDelivery, Awaiting, SENDING_REPORT_NOTIFICATION);
        history.isFilePresent ("gatewayMessage.json");
        testLog ("gateway message sent");
    }

}
