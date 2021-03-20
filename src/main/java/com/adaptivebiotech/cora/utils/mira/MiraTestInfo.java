package com.adaptivebiotech.cora.utils.mira;

import com.fasterxml.jackson.databind.JsonNode;

public class MiraTestInfo extends OrderTestInfo {
    public Integer cellCount;
    public String orderNumber;
    public String specimenNumber;
    public String expansionNumber;
    public String poolIndicator;
    public String tsvPath;
    public String orderStatus;

    public static MiraTestInfo fromSearchJson(JsonNode json) {
        MiraTestInfo info = fromOrderTestInfo(OrderTestInfo.fromSearchJson(json));
        info.cellCount = json.get("cellCount").asInt();
        info.orderNumber = json.get("orderNumber").asText();
        info.specimenNumber = json.get("specimenNumber").asText();
        info.poolIndicator = json.get("poolIndicator").asText();
        info.tsvPath = json.get("tsvPath").asText();

        if (json.hasNonNull("orderStatus")) info.expansionNumber = json.get("orderStatus").asText();
        if (json.hasNonNull("expansionNumber")) info.expansionNumber = json.get("expansionNumber").asText();

        return(info);
    }

    public static MiraTestInfo fromOrderTestInfo(OrderTestInfo testInfo) {
        MiraTestInfo info = new MiraTestInfo();
        info.sampleName = testInfo.sampleName;
        info.testName = testInfo.testName;
        info.orderTestId = testInfo.orderTestId;
        info.status = testInfo.status;
        info.stageName = testInfo.stageName;
        info.stageStatus = testInfo.stageStatus;
        info.subStatusCode = testInfo.subStatusCode;
        info.subStatusMessage = testInfo.subStatusMessage;

        return (info);
    }
}
