package com.adaptivebiotech.cora.utils.mira.techtransfer;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class TestSpecimenInfo {

    private String name;
    private String externalSubjectId;
    private String sampleType;
    private String sampleSource;
    private String compartment;
    private String collectionDate;
    private JsonNode properties;
    private JsonNode projectProperties;
    private List<TestSampleInfo> samples;

    protected static JsonNode toJson(TestSpecimenInfo specimenInfo) {
        ObjectNode specimen = JsonNodeFactory.instance.objectNode();
        specimen.put("name", specimenInfo.name);
        specimen.put("externalSubjectId", specimenInfo.externalSubjectId);
        specimen.put("sampleType", specimenInfo.sampleType);
        specimen.put("sampleSource", specimenInfo.sampleSource);
        specimen.put("compartment", specimenInfo.compartment);
        specimen.put("collectionDate", specimenInfo.collectionDate);
        specimen.set("properties", specimenInfo.properties);
        specimen.set("projectProperties", specimenInfo.projectProperties);

        ArrayNode samples = JsonNodeFactory.instance.arrayNode();
        for (TestSampleInfo sample : specimenInfo.samples) {
            samples.add(TestSampleInfo.toJson(sample));
        }

        specimen.set("samples", samples);

        return specimen;
    }
    
    public TestSpecimenInfo (String name, 
                             String externalSubjectId,
                             String sampleType,
                             String sampleSource,
                             String compartment,
                             String collectionDate,
                             JsonNode properties,
                             JsonNode projectProperties) {
        this.name = name;
        this.externalSubjectId = externalSubjectId;
        this.sampleType = sampleType;
        this.sampleSource = sampleSource;
        this.compartment = compartment;
        this.collectionDate = collectionDate;
        this.properties = properties;
        this.projectProperties = projectProperties;
        this.samples = new ArrayList<> (1);
    }
    
    public void addSample (TestSampleInfo testSampleInfo) {
        samples.add (testSampleInfo);
    }
    
}
