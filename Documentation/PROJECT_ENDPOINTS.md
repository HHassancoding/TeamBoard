# Project API Endpoints

## Base URL
```
/api/workspaces/{workspaceId}/projects
```

## Overview
Project endpoints allow workspace members to create, manage, and retrieve projects within a workspace. Projects are containers for tasks and have an associated Kanban board with columns (Backlog, To Do, In Progress, Done).

---

## 1. Create Project

**Method:** `POST`  
**Endpoint:** `/api/workspaces/{workspaceId}/projects`  
**Description:** Create a new project within a workspace (auto-creates 4 default columns)

**Path Parameters:**
- `workspaceId` (Long, required) - Workspace ID

**Request Headers:**
```
Authorization: Bearer {token} (required - must be workspace member)
```

**Request Body:**
```json
{
  "name": "string (required, max 255 chars)",
  "description": "string (optional)"
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "name": "Website Redesign",
  "description": "Complete redesign of company website",
  "workspaceId": 1,
  "createdById": 1,
  "createdByName": "John Doe",
  "createdAt": "2025-01-10T10:30:00Z",
  "updatedAt": "2025-01-10T10:30:00Z"
}
```

**Auto-created Columns:**
When a project is created, 4 default columns are automatically created:
1. **Backlog** (position 1)
2. **To Do** (position 2)
3. **In Progress** (position 3)
4. **Done** (position 4)

These columns are available immediately for use (retrieve with GET columns endpoint).

**Validation Rules:**
- Project name is required and cannot be empty
- User must be a member of the workspace
- Max name length: 255 characters

**Error Responses:**
- `400 Bad Request` - Invalid input
- `401 Unauthorized` - Token missing or invalid
- `403 Forbidden` - User is not a member of the workspace
- `404 Not Found` - Workspace not found
- `500 Internal Server Error` - Server error

---

## 2. Get Projects in Workspace

**Method:** `GET`  
**Endpoint:** `/api/workspaces/{workspaceId}/projects`  
**Description:** Retrieve all projects in a workspace

**Path Parameters:**
- `workspaceId` (Long, required) - Workspace ID

