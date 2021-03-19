package com.adaptivebiotech.cora.test.mira;

import static com.adaptivebiotech.test.utils.Logging.testLog;
import static org.testng.Assert.assertTrue;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestUrl;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.test.CoraEnvironment;
import com.adaptivebiotech.cora.ui.CoraPage;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.mira.Mira;
import com.adaptivebiotech.cora.ui.mira.MirasList;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.shipment.Accession;
import com.adaptivebiotech.cora.ui.shipment.Shipment;
import com.adaptivebiotech.cora.ui.workflow.History;
import com.adaptivebiotech.cora.utils.PageHelper.MiraExpansionMethod;
import com.adaptivebiotech.cora.utils.PageHelper.MiraLab;
import com.adaptivebiotech.cora.utils.PageHelper.MiraPanel;
import com.adaptivebiotech.cora.utils.PageHelper.MiraQCStatus;
import com.adaptivebiotech.cora.utils.PageHelper.MiraStage;
import com.adaptivebiotech.cora.utils.PageHelper.MiraStatus;
import com.adaptivebiotech.cora.utils.PageHelper.MiraType;
import com.adaptivebiotech.cora.utils.mira.MiraHttpClient;
import com.adaptivebiotech.cora.utils.mira.MiraTestFormInfo;
import com.adaptivebiotech.cora.utils.mira.MiraTestInfoProvider;
import com.adaptivebiotech.cora.utils.mira.MiraTestScenarioBuilder;
import com.adaptivebiotech.cora.utils.mira.MiraTsvCopier;
import com.adaptivebiotech.test.utils.PageHelper.ShippingCondition;
import com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty;

/*
 TODO
 
 get projectId and accountId from cora DB
 run sql against coraDB after job has processed... but this will time out? it takes hours
 maybe best to just output the sql that should be run after the job has processed?
 use azure tsv files
 see if there is a simpler way to setup the tech transfer
 
*/
public class MiraTestSuite extends CoraBaseBrowser {
    
    private final String projectId = "a9d36064-de2a-49c3-b6af-3b3a46ee0c22";
    private final String accountId = "9536e8eb-eff0-4e37-ba54-26caa2592be2";
    private final String prodTestInfoPath = "MIRA/prod_test_info_azure_slim.json";
    private final String sourceMiraNumber = "M-1345";
    private final String sourceSpecimenNumber = "SP-914830";
    
    @Test
    public void testCreateAndActivateAMPL () {

        MiraLab miraLab = MiraLab.AntigenMapProduction;
        MiraType miraType = MiraType.MIRA;
        MiraPanel miraPanel = MiraPanel.Minor;
        MiraExpansionMethod miraExpansionMethod = MiraExpansionMethod.AntiCD3;

        loginToCora ();

        List <String> specimenIds = createGeneralShipmentFromIntakeManifest ("MIRA/cora-intakemanifest_28JUL2020.xlsx");

        String miraId = createNewMira (miraLab,
                                       miraType,
                                       miraPanel,
                                       miraExpansionMethod,
                                       specimenIds.get (0));

        gotoMiraByLabAndId (miraId, miraLab);
        
        Mira mira = new Mira();
        String specimenId = specimenIds.get (0);
        String expansionId = mira.getExpansionId ();

        waitForStageAndStatus (MiraStage.PoolExtraction, MiraStatus.Ready);

        createSampleManifest (miraId, miraLab);
                
        String specimenCollectionDate = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format (LocalDateTime.now ()); // YYYY-MM-DDThh:mm:ss
        
        MiraTestFormInfo miraTestFormInfo = new MiraTestFormInfo ();
        miraTestFormInfo.sourceMiraNumber = sourceMiraNumber;
        miraTestFormInfo.sourceSpecimenNumber = sourceSpecimenNumber;
        miraTestFormInfo.targetEnvironmentType = "test";
        miraTestFormInfo.targetDataPath = "not used";
        miraTestFormInfo.targetHost = coraTestUrl;
        miraTestFormInfo.targetMiraNumber = miraId;
        miraTestFormInfo.targetExpansionNumber = expansionId;
        miraTestFormInfo.targetWorkspace = "Adaptive-Testing";
        miraTestFormInfo.targetProjectId = UUID.fromString (projectId);
        miraTestFormInfo.targetAccountId = UUID.fromString (accountId);
        miraTestFormInfo.targetFlowcellId = "XMIRASCENARIO";
        miraTestFormInfo.targetSpecimenNumber = specimenId;
        miraTestFormInfo.targetSpecimenType = "Blood";
        miraTestFormInfo.targetSpecimenSource = "Blood";
        miraTestFormInfo.targetSpecimenComparment = "Cellular";
        miraTestFormInfo.targetSpecimenCollDate = specimenCollectionDate;
        miraTestFormInfo.fastForwardStage = "NorthQC";
        miraTestFormInfo.fastForwardStatus = "Finished";
        miraTestFormInfo.fastForwardSubstatusCode = "";
        miraTestFormInfo.fastForwardSubstatusMsg = "";
        
        MiraTestInfoProvider miraTestInfoProvider = new MiraTestInfoProvider (prodTestInfoPath);
        MiraHttpClient miraHttpClient = new MiraHttpClient ();
        MiraTsvCopier miraTsvCopier = new MiraTsvCopier ();
        MiraTestScenarioBuilder miraTestScenarioBuilder = new MiraTestScenarioBuilder (miraTestInfoProvider, miraHttpClient, miraTsvCopier);
        miraTestScenarioBuilder.buildTestScenario (miraTestFormInfo);
               
        assertTrue (mira.waitForStage (MiraStage.MIRAQC, 60, 60000));
        assertTrue (mira.waitForStatus (MiraStatus.Awaiting));
        
        stampWorkflow (miraId, specimenId, miraLab);
        
                    
    }
    
