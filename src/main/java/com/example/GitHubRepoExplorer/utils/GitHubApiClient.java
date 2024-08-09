package com.example.GitHubRepoExplorer.utils;

import com.example.GitHubRepoExplorer.dto.RepositoryDTO;
import com.example.GitHubRepoExplorer.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class GitHubApiClient {
    private static final String USER_NOT_FOUND_EXCEPTION_MESSAGE = "User not found";
    private static final String headerValue = "application/vnd.github.v3+json";
    private static final String PAGINATED_URL_FORMAT = "%s?per_page=%d&page=%d";

    private final WebClient webClient;
    private final String authToken;

    public GitHubApiClient(WebClient webClient, @Value("${github.token}") String authToken) {
        this.webClient = webClient;
        this.authToken = authToken;
    }

    public <T> List<T> makeApiRequest(String url, Class<T> responseType) {
        return webClient.get()
                .uri(url)
                .headers(this::setHeaders)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleErrorResponse)
                .bodyToFlux(responseType)
                .collectList()
                .block();
    }

    public <T> List<T> makeApiRequest(String url, Class<T> responseType, int perPage, int page) {
        String paginatedUrl = String.format(PAGINATED_URL_FORMAT, url, perPage, page);
        return webClient.get()
                .uri(paginatedUrl)
                .headers(this::setHeaders)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleErrorResponse)
                .bodyToFlux(responseType)
                .collectList()
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
            return Mono.error(new UserNotFoundException(USER_NOT_FOUND_EXCEPTION_MESSAGE));
        }
        return clientResponse.createException();
    }
}
