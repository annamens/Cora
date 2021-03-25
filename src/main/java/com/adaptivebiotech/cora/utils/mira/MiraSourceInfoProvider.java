package com.adaptivebiotech.cora.utils.mira;

import static com.seleniumfy.test.utils.Logging.error;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import com.adaptivebiotech.cora.dto.mirasource.MiraSourceInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MiraSourceInfoProvider {

    private String       jsonFilename;
    private ObjectMapper objectMapper = new ObjectMapper ();

    public MiraSourceInfoProvider (String jsonFilename) {
        this.jsonFilename = jsonFilename;
    }

    public MiraSourceInfo getMiraSourceInfoFromFile () {
        String path = ClassLoader.getSystemResource (jsonFilename).getPath ();
        try {
            String json = new String (Files.readAllBytes (Paths.get (path)), StandardCharsets.UTF_8);
            MiraSourceInfo miraSourceInfo = objectMapper.readValue (json, MiraSourceInfo.class);
            return miraSourceInfo;
        } catch (Exception e) {
            error (e.getMessage ());
            throw new RuntimeException (e);
        }
    }

}
