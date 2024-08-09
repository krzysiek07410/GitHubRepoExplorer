package com.example.GitHubRepoExplorer.service;

import com.example.GitHubRepoExplorer.domain.Branch;
import com.example.GitHubRepoExplorer.domain.Owner;
import com.example.GitHubRepoExplorer.domain.Repository;
import com.example.GitHubRepoExplorer.dto.RepositoryDTO;
import com.example.GitHubRepoExplorer.utils.GitHubApiClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GitHubService {
    private static final String USER_REPOS_URL_TEMPLATE = "/users/%s/repos";
    private static final String EMPTY_VALUE = "";

    private final GitHubApiClient gitHubApiClient;

    public GitHubService(GitHubApiClient gitHubApiClient) {
        this.gitHubApiClient = gitHubApiClient;
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
        return gitHubApiClient.makeApiRequest(url, RepositoryDTO.class);
    }

    private List<Branch> getBranches(String branchesUrl) {
        return gitHubApiClient.makeApiRequest(branchesUrl, Branch.class);

    }
}
