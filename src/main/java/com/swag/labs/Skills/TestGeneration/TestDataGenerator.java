package com.swag.labs.Skills.TestGeneration;

import com.swag.labs.Skills.Core.SkillLogger;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Generates test data files from Jira ticket information
 */
public class TestDataGenerator {
    private SkillLogger logger = new SkillLogger("TestDataGenerator");
    private static final String TEST_DATA_DIR = "src/test/resources/TestData";
    
    public String generateTestDataFile(JiraTicketParser.TestGenerationContext context) {
        try {
            logger.info("Generating test data for ticket: {}", context.getTicketKey());
            
            String dataDir = TEST_DATA_DIR + "/" + context.getEpicName();
            String fileName = context.getTicketKey() + "_testdata.csv";
            String filePath = dataDir + "/" + fileName;
            
            File dataFile = new File(System.getProperty("user.dir") + File.separator + filePath);
            dataFile.getParentFile().mkdirs();
            
            String csvContent = generateCSVContent(context);
            Files.write(Paths.get(dataFile.getAbsolutePath()), csvContent.getBytes());
            
            logger.info("Test data file generated at: {}", dataFile.getAbsolutePath());
            return dataFile.getAbsolutePath();
            
        } catch (Exception e) {
            logger.error("Error generating test data file: {}", e.getMessage());
            throw new RuntimeException("Failed to generate test data file", e);
        }
    }
    
    /**
     * Generate CSV content with sample data
     */
    private String generateCSVContent(JiraTicketParser.TestGenerationContext context) {
        StringBuilder csv = new StringBuilder();
        
        // CSV Header
        csv.append("testScenario,input,expectedOutput,description\n");
        
        // Sample data rows based on acceptance criteria
        int index = 1;
        for (String criterion : context.getAcceptanceCriteria()) {
            csv.append("Scenario").append(index).append(",");
            csv.append("sample_input,");
            csv.append("expected_output,");
            csv.append(criterion.replace(",", " ")).append("\n");
            index++;
        }
        
        return csv.toString();
    }
}
