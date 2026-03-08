package com.swag.labs.Skills.Core;

import java.io.Serializable;

/**
 * Result object for PR Creator skill
 */
public class PRCreatedResult implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String jiraTicketKey;
    private String branchName;
    private String commitHash;
    private String prUrl;
    private int prNumber;
    private String confluenceUpdateStatus;
    private long timestamp;
    
    public PRCreatedResult(String jiraTicketKey, String branchName) {
        this.jiraTicketKey = jiraTicketKey;
        this.branchName = branchName;
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getJiraTicketKey() { return jiraTicketKey; }
    public String getBranchName() { return branchName; }
    public String getCommitHash() { return commitHash; }
    public String getPrUrl() { return prUrl; }
    public int getPrNumber() { return prNumber; }
    public String getConfluenceUpdateStatus() { return confluenceUpdateStatus; }
    public long getTimestamp() { return timestamp; }
    
    public void setCommitHash(String commitHash) { this.commitHash = commitHash; }
    public void setPrUrl(String prUrl) { this.prUrl = prUrl; }
    public void setPrNumber(int prNumber) { this.prNumber = prNumber; }
    public void setConfluenceUpdateStatus(String confluenceUpdateStatus) { 
        this.confluenceUpdateStatus = confluenceUpdateStatus; 
    }
    
    @Override
    public String toString() {
        return "PRCreatedResult{" +
                "jiraTicketKey='" + jiraTicketKey + '\'' +
                ", branchName='" + branchName + '\'' +
                ", prNumber=" + prNumber +
                ", prUrl='" + prUrl + '\'' +
                ", confluenceStatus='" + confluenceUpdateStatus + '\'' +
                '}';
    }
}
