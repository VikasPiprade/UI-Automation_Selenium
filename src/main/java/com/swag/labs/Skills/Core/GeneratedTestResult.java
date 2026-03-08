package com.swag.labs.Skills.Core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Result object for Test Generation skill
 */
public class GeneratedTestResult implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String ticketKey;
    private String testClassName;
    private String testFilePath;
    private List<String> pageObjectFilePaths = new ArrayList<>();
    private List<String> testDataFilePaths = new ArrayList<>();
    private boolean compilationSuccessful;
    private String compilationLog;
    private long timestamp;
    
    public GeneratedTestResult(String ticketKey, String testClassName, String testFilePath) {
        this.ticketKey = ticketKey;
        this.testClassName = testClassName;
        this.testFilePath = testFilePath;
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getTicketKey() { return ticketKey; }
    public String getTestClassName() { return testClassName; }
    public String getTestFilePath() { return testFilePath; }
    public List<String> getPageObjectFilePaths() { return pageObjectFilePaths; }
    public List<String> getTestDataFilePaths() { return testDataFilePaths; }
    public boolean isCompilationSuccessful() { return compilationSuccessful; }
    public String getCompilationLog() { return compilationLog; }
    public long getTimestamp() { return timestamp; }
    
    public void addPageObjectFilePath(String filePath) {
        pageObjectFilePaths.add(filePath);
    }
    
    public void addTestDataFilePath(String filePath) {
        testDataFilePaths.add(filePath);
    }
    
    public void setCompilationSuccessful(boolean compilationSuccessful) {
        this.compilationSuccessful = compilationSuccessful;
    }
    
    public void setCompilationLog(String compilationLog) {
        this.compilationLog = compilationLog;
    }
    
    @Override
    public String toString() {
        return "GeneratedTestResult{" +
                "ticketKey='" + ticketKey + '\'' +
                ", testClassName='" + testClassName + '\'' +
                ", testFilePath='" + testFilePath + '\'' +
                ", pageObjects=" + pageObjectFilePaths.size() +
                ", testData=" + testDataFilePaths.size() +
                ", compilationSuccessful=" + compilationSuccessful +
                '}';
    }
}
