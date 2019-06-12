package br.inf.teorema.regen.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JSONUtil {

    private static ObjectMapper objectMapper = new ObjectMapper();

    public static String stringify(Object value) throws JsonProcessingException {
        return objectMapper.writeValueAsString(value);
    }

    public static String prettify(Object value) throws JsonProcessingException {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
    }

    public static <T> T read(String value, Class<T> clazz) throws IOException {
        return objectMapper.readValue(value, clazz);
    }

    public static Map<String, Object> read(String value) throws IOException {
        return objectMapper.readValue(value, new HashMap<String, Object>().getClass());
    }

    public static String prettifyJSONString(String value) throws IOException {
        return prettify(read(value));
    }

    public static <T> T convert(Object value, Class<T> clazz) {
        return objectMapper.convertValue(value, clazz);
    }

}
