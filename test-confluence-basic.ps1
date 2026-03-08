# Test basic Confluence connectivity
$ErrorActionPreference = "Continue"

$confluenceUrl = "https://vikaspiprade.atlassian.net/wiki"

Write-Host "===== BASIC CONFLUENCE CONNECTIVITY TEST =====" -ForegroundColor Cyan
Write-Host ""

# TEST 1: Check if Confluence is accessible
Write-Host "TEST 1: Check if Confluence base URL is accessible" -ForegroundColor Yellow
Write-Host "URL: $confluenceUrl" -ForegroundColor Gray

try {
    $response = Invoke-WebRequest -Uri $confluenceUrl -Method Get -ErrorAction Stop
    Write-Host "SUCCESS - Confluence is accessible" -ForegroundColor Green
    Write-Host "  Status: $($response.StatusCode)" -ForegroundColor Cyan
} catch {
    Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# TEST 2: Try direct page URL
Write-Host ""
Write-Host "TEST 2: Test direct page URL (from browser)" -ForegroundColor Yellow
$pageUrl = "https://vikaspiprade.atlassian.net/wiki/spaces/~712020d92d16284df84ee583f2e196894f41fe/pages/688130/Jira+ticket+feasibility"
Write-Host "URL: $pageUrl" -ForegroundColor Gray

try {
    $response = Invoke-WebRequest -Uri $pageUrl -Method Get -ErrorAction Stop
    Write-Host "SUCCESS - Page is accessible via web UI" -ForegroundColor Green
    Write-Host "  Status: $($response.StatusCode)" -ForegroundColor Cyan
    Write-Host "  (This means REST API might be disabled or requires different auth)" -ForegroundColor Yellow
} catch {
    Write-Host "FAILED: $($_.Exception.Response.StatusCode.Value__)" -ForegroundColor Red
}

# TEST 3: Check API endpoint existence
Write-Host ""
Write-Host "TEST 3: Check REST API base endpoint" -ForegroundColor Yellow
$apiUrl = "$confluenceUrl/rest/api/2"
Write-Host "URL: $apiUrl" -ForegroundColor Gray

try {
    $response = Invoke-WebRequest -Uri $apiUrl -Method Get -ErrorAction Stop
    Write-Host "SUCCESS - API base is accessible" -ForegroundColor Green
} catch {
    Write-Host "FAILED: $($_.Exception.Response.StatusCode.Value__)" -ForegroundColor Red
}

# TEST 4: Test with Authorization header at base API
Write-Host ""
Write-Host "TEST 4: Test /rest/api/2/content with auth" -ForegroundColor Yellow

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

if ($confluenceUsername -and $confluencePassword) {
    $pair = "$($confluenceUsername):$($confluencePassword)"
    $encodedAuth = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($pair))
    $headers = @{
        "Authorization" = "Basic $encodedAuth"
        "Accept" = "application/json"
    }
    
    $url = "$confluenceUrl/rest/api/2/content"
    Write-Host "URL: $url" -ForegroundColor Gray
    
    try {
        $response = Invoke-WebRequest -Uri $url -Headers $headers -Method Get -ErrorAction Stop
        Write-Host "SUCCESS - API has content" -ForegroundColor Green
        $data = $response.Content | ConvertFrom-Json
        Write-Host "  Found $($data.results.Count) pages" -ForegroundColor Cyan
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.Value__
        Write-Host "FAILED: $statusCode" -ForegroundColor Red
        
        if ($statusCode -eq 401) {
            Write-Host "  ERROR: Unauthorized - check credentials" -ForegroundColor Yellow
        } elseif ($statusCode -eq 403) {
            Write-Host "  ERROR: Forbidden - may need different API token scope" -ForegroundColor Yellow
        } elseif ($statusCode -eq 404) {
            Write-Host "  ERROR: Endpoint not found - REST API may be disabled" -ForegroundColor Yellow
        }
    }
}

Write-Host ""
Write-Host "===== TEST COMPLETE =====" -ForegroundColor Cyan
