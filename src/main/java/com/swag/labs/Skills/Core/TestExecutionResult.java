package com.swag.labs.Skills.Core;

import java.io.Serializable;

/**
 * Result object for Test Execution skill
 */
public class TestExecutionResult implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String testClassName;
    private String testMethodName;
    private boolean passed;
    private String errorMessage;
    private String stackTrace;
    private long executionTime;
    private int attemptNumber;
    private String screenshotPath;
    
    public TestExecutionResult(String testClassName, String testMethodName) {
        this.testClassName = testClassName;
        this.testMethodName = testMethodName;
        this.executionTime = 0;
        this.attemptNumber = 1;
    }
    
    public String getTestClassName() { return testClassName; }
    public String getTestMethodName() { return testMethodName; }
    public boolean isPassed() { return passed; }
    public String getErrorMessage() { return errorMessage; }
    public String getStackTrace() { return stackTrace; }
    public long getExecutionTime() { return executionTime; }
    public int getAttemptNumber() { return attemptNumber; }
    public String getScreenshotPath() { return screenshotPath; }
    
    public void setPassed(boolean passed) { this.passed = passed; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public void setStackTrace(String stackTrace) { this.stackTrace = stackTrace; }
    public void setExecutionTime(long executionTime) { this.executionTime = executionTime; }
    public void setAttemptNumber(int attemptNumber) { this.attemptNumber = attemptNumber; }
    public void setScreenshotPath(String screenshotPath) { this.screenshotPath = screenshotPath; }
    
    @Override
    public String toString() {
        return "TestExecutionResult{" +
                "testClass='" + testClassName + '\'' +
                ", testMethod='" + testMethodName + '\'' +
                ", passed=" + passed +
                ", attempt=" + attemptNumber +
                ", executionTime=" + executionTime + "ms" +
                '}';
    }
}
