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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class GitHubService {
    private static final String USER_REPOS_URL_TEMPLATE = "/users/%s/repos";
    private static final String USER_NOT_FOUND_EXCEPTION_MESSAGE = "User not found";
    private static final String JSON_PARSE_FAILED_MESSAGE = "Failed to parse json";
    private static final String headerValue = "application/vnd.github.v3+json";
    private static final String EMPTY_VALUE = "";

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String authToken;

    public GitHubService(WebClient webClient, ObjectMapper objectMapper, @Value("${github.token}") String authToken) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
        this.authToken = authToken;
    }

    public List<Repository> getNonForkedRepositoriesByUsername(String username) {
        String url = String.format(USER_REPOS_URL_TEMPLATE, username);
        List<RepositoryDTO> repositoryDTOS = fetchRepositories(url);
        return repositoryDTOS.stream()
                .filter(this::checkIfRepoIsNotForked)
                .map(this::mapToRepository)
                .toList();
    }

    private boolean checkIfRepoIsNotForked(RepositoryDTO repo) {
        return !repo.isFork();
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
            String responseBody = makeApiRequest(url);
            return parseJsonToList(responseBody, RepositoryDTO[].class);
        } catch (HttpClientErrorException e) {
            handleWebClientError(e);
            return List.of();
        }
    }

    private List<Branch> getBranches(String branchesUrl) {
        String responseBody = makeApiRequest(branchesUrl);
        List<BranchDTO> branchDTOS = parseJsonToList(responseBody, BranchDTO[].class);
        return branchDTOS.stream()
                .map(this::mapToBranch)
                .toList();
    }

    private <T> List<T> parseJsonToList(String responseBody, Class<T[]> tClass) {
        try {
            return List.of(objectMapper.readValue(responseBody, tClass));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(JSON_PARSE_FAILED_MESSAGE, e);
        }
    }

    private Branch mapToBranch(BranchDTO branchDTO) {
        String branchName = branchDTO.getName();
        String lastCommitSha = branchDTO.getCommit().getSha();
        return new Branch(branchName, new Commit(lastCommitSha));
    }

    private String makeApiRequest(String url) {
        return webClient.get()
                .uri(url)
                .headers(this::setHeaders)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleErrorResponse)
                .bodyToMono(String.class)
                .block();
    }

    private void setHeaders(HttpHeaders headers) {
        headers.set(HttpHeaders.ACCEPT, headerValue);
        if (!authToken.isEmpty()) {
            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + authToken);
        }
    }

    private Mono<? extends Throwable> handleErrorResponse(ClientResponse clientResponse) {
        if (clientResponse.statusCode() == HttpStatus.NOT_FOUND) {
            return Mono.error((new UserNotFoundException(USER_NOT_FOUND_EXCEPTION_MESSAGE)));
        }
        return clientResponse.createException();
    }

    private void handleWebClientError(HttpClientErrorException e) {
        if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
            throw new UserNotFoundException(USER_NOT_FOUND_EXCEPTION_MESSAGE);
        }
        throw e;
    }
}
