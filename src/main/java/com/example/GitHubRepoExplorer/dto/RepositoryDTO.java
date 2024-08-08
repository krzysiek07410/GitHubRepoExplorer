package com.example.GitHubRepoExplorer.dto;

import com.example.GitHubRepoExplorer.domain.Owner;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RepositoryDTO {
    private String name;
    private Owner owner;
    private boolean fork;
    private String branches_url;
}
