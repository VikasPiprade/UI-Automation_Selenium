package com.swag.labs.Skills;

import com.swag.labs.Skills.Core.FeasibilityResult;
import com.swag.labs.Skills.Core.GeneratedTestResult;
import com.swag.labs.Skills.Core.PRCreatedResult;
import com.swag.labs.Skills.Core.SkillContext;
import com.swag.labs.Skills.Core.SkillLogger;
import com.swag.labs.Skills.Core.SkillRegistry;
import com.swag.labs.Skills.Feasibility.JiraFeasibilitySkill;
import com.swag.labs.Skills.TestGeneration.JiraTestGeneratorSkill;
import com.swag.labs.Skills.TestRunner.TestRunnerHealerSkill;
import com.swag.labs.Skills.PRCreation.PullRequestCreatorSkill;
import com.swag.labs.Skills.PRCommentResolver.PRCommentResolverSkill;

/**
 * Main orchestrator for the complete Skill Pipeline
 * Executes all skills in sequence: Feasibility -> Test Generation -> Test Running -> PR Creation
 */
public class SkillPipelineOrchestrator {
    private SkillLogger logger = new SkillLogger("SkillPipelineOrchestrator");
    private SkillRegistry registry;
    
    // Skill instances
    private JiraFeasibilitySkill feasibilitySkill;
    private JiraTestGeneratorSkill testGeneratorSkill;
    private TestRunnerHealerSkill testRunnerSkill;
    private PullRequestCreatorSkill prCreatorSkill;
    private PRCommentResolverSkill prCommentResolverSkill;
    
    public SkillPipelineOrchestrator() {
        this.registry = SkillRegistry.getInstance();
        initializeSkills();
    }
    
    private void initializeSkills() {
        feasibilitySkill = new JiraFeasibilitySkill();
        testGeneratorSkill = new JiraTestGeneratorSkill();
        testRunnerSkill = new TestRunnerHealerSkill();
        prCreatorSkill = new PullRequestCreatorSkill();
        prCommentResolverSkill = new PRCommentResolverSkill();
    }
    
