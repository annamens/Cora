/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.order.tdetect;

import static com.adaptivebiotech.cora.dto.Containers.ContainerType.SlideBox5CS;
import static com.adaptivebiotech.cora.dto.Orders.Assay.COVID19_DX_IVD;
import static com.adaptivebiotech.cora.dto.Orders.OrderStatus.Active;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.TDetect_client;
import static com.adaptivebiotech.cora.utils.PageHelper.CorrectionType.Updated;
import static com.adaptivebiotech.cora.utils.PageHelper.QC.Pass;
import static com.adaptivebiotech.cora.utils.TestHelper.covidProperties;
import static com.adaptivebiotech.cora.utils.TestHelper.newClientPatient;
import static com.adaptivebiotech.cora.utils.TestHelper.sample_112770_SN_7929;
import static com.adaptivebiotech.cora.utils.TestHelper.scenarioBuilderPatient;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.buildTdetectOrder;
import static com.adaptivebiotech.pipeline.utils.TestHelper.Locus.TCRB_v4b;
import static com.adaptivebiotech.test.utils.DateHelper.convertDateFormat;
import static com.adaptivebiotech.test.utils.DateHelper.formatDt1;
import static com.adaptivebiotech.test.utils.DateHelper.genDate;
import static com.adaptivebiotech.test.utils.DateHelper.pstZoneId;
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
import static com.adaptivebiotech.test.utils.TestHelper.mapper;
import static com.adaptivebiotech.test.utils.TestHelper.randomString;
import static com.adaptivebiotech.test.utils.TestHelper.randomWords;
import static com.seleniumfy.test.utils.Logging.info;
import static java.lang.String.join;
import static java.util.Locale.US;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static org.apache.commons.lang3.StringUtils.substringBefore;
import static org.apache.commons.text.WordUtils.capitalize;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.AssayResponse.CoraTest;
import com.adaptivebiotech.cora.dto.Diagnostic;
import com.adaptivebiotech.cora.dto.Orders.Assay;
import com.adaptivebiotech.cora.dto.Orders.Order;
import com.adaptivebiotech.cora.dto.Orders.OrderTest;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.dto.Workflow.WorkflowProperties;
import com.adaptivebiotech.cora.test.order.NewOrderTestBase;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.debug.OrcaHistory;
import com.adaptivebiotech.cora.ui.order.NewOrderTDetect;
import com.adaptivebiotech.cora.ui.order.OrderDetailTDetect;
import com.adaptivebiotech.cora.ui.order.OrderStatus;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.order.ReportTDetect;
import com.adaptivebiotech.cora.ui.task.TaskDetail;
import com.adaptivebiotech.picasso.dto.ReportRender;
import com.adaptivebiotech.test.utils.PageHelper.SpecimenSource;
import com.adaptivebiotech.test.utils.PageHelper.SpecimenType;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

/**
 * @author jpatel
 *         <a href="mailto:jpatel@adaptivebiotech.com">jpatel@adaptivebiotech.com</a>
 */
@Test (groups = { "regression", "tDetectOrder" })
public class TDetectReportTestSuite extends NewOrderTestBase {

    private Login              login               = new Login ();
    private OrdersList         ordersList          = new OrdersList ();
    private NewOrderTDetect    newOrderTDetect     = new NewOrderTDetect ();
    private OrderDetailTDetect orderDetailTDetect  = new OrderDetailTDetect ();
    private ReportTDetect      reportTDetect       = new ReportTDetect ();
    private OrcaHistory        history             = new OrcaHistory ();
    private TaskDetail         taskDetail          = new TaskDetail ();
    private OrderStatus        orderStatus         = new OrderStatus ();
    private final String       todaysDate          = genDate (0, formatDt1, pstZoneId);
    private final String       todaysDateDash      = convertDateFormat (todaysDate, "MM/dd/yyyy", "yyyy-MM-dd");
    private final String       result              = "RESULT";
    private final String       expTestResult       = "NEGATIVE";
    private final String       reviewedReleasedBy  = "This report was released by an automated process.";
    private final String       approvedBy          = "John Alsobrook, II, PhD, DABCC";
    private final String       reviewSignStr       = "RELEASED BY DATE & TIME";
    private final String       approvedSignStr     = "APPROVED BY SIGNATURE DATE";
    private final String       reasonCorrectionStr = "REASON FOR CORRECTION";
    private final Assay        assayTest           = COVID19_DX_IVD;;
    private String             downloadDir;

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod (Method test) {
        downloadDir = artifacts (this.getClass ().getName (), test.getName ());
        login.doLogin ();
        ordersList.isCorrectPage ();
    }

