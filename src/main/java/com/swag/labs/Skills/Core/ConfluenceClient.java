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
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Wrapper for Confluence REST API with Basic Auth
 */
public class ConfluenceClient {
    private static ConfluenceClient instance;
    private SkillLogger logger = new SkillLogger("ConfluenceClient");
    private Properties config;
    private String baseUrl;
    private String username;
    private String password;
    
    private ConfluenceClient() {
        this.config = new ConfigurationUtils().getProperty();
        this.baseUrl = config.getProperty("confluence.url", "");
        this.username = System.getenv("CONFLUENCE_USERNAME") != null ? 
            System.getenv("CONFLUENCE_USERNAME") : 
            config.getProperty("confluence.username", "");
        this.password = System.getenv("CONFLUENCE_PASSWORD") != null ? 
            System.getenv("CONFLUENCE_PASSWORD") : 
            config.getProperty("confluence.password", "");
        
        if (baseUrl.isEmpty() || username.isEmpty() || password.isEmpty()) {
            logger.warn("Confluence configuration incomplete. Set CONFLUENCE_URL, CONFLUENCE_USERNAME, CONFLUENCE_PASSWORD env vars or config.properties");
        }
    }
    
    public static ConfluenceClient getInstance() {
        if (instance == null) {
            synchronized (ConfluenceClient.class) {
                if (instance == null) {
                    instance = new ConfluenceClient();
                }
            }
        }
        return instance;
    }
    
    /**
     * Get a Confluence page by ID
     */
    public JsonObject getPageById(String pageId) {
        try {
            logger.info("Fetching Confluence page: {}", pageId);
            // Use standard Confluence Cloud API v3 endpoint
            String url = baseUrl + "/rest/api/v3/pages/" + pageId + "?body-format=storage";
            String response = executeGetRequest(url);
            if (response == null || response.isEmpty()) {
                throw new RuntimeException("Empty response from Confluence");
            }
            return JsonParser.parseString(response).getAsJsonObject();
        } catch (RuntimeException e) {
            // Check if this is a "REST API disabled" error
            if (e.getMessage().contains("404")) {
                logger.error("CONFLUENCE REST API DISABLED: Received 404. REST API endpoints are not available on this Confluence instance.");
                logger.error("The Confluence web UI (https://vikaspiprade.atlassian.net/wiki) is accessible, but REST API is disabled.");
                logger.error("To enable Confluence reporting, an administrator must enable REST API on this Confluence instance.");
            }
            throw e;
        } catch (Exception e) {
            logger.error("Failed to fetch Confluence page {}: {}", pageId, e.getMessage());
            throw new RuntimeException("Failed to fetch Confluence page", e);
        }
    }
    
