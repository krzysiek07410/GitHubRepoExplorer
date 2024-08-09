package com.example.GitHubRepoExplorer.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JsonUtils {
    private static final String JSON_PARSE_FAILED_MESSAGE = "Failed to parse json";
    private final ObjectMapper objectMapper;

    public JsonUtils(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T> List<T> parseJsonToList(String responseBody, Class<T[]> tClass) {
        try {
            return List.of(objectMapper.readValue(responseBody, tClass));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(JSON_PARSE_FAILED_MESSAGE, e);
        }
    }
}
