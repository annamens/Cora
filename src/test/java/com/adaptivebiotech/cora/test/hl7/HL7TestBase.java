/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.hl7;

import static com.adaptivebiotech.pipeline.utils.TestHelper.DxStatus.NEGATIVE;
import static com.adaptivebiotech.pipeline.utils.TestHelper.DxStatus.POSITIVE;
import java.util.ArrayList;
import com.adaptivebiotech.cora.dto.Workflow.WorkflowProperties;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.pipeline.dto.dx.ClassifierOutput;

public class HL7TestBase extends CoraBaseBrowser {

    protected WorkflowProperties sample_95268_SN_2205 () {
        WorkflowProperties workflowProperties = new WorkflowProperties ();
        workflowProperties.flowcell = "H752HBGXH";
        workflowProperties.workspaceName = "CLINICAL-CLINICAL";
        workflowProperties.sampleName = "95268-SN-2205";
        return workflowProperties;
    }

    protected ClassifierOutput negativeCovidResult () {
        ClassifierOutput dxResult = new ClassifierOutput ();
        dxResult.disease = "COVID19";
        dxResult.classifierVersion = "v1.0";
        dxResult.dxScore = -6.2313718717738125d;
        dxResult.posteriorProbability = 0.0019628913932077255d;
        dxResult.countEnhancedSeq = 15;
        dxResult.containerVersion = "dx-classifiers/covid-19:d23228f";
        dxResult.pipelineVersion = "v3.1-385-g1340003";
        dxResult.dxStatus = NEGATIVE;
        dxResult.configVersion = "dx.covid19.rev1";
        dxResult.uniqueProductiveTemplates = 251880;
        dxResult.qcFlags = new ArrayList <> ();
        return dxResult;
    }

    protected ClassifierOutput positiveLymeResult () {
        ClassifierOutput dxResult = new ClassifierOutput ();
        dxResult.disease = "LYME";
        dxResult.classifierVersion = "v2.0";
        dxResult.dxScore = 105.83867731819977d;
        dxResult.countEnhancedSeq = 71;
        dxResult.containerVersion = "dx-classifiers/lyme:8532b3f";
        dxResult.pipelineVersion = "v3.1-613-g1b391bc";
        dxResult.dxStatus = POSITIVE;
        dxResult.configVersion = "dx.lyme.rev2";
        dxResult.uniqueProductiveTemplates = 72905;
        dxResult.qcFlags = new ArrayList <> ();
        return dxResult;
    }
}