    /**
     * NOTE: SR-T3070
     */
    public void validateTDetectReportData () {
        Patient patient = newClientPatient ();
        patient.firstName = "Test" + randomString (5);
        patient.middleName = "test" + randomString (5);
        patient.lastName = randomString (5);
        patient.mrn = randomString (10);
        String icdCode1 = "C90.00", icdCode2 = "C91.00";
        String collectionDate = genDate (-1);
        Order order = newOrderTDetect.createTDetectOrder (coraApi.getPhysician (TDetect_client),
                                                          patient,
                                                          new String[] { icdCode1, icdCode2 },
                                                          collectionDate,
                                                          assayTest,
                                                          Active,
                                                          SlideBox5CS);
        testLog ("T-Detect Order created: " + order.orderNumber);

        String patientId = orderDetailTDetect.getPatientId ();
        order = orderDetailTDetect.parseOrder ();
        String sample = order.tests.get (0).sampleName;
        history.gotoOrderDebug (sample);
        order.orderTestId = history.getOrderTestId ();

        history.setWorkflowProperties (covidProperties ());
        history.forceStatusUpdate (NorthQC, Ready);
        history.clickOrder ();
        testLog ("set workflow properties and force workflow to move to NorthQC/Ready stage");

        orderStatus.isCorrectPage ();
        orderStatus.waitFor (sample, NorthQC, Finished);
        orderStatus.waitFor (sample, DxAnalysis, Finished);
        orderStatus.waitFor (sample, DxContamination, Finished);
        orderStatus.waitFor (sample, DxReport, Finished);
        history.gotoOrderDebug (sample);
        ReportRender reportDataJson = parseReportDataJson (history.getFileUrl ("reportData.json"));
        validateReportDataJson (reportDataJson, order, patientId);
        assertFalse (reportDataJson.patientInfo.isCorrected);
        assertNull (reportDataJson.commentInfo.comments);
        assertNull (reportDataJson.commentInfo.correctionReason);
        assertNull (reportDataJson.commentInfo.clinicalConsultantName);
        assertNull (reportDataJson.commentInfo.clinicalConsultantTitle);
        assertEquals (reportDataJson.commentInfo.labDirectorName, approvedBy);
        assertTrue (reportDataJson.commentInfo.signatureImage.isEmpty ());
        assertNull (reportDataJson.previousReportDate);
        testLog ("STEP 2 - validate reportData.json file");

        // navigate to order status page
        history.clickOrderTest ();
        orderStatus.isCorrectPage ();
        orderStatus.clickReportTab (assayTest);
        reportTDetect.isCorrectPage ();

        String fileContent = getTextFromPDF (reportTDetect.getReleasedReportPdfUrl (), 1);
        validateReportContent (fileContent, order);
        validatePdfContent (fileContent, result);
        validatePdfContent (fileContent, expTestResult);
        validatePdfContent (fileContent, reviewSignStr);
        validatePdfContent (fileContent, reviewedReleasedBy + " " + todaysDate);
        validatePdfContent (fileContent, approvedSignStr);
        validatePdfContent (fileContent, approvedBy + " " + todaysDate);
        testLog ("STEP 3 - validate released report");

        history.gotoOrderDebug (sample);
        assertEquals (parseReportDataJson (history.getFileUrl ("reportData.json")), reportDataJson);
        testLog ("STEP 4 - validate released reportData.json file");

        history.waitFor (ReportDelivery, Finished);
        history.clickOrderTest ();
        orderStatus.isCorrectPage ();
        orderStatus.clickReportTab (assayTest);
        reportTDetect.isCorrectPage ();
        reportTDetect.clickCorrectReport ();
        reportTDetect.selectCorrectionType (Updated);
        String correctedReason = "testing corrected report";
        reportTDetect.enterReasonForCorrection (correctedReason);
        reportTDetect.clickSaveAndUpdate ();
        reportTDetect.clickEditJson ();

        ReportRender editReportDataJson = mapper.readValue (reportTDetect.getReportDataJson (), ReportRender.class);
        validateReportDataJson (editReportDataJson, order, patientId);
        assertTrue (editReportDataJson.patientInfo.isCorrected);
        assertNull (editReportDataJson.commentInfo.comments);
        assertEquals (editReportDataJson.commentInfo.correctionReason, correctedReason);
        assertEquals (editReportDataJson.previousReportDate.toString (), todaysDateDash);
        testLog ("STEP 5 - The above JSON properties are listed with the values matching the tables below.");

        String updatedPatientName = "Updated Patient " + randomWords (1);
        editReportDataJson.patientInfo.name = updatedPatientName;
        order.patient.fullname = updatedPatientName;
        reportTDetect.setReportDataJson (editReportDataJson.toString ());
        reportTDetect.releaseReportWithSignatureRequired ();

        String correctedPreviewPdfUrl = reportTDetect.getReleasedReportPdfUrl ();
        String correctedReleasePdfContent = getTextFromPDF (correctedPreviewPdfUrl, 1);
        validateReportContent (correctedReleasePdfContent, order);
        validatePdfContent (correctedReleasePdfContent, reasonCorrectionStr);
        validatePdfContent (correctedReleasePdfContent, correctedReason);
        testLog ("STEP 7.1 - The report pdf Page 1 contains values for the following fields as listed below");

        correctedReleasePdfContent = getTextFromPDF (correctedPreviewPdfUrl, 2);
        validateReportContent (correctedReleasePdfContent, order);
        validatePdfContent (correctedReleasePdfContent, result);
        validatePdfContent (correctedReleasePdfContent, expTestResult);
        validatePdfContent (correctedReleasePdfContent, reviewSignStr);
        validatePdfContent (correctedReleasePdfContent, reviewedReleasedBy + " " + todaysDate);
        validatePdfContent (correctedReleasePdfContent, approvedSignStr);
        validatePdfContent (correctedReleasePdfContent, approvedBy + " " + todaysDate);
        testLog ("STEP 7.2 - The report pdf Page 2 contains additional values for the following fields as listed below");

        taskDetail.gotoTaskDetail (reportTDetect.getCorrectedReportTaskId ());
        taskDetail.isCorrectPage ();
        assertTrue (taskDetail.taskFiles ().containsKey ("reportData.json"));

        reportDataJson = parseReportDataJson (taskDetail.taskFiles ().get ("reportData.json"));
        validateReportDataJson (reportDataJson, order, patientId);
        assertNull (reportDataJson.commentInfo.comments);
        assertEquals (reportDataJson.commentInfo.correctionReason, correctedReason);
        assertEquals (reportDataJson.commentInfo.clinicalConsultantName, "svc_cora_test_phi_preprod, NBCDCH-PS");
        assertNull (reportDataJson.commentInfo.clinicalConsultantTitle);
        assertEquals (reportDataJson.commentInfo.labDirectorName, approvedBy);
        assertTrue (reportDataJson.commentInfo.signedAt.toString ().startsWith (todaysDateDash));
        assertFalse (reportDataJson.commentInfo.signatureImage.isEmpty ());
        assertEquals (reportDataJson.previousReportDate.toString (), todaysDateDash);
        testLog ("STEP 8 - validate released reportData.json file");
    }

