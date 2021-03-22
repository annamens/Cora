package com.adaptivebiotech.cora.utils.mira;

import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.utils.PageHelper.MiraExpansionMethod;
import com.adaptivebiotech.cora.utils.PageHelper.MiraLab;
import com.adaptivebiotech.cora.utils.PageHelper.MiraPanel;
import com.adaptivebiotech.cora.utils.PageHelper.MiraType;
import com.adaptivebiotech.cora.utils.mira.mirasource.MiraSourceInfo;
import com.adaptivebiotech.cora.utils.mira.mirasource.SourceSpecimenInfo;

public class MiraSourceInfoProviderTestSuite {

    @Test
    public void testMiraSourceInfoProvider () {
        String miraSourceInfoFile = "MIRA/mira_ampl_slim_testInfo.json";

        MiraSourceInfoProvider miraSourceInfoProvider = new MiraSourceInfoProvider (miraSourceInfoFile);
        MiraSourceInfo miraSourceInfo = miraSourceInfoProvider.getMiraSourceInfoFromFile ();

        assertEquals (miraSourceInfo.getSourceMiraId (), "M-1345");
        assertEquals (miraSourceInfo.getSourceSpecimenId (), "SP-914830");
        assertEquals (miraSourceInfo.getMiraLab (), MiraLab.AntigenMapProduction);
        assertEquals (miraSourceInfo.getMiraType (), MiraType.MIRA);
        assertEquals (miraSourceInfo.getMiraPanel (), MiraPanel.Minor);
        assertEquals (miraSourceInfo.getExpansionMethod (), MiraExpansionMethod.AntiCD3);
        assertEquals (miraSourceInfo.getSourceSpecimenInfos ().length, 13);

        SourceSpecimenInfo specimenInfo = miraSourceInfo.getSourceSpecimenInfos ()[0];
        assertEquals (specimenInfo.getTsvPath (),
                      "https://adaptivetestcasedata.blob.core.windows.net/selenium/tsv/mira/templates/ampl_slim1/HWFJMBGXC_0_Adaptive-MIRA-AMPL_SP-914830_M-1345_A_positive.adap.txt.results.tsv.gz");
        assertEquals (specimenInfo.getPoolIndicator (), "A");
        assertEquals (specimenInfo.getCellCount ().intValue (), 2292);
        assertEquals (specimenInfo.getFlowcell (), "HWFJMBGXC");
        assertEquals (specimenInfo.getJobId (), "8a848a236fb16b8101708fa7229e68f8");
        assertEquals (specimenInfo.getWorkflowName (), "SP-914830_M-1345_A_positive");

    }
}
