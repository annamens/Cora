package com.adaptivebiotech.cora.test.report;

import static com.adaptivebiotech.cora.dto.Orders.DeliveryType.CustomerShipment;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.TDetect_selfpay;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.clonoSEQ_selfpay;
import static com.adaptivebiotech.cora.utils.PageHelper.OrderType.TDx;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.diagnosticOrder;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.order;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.shipment;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.specimen;
import static com.adaptivebiotech.pipeline.utils.TestHelper.DxStatus.NEGATIVE;
import static com.adaptivebiotech.pipeline.utils.TestHelper.DxStatus.POSITIVE;
import static com.adaptivebiotech.pipeline.utils.TestHelper.Locus.BCell;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestUser;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.gDNA;
import static com.adaptivebiotech.test.utils.TestHelper.formatDt1;
import static com.adaptivebiotech.test.utils.TestHelper.formatDt7;
import static com.adaptivebiotech.test.utils.TestHelper.mapper;
import static com.seleniumfy.test.utils.Logging.info;
import static com.testautomationguru.utility.CompareMode.VISUAL_MODE;
import static java.lang.Math.abs;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;
import static java.util.logging.Level.OFF;
import static javax.imageio.ImageIO.read;
import static org.apache.commons.lang3.StringUtils.countMatches;
import static org.apache.commons.lang3.StringUtils.right;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import com.adaptivebiotech.cora.dto.AssayResponse.CoraTest;
import com.adaptivebiotech.cora.dto.Diagnostic;
import com.adaptivebiotech.cora.dto.Orders.OrderProperties;
import com.adaptivebiotech.cora.dto.Orders.OrderTest;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.dto.Physician;
import com.adaptivebiotech.cora.dto.Specimen.SpecimenProperties;
import com.adaptivebiotech.cora.dto.Workflow.Stage;
import com.adaptivebiotech.cora.dto.report.AnalysisConfig;
import com.adaptivebiotech.cora.dto.report.ClonoSeq;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.picasso.dto.ClinicalReport;
import com.adaptivebiotech.picasso.dto.ReportRender;
import com.adaptivebiotech.pipeline.dto.diagnostic.SecondaryAnalysisResult;
import com.adaptivebiotech.pipeline.dto.dx.ClassifierOutput;
import com.adaptivebiotech.pipeline.utils.TestHelper.Locus;
import com.testautomationguru.utility.PDFUtil;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class ReportTestBase extends CoraBaseBrowser {

    protected final String azTsvPath       = "https://adaptivetestcasedata.blob.core.windows.net/selenium/tsv/scenarios";
    protected final String azPipelineNorth = "https://adaptiveruopipeline.blob.core.windows.net/pipeline-results";
    protected final String azPipelineFda   = "https://adaptiveivdpipeline.blob.core.windows.net/pipeline-results";
    protected final String saResult        = "secondaryAnalysisResult.json";

    protected Diagnostic buildDiagnosticOrder (Patient patient, Stage stage, CoraTest... tests) {
        Physician physician = coraApi.getPhysician (clonoSEQ_selfpay);
        Diagnostic diagnostic = diagnosticOrder (physician, patient, specimen (), shipment ());
        diagnostic.order = order (new OrderProperties (patient.billingType, CustomerShipment, "C91.00"), tests);
        diagnostic.order.mrn = patient.mrn;
        diagnostic.specimen.collectionDate = new int[] { 2019, 4, 1, 18, 6, 59, 639 };
        diagnostic.specimen.reconciliationDate = new int[] { 2019, 5, 10, 18, 6, 59, 639 };
        diagnostic.specimen.properties = new SpecimenProperties ("2019-03-20");
        diagnostic.shipment.arrivalDate = new int[] { 2019, 4, 15, 11, 11, 59, 639 };
        diagnostic.fastForwardStatus = stage;
        return diagnostic;
    }

    protected Diagnostic buildTDetectOrder (Patient patient, Stage stage, CoraTest test) {
        Physician physician = coraApi.getPhysician (TDetect_selfpay);
        Diagnostic diagnostic = diagnosticOrder (physician, patient, null, shipment ());
        diagnostic.order = order (null, test);
        diagnostic.order.orderType = TDx;
        diagnostic.order.mrn = patient.mrn;
        diagnostic.order.billingType = patient.billingType;
        diagnostic.order.specimenDeliveryType = CustomerShipment;
        diagnostic.order.specimenDto = specimen ();
        diagnostic.order.specimenDto.name = test.workflowProperties.sampleName;
        diagnostic.order.specimenDto.properties = null;
        diagnostic.fastForwardStatus = stage;
        diagnostic.task = null;
        return diagnostic;
    }

    protected ClassifierOutput negativeCovidResult () {
        ClassifierOutput dxResult = new ClassifierOutput ();
        dxResult.disease = "COVID19";
        dxResult.classifierVersion = "v1.0";
        dxResult.dxScore = -7.854542620239307d;
        dxResult.posteriorProbability = 0.00038783501397754916d;
        dxResult.countEnhancedSeq = 69;
        dxResult.containerVersion = "dx-classifiers/covid-19:d23228f";
        dxResult.pipelineVersion = "v3.1-385-g1340003";
        dxResult.dxStatus = NEGATIVE;
        dxResult.configVersion = "dx.covid19.rev1";
        dxResult.uniqueProductiveTemplates = 858682;
        dxResult.qcFlags = asList ("COVID_UPPER_UPR_THRESHOLD");
        return dxResult;
    }

    protected ClassifierOutput positiveLymeResult () {
        ClassifierOutput dxResult = new ClassifierOutput ();
        dxResult.disease = "LYME";
        dxResult.classifierVersion = "v2.0";
        dxResult.dxScore = 105.83867731819977d;
        dxResult.countEnhancedSeq = 71;
        dxResult.containerVersion = "dx-classifiers/lyme:8532b3f";
        dxResult.pipelineVersion = "v3.1-613-g1b391bc";
        dxResult.dxStatus = POSITIVE;
        dxResult.configVersion = "dx.lyme.rev2";
        dxResult.uniqueProductiveTemplates = 72905;
        dxResult.qcFlags = new ArrayList <> ();
        return dxResult;
    }

    protected String getReport (String url, String resultFile) {
        try {
            coraApi.get (url, resultFile);
            PDFUtil pdfUtil = new PDFUtil () {
                {
                    setCompareMode (VISUAL_MODE);
                    highlightPdfDifference (true);
                    compareAllPages (true);
                    setImageDestinationPath (resultFile.replaceAll ("(.*)\\/.*\\.pdf", "$1"));
                    setLogLevel (OFF);
                }
            };
            return pdfUtil.getText (resultFile);
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
    }

    protected ClonoSeq basicClonoSeq (Patient patient, Diagnostic diagnostic, OrderTest orderTest, Locus locus) {
        ClonoSeq clonoseq = new ClonoSeq ();
        clonoseq.isEos = BCell.equals (locus);
        clonoseq.sampleType = diagnostic.specimen.sampleType.label == null ? diagnostic.specimen.properties.SourceType.label : diagnostic.specimen.sampleType.label;
        clonoseq.fromOutsideLab = gDNA.label.equals (clonoseq.sampleType);
        clonoseq.locus = locus;
        clonoseq.header.patientName = patient.fullname;
        clonoseq.header.DOB = "01/01/1999";
        clonoseq.header.medicalRecord = patient.mrn;
        clonoseq.header.gender = patient.gender;
        clonoseq.header.reportDt = formatDt1.format (now ());
        clonoseq.header.orderNum = right (orderTest.orderName, 8);
        clonoseq.header.specimen = diagnostic.specimen.sampleType.label;
        if (diagnostic.specimen.properties.SourceType != null)
            clonoseq.header.specimen += " / " + diagnostic.specimen.properties.SourceType.label;
        clonoseq.header.collectionDt = "04/01/2019";
        clonoseq.header.receivedDt = "03/20/2019";
        clonoseq.header.sampleId = orderTest.specimen.specimenNumber;
        clonoseq.header.icdCodes = fullIcdCodes (diagnostic);
        clonoseq.header.orderingPhysician = join (" ", diagnostic.provider.firstName, diagnostic.provider.lastName);
        clonoseq.header.institution = diagnostic.account.name;
        clonoseq.approval.technician = coraTestUser + ", NBCDCH-PS";
        clonoseq.approval.dateTime = formatDt7.format (now ());
        clonoseq.approval.signatureImage = "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";
        clonoseq.approval.directorName = "John Alsobrook, II, PhD, DABCC";
        return clonoseq;
    }

    protected void verifyReport (ClonoSeq clonoseq, String texts) {
        texts = texts.replaceAll ("(\\w)- ", "$1-").replaceAll (" -(\\w)", "-$1");

        // Header
        doCountMatches (texts, clonoseq.title (), clonoseq.pageSize - (clonoseq.isSHM ? clonoseq.pageSizeSHM : 0));
        if (clonoseq.isSHM)
            doCountMatches (texts, clonoseq.titleShm (), clonoseq.pageSizeSHM);
        testLog ("found the correct Title");

        // Patient
        String permHeader = clonoseq.allpagesHeader ();
        doCountMatches (texts,
                        permHeader,
                        clonoseq.pageSize - (clonoseq.isIVD && clonoseq.isSHM ? clonoseq.pageSizeSHM : 0));
        testLog ("found the complete Patient/Order Information Header");
        doCountMatches (texts, clonoseq.header.frontpageHeader (), clonoseq.isSHM ? 2 : 1);
        testLog ("found the summary Patient/Order Information Header");

        // Footer
        doCountMatches (texts, clonoseq.footer, clonoseq.pageSize);
        testLog ("found the correct Footer");

        // Criteria
        doCountMatches (texts, clonoseq.criteriaForDominant (), 1);
        if (clonoseq.isSHM)
            doCountMatches (texts, clonoseq.criteriaForDominant.shm (), 1);
        testLog ("found the correct Criteria For Defining Dominant Sequences");

        // Assay Description, Method & Limitation
        doCountMatches (texts, clonoseq.assayMethodsLimitations (), 1);
        if (clonoseq.isSHM)
            doCountMatches (texts, clonoseq.assayMethodsLimitationsSHM (), 1);
        testLog ("found the correct Assay Method and Limitations");

        // Appendix
        doCountMatches (texts, clonoseq.appendixSampleInfo (), 1);
        if (clonoseq.isSHM) {
            doCountMatches (texts, clonoseq.appendix.shmDefinition (), 1);
            doCountMatches (texts, clonoseq.appendix.refSHM (), 1);
        }
        testLog ("found the correct Appendix - Supplemental Sample Information");

        if (!clonoseq.isClonality && BCell.equals (clonoseq.locus)) {
            doCountMatches (texts, clonoseq.appendixSequenceInfo (), 1);
            testLog ("found the correct Appendix - Supplemental Sequence Information");
        }

        doCountMatches (texts, clonoseq.appendixNotes (), 1);
        testLog ("found the correct Appendix - notes");

        doCountMatches (texts, clonoseq.appendixReferences (), 1);
        testLog ("found the correct Appendix - References");

        // Approval
        doCountMatches (texts, clonoseq.approval (), clonoseq.isSHM ? 2 : 1);
        doCountMatches (texts, clonoseq.approvalDisclaimer (), clonoseq.isSHM ? 2 : 1);
        testLog ("found the correct Report Approval");
    }

    private void doCountMatches (String texts, String matches, int count) {
        String err = join ("\n", format ("Looking for: (%s) %s", count, matches), "Target:", texts);
        assertEquals (countMatches (texts, matches), count, err);
    }

    protected AnalysisConfig parseAnalysisConfig (String file) {
        return mapper.readValue (new File (file), AnalysisConfig.class);
    }

    protected ReportRender parseReportData (String file) {
        return mapper.readValue (new File (file), ReportRender.class);
    }

    protected double getDifferencePercent (String actual, String expected) {
        try {
            BufferedImage actualImg = read (new File (actual));
            BufferedImage expectedImg = read (new File (expected));
            int actualW = actualImg.getWidth ();
            int actualH = actualImg.getHeight ();
            int expectedW = expectedImg.getWidth ();
            int expectedH = expectedImg.getHeight ();
            if (actualW != expectedW || actualH != expectedH)
                fail (format ("images dimensions: (%d,%d) vs. (%d,%d)", actualW, actualH, expectedW, expectedH));

            long diff = 0;
            for (int y = 0; y < actualH; y++) {
                for (int x = 0; x < actualW; x++) {
                    diff += pixelDiff (actualImg.getRGB (x, y), expectedImg.getRGB (x, y));
                }
            }
            long maxDiff = 3L * 255 * actualW * actualH;

            return 100.0 * diff / maxDiff;
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
    }

    private int pixelDiff (int actualRGB, int aexpectedRGB) {
        int actualR = (actualRGB >> 16) & 0xff;
        int actualG = (actualRGB >> 8) & 0xff;
        int actualB = actualRGB & 0xff;
        int expectedR = (aexpectedRGB >> 16) & 0xff;
        int expectedG = (aexpectedRGB >> 8) & 0xff;
        int expectedB = aexpectedRGB & 0xff;
        return abs (actualR - expectedR) + abs (actualG - expectedG) + abs (actualB - expectedB);
    }

    private String fullIcdCodes (Diagnostic diagnostic) {
        if ("C91.00".equals (diagnostic.order.properties.Icd10Codes))
            return "C91.00 Acute lymphoblastic leukemia not having achieved remission";
        if ("C91.10".equals (diagnostic.order.properties.Icd10Codes))
            return "C91.10 Chronic lymphocytic leuk of B-cell type not achieve remis";
        return null;
    }

    protected void compareSecondaryAnalysisResults (String actual, String expected) {
        SecondaryAnalysisResult actualResult = parseSecondaryAnalysisResult (actual);
        SecondaryAnalysisResult baseResult = parseSecondaryAnalysisResult (expected);
        info ("comparing: " + mapper.writeValueAsString (actualResult));
        info ("with: " + mapper.writeValueAsString (baseResult));
        assertEquals (actualResult, baseResult);
    }

    protected ClinicalReport parseAnalysisResult (String file) {
        SecondaryAnalysisResult analysisResult = parseSecondaryAnalysisResult (file);
        return analysisResult.decode64 (analysisResult.analysisResult);
    }

    private SecondaryAnalysisResult parseSecondaryAnalysisResult (String file) {
        try {
            if (file.toString ().endsWith (".gz"))
                try (InputStream input = new GZIPInputStream (new FileInputStream (file))) {
                    return mapper.readValue (input, SecondaryAnalysisResult.class);
                }
            else
                return mapper.readValue (new File (file), SecondaryAnalysisResult.class);
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
    }
}
