package com.swag.labs.Skills.Feasibility;

import com.google.gson.JsonObject;
import com.swag.labs.Skills.Core.JiraClient;
import com.swag.labs.Skills.Core.SkillContext;
import com.swag.labs.Skills.Core.SkillLogger;
import com.swag.labs.Skills.Core.FeasibilityResult;
import com.swag.labs.Utilities.ConfigurationUtils;

import java.util.Properties;

/**
 * Main Skill class for Jira Feasibility Analysis
 * Entry point for feasibility checking workflow
 */
public class JiraFeasibilitySkill {
    private SkillLogger logger = new SkillLogger("JiraFeasibilitySkill");
    private JiraClient jiraClient;
    private JiraFeasibilityAnalyzer analyzer;
    private ConfluenceFeasibilityReporter confluenceReporter;
    private Properties config;
    
    public JiraFeasibilitySkill() {
        this.jiraClient = JiraClient.getInstance();
        this.analyzer = new JiraFeasibilityAnalyzer();
        this.confluenceReporter = new ConfluenceFeasibilityReporter();
        this.config = new ConfigurationUtils().getProperty();
    }
    
    /**
     * Main entry point: Run feasibility check for a Jira ticket
     */
    public FeasibilityResult runFeasibilityCheck(String jiraTicketKey) {
        try {
            logger.info("========== Starting Feasibility Check for {} ==========", jiraTicketKey);
            
            // Step 1: Fetch ticket from Jira
            logger.info("Step 1: Fetching Jira ticket");
            JsonObject ticketJson = jiraClient.getTicketByKey(jiraTicketKey);
            
            // Step 2: Analyze feasibility
            logger.info("Step 2: Analyzing ticket for automation feasibility");
            JiraFeasibilityAnalyzer.FeasibilityAnalysis analysis = analyzer.analyze(ticketJson);
            
            // Step 3: Update Confluence page with report
            logger.info("Step 3: Updating Confluence page with feasibility report");
            String pageId = config.getProperty("confluence.feasibility.page.id", "");
            if (!pageId.isEmpty()) {
                try {
                    confluenceReporter.reportFeasibility(pageId, analysis);
                    logger.info("Confluence page updated successfully");
                } catch (Exception e) {
                    logger.warn("Failed to update Confluence page (continuing anyway): {}", e.getMessage());
                }
            } else {
                logger.warn("Confluence page ID not configured, skipping Confluence update");
            }
            
            // Step 4: Create and return result
            FeasibilityResult result = new FeasibilityResult(
                analysis.getTicketKey(),
                analysis.getScore(),
                analysis.isAutomatable(),
                analysis.getReasoning(),
                analysis.getIssueType()
            );
            
            logger.info("========== Feasibility Check Complete ==========");
            logger.info("Result: {}", result);
            
            return result;
            
        } catch (Exception e) {
            logger.error("Feasibility check failed: {}", e.getMessage(), e);
            throw new RuntimeException("Feasibility check failed for ticket: " + jiraTicketKey, e);
        }
    }
    
    /**
     * Run feasibility check with context
     */
    public FeasibilityResult runFeasibilityCheck(SkillContext context) {
        return runFeasibilityCheck(context.getJiraTicketKey());
    }
}
