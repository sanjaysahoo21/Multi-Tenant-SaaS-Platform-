#!/bin/bash

# WorkStack Submission Verification Script
# This script verifies that all required files and configurations are in place

echo "=========================================="
echo "WorkStack Submission Verification"
echo "=========================================="
echo ""

# Color codes
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

VERIFIED=0
MISSING=0

# Function to check file existence
check_file() {
    if [ -f "$1" ]; then
        echo -e "${GREEN}✓${NC} $1"
        ((VERIFIED++))
    else
        echo -e "${RED}✗${NC} $1"
        ((MISSING++))
    fi
}

# Function to check directory existence
check_dir() {
    if [ -d "$1" ]; then
        echo -e "${GREEN}✓${NC} $1/"
        ((VERIFIED++))
    else
        echo -e "${RED}✗${NC} $1/"
        ((MISSING++))
    fi
}

echo "Checking Project Structure..."
echo "=============================="
check_file "README.md"
check_file "submission.json"
check_file "SUBMISSION_CHECKLIST.md"
check_file "docker-compose.yml"
check_file ".gitignore"
echo ""

echo "Checking Backend..."
echo "=================="
check_dir "backend"
check_file "backend/pom.xml"
check_file "backend/Dockerfile"
check_dir "backend/src/main/java/com/example"
check_dir "backend/src/main/resources/db/migration"
echo ""

echo "Checking Frontend..."
echo "==================="
check_dir "frontend"
check_file "frontend/package.json"
check_file "frontend/Dockerfile"
check_file "frontend/vite.config.js"
check_dir "frontend/src"
check_dir "frontend/src/components"
check_dir "frontend/src/pages"
check_dir "frontend/src/context"
echo ""

echo "Checking Documentation..."
echo "========================="
check_file "docs/research.md"
check_file "docs/PRD.md"
check_file "docs/architecture.md"
check_file "docs/technical-spec.md"
check_file "docs/API.md"
check_file "docs/DOCKER.md"
check_dir "docs/images"
echo ""

echo "Checking Git Repository..."
echo "=========================="
check_dir ".git"
if [ -d ".git" ]; then
    COMMIT_COUNT=$(git rev-list --count HEAD 2>/dev/null || echo "0")
    if [ "$COMMIT_COUNT" -ge 30 ]; then
        echo -e "${GREEN}✓${NC} Git commits: $COMMIT_COUNT (minimum 30 required)"
        ((VERIFIED++))
    else
        echo -e "${YELLOW}!${NC} Git commits: $COMMIT_COUNT (minimum 30 required)"
        ((MISSING++))
    fi
fi
echo ""

echo "Docker Configuration..."
echo "======================"
if grep -q "database:" docker-compose.yml; then
    echo -e "${GREEN}✓${NC} Service name 'database' found in docker-compose.yml"
    ((VERIFIED++))
else
    echo -e "${RED}✗${NC} Service name 'database' NOT found in docker-compose.yml"
    ((MISSING++))
fi

if grep -q "backend:" docker-compose.yml; then
    echo -e "${GREEN}✓${NC} Service name 'backend' found in docker-compose.yml"
    ((VERIFIED++))
else
    echo -e "${RED}✗${NC} Service name 'backend' NOT found in docker-compose.yml"
    ((MISSING++))
fi

if grep -q "frontend:" docker-compose.yml; then
    echo -e "${GREEN}✓${NC} Service name 'frontend' found in docker-compose.yml"
    ((VERIFIED++))
else
    echo -e "${RED}✗${NC} Service name 'frontend' NOT found in docker-compose.yml"
    ((MISSING++))
fi

if grep -q '5432:5432' docker-compose.yml; then
    echo -e "${GREEN}✓${NC} Database port mapping 5432:5432 configured"
    ((VERIFIED++))
else
    echo -e "${RED}✗${NC} Database port mapping 5432:5432 NOT configured"
    ((MISSING++))
fi

if grep -q '5000:5000' docker-compose.yml; then
    echo -e "${GREEN}✓${NC} Backend port mapping 5000:5000 configured"
    ((VERIFIED++))
else
    echo -e "${RED}✗${NC} Backend port mapping 5000:5000 NOT configured"
    ((MISSING++))
fi

if grep -q '3000:80' docker-compose.yml; then
    echo -e "${GREEN}✓${NC} Frontend port mapping 3000:80 configured"
    ((VERIFIED++))
else
    echo -e "${RED}✗${NC} Frontend port mapping 3000:80 NOT configured"
    ((MISSING++))
fi

echo ""

echo "Submission Metadata..."
echo "====================="
if grep -q '"testCredentials"' submission.json; then
    echo -e "${GREEN}✓${NC} Test credentials found in submission.json"
    ((VERIFIED++))
else
    echo -e "${RED}✗${NC} Test credentials NOT found in submission.json"
    ((MISSING++))
fi

if grep -q '"seedData"' submission.json; then
    echo -e "${GREEN}✓${NC} Seed data configuration found in submission.json"
    ((VERIFIED++))
else
    echo -e "${RED}✗${NC} Seed data configuration NOT found in submission.json"
    ((MISSING++))
fi

if grep -q '"apiEndpoints"' submission.json; then
    echo -e "${GREEN}✓${NC} API endpoints list found in submission.json"
    ((VERIFIED++))
else
    echo -e "${RED}✗${NC} API endpoints list NOT found in submission.json"
    ((MISSING++))
fi

echo ""
echo "=========================================="
echo "Verification Summary"
echo "=========================================="
echo -e "Items verified: ${GREEN}$VERIFIED${NC}"
echo -e "Items missing: ${RED}$MISSING${NC}"
echo ""

if [ $MISSING -eq 0 ]; then
    echo -e "${GREEN}✓ All checks passed!${NC}"
    echo "Submission is ready for evaluation."
    exit 0
else
    echo -e "${RED}✗ Some checks failed!${NC}"
    echo "Please resolve the missing items before submission."
    exit 1
fi
