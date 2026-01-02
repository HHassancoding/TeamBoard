# TeamBoard API Complete Reference Guide

## Overview
This is the complete API reference for the TeamBoard backend. TeamBoard is a collaborative task management system where teams create workspaces, organize work into projects, and manage tasks on Kanban boards.

---

## Quick Start

### For Frontend Developers:
1. Read this file for general overview
2. Navigate to specific endpoint documentation based on feature:
   - **Authentication:** `AUTHENTICATION_ENDPOINTS.md`
   - **User Management:** `USER_MANAGEMENT_ENDPOINTS.md`
   - **Workspaces:** `WORKSPACE_ENDPOINTS.md`
   - **Projects:** `PROJECT_ENDPOINTS.md`
   - **Kanban Columns:** `BOARDCOLUMN_ENDPOINTS.md`
   - **Tasks:** `TASK_API_ENDPOINTS.md`

### For Backend Developers:
3. Review the implementation details in each controller file
4. Check tests for expected behavior

---

## Base URL
```
http://localhost:8080/api
```

For production (Render):
```
https://teamboard-backend.onrender.com/api
```

---

## API Structure

The API is organized by feature domain:

### 1. Authentication & Users
- Register new users
- Login and token refresh
- Get current user
- Manage user profiles

**Base:** `/api/auth`, `/api/users`  
**Documentation:** `AUTHENTICATION_ENDPOINTS.md`, `USER_MANAGEMENT_ENDPOINTS.md`

### 2. Workspaces
- Create and manage workspaces
- Add/remove members with roles (ADMIN, MEMBER, VIEWER)
- List workspace members

**Base:** `/api/workspaces`  
**Documentation:** `WORKSPACE_ENDPOINTS.md`

### 3. Projects
- Create projects within workspaces
- Manage project details
- Auto-generated 4-column Kanban board

**Base:** `/api/workspaces/{workspaceId}/projects`  
**Documentation:** `PROJECT_ENDPOINTS.md`

### 4. Kanban Columns
- Read-only access to project columns
- 4 columns per project: Backlog, To Do, In Progress, Done
- Auto-created, cannot be manually modified

**Base:** `/api/projects/{projectId}/columns`  
**Documentation:** `BOARDCOLUMN_ENDPOINTS.md`

### 5. Tasks
- Create, read, update, delete tasks
- Move tasks between columns (drag & drop)
- Assign tasks to team members
- Set priority and due dates

**Base:** `/api/projects/{projectId}/tasks`, `/api/tasks`  
**Documentation:** `TASK_API_ENDPOINTS.md`

---

## Authentication

### How It Works
1. User logs in with email/password
2. Backend returns JWT access token + refresh token
3. Frontend includes token in `Authorization: Bearer {token}` header
4. When token expires, use refresh token to get new access token

### Token Types
- **Access Token:** Used for API requests (expires in 1 hour)
- **Refresh Token:** Used to get new access token (longer expiration)

### Example Request with Auth:
```http
GET /api/workspaces HTTP/1.1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## Common Response Patterns

### Success Response (200 OK)
```json
{
  "id": 1,
  "name": "Example",
  "createdAt": "2025-01-10T10:30:00Z"
}
```

### Error Response (400 Bad Request)
```json
"Error message describing what went wrong"
```

### Error Response (401 Unauthorized)
```json
"User not found" or "Invalid authorization header"
```

### Error Response (403 Forbidden)
```json
"You are not a member of this workspace"
```

### Error Response (404 Not Found)
```json
"Workspace not found"
```

---

## Common Status Codes

| Code | Meaning | Use Case |
|------|---------|----------|
| 200 | OK | Successful GET, PUT request |
| 201 | Created | Successful POST request |
| 204 | No Content | Successful DELETE request |
| 400 | Bad Request | Validation error |
| 401 | Unauthorized | Missing/invalid token |
| 403 | Forbidden | User not authorized (not owner, not member) |
| 404 | Not Found | Resource doesn't exist |
| 409 | Conflict | Business logic violation (e.g., duplicate) |
| 500 | Server Error | Unexpected server error |

---

## Data Models

### User
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "avatarInitials": "JD",
  "createdAt": "2025-01-10T10:30:00Z"
}
```

### Workspace
```json
{
  "id": 1,
  "name": "Marketing Team",
  "description": "Marketing projects",
  "ownerId": 1,
  "ownerName": "John Doe",
  "ownerEmail": "john@example.com",
  "createdAt": "2025-01-10T10:30:00Z"
}
```

### WorkspaceMember
```json
{
  "id": 1,
  "userId": 2,
  "userEmail": "jane@example.com",
  "userName": "Jane Smith",
  "role": "MEMBER",
  "joinedAt": "2025-01-12T10:30:00Z"
}
```

### Project
```json
{
  "id": 1,
  "name": "Website Redesign",
  "description": "Complete redesign",
  "workspaceId": 1,
  "createdById": 1,
  "createdByName": "John Doe",
  "createdAt": "2025-01-10T10:30:00Z"
}
```

### BoardColumn
```json
{
  "id": 1,
  "name": "BACKLOG",
  "position": 1,
  "projectId": 1,
  "createdAt": "2025-01-10T10:30:00Z"
}
```

### Task
```json
{
  "id": 1,
  "title": "Design homepage",
  "description": "Create mockups",
  "projectId": 1,
  "columnId": 1,
  "assignedToId": 2,
  "assignedToName": "Jane Smith",
  "priority": "HIGH",
  "dueDate": "2025-01-20",
  "createdById": 1,
  "createdByName": "John Doe",
  "createdAt": "2025-01-10T10:30:00Z"
}
```

---

