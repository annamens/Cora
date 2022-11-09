/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.report.clonoseq;

import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Orders.Assay.MRD_BCell2_CLIA;
import static com.adaptivebiotech.cora.utils.PageHelper.QC.Pass;
import static com.adaptivebiotech.cora.utils.PdfUtil.getPageCount;
import static com.adaptivebiotech.cora.utils.PdfUtil.getTextFromPDF;
import static com.adaptivebiotech.cora.utils.TestHelper.scenarioBuilderPatient;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.dateToArrInt;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.stage;
import static com.adaptivebiotech.test.utils.DateHelper.formatDt2;
import static com.adaptivebiotech.test.utils.DateHelper.genLocalDate;
import static com.adaptivebiotech.test.utils.DateHelper.genLocalDateTime;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.PageHelper.Compartment.CellFree;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenSource.Blood;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.Plasma;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.ClonoSEQReport;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.SecondaryAnalysis;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Awaiting;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Ready;
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
import com.adaptivebiotech.cora.ui.order.ReportClonoSeq;
import com.adaptivebiotech.picasso.dto.verify.ClonoSeq;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
@Test (groups = "regression")
public class ReportCellfreeTestSuite extends ReportTestBase {

    private Login                 login              = new Login ();
    private OrderStatus           orderStatus        = new OrderStatus ();
    private OrderDetailClonoSeq   orderDetail        = new OrderDetailClonoSeq ();
    private ReportClonoSeq        report             = new ReportClonoSeq ();
    private OrcaHistory           history            = new OrcaHistory ();
    private ThreadLocal <Boolean> cfDna              = new ThreadLocal <> ();
    private ThreadLocal <Boolean> specimenActivation = new ThreadLocal <> ();
    private ThreadLocal <String>  downloadDir        = new ThreadLocal <> ();
    private final String          tsvPathOverrideID  = "https://adaptivetestcasedata.blob.core.windows.net/selenium/tsv/postman-collection/HHTMTBGX5_0_EOS-VALIDATION_CPB_C4_L3_E11.adap.txt.results.tsv.gz";
    private final String          tsvPathOverrideMRD = azTsvPath + "/H2YHWBGXL_0_CLINICAL-CLINICAL_77898-27PC-AJP-012.adap.txt.results.tsv.gz";

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod (Method test) {
        cfDna.set (featureFlags.cfDNA);
        specimenActivation.set (featureFlags.specimenActivation);
        downloadDir.set (artifacts (this.getClass ().getName (), test.getName ()));
    }

    /**
     * NOTE: SR-T4212
     * 
     * @sdlc.requirements SR-10414:R4
     */
    @Test (groups = "irish-wolfhound")
    public void cfDnaBCellTrackingReport () {
        skipTestIfFeatureFlagOff (cfDna.get ());
        skipTestIfFeatureFlagOff (specimenActivation.get ());

        Patient patient = scenarioBuilderPatient ();
        Diagnostic diagnostic = buildCdxOrder (patient,
                                               stage (SecondaryAnalysis, Ready),
                                               genCDxTest (ID_BCell2_CLIA, tsvPathOverrideID));
        assertEquals (coraApi.newBcellOrder (diagnostic).patientId, patient.id);
        OrderTest orderTest = diagnostic.findOrderTest (ID_BCell2_CLIA);
        testLog ("Order name: " + orderTest.orderName + ", forced status updated to SecondaryAnalysis -> Ready");

        login.doLogin ();
        orderStatus.gotoOrderStatusPage (orderTest.orderId);
        orderStatus.waitFor (orderTest.sampleName, ClonoSEQReport, Awaiting, CLINICAL_QC);
        orderDetail.gotoOrderDetailsPage (orderTest.orderId);
        orderDetail.clickReportTab (ID_BCell2_CLIA);
        report.releaseReport (ID_BCell2_CLIA, Pass);
        testLog ("Order name: " + orderTest.orderName + ", Released Report, Clonality Report Generated");

        diagnostic = buildCdxOrder (patient,
                                    stage (SecondaryAnalysis, Ready),
                                    genCDxTest (MRD_BCell2_CLIA, tsvPathOverrideMRD));
        diagnostic.specimen.sampleType = Plasma;
        diagnostic.specimen.properties.SourceType = Blood;
        diagnostic.specimen.properties.Compartment = CellFree;
        diagnostic.specimen.properties.ArrivalDate = genLocalDate (-2).format (formatDt2);
        diagnostic.specimen.collectionDate = dateToArrInt (genLocalDateTime (-3));
        assertEquals (coraApi.newBcellOrder (diagnostic).patientId, patient.id);
        orderTest = diagnostic.findOrderTest (MRD_BCell2_CLIA);
        testLog ("Order name: " + orderTest.orderName + ", forced status updated to SecondaryAnalysis -> Ready");

        orderStatus.gotoOrderStatusPage (orderTest.orderId);
        orderStatus.waitFor (orderTest.sampleName, ClonoSEQReport, Awaiting, CLINICAL_QC);
        orderDetail.gotoOrderDetailsPage (orderTest.orderId);
        orderDetail.clickReportTab (MRD_BCell2_CLIA);
        report.releaseReport (MRD_BCell2_CLIA, Pass);
        testLog ("Order name: " + orderTest.orderName + ", Released Report, Tracking Report Generated");

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
        validatePdfContent (extractedText,
                            "Cell-free DNA (cfDNA)1 was extracted from plasma isolated from a blood sample.");
        validatePdfContent (extractedText,
                            "Circulating tumor DNA (ctDNA)2 is an indirect measure of residual disease and the mechanisms that contribute to the presence of ctDNA in the blood (and hence plasma) are complex.");
        validatePdfContent (extractedText,
                            "ctDNA levels are best assessed in the context of multiple measurements rather than at individual time points.");
        validatePdfContent (extractedText, "New dominant sequences are not assessed when evaluating ctDNA.");
        validatePdfContent (extractedText, "SAMPLE-LEVEL MRD TRACKING: CIRCULATING TUMOR DNA");
        validatePdfContent (extractedText, "SEQUENCE-LEVEL MRD TRACKING: CIRCULATING TUMOR DNA");
        validatePdfContent (extractedText,
                            "Patients with detectable disease in a primary tumor sample may not have detectable ctDNA in a plasma sample; the amount of ctDNA in a plasma sample may not correlate with the amount in a primary tumor.");
        validatePdfContent (extractedText,
                            "Any dominant sequence identified in a Clonality (ID) sample that is subsequently detected in a ctDNA Tracking (MRD) test and is above the assay's LOB is reported as a residual sequence.");
        validatePdfContent (extractedText, "1 Cell-free DNA (cfDNA)");
        validatePdfContent (extractedText,
                            "Comprises short (hundreds of base pairs) DNA fragments found in a variety of acellular biological fluids, including blood plasma.");
        validatePdfContent (extractedText, "2 Circulating tumor DNA (ctDNA)");
        validatePdfContent (extractedText, "The subset of cfDNA in plasma derived from tumor cells.");
        validatePdfContent (extractedText, "3 Sample Clonality");
        validatePdfContent (extractedText, "4 Total Volume (mL)");
        validatePdfContent (extractedText, "5 Total Sequences");
        validatePdfContent (extractedText, "6 Total Unique Sequences");
        validatePdfContent (extractedText, "7 Limit of Detection (LOD)");
        validatePdfContent (extractedText, "8 Limit of Quantitation (LOQ)");
    }
}
