package com.adaptivebiotech.cora.utils.mira.techtransfer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class TestScenarioInfo {
    private TestTechTransferInfo techTransferInfo;
    private TestScenarioProjectInfo projectInfo;
    private TestFastForwardInfo fastForwardInfo;
    private TestScenarioConfig scenarioConfig;

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
    
    public TestScenarioInfo (TestTechTransferInfo techTransferInfo,
                             TestScenarioProjectInfo projectInfo,
                             TestFastForwardInfo fastForwardInfo,
                             TestScenarioConfig scenarioConfig) {
        this.techTransferInfo = techTransferInfo;
        this.projectInfo = projectInfo;
        this.fastForwardInfo = fastForwardInfo;
        this.scenarioConfig = scenarioConfig;
    }
}
