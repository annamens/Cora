/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.hl7.tdetect;

import static com.adaptivebiotech.cora.dto.Containers.ContainerType.Tube;
import static com.adaptivebiotech.cora.dto.Orders.Assay.COVID19_DX_IVD;
import static com.adaptivebiotech.cora.dto.Orders.Assay.LYME_DX;
import static com.adaptivebiotech.cora.dto.Orders.OrderStatus.Active;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.TDetect_canada;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.TDetect_client;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.TDetect_selfpay;
import static com.adaptivebiotech.cora.utils.PageHelper.QC.Pass;
import static com.adaptivebiotech.cora.utils.TestHelper.bloodSpecimen;
import static com.adaptivebiotech.cora.utils.TestHelper.covidProperties;
import static com.adaptivebiotech.cora.utils.TestHelper.newSelfPayPatientTDx;
import static com.adaptivebiotech.cora.utils.TestHelper.scenarioBuilderPatient;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.buildTdetectOrder;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.stage;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.DxAnalysis;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.DxContamination;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.DxReport;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.NorthQC;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.ReportDelivery;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Awaiting;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Finished;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Ready;
import static com.adaptivebiotech.test.utils.PageHelper.StageSubstatus.CLINICAL_QC;
import static com.adaptivebiotech.test.utils.PageHelper.StageSubstatus.SENDING_REPORT_NOTIFICATION;
import static com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty.country;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.AssayResponse.CoraTest;
import com.adaptivebiotech.cora.dto.Diagnostic;
import com.adaptivebiotech.cora.dto.Orders.Order;
import com.adaptivebiotech.cora.dto.Orders.OrderTest;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.dto.Specimen;
import com.adaptivebiotech.cora.test.hl7.HL7TestBase;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.debug.OrcaHistory;
import com.adaptivebiotech.cora.ui.order.NewOrderTDetect;
import com.adaptivebiotech.cora.ui.order.OrderDetailTDetect;
import com.adaptivebiotech.cora.ui.order.OrderStatus;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.order.ReportTDetect;

@Test (groups = { "regression", "akita" }, singleThreaded = true)
public class GatewayNotificationTestSuite extends HL7TestBase {

    private final String       lymeTsv         = azPipelineClia + "/200522_NB501172_0808_AHMHTHBGXF/v3.1/20200524_1354/packaged/rd.Human.TCRB-v4b.nextseq.156x12x0.vblocks.ultralight.rev1/HMHTHBGXF_0_BALFLDB-Horn_LD011378_0001-reflex.adap.txt.results.tsv.gz";
    private final String       gatewayJson     = "gatewayMessage.json";
    private Login              login           = new Login ();
    private OrdersList         ordersList      = new OrdersList ();
    private NewOrderTDetect    newOrderTDetect = new NewOrderTDetect ();
    private ReportTDetect      report          = new ReportTDetect ();
    private OrderStatus        orderStatus     = new OrderStatus ();
    private OrderDetailTDetect orderDetail     = new OrderDetailTDetect ();
    private OrcaHistory        history         = new OrcaHistory ();
    private Specimen           specimen        = bloodSpecimen ();

    /**
     * @sdlc.requirements SR-5243, SR-7370, SR-9446:R2
     */
    @Test (groups = "fox-terrier")
    public void verifyCovidGatewayMessageUpdate () {
        login.doLogin ();
        ordersList.isCorrectPage ();
        Order order = newOrderTDetect.createTDetectOrder (coraApi.getPhysician (TDetect_selfpay),
                                                          newSelfPayPatientTDx (),
                                                          null,
                                                          COVID19_DX_IVD,
                                                          specimen,
                                                          Active,
                                                          Tube);
        testLog ("submitted a new Covid19 order in Cora: " + order.orderNumber);

        String sample = orderDetail.getSampleName (COVID19_DX_IVD);
        history.gotoOrderDebug (sample);
        history.setWorkflowProperties (covidProperties ());
        history.forceStatusUpdate (NorthQC, Ready);
        history.clickOrder ();
        testLog ("set workflow properties and force workflow to move to NorthQC/Ready stage");

        orderStatus.isCorrectPage ();
        orderStatus.waitFor (sample, NorthQC, Finished);
        orderStatus.waitFor (sample, DxAnalysis, Finished);
        orderStatus.waitFor (sample, DxContamination, Finished);
        orderStatus.waitFor (sample, DxReport, Finished);
        orderStatus.waitFor (sample, ReportDelivery, Awaiting, SENDING_REPORT_NOTIFICATION);
        history.gotoOrderDebug (sample);
        assertEquals (history.getWorkflowProperties ().get (country.name ()), "US");
        testLog ("workflow property: 'country' is set and has value: 'US'");

        assertFalse (history.isFilePresent (gatewayJson));
        testLog ("gateway message sent");
    }

