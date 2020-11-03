package com.adaptivebiotech.cora.dto;

import static com.adaptivebiotech.test.utils.TestHelper.mapper;
import com.adaptivebiotech.test.utils.PageHelper.StageName;
import com.adaptivebiotech.test.utils.PageHelper.StageStatus;
import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public final class Workflow {

    public String  sampleName;
    public String  workflowId;
    public Integer sampleCount;
    public String  flowcell;
    public String  workspaceName;
    public String  lastAcceptedTsvPath;
    public String  lastFlowcellId;

    public Workflow () {}

    public Workflow (String flowcell, String workspaceName, String sampleName) {
        this.flowcell = flowcell;
        this.workspaceName = workspaceName;
        this.sampleName = sampleName;
    }

    @Override
    public String toString () {
        try {
            return mapper.writeValueAsString (this);
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
    }

    public static final class Stage {

        public String      id;
        public String      workflowId;
        @JsonAlias ("stage")
        public StageName   stageName;
        public StageStatus stageStatus;
        public String      subStatusCode;
        public String      subStatusMessage;
        public String      drilldownUrl;
        public String      timestamp;
        public String      actor;
        public Boolean     hasFinished;
        public Boolean     isCurrent;
        public Boolean     hasProblems;
        public Integer     requeueCount;
        public Boolean     current;

        @Override
        public String toString () {
            try {
                return mapper.writeValueAsString (this);
            } catch (Exception e) {
                throw new RuntimeException (e);
            }
        }
    }
}
