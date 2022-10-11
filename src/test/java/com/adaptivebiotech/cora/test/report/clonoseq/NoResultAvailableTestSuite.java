/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.report.clonoseq;

import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_BCell2_IVD;
import static com.adaptivebiotech.cora.dto.Orders.Assay.MRD_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Orders.Assay.MRD_BCell2_IVD;
import static com.adaptivebiotech.cora.dto.Specimen.Anticoagulant.Streck;
import static com.adaptivebiotech.cora.utils.PageHelper.QC.Pass;
import static com.adaptivebiotech.cora.utils.PdfUtil.getPageCount;
import static com.adaptivebiotech.cora.utils.PdfUtil.getTextFromPDF;
import static com.adaptivebiotech.cora.utils.TestHelper.scenarioBuilderPatient;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.dateToArrInt;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.stage;
import static com.adaptivebiotech.test.utils.DateHelper.genLocalDateTime;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.PageHelper.Compartment.CellFree;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.Plasma;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.Clarity;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.ClonoSEQReport;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Awaiting;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Failed;
import static com.adaptivebiotech.test.utils.PageHelper.StageSubstatus.CLINICAL_QC;
import static java.lang.String.join;
import static org.testng.Assert.assertEquals;
import java.lang.reflect.Method;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Diagnostic;
import com.adaptivebiotech.cora.dto.Orders.OrderTest;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.test.report.ReportTestBase;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.debug.OrcaHistory;
import com.adaptivebiotech.cora.ui.order.OrderDetailClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrderStatus;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.order.ReportClonoSeq;
import com.adaptivebiotech.picasso.dto.ReportRender;
import com.adaptivebiotech.picasso.dto.verify.ClonoSeq;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
@Test (groups = "regression")
public class NoResultAvailableTestSuite extends ReportTestBase {

    private Login                login                = new Login ();
    private OrdersList           ordersList           = new OrdersList ();
    private OrderStatus          orderStatus          = new OrderStatus ();
    private OrderDetailClonoSeq  orderDetail          = new OrderDetailClonoSeq ();
    private OrcaHistory          history              = new OrcaHistory ();
    private ReportClonoSeq       report               = new ReportClonoSeq ();
    private ThreadLocal <String> downloadDir          = new ThreadLocal <> ();
    private final String         noResultsAvailable   = "No result available";
    private final String         mrdResultDescription = "This sample failed the quality control criteria despite multiple sequencing attempts, exceeded the sample stability time period, or there was a problem processing the test. Please contact Adaptive Biotechnologies for more information, to provide sample disposition instructions, and/or to discuss whether sending a new sample (if one is available) should be considered.";

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod (Method test) {
        downloadDir.set (artifacts (this.getClass ().getName (), test.getName ()));
    }

