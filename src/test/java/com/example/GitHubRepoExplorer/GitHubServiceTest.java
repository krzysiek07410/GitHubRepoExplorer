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
        RepositoryDTO repoDTO = new RepositoryDTO(repoName, owner, false, "http://api.github.com/repos/testUser/testRepo/branches{/branch}");
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

        Owner owner = new Owner(ownerLogin);
        RepositoryDTO nonForkedRepo = new RepositoryDTO(repoName, owner, false, "http://api.github.com/repos/testUser/testRepo/branches{/branch}");
        RepositoryDTO forkedRepo = new RepositoryDTO("forkedRepo", owner, true, "http://api.github.com/repos/testUser/forkedRepo/branches{/branch}");
        List<RepositoryDTO> repoDTOs = List.of(nonForkedRepo, forkedRepo);

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
