package com.swag.labs.Skills.TestRunner;

import com.swag.labs.Skills.Core.SkillLogger;
import com.swag.labs.Skills.Core.TestExecutionResult;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Runs tests using Maven/TestNG and captures results
 */
public class SmartTestRunner {
    private SkillLogger logger = new SkillLogger("SmartTestRunner");
    
    public TestExecutionResult runTest(String testClassName, String testMethodName) {
        try {
            logger.info("Running test: {}.{}", testClassName, testMethodName);
            
            TestExecutionResult result = new TestExecutionResult(testClassName, testMethodName);
            long startTime = System.currentTimeMillis();
            
            // Build Maven test command
            String mvnCommand = String.format(
                "mvn test -Dtest=%s#%s -q",
                testClassName,
                testMethodName
            );
            
            logger.info("Executing: {}", mvnCommand);
            
            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", mvnCommand);
            pb.directory(new java.io.File(System.getProperty("user.dir")));
            Process process = pb.start();
            
            // Capture output
            StringBuilder output = new StringBuilder();
            StringBuilder errorOutput = new StringBuilder();
            
            try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            
            try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    errorOutput.append(line).append("\n");
                }
            }
            
            // Wait for process to complete
            int exitCode = process.waitFor();
            long executionTime = System.currentTimeMillis() - startTime;
            
            result.setExecutionTime(executionTime);
            
            if (exitCode == 0) {
                result.setPassed(true);
                logger.info("Test passed in {} ms", executionTime);
            } else {
                result.setPassed(false);
                result.setErrorMessage("Test execution failed with exit code: " + exitCode);
                result.setStackTrace(errorOutput.toString());
                logger.error("Test failed with exit code: {}", exitCode);
            }
            
            return result;
            
        } catch (Exception e) {
            logger.error("Error running test: {}", e.getMessage(), e);
            TestExecutionResult result = new TestExecutionResult(testClassName, testMethodName);
            result.setPassed(false);
            result.setErrorMessage(e.getMessage());
            result.setStackTrace(e.getStackTrace().toString());
            return result;
        }
    }
}
