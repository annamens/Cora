package com.adaptivebiotech.cora.test.order;

import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.TestHelper.mapper;
import static com.adaptivebiotech.test.utils.TestHelper.randomString;
import static com.adaptivebiotech.test.utils.TestHelper.randomWords;
import static com.seleniumfy.test.utils.HttpClientHelper.get;
import static com.seleniumfy.test.utils.HttpClientHelper.headers;
import static com.seleniumfy.test.utils.HttpClientHelper.resetheaders;
import static java.lang.String.join;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;
import org.apache.http.message.BasicHeader;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Orders.Order;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.OrderDetailTDetect;
import com.adaptivebiotech.cora.ui.order.OrderStatus;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.patient.PatientDetail;
import com.adaptivebiotech.cora.ui.task.TaskDetail;
import com.adaptivebiotech.cora.ui.task.TaskList;
import com.adaptivebiotech.cora.ui.workflow.History;
import com.adaptivebiotech.cora.utils.DateUtils;
import com.adaptivebiotech.cora.utils.PageHelper.CorrectionType;
import com.adaptivebiotech.cora.utils.TestHelper;
import com.adaptivebiotech.picasso.dto.ReportRender;
import com.adaptivebiotech.pipeline.utils.TestHelper.DxStatus;
import com.adaptivebiotech.pipeline.utils.TestHelper.Locus;
import com.adaptivebiotech.test.utils.Logging;
import com.adaptivebiotech.test.utils.PageHelper.Assay;
import com.adaptivebiotech.test.utils.PageHelper.ChargeType;
import com.adaptivebiotech.test.utils.PageHelper.ContainerType;
import com.adaptivebiotech.test.utils.PageHelper.QC;
import com.adaptivebiotech.test.utils.PageHelper.SpecimenSource;
import com.adaptivebiotech.test.utils.PageHelper.SpecimenType;
import com.adaptivebiotech.test.utils.PageHelper.StageName;
import com.adaptivebiotech.test.utils.PageHelper.StageStatus;
import com.adaptivebiotech.test.utils.PageHelper.StageSubstatus;
import com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.seleniumfy.test.utils.HttpClientHelper;

/**
 * @author jpatel
 *         <a href="mailto:jpatel@adaptivebiotech.com">jpatel@adaptivebiotech.com</a>
 */
@Test (groups = { "regression", "tDetectOrder" })
public class TDetectReportTestSuite extends CoraBaseBrowser {

    private OrderDetailTDetect orderDetailTDetect  = new OrderDetailTDetect ();
    private PatientDetail      patientDetail       = new PatientDetail ();
    private History            history             = new History ();
    private TaskList           taskList            = new TaskList ();
    private TaskDetail         task                = new TaskDetail ();
    private OrderStatus        orderStatus         = new OrderStatus ();

    private String             downloadDir;
    private final String       todaysDate          = DateUtils.getPastFutureDate (0,
                                                                                  DateTimeFormatter.ofPattern ("MM/dd/uuuu"),
                                                                                  DateUtils.pstZoneId);
    private final String       todaysDateDash      = DateUtils.convertDateFormat (todaysDate,
                                                                                  "MM/dd/yyyy",
                                                                                  "yyyy-MM-dd");
    private final String       result              = "RESULT";
    private final String       expTestResult       = "POSITIVE";
    private final String       reviewedReleasedBy  = "svc_cora_test_phi_preprod, NBCDCH-PS";
    private final String       approvedBy          = "Stephanie Hallam, Ph.D., D(ABMGG)";

    private final String       lastAcceptedTsvPath = "s3://pipeline-north-production-archive/200613_NB551725_0151_AHM7N7BGXF/v3.1/20200615_1438/packaged/rd.Human.TCRB-v4b.nextseq.156x12x0.vblocks.ultralight.rev1/HM7N7BGXF_0_Hospital12deOctubre-MartinezLopez_860011348.adap.txt.results.tsv.gz";
    private final String       workspaceName       = "Hospital12deOctubre-MartinezLopez";
    private final String       sampleNameOverride  = "860011348";
    private final String       reviewText          = "REVIEWED AND RELEASED BY DATE & TIME";
    private final String       reviewSignStr       = "REVIEWED AND RELEASED BY SIGNATURE DATE & TIME";
    private final String       approvedSignStr     = "APPROVED BY SIGNATURE DATE";
    private final String       reasonCorrectionStr = "REASON FOR CORRECTION";
    private final String       addCommentsStr      = "ADDITIONAL COMMENTS";

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod (Method test) {
        downloadDir = artifacts (this.getClass ().getName (), test.getName ());
        new Login ().doLogin ();
        new OrdersList ().isCorrectPage ();
    }

