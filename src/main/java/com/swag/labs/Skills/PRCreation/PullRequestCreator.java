package com.swag.labs.Skills.PRCreation;

import com.swag.labs.Skills.Core.GitHubClient;
import com.swag.labs.Skills.Core.SkillLogger;
import com.swag.labs.Skills.Core.PRCreatedResult;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.PagedIterable;

/**
 * Creates Pull Requests on GitHub
 */
public class PullRequestCreator {
    private SkillLogger logger = new SkillLogger("PullRequestCreator");
    private GitHubClient githubClient;
    
    public PullRequestCreator() {
        this.githubClient = GitHubClient.getInstance();
    }
    
    /**
     * Create a pull request
     */
    public PRCreatedResult createPullRequest(String branchName, String ticketKey, 
                                            String testClassName, String prDescription) {
        try {
            logger.info("Creating pull request for branch: {}", branchName);
            
            GHRepository repo = githubClient.getRepository();
            
            String prTitle = String.format("[%s] Automated Test: %s", ticketKey, testClassName);
            
            // Create PR from feature branch to main
            var pr = repo.createPullRequest(prTitle, branchName, "main", prDescription);
            
            logger.info("Pull request created successfully!");
            logger.info("PR URL: {}", pr.getHtmlUrl());
            logger.info("PR Number: {}", pr.getNumber());
            
            // Add labels
            try {
                pr.addLabels("auto-generated", "selenium-test", ticketKey);
                logger.info("Labels added to PR");
            } catch (Exception e) {
                logger.warn("Could not add labels: {}", e.getMessage());
            }
            
            PRCreatedResult result = new PRCreatedResult(ticketKey, branchName);
            result.setPrUrl(pr.getHtmlUrl().toString());
            result.setPrNumber(pr.getNumber());
            
            return result;
            
        } catch (Exception e) {
            logger.error("Error creating pull request: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create pull request", e);
        }
    }
}
