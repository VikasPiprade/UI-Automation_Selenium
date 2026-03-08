package com.swag.labs.Skills.TestGeneration;

import com.swag.labs.Skills.Core.SkillLogger;

/**
 * Generates test assertions from acceptance criteria
 */
public class TestAssertionGenerator {
    private SkillLogger logger = new SkillLogger("TestAssertionGenerator");
    
    public String generateAssertionFromCriterion(String criterion) {
        try {
            logger.debug("Generating assertion for: {}", criterion);
            String lowerCriterion = criterion.toLowerCase();
            
            // Pattern matching for different types of assertions
            if (lowerCriterion.contains("visible") || lowerCriterion.contains("displayed")) {
                return generateVisibilityAssertion(criterion);
            } else if (lowerCriterion.contains("equal") || lowerCriterion.contains("match")) {
                return generateEqualityAssertion(criterion);
            } else if (lowerCriterion.contains("verify") || lowerCriterion.contains("assert")) {
                return generateGenericAssertion(criterion);
            } else {
                return generateGenericAssertion(criterion);
            }
        } catch (Exception e) {
            logger.error("Error generating assertion: {}", e.getMessage());
            return "// TODO: Add assertion for: " + criterion;
        }
    }
    
    private String generateVisibilityAssertion(String criterion) {
        return "AssertionUtils.assertTrue(element.isDisplayed(), \\\"Verify: " + criterion + "\\\");";
    }
    
    private String generateEqualityAssertion(String criterion) {
        return "AssertionUtils.assertEquals(expectedValue, actualValue, \\\"Verify: " + criterion + "\\\");";
    }
    
    private String generateGenericAssertion(String criterion) {
        return "AssertionUtils.assertTrue(true, \\\"Verify: " + criterion + "\\\");";
    }
}
