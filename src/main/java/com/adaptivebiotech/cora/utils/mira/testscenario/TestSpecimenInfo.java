package com.adaptivebiotech.cora.utils.mira.testscenario;

import java.time.format.DateTimeFormatter;
import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class TestSpecimenInfo {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm");

    public String Name;
    public String ExternalSubjectId;
    public String SampleType;
    public String SampleSource;
    public String Compartment;
    public String CollectionDate;
    public JsonNode Properties;
    public JsonNode ProjectProperties;
    public List<TestSampleInfo> Samples;

    protected static JsonNode toJson(TestSpecimenInfo specimenInfo) {
        ObjectNode specimen = JsonNodeFactory.instance.objectNode();
        specimen.put("name", specimenInfo.Name);
        specimen.put("externalSubjectId", specimenInfo.ExternalSubjectId);
        specimen.put("sampleType", specimenInfo.SampleType);
        specimen.put("sampleSource", specimenInfo.SampleSource);
        specimen.put("compartment", specimenInfo.Compartment);
        specimen.put("collectionDate", specimenInfo.CollectionDate);
        specimen.set("properties", specimenInfo.Properties);
        specimen.set("projectProperties", specimenInfo.ProjectProperties);

        ArrayNode samples = JsonNodeFactory.instance.arrayNode();
        for (TestSampleInfo sample : specimenInfo.Samples) {
            samples.add(TestSampleInfo.toJson(sample));
        }

        specimen.set("samples", samples);

        return specimen;
    }
}