    /**
     * NOTE: SR-T4204
     * 
     * @sdlc.requirements SR-10414:R3
     */
    @Test (groups = "irish-wolfhound")
    public void verify_clia_eos_report () {
        Patient patient = scenarioBuilderPatient ();
        Diagnostic diagnostic = buildCdxOrder (patient,
                                               stage (Clarity, Failed),
                                               genCDxTest (ID_BCell2_CLIA, null),
                                               genCDxTest (MRD_BCell2_CLIA, null));
        assertEquals (coraApi.newBcellOrder (diagnostic).patientId, patient.id);
        testLog ("[CLIA] submitted clonality and tracking orders");

        OrderTest orderTest = diagnostic.findOrderTest (ID_BCell2_CLIA);
        login.doLogin ();
        ordersList.isCorrectPage ();
        ordersList.gotoOrderStatusPage (orderTest.orderId);
        orderStatus.isCorrectPage ();
        orderStatus.waitFor (orderTest.sampleName, ClonoSEQReport, Awaiting, CLINICAL_QC);
        orderStatus.gotoOrderDetailsPage (orderTest.orderId);
        orderDetail.isCorrectPage ();
        orderDetail.clickReportTab (ID_BCell2_CLIA);
        report.isCorrectPage ();
        report.releaseReport (ID_BCell2_CLIA, Pass);

        String actualPdf = join ("/", downloadDir.get (), orderTest.sampleName + ".pdf");
        coraApi.get (report.getReportUrl (), actualPdf);
        history.gotoOrderDebug (orderTest.sampleName);

        String actual = join ("/", downloadDir.get (), orderTest.sampleName, reportData);
        coraDebugApi.login ();
        coraDebugApi.get (history.getFileLocation (reportData), actual);
        ReportRender reportRender = parseReportData (actual);
        testLog ("[CLIA] downloaded " + reportData);

        ClonoSeq clonoseq = basicClonoSeq (reportRender, patient, diagnostic, orderTest);
        clonoseq.helper.isCLIA = true;
        clonoseq.helper.isFailed = true;
        clonoseq.pageSize = getPageCount (actualPdf);
        verifyReport (clonoseq, getTextFromPDF (actualPdf));
        testLog ("[CLIA] the EOS ClonoSEQ 2.0 clonality report matched with the baseline");

        orderTest = diagnostic.findOrderTest (MRD_BCell2_CLIA);
        history.gotoOrderStatusPage (orderTest.orderId);
        orderStatus.isCorrectPage ();
        orderStatus.waitFor (orderTest.sampleName, ClonoSEQReport, Awaiting, CLINICAL_QC);
        orderStatus.gotoOrderDetailsPage (orderTest.orderId);
        orderDetail.isCorrectPage ();
        orderDetail.clickReportTab (MRD_BCell2_CLIA);
        report.isCorrectPage ();
        report.releaseReport (MRD_BCell2_CLIA, Pass);

        actualPdf = join ("/", downloadDir.get (), orderTest.sampleName + ".pdf");
        coraApi.get (report.getReportUrl (), actualPdf);
        history.gotoOrderDebug (orderTest.sampleName);

        actual = join ("/", downloadDir.get (), orderTest.sampleName, reportData);
        coraDebugApi.get (history.getFileLocation (reportData), actual);
        reportRender = parseReportData (actual);
        testLog ("[CLIA] downloaded " + reportData);

        clonoseq = basicClonoSeq (reportRender, patient, diagnostic, orderTest);
        clonoseq.helper.isCLIA = true;
        clonoseq.helper.isFailed = true;
        clonoseq.pageSize = getPageCount (actualPdf);
        verifyReport (clonoseq, getTextFromPDF (actualPdf));
        testLog ("[CLIA] the EOS ClonoSEQ 2.0 tracking report matched with the baseline");
    }

