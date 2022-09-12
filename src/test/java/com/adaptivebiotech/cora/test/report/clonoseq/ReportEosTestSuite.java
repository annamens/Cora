/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.report.clonoseq;

import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_BCell2_IVD;
import static com.adaptivebiotech.cora.dto.Orders.Assay.MRD_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Orders.Assay.MRD_BCell2_IVD;
import static com.adaptivebiotech.cora.utils.PageHelper.QC.Pass;
import static com.adaptivebiotech.cora.utils.PdfUtil.getTextFromPDF;
import static com.adaptivebiotech.cora.utils.TestHelper.scenarioBuilderPatient;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenSource.Skin;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.gDNA;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.ClonoSEQReport;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.SecondaryAnalysis;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.ShmAnalysis;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Awaiting;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Finished;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Ready;
import static com.adaptivebiotech.test.utils.PageHelper.StageSubstatus.CLINICAL_QC;
import static java.lang.String.format;
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
import com.adaptivebiotech.cora.ui.order.ReportClonoSeq;
import com.adaptivebiotech.picasso.dto.ReportRender;
import com.adaptivebiotech.picasso.dto.verify.ClonoSeq;
import com.adaptivebiotech.pipeline.dto.mrd.ClinicalJson;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
@Test (groups = "regression")
public class ReportEosTestSuite extends ReportTestBase {

    private Login                login       = new Login ();
    private OrcaHistory          history     = new OrcaHistory ();
    private ReportClonoSeq       report      = new ReportClonoSeq ();
    private ThreadLocal <String> downloadDir = new ThreadLocal <> ();

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod (Method test) {
        downloadDir.set (artifacts (this.getClass ().getName (), test.getName ()));
    }

    /**
     * NOTE: SR-T4276
     * 
     * @sdlc.requirements SR-633, SR-1017, SR-1016, SR-1014, SR-1012, SR-1011, SR-1009, SR-1007,
     *                    SR-630, SR-4103, SR-4072, SR-4073, SR-4922, SR-12121
     */
    @Test (groups = { "struay", "sanssouci", "havanese" })
    public void verify_clia_report () {
        Patient patient = scenarioBuilderPatient ();
        Diagnostic diagnostic = buildCdxOrder (patient,
                                               null,
                                               genCDxTest (ID_BCell2_CLIA, azTsvPath + "/above-loq.id.tsv.gz"),
                                               genCDxTest (MRD_BCell2_CLIA, azTsvPath + "/above-loq.mrd.tsv.gz"));
        diagnostic.specimen.sampleType = gDNA;
        diagnostic.specimen.properties.SourceType = Skin;
        assertEquals (coraApi.newBcellOrder (diagnostic).patientId, patient.id);
        testLog ("[CLIA] submitted clonality and tracking orders");

        OrderTest orderTest = diagnostic.findOrderTest (ID_BCell2_CLIA);
        login.doLogin ();
        history.gotoOrderDebug (orderTest.sampleName);
        history.forceStatusUpdate (SecondaryAnalysis, Ready);
        history.waitFor (SecondaryAnalysis, Finished);
        history.waitFor (ShmAnalysis, Finished);
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);
        history.clickOrderTest ();
        report.clickReportTab (ID_BCell2_CLIA);
        report.releaseReport (ID_BCell2_CLIA, Pass);

        String actualPdf = join ("/", downloadDir.get (), orderTest.sampleName + ".pdf");
        coraApi.get (report.getReportUrl (), actualPdf);
        history.gotoOrderDebug (orderTest.sampleName);

        String saInput = format ("CLINICAL-CLINICAL.%s.json", orderTest.sampleName);
        String saInputJson = join ("/", downloadDir.get (), orderTest.sampleName, saInput);
        coraDebugApi.login ();
        coraDebugApi.get (history.getFileLocation (saInput), saInputJson);
        testLog ("[CLIA] downloaded Analysis Config json file");

        ClinicalJson config = parseAnalysisConfig (saInputJson);
        config.patientSamples.forEach (p -> {
            assertEquals (p.specimenType, gDNA);
            assertEquals (p.specimenSource, Skin);
        });
        testLog ("[CLIA] found specimenSource in patientSamples objects in the Analysis Config json file");

        String actual = join ("/", downloadDir.get (), orderTest.sampleName, reportData);
        coraDebugApi.get (history.getFileLocation (reportData), actual);
        testLog ("[CLIA] downloaded " + reportData);

