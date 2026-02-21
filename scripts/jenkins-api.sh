#!/bin/bash
# Jenkins API helper script
# Usage:
#   ./scripts/jenkins-api.sh build <job-path>        # Trigger a build
#   ./scripts/jenkins-api.sh log <job-path> [build#] # Get console log (default: lastBuild)
#   ./scripts/jenkins-api.sh status <job-path>       # Get job status

JENKINS_URL="${JENKINS_URL:-http://localhost:8080}"
JENKINS_USER="${JENKINS_USER:-admin}"
JENKINS_PASS="${JENKINS_PASS:-admin}"

COOKIE_JAR="/tmp/jenkins-cookies.txt"

get_crumb() {
    curl -s -c "$COOKIE_JAR" -u "$JENKINS_USER:$JENKINS_PASS" \
        "$JENKINS_URL/crumbIssuer/api/json" | \
        python3 -c "import sys,json; d=json.load(sys.stdin); print(d['crumb'])"
}

api_get() {
    curl -s -u "$JENKINS_USER:$JENKINS_PASS" "$JENKINS_URL$1"
}

api_post() {
    local crumb=$(get_crumb)
    curl -s -b "$COOKIE_JAR" -u "$JENKINS_USER:$JENKINS_PASS" \
        -H "Jenkins-Crumb: $crumb" -X POST "$JENKINS_URL$1" -w "\n"
}

case "$1" in
    dsl)
        echo "Job DSL API Reference"
        echo "====================="
        echo ""
        echo "Open in browser: $JENKINS_URL/plugin/job-dsl/api-viewer/index.html"
        echo ""
        echo "Common paths (append #path/ to URL):"
        echo "  folder                    - Folder definition"
        echo "  folder-authorization      - Folder permissions (userPermissions, groupPermissions)"
        echo "  job                       - Free-style job"
        echo "  job-authorization         - Job permissions"
        echo "  pipelineJob               - Pipeline job definition"
        echo "  pipelineJob-definition    - Pipeline script/SCM config"
        echo ""
        echo "Example URLs:"
        echo "  $JENKINS_URL/plugin/job-dsl/api-viewer/index.html#path/folder-authorization"
        echo "  $JENKINS_URL/plugin/job-dsl/api-viewer/index.html#path/pipelineJob-definition-cpsScm"
        echo ""
        echo "Key methods for folder permissions (matrix-auth > 3.0):"
        echo "  userPermissions(userName, permissionsList)"
        echo "  userPermissionAll(userName)"
        echo "  groupPermissions(groupName, permissionsList)"
        echo ""
        echo "Example:"
        echo "  folder('my-folder') {"
        echo "      authorization {"
        echo "          userPermissions('dev1', ["
        echo "              'hudson.model.Item.Discover',"
        echo "              'hudson.model.Item.Read',"
        echo "              'hudson.model.Item.Build'"
        echo "          ])"
        echo "      }"
        echo "  }"
        ;;
    build)
        if [ -z "$2" ]; then
            echo "Usage: $0 build <job-path>"
            echo "Example: $0 build pipeline-2-blueocean/job/pipeline"
            exit 1
        fi
        echo "Triggering build for $2..."
        api_post "/job/$2/build"
        echo "Build triggered"
        ;;
    log)
        if [ -z "$2" ]; then
            echo "Usage: $0 log <job-path> [build#]"
            exit 1
        fi
        BUILD="${3:-lastBuild}"
        api_get "/job/$2/$BUILD/consoleText"
        ;;
    status)
        if [ -z "$2" ]; then
            echo "Usage: $0 status <job-path>"
            exit 1
        fi
        api_get "/job/$2/api/json?tree=name,color,lastBuild%5Bnumber,result%5D" | \
            python3 -c "import sys,json; d=json.load(sys.stdin); lb=d.get('lastBuild',{}); print(f\"Job: {d['name']}\nStatus: {d['color']}\nLast Build: #{lb.get('number','N/A')} - {lb.get('result','N/A')}\")"
        ;;
    *)
        echo "Jenkins API Helper"
        echo "Usage: $0 <command> [args]"
        echo ""
        echo "Commands:"
        echo "  build <job-path>         Trigger a build"
        echo "  log <job-path> [build#]  Get console log (default: lastBuild)"
        echo "  status <job-path>        Get job status"
        echo "  dsl                      Show Job DSL documentation reference"
        echo ""
        echo "Examples:"
        echo "  $0 build pipeline-2-blueocean/job/pipeline"
        echo "  $0 log pipeline-2-blueocean/job/pipeline 1"
        echo "  $0 status mobile-pipeline/job/trigger"
        echo "  $0 dsl"
        ;;
esac