    /**
     * Check if a Confluence page exists (for testing)
     */
    public boolean pageExists(String pageId) {
        try {
            JsonObject page = getPageById(pageId);
            return page.has("id") && page.get("id").getAsString().equals(pageId);
        } catch (Exception e) {
            logger.warn("Page {} does not exist or is not accessible: {}", pageId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Update a Confluence page
     */
    public void updatePage(String pageId, String title, JsonObject body) {
        try {
            logger.info("Updating Confluence page: {}", pageId);
            int version = getPageVersion(pageId);
            
            JsonObject updateRequest = new JsonObject();
            updateRequest.addProperty("type", "page");
            updateRequest.addProperty("title", title);
            
            JsonObject bodyObj = new JsonObject();
            bodyObj.add("storage", body);
            updateRequest.add("body", bodyObj);
            
            JsonObject versionObj = new JsonObject();
            versionObj.addProperty("number", version + 1);
            updateRequest.add("version", versionObj);
            
            String url = baseUrl + "/rest/api/content/" + pageId;
            executePutRequest(url, updateRequest.toString());
            logger.info("Page {} updated successfully", pageId);
        } catch (Exception e) {
            logger.error("Failed to update Confluence page {}: {}", pageId, e.getMessage());
            throw new RuntimeException("Failed to update Confluence page", e);
        }
    }
    
    /**
     * Append content to a Confluence page
     */
    public void appendToPage(String pageId, String contentToAppend) {
        try {
            logger.info("Appending content to Confluence page: {}", pageId);
            
            // First verify page exists
            JsonObject page = getPageById(pageId);
            if (!page.has("version")) {
                logger.error("Page response missing version field. Response keys: {}", String.join(", ", page.keySet()));
                throw new RuntimeException("Invalid Confluence response - missing version");
            }
            
            int version = getPageVersion(pageId);
            String currentContent = getPageContent(pageId);
            
            String newContent = currentContent + "\n" + contentToAppend;
            
            // Proper Confluence Cloud API v3 PUT request format
            JsonObject updateRequest = new JsonObject();
            updateRequest.addProperty("type", "page");
            updateRequest.addProperty("version", version + 1);
            updateRequest.addProperty("title", page.has("title") ? page.get("title").getAsString() : "Untitled");
            
            JsonObject storageBody = new JsonObject();
            storageBody.addProperty("value", newContent);
            storageBody.addProperty("representation", "storage");
            
            JsonObject bodyObj = new JsonObject();
            bodyObj.add("storage", storageBody);
            updateRequest.add("body", bodyObj);
            
            String url = baseUrl + "/rest/api/v3/pages/" + pageId;
            executePutRequest(url, updateRequest.toString());
            executePutRequest(url, updateRequest.toString());
            logger.info("Content appended to page {} successfully", pageId);
        } catch (Exception e) {
            logger.error("Failed to append content to page {}: {}", pageId, e.getMessage());
            throw new RuntimeException("Failed to append to Confluence page", e);
        }
    }
    
    private int getPageVersion(String pageId) {
        try {
            JsonObject page = getPageById(pageId);
            
            // Handle Confluence Cloud API v3 response format
            if (page.has("version") && !page.get("version").isJsonNull()) {
                JsonElement versionElement = page.get("version");
                if (versionElement.isJsonObject()) {
                    JsonObject versionObj = versionElement.getAsJsonObject();
                    if (versionObj.has("number")) {
                        return versionObj.get("number").getAsInt();
                    }
                } else if (versionElement.isJsonPrimitive()) {
                    return versionElement.getAsInt();
                }
            }
            
            logger.warn("Could not extract version from page response, defaulting to 0");
            return 0;
        } catch (Exception e) {
            logger.error("Failed to get page version: {}", e.getMessage());
            return 0;
        }
    }
    
    private String getPageContent(String pageId) {
        try {
            JsonObject page = getPageById(pageId);
            
            // Handle Confluence Cloud API v3 response format
            if (page.has("body") && !page.get("body").isJsonNull()) {
                JsonObject bodyObj = page.getAsJsonObject("body");
                
                // Try storage format first (standard for content markdown)
                if (bodyObj.has("storage") && !bodyObj.get("storage").isJsonNull()) {
                    JsonObject storageObj = bodyObj.getAsJsonObject("storage");
                    if (storageObj.has("value") && !storageObj.get("value").isJsonNull()) {
                        return storageObj.get("value").getAsString();
                    }
                }
                
                // Try view format as fallback
                if (bodyObj.has("view") && !bodyObj.get("view").isJsonNull()) {
                    JsonObject viewObj = bodyObj.getAsJsonObject("view");
                    if (viewObj.has("value") && !viewObj.get("value").isJsonNull()) {
                        return viewObj.get("value").getAsString();
                    }
                }
            }
            
            logger.warn("Could not extract page content from response, returning empty string");
            return "";
        } catch (Exception e) {
            logger.error("Failed to get page content: {}", e.getMessage());
            return "";
        }
    }
    
    private String executeGetRequest(String url) throws Exception {
        CloseableHttpClient httpClient = createHttpClient();
        try {
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("Content-Type", "application/json");
            httpGet.setHeader("Accept", "application/json");
            httpGet.setHeader("User-Agent", "SkillPipeline/1.0");
            httpGet.setHeader("X-Atlassian-Token", "no-check");
            
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                
                logger.debug("GET Response Status: {} | URL: {}", statusCode, url);
                
                // Log first 200 chars of response for debugging
                String responsePreview = responseBody.substring(0, Math.min(200, responseBody.length()));
                if (responsePreview.startsWith("<!DOCTYPE") || responsePreview.startsWith("<html")) {
                    logger.error("Received HTML response instead of JSON. Status: {}. This suggests authentication failure or wrong endpoint.", statusCode);
                    logger.error("First 300 chars: {}", responseBody.substring(0, Math.min(300, responseBody.length())));
                }
                
                if (statusCode >= 400) {
                    logger.error("HTTP Error {}: {} | URL: {}", 
                        statusCode, response.getStatusLine().getReasonPhrase(), url);
                    throw new RuntimeException("HTTP Error: " + response.getStatusLine());
                }
                
                if (statusCode >= 300) {
                    logger.warn("Redirect response (status {}). Response: {}", statusCode, responsePreview);
                }
                
                return responseBody;
            }
        } finally {
            httpClient.close();
        }
    }
    
    private void executePutRequest(String url, String body) throws Exception {
        CloseableHttpClient httpClient = createHttpClient();
        try {
            HttpPut httpPut = new HttpPut(url);
            httpPut.setHeader("Content-Type", "application/json");
            httpPut.setHeader("User-Agent", "SkillPipeline/1.0");
            httpPut.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
            
            try (CloseableHttpResponse response = httpClient.execute(httpPut)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                
                if (statusCode >= 400) {
                    logger.error("HTTP Error {} on PUT: {} | Response: {}", 
                        statusCode, response.getStatusLine().getReasonPhrase(),
                        responseBody.substring(0, Math.min(500, responseBody.length())));
                    throw new RuntimeException("HTTP Error: " + response.getStatusLine());
                }
                
                logger.info("PUT Request successful. Status: {}", statusCode);
            }
        } finally {
            httpClient.close();
        }
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
