package com.swag.labs.Skills.Feasibility;

import com.google.gson.JsonObject;
import com.swag.labs.Skills.Core.SkillLogger;

import java.util.HashSet;
import java.util.Set;

/**
 * Analyzes Jira tickets for automation feasibility
 * Scores tickets based on type, labels, description, and acceptance criteria
 */
public class JiraFeasibilityAnalyzer {
    private SkillLogger logger = new SkillLogger("JiraFeasibilityAnalyzer");
    
    // Ticket types that CAN be automated
    private static final Set<String> AUTOMATABLE_TYPES = new HashSet<>();
    
    // Ticket types that CANNOT be automated
    private static final Set<String> NON_AUTOMATABLE_TYPES = new HashSet<>();
    
    static {
        // Automatable types
        AUTOMATABLE_TYPES.add("Story");
        AUTOMATABLE_TYPES.add("Bug");
        AUTOMATABLE_TYPES.add("Task");
        
        // Non-automatable types
        NON_AUTOMATABLE_TYPES.add("Epic");
        NON_AUTOMATABLE_TYPES.add("Sub-task");
    }
    
    /**
     * Analyze a Jira ticket JSON for automation feasibility
     */
    public FeasibilityAnalysis analyze(JsonObject ticketJson) {
        try {
            String ticketKey = ticketJson.get("key").getAsString();
            JsonObject fields = ticketJson.getAsJsonObject("fields");
            
            logger.info("Analyzing ticket feasibility for: {}", ticketKey);
            
            // Extract ticket information
            String issueType = fields.getAsJsonObject("issuetype").get("name").getAsString();
            String summary = fields.get("summary").getAsString();
            
            // Handle description - can be string (Server) or JsonObject (Cloud ADF)
            String description = "";
            if (fields.has("description") && !fields.get("description").isJsonNull()) {
                var descField = fields.get("description");
                if (descField.isJsonPrimitive()) {
                    description = descField.getAsString();
                } else if (descField.isJsonObject()) {
                    // Cloud ADF format - just use toString or extract text
                    description = descField.toString();
                }
            }
            
            double score = 0.0;
            StringBuilder reasoning = new StringBuilder();
            
            // 1. Type-based scoring (30%)
            double typeScore = analyzeType(issueType);
            score += typeScore * 0.3;
            reasoning.append("Type Score: ").append(typeScore).append("/1.0 (").append(issueType).append(")");
            
            // 2. Description quality (30%)
            double descriptionScore = analyzeDescription(description);
            score += descriptionScore * 0.3;
            reasoning.append(" | Description Score: ").append(descriptionScore).append("/1.0");
            
            // 3. Summary analysis (40%)
            double summaryScore = analyzeSummary(summary, description);
            score += summaryScore * 0.4;
            reasoning.append(" | Summary/Content Score: ").append(summaryScore).append("/1.0");
            
            boolean automatable = score > 0.6;
            
            logger.info("Analysis result for {}: Score={}, Automatable={}", ticketKey, score, automatable);
            
            return new FeasibilityAnalysis(
                ticketKey,
                score,
                automatable,
                reasoning.toString(),
                issueType,
                summary
            );
            
        } catch (Exception e) {
            logger.error("Error analyzing ticket feasibility: {}", e.getMessage());
            throw new RuntimeException("Failed to analyze ticket feasibility", e);
        }
    }
    
    /**
     * Analyze ticket type for automation compatibility (0.0 - 1.0)
     */
    private double analyzeType(String issueType) {
        if (NON_AUTOMATABLE_TYPES.contains(issueType)) {
            logger.debug("Ticket type {} is non-automatable", issueType);
            return 0.0;
        } else if (AUTOMATABLE_TYPES.contains(issueType)) {
            logger.debug("Ticket type {} is automatable", issueType);
            return 1.0;
        }
        return 0.5; // Unknown type - neutral score
    }
    
    /**
     * Analyze description for automation indicators (0.0 - 1.0)
     */
    private double analyzeDescription(String description) {
        if (description == null || description.isEmpty()) {
            logger.debug("No description found");
            return 0.0;
        }
        
        String lowerDesc = description.toLowerCase();
        double score = 0.0;
        
        // Check for BDD keywords
        if (containsKeyword(lowerDesc, "given", "when", "then")) {
            score += 0.3;
            logger.debug("Found BDD keywords in description");
        }
        
        // Check for test scenario keywords
        if (containsKeyword(lowerDesc, "verify", "assert", "validate", "check", "ensure")) {
            score += 0.3;
            logger.debug("Found test keywords in description");
        }
        
        // Check for screen/UI keywords
        if (containsKeyword(lowerDesc, "page", "screen", "button", "field", "form", "click", "login")) {
            score += 0.2;
            logger.debug("Found UI element keywords");
        }
        
        // Check for flow keywords
        if (containsKeyword(lowerDesc, "steps", "flow", "scenario", "acceptance criteria")) {
            score += 0.2;
            logger.debug("Found flow/scenario keywords");
        }
        
        return Math.min(score, 1.0);
    }
    
    /**
     * Analyze summary for automation indicators (0.0 - 1.0)
     */
    private double analyzeSummary(String summary, String description) {
        String lowerSummary = summary.toLowerCase();
        double score = 0.0;
        
        // Check for UI/Web indicators
        if (containsKeyword(lowerSummary, "ui", "web", "page", "screen", "login", "form", "button")) {
            score += 0.4;
            logger.debug("Found UI indicators in summary");
        }
        
        // Check for automation keywords
        if (containsKeyword(lowerSummary, "automat", "test", "verify", "validate", "assert")) {
            score += 0.3;
            logger.debug("Found automation keywords");
        }
        
        // Check for non-automatable keywords
        if (containsKeyword(lowerSummary, "manual", "investigate", "spike", "research", "poc", "prototype")) {
            score -= 0.5; // Penalize non-automatable keywords
            logger.debug("Found non-automatable keywords");
        }
        
        // Check for API/Database keywords (usually not automatable without backend setup)
        if (containsKeyword(lowerSummary, "api", "database", "backend", "service", "endpoint")) {
            score -= 0.2; // Slight penalty but not absolute
            logger.debug("Found API/Database keywords");
        }
        
        return Math.max(Math.min(score, 1.0), 0.0);
    }
    
    /**
     * Check if description contains any of the given keywords
     */
    private boolean containsKeyword(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Static class to hold analysis results
     */
    public static class FeasibilityAnalysis {
        private String ticketKey;
        private double score;
        private boolean automatable;
        private String reasoning;
        private String issueType;
        private String summary;
        
        public FeasibilityAnalysis(String ticketKey, double score, boolean automatable, 
                                  String reasoning, String issueType, String summary) {
            this.ticketKey = ticketKey;
            this.score = score;
            this.automatable = automatable;
            this.reasoning = reasoning;
            this.issueType = issueType;
            this.summary = summary;
        }
        
        // Getters
        public String getTicketKey() { return ticketKey; }
        public double getScore() { return score; }
        public boolean isAutomatable() { return automatable; }
        public String getReasoning() { return reasoning; }
        public String getIssueType() { return issueType; }
        public String getSummary() { return summary; }
        public String getStatus() { return automatable ? "PASS" : "FAIL"; }
        
        @Override
        public String toString() {
            return "FeasibilityAnalysis{" +
                    "ticket='" + ticketKey + '\'' +
                    ", score=" + String.format("%.2f", score) +
                    ", automatable=" + automatable +
                    ", type='" + issueType + '\'' +
                    '}';
        }
    }
}
