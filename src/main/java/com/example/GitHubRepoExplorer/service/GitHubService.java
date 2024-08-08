package com.example.GitHubRepoExplorer.service;

import com.example.GitHubRepoExplorer.domain.Branch;
import com.example.GitHubRepoExplorer.domain.Commit;
import com.example.GitHubRepoExplorer.domain.Owner;
import com.example.GitHubRepoExplorer.domain.Repository;
import com.example.GitHubRepoExplorer.dto.BranchDTO;
import com.example.GitHubRepoExplorer.dto.RepositoryDTO;
import com.example.GitHubRepoExplorer.exception.UserNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class GitHubService {
    private static final String BASE_URL = "https://api.github.com";
    private static final String USER_REPOS_URL_TEMPLATE = BASE_URL + "/users/%s/repos";
    private static final String USER_NOT_FOUND_EXCEPTION_MESSAGE = "User not found";
    private static final String REPOSITORIES_RETRIEVAL_EXCEPTION_MESSAGE = "Failed to retrieve repositories";
    private static final String BRANCHES_PARSE_FAILED_MESSAGE = "Failed to parse branches";
    private static final String EMPTY_VALUE = "";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String authToken;

    public GitHubService(RestTemplate restTemplate, ObjectMapper objectMapper, @Value("${github.token}") String authToken) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.authToken = authToken;
    }

    public List<Repository> getNonForkedRepositoriesByUsername(String username) {
        String url = String.format(USER_REPOS_URL_TEMPLATE, username);
        List<RepositoryDTO> repositoryDTOS = fetchRepositories(url);
        return repositoryDTOS.stream()
                .filter(repo -> !repo.isFork())
                .map(this::mapToRepository)
                .toList();
    }

    private Repository mapToRepository(RepositoryDTO repo) {
        String repoName = repo.getName();
        Owner owner = repo.getOwner();
        String ownerLogin = owner.getLogin();
        String branchesUrl = repo.getBranches_url().replace("{/branch}", EMPTY_VALUE);

        List<Branch> branches = getBranches(branchesUrl);
        return new Repository(repoName, new Owner(ownerLogin), branches);
    }

    private List<RepositoryDTO> fetchRepositories(String url) {
        try {
            ResponseEntity<String> response = makeApiRequest(url);
            return List.of(objectMapper.readValue(response.getBody(), RepositoryDTO[].class));
        } catch (HttpClientErrorException e) {
            handleHttpClientError(e);
            return List.of();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(REPOSITORIES_RETRIEVAL_EXCEPTION_MESSAGE, e);
        }
    }

    private List<Branch> getBranches(String branchesUrl) {
        try {
            ResponseEntity<String> response = makeApiRequest(branchesUrl);
            List<BranchDTO> branchDTOS = List.of(objectMapper.readValue(response.getBody(), BranchDTO[].class));
            return branchDTOS.stream()
                    .map(this::mapToBranch)
                    .toList();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(BRANCHES_PARSE_FAILED_MESSAGE, e);
        }
    }

    private Branch mapToBranch(BranchDTO branchDTO) {
        String branchName = branchDTO.getName();
        String lastCommitSha = branchDTO.getCommit().getSha();
        return new Branch(branchName, new Commit(lastCommitSha));
    }

    private ResponseEntity<String> makeApiRequest(String url) {
        HttpHeaders headers = createHeaders();
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        return restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                String.class
        );
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        if (!authToken.isEmpty()) {
            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + authToken);
        }
        return headers;
    }

    private void handleHttpClientError(HttpClientErrorException e) {
        if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
            throw new UserNotFoundException(USER_NOT_FOUND_EXCEPTION_MESSAGE);
        }
        throw e;
    }
}
