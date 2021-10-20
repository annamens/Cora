package com.adaptivebiotech.cora.test.order;

import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Finished;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Ready;
import static com.seleniumfy.test.utils.HttpClientHelper.get;
import static com.seleniumfy.test.utils.HttpClientHelper.headers;
import static com.seleniumfy.test.utils.HttpClientHelper.resetheaders;
import static java.lang.String.join;
import static org.testng.Assert.assertTrue;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.UUID;
import org.apache.http.message.BasicHeader;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Orders.Order;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.Billing;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.order.Specimen;
import com.adaptivebiotech.cora.ui.order.TDetectDiagnostic;
import com.adaptivebiotech.cora.ui.patient.PatientDetail;
import com.adaptivebiotech.cora.ui.shipment.Accession;
import com.adaptivebiotech.cora.ui.shipment.Shipment;
import com.adaptivebiotech.cora.ui.workflow.History;
import com.adaptivebiotech.cora.utils.DateUtils;
import com.adaptivebiotech.cora.utils.TestHelper;
import com.adaptivebiotech.test.utils.Logging;
import com.adaptivebiotech.test.utils.PageHelper.Assay;
import com.adaptivebiotech.test.utils.PageHelper.ChargeType;
import com.adaptivebiotech.test.utils.PageHelper.ContainerType;
import com.adaptivebiotech.test.utils.PageHelper.OrderStatus;
import com.adaptivebiotech.test.utils.PageHelper.QC;
import com.adaptivebiotech.test.utils.PageHelper.ShippingCondition;
import com.adaptivebiotech.test.utils.PageHelper.StageName;
import com.adaptivebiotech.test.utils.PageHelper.StageStatus;
import com.adaptivebiotech.test.utils.PageHelper.StageSubstatus;
import com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

/**
 * @author jpatel
 *         <a href="mailto:jpatel@adaptivebiotech.com">jpatel@adaptivebiotech.com</a>
 */
@Test (groups = { "regression", "tDetectOrder" })
public class TDetectReportTestSuite extends CoraBaseBrowser {

    private TDetectDiagnostic tDetectDiagnostic = new TDetectDiagnostic ();
    private Billing           billing           = new Billing ();
    private Specimen          Specimen          = new Specimen ();
    private Shipment          shipment          = new Shipment ();
    private Accession         accession         = new Accession ();
    private PatientDetail     patientDetail     = new PatientDetail ();
    private History           history           = new History ();

    private String            downloadDir;

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod (Method test) {
        downloadDir = artifacts (this.getClass ().getName (), test.getName ());
    }

