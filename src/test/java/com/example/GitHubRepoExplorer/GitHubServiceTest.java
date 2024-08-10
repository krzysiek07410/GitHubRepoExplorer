package com.example.GitHubRepoExplorer;

import com.example.GitHubRepoExplorer.domain.Branch;
import com.example.GitHubRepoExplorer.domain.Owner;
import com.example.GitHubRepoExplorer.domain.Repository;
import com.example.GitHubRepoExplorer.dto.RepositoryDTO;
import com.example.GitHubRepoExplorer.service.GitHubService;
import com.example.GitHubRepoExplorer.utils.GitHubApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class GitHubServiceTest {
    @Mock
    private GitHubApiClient gitHubApiClient;

    @InjectMocks
    private GitHubService gitHubService;

    @Captor
    private ArgumentCaptor<String> urlCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetNonForkedRepositoriesByUsername_NoForkedRepos() {
        String username = "testUser";
        int perPage = 10;
        int page = 1;
        String repoName = "testRepo";
        String ownerLogin = "testOwner";

        Owner owner = new Owner(ownerLogin);
        String branchesUrl = "http://api.github.com/repos/testUser/testRepo/branches{/branch}";

        RepositoryDTO repoDTO = new RepositoryDTO(repoName, owner, false, branchesUrl);
        List<RepositoryDTO> repoDTOs = Collections.singletonList(repoDTO);

        when(gitHubApiClient.makeApiRequest(anyString(), eq(RepositoryDTO.class), eq(perPage), eq(page)))
                .thenReturn(repoDTOs);
        when(gitHubApiClient.makeApiRequest(anyString(), eq(Branch.class)))
                .thenReturn(Collections.emptyList());

        List<Repository> repositories = gitHubService.getNonForkedRepositoriesByUsername(username, perPage, page);

        verify(gitHubApiClient).makeApiRequest(urlCaptor.capture(), eq(RepositoryDTO.class), eq(perPage), eq(page));
        assertTrue(urlCaptor.getValue().contains(username));
        assertEquals(1, repositories.size());
        assertEquals(repoName, repositories.getFirst().getName());
        assertEquals(ownerLogin, repositories.getFirst().getOwner().getLogin());
        assertTrue(repositories.getFirst().getBranches().isEmpty());
    }

    @Test
    void testGetNonForkedRepositoriesByUsername_WithForkedRepos() {
        String username = "testUser";
        int perPage = 10;
        int page = 1;
        String repoName = "testRepo";
        String ownerLogin = "testOwner";

        List<RepositoryDTO> repoDTOs = getRepositoryDTOS(ownerLogin, repoName);

        when(gitHubApiClient.makeApiRequest(anyString(), eq(RepositoryDTO.class), eq(perPage), eq(page)))
                .thenReturn(repoDTOs);
        when(gitHubApiClient.makeApiRequest(anyString(), eq(Branch.class)))
                .thenReturn(Collections.emptyList());

        List<Repository> repositories = gitHubService.getNonForkedRepositoriesByUsername(username, perPage, page);

        verify(gitHubApiClient).makeApiRequest(urlCaptor.capture(), eq(RepositoryDTO.class), eq(perPage), eq(page));
        assertTrue(urlCaptor.getValue().contains(username));
        assertEquals(1, repositories.size());
        assertEquals(repoName, repositories.getFirst().getName());
        assertEquals(ownerLogin, repositories.getFirst().getOwner().getLogin());
        assertTrue(repositories.getFirst().getBranches().isEmpty());
    }

    private List<RepositoryDTO> getRepositoryDTOS(String ownerLogin, String repoName) {
        Owner owner = new Owner(ownerLogin);
        String nonForkedBranchesUrl = "http://api.github.com/repos/testUser/testRepo/branches{/branch}";
        RepositoryDTO nonForkedRepo = new RepositoryDTO(repoName, owner, false, nonForkedBranchesUrl);
        String forkedBranchesUrl = "http://api.github.com/repos/testUser/forkedRepo/branches{/branch}";
        RepositoryDTO forkedRepo = new RepositoryDTO("forkedRepo", owner, true, forkedBranchesUrl);
        return List.of(nonForkedRepo, forkedRepo);
    }

    @Test
    void testGetNonForkedRepositoriesByUsername_NoRepos() {
        String username = "testUser";
        int perPage = 10;
        int page = 1;

        when(gitHubApiClient.makeApiRequest(anyString(), eq(RepositoryDTO.class), eq(perPage), eq(page)))
                .thenReturn(Collections.emptyList());

        List<Repository> repositories = gitHubService.getNonForkedRepositoriesByUsername(username, perPage, page);

        verify(gitHubApiClient).makeApiRequest(urlCaptor.capture(), eq(RepositoryDTO.class), eq(perPage), eq(page));
        assertTrue(urlCaptor.getValue().contains(username));
        assertTrue(repositories.isEmpty());
    }
}