    /**
     * @sdlc.requirements SR-9446:R2
     */
    @Test (groups = "fox-terrier")
    public void verifyCovidCanadaGatewayMessage () {
        login.doLogin ();
        ordersList.isCorrectPage ();
        Order order = newOrderTDetect.createTDetectOrder (coraApi.getPhysician (TDetect_canada),
                                                          newSelfPayPatientTDx (),
                                                          null,
                                                          COVID19_DX_IVD,
                                                          specimen,
                                                          Active,
                                                          Tube);
        testLog ("submitted a new Covid19 order in Cora: " + order.orderNumber);

        String sample = orderDetail.getSampleName (COVID19_DX_IVD);
        history.gotoOrderDebug (sample);
        history.setWorkflowProperties (covidProperties ());
        history.forceStatusUpdate (NorthQC, Ready);
        history.clickOrder ();
        testLog ("set workflow properties and force workflow to move to NorthQC/Ready stage");

        orderStatus.isCorrectPage ();
        orderStatus.waitFor (sample, NorthQC, Finished);
        orderStatus.waitFor (sample, DxAnalysis, Finished);
        orderStatus.waitFor (sample, DxContamination, Finished);
        orderStatus.waitFor (sample, DxReport, Finished);
        orderStatus.waitFor (sample, ReportDelivery, Awaiting, SENDING_REPORT_NOTIFICATION);
        history.gotoOrderDebug (sample);
        assertEquals (history.getWorkflowProperties ().get (country.name ()), "CA");
        testLog ("workflow property: 'country' is set and has value: 'CA'");

        assertFalse (history.isFilePresent (gatewayJson));
        testLog ("gateway message wasn't sent");
    }

    @Test (groups = "dingo")
    public void verifyLymeGatewayMessage () {
        CoraTest test = getTDxTest (LYME_DX);
        test.tsvPath = lymeTsv;
        test.workflowProperties.flowcell = "H752HBGXH";
        test.workflowProperties.workspaceName = "CLINICAL-CLINICAL";
        test.workflowProperties.sampleName = "95268-SN-2205";

        Patient patient = scenarioBuilderPatient ();
        Diagnostic diagnostic = buildTdetectOrder (coraApi.getPhysician (TDetect_client),
                                                   patient,
                                                   stage (DxReport, Ready),
                                                   test,
                                                   LYME_DX);
        diagnostic.dxResults = positiveLymeResult ();
        assertEquals (coraApi.newTdetectOrder (diagnostic).patientId, patient.id);
        testLog ("submitted a new Lyme order in Cora");

        OrderTest orderTest = diagnostic.findOrderTest (LYME_DX);
        login.doLogin ();
        ordersList.isCorrectPage ();
        orderStatus.gotoOrderStatusPage (orderTest.orderId);
        orderStatus.isCorrectPage ();
        orderStatus.waitFor (orderTest.sampleName, DxReport, Awaiting, CLINICAL_QC);
        orderStatus.gotoOrderDetailsPage (orderTest.orderId);
        orderDetail.isCorrectPage ();
        orderDetail.clickReportTab (LYME_DX);
        report.isCorrectPage ();
        report.releaseReport (LYME_DX, Pass);
        testLog ("released the Lyme report");

        report.clickOrderStatusTab ();
        orderStatus.isCorrectPage ();
        orderStatus.waitFor (orderTest.sampleName, ReportDelivery, Finished);
        history.gotoOrderDebug (orderTest.sampleName);
        assertFalse (history.isFilePresent (gatewayJson));
        testLog ("gateway message wasn't sent");
    }
}
