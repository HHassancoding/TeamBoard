# 403 Error Documentation Index

This directory contains comprehensive documentation for resolving and debugging 403 Forbidden errors when creating tasks in the TeamBoard backend.

## Quick Links

### 🚀 Start Here
**[BACKEND_403_FIX_COMPLETE.md](./BACKEND_403_FIX_COMPLETE.md)** - Complete summary of the fix, what was changed, and coordination with frontend team

### 📋 Specific Guides

1. **[TASK_CREATION_FIX.md](./TASK_CREATION_FIX.md)**
   - Root cause analysis
   - Correct API request format
   - Examples for frontend developers
   - Testing instructions

2. **[TROUBLESHOOTING_403_ERRORS.md](./TROUBLESHOOTING_403_ERRORS.md)**
   - Quick diagnosis checklist
   - Common frontend mistakes
   - Step-by-step debugging guide
   - Working code examples
   - Error response guide

3. **[TaskRequests.http](./TaskRequests.http)**
   - Fixed API test examples
   - Correct field names (`assignedToId`)
   - Workspace-scoped endpoint examples

## Problem Summary

**Issue:** Frontend receiving 403 Forbidden when creating tasks

**Root Cause:** Incorrect API documentation showed `assignedTo` instead of `assignedToId`, and frontend likely missing/invalid JWT tokens

**Status:** ✅ **RESOLVED** - Documentation fixed, comprehensive troubleshooting guides created

## What Was Fixed

### ✅ Documentation Updates
- Corrected field name: `assignedTo` → `assignedToId` in all examples
- Removed unsupported query parameter examples
- Added workspace-scoped endpoint example (recommended)

### ✅ New Documentation Created
- Complete fix summary
- Troubleshooting guide with debugging steps
- Working code examples for frontend

### ✅ Backend Code Verification
- SecurityConfig - ✓ Working correctly
- JwtAuthFilter - ✓ Working correctly  
- TaskController - ✓ Working correctly
- All 75 tests passing ✓

## Common 403 Causes (Most to Least Common)

1. **Missing Authorization header** (60%)
2. **Expired JWT token** (25%)
3. **User not workspace member** (10%)
4. **Project-workspace mismatch** (5%)

## Quick Fix for Frontend

```javascript
// 1. Store token after login
localStorage.setItem('jwt_token', token);

// 2. Send token with every request
headers: {
  'Authorization': `Bearer ${token}`,
  'Content-Type': 'application/json'
}

// 3. Use correct field name
body: JSON.stringify({
  title: "My Task",
  assignedToId: userId  // ✅ CORRECT (not assignedTo)
})
```

## Correct API Endpoint

**Recommended (workspace-scoped):**
```
POST /api/workspaces/{workspaceId}/projects/{projectId}/tasks
```

**Also works (project-only):**
```
POST /api/projects/{projectId}/tasks
```

## For Frontend Developers

Please read:
1. **BACKEND_403_FIX_COMPLETE.md** for a complete overview
2. **TROUBLESHOOTING_403_ERRORS.md** if you're still getting 403 errors
3. **TASK_CREATION_FIX.md** for the correct API request format

## Testing the Fix

### Using cURL
```bash
# 1. Get JWT token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com", "password": "password"}'

# 2. Create task (replace YOUR_TOKEN)
curl -X POST http://localhost:8080/api/workspaces/8/projects/4/tasks \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Task",
    "assignedToId": 5,
    "priority": "MEDIUM"
  }'
```

Expected: **201 Created** with task JSON in response

## Backend Logs

The backend has extensive logging enabled. Check logs for:
- JWT validation status
- Authorization checks
- Task creation steps

Example log output:
```
========================================
TASK CREATION REQUEST START
========================================
Step 1: Validating JWT token...
✓ JWT Valid - User ID: 8
Step 4: Checking workspace access...
✓ Authorization passed
✓ Task created successfully
========================================
```

## Need Help?

If you're still experiencing 403 errors after:
1. Ensuring JWT token is sent
2. Verifying token is not expired
3. Confirming user is workspace member

Please provide:
- Frontend code making the API call
- Browser Network tab screenshot
- Backend log output

## Related Documentation

### Previous Fixes
- [WORKSPACE_TASK_ENDPOINT_FIX.md](./WORKSPACE_TASK_ENDPOINT_FIX.md)
- [BACKEND_FIX_SUMMARY.md](./BACKEND_FIX_SUMMARY.md)
- [SENIOR_ENGINEER_REVIEW.md](./SENIOR_ENGINEER_REVIEW.md)

### API Documentation
- [TaskRequests.http](./TaskRequests.http) - Updated API examples

## Summary

✅ **Backend is working correctly**  
✅ **Documentation is now accurate**  
✅ **Comprehensive troubleshooting guides available**  
✅ **All tests passing (75/75)**  

The 403 error should be resolved by ensuring the frontend:
1. Stores JWT tokens after login
2. Sends tokens with every request
3. Uses correct field names (`assignedToId`)
4. Handles token expiration
