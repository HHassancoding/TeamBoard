# API Quick Reference Card

## 🚀 Endpoints at a Glance

### Authentication (Base: `/api/auth`)
```
POST   /register           Register new user
POST   /login             Login (returns token)
POST   /refresh           Refresh access token
POST   /me                Get current user
```

### Users (Base: `/api/users`)
```
GET    /                  Get all users
GET    /{id}              Get user by ID
POST   /create            Create user
PUT    /update/{id}       Update user
DELETE /delete/{id}       Delete user
```

### Workspaces (Base: `/api/workspaces`)
```
POST   /                           Create workspace
GET    /                           Get all workspaces
GET    /{id}                       Get workspace
PUT    /{id}                       Update workspace (owner)
DELETE /{id}                       Delete workspace (owner)
GET    /owner/{ownerId}            Get by owner

POST   /{id}/members               Add member (owner)
DELETE /{id}/members/{userId}      Remove member (owner)
GET    /{id}/members               List members
```

### Projects (Base: `/api/workspaces/{workspaceId}/projects`)
```
POST   /                           Create project (auto-creates columns)
GET    /                           Get all projects
GET    /{projectId}                Get project
PUT    /{projectId}                Update project (creator)
DELETE /{projectId}                Delete project (creator)
```

### Kanban Columns (Base: `/api/projects/{projectId}`)
```
GET    /columns                    Get all columns (4 fixed: BACKLOG, TO_DO, IN_PROGRESS, DONE)
```

### Tasks (Base: `/api`)
```
POST   /projects/{id}/tasks                    Create task
GET    /projects/{id}/tasks                    Get tasks (filter: columnId, assignedTo, priority)
GET    /tasks/{id}                             Get task
PUT    /tasks/{id}                             Update task
DELETE /tasks/{id}                             Delete task
PATCH  /tasks/{id}/column/{columnId}           Move task to column
```

---

## 📊 HTTP Status Codes

| Code | Meaning | Example Cause |
|------|---------|---------------|
| 200 | OK | GET, PUT successful |
| 201 | Created | POST successful |
| 204 | No Content | DELETE successful |
| 400 | Bad Request | Invalid input, missing field |
| 401 | Unauthorized | Missing/invalid token |
| 403 | Forbidden | Not owner, not member |
| 404 | Not Found | Resource doesn't exist |
| 409 | Conflict | Duplicate resource |
| 500 | Server Error | Unexpected error |

---

## 🔐 Authentication Pattern

```
1. POST /api/auth/register
   Body: { name, email, password }
   ↓
2. POST /api/auth/login
   Body: { email, password }
   Response: { token, refreshToken, expiresIn }
   ↓
3. Store token (secure)
   ↓
4. Include in requests: Authorization: Bearer {token}
   ↓
5. When expired: POST /api/auth/refresh
   Header: Authorization: Bearer {refreshToken}
   Response: { token, refreshToken }
```

---

## 🏢 Role-Based Access

| Role | Create | Edit | Delete | Manage Members |
|------|--------|------|--------|----------------|
| Owner | ✅ | ✅ | ✅ | ✅ |
| ADMIN | ✅ | ✅ | ✅ | ✅ |
| MEMBER | ✅ | ✅ | Own | ❌ |
| VIEWER | ❌ | ❌ | ❌ | ❌ |

---

## 📋 Task Statuses (Columns)

| Column | Position | Purpose |
|--------|----------|---------|
| BACKLOG | 1 | Unstarted tasks |
| TO_DO | 2 | Ready to start |
| IN_PROGRESS | 3 | Being worked on |
| DONE | 4 | Completed |

*Note: Columns are auto-created, read-only*

---

## 🎯 Priority Levels

```
LOW     - Green badge - Can wait
MEDIUM  - Yellow badge - Normal (default)
HIGH    - Red badge - Urgent
```

---

## 📝 Request/Response Pattern

### Create Task Request
```json
POST /api/projects/1/tasks
Authorization: Bearer {token}

{
  "title": "Task name",
  "description": "Optional",
  "assignedToId": 2,
  "priority": "HIGH",
  "dueDate": "2025-01-20"
}
```

