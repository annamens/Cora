package com.adaptivebiotech.cora.utils.mira;

import org.testng.annotations.Test;
import com.adaptivebiotech.cora.test.CoraEnvironment;

public class MiraTsvCopierTestSuite {
    
//    @Test
//    public void testCopyTsvFiles () {
//
//        CoraEnvironment.initialization ();
//        
//        MiraTsvCopier miraTsvCopier = new MiraTsvCopier ();
//        String specimenId = "123456";
//        String miraId = "1234";
//        
//        miraTsvCopier.copyTsvFiles (specimenId, miraId);
//    
//    }
    
    @Test
    public void testCopyTsvFile () {
        CoraEnvironment.initialization ();
        MiraTsvCopier miraTsvCopier = new MiraTsvCopier ();
        String specimenId = "SP-123456";
        String miraId = "M-1234";
        String sourceSpecimenNumber = "SP-914830";
        String sourceMiraNumber = "M-1345";
        
        String url = miraTsvCopier.copyTsvFile (miraId, specimenId, 
                                                "https://adaptivetestcasedata.blob.core.windows.net/selenium/tsv/mira/templates/ampl/HWFJMBGXC_0_Adaptive-MIRA-AMPL_SP-914830_M-1345_B_positive.adap.txt.results.tsv.gz",
                                                sourceMiraNumber, sourceSpecimenNumber);
    
        System.out.println("url is: " + url);
    }

}
