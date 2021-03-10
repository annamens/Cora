package com.adaptivebiotech.cora.utils.mira;

import com.fasterxml.jackson.databind.JsonNode;

public class MiraTestInfo extends OrderTestInfo {
    public Integer CellCount;
    public String OrderNumber;
    public String SpecimenNumber;
    public String ExpansionNumber;
    public String PoolIndicator;
    public String TsvPath;
    public String OrderStatus;

    public static MiraTestInfo fromSearchJson(JsonNode json) {
        MiraTestInfo info = fromOrderTestInfo(OrderTestInfo.fromSearchJson(json));
        info.CellCount = json.get("cellCount").asInt();
        info.OrderNumber = json.get("orderNumber").asText();
        info.SpecimenNumber = json.get("specimenNumber").asText();
        info.PoolIndicator = json.get("poolIndicator").asText();
        info.TsvPath = json.get("tsvPath").asText();

        if (json.hasNonNull("orderStatus")) info.ExpansionNumber = json.get("orderStatus").asText();
        if (json.hasNonNull("expansionNumber")) info.ExpansionNumber = json.get("expansionNumber").asText();

        return(info);
    }

    public static MiraTestInfo fromOrderTestInfo(OrderTestInfo testInfo) {
        MiraTestInfo info = new MiraTestInfo();
        info.SampleName = testInfo.SampleName;
        info.TestName = testInfo.TestName;
        info.OrderTestId = testInfo.OrderTestId;
        info.Status = testInfo.Status;
        info.StageName = testInfo.StageName;
        info.StageStatus = testInfo.StageStatus;
        info.SubStatusCode = testInfo.SubStatusCode;
        info.SubStatusMessage = testInfo.SubStatusMessage;

        return (info);
    }
}
