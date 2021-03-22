package com.adaptivebiotech.cora.utils.mira.techtransfer;

import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class TestSpecimenInfo {

    public String name;
    public String externalSubjectId;
    public String sampleType;
    public String sampleSource;
    public String compartment;
    public String collectionDate;
    public JsonNode properties;
    public JsonNode projectProperties;
    public List<TestSampleInfo> samples;

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
}
