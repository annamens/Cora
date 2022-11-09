/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test;

import static com.adaptivebiotech.cora.test.CoraEnvironment.coraDbInfo;
import static com.adaptivebiotech.cora.test.CoraEnvironment.initialization;
import static com.adaptivebiotech.cora.test.CoraEnvironment.jumpbox;
import static com.adaptivebiotech.cora.test.CoraEnvironment.portalCliaTestUrl;
import static com.adaptivebiotech.cora.test.CoraEnvironment.portalIvdTestUrl;
import static com.adaptivebiotech.pipeline.test.PipelineEnvironment.isIVD;
import static com.adaptivebiotech.pipeline.test.PipelineEnvironment.portalTestUrl;
import static com.adaptivebiotech.test.BaseEnvironment.gitcommitId;
import static com.adaptivebiotech.test.BaseEnvironment.version;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.seleniumfy.test.utils.Logging.info;
import static java.lang.String.format;
import static java.lang.String.join;
import static org.testng.Assert.assertTrue;
import static org.testng.ITestResult.SKIP;
import static org.testng.Reporter.getCurrentTestResult;
import java.lang.reflect.Method;
import org.slf4j.MDC;
import org.testng.SkipException;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import com.adaptivebiotech.cora.api.CoraApi;
import com.adaptivebiotech.cora.api.CoraDebugApi;
import com.adaptivebiotech.cora.db.CoraDb;
import com.adaptivebiotech.cora.dto.AssayResponse.CoraTest;
import com.adaptivebiotech.cora.dto.FeatureFlags;
import com.adaptivebiotech.cora.dto.Orders.Assay;
import com.adaptivebiotech.cora.dto.Workflow.WorkflowProperties;
import com.adaptivebiotech.test.TestBase;

public class CoraBaseBrowser extends TestBase {

    protected final String        azTsvPath              = "https://adaptivetestcasedata.blob.core.windows.net/selenium/tsv/scenarios";
    protected final String        azE2EPath              = "https://adaptivetestcasedata.blob.core.windows.net/selenium/tsv/e2e";
    protected final String        azPipelineClia         = "https://adaptiveruopipeline.blob.core.windows.net/pipeline-results";
    protected final String        azPipelineFda          = "https://adaptiveivdpipeline.blob.core.windows.net/pipeline-results";
    protected final String        SEAaccount             = "SEA_QA";
    protected final String        containerNumberPattern = "CO-\\d{7}";
    protected static CoraApi      coraApi;
    protected static CoraDebugApi coraDebugApi;
    protected static CoraDb       coraDb;
    protected static FeatureFlags featureFlags;

    static {
        initialization ();
        testLog (format ("Current branch: %s - %s", version, gitcommitId));

        coraApi = new CoraApi ();
        coraApi.getAuthToken ();
        coraDebugApi = new CoraDebugApi ();
        coraDb = new CoraDb (coraDbInfo, jumpbox);
        coraDb.openConnection ();
    }

    @BeforeSuite (alwaysRun = true)
    public final void baseBeforeSuite () {
        MDC.put (jobId, this.getClass ().getName ());
        info (format ("running: %s", getClass ()));
    }

    @AfterSuite (alwaysRun = true)
    public final void baseAfterSuite () {
        coraDb.closeConnection ();
    }

    @BeforeMethod (alwaysRun = true)
    public final void baseBeforeMethod (Method method) {
        String testName = join (".", getClass ().getName (), method.getName ());
        MDC.put (jobId, testName);
        coraApi.addTokenAndUsername ();
        featureFlags = coraApi.getFeatureFlags ();
        info (format ("running: %s()", testName));
    }

    protected void prepPipelineApi (boolean isClia) {
        if (isClia) {
            isIVD = false;
            portalTestUrl = portalCliaTestUrl;
        } else {
            isIVD = true;
            portalTestUrl = portalIvdTestUrl;
        }
    }

    protected String artifacts (String... paths) {
        return join ("/", "target/logs", join ("/", paths));
    }

    protected CoraTest genCDxTest (Assay assay, String tsvPath) {
        CoraTest test = coraApi.getCDxTest (assay);
        test.workflowProperties = new WorkflowProperties ();
        test.workflowProperties.disableHiFreqSave = true;
        test.workflowProperties.disableHiFreqSharing = true;
        test.workflowProperties.notifyGateway = true;
        test.workflowProperties.tsvOverridePath = tsvPath;
        return test;
    }

    // don't set workflowProperties.tsvOverridePath, otherwise it will skip IMPORTING substatus
    protected CoraTest genTcrTest (Assay assay, String flowcell, String tsvPath) {
        CoraTest test = coraApi.getCDxTest (assay);
        test.workflowProperties = new WorkflowProperties ();
        test.workflowProperties.disableHiFreqSave = true;
        test.workflowProperties.disableHiFreqSharing = true;
        test.workflowProperties.notifyGateway = true;
        test.flowcell = flowcell;
        test.pipelineConfigOverride = "classic.calib";
        test.tsvPath = tsvPath;
        return test;
    }

    protected CoraTest getTDxTest (Assay assay) {
        CoraTest test = coraApi.getTDxTest (assay);
        test.workflowProperties = new WorkflowProperties ();
        test.workflowProperties.disableHiFreqSave = true;
        test.workflowProperties.disableHiFreqSharing = true;
        return test;
    }

    protected void skipTestIfFeatureFlagOff (Boolean featureFlag) {
        if (!featureFlag) {
            getCurrentTestResult ().setStatus (SKIP);
            throw new SkipException ("Required feature Flag is false, expected true");
        }
    }

    protected void skipTestIfFeatureFlagOn (Boolean featureFlag) {
        if (featureFlag) {
            getCurrentTestResult ().setStatus (SKIP);
            throw new SkipException ("Required feature Flag is true, expected false");
        }
    }

    protected void validatePdfContent (String fileContent, String stringToValidate) {
        fileContent = fileContent.replace ("\n", " ");
        info ("Validate: " + stringToValidate + ", in: " + fileContent);
        assertTrue (fileContent.contains (stringToValidate));
    }
}
