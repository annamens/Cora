/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.report;

import static com.adaptivebiotech.cora.dto.Orders.DeliveryType.CustomerShipment;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.TDetect_selfpay;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.clonoSEQ_selfpay;
import static com.adaptivebiotech.cora.utils.PageHelper.OrderType.TDx;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.diagnosticOrder;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.order;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.shipment;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.specimen;
import static com.adaptivebiotech.pipeline.dto.dx.ClassifierOutput.DiseaseType.LYME;
import static com.adaptivebiotech.pipeline.utils.TestHelper.DxStatus.POSITIVE;
import static com.adaptivebiotech.pipeline.utils.TestHelper.Locus.BCell;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestUser;
import static com.adaptivebiotech.test.utils.DateHelper.formatDt1;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.gDNA;
import static com.adaptivebiotech.test.utils.TestHelper.mapper;
import static com.seleniumfy.test.utils.Logging.info;
import static java.lang.Math.abs;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.time.LocalDate.parse;
import static java.time.LocalDateTime.now;
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
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.picasso.dto.ClinicalReport;
import com.adaptivebiotech.picasso.dto.ReportRender;
import com.adaptivebiotech.picasso.dto.verify.ClonoSeq;
import com.adaptivebiotech.pipeline.dto.diagnostic.SecondaryAnalysisResult;
import com.adaptivebiotech.pipeline.dto.dx.ClassifierOutput;
import com.adaptivebiotech.pipeline.dto.mrd.ClinicalJson;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class ReportTestBase extends CoraBaseBrowser {

    protected final String saResult   = "secondaryAnalysisResult.json";
    protected final String reportData = "reportData.json";

    protected Diagnostic buildCdxOrder (Patient patient, Stage stage, CoraTest... tests) {
        return buildCdxOrder (patient, "C91.00", stage, tests);
    }

    protected Diagnostic buildCdxOrder (Patient patient, String icdcodes, Stage stage, CoraTest... tests) {
        Physician physician = coraApi.getPhysician (clonoSEQ_selfpay);
        Diagnostic diagnostic = diagnosticOrder (physician, patient, specimen (), shipment ());
        diagnostic.order = order (new OrderProperties (patient.billingType, CustomerShipment, icdcodes), tests);
        diagnostic.order.mrn = patient.mrn;
        diagnostic.specimen.collectionDate = new int[] { 2019, 4, 1, 18, 6, 59, 639 };
        diagnostic.specimen.reconciliationDate = new int[] { 2019, 5, 10, 18, 6, 59, 639 };
        diagnostic.specimen.properties = new SpecimenProperties ("2019-03-20");
        diagnostic.shipment.arrivalDate = new int[] { 2019, 4, 15, 11, 11, 59, 639 };
        diagnostic.fastForwardStatus = stage;
        return diagnostic;
    }

    protected Diagnostic buildTdxOrder (Patient patient, Stage stage, CoraTest test) {
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

    protected ClassifierOutput positiveLymeResult () {
        ClassifierOutput dxResult = new ClassifierOutput ();
        dxResult.disease = LYME;
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

    protected ClonoSeq basicClonoSeq (ReportRender report,
                                      Patient patient,
                                      Diagnostic diagnostic,
                                      OrderTest orderTest) {
        ClonoSeq clonoseq = new ClonoSeq (report);
        clonoseq.helper.sampleType = diagnostic.specimen.sampleType.label == null ? diagnostic.specimen.properties.SourceType.label : diagnostic.specimen.sampleType.label;
        clonoseq.helper.fromOutsideLab = gDNA.label.equals (clonoseq.helper.sampleType);
        clonoseq.helper.report.commentInfo.clinicalConsultantName = coraTestUser + ", NBCDCH-PS";
        clonoseq.helper.report.commentInfo.labDirectorName = "John Alsobrook, II, PhD, DABCC";
        clonoseq.header.patientName = patient.fullname;
        clonoseq.header.DOB = parse ("01/01/1999", formatDt1);
        clonoseq.header.medicalRecord = patient.mrn;
        clonoseq.header.gender = patient.gender;
        clonoseq.header.reportDt = now ().toLocalDate ();
        clonoseq.header.orderNum = right (orderTest.orderName, 8);
        clonoseq.header.specimen = diagnostic.specimen.sampleType.label;
        if (diagnostic.specimen.properties.SourceType != null)
            clonoseq.header.specimen += " / " + diagnostic.specimen.properties.SourceType.label;
        clonoseq.header.collectionDt = parse ("04/01/2019", formatDt1);
        clonoseq.header.receivedDt = parse ("03/20/2019", formatDt1);
        clonoseq.header.sampleId = orderTest.specimen.specimenNumber;
        clonoseq.header.icdCodes = fullIcdCodes (diagnostic);
        clonoseq.header.orderingPhysician = join (" ", diagnostic.provider.firstName, diagnostic.provider.lastName);
        clonoseq.header.institution = diagnostic.account.name;
        return clonoseq;
    }

    protected void verifyReport (ClonoSeq clonoseq, String texts) {
        texts = texts.replaceAll ("(\\w)- ", "$1-").replaceAll (" -(\\w)", "-$1").replaceAll ("\\s+", " ");

        // Header
        doCountMatches (texts,
                        clonoseq.header.title (),
                        clonoseq.pageSize - (clonoseq.helper.isSHM ? 2 : 0));
        if (clonoseq.helper.isSHM)
            doCountMatches (texts, clonoseq.header.titleShm (), 2);
        testLog ("found the correct Title");

        // Patient
        String permHeader = clonoseq.header.allpages ();
        doCountMatches (texts,
                        permHeader,
                        clonoseq.pageSize - (clonoseq.helper.isIVD && clonoseq.helper.isSHM ? 2 : 0));
        testLog ("found the complete Patient/Order Information Header");
        doCountMatches (texts, clonoseq.header.frontpage (), clonoseq.helper.isSHM ? 2 : 1);
        testLog ("found the summary Patient/Order Information Header");

        // Footer
        doCountMatches (texts, clonoseq.footer, clonoseq.pageSize);
        testLog ("found the correct Footer");

        // Result
        doCountMatches (texts, clonoseq.result.text (), 1);
        testLog ("found the correct Result");

        // Criteria
        doCountMatches (texts, clonoseq.criteriaForDominant.text (), clonoseq.helper.isFailed ? 0 : 1);
        if (clonoseq.helper.isSHM)
            doCountMatches (texts, clonoseq.criteriaForDominant.shm (), 1);
        testLog ("found the correct Criteria For Defining Dominant Sequences");

        // Assay Description, Method & Limitation
        doCountMatches (texts, clonoseq.limitations.text (), 1);
        if (clonoseq.helper.isSHM)
            doCountMatches (texts, clonoseq.limitations.textSHM (), 1);
        testLog ("found the correct Assay Method and Limitations");

        // Appendix
        doCountMatches (texts, clonoseq.appendix.sampleInfo (), clonoseq.helper.isFailed ? 0 : 1);
        if (clonoseq.helper.isSHM) {
            doCountMatches (texts, clonoseq.appendix.shmDefinition (), 1);
            doCountMatches (texts, clonoseq.appendix.refSHM (), 1);
        }
        testLog ("found the correct Appendix - Supplemental Sample Information");

        if (!clonoseq.helper.isClonality && BCell.equals (clonoseq.helper.locus) && !clonoseq.helper.isFailed) {
            doCountMatches (texts, clonoseq.appendix.sequenceInfo (), 1);
            testLog ("found the correct Appendix - Supplemental Sequence Information");
        }

        doCountMatches (texts, clonoseq.appendix.notes (), clonoseq.helper.isFailed ? 0 : 1);
        testLog ("found the correct Appendix - notes");

        doCountMatches (texts, clonoseq.appendix.references (), clonoseq.helper.isFailed ? 0 : 1);
        testLog ("found the correct Appendix - References");

        // Approval
        doCountMatches (texts, clonoseq.approval.text (), clonoseq.helper.isSHM ? 2 : 1);
        testLog ("found the correct Report Approval");
    }

    private void doCountMatches (String texts, String matches, int count) {
        String err = join ("\n", format ("Looking for: (%s) %s", count, matches), "Target:", texts);
        assertEquals (countMatches (texts, matches), count, err);
    }

    protected ClinicalJson parseAnalysisConfig (String file) {
        return mapper.readValue (new File (file), ClinicalJson.class);
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
