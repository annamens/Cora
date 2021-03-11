package com.adaptivebiotech.cora.utils.mira.testscenario;

import java.util.UUID;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class TestScenarioProjectInfo {
    public UUID projectId;
    public UUID accountId;

    public static JsonNode toJson(TestScenarioProjectInfo projectInfo) {
        ObjectNode project = JsonNodeFactory.instance.objectNode();
        project.put("id", projectInfo.projectId.toString());
        project.put("accountId", projectInfo.accountId.toString());

        return project;
    }
}
