package com.example.GitHubRepoExplorer.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(name = "Commit", description = "A last commit of a branch")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Commit {
    @Schema(description = "SHA of the commit")
    private String sha;
}
