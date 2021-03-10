package com.adaptivebiotech.cora.utils.mira.testscenario;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class TestScenarioInfo {
    public TestTechTransferInfo TechTransferInfo;
    public TestScenarioProjectInfo ProjectInfo;
    public TestFastForwardInfo FastForwardInfo;
    public TestScenarioConfig ScenarioConfig;

    public static JsonNode toJson(TestScenarioInfo scenarioInfo) {
        ObjectNode scenario = JsonNodeFactory.instance.objectNode();

        if (scenarioInfo.TechTransferInfo != null)
            scenario.set("techTransfer", TestTechTransferInfo.toJson(scenarioInfo.TechTransferInfo));

        if (scenarioInfo.ProjectInfo != null)
            scenario.set("project", TestScenarioProjectInfo.toJson(scenarioInfo.ProjectInfo));

        if (scenarioInfo.FastForwardInfo != null)
            scenario.set("fastForwardStatus", TestFastForwardInfo.toJson(scenarioInfo.FastForwardInfo));

        if (scenarioInfo.ScenarioConfig != null)
            scenario.set("scenarioConfig", TestScenarioConfig.toJson(scenarioInfo.ScenarioConfig));

        return scenario;
    }
}
