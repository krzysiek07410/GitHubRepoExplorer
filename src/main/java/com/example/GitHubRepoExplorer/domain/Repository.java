package com.example.GitHubRepoExplorer.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(name = "Repository", description = "A repository on GitHub")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Repository {
    @Schema(description = "Name of the repository")
    private String name;
    @Schema(description = "Owner of the repository")
    private Owner owner;
    @Schema(description = "List of branches of the repository")
    private List<Branch> branches;
}
