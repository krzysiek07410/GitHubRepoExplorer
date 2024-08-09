package com.example.GitHubRepoExplorer.service;

import com.example.GitHubRepoExplorer.domain.Branch;
import com.example.GitHubRepoExplorer.domain.Commit;
import com.example.GitHubRepoExplorer.domain.Owner;
import com.example.GitHubRepoExplorer.domain.Repository;
import com.example.GitHubRepoExplorer.dto.BranchDTO;
import com.example.GitHubRepoExplorer.dto.RepositoryDTO;
import com.example.GitHubRepoExplorer.utils.GitHubApiClient;
import com.example.GitHubRepoExplorer.utils.JsonUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GitHubService {
    private static final String USER_REPOS_URL_TEMPLATE = "/users/%s/repos";
    private static final String EMPTY_VALUE = "";

    private final GitHubApiClient gitHubApiClient;
    private final JsonUtils jsonUtils;

    public GitHubService(GitHubApiClient gitHubApiClient, JsonUtils jsonUtils) {
        this.gitHubApiClient = gitHubApiClient;
        this.jsonUtils = jsonUtils;
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
        String responseBody = gitHubApiClient.makeApiRequest(url);
        return jsonUtils.parseJsonToList(responseBody, RepositoryDTO[].class);
    }

    private List<Branch> getBranches(String branchesUrl) {
        String responseBody = gitHubApiClient.makeApiRequest(branchesUrl);
        List<BranchDTO> branchDTOS = jsonUtils.parseJsonToList(responseBody, BranchDTO[].class);
        return branchDTOS.stream()
                .map(this::mapToBranch)
                .toList();
    }

    private Branch mapToBranch(BranchDTO branchDTO) {
        String branchName = branchDTO.getName();
        String lastCommitSha = branchDTO.getCommit().getSha();
        return new Branch(branchName, new Commit(lastCommitSha));
    }
}
