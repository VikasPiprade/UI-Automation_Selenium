package com.swag.labs.Skills.Feasibility;

import com.google.gson.JsonObject;
import com.swag.labs.Skills.Core.ConfluenceClient;
import com.swag.labs.Skills.Core.SkillContext;
import com.swag.labs.Skills.Core.SkillLogger;

/**
 * Updates Confluence page with feasibility analysis results
 */
public class ConfluenceFeasibilityReporter {
    private SkillLogger logger = new SkillLogger("ConfluenceFeasibilityReporter");
    private ConfluenceClient confluenceClient;
    
    public ConfluenceFeasibilityReporter() {
        this.confluenceClient = ConfluenceClient.getInstance();
    }
    
    /**
     * Update Confluence page with feasibility analysis
     */
    public void reportFeasibility(String pageId, JiraFeasibilityAnalyzer.FeasibilityAnalysis analysis) {
        try {
            logger.info("Updating Confluence page {} with feasibility report for: {}", pageId, analysis.getTicketKey());
            
            String reportContent = generateReportHTML(analysis);
            confluenceClient.appendToPage(pageId, reportContent);
            
            logger.info("Successfully updated Confluence page with feasibility report");
        } catch (Exception e) {
            // Log warning but don't fail the entire pipeline if Confluence update fails
            logger.warn("Could not update Confluence with feasibility report: {}. Pipeline will continue.", e.getMessage());
            logger.debug("Stack trace: ", e);
            // Don't throw exception - allow pipeline to continue
        }
    }
    
    /**
     * Generate HTML content for the report
     */
    private String generateReportHTML(JiraFeasibilityAnalyzer.FeasibilityAnalysis analysis) {
        String statusBadge = analysis.isAutomatable() ? 
            "<span style='background-color: #36b37e; color: white; padding: 3px 8px; border-radius: 3px; font-weight: bold;'>PASS</span>" :
            "<span style='background-color: #ae2a19; color: white; padding: 3px 8px; border-radius: 3px; font-weight: bold;'>FAIL</span>";
        
        String scoreColor = analysis.getScore() > 0.6 ? "#36b37e" : "#ae2a19";
        
        return String.format(
            "<ac:structured-macro ac:name='table'>" +
            "<ac:parameter name='data-macro-table'>" +
            "<tr>" +
            "<td style='background-color: #f5f5f5;'><strong>Ticket</strong></td>" +
            "<td>%s</td>" +
            "</tr>" +
            "<tr>" +
            "<td style='background-color: #f5f5f5;'><strong>Summary</strong></td>" +
            "<td>%s</td>" +
            "</tr>" +
            "<tr>" +
            "<td style='background-color: #f5f5f5;'><strong>Type</strong></td>" +
            "<td>%s</td>" +
            "</tr>" +
            "<tr>" +
            "<td style='background-color: #f5f5f5;'><strong>Feasibility Score</strong></td>" +
            "<td style='color: %s; font-weight: bold;'>%.2f</td>" +
            "</tr>" +
            "<tr>" +
            "<td style='background-color: #f5f5f5;'><strong>Status</strong></td>" +
            "<td>%s</td>" +
            "</tr>" +
            "<tr>" +
            "<td style='background-color: #f5f5f5;'><strong>Analysis</strong></td>" +
            "<td><p style='font-size: 12px;'>%s</p></td>" +
            "</tr>" +
            "<tr>" +
            "<td style='background-color: #f5f5f5;'><strong>Timestamp</strong></td>" +
            "<td>%s</td>" +
            "</tr>" +
            "</ac:parameter>" +
            "</ac:structured-macro>",
            analysis.getTicketKey(),
            analysis.getSummary(),
            analysis.getIssueType(),
            scoreColor,
            analysis.getScore(),
            statusBadge,
            analysis.getReasoning(),
            new java.util.Date().toString()
        );
    }
}
