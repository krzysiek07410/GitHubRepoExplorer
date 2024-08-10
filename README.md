# GitHubRepoExplorer
 
## Description
This project is a simple REST API that allows users to search for GitHub repositories using the GitHub API.

## Features
- Search for repositories by username
- Paginate the results

## Technologies
- Java 21
- Spring Boot 3.3.2

## How to run
1. Clone the repository
2. Run the project
3. Access the API at `http://localhost:8080`
4. Use the endpoints `/repositories/{username}` or `/repositories/{username}?per_page={per_page}&page={page}` to search for repositories
