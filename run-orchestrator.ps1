# ============================================
# Skill Pipeline Orchestrator Runner
# ============================================
# Edit the values below with your actual credentials

$env:JIRA_USERNAME = "vikas.piprade@gmail.com"
$env:JIRA_PASSWORD = "ATATT3xFfGF0O5hfZAV8EcXq8QjjoAf0EOzq_AmqeYb_SsGRiYcBMP7TTVE0vQmh2gEs2Q6zM7Jx0ITJDfTPIst-n60S07tv1B5XdWZ394iEcHkmZNUoS2T3QS86kMYGgzPdQcK630yDPYJ6a8kCf82Sr9G9iJnOQFYptXQLDP8p2d41Uky84co=50DD32CD"
$env:CONFLUENCE_USERNAME = "vikas.piprade@gmail.com"
$env:CONFLUENCE_PASSWORD = "ATATT3xFfGF0O5hfZAV8EcXq8QjjoAf0EOzq_AmqeYb_SsGRiYcBMP7TTVE0vQmh2gEs2Q6zM7Jx0ITJDfTPIst-n60S07tv1B5XdWZ394iEcHkmZNUoS2T3QS86kMYGgzPdQcK630yDPYJ6a8kCf82Sr9G9iJnOQFYptXQLDP8p2d41Uky84co=50DD32CD"
$env:GITHUB_TOKEN = "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"

# Verify credentials are set
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "Environment Variables Set:" -ForegroundColor Green
Write-Host "JIRA_USERNAME:       $env:JIRA_USERNAME"
Write-Host "JIRA_PASSWORD:       [SET]"
Write-Host "CONFLUENCE_USERNAME: $env:CONFLUENCE_USERNAME"
Write-Host "CONFLUENCE_PASSWORD: [SET]"
Write-Host "GITHUB_TOKEN:        [SET]"
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# Run the orchestrator
Write-Host "Running orchestrator for KAN-6..." -ForegroundColor Yellow
mvn exec:java@run-orchestrator -DjiraTicket=KAN-6
