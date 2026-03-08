# Test Jira API Connection
# This script verifies your Jira credentials are working

$username = "vikas.piprade@gmail.com"
$apiToken = "ATATT3xFfGF0O5hfZAV8EcXq8QjjoAf0EOzq_AmqeYb_SsGRiYcBMP7TTVE0vQmh2gEs2Q6zM7Jx0ITJDfTPIst-n60S07tv1B5XdWZ394iEcHkmZNUoS2T3QS86kMYGgzPdQcK630yDPYJ6a8kCf82Sr9G9iJnOQFYptXQLDP8p2d41Uky84co=50DD32CD"
$jiraUrl = "https://vikaspiprade.atlassian.net"
$ticketKey = "KAN-1"

# Create Basic Auth header
$base64Auth = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("$($username):$($apiToken)"))
$headers = @{
    "Authorization" = "Basic $base64Auth"
    "Content-Type" = "application/json"
}

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "Testing Jira Cloud API Connection" -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Username: $username"
Write-Host "Jira URL: $jiraUrl"
Write-Host "Ticket: $ticketKey"
Write-Host ""

# Test 1: Get issue
$url = "$jiraUrl/rest/api/3/issue/$ticketKey"
Write-Host "Test 1: Fetching issue from: $url" -ForegroundColor Yellow

try {
    $response = Invoke-WebRequest -Uri $url -Headers $headers -Method Get -ErrorAction Stop
    Write-Host "✓ SUCCESS!" -ForegroundColor Green
    Write-Host "Status: $($response.StatusCode)"
    
    $json = $response.Content | ConvertFrom-Json
    Write-Host "Issue Key: $($json.key)"
    Write-Host "Summary: $($json.fields.summary)"
    Write-Host ""
    Write-Host "========================================"
    Write-Host "✓ Your credentials are WORKING!" -ForegroundColor Green
    Write-Host "========================================"
} catch {
    Write-Host "✗ FAILED!" -ForegroundColor Red
    Write-Host "Status Code: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "Possible issues:" -ForegroundColor Yellow
    Write-Host "1. API Token is incorrect or expired"
    Write-Host "2. Username is incorrect"
    Write-Host "3. Jira instance URL is wrong"
    Write-Host "4. You don't have permission to view this ticket"
    Write-Host ""
    Write-Host "If credentials are wrong, regenerate at:" -ForegroundColor Cyan
    Write-Host "https://id.atlassian.com/manage-profile/security/api-tokens"
}
