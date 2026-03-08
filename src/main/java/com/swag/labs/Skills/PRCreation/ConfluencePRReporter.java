package com.swag.labs.Skills.PRCreation;

import com.swag.labs.Skills.Core.SkillLogger;
import com.swag.labs.Skills.Core.ConfluenceClient;

/**
 * Updates Confluence with PR creation status
 */
public class ConfluencePRReporter {
    private SkillLogger logger = new SkillLogger("ConfluencePRReporter");
    private ConfluenceClient confluenceClient;
    
    public ConfluencePRReporter() {
        this.confluenceClient = ConfluenceClient.getInstance();
    }
    
    /**
     * Update Confluence page with PR information
     */
    public void reportPRCreated(String pageId, String ticketKey, String prUrl, int prNumber) {
        try {
            logger.info("Updating Confluence page with PR information");
            
            String updateContent = String.format(
                "<p><strong>[%s] PR Created</strong><br/>" +
                "PR #%d: <a href='%s'>%s</a><br/>" +
                "Status: <span style='background-color: #36b37e; color: white; padding: 2px 5px; border-radius: 2px;'>COMPLETE</span><br/>" +
                "Updated: %s</p>",
                ticketKey, prNumber, prUrl, prUrl,
                new java.util.Date()
            );
            
            confluenceClient.appendToPage(pageId, updateContent);
            logger.info("Confluence page updated with PR information");
            
        } catch (Exception e) {
            logger.error("Error updating Confluence page: {}", e.getMessage());
            throw new RuntimeException("Failed to update Confluence with PR information", e);
        }
    }
}
