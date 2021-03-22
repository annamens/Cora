package com.adaptivebiotech.cora.utils.mira.techtransfer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class TestFastForwardInfo {
    private String stageName;
    private String stageStatus;
    private String subStatusCode;
    private String substatusMsg;
    private String Actor = "Automated Test";

    public static JsonNode toJson(TestFastForwardInfo fastForwardInfo) {
        ObjectNode fastForward = JsonNodeFactory.instance.objectNode();
        fastForward.put("stageName", fastForwardInfo.stageName);
        fastForward.put("stageStatus", fastForwardInfo.stageStatus);
        fastForward.put("subStatusCode", fastForwardInfo.subStatusCode);
        fastForward.put("subStatusMessage", fastForwardInfo.substatusMsg);
        fastForward.put("actor", fastForwardInfo.Actor);

        return fastForward;
    }
    
    public TestFastForwardInfo (String stageName, String stageStatus, String subStatusCode, String subStatusMsg) {
        this.stageName = stageName;
        this.stageStatus = stageStatus;
        this.subStatusCode = subStatusCode;
        this.substatusMsg = subStatusMsg;
    }
}