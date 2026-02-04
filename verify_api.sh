#!/bin/bash

BASE_URL="http://localhost:5000/api"
CONTENT_TYPE="Content-Type: application/json"

echo "---------------------------------------------------"
echo "🔍 Starting API Verification"
echo "---------------------------------------------------"

# 1. Health Check
echo ""
echo "👉 Testing Health Check..."
HEALTH=$(curl -s -o /dev/null -w "%{http_code}" $BASE_URL/health)
if [ "$HEALTH" == "200" ]; then
    echo "✅ Health Check Passed (200)"
else
    echo "❌ Health Check Failed ($HEALTH)"
fi

# 2. Login (Super Admin)
echo ""
echo "👉 Testing Login (Super Admin)..."
LOGIN_RESPONSE=$(curl -s -X POST $BASE_URL/auth/login \
  -H "$CONTENT_TYPE" \
  -d '{"email":"superadmin@system.com","password":"Admin@123"}')

TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.data.token')

if [ "$TOKEN" != "null" ] && [ -n "$TOKEN" ]; then
    echo "✅ Login Successful. Token received."
else
    echo "❌ Login Failed."
    echo "Response: $LOGIN_RESPONSE"
    exit 1
fi

AUTH_HEADER="Authorization: Bearer $TOKEN"

# 3. Get Current User
echo ""
echo "👉 Testing Get Current User (/api/auth/me)..."
ME_CODE=$(curl -s -o /dev/null -w "%{http_code}" -H "$AUTH_HEADER" $BASE_URL/auth/me)
if [ "$ME_CODE" == "200" ]; then
    echo "✅ Get Me Passed (200)"
else
    echo "❌ Get Me Failed ($ME_CODE)"
fi

# 4. List Tenants (Super Admin only)
echo ""
echo "👉 Testing List Tenants..."
TENANTS_RES=$(curl -s -H "$AUTH_HEADER" $BASE_URL/tenants)
TENANT_COUNT=$(echo $TENANTS_RES | jq '.data.tenants | length')
echo "✅ Tenants Found: $TENANT_COUNT"

# 5. Create a Project
echo ""
echo "👉 Testing Create Project..."
# Get first tenant ID
TENANT_ID=$(echo $TENANTS_RES | jq -r '.data.tenants[0].id')
PROJECT_RES=$(curl -s -X POST $BASE_URL/projects \
  -H "$CONTENT_TYPE" \
  -H "$AUTH_HEADER" \
  -d "{\"name\":\"Test Project $(date +%s)\",\"description\":\"Automated test project\",\"tenantId\":\"$TENANT_ID\"}")

PROJECT_ID=$(echo $PROJECT_RES | jq -r '.data.id')

if [ "$PROJECT_ID" != "null" ]; then
    echo "✅ Project Created: $PROJECT_ID"
else
    echo "❌ Project Creation Failed"
    echo "$PROJECT_RES"
fi

# 6. List Projects
echo ""
echo "👉 Testing List Projects..."
LIST_PROJ_CODE=$(curl -s -o /dev/null -w "%{http_code}" -H "$AUTH_HEADER" "$BASE_URL/projects?tenantIdFilter=$TENANT_ID")
if [ "$LIST_PROJ_CODE" == "200" ]; then
    echo "✅ List Projects Passed (200)"
else
    echo "❌ List Projects Failed ($LIST_PROJ_CODE)"
fi

# 7. Create Task
echo ""
echo "👉 Testing Create Task..."
if [ "$PROJECT_ID" != "null" ]; then
    TASK_RES=$(curl -s -X POST "$BASE_URL/projects/$PROJECT_ID/tasks" \
      -H "$CONTENT_TYPE" \
      -H "$AUTH_HEADER" \
      -d '{"title":"Test Task","description":"This is a test task","priority":"HIGH"}')
    
    TASK_ID=$(echo $TASK_RES | jq -r '.data.id')
    if [ "$TASK_ID" != "null" ]; then
        echo "✅ Task Created: $TASK_ID"
    else
        echo "❌ Task Creation Failed"
        echo "$TASK_RES"
    fi
else
    echo "⚠️ Skipping Task Creation (No Project ID)"
fi

# 8. List Tasks
echo ""
echo "👉 Testing List Tasks..."
if [ "$PROJECT_ID" != "null" ]; then
    LIST_TASK_CODE=$(curl -s -o /dev/null -w "%{http_code}" -H "$AUTH_HEADER" "$BASE_URL/projects/$PROJECT_ID/tasks")
     if [ "$LIST_TASK_CODE" == "200" ]; then
        echo "✅ List Tasks Passed (200)"
    else
        echo "❌ List Tasks Failed ($LIST_TASK_CODE)"
    fi
fi

# 9. List Users
echo ""
echo "👉 Testing List Users..."
LIST_USERS_CODE=$(curl -s -o /dev/null -w "%{http_code}" -H "$AUTH_HEADER" "$BASE_URL/users")
if [ "$LIST_USERS_CODE" == "200" ]; then
    echo "✅ List Users Passed (200)"
else
    echo "❌ List Users Failed ($LIST_USERS_CODE)"
fi

# 10. Delete Project (Cleanup)
echo ""
echo "👉 Testing Delete Project (Cleanup)..."
if [ "$PROJECT_ID" != "null" ]; then
    DEL_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE -H "$AUTH_HEADER" "$BASE_URL/projects/$PROJECT_ID")
    if [ "$DEL_CODE" == "204" ] || [ "$DEL_CODE" == "200" ]; then
        echo "✅ Project Deleted ($DEL_CODE)"
    else
        echo "❌ Project Delete Failed ($DEL_CODE)"
    fi
fi

echo ""
echo "---------------------------------------------------"
echo "🏁 Verification Complete"
echo "---------------------------------------------------"
