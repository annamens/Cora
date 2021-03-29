package com.adaptivebiotech.cora.dto;

import static com.adaptivebiotech.test.utils.TestHelper.mapper;
import java.util.List;
import com.adaptivebiotech.test.utils.PageHelper.Anticoagulant;
import com.adaptivebiotech.test.utils.PageHelper.SpecimenSource;
import com.adaptivebiotech.test.utils.PageHelper.SpecimenType;
import com.adaptivebiotech.test.utils.PageHelper.TestSkus;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public final class Specimen {

    public String             id;
    public String             key;
    public String             specimenNumber;
    public String             name;
    public Integer            subjectCode;
    public String             subjectId;
    public String             externalSubjectId;
    public String             sampleName;
    public SpecimenType       sampleType;
    public SpecimenSource     sampleSource;
    public SpecimenSource     sourceType;
    public String             arrivalDate;
    public String             sampleTypeDisplayName;
    public String             approvalStatus;
    public List <Sample>      samples;
    public String             compartment;
    public Object             collectionDate;
    public Object             reconciliationDate;
    public SpecimenProperties properties;
    public Anticoagulant      anticoagulant;
    public ProjectProperties  projectProperties;

    @Override
    public String toString () {
        try {
            return mapper.writeValueAsString (this);
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
    }

    public static final class ProjectProperties {
        public String  Var1;
        public String  Var2;
        public Integer Var3;

        @Override
        public String toString () {
            try {
                return mapper.writeValueAsString (this);
            } catch (Exception e) {
                throw new RuntimeException (e);
            }
        }
    }

    public static final class SpecimenProperties {

        public String         ArrivalDate;
        public SpecimenSource SourceType;
        public String         ApprovalStatus;
        public String         SampleTypeDisplayName;
        public String         Treatment;

        public SpecimenProperties () {}

        public SpecimenProperties (String ArrivalDate) {
            this.ArrivalDate = ArrivalDate;
        }

        @Override
        public String toString () {
            try {
                return mapper.writeValueAsString (this);
            } catch (Exception e) {
                throw new RuntimeException (e);
            }
        }
    }

    public static class Sample {
        public String   name;
        public String   externalId;
        public TestSkus test;
        public String   tsvPath;

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
