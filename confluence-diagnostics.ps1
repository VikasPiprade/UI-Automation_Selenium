# Comprehensive Confluence Cloud API v3 Diagnostic
# Helps identify authentication and API issues

$ErrorActionPreference = "Continue"

# Load credentials from .env
$confluenceUsername = $null
$confluencePassword = $null

if (Test-Path ".env") {
    Get-Content ".env" | ForEach-Object {
        if ($_ -match '^([^=]+)=(.*)$') {
            $name = $matches[1]
            $value = $matches[2]
            if ($name -eq "CONFLUENCE_USERNAME") { $confluenceUsername = $value }
            if ($name -eq "CONFLUENCE_PASSWORD") { $confluencePassword = $value }
        }
    }
}

if (-not $confluenceUsername -or -not $confluencePassword) {
    Write-Host "ERROR: Credentials not found" -ForegroundColor Red
    exit 1
}

$confluenceUrl = "https://vikaspiprade.atlassian.net/wiki"
$pageId = "688130"

# Create Basic Auth header
$pair = "$($confluenceUsername):$($confluencePassword)"
$encodedAuth = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($pair))
$headers = @{
    "Authorization" = "Basic $encodedAuth"
    "Accept" = "application/json"
    "Content-Type" = "application/json"
    "X-Atlassian-Token" = "no-check"
}

Write-Host "===== CONFLUENCE CLOUD API v3 DIAGNOSTICS =====" -ForegroundColor Cyan
Write-Host "Base URL: $confluenceUrl" -ForegroundColor White
Write-Host "Page ID: $pageId" -ForegroundColor White
Write-Host "Username: $confluenceUsername" -ForegroundColor White
Write-Host ""

# Test 1: Get page with v3 API
Write-Host "TEST 1: Fetch page using v3 API" -ForegroundColor Yellow
$url1 = "$confluenceUrl/api/v3/pages/$pageId`?body-format=storage"
Write-Host "URL: $url1" -ForegroundColor Gray

try {
    $response = Invoke-WebRequest -Uri $url1 -Headers $headers -Method Get -ErrorAction Stop
    $statusCode = $response.StatusCode
    
    Write-Host "Status Code: $statusCode" -ForegroundColor Green
    
    if ($statusCode -eq 200) {
        Write-Host "SUCCESS - Page fetched" -ForegroundColor Green
        try {
            $pageData = $response.Content | ConvertFrom-Json
            Write-Host "  - Valid JSON response" -ForegroundColor Green
            Write-Host "  - Page ID: $($pageData.id)" -ForegroundColor Gray
            Write-Host "  - Page Title: $($pageData.title)" -ForegroundColor Gray
        } catch {
            Write-Host "  - JSON parse error: $($_.Exception.Message)" -ForegroundColor Red
        }
    } elseif ($statusCode -eq 404) {
        Write-Host "NOT FOUND - Page does not exist" -ForegroundColor Yellow
    } elseif ($statusCode -eq 403) {
        Write-Host "FORBIDDEN - No permission" -ForegroundColor Red
    } elseif ($statusCode -eq 401) {
        Write-Host "UNAUTHORIZED - Auth failed" -ForegroundColor Red
    } else {
        Write-Host "Unexpected status: $statusCode" -ForegroundColor Yellow
    }
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Test 2: Try to list spaces in personal area
Write-Host "TEST 2: Fetch user profile to verify auth" -ForegroundColor Yellow
$url2 = "$confluenceUrl/api/v3/users/me"
Write-Host "URL: $url2" -ForegroundColor Gray

try {
    $response = Invoke-WebRequest -Uri $url2 -Headers $headers -Method Get -ErrorAction Stop
    $statusCode = $response.StatusCode
    
    Write-Host "Status Code: $statusCode" -ForegroundColor Green
    
    if ($statusCode -eq 200) {
        Write-Host "SUCCESS - Authentication working" -ForegroundColor Green
        $userData = $response.Content | ConvertFrom-Json
        Write-Host "  - Current User: $($userData.username)" -ForegroundColor Gray
    } else {
        Write-Host "FAILED - Auth not working. Status: $statusCode" -ForegroundColor Red
    }
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Test 3: Search for accessible pages
Write-Host "TEST 3: Search for pages" -ForegroundColor Yellow
$url3 = "$confluenceUrl/api/v3/pages?title=feasibility" + "&limit=10"
Write-Host "URL: $url3" -ForegroundColor Gray

try {
    $response = Invoke-WebRequest -Uri $url3 -Headers $headers -Method Get -ErrorAction Stop
    $statusCode = $response.StatusCode
    
    Write-Host "Status Code: $statusCode" -ForegroundColor Green
    
    if ($statusCode -eq 200) {
        $results = $response.Content | ConvertFrom-Json
        Write-Host "SUCCESS - Search completed" -ForegroundColor Green
    }
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Yellow
}

Write-Host ""

# Test 4: Try creating a test page
Write-Host "TEST 4: Create a test page" -ForegroundColor Yellow
$testPageData = @{
    "spaceId" = "~712020d92d16284df84ee583f2e196894f41fe"
    "type" = "page"
    "title" = "Test Page"
    "body" = @{
        "representation" = "storage"
        "value" = "<p>Test content</p>"
    }
} | ConvertTo-Json

$url4 = "$confluenceUrl/api/v3/pages"
Write-Host "URL: $url4" -ForegroundColor Gray

try {
    $response = Invoke-WebRequest -Uri $url4 -Headers $headers -Method Post -Body $testPageData -ErrorAction Stop
    $statusCode = $response.StatusCode
    
    Write-Host "Status Code: $statusCode" -ForegroundColor Green
} catch {
    Write-Host "Info: $($_.Exception.Message)" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "===== DIAGNOSTICS COMPLETE =====" -ForegroundColor Cyan
Write-Host ""
Write-Host "RECOMMENDATIONS:" -ForegroundColor Green
Write-Host "1. If AUTH test fails: Verify credentials are correct (email + API token)" -ForegroundColor White
Write-Host "2. If page 404: Use the page ID from the full URL or create a new test page" -ForegroundColor White
Write-Host "3. If 403 Forbidden: Check permissions on the personal space" -ForegroundColor White
Write-Host "4. If CREATE page succeeds: Use that page ID for feasibility reports" -ForegroundColor White
