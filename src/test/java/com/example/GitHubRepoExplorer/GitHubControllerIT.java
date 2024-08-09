package com.example.GitHubRepoExplorer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:test.properties")
public class GitHubControllerIT {
    @Autowired
    private MockMvc mockMvc;
    private static final String MEDIA_TYPE = "application/json";

    @Test
    void shouldReturnMyRepositories() throws Exception {
        MockHttpServletRequestBuilder getRequestBuilder = get("/api/repos/krzysiek07410")
                .accept(MEDIA_TYPE);
        mockMvc.perform(getRequestBuilder)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MEDIA_TYPE))
                .andExpect(jsonPath("$[0].name").value("EatGood-Backend"));
    }

    @Test
    void shouldReturnMyRepositoriesWithOnePerPage() throws Exception {
        MockHttpServletRequestBuilder getRequestBuilder = get("/api/repos/krzysiek07410")
                .accept(MEDIA_TYPE)
                .param("per_page", "1");
        mockMvc.perform(getRequestBuilder)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MEDIA_TYPE))
                .andExpect(content().string("[{\"name\":\"EatGood-Backend\",\"owner\":{\"login\":\"krzysiek07410\"},\"branches\":[{\"name\":\"main\",\"commit\":{\"sha\":\"10376a4e9d4bbf278f9bc99d007c36ae6d535f01\"}}]}]"));
    }

    @Test
    void shouldThrowInvalidAcceptHeaderException() throws Exception {
        MockHttpServletRequestBuilder getRequestBuilder = get("/api/repos/krzysiek07410").accept("text/plain");
        mockMvc.perform(getRequestBuilder)
                .andDo(print())
                .andExpect(status().isNotAcceptable())
                .andExpect(content().string("{\"status\":406,\"message\":\"Invalid Accept header\"}"));
    }

    @Test
    void shouldThrowInvalidParamException() throws Exception {
        MockHttpServletRequestBuilder getRequestBuilder = get("/api/repos/krzysiek07410")
                .accept(MEDIA_TYPE)
                .param("per_page", "101");
        mockMvc.perform(getRequestBuilder)
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"status\":400,\"message\":\"Invalid per_page parameter\"}"));
    }

    @Test
    void shouldThrowUserNotFoundException() throws Exception {
        MockHttpServletRequestBuilder getRequestBuilder = get("/api/repos/klajdsflkasjpoasofjska")
                .accept(MEDIA_TYPE);
        mockMvc.perform(getRequestBuilder)
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().string("{\"status\":404,\"message\":\"User not found\"}"));
    }
}
