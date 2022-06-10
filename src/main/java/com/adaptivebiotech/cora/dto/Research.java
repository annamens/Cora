/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.dto;

import static com.adaptivebiotech.test.utils.TestHelper.mapper;
import java.util.List;
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
        try {
            return mapper.writeValueAsString (this);
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
    }

    public static final class ScenarioConfig {
        public Boolean retainS3Paths;

        @Override
        public String toString () {
            try {
                return mapper.writeValueAsString (this);
            } catch (Exception e) {
                throw new RuntimeException (e);
            }
        }
    }

    public static final class TechTransfer {

        public String          workspace;
        public String          flowcellId;
        public List <Specimen> specimens;

        @Override
        public String toString () {
            try {
                return mapper.writeValueAsString (this);
            } catch (Exception e) {
                throw new RuntimeException (e);
            }
        }
    }

    public static final class Project {

        public String  id;
        public String  accountId;
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
            try {
                return mapper.writeValueAsString (this);
            } catch (Exception e) {
                throw new RuntimeException (e);
            }
        }
    }
}
