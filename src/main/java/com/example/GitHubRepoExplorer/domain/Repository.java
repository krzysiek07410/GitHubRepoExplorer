package com.example.GitHubRepoExplorer.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Repository {
    private String name;
    private Owner owner;
    private List<Branch> branches;
}
