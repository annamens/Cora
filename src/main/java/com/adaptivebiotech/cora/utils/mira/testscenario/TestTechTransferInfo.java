package com.adaptivebiotech.cora.utils.mira.testscenario;

import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class TestTechTransferInfo {
    public String Workspace;
    public String FlowcellId;
    public List<TestSpecimenInfo> Specimens;

    public static JsonNode toJson(TestTechTransferInfo testTechTransferInfo) {
        ObjectNode techTransfer = JsonNodeFactory.instance.objectNode();
        techTransfer.put("workspace", testTechTransferInfo.Workspace);
        techTransfer.put("flowcellId", testTechTransferInfo.FlowcellId);

        ArrayNode specimens = JsonNodeFactory.instance.arrayNode();
        for (TestSpecimenInfo specimen : testTechTransferInfo.Specimens) {
            specimens.add(TestSpecimenInfo.toJson(specimen));
        }

        techTransfer.set("specimens", specimens);

        return techTransfer;
    }
}
