# Jenkins Test Environment

A Docker-based Jenkins test environment with Job DSL and Delivery Pipeline visualization.

## Quick Start

```bash
./scripts/start.sh
```

Access at http://localhost:8080 (admin/admin)

## Project Structure

- `Dockerfile` - Custom Jenkins image with pre-installed plugins
- `docker-compose.yml` - Container orchestration
- `plugins.txt` - Jenkins plugins to install
- `casc/jenkins.yaml` - Jenkins Configuration as Code (auto-setup)
- `jobs/pipeline.groovy` - Job DSL script defining all pipeline jobs
- `scripts/start.sh` - Startup script

## Pipeline

Trigger job starts the pipeline, which fans out to:
- iOS Build → iOS Deploy
- Android Build → Android Deploy
- iOS Unit Tests
- Android Unit Tests
- iOS Linter
- Android Linter

View the pipeline at "Mobile Pipeline View" in Jenkins.

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

Edit `jobs/pipeline.groovy` then run the **seed-job** in Jenkins to regenerate jobs:
- http://localhost:8080/job/seed-job/ → Build Now

Do NOT rebuild Docker for job changes. Only rebuild Docker when changing:
- `Dockerfile`
- `plugins.txt`
- `casc/jenkins.yaml`

```bash
# Only needed for Dockerfile/plugins/casc changes
docker-compose build && docker-compose up -d
```

## Job DSL Notes

- Job DSL `targets()` only accepts relative paths (Ant GLOB), not absolute paths
- For parameterized-trigger, use `triggerWithNoParameters()` not `parameters { currentBuild() }`
- The seed job copies DSL files to workspace before processing
