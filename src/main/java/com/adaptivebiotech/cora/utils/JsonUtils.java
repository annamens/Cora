package com.adaptivebiotech.cora.utils;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtils {

    /**
     * return key value as string from JSON string
     * 
     * @param jsonStr
     *            json string
     * @param key
     * @return string value of key
     */
    public static String getDataFromJsonString (String jsonStr, String key) {

        try {
            JSONObject json = new JSONObject (jsonStr);
            return json.isNull (key) ? "" : json.get (key).toString ();
        } catch (JSONException e) {
            throw new RuntimeException (e);
        }

    }

}
