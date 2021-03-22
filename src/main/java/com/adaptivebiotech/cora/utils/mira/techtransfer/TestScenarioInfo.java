package com.adaptivebiotech.cora.utils.mira.techtransfer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class TestScenarioInfo {
    public TestTechTransferInfo techTransferInfo;
    public TestScenarioProjectInfo projectInfo;
    public TestFastForwardInfo fastForwardInfo;
    public TestScenarioConfig scenarioConfig;

    public static JsonNode toJson(TestScenarioInfo scenarioInfo) {
        ObjectNode scenario = JsonNodeFactory.instance.objectNode();

        if (scenarioInfo.techTransferInfo != null)
            scenario.set("techTransfer", TestTechTransferInfo.toJson(scenarioInfo.techTransferInfo));

        if (scenarioInfo.projectInfo != null)
            scenario.set("project", TestScenarioProjectInfo.toJson(scenarioInfo.projectInfo));

        if (scenarioInfo.fastForwardInfo != null)
            scenario.set("fastForwardStatus", TestFastForwardInfo.toJson(scenarioInfo.fastForwardInfo));

        if (scenarioInfo.scenarioConfig != null)
            scenario.set("scenarioConfig", TestScenarioConfig.toJson(scenarioInfo.scenarioConfig));

        return scenario;
    }
}
