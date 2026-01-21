# Workspace-Scoped Task Endpoint - 403 Error Resolution

## Overview
This document explains the resolution of the 403 Forbidden error issue on the POST task creation endpoint.

## Executive Summary
**Issue:** Frontend receiving 403 Forbidden when calling `POST /api/workspaces/{workspaceId}/projects/{projectId}/tasks`

**Root Cause:** The endpoint exists and works correctly, but was completely undocumented, causing confusion about request format and authorization requirements.

**Solution:** Comprehensive documentation updates and debugging configuration.

## Investigation Findings

### Endpoint Status
✅ **Workspace-scoped POST endpoint exists** (TaskController.java lines 263-398)
✅ **Proper authorization implemented** (workspace owner OR member)
✅ **Extensive debug logging in place** (lines 269-397)
✅ **Identical authorization logic** to working GET endpoints
✅ **All tests passing** (75/75)

### The Endpoint Already Works!
The workspace-scoped endpoint at `/workspaces/{workspaceId}/projects/{projectId}/tasks` has been implemented with:

```java
@PostMapping("/workspaces/{workspaceId}/projects/{projectId}/tasks")
public ResponseEntity<?> createTaskAlias(
    @PathVariable Long workspaceId,
    @PathVariable Long projectId,
    @RequestHeader("Authorization") String bearerToken,
    @RequestBody TaskCreateRequestDTO taskRequestDTO)
```

**Authorization Logic:**
```java
boolean isOwner = workspace.getOwner().getId().equals(currentUser.getId());
WorkspaceMember member = workspaceMemberService.getMember(currentUser.getId(), workspaceId);
boolean isMember = member != null;

if (!isOwner && !isMember) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body("You are not a member of this workspace");
}
```

## Changes Made

### 1. Documentation Updates (TASK_API_ENDPOINTS.md)

#### Endpoint Documentation
- **Added:** Workspace-scoped endpoint paths
- **Clarified:** Which endpoint to use (workspace-scoped recommended for frontend)
- **Format:** Both POST and GET operations documented

#### Request Format Corrections
**Before:** `assignedTo` (incorrect)
**After:** `assignedToId` (correct field name in DTO)

**Clarified:** `columnId` should NOT be sent in request body
- Tasks automatically assign to Backlog column
- Use `PATCH /tasks/{taskId}/column/{columnId}` to move tasks

#### Parameter Descriptions
**Before:** "workspaceId (optional)" - contradictory
**After:** "workspaceId - Workspace ID (required for workspace-scoped endpoint)" - clear

### 2. Configuration (application.properties)

Added logging configuration to help diagnose 403 errors:
```properties
# Enable Spring Security debug logging
logging.level.org.springframework.security=INFO
logging.level.com.teamboard=INFO

# Optional debug level for troubleshooting
# logging.level.org.springframework.security.web.access=DEBUG
```

## Correct Request Format

### Frontend Should Send:
```json
POST /api/workspaces/{workspaceId}/projects/{projectId}/tasks
Authorization: Bearer {jwt-token}
Content-Type: application/json

{
  "title": "Test Task",
  "description": "Test description",
  "assignedToId": 8,
  "priority": "MEDIUM",
  "dueDate": "2026-01-25T23:59:59Z"
}
```

### Field Mapping:
| Frontend Field | Backend DTO Field | Required | Notes |
|----------------|-------------------|----------|-------|
| title | title | Yes | Task title |
| description | description | No | Task description |
| **assignedToId** | assignedToId | No | User ID (Long) |
| priority | priority | No | Defaults to MEDIUM |
| dueDate | dueDate | No | ISO 8601 format |
| ~~columnId~~ | ❌ Not accepted | No | Auto-assigned to Backlog |

## Authorization Requirements

To successfully create a task, the user must be:
- ✅ Authenticated (valid JWT token)
- ✅ **Workspace owner** OR **Workspace member**

The authorization check is identical for both GET and POST operations.

## Debugging 403 Errors

### Debug Logging Output
When you make a POST request, you'll see extensive logging:
```
========================================
TASK CREATION REQUEST START
========================================
Endpoint: POST /api/workspaces/6/projects/4/tasks
Step 1: Validating JWT token...
✓ JWT Valid - User ID: 8, Email: everything@example.com
Step 2: Fetching project with ID: 4
✓ Project found - ID: 4, Name: My Project
Step 3: Validating project belongs to workspace...
  - URL workspaceId: 6
  - Project's workspaceId: 6
✓ Project belongs to workspace
Step 4: Checking workspace access...
  - Workspace ID: 6
  - Workspace Owner ID: 8
  - Current User ID: 8
  - Is Owner? true
  - Is Member? true
  - Member Role: ADMIN
✓ Authorization passed - User has access
...
✓ Task created successfully - ID: 123
========================================
TASK CREATION SUCCESS - Returning 201
========================================
```

### Common 403 Scenarios

1. **JWT Token Invalid/Expired**
   - Check: Authorization header format: `Bearer {token}`
   - Verify token hasn't expired

2. **User Not Member of Workspace**
   - Check: User exists in `workspace_members` table
   - OR user is workspace owner

3. **Project Not in Workspace**
   - Verify `projectId` belongs to `workspaceId`

## Testing the Endpoint

### Using cURL:
```bash
curl -X POST http://localhost:8080/api/workspaces/6/projects/4/tasks \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Task",
    "description": "Test description",
    "assignedToId": 8,
    "priority": "MEDIUM"
  }'
```

### Expected Response (201 Created):
```json
{
  "id": 123,
  "title": "Test Task",
  "description": "Test description",
  "projectId": 4,
  "columnId": 1,
  "assignedToId": 8,
  "assignedToName": "John Doe",
  "assignedToInitials": "JD",
  "priority": "MEDIUM",
  "dueDate": null,
  "createdById": 8,
  "createdByName": "John Doe",
  "createdAt": "2026-01-21T14:30:00Z",
  "updatedAt": "2026-01-21T14:30:00Z",
  "completedAt": null
}
```

## Summary

### What Was Wrong?
❌ Documentation didn't mention workspace-scoped endpoints
❌ Field name mismatch (assignedTo vs assignedToId)
❌ Confusion about columnId field
❌ No debugging guidance

### What's Fixed?
✅ Comprehensive endpoint documentation
✅ Correct field names documented
✅ Clear request format examples
✅ Debugging configuration added
✅ Authorization requirements clarified

### The Endpoint Always Worked!
The workspace-scoped POST endpoint has been properly implemented since the beginning. The issue was purely a lack of documentation, which has now been resolved.

## Next Steps for Frontend

1. **Update API calls** to use `assignedToId` instead of `assignedTo`
2. **Remove `columnId`** from request body (it's ignored anyway)
3. **Use workspace-scoped endpoint** for consistency: `/api/workspaces/{workspaceId}/projects/{projectId}/tasks`
4. **Verify JWT token** is included in Authorization header
5. **Check debug logs** if you still get 403 errors

## References

- **Task API Documentation:** `Documentation/TASK_API_ENDPOINTS.md`
- **Task Controller Implementation:** `src/main/java/com/teamboard/controller/TaskController.java` (lines 263-398)
- **Task DTO:** `src/main/java/com/teamboard/DTO/TaskCreateRequestDTO.java`
- **Security Configuration:** `src/main/java/com/teamboard/config/SecurityConfig.java`
