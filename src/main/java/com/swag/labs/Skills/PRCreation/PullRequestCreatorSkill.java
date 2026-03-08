package com.swag.labs.Skills.PRCreation;

import com.swag.labs.Skills.Core.GeneratedTestResult;
import com.swag.labs.Skills.Core.PRCreatedResult;
import com.swag.labs.Skills.Core.SkillLogger;
import com.swag.labs.Skills.Core.TestExecutionResult;
import com.swag.labs.Utilities.ConfigurationUtils;

import java.util.Properties;

/**
 * Main Skill class for PR Creation
 * Creates feature branch, commits code, pushes to GitHub, and creates PR
 */
public class PullRequestCreatorSkill {
    private SkillLogger logger = new SkillLogger("PullRequestCreatorSkill");
    private GitOperations gitOperations;
    private PullRequestCreator prCreator;
    private ConfluencePRReporter confluenceReporter;
    private Properties config;
    
    public PullRequestCreatorSkill() {
        this.gitOperations = new GitOperations();
        this.prCreator = new PullRequestCreator();
        this.confluenceReporter = new ConfluencePRReporter();
        this.config = new ConfigurationUtils().getProperty();
    }
    
    /**
     * Main entry point: Create PR and update Confluence
     */
    public PRCreatedResult createPRAndUpdateConfluence(String jiraTicketKey, 
                                                       GeneratedTestResult testResult,
                                                       TestExecutionResult testExecResult) {
        try {
            logger.info("========== Starting PR Creation for {} ==========", jiraTicketKey);
            
            // Verify test passed
            if (!testExecResult.isPassed()) {
                logger.warn("Test did not pass, PR creation aborted");
                throw new RuntimeException("Cannot create PR for failing test");
            }
            
            // Step 1: Create feature branch
            logger.info("Step 1: Creating feature branch");
            String branchName = createBranchName(jiraTicketKey);
            gitOperations.createBranch(branchName);
            
            // Step 2: Stage changes
            logger.info("Step 2: Staging generated files");
            gitOperations.stageChanges();
            
            // Step 3: Commit changes
            logger.info("Step 3: Committing changes");
            String commitMessage = String.format("[%s] Auto-generated automated test: %s", 
                                                jiraTicketKey, testResult.getTestClassName());
            String commitHash = gitOperations.commitChanges(commitMessage);
            
            // Step 4: Push branch
            logger.info("Step 4: Pushing branch to remote");
            gitOperations.pushBranch(branchName);
            
            // Step 5: Create PR
            logger.info("Step 5: Creating pull request");
            String prDescription = generatePRDescription(jiraTicketKey, testResult, testExecResult);
            PRCreatedResult prResult = prCreator.createPullRequest(branchName, jiraTicketKey, 
                                                                   testResult.getTestClassName(), 
                                                                   prDescription);
            prResult.setCommitHash(commitHash);
            
            // Step 6: Update Confluence
            logger.info("Step 6: Updating Confluence page");
            String pageId = config.getProperty("confluence.feasibility.page.id", "");
            if (!pageId.isEmpty()) {
                try {
                    confluenceReporter.reportPRCreated(pageId, jiraTicketKey, 
                                                      prResult.getPrUrl(), prResult.getPrNumber());
                    prResult.setConfluenceUpdateStatus("SUCCESS");
                } catch (Exception e) {
                    logger.warn("Failed to update Confluence: {}", e.getMessage());
                    prResult.setConfluenceUpdateStatus("FAILED");
                }
            }
            
            logger.info("========== PR Creation Complete ==========");
            logger.info("Result: {}", prResult);
            
            return prResult;
            
        } catch (Exception e) {
            logger.error("PR creation failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create PR for ticket: " + jiraTicketKey, e);
        }
    }
    
    /**
     * Generate feature branch name from ticket key
     */
    private String createBranchName(String jiraTicketKey) {
        // Format: feature/KAN-1-short-description
        return "feature/" + jiraTicketKey.toLowerCase() + "-automated-test";
    }
    
    /**
     * Generate PR description from test information
     */
    private String generatePRDescription(String jiraTicketKey, GeneratedTestResult testResult,
                                        TestExecutionResult execResult) {
        StringBuilder description = new StringBuilder();
        
        description.append("## Auto-Generated Test\n\n");
        description.append(String.format("**Jira Ticket**: [%s](https://vikaspiprade.atlassian.net/browse/%s)\n\n", 
                                        jiraTicketKey, jiraTicketKey));
        description.append(String.format("**Test Class**: `%s`\n\n", testResult.getTestClassName()));
        
        description.append("### Test Information\n");
        description.append(String.format("- **Test File**: %s\n", testResult.getTestFilePath()));
        description.append(String.format("- **Page Objects Generated**: %d\n", testResult.getPageObjectFilePaths().size()));
        description.append(String.format("- **Test Data Files**: %d\n", testResult.getTestDataFilePaths().size()));
        
        description.append("\n### Execution Result\n");
        description.append(String.format("- **Status**: %s\n", execResult.isPassed() ? "✅ PASSED" : "❌ FAILED"));
        description.append(String.format("- **Execution Time**: %dms\n\n", execResult.getExecutionTime()));
        
        description.append("### Review Notes\n");
        description.append("- Review generated test code for correctness\n");
        description.append("- Update page object locators as needed\n");
        description.append("- Adjust assertions based on actual application behavior\n");
        description.append("- Run full test suite to ensure no regressions\n\n");
        
        description.append("---\n");
        description.append("*This PR was auto-generated by the Skill Pipeline*\n");
        
        return description.toString();
    }
}
