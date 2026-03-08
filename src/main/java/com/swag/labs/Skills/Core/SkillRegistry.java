package com.swag.labs.Skills.Core;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry/Factory for managing skill execution
 * Singleton pattern to orchestrate skill operations
 */
public class SkillRegistry {
    private static SkillRegistry instance;
    private Map<String, Object> registeredSkills = new HashMap<>();
    private SkillLogger logger = new SkillLogger("SkillRegistry");
    
    private SkillRegistry() {
        initializeSingletons();
    }
    
    public static SkillRegistry getInstance() {
        if (instance == null) {
            synchronized (SkillRegistry.class) {
                if (instance == null) {
                    instance = new SkillRegistry();
                }
            }
        }
        return instance;
    }
    
    private void initializeSingletons() {
        logger.info("Initializing Skill Registry with singleton clients");
        
        // Register core clients as singletons
        registerSkill("JiraClient", JiraClient.getInstance());
        registerSkill("ConfluenceClient", ConfluenceClient.getInstance());
        registerSkill("GitHubClient", GitHubClient.getInstance());
    }
    
    /**
     * Register a skill or service
     */
    public void registerSkill(String name, Object skill) {
        logger.debug("Registering skill: {}", name);
        registeredSkills.put(name, skill);
    }
    
    /**
     * Get a registered skill
     */
    public Object getSkill(String name) {
        return registeredSkills.get(name);
    }
    
    /**
     * Get a registered skill with type casting
     */
    public <T> T getSkill(String name, Class<T> type) {
        Object skill = registeredSkills.get(name);
        if (skill == null) {
            logger.warn("Skill not found: {}", name);
            return null;
        }
        return type.cast(skill);
    }
    
    /**
     * Check if a skill is registered
     */
    public boolean hasSkill(String name) {
        return registeredSkills.containsKey(name);
    }
    
    /**
     * Get all registered skills
     */
    public Map<String, Object> getAllSkills() {
        return new HashMap<>(registeredSkills);
    }
}
