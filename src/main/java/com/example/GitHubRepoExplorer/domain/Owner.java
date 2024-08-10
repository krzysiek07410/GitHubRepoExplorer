package com.example.GitHubRepoExplorer.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(name = "Owner", description = "An owner of a repository")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Owner {
    @Schema(description = "Login of the owner")
    private String login;
}
