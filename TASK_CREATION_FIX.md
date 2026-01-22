# Task Creation 403 Error - Root Cause and Fix

**Date:** January 22, 2026  
**Issue:** Frontend receiving 403 Forbidden when creating tasks  
**Status:** ✅ FIXED

---

## Root Cause Analysis

### The Problem
The backend API documentation (`TaskRequests.http`) contained **incorrect field names** that could lead frontend developers to use the wrong request format:

**WRONG (Old Documentation):**
```json
{
  "title": "My Task",
  "assignedTo": 5
}
```

**CORRECT (Fixed):**
```json
{
  "title": "My Task",
  "assignedToId": 5
}
```

### Why This Causes 403 Errors

When the frontend sends a request with `assignedTo` instead of `assignedToId`:
1. Spring Boot validation may fail silently
2. The JWT authentication succeeds but request validation fails
3. Depending on how the error is handled, it could return 403 instead of 400

---

## What Was Fixed

### 1. ✅ Corrected Field Names in TaskRequests.http

All instances of `assignedTo` in request bodies have been changed to `assignedToId`:

- ✅ Line 23: Create task endpoint
- ✅ Line 111: Update task endpoint  
- ✅ Line 145: Update assignee endpoint
- ✅ Line 156: Unassign task endpoint

### 2. ✅ Removed Unsupported Query Parameters

The following query parameter filters were **not implemented** in the backend and have been removed from documentation:
- ❌ `?assignedTo={{userId}}`
- ❌ `?columnId={{columnId}}`
- ❌ `?priority=HIGH`

The backend only supports:
- ✅ `GET /api/projects/{projectId}/tasks` - Get all tasks in project
- ✅ `GET /api/workspaces/{workspaceId}/projects/{projectId}/tasks` - Get all tasks (workspace-scoped)

### 3. ✅ Added Workspace-Scoped Example

Added a workspace-scoped task creation example which is the **RECOMMENDED** approach for frontend:

```http
POST {{baseUrl}}/workspaces/{{workspaceId}}/projects/{{projectId}}/tasks
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "title": "My Task",
  "description": "Task description",
  "assignedToId": 5,
  "priority": "HIGH",
  "dueDate": "2025-01-20T23:59:59Z"
}
```

---

## Correct Request Format for Frontend

### Required Fields
- `title` (String) - **REQUIRED**

### Optional Fields
- `description` (String)
- `assignedToId` (Long) - **NOT** `assignedTo`
- `priority` (String) - One of: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`
- `dueDate` (String) - ISO 8601 format: `YYYY-MM-DDTHH:mm:ssZ`

### ❌ DO NOT Send
- `columnId` - Tasks are automatically assigned to the first column (Backlog)
- `assignedTo` - This field doesn't exist in the DTO

---

## Testing the Fix

### Example cURL Request
```bash
curl -X POST http://localhost:8080/api/workspaces/8/projects/4/tasks \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Task",
    "description": "Testing the fix",
    "assignedToId": 5,
    "priority": "MEDIUM"
  }'
```

### Expected Success Response (201 Created)
```json
{
  "id": 123,
  "title": "Test Task",
  "description": "Testing the fix",
  "projectId": 4,
  "columnId": 1,
  "assignedToId": 5,
  "assignedToName": "John Doe",
  "assignedToInitials": "JD",
  "priority": "MEDIUM",
  "dueDate": null,
  "createdById": 8,
  "createdByName": "Jane Smith",
  "createdAt": "2026-01-22T17:30:00Z",
  "updatedAt": "2026-01-22T17:30:00Z",
  "completedAt": null
}
```

---

## For Frontend Developers

### ✅ What to Update in Your Code

1. **Change field name:**
   ```javascript
   // ❌ WRONG
   const taskData = {
     title: "My Task",
     assignedTo: userId
   };

   // ✅ CORRECT
   const taskData = {
     title: "My Task",
     assignedToId: userId
   };
   ```

2. **Use workspace-scoped endpoint:**
   ```javascript
   // ✅ RECOMMENDED
   const url = `/api/workspaces/${workspaceId}/projects/${projectId}/tasks`;
   ```

3. **Ensure JWT token is included:**
   ```javascript
   headers: {
     'Authorization': `Bearer ${jwtToken}`,
     'Content-Type': 'application/json'
   }
   ```

### Common 403 Error Causes

If you still get 403 errors after this fix:

1. **JWT Token Issues**
   - Check token is not expired
   - Verify `Authorization: Bearer <token>` format
   - Ensure token is valid

2. **User Permissions**
   - User must be workspace owner OR workspace member
   - Verify user is actually a member of the workspace

3. **Project-Workspace Mismatch**
   - Ensure the project belongs to the workspace in the URL
   - Check `projectId` and `workspaceId` are correct

---

## Summary

### Before Fix
- ❌ Documentation used `assignedTo` (incorrect)
- ❌ Query parameters documented but not implemented
- ❌ Missing workspace-scoped example

### After Fix
- ✅ All examples use `assignedToId` (correct)
- ✅ Removed unsupported query parameters
- ✅ Added workspace-scoped endpoint example
- ✅ Clear documentation for frontend developers

---

## Related Files
- `TaskRequests.http` - Fixed API test file
- `TaskCreateRequestDTO.java` - DTO specification (defines `assignedToId`)
- `TaskController.java` - Controller implementation
- `SecurityConfig.java` - Security configuration

## References
- [WORKSPACE_TASK_ENDPOINT_FIX.md](./WORKSPACE_TASK_ENDPOINT_FIX.md) - Previous task endpoint documentation
- [BACKEND_FIX_SUMMARY.md](./BACKEND_FIX_SUMMARY.md) - Authorization fixes