**Request Headers:**
```
Authorization: Bearer {token} (required)
```

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "name": "Website Redesign",
    "description": "Complete redesign of company website",
    "workspaceId": 1,
    "createdById": 1,
    "createdByName": "John Doe",
    "createdAt": "2025-01-10T10:30:00Z",
    "updatedAt": "2025-01-10T10:30:00Z"
  },
  {
    "id": 2,
    "name": "Mobile App Development",
    "description": "Native iOS and Android apps",
    "workspaceId": 1,
    "createdById": 2,
    "createdByName": "Jane Smith",
    "createdAt": "2025-01-11T14:20:00Z",
    "updatedAt": "2025-01-11T14:20:00Z"
  }
]
```

**Error Responses:**
- `401 Unauthorized` - Token missing or invalid
- `403 Forbidden` - User is not a member of the workspace
- `404 Not Found` - Workspace not found
- `500 Internal Server Error` - Server error

---

## 3. Get Single Project

**Method:** `GET`  
**Endpoint:** `/api/workspaces/{workspaceId}/projects/{projectId}`  
**Description:** Retrieve details of a specific project

**Path Parameters:**
- `workspaceId` (Long, required) - Workspace ID
- `projectId` (Long, required) - Project ID

**Request Headers:**
```
Authorization: Bearer {token} (required)
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "name": "Website Redesign",
  "description": "Complete redesign of company website",
  "workspaceId": 1,
  "createdById": 1,
  "createdByName": "John Doe",
  "createdAt": "2025-01-10T10:30:00Z",
  "updatedAt": "2025-01-10T10:30:00Z"
}
```

**Error Responses:**
- `401 Unauthorized` - Token missing or invalid
- `403 Forbidden` - User is not a member of the workspace or not the project creator
- `404 Not Found` - Workspace or project not found
- `500 Internal Server Error` - Server error

---

## 4. Update Project

**Method:** `PUT`  
**Endpoint:** `/api/workspaces/{workspaceId}/projects/{projectId}`  
**Description:** Update project details (project creator only)

**Path Parameters:**
- `workspaceId` (Long, required) - Workspace ID
- `projectId` (Long, required) - Project ID

**Request Headers:**
```
Authorization: Bearer {token} (required - must be project creator)
```

**Request Body:**
```json
{
  "name": "string (required)",
  "description": "string (optional)"
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "name": "Website Redesign - Updated",
  "description": "Complete redesign of company website with new features",
  "workspaceId": 1,
  "createdById": 1,
  "createdByName": "John Doe",
  "createdAt": "2025-01-10T10:30:00Z",
  "updatedAt": "2025-01-12T11:00:00Z"
}
```

**Authorization:**
- Only the project creator can update the project

**Error Responses:**
- `400 Bad Request` - Invalid input
- `401 Unauthorized` - Token missing or invalid
- `403 Forbidden` - User is not a member of the workspace or not the project creator
- `404 Not Found` - Workspace or project not found
- `500 Internal Server Error` - Server error

---

## 5. Delete Project

**Method:** `DELETE`  
**Endpoint:** `/api/workspaces/{workspaceId}/projects/{projectId}`  
**Description:** Delete a project and all its contents (project creator only)

**Path Parameters:**
- `workspaceId` (Long, required) - Workspace ID
- `projectId` (Long, required) - Project ID

**Request Headers:**
```
Authorization: Bearer {token} (required - must be project creator)
```

**Response:** `204 No Content` (empty body)

**Authorization:**
- Only the project creator can delete the project
- Deleting a project will delete all tasks and columns associated with it

**Error Responses:**
- `401 Unauthorized` - Token missing or invalid
- `403 Forbidden` - User is not a member of the workspace or not the project creator
- `404 Not Found` - Workspace or project not found
- `500 Internal Server Error` - Server error

---

## Project Object Structure

```json
{
  "id": "Long - Unique project identifier",
  "name": "String - Project name",
  "description": "String - Optional project description",
  "workspaceId": "Long - ID of the workspace containing this project",
  "createdById": "Long - ID of the user who created the project",
  "createdByName": "String - Name of the user who created the project",
  "createdAt": "Timestamp - When project was created",
  "updatedAt": "Timestamp - When project was last updated"
}
```

---

## Related Endpoints

### Get Project Columns (Kanban Board)
- **Method:** `GET`
- **Endpoint:** `/api/projects/{projectId}/columns`
- **Description:** Get all 4 columns for this project (see BoardColumn API documentation)
- **Returns:** List of BoardColumn objects ordered by position

### Create Task in Project
- **Method:** `POST`
- **Endpoint:** `/api/projects/{projectId}/tasks`
- **Description:** Create a new task in this project (see Task API documentation)
- **Note:** Tasks automatically go to the Backlog column

---

## Important Notes for Frontend

1. **Auto-created Columns:** Don't create columns yourself; they're auto-created when project is created
2. **Column Structure:** Projects always have exactly 4 columns in this order:
   - Backlog (position 1)
   - To Do (position 2)
   - In Progress (position 3)
   - Done (position 4)
3. **Project Creator:** The user who creates the project is the project owner and can edit/delete it
4. **Workspace Membership:** User must be a member of the workspace to create/access projects
5. **Kanban Board:** To display a Kanban board UI, fetch the project then fetch its columns with the columns endpoint
6. **Timestamps:** All timestamps are in ISO 8601 format (UTC)

---

## Workflow Example: Create a Project and View its Board

```
1. POST /api/workspaces/1/projects
   → Creates project and auto-creates 4 columns

2. GET /api/projects/1/columns
   → Returns the 4 columns (Backlog, To Do, In Progress, Done)

3. POST /api/projects/1/tasks
   → Creates task in Backlog column

4. GET /api/projects/1/tasks
   → Gets all tasks, grouped by column
```

---

## Status Codes Summary
| Code | Meaning |
|------|---------|
| 201 | Created successfully |
| 200 | Success |
| 204 | Deleted successfully |
| 400 | Bad request (validation error) |
| 401 | Unauthorized |
| 403 | Forbidden (not creator or not member) |
| 404 | Workspace or project not found |
| 500 | Server error |


