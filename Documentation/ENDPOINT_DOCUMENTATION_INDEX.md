# API Documentation Index

Welcome to the TeamBoard API Documentation! This index will help you navigate all available endpoint documentation.

---

## 📋 Documentation Files

### **START HERE: Complete API Reference**
📄 **`API_COMPLETE_REFERENCE.md`** - Master guide with all endpoints overview, workflows, data models, and troubleshooting

---

## By Feature Area

### 🔐 Authentication & User Management
1. **`AUTHENTICATION_ENDPOINTS.md`**
   - User registration
   - Login with JWT
   - Token refresh
   - Get current user

2. **`USER_MANAGEMENT_ENDPOINTS.md`**
   - Get all users
   - Get single user
   - Create user
   - Update user
   - Delete user

### 🏢 Workspaces
3. **`WORKSPACE_ENDPOINTS.md`**
   - Create workspace
   - Get all workspaces
   - Get single workspace
   - Update workspace
   - Delete workspace
   - Add member to workspace
   - Remove member from workspace
   - List workspace members

### 📊 Projects
4. **`PROJECT_ENDPOINTS.md`**
   - Create project
   - Get projects in workspace
   - Get single project
   - Update project
   - Delete project
   - *Auto-creates 4 Kanban columns*

### 📋 Kanban Columns
5. **`BOARDCOLUMN_ENDPOINTS.md`**
   - Get all columns for project
   - Column structure (BACKLOG, TO_DO, IN_PROGRESS, DONE)
   - *Read-only (auto-created with projects)*

### ✅ Tasks
6. **`TASK_API_ENDPOINTS.md`**
   - Create task
   - Get all tasks in project
   - Get single task
   - Update task
   - Delete task
   - Move task between columns (drag & drop)

---

## 📊 Quick Reference

### Total Endpoints Implemented: **23**

#### Authentication (4)
- POST `/auth/register` - Register user
- POST `/auth/login` - Login
- POST `/auth/refresh` - Refresh token
- POST `/auth/me` - Get current user

#### User Management (5)
- GET `/users` - Get all users
- GET `/users/{id}` - Get user
- POST `/users/create` - Create user
- PUT `/users/update/{id}` - Update user
- DELETE `/users/delete/{id}` - Delete user

#### Workspaces (6)
- POST `/workspaces` - Create workspace
- GET `/workspaces` - Get all workspaces
- GET `/workspaces/{id}` - Get workspace
- PUT `/workspaces/{id}` - Update workspace
- DELETE `/workspaces/{id}` - Delete workspace
- GET `/workspaces/owner/{ownerId}` - Get by owner
- POST `/workspaces/{id}/members` - Add member
- DELETE `/workspaces/{id}/members/{userId}` - Remove member
- GET `/workspaces/{id}/members` - List members

#### Projects (5)
- POST `/workspaces/{workspaceId}/projects` - Create project
- GET `/workspaces/{workspaceId}/projects` - List projects
- GET `/workspaces/{workspaceId}/projects/{projectId}` - Get project
- PUT `/workspaces/{workspaceId}/projects/{projectId}` - Update project
- DELETE `/workspaces/{workspaceId}/projects/{projectId}` - Delete project

#### Kanban Columns (1)
- GET `/projects/{projectId}/columns` - Get columns (read-only)

#### Tasks (6)
- POST `/projects/{projectId}/tasks` - Create task
- GET `/projects/{projectId}/tasks` - Get tasks
- GET `/tasks/{taskId}` - Get task
- PUT `/tasks/{taskId}` - Update task
- DELETE `/tasks/{taskId}` - Delete task
- PATCH `/tasks/{taskId}/column/{columnId}` - Move task

---

## 🎯 Common Use Cases

### "I want to..."

#### Create a workspace and invite team members
→ Read: `WORKSPACE_ENDPOINTS.md` (sections 1, 7, 9)

#### Build a Kanban board UI
→ Read: `PROJECT_ENDPOINTS.md` (section 1) + `BOARDCOLUMN_ENDPOINTS.md` (section 1) + `TASK_API_ENDPOINTS.md` (section 2)

#### Implement drag-and-drop task movement
→ Read: `TASK_API_ENDPOINTS.md` (section 6)

#### Display user profiles
→ Read: `USER_MANAGEMENT_ENDPOINTS.md` (sections 1, 2)

