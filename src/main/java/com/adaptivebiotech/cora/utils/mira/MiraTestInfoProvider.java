package com.adaptivebiotech.cora.utils.mira;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class MiraTestInfoProvider {
    
    private ObjectMapper objectMapper = new ObjectMapper();
    
    public List<MiraTestInfo> getMiraTestsFromFile(String jsonFilename) {
        String path = ClassLoader.getSystemResource (jsonFilename).getPath ();
        List<MiraTestInfo> miraTestInfos = new ArrayList<>();
        
        try {
            String json = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
            JsonNode jsonNode = objectMapper.readTree(json);
            ArrayNode arrayNode = (ArrayNode) jsonNode;
            for (int i = 0; i < arrayNode.size(); ++i) {
                JsonNode jsonMiraTest = arrayNode.get(i);
                miraTestInfos.add(MiraTestInfo.fromSearchJson(jsonMiraTest));
            }
        } catch (Exception e) {
            throw new RuntimeException (e); // cheese?
        }
        
        return miraTestInfos;
    }

}
