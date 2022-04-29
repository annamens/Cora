/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.dto.report;

import static com.adaptivebiotech.test.utils.TestHelper.equalsOverride;
import static com.adaptivebiotech.test.utils.TestHelper.toStringOverride;
import java.util.List;
import com.adaptivebiotech.picasso.dto.ReportRender.SampleInfo;
import com.adaptivebiotech.pipeline.utils.TestHelper.Locus;
import com.adaptivebiotech.test.utils.PageHelper.ReportType;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Zachary Robin <a href="mailto:zrobin@adaptivebiotech.com">zrobin@adaptivebiotech.com</a>
 */
public class AnalysisConfig {

    @JsonProperty ("class")
    public String                   klass   = "com.adaptive.analysis.AnalysisConfigDto";
    public int                      version = 1;
    public String                   reportId;
    public String                   reportSampleOrderTestId;
    public ReportType               reportType;
    public String                   compartment;
    public Locus                    testLocus;
    public String                   sampleName;
    public Boolean                  workflowSensitivityBins;
    public List <KnownId>           knownIds;
    public List <SampleInfo>        patientSamples;
    public List <PatientCloneTrack> patientCloneTracks;

    @Override
    public String toString () {
        return toStringOverride (this);
    }

    @Override
    public boolean equals (Object o) {
        return equalsOverride (this, (AnalysisConfig) o);
    }

    public static class KnownId {

        @JsonProperty ("class")
        public String  klass   = "com.adaptive.mrdengine.IdCloneDto";
        public int     version = 1;
        public String  id;
        public String  orderTestId;
        public Locus   subLocus;
        public String  sequence;
        public double  count;
        public double  normalizedCount;
        public String  countMeasure;
        public double  sampleTemplates;
        public double  sampleAmount;
        public int     cdr3Index;
        public String  cloneVersion;
        public double  uniquenessProbability;
        public double  ndnMutationWeight;
        public double  maxAdjustedMutations;
        public double  diseaseLoadMultiplier;
        public String  cellularSensitivityBin;
        public String  cellfreeSensitivityBin;
        public Boolean trackingOnly;
        public Boolean highSharingSample;
        public String  cloneSource;
        public double  createdMillis;

        @Override
        public String toString () {
            return toStringOverride (this);
        }

        @Override
        public boolean equals (Object o) {
            return equalsOverride (this, (KnownId) o);
        }
    }

    public static class PatientCloneTrack {

        @JsonProperty ("class")
        public String klass   = "com.adaptive.clonoseqreport.dtos.SampleInfoDto";
        public int    version = 1;
        public String patientId;
        public String idCloneId;
        public String nucleotide;
        public int    designation;
        public int    color;
        public int    symbol;
        public int    lineType;

        @Override
        public String toString () {
            return toStringOverride (this);
        }

        @Override
        public boolean equals (Object o) {
            return equalsOverride (this, (PatientCloneTrack) o);
        }
    }
}
