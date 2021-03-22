package com.adaptivebiotech.cora.utils.mira.techtransfer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class TestSampleInfo {
    private String name;
    private String externalId;
    private String test;
    private String tsvPath;

    protected static JsonNode toJson(TestSampleInfo sampleInfo) {
        ObjectNode sample = JsonNodeFactory.instance.objectNode();
        sample.put("name", sampleInfo.name);
        sample.put("externalId", sampleInfo.externalId);
        sample.put("test", sampleInfo.test);
        sample.put("tsvPath", sampleInfo.tsvPath);

        return sample;
    }
    
    public TestSampleInfo (String name, String externalId, String poolIndicator, String tsvPath) {
        this.name = name;
        this.externalId = externalId;
        this.test = poolIndicator.equals ("US") ? "MIRAUNSORTED" : "MIRASORTED";
        this.tsvPath = tsvPath;
    }
}