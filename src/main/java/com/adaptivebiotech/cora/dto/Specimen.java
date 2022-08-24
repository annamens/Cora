/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.dto;

import static com.adaptivebiotech.test.utils.TestHelper.toStringOverride;
import static java.util.EnumSet.allOf;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import com.adaptivebiotech.cora.dto.Orders.DeliveryType;
import com.adaptivebiotech.test.utils.PageHelper.Compartment;
import com.adaptivebiotech.test.utils.PageHelper.SpecimenSource;
import com.adaptivebiotech.test.utils.PageHelper.SpecimenType;
import com.adaptivebiotech.test.utils.PageHelper.TestSkus;
import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public final class Specimen {

    public String             id;
    public UUID               key;
    public String             specimenNumber;
    public String             name;
    public Integer            subjectCode;
    public String             subjectId;
    public String             externalSubjectId;
    public String             sampleName;
    public DeliveryType       specimenDeliveryType;
    public SpecimenType       sampleType;
    @JsonAlias ("sourceType")
    public SpecimenSource     sampleSource;
    public LocalDateTime      approvedDate;
    // activationDate field can be status OR date time based on specimen is activated
    public Object             activationDate;
    public String             sampleTypeDisplayName;
    public SpecimenStatus     approvalStatus;
    public List <Sample>      samples;
    public Compartment        compartment;
    public Object             collectionDate;
    public Object             reconciliationDate;
    public LocalDateTime      retrievalDate;
    public SpecimenProperties properties;
    public Anticoagulant      anticoagulant;
    public ProjectProperties  projectProperties;

    @Override
    public String toString () {
        return toStringOverride (this);
    }

    public static final class ProjectProperties {
        public String  Var1;
        public String  Var2;
        public Integer Var3;

        @Override
        public String toString () {
            return toStringOverride (this);
        }
    }

    public static final class SpecimenProperties {

        // /cora/api/v1/test/scenarios/createPortalJob
        public String         ArrivalDate;

        // cora/api/v1/orders/{order_id}/entry
        public LocalDateTime  ApprovedDate;
        public SpecimenSource SourceType;
        public SpecimenStatus ApprovalStatus;
        public String         SampleTypeDisplayName;
        public String         Treatment;
        public Anticoagulant  Anticoagulant;

        public SpecimenProperties () {}

        public SpecimenProperties (String ArrivalDate) {
            this.ArrivalDate = ArrivalDate;
        }

        @Override
        public String toString () {
            return toStringOverride (this);
        }
    }

    public static class Sample {
        public String   name;
        public String   externalId;
        public TestSkus test;
        public String   tsvPath;

        @Override
        public String toString () {
            return toStringOverride (this);
        }
    }

    public enum Anticoagulant {
        EDTA, CfdRoche, Other, Streck
    }

    public enum SpecimenStatus {
        PASS ("Pass"), FAIL ("Fail");

        public String shipmentLabel;

        private SpecimenStatus (String shipmentLabel) {
            this.shipmentLabel = shipmentLabel;
        }

        public static SpecimenStatus getShipmentSpecimenStatus (String shipmentLabel) {
            return allOf (SpecimenStatus.class).parallelStream ().filter (st -> st.shipmentLabel.equals (shipmentLabel))
                                               .findAny ().orElse (null);
        }
    }

    public enum SpecimenActivation {
        PENDING ("Pending"),
        FAILED ("Failed"),
        FAILED_ACTIVATION ("Failed Activation");

        public String label;

        private SpecimenActivation (String label) {
            this.label = label;
        }

        public static SpecimenActivation getSpecimenActivationStatus (String label) {
            return allOf (SpecimenActivation.class).parallelStream ().filter (st -> st.label.equals (label)).findAny ()
                                                   .orElse (null);
        }
    }
}
