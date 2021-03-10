package com.adaptivebiotech.cora.utils.mira.testscenario;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class TestFastForwardInfo {
    public String StageName;
    public String StageStatus;
    public String SubStatusCode;
    public String SubstatusMsg;
    public String Actor = "Flora";

    public static JsonNode toJson(TestFastForwardInfo fastForwardInfo) {
        ObjectNode fastForward = JsonNodeFactory.instance.objectNode();
        fastForward.put("stageName", fastForwardInfo.StageName);
        fastForward.put("stageStatus", fastForwardInfo.StageStatus);
        fastForward.put("subStatusCode", fastForwardInfo.SubStatusCode);
        fastForward.put("subStatusMessage", fastForwardInfo.SubstatusMsg);
        fastForward.put("actor", fastForwardInfo.Actor);

        return fastForward;
    }
}