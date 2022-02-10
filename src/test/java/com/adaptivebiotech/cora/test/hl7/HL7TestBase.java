package com.adaptivebiotech.cora.test.hl7;

import static com.adaptivebiotech.pipeline.utils.TestHelper.DxStatus.NEGATIVE;
import java.util.ArrayList;
import com.adaptivebiotech.cora.dto.AssayResponse.CoraTest;
import com.adaptivebiotech.cora.dto.Orders.Assay;
import com.adaptivebiotech.cora.dto.Workflow;
import com.adaptivebiotech.cora.dto.Workflow.WorkflowProperties;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.pipeline.dto.dx.ClassifierOutput;

public class HL7TestBase extends CoraBaseBrowser {

    protected CoraTest genCDxTest (Assay assay, String tsvPath) {
        CoraTest test = coraApi.getCDxTest (assay);
        test.workflowProperties = new Workflow.WorkflowProperties ();
        test.workflowProperties.disableHiFreqSave = true;
        test.workflowProperties.disableHiFreqSharing = true;
        test.workflowProperties.notifyGateway = true;
        test.workflowProperties.tsvOverridePath = tsvPath;
        return test;
    }

    protected CoraTest genTcrTest (Assay assay, String flowcell, String tsvPath) {
        CoraTest test = coraApi.getCDxTest (assay);
        test.workflowProperties = new Workflow.WorkflowProperties ();
        test.workflowProperties.notifyGateway = true;
        test.flowcell = flowcell;
        test.pipelineConfigOverride = "classic.calib";
        test.tsvPath = tsvPath;
        return test;
    }

    protected WorkflowProperties sample_95268_SN_2205 () {
        WorkflowProperties workflowProperties = new WorkflowProperties ();
        workflowProperties.flowcell = "H752HBGXH";
        workflowProperties.workspaceName = "CLINICAL-CLINICAL";
        workflowProperties.sampleName = "95268-SN-2205";
        return workflowProperties;
    }

    protected ClassifierOutput negativeDxResult () {
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
}
