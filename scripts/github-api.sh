#!/bin/bash
# GitHub API helper script for checking commit statuses and other info.
# Reads GITHUB_PAT from .env file in the jenkins-setup directory.

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ENV_FILE="$SCRIPT_DIR/../.env"

if [ -f "$ENV_FILE" ]; then
    # shellcheck disable=SC1090
    source "$ENV_FILE"
fi

OWNER="gosuwachu"
REPO="mobile-app"

gh_api() {
    local path="$1"
    curl -s \
        -H "Authorization: token $GITHUB_PAT" \
        -H "Accept: application/vnd.github+json" \
        "https://api.github.com$path"
}

gh_api_post() {
    local path="$1"
    local data="$2"
    curl -s \
        -X POST \
        -H "Authorization: token $GITHUB_PAT" \
        -H "Accept: application/vnd.github+json" \
        -H "Content-Type: application/json" \
        -d "$data" \
        "https://api.github.com$path"
}

cmd_statuses() {
    local sha="$1"
    if [ -z "$sha" ]; then
        # Default to latest commit on main
        sha=$(gh_api "/repos/$OWNER/$REPO/commits/main" | python3 -c "import json,sys; print(json.load(sys.stdin)['sha'])")
        echo "Using latest main commit: $sha"
    fi
    gh_api "/repos/$OWNER/$REPO/statuses/$sha?per_page=30" | python3 -c "
import json, sys
statuses = json.load(sys.stdin)
if isinstance(statuses, dict) and 'message' in statuses:
    print(f\"Error: {statuses['message']}\")
    sys.exit(1)
# Deduplicate: keep most recent per context
seen = {}
for s in statuses:
    if s['context'] not in seen:
        seen[s['context']] = s
for ctx in sorted(seen):
    s = seen[ctx]
    url = s['target_url'] or ''
    print(f\"{s['context']:30s} {s['state']:10s} {url}\")
"
}

cmd_combined() {
    local sha="$1"
    if [ -z "$sha" ]; then
        sha=$(gh_api "/repos/$OWNER/$REPO/commits/main" | python3 -c "import json,sys; print(json.load(sys.stdin)['sha'])")
        echo "Using latest main commit: $sha"
    fi
    gh_api "/repos/$OWNER/$REPO/commits/$sha/status" | python3 -c "
import json, sys
data = json.load(sys.stdin)
print(f\"Combined state: {data['state']}\")
print(f\"Total count: {data['total_count']}\")
for s in data.get('statuses', []):
    url = s['target_url'] or ''
    print(f\"  {s['context']:28s} {s['state']:10s} {url}\")
"
}

cmd_commits() {
    local branch="${1:-main}"
    gh_api "/repos/$OWNER/$REPO/commits?sha=$branch&per_page=5" | python3 -c "
import json, sys
commits = json.load(sys.stdin)
for c in commits:
    sha = c['sha'][:7]
    msg = c['commit']['message'].split('\n')[0][:60]
    author = c['commit']['author']['name']
    print(f\"{sha}  {author:20s}  {msg}\")
"
}

cmd_pulls() {
    gh_api "/repos/$OWNER/$REPO/pulls?state=open&per_page=10" | python3 -c "
import json, sys
pulls = json.load(sys.stdin)
if not pulls:
    print('No open pull requests')
    sys.exit(0)
for pr in pulls:
    labels = ', '.join(l['name'] for l in pr.get('labels', []))
    label_str = f' [{labels}]' if labels else ''
    print(f\"#{pr['number']:4d}  {pr['head']['ref']:30s}  {pr['user']['login']:15s}  {pr['title'][:40]}{label_str}\")
"
}

cmd_pr_statuses() {
    local pr_number="$1"
    if [ -z "$pr_number" ]; then
        echo "Usage: github-api.sh pr-statuses <pr-number>"
        exit 1
    fi
    local sha
    sha=$(gh_api "/repos/$OWNER/$REPO/pulls/$pr_number" | python3 -c "import json,sys; print(json.load(sys.stdin)['head']['sha'])")
    echo "PR #$pr_number head SHA: $sha"
    cmd_statuses "$sha"
}

usage() {
    echo "Usage: $(basename "$0") <command> [args]"
    echo ""
    echo "Commands:"
    echo "  statuses [sha]       Show commit statuses (default: latest main commit)"
    echo "  combined [sha]       Show combined status (default: latest main commit)"
    echo "  commits [branch]     Show recent commits (default: main)"
    echo "  pulls                Show open pull requests"
    echo "  pr-statuses <number> Show statuses for a PR's head commit"
    echo ""
    echo "Examples:"
    echo "  $(basename "$0") statuses"
    echo "  $(basename "$0") statuses abc123def"
    echo "  $(basename "$0") pr-statuses 7"
    echo "  $(basename "$0") pulls"
}

case "${1:-}" in
    statuses)    cmd_statuses "$2" ;;
    combined)    cmd_combined "$2" ;;
    commits)     cmd_commits "$2" ;;
    pulls)       cmd_pulls ;;
    pr-statuses) cmd_pr_statuses "$2" ;;
    *)           usage ;;
esac
