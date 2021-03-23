package com.adaptivebiotech.cora.test.mira;

import static com.adaptivebiotech.test.utils.Logging.testLog;
import static org.testng.Assert.assertTrue;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestUrl;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.adaptivebiotech.cora.utils.mira.MiraSourceInfoProvider;
import com.adaptivebiotech.cora.utils.mira.MiraTargetInfo;
import com.adaptivebiotech.cora.utils.mira.MiraTestScenarioBuilder;
import com.adaptivebiotech.cora.utils.mira.MiraTsvCopier;
import com.adaptivebiotech.cora.utils.mira.mirasource.MiraSourceInfo;
import com.adaptivebiotech.cora.utils.mira.mirasource.SourceSpecimenInfo;
import com.adaptivebiotech.test.utils.PageHelper.ShippingCondition;
import com.adaptivebiotech.test.utils.PageHelper.StageName;
import com.adaptivebiotech.test.utils.PageHelper.StageStatus;
import com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty;

public class MiraTestSuite extends CoraBaseBrowser {

    @Test
    public void testAMPL_MIRA_slimmedInputs () {

        String miraSourceInfoFile = "MIRA/mira_ampl_slim_testInfo.json";

        MiraSourceInfoProvider miraSourceInfoProvider = new MiraSourceInfoProvider (miraSourceInfoFile);
        MiraSourceInfo miraSourceInfo = miraSourceInfoProvider.getMiraSourceInfoFromFile ();

        String sourceMiraNumber = miraSourceInfo.getSourceMiraId ();
        String sourceSpecimenNumber = miraSourceInfo.getSourceSpecimenId ();
        MiraLab miraLab = miraSourceInfo.getMiraLab ();
        MiraType miraType = miraSourceInfo.getMiraType ();
        MiraPanel miraPanel = miraSourceInfo.getMiraPanel ();
        MiraExpansionMethod miraExpansionMethod = miraSourceInfo.getExpansionMethod ();

        loginToCora ();

        List <String> specimenIds = createGeneralShipmentFromIntakeManifest ("MIRA/cora-intakemanifest_28JUL2020.xlsx");

        String miraId = createNewMira (miraLab,
                                       miraType,
                                       miraPanel,
                                       miraExpansionMethod,
                                       specimenIds.get (0));

        gotoMiraByLabAndId (miraId, miraLab);

        Mira mira = new Mira ();
        String specimenId = specimenIds.get (0);
        String expansionId = mira.getExpansionId ();

        waitForStageAndStatus (MiraStage.PoolExtraction, MiraStatus.Ready);

        createSampleManifest (miraId, miraLab);

        MiraTargetInfo miraTargetInfo = buildMiraTargetInfo (sourceMiraNumber,
                                                             sourceSpecimenNumber,
                                                             miraId,
                                                             specimenId,
                                                             expansionId);

        MiraHttpClient miraHttpClient = new MiraHttpClient ();
        MiraTsvCopier miraTsvCopier = new MiraTsvCopier ();
        MiraTestScenarioBuilder miraTestScenarioBuilder = new MiraTestScenarioBuilder (miraHttpClient, miraTsvCopier);
        miraTestScenarioBuilder.buildTestScenarioAndPostToCora (miraTargetInfo, miraSourceInfo);

        assertTrue (mira.waitForStage (MiraStage.MIRAQC, 60 * 60, 60));
        assertTrue (mira.waitForStatus (MiraStatus.Awaiting));

        stampWorkflowsAndWaitForCompletion (miraId, specimenId, miraLab, miraSourceInfo);

    }

    private MiraTargetInfo buildMiraTargetInfo (String sourceMiraNumber, String sourceSpecimenNumber, String miraId,
                                                String specimenId, String expansionId) {
        String specimenCollectionDate = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format (LocalDateTime.now ()); // YYYY-MM-DDThh:mm:ss
        String specimenType = "Blood";
        String specimenSource = "Blood";
        String specimenCompartment = "Cellular";
        String fastForwardStage = "NorthQC";
        String fastForwardStatus = "Finished";

        return new MiraTargetInfo (coraTestUrl, miraId, specimenId, expansionId, CoraEnvironment.miraProjectId,
                CoraEnvironment.miraAccountId,
                specimenType, specimenSource, specimenCompartment, specimenCollectionDate,
                fastForwardStage, fastForwardStatus);

    }

