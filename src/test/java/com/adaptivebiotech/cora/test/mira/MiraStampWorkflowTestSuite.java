package com.adaptivebiotech.cora.test.mira;

import static org.testng.Assert.assertTrue;
import java.util.HashMap;
import java.util.Map;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.test.CoraEnvironment;
import com.adaptivebiotech.cora.ui.CoraPage;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.mira.Mira;
import com.adaptivebiotech.cora.ui.mira.MirasList;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.workflow.History;
import com.adaptivebiotech.cora.utils.PageHelper.MiraLab;
import com.adaptivebiotech.cora.utils.PageHelper.MiraQCStatus;
import com.adaptivebiotech.cora.utils.PageHelper.MiraStage;
import com.adaptivebiotech.cora.utils.PageHelper.MiraStatus;
import com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty;

public class MiraStampWorkflowTestSuite extends CoraBaseBrowser {

    /*
     * 
     * for M-457 (prod)
     * unfortunately we have to get this data manually for each workflow we want to copy from prod
     * 
     * 
     * workflow id flowcell job id
     * 
     * SP-687684_M-457_A_positive HWFJMBGXC 8a848a236fb16b8101708fa7229e68f8
     * SP-687684_M-457_B_positive HWFJMBGXC 8a848a236fb16b8101708fa7229e68f8
     * SP-687684_M-457_C_positive HWFJMBGXC 8a848a236fb16b8101708fa7229e68f8
     * SP-687684_M-457_D_positive HW5FFBGXC 8a848a236fb16b8101708fa7166468e9
     * SP-687684_M-457_E_positive HW5FFBGXC 8a848a236fb16b8101708fa7166468e9
     * SP-687684_M-457_F_positive HW5FFBGXC 8a848a236fb16b8101708fa7166468e9
     * SP-687684_M-457_H_positive HW5FFBGXC 8a848a236fb16b8101708fa7166468e9
     * SP-687684_M-457_I_positive HW5FFBGXC 8a848a236fb16b8101708fa7166468e9
     * SP-687684_M-457_J_positive HW5FFBGXC 8a848a236fb16b8101708fa7166468e9
     * SP-687684_M-457_K_positive HW5FFBGXC 8a848a236fb16b8101708fa7166468e9
     * SP-687684_M-457_nopeptide_pos HW5FFBGXC 8a848a236fb16b8101708fa7166468e9
     * SP-687684_M-457_G_positive HW5FFBGXC 8a848a236fb16b8101708fa7166468e9
     * SP-687684_M-457_unsorted_expanded HWCGNBGXC 8a848a236fb16b8101708fa7179668ec
     */

    Map <String, String> flowcellToJobId          = new HashMap <> ();
    Map <String, String> workflowSuffixToFlowcell = new HashMap <> ();

    @Test
    public void stampWorkflowTest () {

        // for M-457
        // TODO - need to read this from a file

        flowcellToJobId.put ("HWFJMBGXC", "8a848a236fb16b8101708fa7229e68f8");
        flowcellToJobId.put ("HW5FFBGXC", "8a848a236fb16b8101708fa7166468e9");
        flowcellToJobId.put ("HWCGNBGXC", "8a848a236fb16b8101708fa7179668ec");

        workflowSuffixToFlowcell.put ("A_positive", "HWFJMBGXC");
        workflowSuffixToFlowcell.put ("B_positive", "HWFJMBGXC");
        workflowSuffixToFlowcell.put ("C_positive", "HWFJMBGXC");
        workflowSuffixToFlowcell.put ("D_positive", "HW5FFBGXC");
        workflowSuffixToFlowcell.put ("E_positive", "HW5FFBGXC");
        workflowSuffixToFlowcell.put ("F_positive", "HW5FFBGXC");
        workflowSuffixToFlowcell.put ("G_positive", "HW5FFBGXC");
        workflowSuffixToFlowcell.put ("H_positive", "HW5FFBGXC");
        workflowSuffixToFlowcell.put ("I_positive", "HW5FFBGXC");
        workflowSuffixToFlowcell.put ("J_positive", "HW5FFBGXC");
        workflowSuffixToFlowcell.put ("K_positive", "HW5FFBGXC");
        workflowSuffixToFlowcell.put ("nopeptide_pos", "HW5FFBGXC");
        workflowSuffixToFlowcell.put ("unsorted_expanded", "HWCGNBGXC"); // this is the unsorted guy

        String miraPipelineResultOverride = "[{\n" + "\"forwardPcrPrimer\": \"TCRB_1rxn-P43-M164-A15SW\",\n" + "\"reversePcrPrimer\": \"TCRB_1rxn-P43-M164-A15SW\",\n" + "\"sample.flowcell.fcid\": \"190908_NB501176_0706_AHV7HVBGXB\",\n" + "\"sample.flowcell.job.archiveResultsPath\": \"s3://pipeline-cora-test-archive:us-west-2/190908_NB501176_0706_AHV7HVBGXB/v3.1/20190929_0306\",\n" + "\"sample.flowcell.runDate\": \"2019-09-08T07:00:00Z\",\n" + "\"sequencingRead1Primer\": \"\",\n" + "\"sequencingRead2Primer\": \"\"\n" + "}]";

        MiraLab miraLab = MiraLab.AntigenMapProduction;
        String miraId = "M-1362";

        loginToCora ();
        gotoMiraByLabAndId (miraId, miraLab);

        Mira mira = new Mira ();
        mira.isCorrectPage (miraId);

        String specimenId = mira.getSpecimenId ();

        mira.clickTestTab (true);
        String fmtString = "%s_%s_%s";

        for (String workflowSuffix : workflowSuffixToFlowcell.keySet ()) {

            String workflowName = String.format (fmtString, specimenId, miraId, workflowSuffix);

            String flowcellId = workflowSuffixToFlowcell.get (workflowSuffix);
            String jobId = flowcellToJobId.get (flowcellId);

            History history = new History ();
            history.gotoOrderDebug (workflowName);
            history.isCorrectPage ();
            Map <WorkflowProperty, String> properties = new HashMap <> ();
            properties.put (WorkflowProperty.lastFinishedPipelineJobId, jobId);
            properties.put (WorkflowProperty.lastFlowcellId, flowcellId);
            history.setWorkflowProperties (properties);

        }

        History history = new History ();
        history.gotoOrderDebug (miraId);
        history.isCorrectPage ();
        history.setWorkflowProperty (WorkflowProperty.miraPipelineResultOverride,
                                     miraPipelineResultOverride.replaceAll ("\n", ""));

        gotoMiraByLabAndId (miraId, miraLab);

        mira.setQCStatus (MiraQCStatus.ACCEPTED);

        mira.clickStatusTab ();
        assertTrue (mira.waitForStage (MiraStage.Publishing));
        assertTrue (mira.waitForStatus (MiraStatus.Finished, 30, 60000));
    }

    private void loginToCora () {
        Login login = new Login ();
        login.doLogin ();
        OrdersList ordersList = new OrdersList ();
        ordersList.isCorrectPage ();
    }

    private void gotoMiraByLabAndId (String miraId, MiraLab miraLab) {
        CoraPage coraPage = new CoraPage ();
        coraPage.navigateTo (CoraEnvironment.coraTestUrl);
        OrdersList ordersList = new OrdersList ();
        ordersList.isCorrectPage ();
        ordersList.clickMiras ();
        
        MirasList mirasList = new MirasList ();
        mirasList.isCorrectPage ();
        mirasList.searchAndClickMira (miraId, miraLab);
    }

}
