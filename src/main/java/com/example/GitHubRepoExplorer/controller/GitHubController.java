package com.example.GitHubRepoExplorer.controller;

import com.example.GitHubRepoExplorer.domain.Repository;
import com.example.GitHubRepoExplorer.service.GitHubService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class GitHubController {
    private static final String MEDIA_TYPE = "application/json";
    private final GitHubService gitHubService;

    public GitHubController(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    @GetMapping("/repos/{username}")
    public ResponseEntity<List<Repository>> getRepositories(@PathVariable String username, @RequestHeader(HttpHeaders.ACCEPT) String acceptHeader) {
        if (!MEDIA_TYPE.equals(acceptHeader)) {
            return ResponseEntity.badRequest().build();
        }
        List<Repository> repositories = gitHubService.getNonForkedRepositoriesByUsername(username);
        return ResponseEntity.ok(repositories);
    }
}