#### Implement login/signup flow
→ Read: `AUTHENTICATION_ENDPOINTS.md` (sections 1, 2, 3)

#### Show assigned tasks for a user
→ Read: `TASK_API_ENDPOINTS.md` (section 2, use `assignedTo` filter)

---

## 📱 HTTP Request Files

Test these endpoints using the provided HTTP request files:

- **`JWTRequests.http`** - Authentication requests
- **`WorkspaceRequests.http`** - Workspace operations
- **`ProjectRequests.http`** - Project operations
- **`BoardColumnRequests.http`** - Column operations
- **`TaskRequests.http`** - Task operations
- **`WorkspaceMemberRequests.http`** - Member operations

### How to use:
1. Open any `.http` file in IntelliJ IDEA
2. Update variables (token, IDs, etc.) as needed
3. Click the "Run" button on each request
4. View response in the right panel

---

## 🔗 Related Documentation

### Architecture & Design
- `ARCHITECTURE_DIAGRAM.md` - System design overview
- `IMPLEMENTATION_SUMMARY.md` - Features implemented
- `BEFORE_AFTER_COMPARISON.md` - Changes made

### Testing
- `TESTING_GUIDE.md` - How to test endpoints

### Deployment
- `FINAL_DEPLOYMENT_CHECKLIST.md` - Production checklist

---

## 🚀 Getting Started (5 minutes)

### Step 1: Understand the System
Read: `API_COMPLETE_REFERENCE.md` (overview section)

### Step 2: Learn Authentication
Read: `AUTHENTICATION_ENDPOINTS.md`

### Step 3: Create Resources
Read in order:
1. `WORKSPACE_ENDPOINTS.md` (create workspace)
2. `PROJECT_ENDPOINTS.md` (create project)
3. `BOARDCOLUMN_ENDPOINTS.md` (understand columns)
4. `TASK_API_ENDPOINTS.md` (create tasks)

### Step 4: Test Everything
Use the `.http` files to test all endpoints

---

## 📌 Important Notes for Frontend

1. **Authentication Required:** Most endpoints need `Authorization: Bearer {token}` header
2. **Defaults:** 
   - Tasks default to Backlog column
   - Priority defaults to MEDIUM
   - Member role defaults to MEMBER
3. **Read-Only:** Columns are read-only (auto-created with projects)
4. **Workflow:** Workspace → Project → Task flow is typical
5. **Column IDs:** Get from `GET /api/projects/{projectId}/columns` when building Kanban board

---

## 🆘 Need Help?

### Common Issues
- **401 Unauthorized:** Missing or invalid JWT token
- **403 Forbidden:** Not a workspace member or not the owner
- **404 Not Found:** Resource doesn't exist
- **400 Bad Request:** Invalid input (check request body)

See `API_COMPLETE_REFERENCE.md` (troubleshooting section) for more help

---

## 📊 Data Models at a Glance

### User
ID, Name, Email, AvatarInitials, CreatedAt

### Workspace
ID, Name, Description, OwnerId, CreatedAt

### WorkspaceMember
ID, UserId, Role (ADMIN/MEMBER/VIEWER), JoinedAt

### Project
ID, Name, Description, WorkspaceId, CreatedById, CreatedAt

### BoardColumn
ID, Name (enum: BACKLOG, TO_DO, IN_PROGRESS, DONE), Position, ProjectId, CreatedAt

### Task
ID, Title, Description, ProjectId, ColumnId, AssignedToId, Priority (LOW/MEDIUM/HIGH), DueDate, CreatedById, CreatedAt

---

## 🔄 Version History

| Date | Version | Changes |
|------|---------|---------|
| Jan 2025 | 1.0 | Initial API documentation complete |

---

## 💡 Tips for Frontend Development

1. **Use HTTP client in IntelliJ:** Copy `.http` files for easy testing
2. **Store Token:** After login, store token in state management
3. **Automatic Refresh:** Implement token refresh before expiration
4. **Error Handling:** Check status codes and display user-friendly messages
5. **Optimistic Updates:** Update UI immediately for faster perceived performance
6. **Caching:** Cache workspace/project/user data to reduce API calls

---

## 📞 Support

For issues or questions:
1. Check `API_COMPLETE_REFERENCE.md` (troubleshooting section)
2. Review the specific endpoint documentation
3. Check HTTP request files for correct format
4. Review test files for usage examples

---

**Last Updated:** January 2025  
**API Version:** 1.0  
**Status:** In Development


