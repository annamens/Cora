package com.adaptivebiotech.cora.utils.mira.testscenario;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class TestSampleInfo {
    public String Name;
    public String ExternalId;
    public String Test;
    public String TsvPath;

    protected static JsonNode toJson(TestSampleInfo sampleInfo) {
        ObjectNode sample = JsonNodeFactory.instance.objectNode();
        sample.put("name", sampleInfo.Name);
        sample.put("externalId", sampleInfo.ExternalId);
        sample.put("test", sampleInfo.Test);
        sample.put("tsvPath", sampleInfo.TsvPath);

        return sample;
    }
}