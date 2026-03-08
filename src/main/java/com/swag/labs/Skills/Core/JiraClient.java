package com.swag.labs.Skills.Core;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.swag.labs.Utilities.ConfigurationUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Wrapper for Jira REST API with Basic Auth
 * Supports both Jira Cloud (v3 API) and Jira Server (v2 API)
 * Uses HttpClient for REST calls instead of external Jira library
 */
public class JiraClient {
    private static JiraClient instance;
    private SkillLogger logger = new SkillLogger("JiraClient");
    private Properties config;
    private String baseUrl;
    private String username;
    private String password;
    private String apiVersion; // "v2" for Server, "v3" for Cloud
    private boolean isCloud;
    
    private JiraClient() {
        this.config = new ConfigurationUtils().getProperty();
        this.baseUrl = cleanJiraUrl(config.getProperty("jira.url", ""));
        this.username = System.getenv("JIRA_USERNAME") != null ? 
            System.getenv("JIRA_USERNAME") : 
            config.getProperty("jira.username", "");
        this.password = System.getenv("JIRA_PASSWORD") != null ? 
            System.getenv("JIRA_PASSWORD") : 
            config.getProperty("jira.password", "");
        
        // Detect Jira type (Cloud or Server)
        detectJiraType();
        
        if (baseUrl.isEmpty() || username.isEmpty() || password.isEmpty()) {
            logger.warn("Jira configuration incomplete. Set JIRA_USERNAME, JIRA_PASSWORD env vars or config.properties");
        } else {
            logger.info("Jira client initialized for URL: {} (Type: {})", baseUrl, isCloud ? "CLOUD" : "SERVER");
        }
    }
    
    /**
     * Detect if this is Jira Cloud or Server based on URL pattern
     */
    private void detectJiraType() {
        if (baseUrl.contains(".atlassian.net")) {
            this.isCloud = true;
            this.apiVersion = "v3"; // Jira Cloud uses API v3
            logger.info("URL contains '.atlassian.net' - Detected as JIRA CLOUD");
        } else {
            this.isCloud = false;
            this.apiVersion = "v2"; // Jira Server uses API v2
            logger.info("URL does NOT contain '.atlassian.net' - Detected as JIRA SERVER");
        }
        logger.info("Type: {}, API Version: {}", isCloud ? "CLOUD" : "SERVER", apiVersion);
    }
    
    /**
     * Get the correct issue endpoint based on Jira type
     */
    private String getIssueEndpoint(String ticketKey) {
        String endpoint;
        if (isCloud) {
            endpoint = baseUrl + "/rest/api/3/issue/" + ticketKey;
            logger.info("Using CLOUD API v3 endpoint pattern");
        } else {
            endpoint = baseUrl + "/rest/api/2/issue/" + ticketKey;
            logger.info("Using SERVER API v2 endpoint pattern");
        }
        logger.info("Built endpoint: {}", endpoint);
        return endpoint;
    }
    
    /**
     * Clean up Jira URL to ensure it's the correct base URL for API calls
     * Removes /browse, /jira, or other browser paths
     */
    private String cleanJiraUrl(String url) {
        if (url.isEmpty()) return url;
        // Remove trailing slashes
        url = url.replaceAll("/+$", "");
        // Remove /browse if present at the end
        url = url.replaceAll("/browse$", "");
        // Remove /jira if present at the end
        url = url.replaceAll("/jira$", "");
        return url;
    }
    
    public static JiraClient getInstance() {
        if (instance == null) {
            synchronized (JiraClient.class) {
                if (instance == null) {
                    instance = new JiraClient();
                }
            }
        }
        return instance;
    }
    
    /**
     * Retrieve Jira issue by ticket key
     */
    public JsonObject getTicketByKey(String ticketKey) {
        try {
            logger.info("========== Fetching Jira ticket: {} ==========", ticketKey);
            logger.info("Base URL: {}", baseUrl);
            logger.info("Is Cloud: {}", isCloud);
            logger.info("API Version: {}", apiVersion);
            
            String url = getIssueEndpoint(ticketKey);
            logger.info("Full URL to fetch: {}", url);
            
            String response = executeGetRequest(url);
            logger.info("Successfully fetched ticket JSON (size: {} bytes)", response.length());
            return JsonParser.parseString(response).getAsJsonObject();
        } catch (Exception e) {
            logger.error("Failed to fetch ticket {}: {}", ticketKey, e.getMessage());
            throw new RuntimeException("Failed to fetch Jira ticket: " + ticketKey, e);
        }
    }
    
