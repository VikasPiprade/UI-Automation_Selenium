package com.swag.labs.Skills.Core;

import com.swag.labs.Utilities.ConfigurationUtils;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import java.io.IOException;
import java.util.Properties;

/**
 * Wrapper for GitHub API using github-api library
 */
public class GitHubClient {
    private static GitHubClient instance;
    private GitHub github;
    private SkillLogger logger = new SkillLogger("GitHubClient");
    private Properties config;
    private String owner;
    private String repo;
    
    private GitHubClient() {
        this.config = new ConfigurationUtils().getProperty();
        initializeClient();
    }
    
    public static GitHubClient getInstance() {
        if (instance == null) {
            synchronized (GitHubClient.class) {
                if (instance == null) {
                    instance = new GitHubClient();
                }
            }
        }
        return instance;
    }
    
    private void initializeClient() {
        try {
            String token = System.getenv("GITHUB_TOKEN") != null ? 
                System.getenv("GITHUB_TOKEN") : 
                config.getProperty("github.token", "");
            
            this.owner = config.getProperty("github.owner", "");
            this.repo = config.getProperty("github.repo", "");
            
            if (token.isEmpty() || owner.isEmpty() || repo.isEmpty()) {
                logger.warn("GitHub configuration incomplete. Set GITHUB_TOKEN env var and github.owner, github.repo in config.properties");
            }
            
            this.github = new GitHubBuilder()
                .withOAuthToken(token)
                .build();
            
            logger.info("GitHub client initialized successfully for owner: {} repo: {}", owner, repo);
        } catch (IOException e) {
            logger.error("Failed to initialize GitHub client: {}", e.getMessage(), e);
            throw new RuntimeException("GitHub client initialization failed", e);
        }
    }
    
    /**
     * Get the repository object
     */
    public GHRepository getRepository() {
        try {
            String repoFullName = owner + "/" + repo;
            logger.debug("Getting repository: {}", repoFullName);
            return github.getRepository(repoFullName);
        } catch (IOException e) {
            logger.error("Failed to get repository: {}", e.getMessage());
            throw new RuntimeException("Failed to get GitHub repository", e);
        }
    }
    
    /**
     * Get the underlying GitHub API object
     */
    public GitHub getGitHub() {
        return github;
    }
    
    public String getOwner() {
        return owner;
    }
    
    public String getRepo() {
        return repo;
    }
}