    /**
     * NOTE: SR-T4204
     * 
     * @sdlc.requirements SR-10414:R3
     */
    @Test (groups = "irish-wolfhound")
    public void verify_ivd_eos_report () {
        Patient patient = scenarioBuilderPatient ();
        Diagnostic diagnostic = buildCdxOrder (patient,
                                               stage (Clarity, Failed),
                                               genCDxTest (ID_BCell2_IVD, azTsvPath + "/above-loq.id.tsv.gz"),
                                               genCDxTest (MRD_BCell2_IVD, azTsvPath + "/above-loq.mrd.tsv.gz"));
        assertEquals (coraApi.newBcellOrder (diagnostic).patientId, patient.id);
        testLog ("[IVD] submitted clonality and tracking orders");

        OrderTest orderTest = diagnostic.findOrderTest (ID_BCell2_IVD);
        login.doLogin ();
        ordersList.isCorrectPage ();
        ordersList.gotoOrderStatusPage (orderTest.orderId);
        orderStatus.isCorrectPage ();
        orderStatus.waitFor (orderTest.sampleName, ClonoSEQReport, Awaiting, CLINICAL_QC);
        orderStatus.gotoOrderDetailsPage (orderTest.orderId);
        orderDetail.isCorrectPage ();
        orderDetail.clickReportTab (ID_BCell2_IVD);
        report.isCorrectPage ();
        report.releaseReport (ID_BCell2_IVD, Pass);

        String actualPdf = join ("/", downloadDir.get (), orderTest.sampleName + ".pdf");
        coraApi.get (report.getReportUrl (), actualPdf);
        history.gotoOrderDebug (orderTest.sampleName);

        String actual = join ("/", downloadDir.get (), orderTest.sampleName, reportData);
        coraDebugApi.login ();
        coraDebugApi.get (history.getFileLocation (reportData), actual);
        ReportRender reportRender = parseReportData (actual);
        testLog ("[IVD] downloaded " + reportData);

        ClonoSeq clonoseq = basicClonoSeq (reportRender, patient, diagnostic, orderTest);
        clonoseq.helper.isIVD = true;
        clonoseq.helper.isFailed = true;
        clonoseq.pageSize = getPageCount (actualPdf);
        verifyReport (clonoseq, getTextFromPDF (actualPdf));
        testLog ("[IVD] the EOS ClonoSEQ 2.0 clonality report matched with the baseline");

        orderTest = diagnostic.findOrderTest (MRD_BCell2_IVD);
        history.gotoOrderStatusPage (orderTest.orderId);
        orderStatus.isCorrectPage ();
        orderStatus.waitFor (orderTest.sampleName, ClonoSEQReport, Awaiting, CLINICAL_QC);
        orderStatus.gotoOrderDetailsPage (orderTest.orderId);
        orderDetail.isCorrectPage ();
        orderDetail.clickReportTab (MRD_BCell2_IVD);
        report.isCorrectPage ();
        report.releaseReport (MRD_BCell2_IVD, Pass);

        actualPdf = join ("/", downloadDir.get (), orderTest.sampleName + ".pdf");
        coraApi.get (report.getReportUrl (), actualPdf);
        history.gotoOrderDebug (orderTest.sampleName);

        actual = join ("/", downloadDir.get (), orderTest.sampleName, reportData);
        coraDebugApi.get (history.getFileLocation (reportData), actual);
        reportRender = parseReportData (actual);
        testLog ("[IVD] downloaded " + reportData);

        clonoseq = basicClonoSeq (reportRender, patient, diagnostic, orderTest);
        clonoseq.helper.isIVD = true;
        clonoseq.helper.isFailed = true;
        clonoseq.pageSize = getPageCount (actualPdf);
        verifyReport (clonoseq, getTextFromPDF (actualPdf));
        testLog ("[IVD] the EOS ClonoSEQ 2.0 tracking report matched with the baseline");
    }

