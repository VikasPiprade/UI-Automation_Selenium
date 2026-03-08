package com.swag.labs.Skills;

/**
 * Quick example demonstrating how to run the Skill Pipeline Orchestrator
 * This can be executed as a standalone Java application or unit test
 */
public class RunSkillPipeline {
    
    /**
     * Example: Run full pipeline for Jira ticket KAN-1
     */
    public static void main(String[] args) {
        // Read ticket from Maven property or use default
        String ticketKey = System.getProperty("jiraTicket", "KAN-1");
        
        System.out.println("\n╔════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║          Skill Pipeline Orchestrator - " + String.format("%-32s", ticketKey) + "║");
        System.out.println("╚════════════════════════════════════════════════════════════════════════╝\n");
        
        try {
            // Create orchestrator instance
            SkillPipelineOrchestrator orchestrator = new SkillPipelineOrchestrator();
            
            // Run full pipeline for the specified ticket
            // This will execute:
            // 1. Feasibility Check
            // 2. Test Generation
            // 3. Test Execution with Self-Healing (retry up to 3x)
            // 4. Pull Request Creation
            orchestrator.runFullPipeline(ticketKey);
            
            System.out.println("\n✅ Pipeline execution completed successfully!");
            
        } catch (Exception e) {
            System.err.println("\n❌ Pipeline execution failed!");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Alternative: Run only feasibility check
     */
    public static void runFeasibilityCheckOnly(String ticketKey) {
        System.out.println("Running feasibility check for: " + ticketKey);
        
        try {
            SkillPipelineOrchestrator orchestrator = new SkillPipelineOrchestrator();
            orchestrator.runFeasibilityOnly(ticketKey);
            System.out.println("✅ Feasibility check completed");
        } catch (Exception e) {
            System.err.println("❌ Feasibility check failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Alternative: Run only test generation (requires feasibility to pass)
     */
    public static void runTestGenerationOnly(String ticketKey) {
        System.out.println("Running test generation for: " + ticketKey);
        
        try {
            SkillPipelineOrchestrator orchestrator = new SkillPipelineOrchestrator();
            
            // First check feasibility
            com.swag.labs.Skills.Feasibility.JiraFeasibilitySkill feasibilitySkill = 
                new com.swag.labs.Skills.Feasibility.JiraFeasibilitySkill();
            com.swag.labs.Skills.Core.FeasibilityResult feasibilityResult = 
                feasibilitySkill.runFeasibilityCheck(ticketKey);
            
            // Then generate tests
            if (feasibilityResult.isAutomatable()) {
                orchestrator.runTestGenerationOnly(ticketKey, feasibilityResult);
                System.out.println("✅ Test generation completed");
            } else {
                System.err.println("❌ Ticket is not automatable (score: " + 
                                 feasibilityResult.getFeasibilityScore() + ")");
            }
        } catch (Exception e) {
            System.err.println("❌ Test generation failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
