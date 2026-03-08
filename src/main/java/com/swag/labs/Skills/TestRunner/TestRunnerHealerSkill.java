package com.swag.labs.Skills.TestRunner;

import com.swag.labs.Skills.Core.SkillLogger;
import com.swag.labs.Skills.Core.TestExecutionResult;
import com.swag.labs.Utilities.ConfigurationUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Main Skill class for Test Execution with Healing/Retry Logic
 * Attempts to run, fix, and re-run failing tests up to 3 times
 */
public class TestRunnerHealerSkill {
    private SkillLogger logger = new SkillLogger("TestRunnerHealerSkill");
    private SmartTestRunner testRunner;
    private TestHealer testHealer;
    private Properties config;
    private static final int DEFAULT_MAX_RETRIES = 3;
    private int maxRetries;
    
    public TestRunnerHealerSkill() {
        this.testRunner = new SmartTestRunner();
        this.testHealer = new TestHealer();
        this.config = new ConfigurationUtils().getProperty();
        this.maxRetries = Integer.parseInt(config.getProperty("skills.max-retry-attempts", 
                                          String.valueOf(DEFAULT_MAX_RETRIES)));
    }
    
    /**
     * Main entry point: Run test with retry/healing logic
     */
    public TestExecutionResult runAndHealTest(String testClassName, String testMethodName) {
        try {
            logger.info("========== Starting Test Runner/Healer for {}.{} ==========", 
                       testClassName, testMethodName);
            
            List<TestExecutionResult> allAttempts = new ArrayList<>();
            TestExecutionResult result = null;
            
            // Retry loop
            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                logger.info("---- Attempt {} of {} ----", attempt, maxRetries);
                
                // Run test
                result = testRunner.runTest(testClassName, testMethodName);
                result.setAttemptNumber(attempt);
                allAttempts.add(result);
                
                // Check result
                if (result.isPassed()) {
                    logger.info("Test PASSED on attempt {}", attempt);
                    logAllAttempts(allAttempts);
                    return result;
                } else {
                    logger.warn("Test FAILED on attempt {} - Error: {}", attempt, result.getErrorMessage());
                    
                    // If not last attempt, try to heal
                    if (attempt < maxRetries) {
                        String fixStrategy = testHealer.attemptFix(testClassName, result.getErrorMessage(), attempt);
                        logger.info("Applied fix strategy: {}", fixStrategy);
                        
                        // Small delay before retry
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }
            
            // All attempts failed
            logger.error("Test FAILED after all {} attempts", maxRetries);
            logAllAttempts(allAttempts);
            
            return result;
            
        } catch (Exception e) {
            logger.error("Test runner/healer failed: {}", e.getMessage(), e);
            TestExecutionResult errorResult = new TestExecutionResult(testClassName, testMethodName);
            errorResult.setPassed(false);
            errorResult.setErrorMessage(e.getMessage());
            return errorResult;
        }
    }
    
    /**
     * Log summary of all attempts
     */
    private void logAllAttempts(List<TestExecutionResult> attempts) {
        logger.info("========== Test Execution Summary ==========");
        for (TestExecutionResult attempt : attempts) {
            logger.info("Attempt {}: {} ({}ms)", 
                       attempt.getAttemptNumber(),
                       attempt.isPassed() ? "PASSED" : "FAILED",
                       attempt.getExecutionTime());
            if (!attempt.isPassed()) {
                logger.debug("  Error: {}", attempt.getErrorMessage());
            }
        }
        logger.info("==========================================");
    }
}