    /**
     * NOTE: SR-T3070
     */
    public void validateFailedTDetectReportData () {
        WorkflowProperties sample_112770_SN_7929 = new WorkflowProperties ();
        sample_112770_SN_7929.flowcell = "HCYJNBGXJ";
        sample_112770_SN_7929.workspaceName = "CLINICAL-CLINICAL";
        sample_112770_SN_7929.sampleName = "112770-SN-7929";

        CoraTest test = coraApi.getTDxTest (COVID19_DX_IVD);
        test.workflowProperties = sample_112770_SN_7929;

        Patient patient = scenarioBuilderPatient ();
        Diagnostic diagnostic = buildTdetectOrder (coraApi.getPhysician (TDetect_client),
                                                   patient,
                                                   null,
                                                   test,
                                                   COVID19_DX_IVD);
        diagnostic.dxResults = null;
        assertEquals (coraApi.newTdetectOrder (diagnostic).patientId, patient.id);
        testLog ("submitted a new Covid19 order in Cora");

        OrderTest orderTest = diagnostic.findOrderTest (COVID19_DX_IVD);
        orderStatus.gotoOrderStatusPage (orderTest.orderId);
        orderStatus.isCorrectPage ();
        orderStatus.failWorkflow (orderTest.sampleName, "testing failure report");
        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (DxReport, Awaiting, CLINICAL_QC);

        orderDetailTDetect.gotoOrderDetailsPage (orderTest.orderId);
        orderDetailTDetect.isCorrectPage ();
        orderDetailTDetect.clickReportTab (assayTest);
        reportTDetect.isCorrectPage ();
        reportTDetect.setQCstatus (Pass);
        reportTDetect.clickReleaseReport ();
        history.gotoOrderDebug (orderTest.sampleName);
        ReportRender reportDataJson = parseReportDataJson (history.getFileUrl ("reportData.json"));
        assertTrue (reportDataJson.isFailure);
        testLog ("STEP 11 - validate reportData.json displays isFailure as true");
    }

