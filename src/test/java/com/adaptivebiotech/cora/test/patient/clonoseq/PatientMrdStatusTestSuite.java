package com.adaptivebiotech.cora.test.patient.clonoseq;

import static com.adaptivebiotech.cora.dto.Containers.ContainerType.Vacutainer;
import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Orders.OrderStatus.Active;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.clonoSEQ_all_payments;
import static com.adaptivebiotech.cora.utils.TestHelper.bloodSpecimen;
import static com.adaptivebiotech.cora.utils.TestHelper.newSelfPayPatient;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Orders.Assay;
import com.adaptivebiotech.cora.dto.Orders.Order;
import com.adaptivebiotech.cora.dto.Patient.PatientTestStatus;
import com.adaptivebiotech.cora.dto.Specimen;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.debug.OrcaHistory;
import com.adaptivebiotech.cora.ui.order.NewOrderClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrderDetailClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.order.ReportClonoSeq;
import com.adaptivebiotech.cora.ui.patient.EditPatientDemographicsModule;
import com.adaptivebiotech.cora.ui.patient.PatientsList;
import com.adaptivebiotech.cora.utils.PageHelper.QC;
import com.adaptivebiotech.test.utils.PageHelper.StageName;
import com.adaptivebiotech.test.utils.PageHelper.StageStatus;
import com.adaptivebiotech.test.utils.PageHelper.StageSubstatus;
import com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty;

/**
 * @author cbragg
 *         <a href="mailto:<cbragg@adaptivebiotech.com">cbragg@adaptivebiotech.com</a>
 */
@Test (groups = { "clonoSeq", "regression", "havanese" })
public class PatientMrdStatusTestSuite extends CoraBaseBrowser {

    private Login                         login                  = new Login ();
    private OrdersList                    ordersList             = new OrdersList ();
    private NewOrderClonoSeq              newOrderClonoSeq       = new NewOrderClonoSeq ();
    private PatientsList                  patientsList           = new PatientsList ();
    private EditPatientDemographicsModule editPatientModal       = new EditPatientDemographicsModule ();
    private OrderDetailClonoSeq           orderDetailClonoSeq    = new OrderDetailClonoSeq ();
    private OrcaHistory                   orcaHistory            = new OrcaHistory ();
    private ReportClonoSeq                reportClonoSeq         = new ReportClonoSeq ();
    private String[]                      icdCode                = new String[] { "C90.00" };
    private Assay                         orderTest              = ID_BCell2_CLIA;
    private Specimen                      specimen               = bloodSpecimen ();
    private final String                  tsvPathBCellNoClones   = azTsvPath + "/not-detected.mrd.tsv.gz";
    private String                        tsvPathBCellWithClones = azE2EPath + "/HF23MBGX2_0_Janssen-Chiu-HOLDING_650009502008.adap.txt.results.tsv.gz";

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod () {
        login.doLogin ();
        ordersList.isCorrectPage ();
    }

