# Quick Confluence Connection Test
$ErrorActionPreference = "Continue"

# Load credentials
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
$pageId = "753673"

# Create auth header
$pair = "$($confluenceUsername):$($confluencePassword)"
$encodedAuth = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($pair))
$headers = @{
    "Authorization" = "Basic $encodedAuth"
    "Accept" = "application/json"
    "Content-Type" = "application/json"
}

Write-Host "===== CONFLUENCE CONNECTION TEST =====" -ForegroundColor Cyan
Write-Host "Base URL: $confluenceUrl" -ForegroundColor White
Write-Host "Page ID: $pageId" -ForegroundColor White
Write-Host "Username: $confluenceUsername" -ForegroundColor White
Write-Host ""

# Test: Fetch page with v3 API
Write-Host "Testing: GET /rest/api/v3/pages/$pageId" -ForegroundColor Yellow
$url = "$confluenceUrl/rest/api/v3/pages/$pageId" + "?body-format=storage"
Write-Host "Full URL: $url" -ForegroundColor Gray
Write-Host ""

try {
    $response = Invoke-WebRequest -Uri $url -Headers $headers -Method Get -ErrorAction Stop
    $statusCode = $response.StatusCode
    
    Write-Host "SUCCESS - Status: $statusCode" -ForegroundColor Green
    
    try {
        $pageData = $response.Content | ConvertFrom-Json
        Write-Host "Valid JSON response" -ForegroundColor Green
        Write-Host "  - Page ID: $($pageData.id)" -ForegroundColor Cyan
        Write-Host "  - Page Title: $($pageData.title)" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "PAGE IS ACCESSIBLE AND API WORKING!" -ForegroundColor Green
    } catch {
        Write-Host "JSON parsing error: $($_.Exception.Message)" -ForegroundColor Red
    }
} catch {
    Write-Host "FAILED" -ForegroundColor Red
    $statusCode = $_.Exception.Response.StatusCode.Value__
    Write-Host "  Status: $statusCode" -ForegroundColor Red
    
    if ($statusCode -eq 404) {
        Write-Host "  Page not found" -ForegroundColor Yellow
    } elseif ($statusCode -eq 403) {
        Write-Host "  Permission denied" -ForegroundColor Yellow
    } elseif ($statusCode -eq 401) {
        Write-Host "  Authentication failed" -ForegroundColor Yellow
    }
}