## Typical User Workflows

### Workflow 1: Create Workspace and Add Members
```
1. POST /api/auth/register
   → Create user account

2. POST /api/auth/login
   → Get JWT tokens

3. POST /api/workspaces
   → Create workspace (current user is owner)

4. GET /api/workspaces/{id}
   → View workspace details

5. POST /api/workspaces/{id}/members
   → Invite other users to workspace

6. GET /api/workspaces/{id}/members
   → View all members
```

### Workflow 2: Create Project and View Kanban Board
```
1. POST /api/workspaces/{workspaceId}/projects
   → Create project (auto-creates 4 columns)

2. GET /api/projects/{projectId}/columns
   → Fetch columns (BACKLOG, TO_DO, IN_PROGRESS, DONE)

3. GET /api/projects/{projectId}/tasks
   → Fetch all tasks

4. Render 4 columns in UI, distribute tasks by columnId
```

### Workflow 3: Create and Manage Tasks
```
1. POST /api/projects/{projectId}/tasks
   → Create task (goes to Backlog by default)

2. GET /api/tasks/{taskId}
   → View task details

3. PUT /api/tasks/{taskId}
   → Update task (title, description, priority, assignee)

4. PATCH /api/tasks/{taskId}/column/{columnId}
   → Move task to different column (drag & drop)

5. DELETE /api/tasks/{taskId}
   → Delete task
```

---

## Role-Based Access

### Workspace Roles
| Role | Permissions |
|------|-------------|
| ADMIN | Create/edit projects, create/edit tasks, manage members |
| MEMBER | Create/edit projects, create/edit tasks |
| VIEWER | Read-only access |

### Other Ownership Rules
- **Workspace:** Only owner can update/delete, manage members
- **Project:** Only creator can update/delete
- **Tasks:** Anyone in workspace can create, but only assignee/creator can edit
- **Columns:** Read-only (auto-created, cannot modify)

---

## CORS & Security

### CORS Settings
Currently allows requests from all origins (`*`). In production, this should be restricted to your frontend domain.

### Authentication
- All endpoints except `/auth/register` and `/auth/login` require JWT token
- Token must be in `Authorization: Bearer {token}` header
- Token expires in 1 hour; use refresh token to get new one

### Password Security
- Passwords are hashed with BCrypt before storage
- Never send passwords in plain text (always use HTTPS)

---

## Rate Limiting
Not currently implemented. Should be added for production.

---

## API Evolution Notes

### Planned Additions
- Task comments and activity log
- Task labels/tags
- Task attachments
- Workspace invitations (email)
- User search
- More advanced filtering and sorting
- Webhook support for external integrations

### Breaking Changes in Future
None planned for current endpoints. New features will be additive.

---

## Troubleshooting Common Issues

### 401 Unauthorized
- **Cause:** Missing or invalid JWT token
- **Solution:** Login again to get new token, ensure token is in `Authorization: Bearer {token}` format

### 403 Forbidden
- **Cause:** User doesn't have permission (not owner, not member, etc.)
- **Solution:** Verify user role and workspace/project ownership

### 404 Not Found
- **Cause:** Resource doesn't exist with that ID
- **Solution:** Verify ID is correct and resource hasn't been deleted

### 400 Bad Request
- **Cause:** Invalid input or validation error
- **Solution:** Check request body format matches documentation

### Circular Dependency Error
- **Cause:** Service circular dependency (e.g., Project → BoardColumn → Project)
- **Solution:** Review service dependencies and inject properly via constructor

---

## Testing Endpoints

HTTP request files are provided for testing:
- `JWTRequests.http` - Authentication testing
- `WorkspaceRequests.http` - Workspace operations
- `ProjectRequests.http` - Project operations
- `BoardColumnRequests.http` - Column operations
- `TaskRequests.http` - Task operations

These can be tested with:
- IntelliJ IDEA built-in HTTP client
- Postman
- REST Client VS Code extension
- curl command line

---

## Database Schema

### Key Tables
- `users` - User accounts
- `workspaces` - Workspace containers
- `workspace_members` - User-Workspace mapping with roles
- `projects` - Project containers within workspaces
- `board_columns` - Kanban columns (4 per project)
- `tasks` - Task items with assignments
- `task_assignees` - Task-User mapping (future: multi-assign)

### Foreign Key Relationships
```
users
  ├── owns → workspaces
  ├── creates → projects
  ├── creates → tasks
  └── receives → task_assignments

workspaces
  ├── has many → workspace_members
  ├── has many → projects
  └── has many → board_columns (via projects)

projects
  └── has → board_columns (4 columns)

board_columns
  └── has many → tasks

tasks
  ├── belongs to → projects
  ├── belongs to → board_columns
  └── assigned to → users (optional)
```

---

## Support & Documentation

For detailed endpoint documentation, see:
- `AUTHENTICATION_ENDPOINTS.md` - Auth endpoints
- `USER_MANAGEMENT_ENDPOINTS.md` - User endpoints
- `WORKSPACE_ENDPOINTS.md` - Workspace endpoints
- `PROJECT_ENDPOINTS.md` - Project endpoints
- `BOARDCOLUMN_ENDPOINTS.md` - Column endpoints
- `TASK_API_ENDPOINTS.md` - Task endpoints

For architecture overview:
- `ARCHITECTURE_DIAGRAM.md` - System design
- `IMPLEMENTATION_SUMMARY.md` - Features implemented

---

## Version Info

**API Version:** 1.0  
**Last Updated:** January 2025  
**Status:** In Development  
**Backend:** Spring Boot 4.0.0, Java 23  
**Database:** PostgreSQL 15 (Production), H2 (Testing)


