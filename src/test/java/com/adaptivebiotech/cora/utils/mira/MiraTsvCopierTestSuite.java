package com.adaptivebiotech.cora.utils.mira;

import org.testng.annotations.Test;

public class MiraTsvCopierTestSuite {
    
    @Test
    public void testCopyTsvFile () {
        String textToReplace = "SP-914830_M-1345";
//        String sourceTsvPath = "s3://cora-scripts-data-xfer-test/mgrossman/M-1345/Pools/HW5FFBGXC_0_Adaptive-MIRA-AMPL_SP-914830_M-1345_D_positive.adap.txt.results.tsv.gz";
        
//        String targetDataPath = "s3://cora-scripts-data-xfer-test/mgrossman/test1";
        
        String sourceTsvPath = "https://adaptivetestcasedata.blob.core.windows.net/selenium/tsv/mira/templates/ampl/HW5FFBGXC_0_Adaptive-MIRA-AMPL_SP-914830_M-1345_D_positive.adap.txt.results.tsv.gz";
        String targetDataPath = "https://adaptivetestcasedata.blob.core.windows.net/selenium/tsv/mira/mgrossman/test1";
        
        String targetSpecimenNumber = "SP-123456";
        String targetMiraNumber = "M-1234";
        
        MiraTsvCopier miraTsvCopier = new MiraTsvCopier();
        
        miraTsvCopier.copyTsvFile (textToReplace, sourceTsvPath, targetDataPath, 
                                   targetSpecimenNumber, targetMiraNumber);
    
    
    }

}
