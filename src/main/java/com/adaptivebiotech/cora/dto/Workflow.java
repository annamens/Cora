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

    public String sampleName;
    public String workflowId;
    public int    sampleCount;

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
        public boolean     hasFinished;
        public boolean     isCurrent;
        public boolean     hasProblems;
        public int         requeueCount;
        public boolean     current;

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
