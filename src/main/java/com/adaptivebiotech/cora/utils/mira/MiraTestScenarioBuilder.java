package com.adaptivebiotech.cora.utils.mira;

import static com.seleniumfy.test.utils.Logging.info;

import java.util.ArrayList;
import java.util.List;
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
    
    private MiraTestInfoProvider miraTestInfoProvider;
    private MiraHttpClient miraHttpClient;

    public MiraTestScenarioBuilder (MiraTestInfoProvider miraTestInfoProvider, MiraHttpClient miraHttpClient) {
        
        this.miraTestInfoProvider = miraTestInfoProvider;    
        this.miraHttpClient = miraHttpClient;
    }
    
    
    public void buildTestScenario(MiraTestFormInfo miraTestFormInfo) {
        
        
        TestScenarioInfo testScenarioInfo = new TestScenarioInfo();
       
        List<MiraTestInfo> miraTestInfos = miraTestInfoProvider.getMiraTestsFromFile ();
        info("number of mira tests is: " + miraTestInfos.size ());
        
        TestTechTransferInfo testTechTransferInfo = new TestTechTransferInfo();
        testTechTransferInfo.workspace = miraTestFormInfo.targetWorkspace;
        testTechTransferInfo.flowcellId = miraTestFormInfo.targetFlowcellId;
        testTechTransferInfo.specimens = new ArrayList<>(miraTestInfos.size ());
        
        TestScenarioProjectInfo projectInfo = new TestScenarioProjectInfo();
        projectInfo.projectId = miraTestFormInfo.targetProjectId;
        projectInfo.accountId = miraTestFormInfo.targetAccountId;
        testScenarioInfo.projectInfo = projectInfo;
        
        testScenarioInfo.scenarioConfig = new TestScenarioConfig();
        
        // let's just use the tsv files...
        
        for (MiraTestInfo miraTestInfo : miraTestInfos) {
            
            testScenarioInfo.fastForwardInfo = buildForward(miraTestFormInfo);
            
            TestSpecimenInfo specimen = buildSpecimen (miraTestInfo, miraTestFormInfo);
            String tsvPath = miraTestInfo.TsvPath;
            info("tsvPath is: " + tsvPath);
            specimen.samples.get (0).tsvPath = tsvPath;
            testTechTransferInfo.specimens.add (specimen);
               
        }
        
        testScenarioInfo.techTransferInfo = testTechTransferInfo;
        
        miraHttpClient.doCoraApiLogin();
        miraHttpClient.postTestScenarioToCora (testScenarioInfo);
               
    }
     
    private TestSpecimenInfo buildSpecimen(MiraTestInfo miraTest, MiraTestFormInfo formInfo) {

        String miraTargetSample = getTargetSampleName(miraTest, formInfo.sourceMiraNumber, formInfo.targetSpecimenNumber,
                                                      formInfo.targetMiraNumber);
        TestSpecimenInfo specimenInfo = new TestSpecimenInfo();
        specimenInfo.name = miraTargetSample;
        specimenInfo.externalSubjectId = miraTargetSample;
        specimenInfo.sampleType = formInfo.targetSpecimenType;
        specimenInfo.sampleSource = formInfo.targetSpecimenSource;
        specimenInfo.compartment = formInfo.targetSpecimenComparment;
        specimenInfo.collectionDate = formInfo.targetSpecimenCollDate;
        specimenInfo.samples = new ArrayList<TestSampleInfo>(1);

        ObjectNode properties = new ObjectMapper().createObjectNode();
        properties.put("Treatment", miraTest.PoolIndicator);
        specimenInfo.properties = properties;

        ObjectNode projProperties = new ObjectMapper().createObjectNode();
        projProperties.put("Var1", formInfo.targetMiraNumber);
        projProperties.put("Var2", formInfo.targetExpansionNumber);
        projProperties.put("Var3", miraTest.CellCount);
        specimenInfo.projectProperties = projProperties;

        TestSampleInfo sampleInfo = new TestSampleInfo();
        sampleInfo.name = miraTargetSample;
        sampleInfo.externalId = miraTargetSample;
        sampleInfo.test = miraTest.PoolIndicator.equals("US") ? "MIRAUNSORTED" : "MIRASORTED";
        sampleInfo.tsvPath = miraTest.TsvPath;
        specimenInfo.samples.add(sampleInfo);

        return specimenInfo;
    }
    
    
    private String getSourceSamplePrefix(MiraTestInfo miraTest, String sourceMiraNumber) {
        String sourceSampleSpecimen = miraTest.SampleName.split("_")[0];
        return String.format("%s_%s", sourceSampleSpecimen, sourceMiraNumber);
    }

    private String getTargetSamplePrefix(String targetSpecimenNumber, String targetMiraNumber) {
        return String.format("%s_%s", targetSpecimenNumber, targetMiraNumber);
    }
    
    private String getTargetSampleName(MiraTestInfo miraTest, 
                                       String sourceMiraNumber,
                                       String targetSpecimenNumber,
                                       String targetMiraNumber) {
        String targetSamplePrefix = getTargetSamplePrefix(targetSpecimenNumber, targetMiraNumber);
        return miraTest.SampleName.replace(getSourceSamplePrefix(miraTest, sourceMiraNumber), targetSamplePrefix);
    }
    
    private TestFastForwardInfo buildForward(MiraTestFormInfo formInfo) {

        TestFastForwardInfo fastForwardInfo = new TestFastForwardInfo();
        fastForwardInfo.stageName = formInfo.fastForwardStage;
        fastForwardInfo.stageStatus = formInfo.fastForwardStatus;
        fastForwardInfo.subStatusCode = formInfo.fastForwardSubstatusCode;
        fastForwardInfo.substatusMsg = formInfo.fastForwardSubstatusMsg;

        return fastForwardInfo;
    }
    
    
}
