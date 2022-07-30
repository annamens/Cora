/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.dto;

import static com.adaptivebiotech.test.utils.TestHelper.toStringOverride;
import java.util.List;
import java.util.UUID;
import com.adaptivebiotech.cora.dto.Workflow.Stage;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public final class Research {

    public TechTransfer   techTransfer;
    public Project        project;
    public Stage          fastForwardStatus;
    public ScenarioConfig scenarioConfig;

    public Research () {}

    public Research (TechTransfer techTransfer) {
        this.techTransfer = techTransfer;
    }

    @Override
    public String toString () {
        return toStringOverride (this);
    }

    public static final class ScenarioConfig {
        public Boolean retainS3Paths;

        @Override
        public String toString () {
            return toStringOverride (this);
        }
    }

    public static final class TechTransfer {

        public String          workspace;
        public String          flowcellId;
        public List <Specimen> specimens;

        @Override
        public String toString () {
            return toStringOverride (this);
        }
    }

    public static final class Project {

        public UUID    id;
        public UUID    accountId;
        public String  name;
        public String  stage;
        public String  customerTrialNumber;
        public String  codenames;
        public String  recordType;
        public String  accessioningSpecialRequests;
        public String  croContactDetails;
        public Boolean transitionedToV2;
        public String  handlingNotes;

        @Override
        public String toString () {
            return toStringOverride (this);
        }
    }
}
