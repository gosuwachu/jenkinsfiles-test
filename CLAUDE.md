# Jenkins Test Environment

A Docker-based Jenkins test environment demonstrating multiple pipeline architecture options.

## Quick Start

```bash
./scripts/start.sh
```

Access at http://localhost:8080 (admin/admin)

## Pipeline Architecture Options

This repo implements 6 different approaches to Jenkins pipelines for comparison:

| Folder | Option | Orchestration | Job Type | Re-triggerable |
|--------|--------|---------------|----------|----------------|
| `mobile-pipeline` | 0: Current | Job DSL triggers | Free-style | Yes |
| `pipeline-1-hybrid` | 1: Hybrid | Jenkinsfile `build job:` | Free-style | Yes |
| `pipeline-2-blueocean` | 2: Blue Ocean | Single Jenkinsfile | Pipeline | Via Blue Ocean |
| `pipeline-3-skip-params` | 3: Skip Params | Single Jenkinsfile | Pipeline | Via params |
| `pipeline-4a` | 4A: DSL + Jenkinsfiles | Job DSL triggers | Pipeline | Yes |
| `pipeline-5b` | 5B: Orchestrator | Jenkinsfile `build job:` | Pipeline | Yes |

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

### Option 4A: Job DSL + Pipeline Jobs with Jenkinsfiles
- **Folder:** `pipeline-4a`
- **How it works:** Job DSL creates `pipelineJob` entries that read `ci/*.Jenkinsfile`
- **Pros:** Job implementations versioned, re-triggerable, Job DSL handles triggers
- **Cons:** Many Jenkinsfiles

### Option 5B: Jenkinsfile Orchestrator + Pipeline Jobs
- **Folder:** `pipeline-5b`
- **How it works:** `Jenkinsfile.5b-orchestrator` uses `build job:` to call pipeline jobs
- **Pros:** All pipeline logic in Jenkinsfiles, re-triggerable jobs
- **Cons:** Job DSL still needed to create jobs

## Project Structure

```
├── Jenkinsfile.1-hybrid           # Option 1 orchestrator
├── Jenkinsfile.2-blueocean        # Option 2 single pipeline
├── Jenkinsfile.3-skip-params      # Option 3 with skip params
├── Jenkinsfile.5b-orchestrator    # Option 5B orchestrator
├── ci/
│   ├── trigger.Jenkinsfile        # Option 4A trigger
│   ├── ios-build.Jenkinsfile      # Shared by 4A, 5B
│   ├── ios-deploy.Jenkinsfile
│   ├── android-build.Jenkinsfile
│   └── ...
├── jobs/
│   └── pipeline.groovy            # Job DSL (all options)
├── casc/
│   └── jenkins.yaml               # Jenkins Configuration as Code
├── Dockerfile
├── docker-compose.yml
├── plugins.txt
└── scripts/
    └── start.sh
```

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

## Modifying Jobs

Edit `jobs/pipeline.groovy` then run the **seed-job** in Jenkins:
- http://localhost:8080/job/seed-job/ → Build Now

For Jenkinsfile changes, just re-run the job (it pulls from repo).

Only rebuild Docker when changing `Dockerfile`, `plugins.txt`, or `casc/jenkins.yaml`:
```bash
docker-compose build && docker-compose up -d
```

## Job DSL Notes

- Job DSL `targets()` only accepts relative paths (Ant GLOB), not absolute paths
- For parameterized-trigger, use `triggerWithNoParameters()` not `parameters { currentBuild() }`
- The seed job copies DSL files to workspace before processing
- `pipelineJob` uses `cpsScm` to read Jenkinsfiles from SCM
