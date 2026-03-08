package com.swag.labs.Skills.PRCommentResolver;

import com.swag.labs.Skills.Core.GitHubClient;
import com.swag.labs.Skills.Core.SkillLogger;
import org.kohsuke.github.GHIssueComment;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;

/**
 * Resolves PR comments with appropriate responses
 */
public class CommentResolver {
    private SkillLogger logger = new SkillLogger("CommentResolver");
    private GitHubClient githubClient;
    
    public CommentResolver() {
        this.githubClient = GitHubClient.getInstance();
    }
    
    /**
     * Resolve a comment based on its type
     */
    public void resolveComment(int prNumber, PRCommentAnalyzer.CommentAnalysis analysis) {
        try {
            GHRepository repo = githubClient.getRepository();
            GHPullRequest pr = repo.getPullRequest(prNumber);
            
            String reply = "";
            
            switch (analysis.getType()) {
                case APPROVAL:
                    logger.info("Comment is an approval, no action needed");
                    return; // Don't reply to approvals
                    
                case NITPICK:
                    reply = generateNitpickReply(analysis.getBody());
                    break;
                    
                case CLARIFICATION:
                    reply = generateClarificationReply(analysis.getBody());
                    break;
                    
                case CHANGE_REQUEST:
                    reply = generateChangeReply(analysis.getBody());
                    break;
                    
                default:
                    reply = generateGenericReply();
            }
            
            if (!reply.isEmpty()) {
                pr.comment(reply);
                logger.info("Reply posted to comment {}", analysis.getCommentId());
            }
            
        } catch (Exception e) {
            logger.error("Error resolving comment: {}", e.getMessage());
            throw new RuntimeException("Failed to resolve comment", e);
        }
    }
    
    private String generateNitpickReply(String comment) {
        return "@bot Noted! Thanks for the nitpick suggestion. We'll improve this in the next iteration. ✓";
    }
    
    private String generateClarificationReply(String comment) {
        return "@bot Great question! This test was auto-generated from the Jira ticket. " +
               "The logic reflects the acceptance criteria specified in the ticket. " +
               "Please refer to the Jira ticket for more context. Thank you!";
    }
    
    private String generateChangeReply(String comment) {
        return "@bot Thank you for the feedback! We'll review your suggestion and " +
               "attempt to apply the recommended changes. If the fix is successful, " +
               "we'll push an update to this PR.";
    }
    
    private String generateGenericReply() {
        return "@bot Thank you for your review! This PR was auto-generated from a Jira ticket. " +
               "Please review the generated code and provide feedback.";
    }
}
