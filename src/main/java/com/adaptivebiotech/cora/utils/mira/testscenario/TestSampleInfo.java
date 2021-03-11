package com.adaptivebiotech.cora.utils.mira.testscenario;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class TestSampleInfo {
    public String name;
    public String externalId;
    public String test;
    public String tsvPath;

    protected static JsonNode toJson(TestSampleInfo sampleInfo) {
        ObjectNode sample = JsonNodeFactory.instance.objectNode();
        sample.put("name", sampleInfo.name);
        sample.put("externalId", sampleInfo.externalId);
        sample.put("test", sampleInfo.test);
        sample.put("tsvPath", sampleInfo.tsvPath);

        return sample;
    }
}