package com.swag.labs.Skills.Core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Context object that holds shared data between skills in the pipeline
 */
public class SkillContext implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String jiraTicketKey;
    private Map<String, Object> data = new HashMap<>();
    
    public SkillContext(String jiraTicketKey) {
        this.jiraTicketKey = jiraTicketKey;
    }
    
    public String getJiraTicketKey() {
        return jiraTicketKey;
    }
    
    public void setJiraTicketKey(String jiraTicketKey) {
        this.jiraTicketKey = jiraTicketKey;
    }
    
    public void put(String key, Object value) {
        data.put(key, value);
    }
    
    public Object get(String key) {
        return data.get(key);
    }
    
    public <T> T get(String key, Class<T> type) {
        return type.cast(data.get(key));
    }
    
    public boolean has(String key) {
        return data.containsKey(key);
    }
    
    public void remove(String key) {
        data.remove(key);
    }
    
    public Map<String, Object> getAllData() {
        return new HashMap<>(data);
    }
    
    @Override
    public String toString() {
        return "SkillContext{" +
                "jiraTicketKey='" + jiraTicketKey + '\'' +
                ", data=" + data +
                '}';
    }
}