    /**
     * 
     * @sdlc.requirements SR-2959,SR-12197
     */
    public void verifyPendingAndDeceased () {
        Order order = newOrderClonoSeq.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_all_payments),
                                                            newSelfPayPatient (),
                                                            icdCode,
                                                            orderTest,
                                                            specimen);

        // Verify patient MRD Status is Pending on Order Details
        assertEquals (newOrderClonoSeq.getPatientMRDStatus (), PatientTestStatus.Pending.label);
        testLog (order.orderNumber + " patient MRD status on Order Details = " + PatientTestStatus.Pending.label);

        // Verify patient MRD Status is Pending on Patient List
        verifyPatientMrdStatusOnPatientList (newOrderClonoSeq.getPatientCode ().toString (), PatientTestStatus.Pending);
        newOrderClonoSeq.gotoOrderEntry (order.id);

        // Verify patient MRD Status is Deceased on Order Details
        newOrderClonoSeq.clickEditPatient ();
        editPatientModal.clickPatientDeceased ();
        editPatientModal.clickSave ();
        newOrderClonoSeq.refresh ();
        assertEquals (newOrderClonoSeq.getPatientMRDStatus (), PatientTestStatus.Deceased.label);
        testLog (order.orderNumber + " patient MRD status on Order Details = " + PatientTestStatus.Deceased.label);

        // Verify patient MRD Status is Deceased on Patient List
        verifyPatientMrdStatusOnPatientList (newOrderClonoSeq.getPatientCode ().toString (),
                                             PatientTestStatus.Deceased);
    }

    /**
     * @sdlc.requirements SR-2959,SR-12197
     */
    public void verifyClonalityIdProcessingNoCalibratedClonesFound () {
        Order order = newOrderClonoSeq.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_all_payments),
                                                            newSelfPayPatient (),
                                                            icdCode,
                                                            orderTest,
                                                            specimen,
                                                            Active,
                                                            Vacutainer);

        // Verify patient MRD Status is Clonality ID Processing on Order Details
        String sampleName = orderDetailClonoSeq.getSampleName (orderTest);
        assertEquals (orderDetailClonoSeq.getPatientMRDStatus (), PatientTestStatus.ClonalityProcessing.label);
        testLog (order.orderNumber + " patient MRD status on Order Details = " + PatientTestStatus.ClonalityProcessing.label);

        // Verify patient MRD Status is Clonality ID Processing on Patient List
        verifyPatientMrdStatusOnPatientList (orderDetailClonoSeq.getPatientCode (),
                                             PatientTestStatus.ClonalityProcessing);

        // Verify patient MRD Status is No Clones Found on Order Details
        pushOrderToReportReleased (sampleName, order.orderNumber, tsvPathBCellNoClones, orderTest);
        assertEquals (orderDetailClonoSeq.getPatientMRDStatus (), PatientTestStatus.NoClonesFound.label);

        // Verify patient MRD Status is No Clones Found on Patient List
        testLog (order.orderNumber + " patient MRD status on Order Details = " + PatientTestStatus.NoClonesFound.label);
        verifyPatientMrdStatusOnPatientList (orderDetailClonoSeq.getPatientCode (), PatientTestStatus.NoClonesFound);
    }

    /**
     * @sdlc.requirements SR-2959,SR-12197
     */
    public void verifyTrackingMrdEnabled () {
        Order order = newOrderClonoSeq.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_all_payments),
                                                            newSelfPayPatient (),
                                                            icdCode,
                                                            orderTest,
                                                            specimen,
                                                            Active,
                                                            Vacutainer);

        // Verify patient MRD Status is Tracking MRD Enabled on Order Details
        String sampleName = orderDetailClonoSeq.getSampleName (orderTest);
        pushOrderToReportReleased (sampleName, order.orderNumber, tsvPathBCellWithClones, orderTest);
        assertEquals (orderDetailClonoSeq.getPatientMRDStatus (), PatientTestStatus.TrackingEnabled.label);
        testLog (order.orderNumber + " patient MRD status on Order Details = " + PatientTestStatus.TrackingEnabled.label);

        // Verify patient MRD Status is Tracking MRD Enabled on Patient List
        verifyPatientMrdStatusOnPatientList (orderDetailClonoSeq.getPatientCode (), PatientTestStatus.TrackingEnabled);
    }

    private void verifyPatientMrdStatusOnPatientList (String searchTerm, PatientTestStatus expectedPatientStatus) {
        newOrderClonoSeq.clickPatients ();
        patientsList.searchPatient (searchTerm);
        patientsList.waitForNewPatientToPopulate ();
        patientsList.clickPatientDetails (1);
        assertEquals (patientsList.getPatientMRDStatus (), expectedPatientStatus.label);
        testLog (searchTerm + " patient MRD status on Patient List = " + expectedPatientStatus.label);
    }

    private void pushOrderToReportReleased (String sampleName, String orderNo, String tsvPath, Assay orderTest) {
        orcaHistory.gotoOrderDebug (sampleName);
        orcaHistory.setWorkflowProperty (WorkflowProperty.lastAcceptedTsvPath, tsvPath);
        orcaHistory.forceStatusUpdate (StageName.SecondaryAnalysis, StageStatus.Ready);
        testLog ("Order No: " + orderNo + ", forced status updated to SecondaryAnalysis -> Ready");
        orcaHistory.waitFor (StageName.ClonoSEQReport, StageStatus.Awaiting, StageSubstatus.CLINICAL_QC);
        orcaHistory.clickOrderTest ();
        orderDetailClonoSeq.clickReportTab (orderTest);
        reportClonoSeq.releaseReport (orderTest, QC.Pass);
        testLog ("Order No: " + orderNo + ", Released Report, waiting for delivery finished");
        orcaHistory.gotoOrderDebug (sampleName);
        orcaHistory.waitFor (StageName.ReportDelivery, StageStatus.Finished, StageSubstatus.ALL_SUCCEEDED);
        testLog ("Order No: " + orderNo + ", Report Delivery Finished");
        orcaHistory.clickOrderTest ();
        orderDetailClonoSeq.clickOrderDetailsTab ();
    }

}