    private void stampWorkflow (String miraId, String specimenId, MiraLab miraLab) {
        
        Map <String, String> flowcellToJobId          = new HashMap <> ();
        Map <String, String> workflowSuffixToFlowcell = new HashMap <> ();
        
        flowcellToJobId.put ("HWFJMBGXC", "8a848a236fb16b8101708fa7229e68f8");
        flowcellToJobId.put ("HW5FFBGXC", "8a848a236fb16b8101708fa7166468e9");
        flowcellToJobId.put ("HWCGNBGXC", "8a848a236fb16b8101708fa7179668ec");

        workflowSuffixToFlowcell.put ("A_positive", "HWFJMBGXC");
        workflowSuffixToFlowcell.put ("B_positive", "HWFJMBGXC");
        workflowSuffixToFlowcell.put ("C_positive", "HWFJMBGXC");
        workflowSuffixToFlowcell.put ("D_positive", "HW5FFBGXC");
        workflowSuffixToFlowcell.put ("E_positive", "HW5FFBGXC");
        workflowSuffixToFlowcell.put ("F_positive", "HW5FFBGXC");
        workflowSuffixToFlowcell.put ("G_positive", "HW5FFBGXC");
        workflowSuffixToFlowcell.put ("H_positive", "HW5FFBGXC");
        workflowSuffixToFlowcell.put ("I_positive", "HW5FFBGXC");
        workflowSuffixToFlowcell.put ("J_positive", "HW5FFBGXC");
        workflowSuffixToFlowcell.put ("K_positive", "HW5FFBGXC");
        workflowSuffixToFlowcell.put ("nopeptide_pos", "HW5FFBGXC");
        workflowSuffixToFlowcell.put ("unsorted_expanded", "HWCGNBGXC"); // this is the unsorted guy

        String miraPipelineResultOverride = "[{\n" + "\"forwardPcrPrimer\": \"TCRB_1rxn-P43-M164-A15SW\",\n" + "\"reversePcrPrimer\": \"TCRB_1rxn-P43-M164-A15SW\",\n" + "\"sample.flowcell.fcid\": \"190908_NB501176_0706_AHV7HVBGXB\",\n" + "\"sample.flowcell.job.archiveResultsPath\": \"s3://pipeline-cora-test-archive:us-west-2/190908_NB501176_0706_AHV7HVBGXB/v3.1/20190929_0306\",\n" + "\"sample.flowcell.runDate\": \"2019-09-08T07:00:00Z\",\n" + "\"sequencingRead1Primer\": \"\",\n" + "\"sequencingRead2Primer\": \"\"\n" + "}]";

        testLog ("mira is: " + miraId);


        Mira mira = new Mira ();
        mira.clickTestTab (true);
        String fmtString = "%s_%s_%s";

        for (String workflowSuffix : workflowSuffixToFlowcell.keySet ()) {

            String workflowName = String.format (fmtString, specimenId, miraId, workflowSuffix);

            String flowcellId = workflowSuffixToFlowcell.get (workflowSuffix);
            String jobId = flowcellToJobId.get (flowcellId);

            History history = new History ();
            history.gotoOrderDebug (workflowName);
            history.isCorrectPage ();
            Map <WorkflowProperty, String> properties = new HashMap <> ();
            properties.put (WorkflowProperty.lastFinishedPipelineJobId, jobId);
            properties.put (WorkflowProperty.lastFlowcellId, flowcellId);
            history.setWorkflowProperties (properties);

        }

        History history = new History ();
        history.gotoOrderDebug (miraId);
        history.isCorrectPage ();
        history.setWorkflowProperty (WorkflowProperty.miraPipelineResultOverride,
                                     miraPipelineResultOverride.replaceAll ("\n", ""));

        gotoMiraByLabAndId (miraId, miraLab);

        mira.setQCStatus (MiraQCStatus.ACCEPTED);

        mira.clickStatusTab ();
        assertTrue (mira.waitForStage (MiraStage.Publishing));
        assertTrue (mira.waitForStatus (MiraStatus.Finished, 30, 60000));
    }

