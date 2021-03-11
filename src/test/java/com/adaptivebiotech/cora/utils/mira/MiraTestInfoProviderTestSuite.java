package com.adaptivebiotech.cora.utils.mira;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import java.util.List;
import org.testng.annotations.Test;

public class MiraTestInfoProviderTestSuite {
    
    @Test
    public void testGetMiraTestInfo () {
        
        String prodTestInfoPath = "MIRA/M-457_test_info.json";

        MiraTestInfoProvider miraTestInfoProvider = new MiraTestInfoProvider (prodTestInfoPath);
        List<MiraTestInfo> miraTestInfos = miraTestInfoProvider.getMiraTestsFromFile ();
        assertNotNull (miraTestInfos);
        assertEquals(miraTestInfos.size (), 13);
 
        verifyMiraTestInfo (miraTestInfos.get (0), "R-054937", "SP-708271", 
                            "s3://pipeline-north-production-archive:us-west-2/200228_NB551550_0137_AHWFJMBGXC/v3.1/20200301_1245/packaged/rd.Human.TCRB-v4b.nextseq.156x12x0.vblocks.ultralight.rev1/HWFJMBGXC_0_Adaptive-MIRA-AMPL_SP-687684_M-457_A_positive.adap.txt.results.tsv.gz");
        
        
    }
    
    private void verifyMiraTestInfo (MiraTestInfo miraTestInfo, String orderNumber, String specimenNumber, String tsvPath) {
        assertEquals(miraTestInfo.TsvPath, tsvPath);
        assertEquals(miraTestInfo.OrderNumber, orderNumber);
        assertEquals(miraTestInfo.SpecimenNumber, specimenNumber);
    }
    
    

}
