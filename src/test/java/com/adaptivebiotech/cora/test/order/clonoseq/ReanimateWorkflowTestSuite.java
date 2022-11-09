/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.order.clonoseq;

import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.NorthPipeline;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import java.util.Arrays;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Orders.OrderTest;
import com.adaptivebiotech.cora.dto.Workflow.Stage;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.debug.OrcaHistory;
import com.adaptivebiotech.cora.ui.order.OrderStatus;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.test.utils.PageHelper.StageName;
import com.adaptivebiotech.test.utils.PageHelper.StageStatus;
import com.adaptivebiotech.test.utils.PageHelper.StageSubstatus;

/**
 * @author sgarine
 *
 */
@Test (groups = { "regression", "irish-wolfhound" })
public class ReanimateWorkflowTestSuite extends CoraBaseBrowser {

    private Login       login       = new Login ();
    private OrdersList  ordersList  = new OrdersList ();
    private OrcaHistory orcaHistory = new OrcaHistory ();
    private OrderStatus orderStatus = new OrderStatus ();

    /**
     * Note:SR-T4299
     * 
     * @sdlc.requirements SR-12650:R2
     */
    public void verifyReanimateForOrderTests () {

        String reason = "Testing";
        String reanimateWorkflow = "Reanimate Workflow";
        String reanimateConfirmation = "Are you sure you want to reanimate this workflow?";

        login.doLogin ();
        ordersList.isCorrectPage ();
        OrderTest[] orderTests = coraApi.searchOrderTests (Arrays.asList ("category=Diagnostic",
                                                                          "status=Cancelled",
                                                                          "dueDate=all",
                                                                          "ascending=true",
                                                                          "stage=Clarity",
                                                                          "diagnosticOrderType=CDx"));
        testLog ("Searched for Cancelled orders");

        orcaHistory.gotoOrderDebug (orderTests[0].workflowId);
        assertTrue (orcaHistory.isWorkflowHeaderVisible ());
        assertEquals (orcaHistory.getWorkflowText (), reanimateWorkflow);
        orcaHistory.reanimateWorkflow (NorthPipeline, reason);
        String reanimatePopupText = orcaHistory.getPopUpConfirmationText ();
        orcaHistory.cancelPopUpAlert ();
        assertEquals (reanimatePopupText, reanimateConfirmation);
        orcaHistory.reanimateWorkflow (NorthPipeline, reason);
        assertTrue (orcaHistory.acceptPopUpAlert ());
        verifyWorkflowLatestStatus (orcaHistory.getWorkflowLatestStatus (),
                                    NorthPipeline,
                                    StageStatus.Ready,
                                    StageSubstatus.Re_animated,
                                    reason);
        testLog ("Reanimated workflow working as expected for cancelled orders");

        assertTrue (orcaHistory.isWorkflowHeaderVisible ());
        assertEquals (orcaHistory.getWorkflowText (), "Force Status Update (are you sure?)");
        testLog ("Force Status Update (are you sure?) text displayed for active order test as expected");
        orcaHistory.clickOrderTest ();
        orderStatus.completeWorkflow ("Completing the workflow");
        testLog ("completed order workflow");

        orcaHistory.gotoOrderDebug (orderTests[0].workflowId);
        assertEquals (orcaHistory.getWorkflowText (), reanimateWorkflow);
        orcaHistory.reanimateWorkflow (NorthPipeline, reason);
        reanimatePopupText = orcaHistory.getPopUpConfirmationText ();
        orcaHistory.cancelPopUpAlert ();
        assertEquals (reanimatePopupText, reanimateConfirmation);
        orcaHistory.reanimateWorkflow (NorthPipeline, reason);
        assertTrue (orcaHistory.acceptPopUpAlert ());
        verifyWorkflowLatestStatus (orcaHistory.getWorkflowLatestStatus (),
                                    NorthPipeline,
                                    StageStatus.Ready,
                                    StageSubstatus.Re_animated,
                                    reason);
        testLog ("Reanimate workflow working as expected for completed orders");

    }

    private void verifyWorkflowLatestStatus (Stage stage,
                                             StageName stageName,
                                             StageStatus stageStatus,
                                             StageSubstatus stageSubStatus,
                                             String message) {
        assertEquals (stage.stageName, stageName);
        assertEquals (stage.stageStatus, stageStatus);
        assertEquals (stage.stageSubstatus, stageSubStatus);
        assertEquals (stage.subStatusMessage, message);
    }
}
