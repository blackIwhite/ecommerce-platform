package com.ecommerce.common.core.util;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonUtilsTest {

    @Test
    void toJson_shouldSerializeObject() {
        Map<String, Object> obj = Map.of("name", "test", "value", 123);
        String json = JsonUtils.toJson(obj);
        assertNotNull(json);
        assertTrue(json.contains("\"name\""));
        assertTrue(json.contains("\"test\""));
    }

    @Test
    void toJson_nullInput_shouldReturnNull() {
        assertNull(JsonUtils.toJson(null));
    }

    @Test
    void toJson_shouldWriteNumbersAsStrings() {
        Map<String, Long> obj = Map.of("id", 123456789012345L);
        String json = JsonUtils.toJson(obj);
        assertTrue(json.contains("\"123456789012345\""));
    }

    @Test
    void fromJson_shouldDeserializeToObject() {
        String json = """
                {"name":"test","value":"123"}
                """;
        Map<?, ?> result = JsonUtils.fromJson(json, Map.class);
        assertNotNull(result);
        assertEquals("test", result.get("name"));
    }

    @Test
    void fromJson_nullInput_shouldReturnNull() {
        assertNull(JsonUtils.fromJson(null, Map.class));
        assertNull(JsonUtils.fromJson("", Map.class));
    }

    @Test
    void fromJson_shouldIgnoreUnknownProperties() {
        String json = """
                {"name":"test","unknownField":"ignored"}
                """;
        SimpleBean result = JsonUtils.fromJson(json, SimpleBean.class);
        assertNotNull(result);
        assertEquals("test", result.name);
    }

    @Test
    void fromJson_invalidJson_shouldThrowException() {
        assertThrows(RuntimeException.class, () ->
                JsonUtils.fromJson("not valid json", Map.class));
    }

    @Test
    void fromJson_withTypeReference_shouldDeserializeGenericTypes() {
        String json = """
                [{"name":"a"},{"name":"b"}]
                """;
        List<SimpleBean> result = JsonUtils.fromJson(json, new TypeReference<List<SimpleBean>>() {});
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("a", result.get(0).name);
    }

    @Test
    void fromJson_typeReference_nullInput_shouldReturnNull() {
        assertNull(JsonUtils.fromJson(null, new TypeReference<List<String>>() {}));
        assertNull(JsonUtils.fromJson("", new TypeReference<List<String>>() {}));
    }

    @Test
    void getObjectMapper_shouldReturnNonNull() {
        assertNotNull(JsonUtils.getObjectMapper());
    }

    public static class SimpleBean {
        public String name;
    }
}
