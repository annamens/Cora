package com.adaptivebiotech.cora.dto;

import static com.adaptivebiotech.test.utils.TestHelper.mapper;
import java.time.LocalDateTime;
import java.util.List;
import com.adaptivebiotech.cora.dto.HttpResponse.Meta;
import com.adaptivebiotech.test.utils.PageHelper.Assay;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public final class AssayResponse {

    public Meta        meta;
    public List <Test> objects;

    public Test get (Assay assay) {
        return objects.parallelStream ().filter (o -> assay.test.equals (o.name)).findAny ().get ();
    }

    @Override
    public String toString () {
        try {
            return mapper.writeValueAsString (this);
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
    }

    public static final class Test {

        public String         id;
        public Integer        version;
        @JsonFormat (shape = JsonFormat.Shape.STRING)
        public LocalDateTime  created;
        @JsonFormat (shape = JsonFormat.Shape.STRING)
        public LocalDateTime  modified;
        public String         createdBy;
        public String         modifiedBy;
        public TestProperties properties;
        public String         name;
        public String         testFamily;
        public String         testVersion;
        public String         productCode;
        public String         receptorFamily;
        public String         dxType;
        public String         pipelineConfigOverride;
        public String         portalDisplayName;
        public Boolean        miraEnabled;
        public String         measure;
        public Boolean        clonoSeqV1;
        public Boolean        deprecated;
        public String         minProcessingLevel;
        public Boolean        allowMrdCoProcessing;
        public Boolean        lineClearance;
        public String         extractionFamily;
        public String         patientReport;
        public Boolean        checkExtractionBatch;
        public Boolean        checkCloneShareContamination;
        public String         moltagThreshold;
        public Boolean        isIUO;
        public Boolean        iuo;
        public String         locus;
        public String         regulationLevel;
        public Boolean        qc;
        public String         analyzerLocus;
        public String         analyzerResolution;
        public String         species;
        public String         resolution;
        public String         key;
    }

    public static final class TestProperties {

        public Boolean isIUO;
        public String  locus;
        public String  measure;
        public String  species;
        public String  workflow;
        public String  resolution;
        public Boolean lineClearance;
        public String  patientReport;
        public Boolean autoCalibrating;
        public String  depthEquivalent;
        public String  moltagThreshold;
        public String  regulationLevel;
        public String  extractionFamily;
        public String  portalDisplayName;
        public String  minProcessingLevel;
        public Boolean isClonoSEQBlockable;
        public Boolean allowMrdCoProcessing;
        public Boolean checkExtractionBatch;
        public Boolean clinicalQcAutoAccept;
        public Boolean checkCloneShareContamination;
        public Boolean disableMrdAwaitClonesTimeout;
    }
}
