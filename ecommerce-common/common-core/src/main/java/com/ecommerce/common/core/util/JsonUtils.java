package com.ecommerce.common.core.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

/**
 * Jackson ObjectMapper utility.
 */
@Slf4j
public final class JsonUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        // Register Java 8 date/time module
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Ignore unknown properties on deserialization
        OBJECT_MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        // Serialize Long as String to avoid JS precision loss
        OBJECT_MAPPER.configure(SerializationFeature.WRITE_NUMBERS_AS_STRINGS, true);
    }

    private JsonUtils() {
    }

    /**
     * Returns the shared ObjectMapper instance.
     */
    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    /**
     * Serialize an object to JSON string.
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Serialize object to JSON failed: {}", e.getMessage(), e);
            throw new RuntimeException("JSON serialization error", e);
        }
    }

    /**
     * Deserialize a JSON string to the specified type.
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("Deserialize JSON to object failed: {}", e.getMessage(), e);
            throw new RuntimeException("JSON deserialization error", e);
        }
    }

    /**
     * Deserialize a JSON string to a generic type reference.
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            log.error("Deserialize JSON to object failed: {}", e.getMessage(), e);
            throw new RuntimeException("JSON deserialization error", e);
        }
    }
}
