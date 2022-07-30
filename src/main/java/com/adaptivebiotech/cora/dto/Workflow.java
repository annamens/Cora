/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.dto;

import static com.adaptivebiotech.test.utils.TestHelper.toStringOverride;
import java.util.UUID;
import com.adaptivebiotech.test.utils.PageHelper.StageName;
import com.adaptivebiotech.test.utils.PageHelper.StageStatus;
import com.adaptivebiotech.test.utils.PageHelper.StageSubstatus;
import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public final class Workflow {

    public UUID               id;
    public String             name;
    public WorkflowProperties workflowProperties;

    @Override
    public String toString () {
        return toStringOverride (this);
    }

    public static final class WorkflowProperties {

        public String  flowcell;
        public String  sampleName;
        public String  workspaceName;
        public String  lastAcceptedTsvPath;
        public String  lastFlowcellId;
        public String  tsvOverridePath;
        public String  lastFinishedPipelineJobId;
        public Boolean disableHiFreqSave;
        public Boolean disableHiFreqSharing;
        public Boolean skipNorthQCToContam;
        public Boolean ighvAnalysisEnabled;
        public Boolean ighvReportEnabled;
        public Boolean notifyGateway;
        public String  shmDataSourcePath;
        public String  country;
        public String  reportNotes;
    }

    public static final class Stage {

        public UUID           id;
        public UUID           workflowId;
        @JsonAlias ("stage")
        public StageName      stageName;
        public StageStatus    stageStatus;
        public StageSubstatus stageSubstatus;
        public String         subStatusCode;
        public String         subStatusMessage;
        public String         drilldownUrl;
        public String         timestamp;
        public String         actor;
        public Boolean        hasFinished;
        public Boolean        isCurrent;
        public Boolean        hasProblems;
        public Integer        requeueCount;
        public Boolean        current;

        @Override
        public String toString () {
            return toStringOverride (this);
        }
    }
}