    /**
     * NOTE: SR-T3070
     */
    public void validateTDetectOrderActivation () {
        new Login ().doLogin ();
        new OrdersList ().isCorrectPage ();
        Assay assayTest = Assay.COVID19_DX_IVD;

        // create T-Detect diagnostic order
        tDetectDiagnostic.selectNewTDetectDiagnosticOrder ();
        tDetectDiagnostic.isCorrectPage ();

        tDetectDiagnostic.selectPhysician (TestHelper.physicianTRF ());
        Patient patient = TestHelper.newPatient ();
        tDetectDiagnostic.createNewPatient (patient);
        String icdCode1 = "C90.00", icdCode2 = "C91.00";
        tDetectDiagnostic.enterPatientICD_Codes (icdCode1);
        tDetectDiagnostic.enterPatientICD_Codes (icdCode2);
        tDetectDiagnostic.clickSave ();

        Specimen.enterCollectionDate (DateUtils.getPastFutureDate (-1));

        tDetectDiagnostic.clickAssayTest (assayTest);
        billing.selectBilling (ChargeType.Client);
        billing.enterPatientAddress (TestHelper.address ());
        billing.clickSave ();

        String orderNum = tDetectDiagnostic.getOrderNum ();
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
        tDetectDiagnostic.activateOrder ();
        String orderId = tDetectDiagnostic.getOrderId ();
        tDetectDiagnostic.navigateToOrderDetailsPage (orderId);
        tDetectDiagnostic.isCorrectPage ();
        tDetectDiagnostic.clickPatientCode (OrderStatus.Active);
        patientDetail.isCorrectPage ();
        String patientId = patientDetail.getPatientId ();
        tDetectDiagnostic.navigateToTab (0);
        tDetectDiagnostic.isCorrectPage ();

        // $$$$$$$$$$$$$$$
        // tDetectDiagnostic.navigateTo
        // ("https://cora-test.dna.corp.adaptivebiotech.com/cora/order/details/1e879635-3b0f-4cd2-9692-1c3990272013");
        // tDetectDiagnostic.isCorrectPage ();
        // $$$$$$$$$$$$$$$
        Order order = tDetectDiagnostic.parseOrder (OrderStatus.Active);

        history.gotoOrderDebug (order.tests.get (0).sampleName);

        history.setWorkflowProperty (WorkflowProperty.lastAcceptedTsvPath,
                                     "s3://pipeline-north-production-archive/200613_NB551725_0151_AHM7N7BGXF/v3.1/20200615_1438/packaged/rd.Human.TCRB-v4b.nextseq.156x12x0.vblocks.ultralight.rev1/HM7N7BGXF_0_Hospital12deOctubre-MartinezLopez_860011348.adap.txt.results.tsv.gz");
        history.setWorkflowProperty (WorkflowProperty.workspaceName, "Hospital12deOctubre-MartinezLopez");
        history.setWorkflowProperty (WorkflowProperty.sampleName, "860011348");

        history.forceStatusUpdate (StageName.DxContamination, Ready);

        history.waitFor (StageName.DxContamination, StageStatus.Stuck);
        history.forceStatusUpdate (StageName.DxContamination, Finished);

        history.waitFor (StageName.DxReport, StageStatus.Awaiting, StageSubstatus.CLINICAL_QC);
        assertTrue (history.isStagePresent (StageName.DxReport, StageStatus.Awaiting, StageSubstatus.CLINICAL_QC));

        history.isCorrectPage ();
        history.clickOrderTest ();

        // navigate to order status page
        tDetectDiagnostic.isOrderStatusPage ();
        tDetectDiagnostic.clickReportTab (assayTest);

        tDetectDiagnostic.setQCstatus (QC.Pass);
        tDetectDiagnostic.releaseReport ();

        String pdfUrl = tDetectDiagnostic.getPreviewReportPdfUrl ();
        testLog ("PDF File URL: " + pdfUrl);
        String fileContent = getTextFromPDF (pdfUrl, 1);

        assertTrue (fileContent.contains (String.join (" ",
                                                       "PATIENT NAME",
                                                       "DATE OF BIRTH",
                                                       "MEDICAL RECORD #",
                                                       "GENDER",
                                                       "REPORT DATE",
                                                       "ORDER #")));
        assertTrue (fileContent.contains (String.join (" ",
                                                       "PATIENT NAME",
                                                       "DATE OF BIRTH",
                                                       "MEDICAL RECORD #",
                                                       "GENDER",
                                                       "REPORT DATE",
                                                       "ORDER #")));
    }

    private String getTextFromPDF (String url, int pageNumber) {
        String pdfFileLocation = join ("/", downloadDir, UUID.randomUUID () + ".pdf");
        testLog ("PDF File Location: " + pdfFileLocation);

        // get file from URL and save it
        doCoraLogin ();
        headers.set (new ArrayList <> ());
        headers.get ().add (new BasicHeader ("Connection", "keep-alive"));
        get (url, new File (pdfFileLocation));
        resetheaders ();

        // read PDF and extract text
        PdfReader reader = null;
        String extractedText = null;
        try {
            reader = new PdfReader (pdfFileLocation);
            String fileContent = PdfTextExtractor.getTextFromPage (reader, pageNumber);
            testLog ("File Content: " + fileContent);
        } catch (IOException e) {
            throw new RuntimeException (e);
        } finally {
            reader.close ();
        }
        return extractedText;
    }

}
