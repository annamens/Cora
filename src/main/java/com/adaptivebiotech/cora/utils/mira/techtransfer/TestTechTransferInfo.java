package com.adaptivebiotech.cora.utils.mira.techtransfer;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class TestTechTransferInfo {
    private String                  workspace;
    private String                  flowcellId;
    private List <TestSpecimenInfo> specimens;

    public static JsonNode toJson (TestTechTransferInfo testTechTransferInfo) {
        ObjectNode techTransfer = JsonNodeFactory.instance.objectNode ();
        techTransfer.put ("workspace", testTechTransferInfo.workspace);
        techTransfer.put ("flowcellId", testTechTransferInfo.flowcellId);

        ArrayNode specimens = JsonNodeFactory.instance.arrayNode ();
        for (TestSpecimenInfo specimen : testTechTransferInfo.specimens) {
            specimens.add (TestSpecimenInfo.toJson (specimen));
        }

        techTransfer.set ("specimens", specimens);

        return techTransfer;
    }

    public TestTechTransferInfo (String workspace, String flowcellId, int specimensSize) {
        this.workspace = workspace;
        this.flowcellId = flowcellId;
        this.specimens = new ArrayList <> (specimensSize);
    }

    public void addSpecimen (TestSpecimenInfo testSpecimenInfo) {
        specimens.add (testSpecimenInfo);
    }
}
