# Test Confluence v2 API (legacy but compatible)
$ErrorActionPreference = "Continue"

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

$confluenceUrl = "https://vikaspiprade.atlassian.net/wiki"
$pair = "$($confluenceUsername):$($confluencePassword)"
$encodedAuth = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($pair))
$headers = @{
    "Authorization" = "Basic $encodedAuth"
    "Accept" = "application/json"
    "Content-Type" = "application/json"
}

Write-Host "===== TESTING CONFLUENCE v2 API (LEGACY) =====" -ForegroundColor Cyan
Write-Host ""

# TEST 1: Test v2 API pages
Write-Host "TEST 1: v2 API - Fetch page 688130" -ForegroundColor Yellow
$url1 = "$confluenceUrl/rest/api/2/content/688130"
Write-Host "URL: $url1" -ForegroundColor Gray

try {
    $response = Invoke-WebRequest -Uri $url1 -Headers $headers -Method Get -ErrorAction Stop
    Write-Host "SUCCESS - Status: 200" -ForegroundColor Green
    $pageData = $response.Content | ConvertFrom-Json
    Write-Host "  Page Title: $($pageData.title)" -ForegroundColor Cyan
    Write-Host "  Page Type: $($pageData.type)" -ForegroundColor Cyan
    Write-Host ""
} catch {
    Write-Host "FAILED: $($_.Exception.Response.StatusCode.Value__)" -ForegroundColor Red
}

# TEST 2: Test v2 API pages with 753673
Write-Host "TEST 2: v2 API - Fetch page 753673" -ForegroundColor Yellow
$url2 = "$confluenceUrl/rest/api/2/content/753673"
Write-Host "URL: $url2" -ForegroundColor Gray

try {
    $response = Invoke-WebRequest -Uri $url2 -Headers $headers -Method Get -ErrorAction Stop
    Write-Host "SUCCESS - Status: 200" -ForegroundColor Green
    $pageData = $response.Content | ConvertFrom-Json
    Write-Host "  Page Title: $($pageData.title)" -ForegroundColor Cyan
} catch {
    Write-Host "FAILED: $($_.Exception.Response.StatusCode.Value__)" -ForegroundColor Red
}

# TEST 3: List recent pages
Write-Host "TEST 3: v2 API - List recent pages" -ForegroundColor Yellow
$url3 = "$confluenceUrl/rest/api/2/content?limit=20"
Write-Host "URL: $url3" -ForegroundColor Gray

try {
    $response = Invoke-WebRequest -Uri $url3 -Headers $headers -Method Get -ErrorAction Stop
    Write-Host "SUCCESS" -ForegroundColor Green
    $data = $response.Content | ConvertFrom-Json
    $pageCount = @($data.results).Count
    Write-Host "Found $pageCount pages:" -ForegroundColor Cyan
    Write-Host ""
    $data.results | ForEach-Object {
        Write-Host "  ID: $($_.id) | Title: $($_.title)" -ForegroundColor White
    }
} catch {
    Write-Host "FAILED: $($_.Exception.Response.StatusCode.Value__)" -ForegroundColor Red
}

Write-Host ""
Write-Host "===== TEST COMPLETE =====" -ForegroundColor Cyan
