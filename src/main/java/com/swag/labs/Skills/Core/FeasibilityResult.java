package com.swag.labs.Skills.Core;

import java.io.Serializable;

/**
 * Result object for Feasibility check skill
 */
public class FeasibilityResult implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String ticketKey;
    private double feasibilityScore;
    private boolean automatable;
    private String reasoning;
    private String ticketType;
    private long timestamp;
    
    public FeasibilityResult(String ticketKey, double feasibilityScore, boolean automatable, 
                            String reasoning, String ticketType) {
        this.ticketKey = ticketKey;
        this.feasibilityScore = feasibilityScore;
        this.automatable = automatable;
        this.reasoning = reasoning;
        this.ticketType = ticketType;
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getTicketKey() { return ticketKey; }
    public double getFeasibilityScore() { return feasibilityScore; }
    public boolean isAutomatable() { return automatable; }
    public String getReasoning() { return reasoning; }
    public String getTicketType() { return ticketType; }
    public long getTimestamp() { return timestamp; }
    
    public String getStatus() {
        return automatable ? "PASS" : "FAIL";
    }
    
    @Override
    public String toString() {
        return "FeasibilityResult{" +
                "ticketKey='" + ticketKey + '\'' +
                ", feasibilityScore=" + feasibilityScore +
                ", automatable=" + automatable +
                ", status='" + getStatus() + '\'' +
                ", reasoning='" + reasoning + '\'' +
                '}';
    }
}
