#!/bin/bash
set -e

echo "==================================="
echo "Jenkins Test Environment Startup"
echo "==================================="

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "Error: Docker is not running. Please start Docker first."
    exit 1
fi

# Navigate to project directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_DIR"

echo "Project directory: $PROJECT_DIR"

# Build and start Jenkins
echo ""
echo "Building Jenkins Docker image..."
docker-compose build

echo ""
echo "Starting Jenkins container..."
docker-compose up -d

echo ""
echo "Waiting for Jenkins to start..."
JENKINS_URL="http://localhost:8080"
MAX_ATTEMPTS=60
ATTEMPT=0

while [ $ATTEMPT -lt $MAX_ATTEMPTS ]; do
    if curl -s -o /dev/null -w "%{http_code}" "$JENKINS_URL/login" | grep -q "200"; then
        echo "Jenkins is ready!"
        break
    fi
    ATTEMPT=$((ATTEMPT + 1))
    echo "Waiting for Jenkins... (attempt $ATTEMPT/$MAX_ATTEMPTS)"
    sleep 2
done

if [ $ATTEMPT -eq $MAX_ATTEMPTS ]; then
    echo "Error: Jenkins failed to start within expected time"
    docker-compose logs
    exit 1
fi

echo ""
echo "==================================="
echo "Jenkins is ready!"
echo "==================================="
echo ""
echo "Access Jenkins at: $JENKINS_URL"
echo "Username: admin"
echo "Password: admin"
echo ""
echo "Pipeline View: $JENKINS_URL/view/Mobile%20Pipeline%20View/"
echo ""
echo "To stop Jenkins: docker-compose down"
echo "To view logs: docker-compose logs -f"
