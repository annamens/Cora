package com.adaptivebiotech.cora.utils.mira;

import static com.seleniumfy.test.utils.Logging.error;
import static com.seleniumfy.test.utils.Logging.info;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptivebiotech.cloudfiles.CloudFilesClient;
import com.adaptivebiotech.cloudfiles.configs.AWSCredentials;
import com.adaptivebiotech.cloudfiles.exceptions.ConfigException;


public class MiraTsvCopier {
    
    // data in azure in adaptivetestcasedata/selenium/tsv/mira/templates/ampl

    // maybe try the creds in flora repo? 
    
    private CloudFilesClient sourceClient;
    private CloudFilesClient targetClient;
    private AWSCredentials awsCredentials;
    
    private static final Logger logger = LoggerFactory.getLogger (MiraTsvCopier.class);
    
    public MiraTsvCopier () {
        
        awsCredentials = new AWSCredentials (awskey, awssecret);
        try {
            sourceClient = new CloudFilesClient(logger).withAws (awsCredentials);
            targetClient = sourceClient; // since we are using same creds
        } catch (ConfigException e) {
            error (e.getMessage ());
            throw new RuntimeException (e);
        }
        
        
    }
    
    public void copyTsvFile (String textToReplace, String sourceTsvPath, String targetDataPath,
                             String targetSpecimenNumber, String targetMiraNumber) {
        File tmpFile = null;
        try {
            
            // source file = s3://cora-scripts-data-xfer-test/mgrossman/M-1345/Pools/HW5FFBGXC_0_Adaptive-MIRA-AMPL_SP-914830_M-1345_D_positive.adap.txt.results.tsv.gz
            // we want to replace "SP-914830_M-1345" with targetSpecimenNumber_targetMiraNumber
            // TODO need to refigure this for azure
            
            String newSpecimenMiraNumber = targetSpecimenNumber + "_" + targetMiraNumber;
            String[] sourcePathParts = sourceTsvPath.split ("/");
            String sourceTsvFilename = sourcePathParts[sourcePathParts.length - 1];
            info("sourceTsvFilename is: " + sourceTsvFilename);
            
            String targetTsvFilename = sourceTsvFilename.replace (textToReplace, newSpecimenMiraNumber);
            String targetPath = targetDataPath + "/Pools/" + targetTsvFilename;
            info("targetPath is: " + targetPath);

            
            
//            String targetPath = String.format("%s/%s/%s", targetDataPath,
//                                          "Pools", getTargetTsvName(sourceClient, miraTestInfo, 
//                                                                    sourceMiraNumber,
//                                                                    targetSpecimenNumber,
//                                                                    targetMiraNumber));
        
        
            tmpFile = sourceClient.getAsTempFile(sourceTsvPath);
            info("tmpFile is: " + tmpFile.getAbsolutePath ());
            targetClient.putFile (targetPath, tmpFile);
        } catch (Exception e) {
            error (e.getMessage ());
            throw new RuntimeException (e);
        } finally {
            if(tmpFile != null) {
                tmpFile.delete ();
            }
        } 
    }
    
//    
//    private String getSourceSamplePrefix(String sourceSampleName, String sourceMiraNumber) {
//        String sourceSampleSpecimen = sourceSampleName.split("_")[0];
//        return String.format("%s_%s", sourceSampleSpecimen, sourceMiraNumber);
//    }
//
//    private String getTargetSamplePrefix(String targetSpecimenNumber, String targetMiraNumber) {
//        return String.format("%s_%s", targetSpecimenNumber, targetMiraNumber);
//    }
//    
//    private String getTargetTsvName(CloudFilesClient cf, 
//                                    String sourceFilePath,
//                                    String sourceMiraNumber,
//                                    String sourceSampleName,
//                                    String targetSpecimenNumber,
//                                    String targetMiraNumber) throws Exception {
//        try {
//            CloudFileInfo tsvInfo = cf.parseUrl(sourceFilePath);
//            String tsvName = tsvInfo.getObjectKey().substring(tsvInfo.getObjectKey().lastIndexOf("/") + 1);
//            return tsvName.replace(getSourceSamplePrefix(sourceSampleName, sourceMiraNumber), getTargetSamplePrefix(targetSpecimenNumber,
//                                                                                                            targetMiraNumber));
//        }
//        catch (Exception e) {
//            error(e.getMessage());
//            throw e;
//        }
//    }

}
