package com.swag.labs.Skills.PRCreation;

import com.swag.labs.Skills.Core.SkillLogger;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Wrapper for Git operations: branch creation, commit, push
 */
public class GitOperations {
    private SkillLogger logger = new SkillLogger("GitOperations");
    
    /**
     * Create a new feature branch
     */
    public void createBranch(String branchName) {
        try {
            logger.info("Creating branch: {}", branchName);
            
            // Checkout from main first
            executeGitCommand("git checkout main");
            Thread.sleep(500); // Brief wait
            
            // Create and checkout new branch
            executeGitCommand("git checkout -b " + branchName);
            
            logger.info("Branch created successfully: {}", branchName);
        } catch (Exception e) {
            logger.error("Error creating branch: {}", e.getMessage());
            throw new RuntimeException("Failed to create branch: " + branchName, e);
        }
    }
    
    /**
     * Stage all changes
     */
    public void stageChanges() {
        try {
            logger.info("Staging changes");
            executeGitCommand("git add .");
            logger.info("Changes staged");
        } catch (Exception e) {
            logger.error("Error staging changes: {}", e.getMessage());
            throw new RuntimeException("Failed to stage changes", e);
        }
    }
    
    /**
     * Commit changes
     */
    public String commitChanges(String commitMessage) {
        try {
            logger.info("Committing changes: {}", commitMessage);
            
            String output = executeGitCommand("git commit -m \"" + commitMessage + "\"");
            
            // Extract commit hash from output
            String commitHash = extractCommitHash(output);
            logger.info("Changes committed with hash: {}", commitHash);
            
            return commitHash;
        } catch (Exception e) {
            logger.error("Error committing changes: {}", e.getMessage());
            throw new RuntimeException("Failed to commit changes", e);
        }
    }
    
    /**
     * Push branch to remote
     */
    public void pushBranch(String branchName) {
        try {
            logger.info("Pushing branch to remote: {}", branchName);
            executeGitCommand("git push origin " + branchName);
            logger.info("Branch pushed successfully");
        } catch (Exception e) {
            logger.error("Error pushing branch: {}", e.getMessage());
            throw new RuntimeException("Failed to push branch", e);
        }
    }
    
    /**
     * Execute git command
     */
    private String executeGitCommand(String command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", command);
        pb.directory(new java.io.File(System.getProperty("user.dir")));
        Process process = pb.start();
        
        StringBuilder output = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    output.append("ERROR: ").append(line).append("\n");
                }
            }
        }
        
        return output.toString();
    }
    
    /**
     * Extract commit hash from git output
     */
    private String extractCommitHash(String output) {
        try {
            // Look for format like: [main abc1234] message
            if (output.contains("[")) {
                String[] parts = output.split(" ");
                for (String part : parts) {
                    if (part.matches("[a-f0-9]{7}[\\]\\n]*")) {
                        return part.replaceAll("[\\]\\n]", "");
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Could not extract commit hash: {}", e.getMessage());
        }
        return "unknown";
    }
}
