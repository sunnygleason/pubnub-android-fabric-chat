package com.pubnub.example.android.fabric.pnfabricchat.util;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONObject;

import java.util.Map;

/**
 * Utility class for converting JSON objects/strings/etc.
 */
public class JsonUtil {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static <T> T fromJSONObject(JSONObject value, Class<T> clazz) throws Exception {
        return mapper.readValue(value.toString(), clazz);
    }

    public static String asJson(Object value) throws Exception {
        return mapper.writeValueAsString(value);
    }

    public static JSONObject asJSONObject(Object value) throws Exception {
        return new JSONObject(mapper.writeValueAsString(value));
    }
}
