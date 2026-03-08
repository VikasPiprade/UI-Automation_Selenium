package com.swag.labs.Skills.PRCommentResolver;

import com.swag.labs.Skills.Core.GitHubClient;
import com.swag.labs.Skills.Core.SkillLogger;
import org.kohsuke.github.GHIssueComment;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Analyzes PR comments for action items
 */
public class PRCommentAnalyzer {
    private SkillLogger logger = new SkillLogger("PRCommentAnalyzer");
    
    /**
     * Analyze a PR comment
     */
    public CommentAnalysis analyze(GHIssueComment comment) {
        try {
            String body = comment.getBody();
            logger.debug("Analyzing comment: {}", body.substring(0, Math.min(50, body.length())));
            
            CommentAnalysis analysis = new CommentAnalysis(comment.getId(), body);
            
            // Classify comment
            String lowerBody = body.toLowerCase();
            
            if (lowerBody.contains("approved") || lowerBody.contains("looks good") || lowerBody.contains("+1")) {
                analysis.setType(CommentType.APPROVAL);
            } 
            else if (lowerBody.contains("nit:") || lowerBody.contains("typo") || lowerBody.contains("minor")) {
                analysis.setType(CommentType.NITPICK);
            } 
            else if (lowerBody.contains("why") || lowerBody.contains("explain") || 
                    lowerBody.contains("what") || lowerBody.contains("how")) {
                analysis.setType(CommentType.CLARIFICATION);
            } 
            else if (lowerBody.contains("update") || lowerBody.contains("fix") || 
                    lowerBody.contains("change") || lowerBody.contains("adjust")) {
                analysis.setType(CommentType.CHANGE_REQUEST);
            } 
            else {
                analysis.setType(CommentType.GENERAL);
            }
            
            return analysis;
            
        } catch (Exception e) {
            logger.error("Error analyzing comment: {}", e.getMessage());
            throw new RuntimeException("Failed to analyze comment", e);
        }
    }
    
    /**
     * Fetch all comments from a PR
     */
    public List<CommentAnalysis> analyzeAllComments(int prNumber) {
        try {
            logger.info("Fetching comments for PR: {}", prNumber);
            
            GitHubClient githubClient = GitHubClient.getInstance();
            GHRepository repo = githubClient.getRepository();
            GHPullRequest pr = repo.getPullRequest(prNumber);
            
            List<CommentAnalysis> analyses = new ArrayList<>();
            for (GHIssueComment comment : pr.getComments()) {
                CommentAnalysis analysis = analyze(comment);
                analyses.add(analysis);
            }
            
            logger.info("Found {} comments in PR {}", analyses.size(), prNumber);
            return analyses;
            
        } catch (Exception e) {
            logger.error("Error fetching PR comments: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch PR comments", e);
        }
    }
    
    /**
     * Comment types
     */
    public enum CommentType {
        APPROVAL, NITPICK, CLARIFICATION, CHANGE_REQUEST, GENERAL
    }
    
    /**
     * Comment analysis result
     */
    public static class CommentAnalysis {
        private long commentId;
        private String body;
        private CommentType type;
        
        public CommentAnalysis(long commentId, String body) {
            this.commentId = commentId;
            this.body = body;
            this.type = CommentType.GENERAL;
        }
        
        public long getCommentId() { return commentId; }
        public String getBody() { return body; }
        public CommentType getType() { return type; }
        public void setType(CommentType type) { this.type = type; }
        
        @Override
        public String toString() {
            return "CommentAnalysis{" +
                    "id=" + commentId +
                    ", type=" + type +
                    ", bodyLength=" + body.length() +
                    '}';
        }
    }
}
