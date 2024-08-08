package com.example.GitHubRepoExplorer.dto;

import com.example.GitHubRepoExplorer.domain.Commit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BranchDTO {
    private String name;
    private Commit commit;
}
