# Skill Pipeline Orchestrator - Quick Start Guide

## Running the Pipeline for KAN-1

### Option 1: Run as Java Application (Easiest)

#### Step 1: Set Environment Variables
```bash
# Windows PowerShell
$env:JIRA_USERNAME = "your_jira_username"
$env:JIRA_PASSWORD = "your_jira_password"
$env:CONFLUENCE_USERNAME = "your_confluence_username"
$env:CONFLUENCE_PASSWORD = "your_confluence_password"
$env:GITHUB_TOKEN = "your_github_token"

# Windows CMD
set JIRA_USERNAME=your_jira_username
set JIRA_PASSWORD=your_jira_password
set CONFLUENCE_USERNAME=your_confluence_username
set CONFLUENCE_PASSWORD=your_confluence_password
set GITHUB_TOKEN=your_github_token
```

#### Step 2: Compile and Run
```bash
# Navigate to project directory
cd D:\seleniumjavaproject\UI-Automation_Selenium

# Compile the project
mvn clean compile

# Run the orchestrator for KAN-1
mvn exec:java@run-orchestrator -DjiraTicket=KAN-1
```

---

### Option 2: Run from IDE (IntelliJ / Eclipse)

#### IntelliJ IDEA:
1. Right-click on `RunSkillPipeline.java`
2. Select **Run 'RunSkillPipeline.main()'**
3. View output in the Run panel

#### Eclipse:
1. Right-click on `RunSkillPipeline.java`
2. Select **Run As > Java Application**

---

### Option 3: Run Programmatically in Your Own Code

```java
import com.swag.labs.Skills.SkillPipelineOrchestrator;

public class MyScript {
    public static void main(String[] args) {
        // Initialize orchestrator
        SkillPipelineOrchestrator orchestrator = new SkillPipelineOrchestrator();
        
        // Run full pipeline for KAN-1
        orchestrator.runFullPipeline("KAN-1");
    }
}
```

---

### Option 4: Run Individual Skills

```java
import com.swag.labs.Skills.Feasibility.JiraFeasibilitySkill;
import com.swag.labs.Skills.TestGeneration.JiraTestGeneratorSkill;
import com.swag.labs.Skills.Core.FeasibilityResult;

// Skill 1: Feasibility Check Only
JiraFeasibilitySkill feasibilitySkill = new JiraFeasibilitySkill();
FeasibilityResult result = feasibilitySkill.runFeasibilityCheck("KAN-1");
System.out.println("Automatable: " + result.isAutomatable());

// Skill 2: Test Generation (if feasibility passed)
if (result.isAutomatable()) {
    JiraTestGeneratorSkill testGen = new JiraTestGeneratorSkill();
    testGen.generateTests("KAN-1", result);
}
```

---

## Expected Output

When you run the full pipeline, you'll see output like:

```
╔════════════════════════════════════════════════════════════════════════╗
║  SKILL PIPELINE ORCHESTRATOR - Full Execution Started                  ║
╚════════════════════════════════════════════════════════════════════════╝

[SKILL 1/4] Jira Feasibility Check
────────────────────────────────────────────────────────────────────────────
[SKILL] JiraFeasibilitySkill - ========== Starting Feasibility Check for KAN-1 ==========
[SKILL] JiraClient - Fetching Jira ticket: KAN-1
✅ Ticket is feasible for automation (Score: 0.85)

[SKILL 2/4] Test Generation
────────────────────────────────────────────────────────────────────────────
[SKILL] JiraTestGeneratorSkill - ========== Starting Test Generation for KAN-1 ==========
[SKILL] JiraTicketParser - Parsing ticket: KAN-1
[SKILL] TestClassGenerator - Generating test class for ticket: KAN-1
✅ Test generated successfully: Test_GeneralEpic_KAN1

[SKILL 3/4] Test Execution with Self-Healing
────────────────────────────────────────────────────────────────────────────
[SKILL] TestRunnerHealerSkill - ========== Starting Test Runner/Healer for Test_GeneralEpic_KAN1.testScenario1 ==========
---- Attempt 1 of 3 ----
✅ Test passed on attempt 1

[SKILL 4/4] Pull Request Creation
────────────────────────────────────────────────────────────────────────────
[SKILL] PullRequestCreatorSkill - ========== Starting PR Creation for KAN-1 ==========
✅ PR created successfully: https://github.com/VikasPiprade/UI-Automation_Selenium/pull/42
✅ Confluence page updated

╔════════════════════════════════════════════════════════════════════════╗
║  SKILL PIPELINE ORCHESTRATOR - Execution Completed Successfully! ✅    ║
╚════════════════════════════════════════════════════════════════════════╝

EXECUTION SUMMARY
════════════════════════════════════════════════════════════════════════════
Jira Ticket: KAN-1
  Feasibility: PASS (Score: 0.85)
  Test Generated: Test_GeneralEpic_KAN1 (Path: src/test/java/com/swag/labs/Tests/Test_GeneralEpic_KAN1.java)
  PR Created: #42 - https://github.com/VikasPiprade/UI-Automation_Selenium/pull/42
════════════════════════════════════════════════════════════════════════════
```

---

## Artifacts Generated

After successful execution, check these locations:

### Test Class
```
src/test/java/com/swag/labs/Tests/
└── Test_GeneralEpic_KAN1.java
```

### Page Objects
```
src/main/java/com/swag/labs/PageObjects/
└── Generated_GeneralEpic.java
```

### Test Data
```
src/test/resources/TestData/GeneralEpic/
└── KAN-1_testdata.csv
```

### Confluence
Feasibility page updated with: score, status, analysis, PR link

### GitHub
New PR created: `feature/kan-1-automated-test`

---

## Troubleshooting

### Issue: "Jira configuration incomplete"
**Solution**: Set environment variables:
```bash
set JIRA_USERNAME=your_username
set JIRA_PASSWORD=your_password
```

### Issue: "Failed to fetch Jira ticket: KAN-1"
**Solution**: Verify:
- Environment variables are set
- Jira URL in config.properties is correct
- Jira username/password are valid
- Ticket KAN-1 exists in Jira

### Issue: "Test execution failed"
**Solution**: 
- Check generated test class for compilation errors
- Review generated page objects - may need locator updates
- Check Selenium WebDriver and browser compatibility

### Issue: "Failed to create pull request"
**Solution**:
- Verify GitHub token is valid
- Check Git is installed and working
- Verify branch naming conflicts don't exist
- Ensure you have push permissions on the repo

---

## Customization

### Change Retry Attempts
Edit `config.properties`:
```
skills.max-retry-attempts=5
```

### Change Page IDs / Endpoints
Edit `config.properties`:
```
jira.url=https://your-jira-instance.atlassian.net
confluence.url=https://your-confluence.atlassian.net/wiki
github.owner=your-github-org
github.repo=your-repo-name
confluence.feasibility.page.id=YOUR_PAGE_ID
```

### Run Different Ticket
Simply change the ticket key in the orchestrator call:
```java
orchestrator.runFullPipeline("JIRA-123");
```

---

## Next Steps

1. **Customize generated tests** - Update assertions and test logic
2. **Update page object locators** - Based on your app's actual elements
3. **Run full test suite** - Verify no regressions
4. **Add to CI/CD** - Integrate into your build pipeline

---

Generated: 2026-03-09
