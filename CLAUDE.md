# Jenkins Test Environment

A Docker-based Jenkins test environment demonstrating multiple pipeline architecture options.

## Quick Start

```bash
./scripts/start.sh
```

Access at http://localhost:8080

**Users:**
| User | Password | Access |
|------|----------|--------|
| admin | admin | Full administrator |
| dev1 | dev1 | mobile-pipeline, pipeline-1-hybrid, pipeline-2-blueocean |
| dev2 | dev2 | pipeline-3-skip-params, pipeline-4a, pipeline-3-mb, pipeline-4a-mb, pipeline-4b-mb |

## Pipeline Architecture Options

This repo implements 8 different approaches to Jenkins pipelines for comparison:

| Folder | Option | Orchestration | Job Type | Re-triggerable | Smart Re-run |
|--------|--------|---------------|----------|----------------|--------------|
| `mobile-pipeline` | 0: Current | Job DSL triggers | Free-style | Yes | No |
| `pipeline-1-hybrid` | 1: Hybrid | Jenkinsfile `build job:` | Free-style | Yes | No |
| `pipeline-2-blueocean` | 2: Blue Ocean | Single Jenkinsfile | Pipeline | Via Blue Ocean | No |
| `pipeline-3-skip-params` | 3: Skip Params | Single Jenkinsfile | Pipeline | Via params | No |
| `pipeline-4a` | 4A: Jenkinsfile + Pipeline Jobs | Jenkinsfile `build job:` | Pipeline | Yes | No |
| `pipeline-3-mb` | 3-MB: Skip Params (GitHub) | Single Jenkinsfile | Multibranch | Via params | Yes (Checks API) |
| `pipeline-4a-mb` | 4A-MB: Orchestrator (GitHub) | Jenkinsfile `build job:` | Multibranch + Pipeline | Yes | Yes (Checks API) |
| `pipeline-4b-mb` | 4B-MB: Orchestrator + Commit Status (GitHub) | Jenkinsfile `build job:` | Multibranch + Pipeline | Yes (child jobs) | N/A (Commit Status) |

### Option 0: Current (Job DSL Free-Style)
- **Folder:** `mobile-pipeline`
- **How it works:** Job DSL creates free-style jobs with `downstreamParameterized` triggers
- **Pros:** Simple, Delivery Pipeline view works, re-triggerable
- **Cons:** Pipeline definition not versioned with code

### Option 1: Hybrid (Jenkinsfile → Free-Style Jobs)
- **Folder:** `pipeline-1-hybrid`
- **How it works:** `Jenkinsfile.1-hybrid` uses `build job:` to call free-style jobs
- **Pros:** Pipeline-as-code, re-triggerable jobs
- **Cons:** Two config locations (Jenkinsfile + Job DSL)

### Option 2: Blue Ocean (Restart from Stage)
- **Folder:** `pipeline-2-blueocean`
- **How it works:** Single `Jenkinsfile.2-blueocean` with all stages
- **Pros:** Single pipeline definition, native restart feature
- **Cons:** Only works in Blue Ocean UI, limited restart options

### Option 3: Skip Params (Parameterized Pipeline)
- **Folder:** `pipeline-3-skip-params`
- **How it works:** `Jenkinsfile.3-skip-params` with `SKIP_*` boolean parameters
- **Pros:** Simple, single pipeline
- **Cons:** Manual parameter management, re-runs entire pipeline

### Option 4A: Jenkinsfile Orchestrator + Pipeline Jobs
- **Folder:** `pipeline-4a`
- **How it works:** `ci/trigger.Jenkinsfile` uses `build job:` to call pipeline jobs, each reading their own `ci/*.Jenkinsfile`
- **Pros:** All pipeline logic in Jenkinsfiles, re-triggerable jobs, job implementations versioned
- **Cons:** Job DSL still needed to create job definitions

**Note:** `pipelineJob` doesn't support `publishers { downstreamParameterized }`, so Job DSL can only define the jobs, not orchestrate them. Orchestration must be done via Jenkinsfile.

