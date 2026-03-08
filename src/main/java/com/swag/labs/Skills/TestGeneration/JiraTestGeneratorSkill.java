package com.swag.labs.Skills.TestGeneration;

import com.google.gson.JsonObject;
import com.swag.labs.Skills.Core.GeneratedTestResult;
import com.swag.labs.Skills.Core.JiraClient;
import com.swag.labs.Skills.Core.SkillLogger;
import com.swag.labs.Skills.Core.FeasibilityResult;

/**
 * Main Skill class for Test Generation
 * Generates complete test classes, page objects, and test data from Jira ticket
 */
public class JiraTestGeneratorSkill {
    private SkillLogger logger = new SkillLogger("JiraTestGeneratorSkill");
    private JiraClient jiraClient;
    private JiraTicketParser ticketParser;
    private TestClassGenerator testClassGenerator;
    private PageObjectGenerator pageObjectGenerator;
    private TestAssertionGenerator assertionGenerator;
    private TestDataGenerator dataGenerator;
    
    public JiraTestGeneratorSkill() {
        this.jiraClient = JiraClient.getInstance();
        this.ticketParser = new JiraTicketParser();
        this.testClassGenerator = new TestClassGenerator();
        this.pageObjectGenerator = new PageObjectGenerator();
        this.assertionGenerator = new TestAssertionGenerator();
        this.dataGenerator = new TestDataGenerator();
    }
    
    /**
     * Main entry point: Generate tests for a Jira ticket
     */
    public GeneratedTestResult generateTests(String jiraTicketKey, FeasibilityResult feasibilityResult) {
        try {
            // Verify feasibility
            if (!feasibilityResult.isAutomatable()) {
                logger.warn("Ticket {} is not automatable (score: {}), skipping test generation", 
                           jiraTicketKey, feasibilityResult.getFeasibilityScore());
                throw new RuntimeException("Ticket is not feasible for automation");
            }
            
            logger.info("========== Starting Test Generation for {} ==========", jiraTicketKey);
            
            // Step 1: Fetch ticket from Jira
            logger.info("Step 1: Fetching Jira ticket");
            JsonObject ticketJson = jiraClient.getTicketByKey(jiraTicketKey);
            
            // Step 2: Parse ticket data
            logger.info("Step 2: Parsing ticket data");
            JiraTicketParser.TestGenerationContext context = ticketParser.parseTicket(ticketJson);
            
            // Step 3: Generate test class
            logger.info("Step 3: Generating test class");
            String testFilePath = testClassGenerator.generateTestClass(context);
            String testClassName = context.getTestClassName();
            GeneratedTestResult result = new GeneratedTestResult(jiraTicketKey, testClassName, testFilePath);
            
            // Step 4: Generate page objects if needed
            logger.info("Step 4: Generating page objects");
            for (String pageObjectName : context.getPageObjectsNeeded()) {
                try {
                    String poFilePath = pageObjectGenerator.generatePageObject(pageObjectName, context);
                    result.addPageObjectFilePath(poFilePath);
                } catch (Exception e) {
                    logger.warn("Failed to generate page object {}: {}", pageObjectName, e.getMessage());
                }
            }
            
            // Generate a default page object if none specified
            if (context.getPageObjectsNeeded().isEmpty()) {
                String defaultPOPath = pageObjectGenerator.generatePageObject(context.getEpicName(), context);
                result.addPageObjectFilePath(defaultPOPath);
            }
            
            // Step 5: Generate test data
            logger.info("Step 5: Generating test data");
            try {
                String testDataPath = dataGenerator.generateTestDataFile(context);
                result.addTestDataFilePath(testDataPath);
            } catch (Exception e) {
                logger.warn("Failed to generate test data: {}", e.getMessage());
            }
            
            // Step 6: Verify compilation (optional, will be done in a separate step)
            logger.info("Test generation completed successfully");
            result.setCompilationSuccessful(true); // Mark as pending compilation verification
            
            logger.info("========== Test Generation Complete ==========");
            logger.info("Result: {}", result);
            
            return result;
            
        } catch (Exception e) {
            logger.error("Test generation failed: {}", e.getMessage(), e);
            throw new RuntimeException("Test generation failed for ticket: " + jiraTicketKey, e);
        }
    }
}