        ReportRender reportRender = parseReportData (actual);
        assertEquals (reportRender.patientInfo.reportSpecimenType, gDNA);
        assertEquals (reportRender.patientInfo.reportSpecimenSource, Skin);
        reportRender.specimenInfo.forEach (s -> {
            assertEquals (s.specimenType, gDNA);
            assertEquals (s.specimenSource, Skin);
        });
        testLog ("[CLIA] found specimenSource in specimenInfo objects in " + reportData);

        ClonoSeq clonoseq = basicClonoSeq (reportRender, patient, diagnostic, orderTest);
        clonoseq.helper.isCLIA = true;
        clonoseq.pageSize = 3;
        verifyReport (clonoseq, getTextFromPDF (actualPdf));
        testLog ("[CLIA] the EOS ClonoSEQ 2.0 clonality report matched with the baseline");

        orderTest = diagnostic.findOrderTest (MRD_BCell2_CLIA);
        history.gotoOrderDebug (orderTest.sampleName);
        history.forceStatusUpdate (SecondaryAnalysis, Ready);
        history.waitFor (SecondaryAnalysis, Finished);
        history.waitFor (ShmAnalysis, Finished);
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);
        history.clickOrderTest ();
        report.clickReportTab (MRD_BCell2_CLIA);
        report.releaseReport (MRD_BCell2_CLIA, Pass);

        actualPdf = join ("/", downloadDir.get (), orderTest.sampleName + ".pdf");
        coraApi.get (report.getReportUrl (), actualPdf);
        history.gotoOrderDebug (orderTest.sampleName);

        saInput = format ("CLINICAL-CLINICAL.%s.json", orderTest.sampleName);
        saInputJson = join ("/", downloadDir.get (), orderTest.sampleName, saInput);
        coraDebugApi.login ();
        coraDebugApi.get (history.getFileLocation (saInput), saInputJson);
        testLog ("[CLIA] downloaded Analysis Config json file");

        config = parseAnalysisConfig (saInputJson);
        config.patientSamples.forEach (p -> {
            assertEquals (p.specimenType, gDNA);
            assertEquals (p.specimenSource, Skin);
        });
        testLog ("[CLIA] found specimenSource in patientSamples objects in the Analysis Config json file");

        actual = join ("/", downloadDir.get (), orderTest.sampleName, reportData);
        coraDebugApi.get (history.getFileLocation (reportData), actual);
        testLog ("[CLIA] downloaded " + reportData);

        reportRender = parseReportData (actual);
        assertEquals (reportRender.patientInfo.reportSpecimenType, gDNA);
        assertEquals (reportRender.patientInfo.reportSpecimenSource, Skin);
        reportRender.specimenInfo.forEach (s -> {
            assertEquals (s.specimenType, gDNA);
            assertEquals (s.specimenSource, Skin);
        });
        testLog ("[CLIA] found specimenSource in specimenInfo objects in " + reportData);

        clonoseq = basicClonoSeq (reportRender, patient, diagnostic, orderTest);
        clonoseq.helper.isCLIA = true;
        clonoseq.pageSize = 4;
        verifyReport (clonoseq, getTextFromPDF (actualPdf));
        testLog ("[CLIA] the EOS ClonoSEQ 2.0 tracking report matched with the baseline");
    }

    /**
     * NOTE: SR-T4275
     * 
     * @sdlc.requirements SR-633, SR-1017, SR-1016, SR-1014, SR-1012, SR-1011, SR-1009, SR-1007,
     *                    SR-630, SR-4103, SR-4072, SR-4073, SR-5428, SR-12120
     */
    @Test (groups = { "struay", "sanssouci", "havanese" })
    public void verify_ivd_report () {
        Patient patient = scenarioBuilderPatient ();
        Diagnostic diagnostic = buildCdxOrder (patient,
                                               null,
                                               genCDxTest (ID_BCell2_IVD, azTsvPath + "/above-loq.id.tsv.gz"),
                                               genCDxTest (MRD_BCell2_IVD, azTsvPath + "/above-loq.mrd.tsv.gz"));
        diagnostic.specimen.sampleType = gDNA;
        diagnostic.specimen.properties.SourceType = Skin;
        assertEquals (coraApi.newBcellOrder (diagnostic).patientId, patient.id);
        testLog ("[IVD] submitted clonality and tracking orders");

        OrderTest orderTest = diagnostic.findOrderTest (ID_BCell2_IVD);
        login.doLogin ();
        history.gotoOrderDebug (orderTest.sampleName);
        history.forceStatusUpdate (SecondaryAnalysis, Ready);
        history.waitFor (SecondaryAnalysis, Finished);
        history.waitFor (ShmAnalysis, Finished);
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);
        history.clickOrderTest ();
        report.clickReportTab (ID_BCell2_IVD);
        report.releaseReport (ID_BCell2_IVD, Pass);

        String actualPdf = join ("/", downloadDir.get (), orderTest.sampleName + ".pdf");
        coraApi.get (report.getReportUrl (), actualPdf);
        history.gotoOrderDebug (orderTest.sampleName);

        String saInput = format ("CLINICAL-CLINICAL.%s.json", orderTest.sampleName);
        String saInputJson = join ("/", downloadDir.get (), orderTest.sampleName, saInput);
        coraDebugApi.login ();
        coraDebugApi.get (history.getFileLocation (saInput), saInputJson);
        testLog ("[IVD] downloaded Analysis Config json file");

        ClinicalJson config = parseAnalysisConfig (saInputJson);
        config.patientSamples.forEach (p -> {
            assertEquals (p.specimenType, gDNA);
            assertEquals (p.specimenSource, Skin);
        });
        testLog ("[IVD] found specimenSource in patientSamples objects in the Analysis Config json file");

        String actual = join ("/", downloadDir.get (), orderTest.sampleName, reportData);
        coraDebugApi.get (history.getFileLocation (reportData), actual);
        testLog ("[IVD] downloaded " + reportData);

        ReportRender reportRender = parseReportData (actual);
        assertEquals (reportRender.patientInfo.reportSpecimenType, gDNA);
        assertEquals (reportRender.patientInfo.reportSpecimenSource, Skin);
        reportRender.specimenInfo.forEach (s -> {
            assertEquals (s.specimenType, gDNA);
            assertEquals (s.specimenSource, Skin);
        });
        testLog ("[IVD] found specimenSource in specimenInfo objects in " + reportData);

        ClonoSeq clonoseq = basicClonoSeq (reportRender, patient, diagnostic, orderTest);
        clonoseq.helper.isIVD = true;
        clonoseq.pageSize = 3;
        verifyReport (clonoseq, getTextFromPDF (actualPdf));
        testLog ("[IVD] the EOS ClonoSEQ 2.0 clonality report matched with the baseline");

        orderTest = diagnostic.findOrderTest (MRD_BCell2_IVD);
        history.gotoOrderDebug (orderTest.sampleName);
        history.forceStatusUpdate (SecondaryAnalysis, Ready);
        history.waitFor (SecondaryAnalysis, Finished);
        history.waitFor (ShmAnalysis, Finished);
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);
        history.clickOrderTest ();
        report.clickReportTab (MRD_BCell2_IVD);
        report.releaseReport (MRD_BCell2_IVD, Pass);

        actualPdf = join ("/", downloadDir.get (), orderTest.sampleName + ".pdf");
        coraApi.get (report.getReportUrl (), actualPdf);
        history.gotoOrderDebug (orderTest.sampleName);

        saInput = format ("CLINICAL-CLINICAL.%s.json", orderTest.sampleName);
        saInputJson = join ("/", downloadDir.get (), orderTest.sampleName, saInput);
        coraDebugApi.login ();
        coraDebugApi.get (history.getFileLocation (saInput), saInputJson);
        testLog ("[IVD] downloaded Analysis Config json file");

        config = parseAnalysisConfig (saInputJson);
        config.patientSamples.forEach (p -> {
            assertEquals (p.specimenType, gDNA);
            assertEquals (p.specimenSource, Skin);
        });
        testLog ("[IVD] found specimenSource in patientSamples objects in the Analysis Config json file");

        actual = join ("/", downloadDir.get (), orderTest.sampleName, reportData);
        coraDebugApi.get (history.getFileLocation (reportData), actual);
        testLog ("[IVD] downloaded " + reportData);

        reportRender = parseReportData (actual);
        assertEquals (reportRender.patientInfo.reportSpecimenType, gDNA);
        assertEquals (reportRender.patientInfo.reportSpecimenSource, Skin);
        reportRender.specimenInfo.forEach (s -> {
            assertEquals (s.specimenType, gDNA);
            assertEquals (s.specimenSource, Skin);
        });
        testLog ("[IVD] found specimenSource in specimenInfo objects in " + reportData);

        clonoseq = basicClonoSeq (reportRender, patient, diagnostic, orderTest);
        clonoseq.helper.isIVD = true;
        clonoseq.pageSize = 4;
        verifyReport (clonoseq, getTextFromPDF (actualPdf));
        testLog ("[IVD] the EOS ClonoSEQ 2.0 tracking report matched with the baseline");
    }
}
