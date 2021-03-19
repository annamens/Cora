package com.adaptivebiotech.cora.utils.mira;

import static com.seleniumfy.test.utils.Logging.error;
import static com.seleniumfy.test.utils.Logging.info;
import static org.testng.Assert.assertEquals;
import com.adaptivebiotech.cora.test.CoraEnvironment;


public class MiraTsvCopier {   
    
    private String containerName = "selenium";
    private String accountName = "adaptivetestcasedata";
//    private String sourceFilePrefix = "tsv/mira/templates/ampl/";
        
    private String destinationPrefix = "tsv/mira/automatedtest";
    private String destinationBase = "%s/%s/%s";
    
    private String azCLI = "az";
    
    private String loginCommandBase = azCLI + " login -u %s -p %s";
    private String downloadCommandBase = azCLI + " storage blob download --container-name %s --auth-mode login --account-name %s --name %s --file %s";
    private String uploadCommandBase = azCLI + " storage blob upload --container-name %s --auth-mode login --account-name %s --name %s --file %s";
    
    private String tmpfileBase = "target/%s";    
    
    public MiraTsvCopier () {
        String azureLogin = CoraEnvironment.azureLogin;
        String azurePassword = CoraEnvironment.azurePassword;
        
        String loginCommand = String.format (loginCommandBase, azureLogin, azurePassword);
        runCommand (loginCommand);
    }
    
    
    public String copyTsvFile (String miraId, String specimenId, String sourceFilePath,
                               String sourceMiraId, String sourceSpecimenId) {
        String specimenId_miraId = specimenId + "_" + miraId;
        String textToReplace = sourceSpecimenId + "_" + sourceMiraId;
        
        String [] sourceFilePathParts = sourceFilePath.split ("/");
        String sourceFile = sourceFilePathParts[sourceFilePathParts.length - 1];
        String sourceContainerName = sourceFilePathParts[3];
        String sourceFilePrefix = "";
        for(int i = 4; i < sourceFilePathParts.length - 1; i++) {
            sourceFilePrefix += sourceFilePathParts[i] + "/";
        }
        
        String tmpFilename = String.format (tmpfileBase, sourceFile);
        String downloadCommand = String.format (downloadCommandBase, 
                                              sourceContainerName, accountName, 
                                              sourceFilePrefix + sourceFile, tmpFilename);
        
        runCommand (downloadCommand);
        
        String renamedFile = sourceFile.replace (textToReplace, specimenId_miraId);
        String destinationFile = String.format (destinationBase, 
                                                destinationPrefix, miraId, renamedFile);
        
        String uploadCommand = String.format (uploadCommandBase,
                                              containerName, accountName, 
                                              destinationFile, tmpFilename);
        
        runCommand (uploadCommand);
        
        return "https://adaptivetestcasedata.blob.core.windows.net/" + containerName + "/" + destinationFile;
    }
    
    private void runCommand (String command) {
        if (command.contains ("login")) {
            info ("logging into azure");
        } else {
            info ("running " + command);
        }
        try {
            Process loginProcess = Runtime.getRuntime ().exec (command);
            int exitValue = loginProcess.waitFor ();
            info ("command exited with value " + exitValue);
            assertEquals(exitValue, 0);
        } catch (Exception e) {
            error (e.getMessage ());
            throw new RuntimeException (e);
        }
    }
}
