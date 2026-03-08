# Test Confluence Cloud API v3 Connection
# This script tests if we can successfully authenticate and access the Confluence page

$ErrorActionPreference = "Stop"

# Load credentials from environment or .env
$confluenceUsername = $env:CONFLUENCE_USERNAME
$confluencePassword = $env:CONFLUENCE_PASSWORD

# If not set, try loading from .env file
if (-not $confluenceUsername -or -not $confluencePassword) {
    if (Test-Path ".env") {
        Write-Host "Loading credentials from .env file..."
        Get-Content ".env" | ForEach-Object {
            if ($_ -match '^([^=]+)=(.*)$') {
                $name = $matches[1]
                $value = $matches[2]
                if ($name -eq "CONFLUENCE_USERNAME") { $confluenceUsername = $value }
                if ($name -eq "CONFLUENCE_PASSWORD") { $confluencePassword = $value }
            }
        }
    }
}
$confluenceUrl = "https://vikaspiprade.atlassian.net/wiki"
$pageId = "688130"

if (-not $confluenceUsername -or -not $confluencePassword) {
    Write-Host "ERROR: Confluence credentials not set in environment" -ForegroundColor Red
    Write-Host "Set CONFLUENCE_USERNAME and CONFLUENCE_PASSWORD environment variables"
    exit 1
}

Write-Host "Testing Confluence Cloud API v3..." -ForegroundColor Green
Write-Host "URL: $confluenceUrl"
Write-Host "Page ID: $pageId"
Write-Host "Username: $confluenceUsername (length: $($confluenceUsername.Length))" -ForegroundColor Gray
Write-Host "Password: **(length: $($confluencePassword.Length))**" -ForegroundColor Gray
Write-Host ""

# Show what will be sent in auth header
$pair = "$($confluenceUsername):$($confluencePassword)"
Write-Host "Auth String (before base64): $pair (length: $($pair.Length))" -ForegroundColor Gray

# Create Basic Auth header
$pair = "$($confluenceUsername):$($confluencePassword)"
$encodedAuth = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($pair))
$headers = @{
    "Authorization" = "Basic $encodedAuth"
    "Content-Type" = "application/json"
}

# Test 1: GET page with correct endpoint
Write-Host "TEST 1: Using v3 API endpoint (api/v3/pages/...)" -ForegroundColor Cyan
$url = "$confluenceUrl/api/v3/pages/$pageId`?body-format=storage"
Write-Host "URL: $url" -ForegroundColor White

try {
    $response = Invoke-WebRequest -Uri $url -Headers $headers -Method Get -ErrorAction Stop
    Write-Host "SUCCESS - Status: $($response.StatusCode)" -ForegroundColor Green
    
    Write-Host "  Raw Response Length: $($response.Content.Length) bytes" -ForegroundColor Gray
    Write-Host "  Raw Response (first 500 chars):" -ForegroundColor Gray
    $preview = $response.Content.Substring(0, [Math]::Min(500, $response.Content.Length))
    Write-Host "  $preview" -ForegroundColor DarkGray
    
    Write-Host "  Attempting JSON parse..." -ForegroundColor Gray
    $pageData = $response.Content | ConvertFrom-Json
    Write-Host "  Page ID: $($pageData.id)" -ForegroundColor Gray
    Write-Host "  Title: $($pageData.title)" -ForegroundColor Gray
    Write-Host "  Has version: $($pageData.PSObject.Properties.Name -contains 'version')" -ForegroundColor Gray
    Write-Host "  Has body: $($pageData.PSObject.Properties.Name -contains 'body')" -ForegroundColor Gray
    Write-Host "  Response Keys: $($pageData.PSObject.Properties.Name -join ', ')" -ForegroundColor Gray
    
} catch {
    Write-Host "JSON Parsing FAILED" -ForegroundColor Yellow
    Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Yellow
    Write-Host "  Raw Response (first 1000 chars):" -ForegroundColor Gray
    Write-Host "  $($response.Content.Substring(0, [Math]::Min(1000, $response.Content.Length)))" -ForegroundColor DarkGray
}

Write-Host ""

# Test 2: Test old endpoint (what was being used before)
Write-Host "TEST 2: Using old API endpoint (rest/api/content/...)" -ForegroundColor Yellow
$url2 = "$confluenceUrl/rest/api/content/$pageId"
Write-Host "URL: $url2" -ForegroundColor White

try {
    $response2 = Invoke-WebRequest -Uri $url2 -Headers $headers -Method Get -ErrorAction Stop
    Write-Host "SUCCESS - Status: $($response2.StatusCode)" -ForegroundColor Green
} catch {
    Write-Host "FAILED - Status: $($_.Exception.Response.StatusCode)" -ForegroundColor Yellow
    Write-Host "  Note: This endpoint format may not work with Cloud API" -ForegroundColor Gray
}

Write-Host ""
Write-Host "Test Complete" -ForegroundColor Green
