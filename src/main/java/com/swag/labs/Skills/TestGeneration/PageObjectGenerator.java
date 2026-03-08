package com.swag.labs.Skills.TestGeneration;

import com.swag.labs.Skills.Core.SkillLogger;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Generates dynamic Page Object classes for tests
 */
public class PageObjectGenerator {
    private SkillLogger logger = new SkillLogger("PageObjectGenerator");
    private static final String PAGE_OBJECT_PACKAGE = "com.swag.labs.PageObjects";
    private static final String PAGE_OBJECT_DIR = "src/main/java/com/swag/labs/PageObjects";
    
    public String generatePageObject(String pageObjectName, JiraTicketParser.TestGenerationContext context) {
        try {
            logger.info("Generating page object: {}", pageObjectName);
            
            String className = "Generated_" + pageObjectName;
            String pageObjectCode = buildPageObjectCode(className, pageObjectName, context);
            
            // Write to file
            String filePath = PAGE_OBJECT_DIR + "/" + className + ".java";
            File pageObjectFile = new File(System.getProperty("user.dir") + File.separator + filePath);
            pageObjectFile.getParentFile().mkdirs();
            Files.write(Paths.get(pageObjectFile.getAbsolutePath()), pageObjectCode.getBytes());
            
            logger.info("Page object generated successfully at: {}", pageObjectFile.getAbsolutePath());
            return pageObjectFile.getAbsolutePath();
            
        } catch (Exception e) {
            logger.error("Error generating page object: {}", e.getMessage());
            throw new RuntimeException("Failed to generate page object", e);
        }
    }
    
    /**
     * Build the Java page object class source code
     */
    private String buildPageObjectCode(String className, String pageObjectName, JiraTicketParser.TestGenerationContext context) {
        StringBuilder code = new StringBuilder();
        
        // Package declaration
        code.append("package ").append(PAGE_OBJECT_PACKAGE).append(";\n\n");
        
        // Imports
        code.append("import org.openqa.selenium.WebDriver;\n");
        code.append("import org.openqa.selenium.WebElement;\n");
        code.append("import org.openqa.selenium.By;\n");
        code.append("import com.swag.labs.Utilities.BasePage;\n");
        code.append("import org.apache.logging.log4j.LogManager;\n");
        code.append("import org.apache.logging.log4j.Logger;\n");
        code.append("\n");
        
        // Class JavaDoc
        code.append("/**\n");
        code.append(" * Auto-generated Page Object for: ").append(pageObjectName).append("\n");
        code.append(" * Jira Ticket: ").append(context.getTicketKey()).append("\n");
        code.append(" * \n");
        code.append(" * NOTE: Review and update the locators manually as they are auto-generated\n");
        code.append(" * with placeholder XPath values.\n");
        code.append(" * Generated timestamp: ").append(new java.util.Date()).append("\n");
        code.append(" */\n");
        
        // Class declaration
        code.append("public class ").append(className).append(" extends BasePage {\n");
        code.append("    private static final Logger logger = LogManager.getLogger(").append(className).append(".class);\n\n");
        
        // Locators (placeholder examples)
        code.append("    // TODO: Update these locators based on actual page elements\n");
        code.append("    private static final By HEADER = By.xpath(\"//h1[@class='page-header']\");\n");
        code.append("    private static final By SUBMIT_BUTTON = By.id(\"submit-btn\");\n");
        code.append("    private static final By FORM_FIELD = By.name(\"inputField\");\n\n");
        
        // Constructor
        code.append("    public ").append(className).append("(WebDriver driver, Logger logger) {\n");
        code.append("        super(driver, logger);\n");
        code.append("        logger.debug(\"Initializing ").append(className).append("\");\n");
        code.append("    }\n\n");
        
        // Example methods
        code.append("    /**\n");
        code.append("     * Verify page is loaded by checking header visibility\n");
        code.append("     */\n");
        code.append("    public boolean isPageLoaded() {\n");
        code.append("        try {\n");
        code.append("            WebElement header = waitForElementVisible(driver.findElement(HEADER));\n");
        code.append("            return header != null;\n");
        code.append("        } catch (Exception e) {\n");
        code.append("            log.debug(\"Page not loaded: {}\", e.getMessage());\n");
        code.append("            return false;\n");
        code.append("        }\n");
        code.append("    }\n\n");
        
        code.append("    /**\n");
        code.append("     * Example method to interact with form field\n");
        code.append("     */\n");
        code.append("    public void fillFormField(String value) {\n");
        code.append("        log.info(\"Filling form field with: {}\", value);\n");
        code.append("        WebElement field = waitForElementPresence(FORM_FIELD);\n");
        code.append("        if (field != null) {\n");
        code.append("            field.clear();\n");
        code.append("            field.sendKeys(value);\n");
        code.append("        }\n");
        code.append("    }\n\n");
        
        code.append("    /**\n");
        code.append("     * Example method to click submit button\n");
        code.append("     */\n");
        code.append("    public void clickSubmit() {\n");
        code.append("        log.info(\"Clicking submit button\");\n");
        code.append("        WebElement button = driver.findElement(SUBMIT_BUTTON);\n");
        code.append("        waitForElementClickable(button).click();\n");
        code.append("    }\n");
        
        code.append("}\n");
        
        return code.toString();
    }
}
