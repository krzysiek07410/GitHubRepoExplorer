package com.example.GitHubRepoExplorer;

import com.example.GitHubRepoExplorer.domain.Owner;
import com.example.GitHubRepoExplorer.dto.RepositoryDTO;
import com.example.GitHubRepoExplorer.exception.UserNotFoundException;
import com.example.GitHubRepoExplorer.utils.GitHubApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersUriSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GitHubApiClientTest {

    @Mock
    private WebClient webClient;

    @Mock
    private RequestHeadersUriSpec<?> requestHeadersUriSpec;

    @Mock
    private RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private ResponseSpec responseSpec;

    @InjectMocks
    private GitHubApiClient gitHubApiClient;

    private static final String AUTH_TOKEN = "testAuthToken";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        gitHubApiClient = new GitHubApiClient(webClient, AUTH_TOKEN);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testMakeApiRequest_Success() {
        String url = "https://api.github.com/users/testUser/repos";
        RepositoryDTO expectedRepo = new RepositoryDTO("testRepo", new Owner("testOwner"), false, "branches_url");
        List<RepositoryDTO> expectedList = List.of(expectedRepo);

        when(webClient.get()).thenReturn((RequestHeadersUriSpec) requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(eq(url))).thenReturn((RequestHeadersSpec) requestHeadersSpec);
        when(requestHeadersSpec.headers(any())).thenReturn((RequestHeadersSpec) requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(RepositoryDTO.class)).thenReturn(Flux.just(expectedRepo));

        List<RepositoryDTO> result = gitHubApiClient.makeApiRequest(url, RepositoryDTO.class);

        verify(webClient).get();
        verify(requestHeadersUriSpec).uri(eq(url));
        verify(requestHeadersSpec).headers(any());
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).onStatus(any(), any());
        verify(responseSpec).bodyToFlux(RepositoryDTO.class);

        assertEquals(expectedList, result);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testMakeApiRequest_Paginated_Success() {
        String url = "https://api.github.com/users/testUser/repos";
        int perPage = 10;
        int page = 1;
        String expectedUrl = String.format("%s?per_page=%d&page=%d", url, perPage, page);
        RepositoryDTO expectedRepo = new RepositoryDTO("testRepo", new Owner("testOwner"), false, "branches_url");
        List<RepositoryDTO> expectedList = List.of(expectedRepo);

        when(webClient.get()).thenReturn((RequestHeadersUriSpec) requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(eq(expectedUrl))).thenReturn((RequestHeadersSpec) requestHeadersSpec);
        when(requestHeadersSpec.headers(any())).thenReturn((RequestHeadersSpec) requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(RepositoryDTO.class)).thenReturn(Flux.just(expectedRepo));

        List<RepositoryDTO> result = gitHubApiClient.makeApiRequest(url, RepositoryDTO.class, perPage, page);

        verify(webClient).get();
        verify(requestHeadersUriSpec).uri(eq(expectedUrl));
        verify(requestHeadersSpec).headers(any());
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).onStatus(any(), any());
        verify(responseSpec).bodyToFlux(RepositoryDTO.class);

        assertEquals(expectedList, result);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testMakeApiRequest_UserNotFound() {
        String url = "https://api.github.com/users/nonExistentUser/repos";

        when(webClient.get()).thenReturn((RequestHeadersUriSpec) requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(eq(url))).thenReturn((RequestHeadersSpec) requestHeadersSpec);
        when(requestHeadersSpec.headers(any())).thenReturn((RequestHeadersSpec) requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(RepositoryDTO.class)).thenReturn(Flux.error(new UserNotFoundException("User not found")));

        assertThrows(UserNotFoundException.class, () -> gitHubApiClient.makeApiRequest(url, RepositoryDTO.class));

        verify(webClient).get();
        verify(requestHeadersUriSpec).uri(eq(url));
        verify(requestHeadersSpec).headers(any());
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).onStatus(any(), any());
        verify(responseSpec).bodyToFlux(RepositoryDTO.class);
    }
}
