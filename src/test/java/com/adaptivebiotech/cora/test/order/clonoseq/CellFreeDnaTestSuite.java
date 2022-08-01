/**
* Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
*/
package com.adaptivebiotech.cora.test.order.clonoseq;

import static com.adaptivebiotech.cora.dto.Containers.ContainerType.Tube;
import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Orders.Assay.MRD_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Orders.OrderStatus.Active;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.clonoSEQ_trial;
import static com.adaptivebiotech.cora.dto.Specimen.Anticoagulant.Streck;
import static com.adaptivebiotech.cora.utils.TestHelper.bloodSpecimen;
import static com.adaptivebiotech.cora.utils.TestHelper.newTrialProtocolPatient;
import static com.adaptivebiotech.test.utils.DateHelper.formatDt7;
import static com.adaptivebiotech.test.utils.DateHelper.genLocalDate;
import static com.adaptivebiotech.test.utils.DateHelper.pstZoneId;
import static com.adaptivebiotech.test.utils.DateHelper.utcZoneId;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.PageHelper.Compartment.CellFree;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.Plasma;
import static com.seleniumfy.test.utils.Logging.info;
import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ofPattern;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import java.lang.reflect.Method;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Orders.Assay;
import com.adaptivebiotech.cora.dto.Orders.Order;
import com.adaptivebiotech.cora.dto.Specimen;
import com.adaptivebiotech.cora.test.order.NewOrderTestBase;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.debug.OrcaHistory;
import com.adaptivebiotech.cora.ui.order.NewOrderClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrderDetailClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.order.ReportClonoSeq;
import com.adaptivebiotech.cora.ui.shipment.Accession;
import com.adaptivebiotech.cora.ui.shipment.NewShipment;
import com.adaptivebiotech.cora.utils.PageHelper.QC;
import com.adaptivebiotech.test.utils.PageHelper.StageName;
import com.adaptivebiotech.test.utils.PageHelper.StageStatus;
import com.adaptivebiotech.test.utils.PageHelper.StageSubstatus;

/**
 * @author jpatel
 *
 */
@Test (groups = { "clonoSeq", "regression", "golden-retriever" })
public class CellFreeDnaTestSuite extends NewOrderTestBase {

    private Login                   login                = new Login ();
    private OrdersList              ordersList           = new OrdersList ();
    private NewOrderClonoSeq        newOrderClonoSeq     = new NewOrderClonoSeq ();
    private NewShipment             shipment             = new NewShipment ();
    private Accession               accession            = new Accession ();
    private OrderDetailClonoSeq     orderDetailClonoSeq  = new OrderDetailClonoSeq ();
    private ReportClonoSeq          reportClonoSeq       = new ReportClonoSeq ();
    private OrcaHistory             orcaHistory          = new OrcaHistory ();

    private final String            noResultsAvailable   = "No result available";
    private final String            mrdResultDescription = "This sample failed the quality control criteria despite multiple sequencing attempts, or there was a problem processing the test. Please contact Adaptive Biotechnologies for more information, to provide sample disposition instructions, and/or to discuss whether sending a new sample (if one is available) should be considered.";
    private final String            updateQuery          = "UPDATE cora.specimens SET properties = jsonb_set(properties, '{ActivationDate}', '\"%s\"', true) WHERE id = (SELECT specimen_id FROM cora.specimen_order_xref WHERE order_id = '%s')";

    private final DateTimeFormatter formatDt8            = ofPattern ("uuuu-MM-dd'T'HH:mm:ss.SSS'Z'");

    private final String[]          icdCodes             = { "C90.00" };

    private String                  downloadDir;

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod (Method test) {
        downloadDir = artifacts (this.getClass ().getName (), test.getName ());
        login.doLogin ();
        ordersList.isCorrectPage ();
    }

    /**
     * NOTE: SR-T4204
     * 
     * @sdlc.requirements SR-10414:R1
     */
    public void cfDnaPlasmaNoResultAvailable () {
        Specimen specimenDto = new Specimen ();
        specimenDto.sampleType = Plasma;
        specimenDto.collectionDate = genLocalDate (-3);
        Assay assayTest = MRD_BCell2_CLIA;

        createOrderAndValidateFailReport (specimenDto, assayTest);

    }

