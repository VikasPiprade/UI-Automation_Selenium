package com.swag.labs.Skills.TestGeneration;

import com.swag.labs.Skills.Core.SkillLogger;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Generates dynamic Java test classes from Jira ticket context
 */
public class TestClassGenerator {
    private SkillLogger logger = new SkillLogger("TestClassGenerator");
    private static final String TEST_PACKAGE = "com.swag.labs.Tests";
    private static final String TEST_DIR = "src/test/java/com/swag/labs/Tests";
    
    public String generateTestClass(JiraTicketParser.TestGenerationContext context) {
        try {
            logger.info("Generating test class for ticket: {}", context.getTicketKey());
            
            String className = context.getTestClassName();
            String testCode = buildTestClassCode(className, context);
            
            // Write to file
            String filePath = TEST_DIR + "/" + className + ".java";
            File testFile = new File(System.getProperty("user.dir") + File.separator + filePath);
            testFile.getParentFile().mkdirs();
            Files.write(Paths.get(testFile.getAbsolutePath()), testCode.getBytes());
            
            logger.info("Test class generated successfully at: {}", testFile.getAbsolutePath());
            return testFile.getAbsolutePath();
            
        } catch (Exception e) {
            logger.error("Error generating test class: {}", e.getMessage());
            throw new RuntimeException("Failed to generate test class", e);
        }
    }
    
    /**
     * Build the Java test class source code
     */
    private String buildTestClassCode(String className, JiraTicketParser.TestGenerationContext context) {
        StringBuilder code = new StringBuilder();
        
        // Package declaration
        code.append("package ").append(TEST_PACKAGE).append(";\n\n");
        
        // Imports
        code.append("import org.testng.annotations.Test;\n");
        code.append("import org.testng.annotations.BeforeMethod;\n");
        code.append("import org.testng.annotations.AfterMethod;\n");
        code.append("import com.swag.labs.BaseComponents.BaseTest;\n");
        code.append("import com.swag.labs.Utils.AssertionUtils;\n");
        code.append("import com.aventstack.extentreports.Status;\n");
        code.append("import org.apache.logging.log4j.LogManager;\n");
        code.append("import org.apache.logging.log4j.Logger;\n");
        code.append("\n");
        
        // Class JavaDoc
        code.append("/**\n");
        code.append(" * Auto-generated test class from Jira ticket: ").append(context.getTicketKey()).append("\n");
        code.append(" * Summary: ").append(context.getSummary()).append("\n");
        code.append(" * Generated timestamp: ").append(new java.util.Date()).append("\n");
        code.append(" */\n");
        
        // Class declaration
        code.append("public class ").append(className).append(" extends BaseTest {\n");
        code.append("    private static final Logger logger = LogManager.getLogger(").append(className).append(".class);\n\n");
        
        // Setup method
        code.append("    @BeforeMethod\n");
        code.append("    public void setup() {\n");
        code.append("        logger.info(\"Setting up test for: ").append(context.getSummary()).append("\");\n");
        code.append("    }\n\n");
        
        // Generate test methods from acceptance criteria
        List<String> acceptanceCriteria = context.getAcceptanceCriteria();
        for (int i = 0; i < acceptanceCriteria.size(); i++) {
            String ac = acceptanceCriteria.get(i);
            String testMethodName = "testScenario" + (i + 1);
            
            code.append("    /**\n");
            code.append("     * Test Scenario: ").append(ac).append("\n");
            code.append("     * Jira Ticket: ").append(context.getTicketKey()).append("\n");
            code.append("     */\n");
            code.append("    @Test(groups = {\"smoke\", \"").append(context.getEpicName()).append("\"})\n");
            code.append("    public void ").append(testMethodName).append("() {\n");
            code.append("        logger.info(\"Executing test scenario: ").append(ac).append("\");\n");
            code.append("        \n");
            code.append("        // TODO: Implement test logic for acceptance criterion\n");
            code.append("        // ").append(ac).append("\n");
            code.append("        \n");
            code.append("        // Example assertion\n");
            code.append("        // AssertionUtils.assertTrue(actualValue, \"Verify: ").append(ac.replace("\"", "'")).append("\");\n");
            code.append("    }\n\n");
        }
        
        // Cleanup method
        code.append("    @AfterMethod\n");
        code.append("    public void cleanup() {\n");
        code.append("        logger.info(\"Cleaning up after test\");\n");
        code.append("    }\n");
        
        code.append("}\n");
        
        return code.toString();
    }
}
