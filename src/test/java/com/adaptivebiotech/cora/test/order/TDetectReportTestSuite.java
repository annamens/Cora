package com.adaptivebiotech.cora.test.order;

import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.TestHelper.mapper;
import static com.adaptivebiotech.test.utils.TestHelper.randomString;
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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Orders.Order;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.Billing;
import com.adaptivebiotech.cora.ui.order.OrderDetailTDetect;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.order.Specimen;
import com.adaptivebiotech.cora.ui.patient.PatientDetail;
import com.adaptivebiotech.cora.ui.shipment.Accession;
import com.adaptivebiotech.cora.ui.shipment.Shipment;
import com.adaptivebiotech.cora.ui.workflow.History;
import com.adaptivebiotech.cora.utils.DateUtils;
import com.adaptivebiotech.cora.utils.TestHelper;
import com.adaptivebiotech.picasso.dto.ReportRender;
import com.adaptivebiotech.pipeline.utils.TestHelper.DxStatus;
import com.adaptivebiotech.pipeline.utils.TestHelper.Locus;
import com.adaptivebiotech.test.utils.Logging;
import com.adaptivebiotech.test.utils.PageHelper.Assay;
import com.adaptivebiotech.test.utils.PageHelper.ChargeType;
import com.adaptivebiotech.test.utils.PageHelper.ContainerType;
import com.adaptivebiotech.test.utils.PageHelper.OrderStatus;
import com.adaptivebiotech.test.utils.PageHelper.QC;
import com.adaptivebiotech.test.utils.PageHelper.ShippingCondition;
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

    private OrderDetailTDetect orderDetailTDetect = new OrderDetailTDetect ();
    private Billing            billing            = new Billing ();
    private Specimen           Specimen           = new Specimen ();
    private Shipment           shipment           = new Shipment ();
    private Accession          accession          = new Accession ();
    private PatientDetail      patientDetail      = new PatientDetail ();
    private History            history            = new History ();

    private String             downloadDir;
    private final String       todaysDate         = DateUtils.getPastFutureDate (0,
                                                                                 DateTimeFormatter.ofPattern ("MM/dd/uuuu"),
                                                                                 DateUtils.pstZoneId);
    private final String       expTestResult      = "POSITIVE";
    private final String       reviewedReleasedBy = "svc_cora_test_phi_preprod, NBCDCH-PS";
    private final String       approvedBy         = "Stephanie Hallam, Ph.D., D(ABMGG)";

    @BeforeClass (alwaysRun = true)
    public void beforeClass () {
        doCoraLogin ();
    }

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod (Method test) {
        downloadDir = artifacts (this.getClass ().getName (), test.getName ());
    }

    /**
     * NOTE: SR-T3070
     */
    public void validateTDetectReportData () {
        new Login ().doLogin ();
        new OrdersList ().isCorrectPage ();
        Assay assayTest = Assay.COVID19_DX_IVD;

        // create T-Detect diagnostic order
        orderDetailTDetect.selectNewTDetectDiagnosticOrder ();
        orderDetailTDetect.isCorrectPage ();

        orderDetailTDetect.selectPhysician (TestHelper.physicianTRF ());
        Patient patient = TestHelper.newPatient ();
        patient.mrn = randomString (10);
        orderDetailTDetect.createNewPatient (patient);
        String icdCode1 = "C90.00", icdCode2 = "C91.00";
        orderDetailTDetect.enterPatientICD_Codes (icdCode1);
        orderDetailTDetect.enterPatientICD_Codes (icdCode2);
        orderDetailTDetect.clickSave ();

        String collectionDate = DateUtils.getPastFutureDate (-1);
        Specimen.enterCollectionDate (collectionDate);

        orderDetailTDetect.clickAssayTest (assayTest);
        billing.selectBilling (ChargeType.Client);
        billing.enterPatientAddress (TestHelper.address ());
        billing.clickSave ();

        String orderNum = orderDetailTDetect.getOrderNum ();
        Logging.info ("Order Number: " + orderNum);

        // add diagnostic shipment
        shipment.selectNewDiagnosticShipment ();
        shipment.isDiagnostic ();
        shipment.enterShippingCondition (ShippingCondition.Ambient);
        shipment.enterOrderNumber (orderNum);
        shipment.selectDiagnosticSpecimenContainerType (ContainerType.SlideBox5CS);
        shipment.clickAddSlide ();
        shipment.clickSave ();

        shipment.gotoAccession ();
        accession.isCorrectPage ();
        accession.clickIntakeComplete ();
        accession.labelingComplete ();
        accession.labelVerificationComplete ();
        accession.clickPass ();
        accession.gotoOrderDetail ();

        // activate order
        billing.isCorrectPage ();
        orderDetailTDetect.activateOrder ();
        String orderId = orderDetailTDetect.getOrderId ();
        orderDetailTDetect.navigateToOrderDetailsPage (orderId);
        orderDetailTDetect.isCorrectPage ();
        orderDetailTDetect.clickPatientCode (OrderStatus.Active);
        patientDetail.isCorrectPage ();
        String patientId = patientDetail.getPatientId ();
        orderDetailTDetect.navigateToTab (0);
        orderDetailTDetect.isCorrectPage ();

        Order order = orderDetailTDetect.parseOrder (OrderStatus.Active);

        String sampleName = order.tests.get (0).sampleName;
        history.gotoOrderDebug (order.tests.get (0).sampleName);
        String orderTestId = history.getOrderTestId ();

        history.setWorkflowProperty (WorkflowProperty.lastAcceptedTsvPath,
                                     "s3://pipeline-north-production-archive/200613_NB551725_0151_AHM7N7BGXF/v3.1/20200615_1438/packaged/rd.Human.TCRB-v4b.nextseq.156x12x0.vblocks.ultralight.rev1/HM7N7BGXF_0_Hospital12deOctubre-MartinezLopez_860011348.adap.txt.results.tsv.gz");
        history.setWorkflowProperty (WorkflowProperty.workspaceName,
                                     "Hospital12deOctubre-MartinezLopez");
        history.setWorkflowProperty (WorkflowProperty.sampleName, "860011348");

        history.forceStatusUpdate (StageName.DxAnalysis, StageStatus.Ready);

        history.waitFor (StageName.DxContamination, StageStatus.Stuck);
        history.forceStatusUpdate (StageName.DxContamination, StageStatus.Finished);

        history.waitFor (StageName.DxReport, StageStatus.Awaiting, StageSubstatus.CLINICAL_QC);

        history.isCorrectPage ();
        history.clickOrderTest ();
        // TODO remove below line
        // String sampleName = "122448-SN-10699";
        // String orderTestId =
        // "https://cora-test.dna.corp.adaptivebiotech.com/cora/order/status/5e3a4a74-5091-49a3-9fe0-769a84c08754?ordertestid=4e6ee727-ade0-4715-8d19-8b7d7a160517";
        // String patientId = "4b1fcf35-2205-4a6c-9cc2-70d5cf6e9d3e";
        // Patient patient = new Patient ();
        // patient.firstName = "seleniumcongue";
        // patient.middleName = "contentionestest";
        // patient.lastName = "f8b75a5a962f4ad9951376b27f42c2d3";
        // patient.fullname = String.join (" ",
        // patient.firstName,
        // patient.middleName,
        // patient.lastName);
        // patient.dateOfBirth = "01/01/1999";
        // patient.gender = "Male";
        // patient.mrn = "C^HzeFos]k";
        // patient.race = Race.ASKED;
        // patient.ethnicity = Ethnicity.ASKED;
        // tDetectDiagnostic.navigateTo
        // ("https://cora-test.dna.corp.adaptivebiotech.com/cora/order/details/5e3a4a74-5091-49a3-9fe0-769a84c08754");
        // tDetectDiagnostic.isCorrectPage ();
        // Order order = tDetectDiagnostic.parseOrder (OrderStatus.Active);
        // tDetectDiagnostic.clickReportTab (assayTest);
        // TODO remove above
        // navigate to order status page
        orderDetailTDetect.isOrderStatusPage ();
        orderDetailTDetect.clickReportTab (assayTest);

        orderDetailTDetect.setQCstatus (QC.Pass);
        String pdfUrl = orderDetailTDetect.getPreviewReportPdfUrl ();
        // String pdfUrl = tDetectDiagnostic.getReleasedReportPdfUrl ();
        testLog ("PDF File URL: " + pdfUrl);
        String fileContent = getTextFromPDF (pdfUrl, 1);
        validateReportContent (fileContent, order, patient);
        assertTrue (fileContent.contains ("REVIEWED AND RELEASED BY DATE & TIME"));
        assertTrue (fileContent.contains ("APPROVED BY SIGNATURE DATE"));
        assertTrue (fileContent.contains (approvedBy));
        Logging.testLog ("STEP 1 - validate preview report");

        history.gotoOrderDebug (sampleName);
        String reportDataJsonFileUrl = history.getFileUrl ("reportData.json");
        ReportRender reportDataJson = getReportDataJsonFile (reportDataJsonFileUrl);
        validateReportDataJson (reportDataJson, order, patientId, orderTestId);
        assertNull (reportDataJson.commentInfo.clinicalConsultantName);
        assertNull (reportDataJson.commentInfo.signatureImage);
        Logging.testLog ("STEP 2 - validate reportData.json file");

        // navigate to order status page
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
        validateReportContent (fileContent, order, patient);
        assertTrue (fileContent.contains ("REVIEWED AND RELEASED BY SIGNATURE DATE & TIME"));
        assertTrue (fileContent.contains (reviewedReleasedBy + " " + todaysDate));
        assertTrue (fileContent.contains ("APPROVED BY SIGNATURE DATE"));
        assertTrue (fileContent.contains (approvedBy + " " + todaysDate));
        Logging.testLog ("STEP 3 - validate released report");

        history.gotoOrderDebug (sampleName);
        reportDataJsonFileUrl = history.getFileUrl ("reportData.json");
        reportDataJson = getReportDataJsonFile (reportDataJsonFileUrl);
        validateReportDataJson (reportDataJson, order, patientId, orderTestId);
        assertEquals (reportDataJson.commentInfo.clinicalConsultantName, reviewedReleasedBy);
        // TODO
        Logging.testLog ("$$$$ SignedAt: " + reportDataJson.commentInfo.signedAt.toString ());
        assertNotNull (reportDataJson.commentInfo.signatureImage);
        Logging.testLog ("STEP 4 - validate released reportData.json file");

        history.waitFor (StageName.ReportDelivery, StageStatus.Finished);
        history.clickOrderTest ();
    }

    private void validateReportDataJson (ReportRender reportDataJson, Order order,
                                         String patientId, String orderTestId) {
        assertEquals (reportDataJson.klass, "com.adaptive.clonoseqreport.dtos.ReportRenderDto");
        assertEquals (reportDataJson.version, new Integer (1));
        assertEquals (reportDataJson.patientInfo.id, patientId);
        assertEquals (reportDataJson.patientInfo.name, order.patient.fullname);
        assertEquals (reportDataJson.patientInfo.dob.toString (),
                      DateUtils.convertDateFormat (order.patient.dateOfBirth, "MM/dd/yyyy", "yyyy-MM-dd"));
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
        assertEquals (reportDataJson.patientInfo.reportDate.toString (),
                      DateUtils.convertDateFormat (todaysDate, "MM/dd/yyyy", "yyyy-MM-dd"));

        assertEquals (reportDataJson.patientInfo.isCorrected, false);
        assertEquals (reportDataJson.patientInfo.receptorFamily, "TCell");
        assertEquals (reportDataJson.patientInfo.isClonoSEQV1, false);
        assertEquals (reportDataJson.patientInfo.isIuo, false);
        assertEquals (reportDataJson.patientInfo.localeCode, Locale.US);
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
        assertNull (reportDataJson.commentInfo.comments);
        assertNull (reportDataJson.commentInfo.correctionReason);
        assertNull (reportDataJson.commentInfo.clinicalConsultantTitle);
        assertEquals (reportDataJson.commentInfo.labDirectorName,
                      approvedBy.replaceAll ("\\.", "").replace ("(", "").replace (")", ""));

        assertEquals (reportDataJson.isFailure, Boolean.valueOf (false));
    }

    private void validateReportContent (String fileContent, Order order, Patient patient) {
        assertTrue (fileContent.contains (String.join (" ",
                                                       "PATIENT NAME",
                                                       "DATE OF BIRTH",
                                                       "MEDICAL RECORD #",
                                                       "GENDER",
                                                       "REPORT DATE",
                                                       "ORDER #")));
        assertTrue (fileContent.toUpperCase ()
                               .contains ( (patient.firstName + " " + patient.middleName).toUpperCase ()));
        assertTrue (fileContent.toUpperCase ()
                               .contains (patient.lastName.toUpperCase ()));
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

        assertTrue (fileContent.contains ("RESULT"));
        assertTrue (fileContent.contains (expTestResult));
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
