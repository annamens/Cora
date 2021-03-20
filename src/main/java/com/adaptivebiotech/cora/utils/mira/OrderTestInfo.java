package com.adaptivebiotech.cora.utils.mira;

import java.time.LocalDateTime;
import java.util.UUID;
import com.fasterxml.jackson.databind.JsonNode;

public class OrderTestInfo
{
    public String sampleName;
    public String testName;
    public UUID orderTestId;
    public String measure;
    public String locus;
    public String status;
    public LocalDateTime created;

    public String stageName;
    public String stageStatus;
    public String subStatusCode;
    public String subStatusMessage;

    // NOTE BOTH OF THESE LEAVE SOME FIELDS NULL WHICH IS STUPID. IF THAT
    // KEEPS HAPPENING SPLIT INTO TWO CLASSES JEEZ IT'S NOT THAT HARD.

    public static OrderTestInfo fromJson(JsonNode json) {
        OrderTestInfo info = new OrderTestInfo();
        info.sampleName = json.get("sampleName").asText();
        info.testName = json.get("test").get("name").asText();
        info.orderTestId = UUID.fromString(json.get("id").asText());
        info.measure = json.get("test").get("measure").asText();
        info.locus = json.get("test").get("locus").asText();
        info.status = json.get("status").asText();
        info.created = LocalDateTime.parse(json.get("created").asText());
        return(info);
    }

    public static OrderTestInfo fromSearchJson(JsonNode json) {
        OrderTestInfo info = new OrderTestInfo();
        info.sampleName = json.get("workflowName").asText();
        info.testName = json.get("testName").asText();
        info.orderTestId = UUID.fromString(json.get("id").asText());
        info.status = json.get("status").asText();

        if (json.hasNonNull("stage")) info.stageName = json.get("stage").asText();
        if (json.hasNonNull("stageStatus")) info.stageStatus = json.get("stageStatus").asText();
        if (json.hasNonNull("subStatusCode")) info.subStatusCode = json.get("subStatusCode").asText();
        if (json.hasNonNull("subStatusMessage")) info.subStatusMessage = json.get("subStatusMessage").asText();

        return(info);
    }
}
