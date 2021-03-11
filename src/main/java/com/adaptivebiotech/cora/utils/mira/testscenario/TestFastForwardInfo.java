package com.adaptivebiotech.cora.utils.mira.testscenario;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class TestFastForwardInfo {
    public String stageName;
    public String stageStatus;
    public String subStatusCode;
    public String substatusMsg;
    public String Actor = "Automated Test";

    public static JsonNode toJson(TestFastForwardInfo fastForwardInfo) {
        ObjectNode fastForward = JsonNodeFactory.instance.objectNode();
        fastForward.put("stageName", fastForwardInfo.stageName);
        fastForward.put("stageStatus", fastForwardInfo.stageStatus);
        fastForward.put("subStatusCode", fastForwardInfo.subStatusCode);
        fastForward.put("subStatusMessage", fastForwardInfo.substatusMsg);
        fastForward.put("actor", fastForwardInfo.Actor);

        return fastForward;
    }
}