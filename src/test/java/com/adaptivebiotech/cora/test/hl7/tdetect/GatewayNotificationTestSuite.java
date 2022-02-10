package com.adaptivebiotech.cora.test.hl7.tdetect;

import static com.adaptivebiotech.cora.dto.Orders.Assay.COVID19_DX_IVD;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.TDetect_client;
import static com.adaptivebiotech.cora.utils.PageHelper.QC.Pass;
import static com.adaptivebiotech.cora.utils.TestHelper.scenarioBuilderPatient;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.buildCovidOrder;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.stage;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.DxReport;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.ReportDelivery;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Awaiting;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Ready;
import static com.adaptivebiotech.test.utils.PageHelper.StageSubstatus.CLINICAL_QC;
import static com.adaptivebiotech.test.utils.PageHelper.StageSubstatus.SENDING_REPORT_NOTIFICATION;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.AssayResponse.CoraTest;
import com.adaptivebiotech.cora.dto.Diagnostic;
import com.adaptivebiotech.cora.dto.Orders.OrderTest;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.test.hl7.HL7TestBase;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.debug.OrcaHistory;
import com.adaptivebiotech.cora.ui.order.OrderDetailTDetect;
import com.adaptivebiotech.cora.ui.order.OrderStatus;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.order.ReportTDetect;

@Test (groups = { "akita", "regression" })
public class GatewayNotificationTestSuite extends HL7TestBase {

    private final String       covidTsv    = "https://adaptiveivdpipeline.blob.core.windows.net/pipeline-results/210209_NB551550_0241_AHTT33BGXG/v3.1/20210211_0758/packaged/rd.Human.TCRB-v4b.nextseq.156x12x0.vblocks.ultralight.rev3/HTT33BGXG_0_CLINICAL-CLINICAL_95268-SN-2205.adap.txt.results.tsv.gz";
    private final String       gatewayJson = "gatewayMessage.json";
    private Login              login       = new Login ();
    private OrdersList         ordersList  = new OrdersList ();
    private ReportTDetect      report      = new ReportTDetect ();
    private OrderStatus        orderStatus = new OrderStatus ();
    private OrderDetailTDetect orderDetail = new OrderDetailTDetect ();
    private OrcaHistory        history     = new OrcaHistory ();

    /**
     * @sdlc.requirements SR-7370
     */
    public void verifyCovidGatewayMessageUpdate () {
        CoraTest test = coraApi.getTDxTest (COVID19_DX_IVD);
        test.tsvPath = covidTsv;
        test.workflowProperties = sample_95268_SN_2205 ();

        Patient patient = scenarioBuilderPatient ();
        Diagnostic diagnostic = buildCovidOrder (coraApi.getPhysician (TDetect_client),
                                                 patient,
                                                 stage (DxReport, Ready),
                                                 test);
        diagnostic.dxResults = negativeDxResult ();
        assertEquals (coraApi.newCovidOrder (diagnostic).patientId, patient.id);
        testLog ("submitted a new Covid19 order in Cora");

        OrderTest orderTest = diagnostic.findOrderTest (COVID19_DX_IVD);
        login.doLogin ();
        ordersList.isCorrectPage ();
        orderStatus.gotoOrderStatusPage (orderTest.orderId);
        orderStatus.isCorrectPage ();
        orderStatus.waitFor (orderTest.sampleName, DxReport, Awaiting, CLINICAL_QC);
        orderStatus.gotoOrderDetailsPage (orderTest.orderId);
        orderDetail.isCorrectPage ();
        orderDetail.clickReportTab (COVID19_DX_IVD);
        report.isCorrectPage ();
        report.releaseReport (COVID19_DX_IVD, Pass);
        testLog ("released the Covid report");

        report.clickOrderStatusTab ();
        orderStatus.isCorrectPage ();
        orderStatus.waitFor (orderTest.sampleName, ReportDelivery, Awaiting, SENDING_REPORT_NOTIFICATION);
        history.gotoOrderDebug (orderTest.sampleName);
        assertTrue (history.isFilePresent (gatewayJson));
        testLog ("gateway message sent");
    }
}
