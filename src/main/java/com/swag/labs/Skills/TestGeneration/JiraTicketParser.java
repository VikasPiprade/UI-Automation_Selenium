package com.swag.labs.Skills.TestGeneration;

import com.google.gson.JsonObject;
import com.swag.labs.Skills.Core.SkillLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses Jira ticket data for test generation context
 */
public class JiraTicketParser {
    private SkillLogger logger = new SkillLogger("JiraTicketParser");
    
    public TestGenerationContext parseTicket(JsonObject ticketJson) {
        try {
            String ticketKey = ticketJson.get("key").getAsString();
            logger.info("Parsing ticket: {}", ticketKey);
            
            JsonObject fields = ticketJson.getAsJsonObject("fields");
            
            TestGenerationContext context = new TestGenerationContext(ticketKey);
            
            // Extract basic fields
            if (fields.has("summary") && !fields.get("summary").isJsonNull()) {
                context.setSummary(fields.get("summary").getAsString());
            }
            
            // Description in Jira Cloud API v3 is in ADF (Atlassian Document Format)
            String description = "";
            if (fields.has("description") && !fields.get("description").isJsonNull()) {
                try {
                    // Try to get as string first (for simple cases)
                    if (fields.get("description").isJsonPrimitive()) {
                        description = fields.get("description").getAsString();
                    } else if (fields.get("description").isJsonObject()) {
                        // ADF format - try to extract text content
                        description = extractTextFromADF(fields.getAsJsonObject("description"));
                    }
                } catch (Exception e) {
                    logger.warn("Could not parse description: {}", e.getMessage());
                }
            }
            context.setDescription(description);
            
            // Extract issue type
            String issueType = fields.getAsJsonObject("issuetype").get("name").getAsString();
            context.setIssueType(issueType);
            
            // Extract labels
            if (fields.get("labels") != null && fields.get("labels").isJsonArray()) {
                List<String> labels = new ArrayList<>();
                fields.get("labels").getAsJsonArray().forEach(label -> {
                    labels.add(label.getAsString());
                });
                context.setLabels(labels);
            }
            
            // Extract epic link if exists
            if (fields.has("customfield_10000") && !fields.get("customfield_10000").isJsonNull()) {
                // Epic link is typically a custom field
                context.setEpicName("GeneralEpic");
            } else {
                context.setEpicName("GeneralEpic");
            }
            
            // Parse acceptance criteria from description
            extractAcceptanceCriteria(context);
            
            logger.debug("Parsed ticket context: {}", context);
            return context;
            
        } catch (Exception e) {
            logger.error("Error parsing Jira ticket: {}", e.getMessage());
            throw new RuntimeException("Failed to parse Jira ticket", e);
        }
    }
    
    /**
     * Extract plain text from Jira ADF (Atlassian Document Format)
     */
    private String extractTextFromADF(JsonObject adfObject) {
        StringBuilder text = new StringBuilder();
        try {
            if (adfObject.has("content") && adfObject.get("content").isJsonArray()) {
                adfObject.get("content").getAsJsonArray().forEach(element -> {
                    if (element.isJsonObject()) {
                        JsonObject obj = element.getAsJsonObject();
                        if (obj.has("content") && obj.get("content").isJsonArray()) {
                            obj.get("content").getAsJsonArray().forEach(innerElement -> {
                                if (innerElement.isJsonObject()) {
                                    JsonObject inner = innerElement.getAsJsonObject();
                                    if (inner.has("text")) {
                                        text.append(inner.get("text").getAsString()).append(" ");
                                    }
                                }
                            });
                        }
                    }
                });
            }
        } catch (Exception e) {
            logger.warn("Error extracting text from ADF: {}", e.getMessage());
        }
        return text.toString().trim();
    }
    
    /**
     * Extract acceptance criteria from description
     */
    private void extractAcceptanceCriteria(TestGenerationContext context) {
        String description = context.getDescription();
        if (description == null || description.isEmpty()) {
            // If no description, create a default AC from summary
            context.addAcceptanceCriterion("Verify that " + context.getSummary() + " works as expected");
            return;
        }
        
        // Look for numbered items or bullet points
        String[] lines = description.split("\n");
        int acCount = 0;
        for (String line : lines) {
            String trimmed = line.trim();
            // Look for lines like: "1. ", "- ", "* " or containing "Given/When/Then"
            if ((trimmed.matches("^\\d+\\.\\s.*") || 
                 trimmed.startsWith("- ") || 
                 trimmed.startsWith("* ")) &&
                (trimmed.toLowerCase().contains("verify") || 
                 trimmed.toLowerCase().contains("assert") ||
                 trimmed.toLowerCase().contains("should") ||
                 trimmed.toLowerCase().contains("given") ||
                 trimmed.toLowerCase().contains("when") ||
                 trimmed.toLowerCase().contains("then"))) {
                String ac = trimmed.replaceAll("^[\\d\\-\\*\\.\\s]+", "").trim();
                if (!ac.isEmpty()) {
                    context.addAcceptanceCriterion(ac);
                    acCount++;
                }
            }
        }
        
        // If no AC found, create default
        if (acCount == 0) {
            context.addAcceptanceCriterion("Verify that " + context.getSummary() + " works as expected");
        }
    }
    
    /**
     * Context object for test generation
     */
    public static class TestGenerationContext {
        private String ticketKey;
        private String summary;
        private String description;
        private String issueType;
        private String epicName;
        private List<String> labels = new ArrayList<>();
        private List<String> acceptanceCriteria = new ArrayList<>();
        private List<String> pageObjectsNeeded = new ArrayList<>();
        
        public TestGenerationContext(String ticketKey) {
            this.ticketKey = ticketKey;
        }
        
        // Getters and Setters
        public String getTicketKey() { return ticketKey; }
        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getIssueType() { return issueType; }
        public void setIssueType(String issueType) { this.issueType = issueType; }
        
        public String getEpicName() { return epicName; }
        public void setEpicName(String epicName) { this.epicName = epicName; }
        
        public List<String> getLabels() { return labels; }
        public void setLabels(List<String> labels) { this.labels = labels; }
        
        public List<String> getAcceptanceCriteria() { return acceptanceCriteria; }
        public void addAcceptanceCriterion(String criterion) { 
            this.acceptanceCriteria.add(criterion); 
        }
        
        public List<String> getPageObjectsNeeded() { return pageObjectsNeeded; }
        public void addPageObjectNeeded(String pageObject) { 
            if (!pageObjectsNeeded.contains(pageObject)) {
                pageObjectsNeeded.add(pageObject);
            }
        }
        
        public String getTestClassName() {
            return "Test_" + epicName + "_" + ticketKey.replace("-", "");
        }
        
        @Override
        public String toString() {
            return "TestGenerationContext{" +
                    "ticketKey='" + ticketKey + '\'' +
                    ", epicName='" + epicName + '\'' +
                    ", testClassName='" + getTestClassName() + '\'' +
                    ", acceptanceCriteria=" + acceptanceCriteria.size() + " items" +
                    '}';
        }
    }
}