### Create Task Response
```json
201 Created

{
  "id": 1,
  "title": "Task name",
  "projectId": 1,
  "columnId": 1,
  "assignedToId": 2,
  "assignedToName": "Jane Smith",
  "priority": "HIGH",
  "dueDate": "2025-01-20",
  "createdById": 1,
  "createdByName": "John Doe",
  "createdAt": "2025-01-10T10:30:00Z",
  "updatedAt": "2025-01-10T10:30:00Z"
}
```

---

## 🔄 Typical Workflow

```
1. Register/Login
   ↓
2. Create Workspace
   ↓
3. Add Members to Workspace
   ↓
4. Create Project
   └─→ Auto-creates 4 columns
   ↓
5. Get Columns (for Kanban)
   ↓
6. Create Tasks
   └─→ Default to Backlog
   ↓
7. Drag Tasks Between Columns
   ↓
8. Update Task Details
   ↓
9. Delete Tasks When Done
```

---

## ⚙️ Common Filtering

### Get Tasks with Filters
```
GET /api/projects/1/tasks?columnId=2&assignedTo=3&priority=HIGH
```

**Available Filters:**
- `columnId` - Filter by column ID
- `assignedTo` - Filter by user ID
- `priority` - Filter by priority (LOW, MEDIUM, HIGH)

---

## 🛠️ Error Response Examples

### 400 Bad Request
```json
"Workspace name is required"
```

### 401 Unauthorized
```json
"User not found" or "Invalid authorization header"
```

### 403 Forbidden
```json
"You are not a member of this workspace"
```

### 404 Not Found
```json
"Workspace not found"
```

---

## 📦 Data Model Summary

### User
- id, name, email, avatarInitials, createdAt

### Workspace
- id, name, description, ownerId, createdAt

### Project
- id, name, description, workspaceId, createdById, createdAt

### Task
- id, title, description, projectId, columnId, assignedToId, priority, dueDate, createdById, createdAt

### Column
- id, name, position, projectId, createdAt
- *4 per project: BACKLOG(1), TO_DO(2), IN_PROGRESS(3), DONE(4)*

---

## 🔑 Key Points

✅ **Always Include:** `Authorization: Bearer {token}` header  
✅ **Task Defaults:** Backlog column, MEDIUM priority  
✅ **Columns:** Auto-created, read-only, 4 per project  
✅ **Drag & Drop:** Use PATCH `/tasks/{id}/column/{columnId}`  
✅ **Timestamps:** ISO 8601 format (UTC)  
✅ **Token Expires:** 1 hour (use refresh token)  
✅ **Roles:** ADMIN, MEMBER, VIEWER (workspace), Owner (workspace/project)  

---

## 🚫 Common Mistakes

❌ Forgetting `Authorization` header  
❌ Trying to manually create columns  
❌ Trying to move task to column in different project  
❌ Not handling 401 (expired token)  
❌ Storing plaintext passwords  
❌ Using wrong ID types (mixing user ID, workspace ID, etc.)  

---

## 📚 Documentation Files

| File | Purpose |
|------|---------|
| API_COMPLETE_REFERENCE.md | Master guide |
| ENDPOINT_DOCUMENTATION_INDEX.md | Navigation hub |
| AUTHENTICATION_ENDPOINTS.md | Auth details |
| WORKSPACE_ENDPOINTS.md | Workspace details |
| PROJECT_ENDPOINTS.md | Project details |
| BOARDCOLUMN_ENDPOINTS.md | Column details |
| TASK_API_ENDPOINTS.md | Task details |
| FRONTEND_IMPLEMENTATION_GUIDE.md | What to build |

---

## 🔗 Common Links

**Base URL:** `http://localhost:8080/api`  
**Production:** `https://teamboard-backend.onrender.com/api`  
**CORS:** Allows all origins (*)

---

## 💾 Database

- **Database:** PostgreSQL 15 (Prod), H2 (Test)
- **ORM:** JPA/Hibernate
- **Migrations:** Flyway

---

**Last Updated:** January 2025 | API v1.0


