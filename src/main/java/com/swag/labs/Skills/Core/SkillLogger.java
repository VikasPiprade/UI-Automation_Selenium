package com.swag.labs.Skills.Core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

/**
 * Unified logging utility for all Skills
 * Uses log4j2 with a SKILL marker for easy filtering
 */
public class SkillLogger {
    private static final Logger logger = LogManager.getLogger(SkillLogger.class);
    private static final Marker SKILL_MARKER = MarkerManager.getMarker("SKILL");
    
    private String skillName;

    public SkillLogger(String skillName) {
        this.skillName = skillName;
    }

    public void info(String message) {
        logger.info(SKILL_MARKER, "[{}] {}", skillName, message);
    }

    public void info(String message, Object... params) {
        logger.info(SKILL_MARKER, "[{}] " + message, addSkillNameToParams(params));
    }

    public void debug(String message) {
        logger.debug(SKILL_MARKER, "[{}] {}", skillName, message);
    }

    public void debug(String message, Object... params) {
        logger.debug(SKILL_MARKER, "[{}] " + message, addSkillNameToParams(params));
    }

    public void warn(String message) {
        logger.warn(SKILL_MARKER, "[{}] {}", skillName, message);
    }

    public void warn(String message, Object... params) {
        logger.warn(SKILL_MARKER, "[{}] " + message, addSkillNameToParams(params));
    }

    public void error(String message) {
        logger.error(SKILL_MARKER, "[{}] {}", skillName, message);
    }

    public void error(String message, Throwable throwable) {
        logger.error(SKILL_MARKER, "[{}] {}", skillName, message, throwable);
    }

    public void error(String message, Object... params) {
        logger.error(SKILL_MARKER, "[{}] " + message, addSkillNameToParams(params));
    }

    private Object[] addSkillNameToParams(Object[] params) {
        Object[] newParams = new Object[params.length + 1];
        newParams[0] = skillName;
        System.arraycopy(params, 0, newParams, 1, params.length);
        return newParams;
    }
}
