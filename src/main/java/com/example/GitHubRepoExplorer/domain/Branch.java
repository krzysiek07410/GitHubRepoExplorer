package com.example.GitHubRepoExplorer.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(name = "Branch", description = "A branch of a repository")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Branch {
    @Schema(description = "Name of the branch")
    private String name;
    @Schema(description = "Last commit of the branch")
    private Commit commit;
}