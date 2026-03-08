# Test both Confluence URL formats
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

$pair = "$($confluenceUsername):$($confluencePassword)"
$encodedAuth = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($pair))
$headers = @{
    "Authorization" = "Basic $encodedAuth"
    "Accept" = "application/json"
    "Content-Type" = "application/json"
}

$pageId = "688130"

Write-Host "Testing different Confluence URL formats..." -ForegroundColor Cyan
Write-Host ""

# Format 1: With /wiki in base URL
Write-Host "FORMAT 1: /wiki/rest/api/v3/pages" -ForegroundColor Yellow
$url1 = "https://vikaspiprade.atlassian.net/wiki/rest/api/v3/pages/$pageId"
Write-Host "URL: $url1" -ForegroundColor Gray

try {
    $response = Invoke-WebRequest -Uri $url1 -Headers $headers -Method Get -ErrorAction Stop
    Write-Host "Status: $($response.StatusCode) - SUCCESS" -ForegroundColor Green
    try {
        $data = $response.Content | ConvertFrom-Json
        Write-Host "  Valid JSON - Page: $($data.title)" -ForegroundColor Green
    } catch {
        Write-Host "  Invalid JSON" -ForegroundColor Yellow
    }
} catch {
    Write-Host "Failed: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
}

Write-Host ""

# Format 2: Without /wiki
Write-Host "FORMAT 2: /rest/api/v3/pages (no /wiki)" -ForegroundColor Yellow
$url2 = "https://vikaspiprade.atlassian.net/rest/api/v3/pages/$pageId"
Write-Host "URL: $url2" -ForegroundColor Gray

try {
    $response = Invoke-WebRequest -Uri $url2 -Headers $headers -Method Get -ErrorAction Stop
    Write-Host "Status: $($response.StatusCode) - SUCCESS" -ForegroundColor Green
    try {
        $data = $response.Content | ConvertFrom-Json
        Write-Host "  Valid JSON - Page: $($data.title)" -ForegroundColor Green
    } catch {
        Write-Host "  Invalid JSON" -ForegroundColor Yellow
    }
} catch {
    Write-Host "Failed: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
}

Write-Host ""

# Format 3: /wiki/api/v3 (different API path)
Write-Host "FORMAT 3: /wiki/api/v3/pages" -ForegroundColor Yellow
$url3 = "https://vikaspiprade.atlassian.net/wiki/api/v3/pages/$pageId"
Write-Host "URL: $url3" -ForegroundColor Gray

try {
    $response = Invoke-WebRequest -Uri $url3 -Headers $headers -Method Get -ErrorAction Stop
    Write-Host "Status: $($response.StatusCode) - SUCCESS" -ForegroundColor Green
    try {
        $data = $response.Content | ConvertFrom-Json
        Write-Host "  Valid JSON - Page: $($data.title)" -ForegroundColor Green
    } catch {
        Write-Host "  Invalid JSON" -ForegroundColor Yellow
    }
} catch {
    Write-Host "Failed: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
}

Write-Host ""

# Format 4: /wiki/rest/api/2/content (old API)
Write-Host "FORMAT 4: /wiki/rest/api/2/content (legacy)" -ForegroundColor Yellow
$url4 = "https://vikaspiprade.atlassian.net/wiki/rest/api/2/content/$pageId"
Write-Host "URL: $url4" -ForegroundColor Gray

try {
    $response = Invoke-WebRequest -Uri $url4 -Headers $headers -Method Get -ErrorAction Stop
    Write-Host "Status: $($response.StatusCode) - SUCCESS" -ForegroundColor Green
} catch {
    Write-Host "Failed: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
}

Write-Host ""
Write-Host "TEST COMPLETE" -ForegroundColor Cyan
