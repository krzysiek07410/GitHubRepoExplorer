package com.example.GitHubRepoExplorer.controller;

import com.example.GitHubRepoExplorer.domain.Repository;
import com.example.GitHubRepoExplorer.service.GitHubService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class GitHubController {
    private final GitHubService gitHubService;

    public GitHubController(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    @GetMapping("/repos/{username}")
    public ResponseEntity<List<Repository>> getRepositories(@PathVariable String username) {
        List<Repository> repositories = gitHubService.getNonForkedRepositoriesByUsername(username);
        return ResponseEntity.ok(repositories);
    }
}
