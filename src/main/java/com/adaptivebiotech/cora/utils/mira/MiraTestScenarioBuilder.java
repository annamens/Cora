package com.adaptivebiotech.cora.utils.mira;

import static com.seleniumfy.test.utils.Logging.info;
import static com.seleniumfy.test.utils.Logging.error;


import java.io.File;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.adaptivebiotech.cloudfiles.CloudFileInfo;
import com.adaptivebiotech.cloudfiles.CloudFilesClient;
import com.adaptivebiotech.cora.utils.mira.testscenario.TestScenarioConfig;
import com.adaptivebiotech.cora.utils.mira.testscenario.TestScenarioInfo;
import com.adaptivebiotech.cora.utils.mira.testscenario.TestScenarioProjectInfo;
import com.adaptivebiotech.cora.utils.mira.testscenario.TestSpecimenInfo;
import com.adaptivebiotech.cora.utils.mira.testscenario.TestTechTransferInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * build MIRA test scenario
 * 
 * @author mgrossman
 *
 */
public class MiraTestScenarioBuilder {

    private final String prodTestInfoPath = "MIRA/prod_test_info.json";
    private final String sourceMiraNumber = "M-1345";
    // TODO use azure
    private final String miraTSVSourcePath = "s3://cora-scripts-data-xfer-test/mgrossman/M-1345/Pools/";
    private ObjectMapper objectMapper = new ObjectMapper();
    
    private final String targetWorkspace = "Adaptive-Testing";
    private final String flowcellId = "XMIRASCENARIO";
    
    private MiraTestInfoProvider miraTestInfoProvider;
    private CloudFilesClient sourceCf;
    private CloudFilesClient targetCf;

    
    public MiraTestScenarioBuilder (MiraTestInfoProvider miraTestInfoProvider,
                                    CloudFilesClient sourceCf,
                                    CloudFilesClient targetCf) {
        
        this.miraTestInfoProvider = miraTestInfoProvider;
        this.sourceCf = sourceCf;
        this.targetCf = targetCf;
        
    }
    
    
    public void buildTestScenario(UUID projectId, UUID accountId, String targetDataPath, String targetSpecimenNumber,
                                  String targetMiraNumber) throws Exception {
        
        
        TestScenarioInfo testScenarioInfo = new TestScenarioInfo();
       
        List<MiraTestInfo> miraTestInfos = miraTestInfoProvider.getMiraTestsFromFile (prodTestInfoPath);
        info("number of mira tests is: " + miraTestInfos.size ());
        
        TestTechTransferInfo testTechTransferInfo = new TestTechTransferInfo();
        testTechTransferInfo.Workspace = targetWorkspace;
        testTechTransferInfo.FlowcellId = flowcellId;
        testTechTransferInfo.Specimens = new ArrayList<>(miraTestInfos.size ());
        
        TestScenarioProjectInfo projectInfo = new TestScenarioProjectInfo();
        projectInfo.ProjectId = projectId;
        projectInfo.AccountId = accountId;
        testScenarioInfo.ProjectInfo = projectInfo;
        
        testScenarioInfo.ScenarioConfig = new TestScenarioConfig();
        
        for (MiraTestInfo miraTestInfo : miraTestInfos) {
            File resultFile = null;
            
            
            // source = s3://cora-scripts-data-xfer-test/mgrossman/M-1345/Pools/HW5FFBGXC_0_Adaptive-MIRA-AMPL_SP-914830_M-1345_D_positive.adap.txt.results.tsv.gz
            String targetPath = String.format("%s/%s/%s", targetDataPath,
                                              "Pools", getTargetTsvName(sourceCf, miraTestInfo, 
                                                                        sourceMiraNumber,
                                                                        targetSpecimenNumber,
                                                                        targetMiraNumber));
            
            info("targetPath is: " + targetPath);
            
            resultFile = sourceCf.getAsTempFile(miraTestInfo.TsvPath);
            info("resultFile is: " + resultFile.getAbsolutePath ());
            targetCf.putFile (targetPath, resultFile);
            
            
        }
        
  
//
//
//        CloudFilesClient sourceCf = CloudTools.getCloudFilesClient(config, Optional.empty(), logger);
//        CloudFilesClient targetCf = CloudTools.getCloudFilesClient(config, Optional.of(formInfo.TargetEnvironmentType), logger);
//

//
//
//        for (Cora.MiraTestInfo miraTest : miraTests) {
//            File resultFile = null;
//            try {
//                // Build fast forward object
//                scenario.FastForwardInfo = buildForward(formInfo);;
//
//                // Pull source file and upload to target data path
//                String targetPath = String.format("%s/%s/%s", formInfo.TargetDataPath,
//                        "Pools", getTargetTsvName(sourceCf, miraTest, formInfo));
//
//                Logger.warn("target path is: " + targetPath);
//
//                resultFile = sourceCf.getAsTempFile(miraTest.TsvPath);
//
//                Logger.warn("resultFile is: " + resultFile.getAbsolutePath());
//                targetCf.putFile(targetPath, resultFile);
//
//                // Build specimen object and update with target tsv path
//                CoraTestScenario.TestSpecimenInfo specimen = buildSpecimen(miraTest, formInfo);
//                specimen.Samples.get(0).TsvPath = targetPath;
//                techTransfer.Specimens.add(specimen);
//            }
//            catch (Exception e) {
//                Logger.error(e.getMessage());
//                throw e;
//            }
//            finally {
//                if (resultFile != null) resultFile.delete();
//            }
//        }
//
//        // Update scenario object and call target endpoint to create order
//        scenario.TechTransferInfo = techTransfer;
//        callTechTransferEndpoint(CoraTestScenario.TestScenarioInfo.toJson(scenario), formInfo);
//
//        response().setHeader("Content-Disposition",
//                String.format("attachment; filename=%s.json", formInfo.TargetMiraNumber));
//        return(ok(CoraTestScenario.TestScenarioInfo.toJson(scenario)));
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
    
    private String getTargetTsvName(CloudFilesClient cf, 
                                    MiraTestInfo miraTest, 
                                    String sourceMiraNumber,
                                    String targetSpecimenNumber,
                                    String targetMiraNumber) throws Exception {
        try {
            CloudFileInfo tsvInfo = cf.parseUrl(miraTest.TsvPath);
            String tsvName = tsvInfo.getObjectKey().substring(tsvInfo.getObjectKey().lastIndexOf("/") + 1);
            return tsvName.replace(getSourceSamplePrefix(miraTest, sourceMiraNumber), getTargetSamplePrefix(targetSpecimenNumber,
                                                                                                            targetMiraNumber));
        }
        catch (Exception e) {
            error(e.getMessage());
            throw e;
        }
    }
    
   
    
    
}