    private void validateReportDataJson (ReportRender reportDataJson, Order order, String patientId) {
        assertEquals (reportDataJson.klass, "com.adaptive.clonoseqreport.dtos.ReportRenderDto");
        assertEquals (reportDataJson.version.intValue (), 1);
        assertEquals (reportDataJson.patientInfo.id, patientId);
        assertEquals (reportDataJson.patientInfo.name, order.patient.fullname);
        assertEquals (reportDataJson.patientInfo.dob.toString (),
                      convertDateFormat (order.patient.dateOfBirth, "MM/dd/yyyy", "yyyy-MM-dd"));
        assertEquals (reportDataJson.patientInfo.mrn, order.patient.mrn);
        assertEquals (reportDataJson.patientInfo.gender, order.patient.gender);

        assertEquals (reportDataJson.patientInfo.reportSpecimenSource, order.specimenDto.sampleSource);
        assertEquals (reportDataJson.patientInfo.reportSpecimenType, order.specimenDto.sampleType);
        assertEquals (reportDataJson.patientInfo.reportSpecimenCompartment, "Cellular");
        assertEquals (reportDataJson.patientInfo.reportSpecimenId, order.specimenDto.specimenNumber);
        assertEquals (reportDataJson.patientInfo.reportLocus, TCRB_v4b);
        assertEquals (reportDataJson.patientInfo.reportSpecimenCollectionDate.toString (),
                      convertDateFormat (order.specimenDto.collectionDate.toString (),
                                         "MM/dd/yyyy",
                                         "yyyy-MM-dd"));
        assertEquals (reportDataJson.patientInfo.reportSpecimenArrivalDate.toString (),
                      convertDateFormat (order.specimenDto.arrivalDate.split ("\\s+")[0],
                                         "MM/dd/yyyy",
                                         "yyyy-MM-dd"));
        assertEquals (order.orderTestId, reportDataJson.patientInfo.reportSampleOrderTestId);
        assertEquals (reportDataJson.patientInfo.orderNumber, order.orderNumber);
        assertEquals (reportDataJson.patientInfo.institutionName, order.physician.accountName);
        assertEquals (reportDataJson.patientInfo.orderingPhysician, order.physician.providerFullName);
        assertEquals (order.icdcodes.size (), 2);
        assertTrue (reportDataJson.patientInfo.icd10Codes.contains (order.icdcodes.get (0).replaceAll ("\\s+", " ")));
        assertTrue (reportDataJson.patientInfo.icd10Codes.contains (order.icdcodes.get (1).replaceAll ("\\s+", " ")));
        assertEquals (reportDataJson.patientInfo.reportDate.toString (), todaysDateDash);

        assertEquals (reportDataJson.patientInfo.receptorFamily, "TCell");
        assertEquals (reportDataJson.patientInfo.isClonoSEQV1, false);
        assertEquals (reportDataJson.patientInfo.isIuo, false);
        assertEquals (reportDataJson.patientInfo.localeCode, US);
        assertNull (reportDataJson.patientInfo.patientId);
        assertNull (reportDataJson.patientInfo.reportNumber);
        assertEquals (reportDataJson.patientInfo.klass, "com.adaptive.clonoseqreport.dtos.PatientInfoDto");
        assertEquals (reportDataJson.dxResult, sample_112770_SN_7929 ());
        assertEquals (reportDataJson.commentInfo.klass, "com.adaptive.clonoseqreport.dtos.ReportCommentDto");
        assertEquals (reportDataJson.commentInfo.version, 1);
        assertNull (reportDataJson.commentInfo.clinicalConsultantTitle);
        assertEquals (reportDataJson.commentInfo.labDirectorName, approvedBy);
        assertEquals (reportDataJson.isFailure, Boolean.valueOf (false));
    }