### Option 3-MB: Skip Params Multibranch (GitHub) — with Smart Re-run
- **Folder:** `pipeline-3-mb`
- **GitHub repo:** [jenkinsfiles-test-app](https://github.com/gosuwachu/jenkinsfiles-test-app)
- **How it works:** Multibranch pipeline discovers branches/PRs from GitHub, runs `Jenkinsfile.3-skip-params`
- **Smart re-run:** Detects GitHub Check re-runs, queries Checks API, skips already-passed stages
- **Pros:** Automatic PR discovery, skip-params flexibility, smart re-run from GitHub
- **Cons:** Re-run still triggers the pipeline (but only failed stages execute)

### Option 4A-MB: Orchestrator Multibranch + Pipeline Jobs (GitHub) — with Smart Re-run
- **Folder:** `pipeline-4a-mb`
- **GitHub repo:** [jenkinsfiles-test-app](https://github.com/gosuwachu/jenkinsfiles-test-app)
- **How it works:** Multibranch orchestrator discovers branches/PRs, triggers child `pipelineJob`s passing `BRANCH_NAME` parameter
- **Smart re-run:** Detects GitHub Check re-runs, queries Checks API, skips child jobs whose checks already passed
- **Pros:** PR discovery, re-triggerable child jobs, smart re-run from GitHub
- **Cons:** Child jobs are not multibranch (regular pipeline jobs with branch parameter)

### Option 4B-MB: Orchestrator + Commit Status API (GitHub)
- **Folder:** `pipeline-4b-mb`
- **GitHub repo:** [jenkinsfiles-test-app](https://github.com/gosuwachu/jenkinsfiles-test-app)
- **How it works:** Multibranch orchestrator triggers child `pipelineJob`s; each child job publishes its own GitHub commit status (not Checks API)
- **Pros:** Each child job owns its status reporting, `target_url` links to child job build page, individually re-triggerable from Jenkins
- **Cons:** No "Re-run" button from GitHub (commit statuses don't support it), no rich check details

## Project Structure

```
├── Jenkinsfile.1-hybrid           # Option 1 orchestrator
├── Jenkinsfile.2-blueocean        # Option 2 single pipeline
├── Jenkinsfile.3-skip-params      # Option 3 with skip params
├── ci/
│   ├── trigger.Jenkinsfile        # Option 4A orchestrator
│   ├── ios-build.Jenkinsfile      # Used by Option 4A
│   ├── ios-deploy.Jenkinsfile
│   ├── android-build.Jenkinsfile
│   └── ...
├── jobs/
│   └── pipeline.groovy            # Job DSL (all options)
├── casc/
│   └── jenkins.yaml               # Jenkins Configuration as Code
├── init.groovy.d/
│   └── create-github-app-credential.groovy  # GitHub App credential setup
├── Dockerfile
├── docker-compose.yml
├── plugins.txt
├── .env                           # GitHub credentials (gitignored)
├── github-app-key.pem             # GitHub App private key (gitignored)
└── scripts/
    └── start.sh
```

**Companion repo:** [jenkinsfiles-test-app](https://github.com/gosuwachu/jenkinsfiles-test-app) - contains Jenkinsfiles for multibranch options (3-MB, 4A-MB, 4B-MB).

## Common Commands

```bash
# Start Jenkins
./scripts/start.sh

# Stop Jenkins
docker-compose down

# View logs
docker-compose logs -f

# Reset Jenkins (clear all data)
docker-compose down -v && docker-compose up -d
```

## Jenkins API Helper

Use `scripts/jenkins-api.sh` for API interactions (handles crumb authentication automatically):

```bash
# Trigger a build
./scripts/jenkins-api.sh build <job-path>
./scripts/jenkins-api.sh build pipeline-2-blueocean/job/pipeline

# Get console log (default: lastBuild)
./scripts/jenkins-api.sh log <job-path> [build#]
./scripts/jenkins-api.sh log pipeline-2-blueocean/job/pipeline
./scripts/jenkins-api.sh log mobile-pipeline/job/trigger 5

# Get job status
./scripts/jenkins-api.sh status <job-path>
./scripts/jenkins-api.sh status pipeline-2-blueocean/job/pipeline
```

**Note:** Job paths use `/job/` between folder and job name (e.g., `folder/job/jobname`).

## Modifying Jobs

Edit `jobs/pipeline.groovy` then run the **seed-job** in Jenkins:
- http://localhost:8080/job/seed-job/ → Build Now

For Jenkinsfile changes, just re-run the job (it pulls from repo).

Only rebuild Docker when changing `Dockerfile`, `plugins.txt`, or `casc/jenkins.yaml`:
```bash
docker-compose build && docker-compose up -d
```

## Per-Project Permissions

This environment uses **Project-based Matrix Authorization Strategy** (via `matrix-auth` plugin) to restrict folder visibility per user.

**Configuration:**
- Global permissions in `casc/jenkins.yaml` (Overall/Read for authenticated users)
- Folder permissions in `jobs/pipeline.groovy` using Job DSL

**Job DSL syntax for folder permissions (matrix-auth > 3.0):**
```groovy
folder('my-folder') {
    authorization {
        userPermissions('username', [
            'hudson.model.Item.Discover',
            'hudson.model.Item.Read',
            'hudson.model.Item.Build',
            'hudson.model.Item.Workspace'
        ])
    }
}
```

**Available methods:**
- `userPermissions(userName, permissionsList)` - grant multiple permissions to a user
- `userPermissionAll(userName)` - grant all permissions to a user
- `groupPermissions(groupName, permissionsList)` - grant permissions to a group

## GitHub Integration (Multibranch Pipelines)

Options 3-MB, 4A-MB, and 4B-MB use multibranch pipelines that discover branches/PRs from GitHub.

**Setup:**
1. Create a GitHub PAT with `repo` and `admin:repo_hook` scopes
2. Create a GitHub App (see "GitHub App & Per-Stage Checks" below)
3. Add credentials to `.env`:
   ```
   GITHUB_USERNAME=gosuwachu
   GITHUB_PAT=ghp_xxxx
   GITHUB_APP_ID=<app-id>
   ```
4. Place the GitHub App private key as `github-app-key.pem` (PKCS#8 format, gitignored)
5. Rebuild: `docker-compose build && docker-compose up -d`
6. Run seed-job to create the multibranch jobs

**Webhook setup (for instant PR triggers):**
1. Start ngrok: `ngrok http 8080`
2. In GitHub repo Settings > Webhooks > Add webhook:
   - Payload URL: `https://<ngrok-url>/github-webhook/` (trailing slash required)
   - Content type: `application/json`
   - Events: Push + Pull requests

Without a webhook, Jenkins polls GitHub every 5 minutes via `periodicFolderTrigger`.

## GitHub App & Per-Stage Checks

Each pipeline stage (iOS Build, Android Tests, etc.) publishes a **separate GitHub Check** on PRs, so you can see at a glance which stage passed or failed. This requires GitHub App authentication (PATs cannot use the Checks API).

**GitHub App:** `jenkinsfiles-test-github-app` (App ID in `.env`)

**Required GitHub App permissions:**
- Checks: Read & Write (for `publishChecks`)
- Contents: Read (for SCM checkout)
- Pull requests: Read (for PR discovery)
- Metadata: Read
- Commit statuses: Read & Write (required for 4B-MB commit status publishing, also suppresses a 403 warning for other options)

**How it works:**
- `init.groovy.d/create-github-app-credential.groovy` reads the PEM key at startup and creates a `github-app` credential
- Multibranch pipelines use `github-app` credential for scanning and checkout
- Jenkinsfiles call `publishChecks` per stage (IN_PROGRESS → SUCCESS/FAILURE)
- Check names include a suffix like `(3-MB)` or `(4A-MB)` to distinguish pipeline options

**Key files:**
| File | Purpose |
|------|---------|
| `github-app-key.pem` | GitHub App private key (PKCS#8, gitignored) |
| `init.groovy.d/create-github-app-credential.groovy` | Creates `github-app` credential at startup |
| `plugins.txt` | Includes `checks-api` and `github-checks` plugins |

**PEM key format:** Must be PKCS#8 (`BEGIN PRIVATE KEY`). Convert from PKCS#1 if needed:
```bash
openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in key.pem -out github-app-key.pem
```

## Smart Re-run (Failed Stage Only)

Options 3-MB and 4A-MB support **smart re-run**: when a pipeline stage fails and you click "Re-run" on the failed GitHub Check, only the failed stages re-execute — stages that already passed are skipped.

**How it works:**
1. Build detects `GitHubChecksRerunActionCause` via `currentBuild.getBuildCauses()`
2. Queries GitHub Checks API for current check statuses on the commit
3. Skips stages whose checks already have `success` conclusion
4. Runs all other stages (failed, neutral, or missing)

**Manual override — `ONLY_STAGE` parameter:**
Both 3-MB and 4A-MB accept an `ONLY_STAGE` string parameter. Set it to a stage name (e.g., `"Android Tests"`) to run only that stage and skip everything else.

**Edge cases:**
- If ALL checks already passed, all stages re-run (assumes user wants a full re-run)
- If the GitHub API call fails, all stages run (fail-open)
- Check suffixes `(3-MB)` / `(4A-MB)` prevent cross-contamination between pipeline options

**Option 4B-MB uses Commit Status API instead:**
Child jobs publish their own commit statuses via `POST /repos/{owner}/{repo}/statuses/{sha}`. No "Re-run" button from GitHub, but each child job can be individually re-triggered from Jenkins UI. The status `target_url` links directly to the child job's build page.

## Job DSL Notes

- **Git branch is `main`** (not `master`) - configured in `jobs/pipeline.groovy` as `def branch = '*/main'`
- Job DSL `targets()` only accepts relative paths (Ant GLOB), not absolute paths
- For parameterized-trigger, use `triggerWithNoParameters()` not `parameters { currentBuild() }`
- The seed job copies DSL files to workspace before processing
- `pipelineJob` uses `cpsScm` to read Jenkinsfiles from SCM
- **`pipelineJob` does NOT support `publishers { downstreamParameterized }`** - use Jenkinsfile orchestration instead

**Job DSL API Reference:** http://localhost:8080/plugin/job-dsl/api-viewer/index.html (when Jenkins is running)

## Troubleshooting

**"Couldn't find any revision to build"** - Check that the git branch in `jobs/pipeline.groovy` matches the actual branch (should be `*/main`).

**Permission warnings about "ambiguous entries"** - Use explicit `USER:` or `GROUP:` prefixes in CASC, or use `userPermissions()`/`groupPermissions()` in Job DSL.

**Jobs not visible to users** - Ensure folder has `hudson.model.Item.Discover` permission for the user (in addition to `Item.Read`).

**New Job DSL not taking effect after `docker-compose build`** - The `jenkins_home` volume persists old files. Either `docker cp` the updated files into the running container, or reset the volume with `docker-compose down -v && docker-compose up -d`.
