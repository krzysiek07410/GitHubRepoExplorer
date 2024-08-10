package com.example.GitHubRepoExplorer.controller;

import com.example.GitHubRepoExplorer.domain.Repository;
import com.example.GitHubRepoExplorer.exception.InvalidAcceptHeaderException;
import com.example.GitHubRepoExplorer.exception.InvalidParamException;
import com.example.GitHubRepoExplorer.service.GitHubService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "GitHubRepoExplorer", description = "Endpoints for accessing repositories from GitHub API")
@RestController
@RequestMapping("/api")
public class GitHubController {
    private static final String MEDIA_TYPE = "application/json";
    private static final String INVALID_ACCEPT_HEADER_EXCEPTION_MESSAGE = "Invalid Accept header";
    private static final String INVALID_PER_PAGE_PARAMETER_EXCEPTION_MESSAGE = "Invalid per_page parameter";
    private static final String INVALID_PAGE_PARAMETER_EXCEPTION_MESSAGE = "Invalid page parameter";
    private static final int MAX_PER_PAGE = 100;
    private static final int MIN_PER_PAGE = 1;
    private final GitHubService gitHubService;

    public GitHubController(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    @Operation(
            summary = "Get non forked repositories",
            description = "Fetches non forked repositories by username from GitHub API"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully fetched repositories"),
            @ApiResponse(responseCode = "400", description = "Invalid per_page parameter", content = @Content(examples = {
                    @ExampleObject(value = "{\n" +
                            "    \"status\": 400,\n" +
                            "    \"message\": \"Invalid per_page parameter\"\n" +
                            "}")
            })),
            @ApiResponse(responseCode = "400", description = "Invalid page parameter", content = @Content(examples = {
                    @ExampleObject(value = "{\n" +
                            "    \"status\": 400,\n" +
                            "    \"message\": \"Invalid page parameter\"\n" +
                            "}")
            })),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(examples = {
                    @ExampleObject(value = "{\n" +
                            "    \"status\": 404,\n" +
                            "    \"message\": \"User not found\"\n" +
                            "}")
            })),
            @ApiResponse(responseCode = "406", description = "Invalid Accept header", content = @Content(examples = {
                    @ExampleObject(value = "{\n" +
                            "    \"status\": 406,\n" +
                            "    \"message\": \"Invalid Accept header\"\n" +
                            "}")
            }))
    })
    @GetMapping("/repos/{username}")
    public ResponseEntity<List<Repository>> getRepositories(
            @PathVariable String username,
            @RequestHeader(HttpHeaders.ACCEPT) String acceptHeader,
            @RequestParam(name = "per_page", defaultValue = "30") int perPage,
            @RequestParam(name = "page", defaultValue = "1") int page
    ) {
        checkAcceptHeader(acceptHeader);

        checkPerPageParameter(perPage);

        checkPageParameter(page);

        List<Repository> repositories = gitHubService.getNonForkedRepositoriesByUsername(username, perPage, page);
        return ResponseEntity.ok(repositories);
    }

    private void checkAcceptHeader(String acceptHeader) {
        if (!MEDIA_TYPE.equals(acceptHeader)) {
            throw new InvalidAcceptHeaderException(INVALID_ACCEPT_HEADER_EXCEPTION_MESSAGE);
        }
    }

    private void checkPerPageParameter(int perPage) {
        if (perPage < MIN_PER_PAGE || perPage > MAX_PER_PAGE) {
            throw new InvalidParamException(INVALID_PER_PAGE_PARAMETER_EXCEPTION_MESSAGE);
        }
    }

    private void checkPageParameter(int page) {
        if (page < 1) {
            throw new InvalidParamException(INVALID_PAGE_PARAMETER_EXCEPTION_MESSAGE);
        }
    }
}