    /**
     * NOTE: SR-T4204
     * 
     * @sdlc.requirements SR-10414:R1
     */
    public void cfDnaBloodNoResultAvailable () {
        Specimen specimenDto = bloodSpecimen ();
        specimenDto.compartment = CellFree;
        Assay assayTest = MRD_BCell2_CLIA;

        createOrderAndValidateFailReport (specimenDto, assayTest);

    }

    /**
     * NOTE: SR-T4235
     * 
     * @sdlc.requirements SR-11228:R3
     */
    @Test (groups = "havanese")
    public void validateSpecimenActivationPresent () {
        Specimen specimenDto = bloodSpecimen ();
        specimenDto.compartment = CellFree;
        specimenDto.anticoagulant = Streck;
        Assay assayTest = ID_BCell2_CLIA;

        Order order = newOrderClonoSeq.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_trial),
                                                            newTrialProtocolPatient (),
                                                            icdCodes,
                                                            assayTest,
                                                            specimenDto);
        testLog ("Order No: " + order.orderNumber);

        shipment.createShipment (order.orderNumber, Tube);
        UUID shipmentId = accession.getShipmentId ();
        accession.clickIntakeComplete ();
        accession.clickLabelingComplete ();

        newOrderClonoSeq.gotoOrderEntry (order.id);
        validateSpecimenSectionFields (true, false);
        assertNull (newOrderClonoSeq.getSpecimenActivationDate ());
        testLog ("Validate Specimen fields are enabled before Specimen activation");

        accession.gotoAccession (shipmentId);
        accession.clickLabelVerificationComplete ();
        testLog ("Label verification Complete");

        Instant instant = Instant.ofEpochMilli (System.currentTimeMillis ());
        String utcDateTime = LocalDateTime.ofInstant (instant, utcZoneId).format (formatDt8);
        String pstDateTime = LocalDateTime.ofInstant (instant, pstZoneId).format (formatDt7);
        int updateCount = coraDb.executeUpdate (format (updateQuery, utcDateTime, order.id));
        assertEquals (updateCount, 1);
        newOrderClonoSeq.gotoOrderEntry (order.id);
        validateSpecimenSectionFields (false, false);
        assertEquals (newOrderClonoSeq.getSpecimenActivationDate ().format (formatDt7), pstDateTime);
        testLog ("Specimen Activation Date is present and specimen fields are disabled");

        accession.gotoAccession (shipmentId);
        accession.clickPass ();
        testLog ("Specimen Pass");

        newOrderClonoSeq.gotoOrderEntry (order.id);
        validateSpecimenSectionFields (false, false);
        assertEquals (newOrderClonoSeq.getSpecimenActivationDate ().format (formatDt7), pstDateTime);
        testLog ("Specimen Activation Date is present and specimen fields are disabled");

        newOrderClonoSeq.activateOrder ();
        testLog ("Activate Order");

        orderDetailClonoSeq.gotoOrderDetailsPage (order.id);
        assertEquals (orderDetailClonoSeq.getSpecimenActivationDate ().format (formatDt7), pstDateTime);
        testLog ("Specimen Activation Date is present");
    }

    /**
     * NOTE: SR-T4235
     * 
     * @sdlc.requirements SR-11228:R3
     */
    @Test (groups = "havanese")
    public void validateSpecimenActivationNotPresent () {
        Specimen specimenDto = bloodSpecimen ();
        Assay assayTest = ID_BCell2_CLIA;

        Order order = newOrderClonoSeq.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_trial),
                                                            newTrialProtocolPatient (),
                                                            icdCodes,
                                                            assayTest,
                                                            specimenDto);
        testLog ("Order No: " + order.orderNumber);

        shipment.createShipment (order.orderNumber, Tube);
        UUID shipmentId = accession.getShipmentId ();
        accession.clickIntakeComplete ();
        accession.clickLabelingComplete ();

        newOrderClonoSeq.gotoOrderEntry (order.id);
        validateSpecimenSectionFields (true, false);
        assertNull (newOrderClonoSeq.getSpecimenActivationDate ());
        testLog ("Validate Specimen fields are enabled before Specimen activation");

        accession.gotoAccession (shipmentId);
        accession.clickLabelVerificationComplete ();
        testLog ("Label verification Complete");

        newOrderClonoSeq.gotoOrderEntry (order.id);
        validateSpecimenSectionFields (true, false);
        assertNull (newOrderClonoSeq.getSpecimenActivationDate ());
        testLog ("Specimen Activation Date is not present and specimen fields are enabled");

        accession.gotoAccession (shipmentId);
        accession.clickPass ();
        testLog ("Specimen Pass");

        newOrderClonoSeq.gotoOrderEntry (order.id);
        validateSpecimenSectionFields (true, false);
        assertNull (newOrderClonoSeq.getSpecimenActivationDate ());
        testLog ("Specimen Activation Date is not present and specimen fields are enabled");

        newOrderClonoSeq.activateOrder ();
        testLog ("Activate Order");

        orderDetailClonoSeq.gotoOrderDetailsPage (order.id);
        assertNull (orderDetailClonoSeq.getSpecimenActivationDate ());
        testLog ("Specimen Activation Date is not present");
    }

    private void createOrderAndValidateFailReport (Specimen specimenDto, Assay assayTest) {
        Order order = newOrderClonoSeq.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_trial),
                                                            newTrialProtocolPatient (),
                                                            icdCodes,
                                                            assayTest,
                                                            specimenDto,
                                                            Active,
                                                            Tube);
        String sampleName = orderDetailClonoSeq.getSampleName (assayTest);

        generateFailureReport (order.orderNumber, sampleName, assayTest);

        validateReportDescription (assayTest);
    }

    private void generateFailureReport (String orderNumber, String sampleName, Assay assayTest) {
        orcaHistory.gotoOrderDebug (sampleName);
        orcaHistory.forceStatusUpdate (StageName.Clarity, StageStatus.Failed);
        testLog ("Order No: " + orderNumber + ", forced status updated to Clarity -> Failed");
        orcaHistory.waitFor (StageName.ClonoSEQReport, StageStatus.Awaiting, StageSubstatus.CLINICAL_QC);
        orcaHistory.clickOrderTest ();
        orderDetailClonoSeq.clickReportTab (assayTest);
        reportClonoSeq.releaseReport (assayTest, QC.Pass);
        testLog ("Order Number: " + orderNumber + ", Released Report, Failure Report Generated");

        orcaHistory.gotoOrderDebug (sampleName);
        orcaHistory.waitFor (StageName.ReportDelivery, StageStatus.Finished, StageSubstatus.ALL_SUCCEEDED);
        testLog ("New Clonality (ID) order needed alert should be triggered");
        orcaHistory.clickOrderTest ();

    }

    private void validateReportDescription (Assay assayTest) {
        orderDetailClonoSeq.clickReportTab (assayTest);
        String pdfUrl = reportClonoSeq.getReleasedReportPdfUrl ();
        info ("PDF File URL: " + pdfUrl);
        String extractedText = getTextFromPDF (downloadDir, pdfUrl, 1);
        System.out.println ("Extracted Text:\n" + extractedText);
        assertTrue (extractedText.contains (noResultsAvailable));
        assertTrue (extractedText.contains (mrdResultDescription));
    }

    private void validateSpecimenSectionFields (boolean allFields, boolean specimenSource) {
        assertEquals (newOrderClonoSeq.isSpecimenDeliveryEnabled (), allFields);
        assertEquals (newOrderClonoSeq.isSpecimenTypeEnabled (), allFields);
        assertEquals (newOrderClonoSeq.isCompartmentEnabled (), allFields);
        assertEquals (newOrderClonoSeq.isAnticoagulantEnabled (), allFields);
        assertEquals (newOrderClonoSeq.isCollectionDateEnabled (), allFields);
        assertEquals (newOrderClonoSeq.isUniqueSpecimenIdEnabled (), allFields);
        assertEquals (newOrderClonoSeq.isRetrievalDateEnabled (), allFields);
        assertEquals (newOrderClonoSeq.isSpecimenSourceEnabled (), specimenSource);
    }

}