    /**
     * NOTE: SR-T3070
     */
    public void validateTDetectReportData () {
        Assay assayTest = Assay.COVID19_DX_IVD;
        com.adaptivebiotech.test.utils.PageHelper.OrderStatus active = com.adaptivebiotech.test.utils.PageHelper.OrderStatus.Active;
        Patient patient = TestHelper.newPatient ();
        patient.firstName = "Test" + randomString (5);
        patient.middleName = "test" + randomString (5);
        patient.lastName = randomString (5);
        patient.mrn = randomString (10);
        String icdCode1 = "C90.00", icdCode2 = "C91.00";
        String collectionDate = DateUtils.getPastFutureDate (-1);
        String orderNum = orderDetailTDetect.createTDetectOrder (TestHelper.physicianTRF (),
                                                                 patient,
                                                                 new String[] { icdCode1, icdCode2 },
                                                                 collectionDate,
                                                                 assayTest,
                                                                 ChargeType.Client,
                                                                 TestHelper.getRandomAddress (),
                                                                 active,
                                                                 ContainerType.SlideBox5CS);
        Logging.testLog ("T-Detect Order created: " + orderNum);

        orderDetailTDetect.clickPatientCode (active);
        patientDetail.isCorrectPage ();
        String patientId = patientDetail.getPatientId ();
        orderDetailTDetect.navigateToTab (0);
        orderDetailTDetect.isCorrectPage ();

        Order order = orderDetailTDetect.parseOrder (active);

        String sampleName = order.tests.get (0).sampleName;
        history.gotoOrderDebug (sampleName);
        String orderTestId = history.getOrderTestId ();

        history.setWorkflowProperty (WorkflowProperty.lastAcceptedTsvPath, lastAcceptedTsvPath);
        history.setWorkflowProperty (WorkflowProperty.workspaceName, workspaceName);
        history.setWorkflowProperty (WorkflowProperty.sampleName, sampleNameOverride);

        history.forceStatusUpdate (StageName.DxAnalysis, StageStatus.Ready);

        history.waitFor (StageName.DxContamination, StageStatus.Stuck);
        history.forceStatusUpdate (StageName.DxContamination, StageStatus.Finished);

        history.waitFor (StageName.DxReport, StageStatus.Awaiting, StageSubstatus.CLINICAL_QC);

        history.isCorrectPage ();
        history.clickOrderTest ();
        orderDetailTDetect.isOrderStatusPage ();
        orderDetailTDetect.clickReportTab (assayTest);

        orderDetailTDetect.setQCstatus (QC.Pass);

        String pdfUrl = orderDetailTDetect.getPreviewReportPdfUrl ();
        testLog ("PDF File URL: " + pdfUrl);
        String fileContent = getTextFromPDF (pdfUrl, 1);
        validateReportContent (fileContent, order);
        assertTrue (fileContent.contains (result));
        assertTrue (fileContent.contains (expTestResult));
        assertTrue (fileContent.contains (reviewText));
        assertTrue (fileContent.contains (approvedSignStr));
        assertTrue (fileContent.contains (approvedBy));
        Logging.testLog ("STEP 1 - validate preview report");

        history.gotoOrderDebug (sampleName);
        String reportDataJsonFileUrl = history.getFileUrl ("reportData.json");
        ReportRender reportDataJson = getReportDataJsonFile (reportDataJsonFileUrl);
        validateReportDataJson (reportDataJson, order, patientId, orderTestId);
        assertEquals (reportDataJson.patientInfo.isCorrected, false);
        assertNull (reportDataJson.commentInfo.comments);
        assertNull (reportDataJson.commentInfo.correctionReason);
        assertNull (reportDataJson.commentInfo.clinicalConsultantName);
        assertTrue (reportDataJson.commentInfo.signatureImage.isEmpty ());
        assertNull (reportDataJson.previousReportDate);
        Logging.testLog ("STEP 2 - validate reportData.json file");

        // navigate to order status page
        history.clickOrderTest ();
        orderDetailTDetect.isOrderStatusPage ();
        orderDetailTDetect.clickReportTab (assayTest);
        String reportNotes = "testing report notes";
        orderDetailTDetect.enterReportNotes (reportNotes);
        String additionalComments = "testing additional comments";
        orderDetailTDetect.enterAdditionalComments (additionalComments);
        orderDetailTDetect.clickSaveAndUpdate ();
        orderDetailTDetect.releaseReport ();

        String releasePdfUrl = orderDetailTDetect.getReleasedReportPdfUrl ();
        testLog ("PDF File URL: " + releasePdfUrl);
        fileContent = getTextFromPDF (releasePdfUrl, 1);
        validateReportContent (fileContent, order);
        assertTrue (fileContent.contains (result));
        assertTrue (fileContent.contains (expTestResult));
        assertTrue (fileContent.contains (reviewSignStr));
        assertTrue (fileContent.contains (reviewedReleasedBy + " " + todaysDate));
        assertTrue (fileContent.contains (approvedSignStr));
        assertTrue (fileContent.contains (approvedBy + " " + todaysDate));
        Logging.testLog ("STEP 3 - validate released report");

        history.gotoOrderDebug (sampleName);
        reportDataJsonFileUrl = history.getFileUrl ("reportData.json");
        reportDataJson = getReportDataJsonFile (reportDataJsonFileUrl);
        validateReportDataJson (reportDataJson, order, patientId, orderTestId);
        assertEquals (reportDataJson.patientInfo.isCorrected, false);
        assertEquals (reportDataJson.commentInfo.comments, additionalComments);
        assertNull (reportDataJson.commentInfo.correctionReason);
        assertEquals (reportDataJson.commentInfo.clinicalConsultantName, reviewedReleasedBy);
        assertTrue (reportDataJson.commentInfo.signedAt.toString ()
                                                       .startsWith (todaysDateDash));
        assertNotNull (reportDataJson.commentInfo.signatureImage);
        assertNull (reportDataJson.previousReportDate);
        Logging.testLog ("STEP 4 - validate released reportData.json file");

        history.waitFor (StageName.ReportDelivery, StageStatus.Finished);
        history.clickOrderTest ();
        orderDetailTDetect.isOrderStatusPage ();
        orderDetailTDetect.clickReportTab (assayTest);
        orderDetailTDetect.clickCorrectReport ();
        orderDetailTDetect.selectCorrectionType (CorrectionType.Updated);
        String correctedReason = "testing corrected report";
        orderDetailTDetect.enterReasonForCorrection (correctedReason);
        orderDetailTDetect.clickSaveAndUpdate ();
        orderDetailTDetect.clickEditJson ();
        ReportRender editReportDataJson = null;
        try {
            editReportDataJson = mapper.readValue (orderDetailTDetect.getReportDataJson (),
                                                   ReportRender.class);
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
        validateReportDataJson (editReportDataJson, order, patientId, orderTestId);
        assertEquals (editReportDataJson.patientInfo.isCorrected, true);
        assertEquals (editReportDataJson.commentInfo.comments, additionalComments);
        assertEquals (editReportDataJson.commentInfo.correctionReason, correctedReason);
        assertEquals (editReportDataJson.previousReportDate.toString (), todaysDateDash);
        Logging.testLog ("STEP 5 - The above JSON properties are listed with the values matching the tables below.");

        String updatedPatientName = "Updated Patient " + randomWords (1);
        editReportDataJson.patientInfo.name = updatedPatientName;
        order.patient.fullname = updatedPatientName;
        orderDetailTDetect.setReportDataJson (editReportDataJson.toString ());
        String correctedPreviewPdfUrl = orderDetailTDetect.getPreviewReportPdfUrl ();
        testLog ("Corrected Preview PDF File URL: " + correctedPreviewPdfUrl);
        String correctedPreviewPdfContent = getTextFromPDF (correctedPreviewPdfUrl, 1);
        validateReportContent (correctedPreviewPdfContent, order);
        assertTrue (correctedPreviewPdfContent.contains (reasonCorrectionStr));
        assertTrue (correctedPreviewPdfContent.contains (correctedReason));
        Logging.testLog ("STEP 6.1 - The report pdf Page 1 preview contains values for the following fields as listed above");

        correctedPreviewPdfContent = getTextFromPDF (correctedPreviewPdfUrl, 2);
        validateReportContent (correctedPreviewPdfContent, order);
        assertTrue (correctedPreviewPdfContent.contains (result));
        assertTrue (correctedPreviewPdfContent.contains (expTestResult));
        assertTrue (correctedPreviewPdfContent.contains (addCommentsStr));
        assertTrue (correctedPreviewPdfContent.contains (additionalComments));
        assertTrue (correctedPreviewPdfContent.contains (reviewText));
        assertTrue (correctedPreviewPdfContent.contains (approvedSignStr));
        assertTrue (correctedPreviewPdfContent.contains (approvedBy));
        Logging.testLog ("STEP 6.2 - The report pdf Page 2 preview contains additional values for the following fields as listed below");

        orderDetailTDetect.releaseReportWithSignatureRequired ();
        String correctedReleasePdfUrl = orderDetailTDetect.getReleasedReportPdfUrl ();
        testLog ("Corrected Release PDF File URL: " + correctedReleasePdfUrl);
        String correctedReleasePdfContent = getTextFromPDF (correctedReleasePdfUrl, 1);
        validateReportContent (correctedReleasePdfContent, order);
        assertTrue (correctedReleasePdfContent.contains (reasonCorrectionStr));
        assertTrue (correctedReleasePdfContent.contains (correctedReason));
        Logging.testLog ("STEP 7.1 - The report pdf Page 1 contains values for the following fields as listed below");

        correctedReleasePdfContent = getTextFromPDF (correctedPreviewPdfUrl, 2);
        validateReportContent (correctedReleasePdfContent, order);
        assertTrue (correctedReleasePdfContent.contains (result));
        assertTrue (correctedReleasePdfContent.contains (expTestResult));
        assertTrue (correctedReleasePdfContent.contains (addCommentsStr));
        assertTrue (correctedReleasePdfContent.contains (additionalComments));
        assertTrue (correctedReleasePdfContent.contains (reviewSignStr));
        assertTrue (correctedReleasePdfContent.contains (reviewedReleasedBy + " " + todaysDate));
        assertTrue (correctedReleasePdfContent.contains (approvedSignStr));
        assertTrue (correctedReleasePdfContent.contains (approvedBy + " " + todaysDate));
        Logging.testLog ("STEP 7.2 - The report pdf Page 2 contains additional values for the following fields as listed below");

        taskList.searchAndClickFirstTask ("Dx Corrected Report");
        assertTrue (task.taskFiles ().containsKey ("reportData.json"));
        reportDataJson = getReportDataJsonFile (task.taskFiles ().get ("reportData.json"));
        validateReportDataJson (reportDataJson, order, patientId, orderTestId);
        assertEquals (reportDataJson.commentInfo.comments, additionalComments);
        assertEquals (reportDataJson.commentInfo.correctionReason, correctedReason);
        assertEquals (reportDataJson.commentInfo.clinicalConsultantName, reviewedReleasedBy);
        assertTrue (reportDataJson.commentInfo.signedAt.toString ()
                                                       .startsWith (todaysDateDash));
        assertNotNull (reportDataJson.commentInfo.signatureImage);
        assertEquals (reportDataJson.previousReportDate.toString (), todaysDateDash);
        Logging.testLog ("STEP 8 - validate released reportData.json file");
    }

    /**
     * NOTE: SR-T3070
     */
    public void validateFailedTDetectReportData () {
        Assay assayTest = Assay.COVID19_DX_IVD;
        String orderNum = orderDetailTDetect.createTDetectOrder (TestHelper.physicianTRF (),
                                                                 TestHelper.newPatient (),
                                                                 new String[] { "C90.00" },
                                                                 DateUtils.getPastFutureDate (-1),
                                                                 assayTest,
                                                                 ChargeType.Client,
                                                                 TestHelper.getRandomAddress (),
                                                                 com.adaptivebiotech.test.utils.PageHelper.OrderStatus.Active,
                                                                 ContainerType.SlideBox5CS);
        Logging.testLog ("T-Detect Order created: " + orderNum);

        Order order = orderDetailTDetect.parseOrder (com.adaptivebiotech.test.utils.PageHelper.OrderStatus.Active);
        orderDetailTDetect.navigateToOrderStatusPage (order.id);
        orderStatus.isCorrectPage ();
        orderStatus.failWorkflow ("testing failure report");
        history.gotoOrderDebug (order.tests.get (0).sampleName);
        history.waitFor (StageName.DxReport, StageStatus.Awaiting, StageSubstatus.CLINICAL_QC);

        orderDetailTDetect.navigateToOrderDetailsPage (order.id);
        orderDetailTDetect.isCorrectPage ();
        orderDetailTDetect.clickReportTab (assayTest);

        orderDetailTDetect.setQCstatus (QC.Pass);
        orderDetailTDetect.releaseReport ();
        history.gotoOrderDebug (order.tests.get (0).sampleName);
        String reportDataJsonFileUrl = history.getFileUrl ("reportData.json");
        ReportRender reportDataJson = getReportDataJsonFile (reportDataJsonFileUrl);
        assertEquals (reportDataJson.isFailure, Boolean.valueOf (true));
        Logging.testLog ("STEP 11 - validate reportData.json displays isFailure as true");
    }

    private void validateReportDataJson (ReportRender reportDataJson, Order order,
                                         String patientId, String orderTestId) {
        assertEquals (reportDataJson.klass, "com.adaptive.clonoseqreport.dtos.ReportRenderDto");
        assertEquals (reportDataJson.version, new Integer (1));
        assertEquals (reportDataJson.patientInfo.id, patientId);
        assertEquals (reportDataJson.patientInfo.name, order.patient.fullname);
        assertEquals (reportDataJson.patientInfo.dob.toString (),
                      DateUtils.convertDateFormat (order.patient.dateOfBirth, "MM/dd/yyyy", "yyyy-MM-dd"));
        Logging.testLog ("Date stored with Dash: " + reportDataJson.patientInfo.dob);
        assertEquals (reportDataJson.patientInfo.mrn, order.patient.mrn);
        assertEquals (reportDataJson.patientInfo.gender, order.patient.gender);

        assertEquals (reportDataJson.patientInfo.reportSpecimenSource, order.specimenDto.sourceType);
        assertEquals (reportDataJson.patientInfo.reportSpecimenType, order.specimenDto.sampleType);
        assertEquals (reportDataJson.patientInfo.reportSpecimenCompartment, "Cellular");
        assertEquals (reportDataJson.patientInfo.reportSpecimenId, order.specimenDto.specimenNumber);
        assertEquals (reportDataJson.patientInfo.reportLocus, Locus.TCRB_v4b);
        assertEquals (reportDataJson.patientInfo.reportSpecimenCollectionDate.toString (),
                      DateUtils.convertDateFormat (order.specimenDto.collectionDate.toString (),
                                                   "MM/dd/yyyy",
                                                   "yyyy-MM-dd"));
        assertEquals (reportDataJson.patientInfo.reportSpecimenArrivalDate.toString (),
                      DateUtils.convertDateFormat (order.specimenDto.arrivalDate.split ("\\s+")[0],
                                                   "MM/dd/yyyy",
                                                   "yyyy-MM-dd"));
        assertTrue (orderTestId.endsWith (reportDataJson.patientInfo.reportSampleOrderTestId));
        assertEquals (reportDataJson.patientInfo.orderNumber, order.order_number);
        assertEquals (reportDataJson.patientInfo.institutionName, order.physician.accountName);
        assertEquals (reportDataJson.patientInfo.orderingPhysician, order.physician.providerFullName);
        assertTrue (order.icdcodes.size () == 2);
        assertTrue (reportDataJson.patientInfo.icd10Codes.contains (order.icdcodes.get (0).replaceAll ("\\s+", " ")));
        assertTrue (reportDataJson.patientInfo.icd10Codes.contains (order.icdcodes.get (1).replaceAll ("\\s+", " ")));
        assertEquals (reportDataJson.patientInfo.reportDate.toString (), todaysDateDash);

        assertEquals (reportDataJson.patientInfo.receptorFamily, "TCell");
        assertEquals (reportDataJson.patientInfo.isClonoSEQV1, false);
        assertEquals (reportDataJson.patientInfo.isIuo, false);
        assertEquals (reportDataJson.patientInfo.localeCode, Locale.US);
        assertNull (reportDataJson.patientInfo.patientId);
        assertNull (reportDataJson.patientInfo.reportNumber);
        assertEquals (reportDataJson.patientInfo.klass, "com.adaptive.clonoseqreport.dtos.PatientInfoDto");

        assertEquals (reportDataJson.dxResult.disease, "COVID19");
        assertEquals (reportDataJson.dxResult.dxStatus, DxStatus.POSITIVE);
        assertEquals (reportDataJson.dxResult.dxScore, 52.24133872081212);
        assertTrue (reportDataJson.dxResult.containerVersion.startsWith ("dx-classifiers/covid-19:"));
        assertEquals (reportDataJson.dxResult.classifierVersion, "v1.0");
        assertTrue (reportDataJson.dxResult.pipelineVersion.startsWith ("v3.1-"));
        assertTrue (reportDataJson.dxResult.configVersion.startsWith ("dx.covid19."));
        assertTrue (reportDataJson.dxResult.qcFlags.size () == 0);
        assertEquals (reportDataJson.dxResult.posteriorProbability, 1.0);
        assertEquals (reportDataJson.dxResult.countEnhancedSeq, 128);
        assertEquals (reportDataJson.dxResult.uniqueProductiveTemplates, 222554);

        assertEquals (reportDataJson.commentInfo.klass, "com.adaptive.clonoseqreport.dtos.ReportCommentDto");
        assertEquals (reportDataJson.commentInfo.version, 1);
        assertNull (reportDataJson.commentInfo.clinicalConsultantTitle);
        assertEquals (reportDataJson.commentInfo.labDirectorName,
                      approvedBy.replaceAll ("\\.", "").replace ("(", "").replace (")", ""));

        assertEquals (reportDataJson.isFailure, Boolean.valueOf (false));
    }

    private void validateReportContent (String fileContent, Order order) {
        assertTrue (fileContent.contains (String.join (" ",
                                                       "PATIENT NAME",
                                                       "DATE OF BIRTH",
                                                       "MEDICAL RECORD #",
                                                       "GENDER",
                                                       "REPORT DATE",
                                                       "ORDER #")));
        assertTrue (fileContent.toUpperCase ()
                               .contains (order.patient.fullname.toUpperCase ()));
        assertTrue (fileContent.contains (order.patient.dateOfBirth));
        assertTrue (fileContent.contains (order.patient.mrn));
        assertTrue (fileContent.contains (order.patient.gender));
        assertTrue (fileContent.contains (todaysDate + " " + order.order_number));

        assertTrue (fileContent.contains (String.join (" ",
                                                       "SPECIMEN TYPE / SPECIMEN SOURCE",
                                                       "COLLECTION DATE",
                                                       "DATE RECEIVED",
                                                       "SAMPLE ID")));
        Logging.info ("##" + String.join (" ",
                                          SpecimenType.Blood + " / " + SpecimenSource.Blood,
                                          order.specimenDto.collectionDate.toString (),
                                          order.specimenDto.arrivalDate.split ("\\s+")[0],
                                          order.specimenDto.specimenNumber));
        assertTrue (fileContent.contains (String.join (" ",
                                                       SpecimenType.Blood + " / " + SpecimenSource.Blood,
                                                       order.specimenDto.collectionDate.toString (),
                                                       order.specimenDto.arrivalDate.split ("\\s+")[0],
                                                       order.specimenDto.specimenNumber)));

        assertTrue (fileContent.contains ("ICD CODE(S)"));
        assertTrue (order.icdcodes.size () == 2);
        assertTrue (fileContent.contains (order.icdcodes.get (0).replaceAll ("\\s+", " ")));
        assertTrue (fileContent.contains (order.icdcodes.get (1).replaceAll ("\\s+", " ")));

        assertTrue (fileContent.contains ("ORDERING PHYSICIAN INSTITUTION"));
        assertTrue (fileContent.contains (order.physician.accountName));
        assertTrue (fileContent.contains (order.physician.providerFullName));

    }

    private String getTextFromPDF (String url, int pageNumber) {
        String pdfFileLocation = join ("/", downloadDir, UUID.randomUUID () + ".pdf");
        testLog ("PDF File Location: " + pdfFileLocation);

        // get file from URL and save it
        doCoraLogin ();
        headers.get ().add (new BasicHeader ("Connection", "keep-alive"));
        get (url, new File (pdfFileLocation));
        resetheaders ();

        // read PDF and extract text
        PdfReader reader = null;
        String fileContent = null;
        try {
            reader = new PdfReader (pdfFileLocation);
            fileContent = PdfTextExtractor.getTextFromPage (reader, pageNumber);
            testLog ("File Content: " + fileContent);
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
    private ReportRender getReportDataJsonFile (String fileUrl) {
        // get file using get request
        doCoraLogin ();
        ReportRender reportDataJson = null;
        try {
            testLog ("File URL: " + fileUrl);
            resetheaders ();
            headers.get ().add (username);
            String getResponse = get (fileUrl);
            testLog ("File URL Response: " + getResponse);
            reportDataJson = mapper.readValue (getResponse, ReportRender.class);
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
        testLog ("Json File Data " + reportDataJson);
        HttpClientHelper.resetheaders ();
        return reportDataJson;
    }

}