    /**
     * NOTE: SR-T4204
     * 
     * @sdlc.requirements SR-10414:R1
     */
    @Test (groups = "irish-wolfhound")
    public void cfDnaPlasmaNoResultAvailable () {
        Patient patient = scenarioBuilderPatient ();
        Diagnostic diagnostic = buildCdxOrder (patient,
                                               stage (Clarity, Failed),
                                               genCDxTest (MRD_BCell2_CLIA, null));
        diagnostic.specimen.sampleType = Plasma;
        diagnostic.specimen.properties.Compartment = CellFree;
        diagnostic.specimen.collectionDate = dateToArrInt (genLocalDateTime (-3));
        assertEquals (coraApi.newBcellOrder (diagnostic).patientId, patient.id);
        testLog ("[CLIA] submitted tracking orders");

        OrderTest orderTest = diagnostic.findOrderTest (MRD_BCell2_CLIA);
        testLog ("Order No: " + orderTest.orderName + ", forced status updated to Clarity -> Failed");

        login.doLogin ();
        ordersList.isCorrectPage ();
        ordersList.gotoOrderStatusPage (orderTest.orderId);
        orderStatus.isCorrectPage ();
        orderStatus.waitFor (orderTest.sampleName, ClonoSEQReport, Awaiting, CLINICAL_QC);
        orderStatus.gotoOrderDetailsPage (orderTest.orderId);
        orderDetail.isCorrectPage ();
        orderDetail.clickReportTab (MRD_BCell2_CLIA);
        report.isCorrectPage ();
        report.releaseReport (MRD_BCell2_CLIA, Pass);
        testLog ("Order Number: " + orderTest.orderName + ", Released Report, Failure Report Generated");

        String actualPdf = join ("/", downloadDir.get (), orderTest.sampleName + ".pdf");
        coraApi.get (report.getReportUrl (), actualPdf);
        history.gotoOrderDebug (orderTest.sampleName);

        coraDebugApi.login ();
        String actual = join ("/", downloadDir.get (), orderTest.sampleName, reportData);
        coraDebugApi.get (history.getFileLocation (reportData), actual);
        testLog ("downloaded " + reportData);

        ClonoSeq clonoseq = basicClonoSeq (parseReportData (actual), patient, diagnostic, orderTest);
        clonoseq.helper.isCLIA = true;
        clonoseq.pageSize = getPageCount (actualPdf);
        String extractedText = getTextFromPDF (actualPdf);
        verifyReport (clonoseq, extractedText);
        validatePdfContent (extractedText, noResultsAvailable);
        validatePdfContent (extractedText, mrdResultDescription);
    }

    /**
     * NOTE: SR-T4204
     * 
     * @sdlc.requirements SR-10414:R1
     */
    @Test (groups = "irish-wolfhound")
    public void cfDnaBloodNoResultAvailable () {
        Patient patient = scenarioBuilderPatient ();
        Diagnostic diagnostic = buildCdxOrder (patient,
                                               stage (Clarity, Failed),
                                               genCDxTest (MRD_BCell2_CLIA, null));
        diagnostic.specimen.properties.Compartment = CellFree;
        diagnostic.specimen.properties.Anticoagulant = Streck;
        assertEquals (coraApi.newBcellOrder (diagnostic).patientId, patient.id);
        testLog ("[CLIA] submitted tracking orders");

        OrderTest orderTest = diagnostic.findOrderTest (MRD_BCell2_CLIA);
        testLog ("Order No: " + orderTest.orderName + ", forced status updated to Clarity -> Failed");

        login.doLogin ();
        ordersList.isCorrectPage ();
        ordersList.gotoOrderStatusPage (orderTest.orderId);
        orderStatus.isCorrectPage ();
        orderStatus.waitFor (orderTest.sampleName, ClonoSEQReport, Awaiting, CLINICAL_QC);
        orderStatus.gotoOrderDetailsPage (orderTest.orderId);
        orderDetail.isCorrectPage ();
        orderDetail.clickReportTab (MRD_BCell2_CLIA);
        report.isCorrectPage ();
        report.releaseReport (MRD_BCell2_CLIA, Pass);
        testLog ("Order Number: " + orderTest.orderName + ", Released Report, Failure Report Generated");

        String actualPdf = join ("/", downloadDir.get (), orderTest.sampleName + ".pdf");
        coraApi.get (report.getReportUrl (), actualPdf);
        history.gotoOrderDebug (orderTest.sampleName);

        coraDebugApi.login ();
        String actual = join ("/", downloadDir.get (), orderTest.sampleName, reportData);
        coraDebugApi.get (history.getFileLocation (reportData), actual);
        testLog ("downloaded " + reportData);

        ClonoSeq clonoseq = basicClonoSeq (parseReportData (actual), patient, diagnostic, orderTest);
        clonoseq.helper.isCLIA = true;
        clonoseq.pageSize = getPageCount (actualPdf);
        String extractedText = getTextFromPDF (actualPdf);
        verifyReport (clonoseq, extractedText);
        validatePdfContent (extractedText, noResultsAvailable);
        validatePdfContent (extractedText, mrdResultDescription);
    }
}