    /**
     * Get ticket summary (works with both Cloud and Server)
     */
    public String getTicketSummary(String ticketKey) {
        try {
            JsonObject ticket = getTicketByKey(ticketKey);
            JsonObject fields = ticket.getAsJsonObject("fields");
            if (fields != null && fields.has("summary")) {
                return fields.get("summary").getAsString();
            }
            return "";
        } catch (Exception e) {
            logger.error("Failed to get ticket summary: {}", e.getMessage());
            return "";
        }
    }
    
    /**
     * Get ticket description (works with both Cloud and Server)
     */
    public String getTicketDescription(String ticketKey) {
        try {
            JsonObject ticket = getTicketByKey(ticketKey);
            JsonObject fields = ticket.getAsJsonObject("fields");
            if (fields != null && fields.has("description")) {
                JsonElement description = fields.get("description");
                if (description != null && !description.isJsonNull()) {
                    if (description.isJsonPrimitive()) {
                        return description.getAsString();
                    }
                }
            }
            return "";
        } catch (Exception e) {
            logger.error("Failed to get ticket description: {}", e.getMessage());
            return "";
        }
    }
    
    /**
     * Get ticket type (works with both Cloud and Server)
     */
    public String getTicketType(String ticketKey) {
        try {
            JsonObject ticket = getTicketByKey(ticketKey);
            JsonObject fields = ticket.getAsJsonObject("fields");
            if (fields != null && fields.has("issuetype")) {
                JsonObject issueType = fields.getAsJsonObject("issuetype");
                if (issueType != null && issueType.has("name")) {
                    return issueType.get("name").getAsString();
                }
            }
            return "Unknown";
        } catch (Exception e) {
            logger.error("Failed to get ticket type: {}", e.getMessage());
            return "Unknown";
        }
    }
    
    /**
     * Get ticket labels (works with both Cloud and Server)
     */
    public String getTicketLabels(String ticketKey) {
        try {
            JsonObject ticket = getTicketByKey(ticketKey);
            JsonObject fields = ticket.getAsJsonObject("fields");
            if (fields != null && fields.has("labels")) {
                JsonElement labels = fields.get("labels");
                if (labels != null && labels.isJsonArray()) {
                    StringBuilder labelStr = new StringBuilder();
                    labels.getAsJsonArray().forEach(label -> {
                        if (labelStr.length() > 0) labelStr.append(", ");
                        labelStr.append(label.getAsString());
                    });
                    return labelStr.toString();
                }
            }
            return "";
        } catch (Exception e) {
            logger.error("Failed to get ticket labels: {}", e.getMessage());
            return "";
        }
    }
    
    private String executeGetRequest(String url) throws Exception {
        logger.info("Executing GET request to: {}", url);
        logger.info("Auth - Username: {} (Password set: {})", username, !password.isEmpty());
        
        CloseableHttpClient httpClient = createHttpClient();
        try {
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("Content-Type", "application/json");
            httpGet.setHeader("Accept", "application/json");
            httpGet.setHeader("Authorization", "Basic " + getBasicAuthHeader());
            
            logger.info("Headers set: Authorization and Content-Type");
            
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                
                logger.info("Response Status: {} {}", statusCode, response.getStatusLine().getReasonPhrase());
                
                if (statusCode >= 400) {
                    logger.error("HTTP {} error response", statusCode);
                    if (responseBody != null && !responseBody.isEmpty()) {
                        String preview = responseBody.length() > 200 ? responseBody.substring(0, 200) : responseBody;
                        logger.error("Response preview: {}", preview);
                    }
                    throw new RuntimeException("HTTP Error: " + statusCode);
                }
                logger.info("Response received successfully");
                return responseBody;
            }
        } catch (Exception e) {
            logger.error("Request execution failed for URL: {}", url);
            logger.error("Error: {}", e.getMessage());
            throw e;
        } finally {
            httpClient.close();
        }
    }
    
    private String getBasicAuthHeader() {
        String credentials = username + ":" + password;
        return java.util.Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }
    
    private CloseableHttpClient createHttpClient() {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
            AuthScope.ANY,
            new UsernamePasswordCredentials(username, password)
        );
        
        return HttpClients.custom()
            .setDefaultCredentialsProvider(credentialsProvider)
            .build();
    }
}

