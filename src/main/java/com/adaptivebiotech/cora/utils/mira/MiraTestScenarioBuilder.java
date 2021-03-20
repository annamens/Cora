package com.adaptivebiotech.cora.utils.mira;

import static com.seleniumfy.test.utils.Logging.info;

import java.util.ArrayList;
import com.adaptivebiotech.cora.utils.mira.MiraSourceInfo.SourceSpecimenInfo;
import com.adaptivebiotech.cora.utils.mira.testscenario.TestFastForwardInfo;
import com.adaptivebiotech.cora.utils.mira.testscenario.TestSampleInfo;
import com.adaptivebiotech.cora.utils.mira.testscenario.TestScenarioConfig;
import com.adaptivebiotech.cora.utils.mira.testscenario.TestScenarioInfo;
import com.adaptivebiotech.cora.utils.mira.testscenario.TestScenarioProjectInfo;
import com.adaptivebiotech.cora.utils.mira.testscenario.TestSpecimenInfo;
import com.adaptivebiotech.cora.utils.mira.testscenario.TestTechTransferInfo;
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

    public void buildTestScenario (MiraTestFormInfo miraTestFormInfo,
                                   MiraSourceInfo miraSourceInfo) {

        TestScenarioInfo testScenarioInfo = new TestScenarioInfo ();

        SourceSpecimenInfo[] specimenInfos = miraSourceInfo.getSpecimenInfos ();

        info ("number of mira tests is: " + specimenInfos.length);

        TestTechTransferInfo testTechTransferInfo = new TestTechTransferInfo ();
        testTechTransferInfo.workspace = miraTestFormInfo.targetWorkspace;
        testTechTransferInfo.flowcellId = miraTestFormInfo.targetFlowcellId;
        testTechTransferInfo.specimens = new ArrayList <> (specimenInfos.length);

        TestScenarioProjectInfo projectInfo = new TestScenarioProjectInfo ();
        projectInfo.projectId = miraTestFormInfo.targetProjectId;
        projectInfo.accountId = miraTestFormInfo.targetAccountId;
        testScenarioInfo.projectInfo = projectInfo;
        testScenarioInfo.fastForwardInfo = buildForward (miraTestFormInfo);

        testScenarioInfo.scenarioConfig = new TestScenarioConfig ();

        for (SourceSpecimenInfo specimenInfo : specimenInfos) {

            TestSpecimenInfo specimen = buildSpecimen (specimenInfo, miraTestFormInfo);

            String tsvPath = miraTsvCopier.copyTsvFile (miraTestFormInfo.targetMiraNumber,
                                                        miraTestFormInfo.targetSpecimenNumber,
                                                        specimenInfo.getTsvPath (),
                                                        miraTestFormInfo.sourceMiraNumber,
                                                        miraTestFormInfo.sourceSpecimenNumber);

            info ("tsvPath is: " + tsvPath);
            specimen.samples.get (0).tsvPath = tsvPath;
            testTechTransferInfo.specimens.add (specimen);

        }

        testScenarioInfo.techTransferInfo = testTechTransferInfo;

        miraHttpClient.doCoraApiLogin ();
        miraHttpClient.postTestScenarioToCora (testScenarioInfo);

    }

    private TestSpecimenInfo buildSpecimen (SourceSpecimenInfo sourceSpecimenInfo, MiraTestFormInfo formInfo) {

        String miraTargetSample = getTargetSampleName (sourceSpecimenInfo,
                                                       formInfo.sourceMiraNumber,
                                                       formInfo.targetSpecimenNumber,
                                                       formInfo.targetMiraNumber);
        TestSpecimenInfo specimenInfo = new TestSpecimenInfo ();
        specimenInfo.name = miraTargetSample;
        specimenInfo.externalSubjectId = miraTargetSample;
        specimenInfo.sampleType = formInfo.targetSpecimenType;
        specimenInfo.sampleSource = formInfo.targetSpecimenSource;
        specimenInfo.compartment = formInfo.targetSpecimenComparment;
        specimenInfo.collectionDate = formInfo.targetSpecimenCollDate;
        specimenInfo.samples = new ArrayList <TestSampleInfo> (1);

        ObjectNode properties = new ObjectMapper ().createObjectNode ();
        properties.put ("Treatment", sourceSpecimenInfo.getPoolIndicator ());
        specimenInfo.properties = properties;

        ObjectNode projProperties = new ObjectMapper ().createObjectNode ();
        projProperties.put ("Var1", formInfo.targetMiraNumber);
        projProperties.put ("Var2", formInfo.targetExpansionNumber);
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

    private String getSourceSamplePrefix (SourceSpecimenInfo sourceSpecimenInfo, String sourceMiraNumber) {
        String sourceSampleSpecimen = sourceSpecimenInfo.getWorkflowName ().split ("_")[0];
        return String.format ("%s_%s", sourceSampleSpecimen, sourceMiraNumber);
    }

    private String getTargetSamplePrefix (String targetSpecimenNumber, String targetMiraNumber) {
        return String.format ("%s_%s", targetSpecimenNumber, targetMiraNumber);
    }

    private String getTargetSampleName (SourceSpecimenInfo sourceSpecimenInfo,
                                        String sourceMiraNumber,
                                        String targetSpecimenNumber,
                                        String targetMiraNumber) {
        String targetSamplePrefix = getTargetSamplePrefix (targetSpecimenNumber, targetMiraNumber);
        return sourceSpecimenInfo.getWorkflowName ()
                                 .replace (getSourceSamplePrefix (sourceSpecimenInfo, sourceMiraNumber),
                                           targetSamplePrefix);
    }

    private TestFastForwardInfo buildForward (MiraTestFormInfo formInfo) {

        TestFastForwardInfo fastForwardInfo = new TestFastForwardInfo ();
        fastForwardInfo.stageName = formInfo.fastForwardStage;
        fastForwardInfo.stageStatus = formInfo.fastForwardStatus;
        fastForwardInfo.subStatusCode = formInfo.fastForwardSubstatusCode;
        fastForwardInfo.substatusMsg = formInfo.fastForwardSubstatusMsg;

        return fastForwardInfo;
    }

}
