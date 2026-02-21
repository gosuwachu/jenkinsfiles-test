# Jenkins Pipeline Architecture Comparison

A Docker-based Jenkins environment demonstrating different approaches to pipeline orchestration. Each approach offers different trade-offs between simplicity, flexibility, and re-runnability.

## Quick Start

```bash
docker-compose up -d
```

Open http://localhost:8080 and login with `admin` / `admin`

## The Problem

When a Jenkins pipeline fails mid-way, you often want to:
- **Re-run just the failed stage** without starting from scratch
- **Re-trigger individual jobs** (e.g., just iOS deploy, not the whole pipeline)
- **Keep pipeline logic in version control** alongside your code

Different Jenkins architectures handle these requirements differently.

## Architecture Options

| Option | Approach | Re-trigger Individual Jobs? | Pipeline as Code? |
|--------|----------|----------------------------|-------------------|
| **0** | Job DSL triggers | Yes | No (DSL only) |
| **1** | Jenkinsfile → Free-style | Yes | Yes |
| **2** | Single Jenkinsfile (Blue Ocean) | Via Blue Ocean UI | Yes |
| **3** | Single Jenkinsfile (Skip Params) | Via parameters | Yes |
| **4A** | Jenkinsfile → Pipeline Jobs | Yes | Yes |

---

### Option 0: Job DSL with Triggers
**Folder:** `mobile-pipeline`

Traditional approach using Job DSL to create free-style jobs with `downstreamParameterized` triggers.

```
trigger → ios-build → ios-deploy
        → android-build → android-deploy
        → ios-unit-tests
        → android-unit-tests
        → ios-linter
        → android-linter
```

**Pros:**
- Simple, well-understood pattern
- Works with Delivery Pipeline Plugin for visualization
- Each job is independently re-triggerable

**Cons:**
- Pipeline logic not in version control
- Changes require editing Job DSL and running seed job

---

### Option 1: Jenkinsfile Orchestrates Free-Style Jobs
**Folder:** `pipeline-1-hybrid`

A Jenkinsfile uses `build job:` to trigger free-style jobs created by Job DSL.

```groovy
// Jenkinsfile.1-hybrid
stage('Build') {
    parallel {
        stage('iOS') { steps { build job: 'pipeline-1-hybrid/ios-build' } }
        stage('Android') { steps { build job: 'pipeline-1-hybrid/android-build' } }
    }
}
```

**Pros:**
- Pipeline flow defined in Jenkinsfile (version controlled)
- Individual jobs still re-triggerable
- Good for migrating from Option 0

**Cons:**
- Two places to maintain (Jenkinsfile + Job DSL)
- Job implementations still in Job DSL

---

### Option 2: Single Jenkinsfile with Blue Ocean Restart
**Folder:** `pipeline-2-blueocean`

Everything in one Jenkinsfile. Use Blue Ocean UI to restart from a failed stage.

```groovy
// Jenkinsfile.2-blueocean
options {
    preserveStashes(buildCount: 5)  // Keep artifacts for restart
}
stages {
    stage('Build') { ... }
    stage('Test') { ... }
    stage('Deploy') { ... }
}
```

**Pros:**
- Simplest setup - single file defines everything
- Native "Restart from Stage" in Blue Ocean UI
- Full pipeline visibility

**Cons:**
- Restart only works in Blue Ocean UI (not classic Jenkins)
- Can only restart from stage boundaries
- Parallel branches restart together

---

### Option 3: Single Jenkinsfile with Skip Parameters
**Folder:** `pipeline-3-skip-params`

Jenkinsfile with boolean parameters to skip completed stages on re-run.

```groovy
// Jenkinsfile.3-skip-params
parameters {
    booleanParam(name: 'SKIP_BUILD', defaultValue: false)
    booleanParam(name: 'SKIP_TEST', defaultValue: false)
}
stages {
    stage('Build') {
        when { expression { !params.SKIP_BUILD } }
        steps { ... }
    }
}
```

**Pros:**
- Works in classic Jenkins UI
- Simple to understand
- No special plugins needed

**Cons:**
- Manual parameter management
- Must remember which stages completed
- Re-runs entire pipeline (just skips stages)

---

### Option 4A: Jenkinsfile Orchestrates Pipeline Jobs
**Folder:** `pipeline-4a`

Best of both worlds: Jenkinsfile orchestration with pipeline jobs that each have their own Jenkinsfile.

```
ci/trigger.Jenkinsfile (orchestrator)
    → pipeline-4a/ios-build (reads ci/ios-build.Jenkinsfile)
    → pipeline-4a/android-build (reads ci/android-build.Jenkinsfile)
    → ...
```

**Pros:**
- All pipeline logic in version control
- Each job independently re-triggerable
- Job implementations in separate Jenkinsfiles (modular)

**Cons:**
- More files to manage
- Job DSL still needed to create job definitions

---

## Project Structure

```
├── Jenkinsfile.1-hybrid        # Option 1: orchestrates free-style jobs
├── Jenkinsfile.2-blueocean     # Option 2: single pipeline, Blue Ocean restart
├── Jenkinsfile.3-skip-params   # Option 3: single pipeline with skip params
├── ci/
│   ├── trigger.Jenkinsfile     # Option 4A: orchestrator
│   ├── ios-build.Jenkinsfile   # Option 4A: job implementation
│   ├── android-build.Jenkinsfile
│   └── ...
├── jobs/
│   └── pipeline.groovy         # Job DSL definitions for all options
├── casc/
│   └── jenkins.yaml            # Jenkins Configuration as Code
├── Dockerfile
├── docker-compose.yml
└── plugins.txt
```

## Choosing an Approach

| If you need... | Use |
|----------------|-----|
| Simplest migration from existing free-style jobs | Option 0 |
| Version-controlled orchestration, existing free-style jobs | Option 1 |
| Single Jenkinsfile, Blue Ocean for restarts | Option 2 |
| Single Jenkinsfile, classic UI, simple skip logic | Option 3 |
| Full version control, modular jobs, independent re-runs | Option 4A |

## Common Commands

```bash
# Start Jenkins
docker-compose up -d

# View logs
docker-compose logs -f

# Stop Jenkins
docker-compose down

# Full reset (clear all data)
docker-compose down -v
docker-compose up -d

# Rebuild after changing Dockerfile/plugins
docker-compose build --no-cache
docker-compose up -d
```

## Modifying Jobs

**Job DSL changes:** Edit `jobs/pipeline.groovy`, then run the seed-job:
http://localhost:8080/job/seed-job/ → Build Now

**Jenkinsfile changes:** Just commit and re-run the pipeline (it pulls from the repo).

## Technical Notes

- `pipelineJob` in Job DSL does **not** support `publishers { downstreamParameterized }` - use Jenkinsfile orchestration instead
- Job DSL `targets()` only accepts relative paths (Ant GLOB pattern)
- Local git repos require `-Dhudson.plugins.git.GitSCM.ALLOW_LOCAL_CHECKOUT=true`