    private void validateReportContent (String fileContent, Order order) {
        validatePdfContent (fileContent,
                            join (" ",
                                  "PATIENT NAME",
                                  "DATE OF BIRTH",
                                  "MEDICAL RECORD #",
                                  "GENDER",
                                  "REPORT DATE",
                                  "ORDER #"));
        validatePdfContent (fileContent, capitalize (order.patient.fullname));
        validatePdfContent (fileContent, order.patient.dateOfBirth);
        validatePdfContent (fileContent, order.patient.mrn);
        validatePdfContent (fileContent, order.patient.gender);
        validatePdfContent (fileContent, todaysDate + " " + order.orderNumber);

        validatePdfContent (fileContent,
                            join (" ",
                                  "SPECIMEN TYPE / SPECIMEN SOURCE",
                                  "COLLECTION DATE",
                                  "DATE RECEIVED",
                                  "SAMPLE ID"));
        validatePdfContent (fileContent,
                            join (" ",
                                  SpecimenType.Blood + " / " + SpecimenSource.Blood,
                                  order.specimenDto.collectionDate.toString (),
                                  order.specimenDto.arrivalDate.split ("\\s+")[0],
                                  order.specimenDto.specimenNumber));

        validatePdfContent (fileContent, "ICD CODE(S)");
        assertEquals (order.icdcodes.size (), 2);
        validatePdfContent (fileContent, order.icdcodes.get (0).replaceAll ("\\s+", " "));
        validatePdfContent (fileContent, order.icdcodes.get (1).replaceAll ("\\s+", " "));

        validatePdfContent (fileContent, "ORDERING PHYSICIAN INSTITUTION");
        validatePdfContent (fileContent, order.physician.accountName);
        validatePdfContent (fileContent, order.physician.providerFullName);
    }

    private String getTextFromPDF (String url, int pageNumber) {
        String pdfFileLocation = join ("/", downloadDir, substringBefore (substringAfterLast (url, "/"), "?"));
        info ("PDF File Location: " + pdfFileLocation);

        // get file from URL and save it
        coraApi.get (url, pdfFileLocation);

        // read PDF and extract text
        PdfReader reader = null;
        String fileContent = null;
        try {
            reader = new PdfReader (pdfFileLocation);
            fileContent = PdfTextExtractor.getTextFromPage (reader, pageNumber);
        } catch (IOException e) {
            throw new RuntimeException (e);
        } finally {
            reader.close ();
        }
        return fileContent;
    }

    /**
     * go to debug page, parse reportData.json file
     * 
     * @return ReportRender object for reportData.json file
     */
    private ReportRender parseReportDataJson (String fileUrl) {
        String reportDataJson = join ("/", downloadDir, "reportData.json");
        coraDebugApi.login ();
        coraDebugApi.get (fileUrl, reportDataJson);
        return mapper.readValue (new File (reportDataJson), ReportRender.class);
    }

    private void validatePdfContent (String fileContent, String stringToValidate) {
        fileContent = fileContent.replace ("\n", " ");
        info ("Validate: " + stringToValidate + ", in: " + fileContent);
        assertTrue (fileContent.contains (stringToValidate));
    }
}
