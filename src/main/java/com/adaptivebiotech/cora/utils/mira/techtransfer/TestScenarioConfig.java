package com.adaptivebiotech.cora.utils.mira.techtransfer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class TestScenarioConfig {
    public Boolean retainS3Paths = true;

    public static JsonNode toJson(TestScenarioConfig testScenarioConfig) {
        ObjectNode scenarioConfig = JsonNodeFactory.instance.objectNode();
        scenarioConfig.put("retainS3Paths", testScenarioConfig.retainS3Paths);

        return scenarioConfig;
    }
}
