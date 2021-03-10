package com.adaptivebiotech.cora.utils.mira;

import java.time.LocalDateTime;
import java.util.UUID;
import com.fasterxml.jackson.databind.JsonNode;

public class OrderTestInfo
{
    public String SampleName;
    public String TestName;
    public UUID OrderTestId;
    public String Measure;
    public String Locus;
    public String Status;
    public LocalDateTime Created;

    public String StageName;
    public String StageStatus;
    public String SubStatusCode;
    public String SubStatusMessage;

    // NOTE BOTH OF THESE LEAVE SOME FIELDS NULL WHICH IS STUPID. IF THAT
    // KEEPS HAPPENING SPLIT INTO TWO CLASSES JEEZ IT'S NOT THAT HARD.

    public static OrderTestInfo fromJson(JsonNode json) {
        OrderTestInfo info = new OrderTestInfo();
        info.SampleName = json.get("sampleName").asText();
        info.TestName = json.get("test").get("name").asText();
        info.OrderTestId = UUID.fromString(json.get("id").asText());
        info.Measure = json.get("test").get("measure").asText();
        info.Locus = json.get("test").get("locus").asText();
        info.Status = json.get("status").asText();
        info.Created = LocalDateTime.parse(json.get("created").asText());
        return(info);
    }

    public static OrderTestInfo fromSearchJson(JsonNode json) {
        OrderTestInfo info = new OrderTestInfo();
        info.SampleName = json.get("workflowName").asText();
        info.TestName = json.get("testName").asText();
        info.OrderTestId = UUID.fromString(json.get("id").asText());
        info.Status = json.get("status").asText();

        if (json.hasNonNull("stage")) info.StageName = json.get("stage").asText();
        if (json.hasNonNull("stageStatus")) info.StageStatus = json.get("stageStatus").asText();
        if (json.hasNonNull("subStatusCode")) info.SubStatusCode = json.get("subStatusCode").asText();
        if (json.hasNonNull("subStatusMessage")) info.SubStatusMessage = json.get("subStatusMessage").asText();

        return(info);
    }
}
