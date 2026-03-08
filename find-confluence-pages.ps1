# Find Accessible Confluence Pages
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

$confluenceUrl = "https://vikaspiprade.atlassian.net/wiki"
$pair = "$($confluenceUsername):$($confluencePassword)"
$encodedAuth = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($pair))
$headers = @{
    "Authorization" = "Basic $encodedAuth"
    "Accept" = "application/json"
    "Content-Type" = "application/json"
}

Write-Host "===== CONFLUENCE DIAGNOSTIC =====" -ForegroundColor Cyan
Write-Host ""

# TEST 1: Verify Auth Works
Write-Host "TEST 1: Verify Authentication" -ForegroundColor Yellow
$userUrls = @(
    "$confluenceUrl/api/v3/users/me",
    "$confluenceUrl/rest/api/v3/users/me", 
    "https://vikaspiprade.atlassian.net/rest/api/v3/users/me",
    "https://vikaspiprade.atlassian.net/api/v3/users/me"
)

$authWorking = $false
foreach ($url in $userUrls) {
    Write-Host "  Trying: $url" -ForegroundColor Gray
    try {
        $response = Invoke-WebRequest -Uri $url -Headers $headers -Method Get -ErrorAction Stop
        $userData = $response.Content | ConvertFrom-Json
        Write-Host "  SUCCESS on $url" -ForegroundColor Green
        Write-Host "  User: $($userData.email)" -ForegroundColor Cyan
        $authWorking = $true
        Write-Host ""
        break
    } catch {
        Write-Host "  Failed: $($_.Exception.Response.StatusCode.Value__)" -ForegroundColor DarkGray
    }
}

if (-not $authWorking) {
    Write-Host "Authentication test failed. API may not be accessible." -ForegroundColor Red
    Write-Host ""
}

# TEST 2: Search for pages
Write-Host "TEST 2: Search for Accessible Pages" -ForegroundColor Yellow
$url2 = "$confluenceUrl/api/v3/pages?limit=50"
Write-Host "URL: $url2" -ForegroundColor Gray

try {
    $response = Invoke-WebRequest -Uri $url2 -Headers $headers -Method Get -ErrorAction Stop
    $data = $response.Content | ConvertFrom-Json
    $pageCount = @($data.results).Count
    
    if ($pageCount -gt 0) {
        Write-Host "Found $pageCount accessible pages:" -ForegroundColor Green
        Write-Host ""
        $data.results | ForEach-Object {
            Write-Host "  ID: $($_.id)" -ForegroundColor Cyan
            Write-Host "  Title: $($_.title)" -ForegroundColor White
            Write-Host "  Space: $($_.spaceId)" -ForegroundColor Gray
            Write-Host ""
        }
    } else {
        Write-Host "No accessible pages found" -ForegroundColor Yellow
    }
} catch {
    Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# TEST 3: Test page 688130 (original page)
Write-Host "TEST 3: Test Original Page ID (688130)" -ForegroundColor Yellow
$url3 = "$confluenceUrl/rest/api/v3/pages/688130?body-format=storage"
Write-Host "URL: $url3" -ForegroundColor Gray

try {
    $response = Invoke-WebRequest -Uri $url3 -Headers $headers -Method Get -ErrorAction Stop
    Write-Host "SUCCESS - Page accessible!" -ForegroundColor Green
    $page = $response.Content | ConvertFrom-Json
    Write-Host "  Page: $($page.title)" -ForegroundColor Cyan
} catch {
    Write-Host "FAILED: $($_.Exception.Response.StatusCode.Value__)" -ForegroundColor Yellow
}

# TEST 4: Test page 753673 (new page ID)
Write-Host "TEST 4: Test New Page ID (753673)" -ForegroundColor Yellow
$url4 = "$confluenceUrl/rest/api/v3/pages/753673?body-format=storage"
Write-Host "URL: $url4" -ForegroundColor Gray

try {
    $response = Invoke-WebRequest -Uri $url4 -Headers $headers -Method Get -ErrorAction Stop
    Write-Host "SUCCESS - Page accessible!" -ForegroundColor Green
    $page = $response.Content | ConvertFrom-Json
    Write-Host "  Page: $($page.title)" -ForegroundColor Cyan
} catch {
    Write-Host "FAILED: $($_.Exception.Response.StatusCode.Value__)" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "===== DIAGNOSTIC COMPLETE =====" -ForegroundColor Cyan
Write-Host "RECOMMENDATION: Use one of the accessible page IDs above" -ForegroundColor White
