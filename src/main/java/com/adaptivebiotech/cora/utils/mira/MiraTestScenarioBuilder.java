package com.adaptivebiotech.cora.utils.mira;

import static com.seleniumfy.test.utils.Logging.info;

import java.util.ArrayList;
import com.adaptivebiotech.cora.utils.mira.mirasource.MiraSourceInfo;
import com.adaptivebiotech.cora.utils.mira.mirasource.SourceSpecimenInfo;
import com.adaptivebiotech.cora.utils.mira.techtransfer.TestFastForwardInfo;
import com.adaptivebiotech.cora.utils.mira.techtransfer.TestSampleInfo;
import com.adaptivebiotech.cora.utils.mira.techtransfer.TestScenarioConfig;
import com.adaptivebiotech.cora.utils.mira.techtransfer.TestScenarioInfo;
import com.adaptivebiotech.cora.utils.mira.techtransfer.TestScenarioProjectInfo;
import com.adaptivebiotech.cora.utils.mira.techtransfer.TestSpecimenInfo;
import com.adaptivebiotech.cora.utils.mira.techtransfer.TestTechTransferInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * build MIRA test scenario
 * 
 * @author mgrossman
 *
 */
public class MiraTestScenarioBuilder {

    private MiraHttpClient miraHttpClient;
    private MiraTsvCopier  miraTsvCopier;

    public MiraTestScenarioBuilder (MiraHttpClient miraHttpClient,
                                    MiraTsvCopier miraTsvCopier) {

        this.miraHttpClient = miraHttpClient;
        this.miraTsvCopier = miraTsvCopier;
    }

    public void buildTestScenario (MiraTargetInfo miraTargetInfo,
                                   MiraSourceInfo miraSourceInfo) {

        TestScenarioInfo testScenarioInfo = new TestScenarioInfo ();

        SourceSpecimenInfo[] sourceSpecimenInfos = miraSourceInfo.getSourceSpecimenInfos ();

        info ("number of mira tests is: " + sourceSpecimenInfos.length);

        TestTechTransferInfo testTechTransferInfo = new TestTechTransferInfo ();
        testTechTransferInfo.workspace = miraTargetInfo.targetWorkspace;
        testTechTransferInfo.flowcellId = miraTargetInfo.targetFlowcellId;
        testTechTransferInfo.specimens = new ArrayList <> (sourceSpecimenInfos.length);

        TestScenarioProjectInfo projectInfo = new TestScenarioProjectInfo ();
        projectInfo.projectId = miraTargetInfo.targetProjectId;
        projectInfo.accountId = miraTargetInfo.targetAccountId;
        testScenarioInfo.projectInfo = projectInfo;
        testScenarioInfo.fastForwardInfo = buildForward (miraTargetInfo);

        testScenarioInfo.scenarioConfig = new TestScenarioConfig ();

        for (SourceSpecimenInfo sourceSpecimenInfo : sourceSpecimenInfos) {

            TestSpecimenInfo specimen = buildSpecimen (sourceSpecimenInfo, miraTargetInfo, miraSourceInfo);

            String tsvPath = miraTsvCopier.copyTsvFile (miraTargetInfo.targetMiraNumber,
                                                        miraTargetInfo.targetSpecimenNumber,
                                                        sourceSpecimenInfo.getTsvPath (),
                                                        miraSourceInfo.getSourceMiraId (),
                                                        miraSourceInfo.getSourceSpecimenId ());

            info ("tsvPath is: " + tsvPath);
            specimen.samples.get (0).tsvPath = tsvPath;
            testTechTransferInfo.specimens.add (specimen);

        }

        testScenarioInfo.techTransferInfo = testTechTransferInfo;

        miraHttpClient.doCoraApiLogin ();
        miraHttpClient.postTestScenarioToCora (testScenarioInfo);

    }

    private TestSpecimenInfo buildSpecimen (SourceSpecimenInfo sourceSpecimenInfo, MiraTargetInfo miraTargetInfo,
                                            MiraSourceInfo miraSourceInfo) {

        String miraTargetSample = sourceSpecimenInfo.getTargetWorkflowName (miraSourceInfo.getSourceSpecimenId (),
                                                                            miraSourceInfo.getSourceMiraId (),
                                                                            miraTargetInfo.targetSpecimenNumber,
                                                                            miraTargetInfo.targetMiraNumber);
        TestSpecimenInfo specimenInfo = new TestSpecimenInfo ();
        specimenInfo.name = miraTargetSample;
        specimenInfo.externalSubjectId = miraTargetSample;
        specimenInfo.sampleType = miraTargetInfo.targetSpecimenType;
        specimenInfo.sampleSource = miraTargetInfo.targetSpecimenSource;
        specimenInfo.compartment = miraTargetInfo.targetSpecimenComparment;
        specimenInfo.collectionDate = miraTargetInfo.targetSpecimenCollDate;
        specimenInfo.samples = new ArrayList <TestSampleInfo> (1);

        ObjectNode properties = new ObjectMapper ().createObjectNode ();
        properties.put ("Treatment", sourceSpecimenInfo.getPoolIndicator ());
        specimenInfo.properties = properties;

        ObjectNode projProperties = new ObjectMapper ().createObjectNode ();
        projProperties.put ("Var1", miraTargetInfo.targetMiraNumber);
        projProperties.put ("Var2", miraTargetInfo.targetExpansionNumber);
        projProperties.put ("Var3", sourceSpecimenInfo.getCellCount ());
        specimenInfo.projectProperties = projProperties;

        TestSampleInfo sampleInfo = new TestSampleInfo ();
        sampleInfo.name = miraTargetSample;
        sampleInfo.externalId = miraTargetSample;
        sampleInfo.test = sourceSpecimenInfo.getPoolIndicator ().equals ("US") ? "MIRAUNSORTED" : "MIRASORTED";
        sampleInfo.tsvPath = sourceSpecimenInfo.getTsvPath ();
        specimenInfo.samples.add (sampleInfo);

        return specimenInfo;
    }

    private TestFastForwardInfo buildForward (MiraTargetInfo formInfo) {

        TestFastForwardInfo fastForwardInfo = new TestFastForwardInfo ();
        fastForwardInfo.stageName = formInfo.fastForwardStage;
        fastForwardInfo.stageStatus = formInfo.fastForwardStatus;
        fastForwardInfo.subStatusCode = formInfo.fastForwardSubstatusCode;
        fastForwardInfo.substatusMsg = formInfo.fastForwardSubstatusMsg;

        return fastForwardInfo;
    }

}