    /**
     * Execute the full pipeline for a Jira ticket
     */
    public void runFullPipeline(String jiraTicketKey) {
        try {
            logger.info("╔" + "═".repeat(80) + "╗");
            logger.info("║  SKILL PIPELINE ORCHESTRATOR - Full Execution Started                        ║");
            logger.info("╚" + "═".repeat(80) + "╝");
            
            SkillContext context = new SkillContext(jiraTicketKey);
            
            // ========== SKILL 1: Feasibility Check ==========
            logger.info("\n[SKILL 1/4] Jira Feasibility Check");
            logger.info("─".repeat(82));
            FeasibilityResult feasibilityResult = null;
            try {
                feasibilityResult = feasibilitySkill.runFeasibilityCheck(jiraTicketKey);
                context.put("feasibilityResult", feasibilityResult);
                
                if (!feasibilityResult.isAutomatable()) {
                    logger.error("❌ Ticket is NOT feasible for automation (Score: {})", 
                               feasibilityResult.getFeasibilityScore());
                    logger.error("PIPELINE TERMINATED: Feasibility check failed");
                    return;
                }
                logger.info("✅ Ticket is feasible for automation (Score: {})", 
                          feasibilityResult.getFeasibilityScore());
            } catch (Exception e) {
                logger.error("❌ Feasibility check failed: {}", e.getMessage());
                throw e;
            }
            
            // ========== SKILL 2: Test Generation ==========
            logger.info("\n[SKILL 2/4] Test Generation");
            logger.info("─".repeat(82));
            GeneratedTestResult testResult = null;
            try {
                testResult = testGeneratorSkill.generateTests(jiraTicketKey, feasibilityResult);
                context.put("generatedTestResult", testResult);
                logger.info("✅ Test generated successfully: {}", testResult.getTestClassName());
            } catch (Exception e) {
                logger.error("❌ Test generation failed: {}", e.getMessage());
                throw e;
            }
            
            // ========== SKILL 3: Test Runner/Healer ==========
            logger.info("\n[SKILL 3/4] Test Execution with Self-Healing");
            logger.info("─".repeat(82));
            try {
                var execResult = testRunnerSkill.runAndHealTest(
                    testResult.getTestClassName(),
                    "testScenario1"  // Run first test method as example
                );
                context.put("testExecutionResult", execResult);
                
                if (!execResult.isPassed()) {
                    logger.error("❌ Test execution failed after {} attempts", 3);  // Default max retries
                    logger.error("PIPELINE TERMINATED: Test execution failed");
                    return;
                }
                logger.info("✅ Test passed successfully");
            } catch (Exception e) {
                logger.error("❌ Test execution failed: {}", e.getMessage());
                throw e;
            }
            
            // ========== SKILL 4: PR Creation ==========
            logger.info("\n[SKILL 4/4] Pull Request Creation");
            logger.info("─".repeat(82));
            PRCreatedResult prResult = null;
            try {
                var execResult = context.get("testExecutionResult", com.swag.labs.Skills.Core.TestExecutionResult.class);
                prResult = prCreatorSkill.createPRAndUpdateConfluence(jiraTicketKey, testResult, execResult);
                context.put("prCreatedResult", prResult);
                logger.info("✅ PR created successfully: {}", prResult.getPrUrl());
                logger.info("✅ Confluence page updated");
            } catch (Exception e) {
                logger.error("❌ PR creation failed: {}", e.getMessage());
                // Don't throw - allow pipeline to complete even if PR fails
                logger.warn("Continuing despite PR creation failure...");
            }
            
            // ========== PIPELINE COMPLETE ==========
            logger.info("\n╔" + "═".repeat(80) + "╗");
            logger.info("║  SKILL PIPELINE ORCHESTRATOR - Execution Completed Successfully! ✅        ║");
            logger.info("╚" + "═".repeat(80) + "╝");
            
            printExecutionSummary(context);
            
        } catch (Exception e) {
            logger.error("╔" + "═".repeat(80) + "╗");
            logger.error("║  SKILL PIPELINE ORCHESTRATOR - Execution Failed! ❌                          ║");
            logger.error("╚" + "═".repeat(80) + "╝");
            logger.error("Pipeline error: {}", e.getMessage(), e);
            throw new RuntimeException("Full pipeline execution failed", e);
        }
    }
    
    /**
     * Print execution summary
     */
    private void printExecutionSummary(SkillContext context) {
        logger.info("\n" + "═".repeat(82));
        logger.info("EXECUTION SUMMARY");
        logger.info("═".repeat(82));
        
        logger.info("Jira Ticket: {}", context.getJiraTicketKey());
        
        FeasibilityResult feas = context.get("feasibilityResult", FeasibilityResult.class);
        if (feas != null) {
            logger.info("  Feasibility: {} (Score: {})", feas.getStatus(), feas.getFeasibilityScore());
        }
        
        GeneratedTestResult test = context.get("generatedTestResult", GeneratedTestResult.class);
        if (test != null) {
            logger.info("  Test Generated: {} (Path: {})", test.getTestClassName(), test.getTestFilePath());
        }
        
        PRCreatedResult pr = context.get("prCreatedResult", PRCreatedResult.class);
        if (pr != null) {
            logger.info("  PR Created: #{} - {}", pr.getPrNumber(), pr.getPrUrl());
        }
        
        logger.info("═".repeat(82));
    }
    
    /**
     * Run only specific skill
     */
    public void runFeasibilityOnly(String jiraTicketKey) {
        feasibilitySkill.runFeasibilityCheck(jiraTicketKey);
    }
    
    public GeneratedTestResult runTestGenerationOnly(String jiraTicketKey, FeasibilityResult feasibilityResult) {
        return testGeneratorSkill.generateTests(jiraTicketKey, feasibilityResult);
    }
}