    private void loginToCora () {
        Login login = new Login ();
        login.doLogin ();
        OrdersList ordersList = new OrdersList ();
        ordersList.isCorrectPage ();
    }

    private void waitForStageAndStatus (MiraStage miraStage, MiraStatus miraStatus) {
        Mira mira = new Mira ();
        mira.clickStatusTab ();
        assertTrue (mira.waitForStage (miraStage));
        assertTrue (mira.waitForStatus (miraStatus));
    }

    private void gotoMiraByLabAndId (String miraId, MiraLab miraLab) {
        CoraPage coraPage = new CoraPage ();
        coraPage.navigateTo (CoraEnvironment.coraTestUrl);
        OrdersList ordersList = new OrdersList ();
        ordersList.isCorrectPage ();
        ordersList.clickMiras ();

        MirasList mirasList = new MirasList ();
        mirasList.isCorrectPage ();
        mirasList.searchAndClickMira (miraId, miraLab);
    }

    private void createSampleManifest (String miraId, MiraLab miraLab) {
        CoraPage coraPage = new CoraPage ();
        coraPage.clickMiras ();
        MirasList mirasList = new MirasList ();

        mirasList.isCorrectPage ();
        mirasList.searchForMira (miraId, miraLab);
        mirasList.clickSelect ();
        mirasList.selectMiraInList (miraId);

        testLog ("creating sample manifest");
        String downloadedFileName = mirasList.clickCreateSampleManifest ();
        testLog ("downloaded sample manifest " + downloadedFileName);

        mirasList.clickMira (miraId);

        waitForStageAndStatus (MiraStage.immunoSEQ, MiraStatus.Awaiting);
        testLog ("mira " + miraId + " now in ImmunoSEQ/Awaiting stage");
    }

    private String createNewMira (MiraLab miraLab, MiraType miraType, MiraPanel miraPanel,
                                  MiraExpansionMethod miraEM, String specimenId) {
        CoraPage coraPage = new CoraPage ();
        coraPage.selectNewMira ();

        Mira mira = new Mira ();
        mira.isCorrectPage ();
        mira.selectLab (miraLab);
        mira.selectType (miraType);
        mira.selectPanel (miraPanel);
        mira.selectExpansionMethod (miraEM);
        mira.enterSpecimenAndFind (specimenId);
        mira.clickSave (false);

        String miraId = mira.getMiraId ();
        testLog ("mira id is: " + miraId);

        List <String> containerIds = mira.getContainerIds ();
        testLog ("container Ids are: ");
        for (String id : containerIds) {
            testLog (id);
        }

        mira.verifyContainerId (containerIds.get (0));
        testLog ("verified container id: " + containerIds.get (0));

        String batchRecord = mira.createNewBatchRecord (miraId);
        testLog ("new batch record is: " + batchRecord);

        testLog ("about to upload batch record");
        mira.uploadBatchRecord (batchRecord);
        testLog ("uploaded batch record");

        mira.clickUploadAndSave (miraId);
        testLog ("clicked upload and save");
        mira.clickSave (true);
        testLog ("clicked save");
        mira.clickMiraPrepComplete ();

        testLog ("mira prep complete");
        mira.refresh ();

        testLog ("prep complete for mira " + miraId);

        return miraId;
    }

    private List <String> createGeneralShipmentFromIntakeManifest (String intakeManifest) {
        CoraPage coraPage = new CoraPage ();
        coraPage.selectNewGeneralShipment ();

        Shipment shipment = new Shipment ();
        shipment.isBatchOrGeneral ();
        shipment.enterShippingCondition (ShippingCondition.Ambient);
        shipment.clickSave ();
        testLog ("saved general shipment " + shipment.getShipmentNum ());

        shipment.gotoAccession ();

        Accession accession = new Accession ();
        accession.isCorrectPage ();
        String fullPath = ClassLoader.getSystemResource (intakeManifest).getPath ();
        accession.uploadIntakeManifest (fullPath);
        accession.clickIntakeComplete ();
        accession.labelingComplete ();
        accession.labelVerificationComplete ();
        accession.clickAccessionComplete ();
        accession.waitForStatus ("Accession Complete");
        testLog ("accession complete");

        List <String> specimenIds = accession.getSpecimenIds ();
        // these should all be the same
        testLog ("specimen ids are: ");
        for (String specimenId : specimenIds) {
            testLog (specimenId);
        }

        return specimenIds;
    }

}
