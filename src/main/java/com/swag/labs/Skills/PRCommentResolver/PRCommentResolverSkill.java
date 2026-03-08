package com.swag.labs.Skills.PRCommentResolver;

import com.swag.labs.Skills.Core.SkillLogger;
import java.util.List;

/**
 * Main Skill class for PR Comment Resolution
 * Analyzes PR comments and provides automated responses
 */
public class PRCommentResolverSkill {
    private SkillLogger logger = new SkillLogger("PRCommentResolverSkill");
    private PRCommentAnalyzer analyzer;
    private CommentResolver resolver;
    
    public PRCommentResolverSkill() {
        this.analyzer = new PRCommentAnalyzer();
        this.resolver = new CommentResolver();
    }
    
    /**
     * Main entry point: Resolve all comments in a PR
     */
    public void resolvePRComments(int prNumber) {
        try {
            logger.info("========== Starting PR Comment Resolution for PR #{} ==========", prNumber);
            
            // Step 1: Fetch and analyze comments
            logger.info("Step 1: Fetching and analyzing PR comments");
            List<PRCommentAnalyzer.CommentAnalysis> analyses = analyzer.analyzeAllComments(prNumber);
            
            logger.info("Found {} comments to process", analyses.size());
            
            // Step 2: Resolve each comment
            logger.info("Step 2: Resolving comments");
            int resolved = 0;
            for (PRCommentAnalyzer.CommentAnalysis analysis : analyses) {
                try {
                    logger.debug("Processing: {}", analysis);
                    resolver.resolveComment(prNumber, analysis);
                    resolved++;
                } catch (Exception e) {
                    logger.warn("Failed to resolve comment {}: {}", analysis.getCommentId(), e.getMessage());
                }
            }
            
            logger.info("========== Comment Resolution Complete ==========");
            logger.info("Resolved {} out of {} comments", resolved, analyses.size());
            
        } catch (Exception e) {
            logger.error("PR comment resolution failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to resolve PR comments", e);
        }
    }
}