    private void stampWorkflowsAndWaitForCompletion (String miraId, String specimenId, MiraLab miraLab,
                                                     MiraSourceInfo miraSourceInfo) {

        String miraPipelineResultOverride = "[{\n" + "\"forwardPcrPrimer\": \"TCRB_1rxn-P43-M164-A15SW\",\n" + "\"reversePcrPrimer\": \"TCRB_1rxn-P43-M164-A15SW\",\n" + "\"sample.flowcell.fcid\": \"190908_NB501176_0706_AHV7HVBGXB\",\n" + "\"sample.flowcell.job.archiveResultsPath\": \"s3://pipeline-cora-test-archive:us-west-2/190908_NB501176_0706_AHV7HVBGXB/v3.1/20190929_0306\",\n" + "\"sample.flowcell.runDate\": \"2019-09-08T07:00:00Z\",\n" + "\"sequencingRead1Primer\": \"\",\n" + "\"sequencingRead2Primer\": \"\"\n" + "}]";

        testLog ("setting workflow properties");
        Mira mira = new Mira ();
        mira.clickTestTab (true);

        SourceSpecimenInfo[] sourceSpecimenInfos = miraSourceInfo.getSourceSpecimenInfos ();
        for (SourceSpecimenInfo sourceSpecimenInfo : sourceSpecimenInfos) {
            stampWorkflow (miraId, specimenId, miraSourceInfo, sourceSpecimenInfo);
        }

        History history = new History ();
        history.gotoOrderDebug (miraId);
        history.isCorrectPage ();
        history.setWorkflowProperty (WorkflowProperty.miraPipelineResultOverride,
                                     miraPipelineResultOverride.replaceAll ("\n", ""));

        gotoMiraByLabAndId (miraId, miraLab);

        testLog ("going to accept MIRA QC");
        mira.setQCStatus (MiraQCStatus.ACCEPTED);
        testLog ("accepted MIRA QC");

        // skip MIRAAgate stage

        testLog ("skipping MIRAAgate stage");
        waitForStageAndStatus (MiraStage.MIRAAgate, MiraStatus.Ready);
        history.gotoOrderDebug (miraId);
        history.isCorrectPage ();
        history.forceStatusUpdate (StageName.Publishing, StageStatus.Ready);

        gotoMiraByLabAndId (miraId, miraLab);

        mira.clickStatusTab ();
        assertTrue (mira.waitForStage (MiraStage.Publishing));
        assertTrue (mira.waitForStatus (MiraStatus.Finished, 30 * 60, 60));
    }

    private void stampWorkflow (String miraId, String specimenId, MiraSourceInfo miraSourceInfo,
                                SourceSpecimenInfo sourceSpecimenInfo) {
        String targetWorkflowName = sourceSpecimenInfo.getTargetWorkflowName (miraSourceInfo.getSourceSpecimenId (),
                                                                              miraSourceInfo.getSourceMiraId (),
                                                                              specimenId,
                                                                              miraId);
        testLog ("target workflow is: " + targetWorkflowName);
        String flowcell = sourceSpecimenInfo.getFlowcell ();
        String jobId = sourceSpecimenInfo.getJobId ();

        History history = new History ();
        history.gotoOrderDebug (targetWorkflowName);
        history.isCorrectPage ();
        Map <WorkflowProperty, String> properties = new HashMap <> ();
        properties.put (WorkflowProperty.lastFinishedPipelineJobId, jobId);
        properties.put (WorkflowProperty.lastFlowcellId, flowcell);
        history.setWorkflowProperties (properties);
        testLog ("set properties for workflow " + targetWorkflowName);
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
