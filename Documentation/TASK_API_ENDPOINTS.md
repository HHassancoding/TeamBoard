# Task API Endpoints

## Base URL
```
/api
```

## Overview
Task endpoints allow workspace members to create, manage, and retrieve tasks within a project. Tasks are the atomic units of work, organized in a Kanban board by columns. When a task is created, it automatically goes to the Backlog column and can be moved between columns via drag-and-drop.

---

## 1. Create Task
**Method:** `POST`  
**Endpoint:** `/projects/{projectId}/tasks`  
**Description:** Create a new task in the specified project (defaults to Backlog column)

**Request Body:**
```json
{
  "title": "string (required)",
  "description": "string (optional)",
  "assignedTo": "Long (optional - user ID)",
  "priority": "LOW | MEDIUM | HIGH (optional, defaults to MEDIUM)",
  "dueDate": "2025-01-15T23:59:59Z (optional, ISO 8601 format)"
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "title": "string",
  "description": "string",
  "projectId": 1,
  "columnId": 1,
  "assignedToId": 2,
  "assignedToName": "Jane Smith",
  "assignedToInitials": "JS",
  "priority": "LOW | MEDIUM | HIGH",
  "dueDate": "2025-01-15",
  "createdById": 1,
  "createdByName": "John Doe",
  "createdAt": "2025-01-10T10:30:00Z",
  "updatedAt": "2025-01-10T10:30:00Z",
  "completedAt": null
}
```

**Path Parameters:**
- `projectId` (Long, required) - Project ID

**Request Headers:**
```
Authorization: Bearer {token} (required - must be workspace member)
```

**Validation Rules:**
- Title is required and cannot be empty
- Task defaults to BACKLOG column (position 1)
- Priority defaults to MEDIUM if not specified
- Assignee is optional (null means unassigned)
- Due date is optional
- User must be a member of the workspace

**Error Responses:**
- `400 Bad Request` - Invalid input, title required
- `401 Unauthorized` - Token missing or invalid
- `403 Forbidden` - User is not a member of the workspace
- `404 Not Found` - Project not found
- `500 Internal Server Error` - Server error

---

## 2. Get All Tasks in Project
**Method:** `GET`  
**Endpoint:** `/projects/{projectId}/tasks`  
**Description:** Retrieve all tasks for a specific project


**Path Parameters:**
- `projectId` (Long, required) - Project ID

**Request Headers:**
```
Authorization: Bearer {token} (required)
```

**Query Parameters (optional):**
- `columnId`: Filter by column (Long)
- `assignedTo`: Filter by assignee user ID (Long)
- `priority`: Filter by priority (LOW, MEDIUM, HIGH)

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "title": "Design homepage",
    "description": "Create mockups for homepage",
    "projectId": 1,
    "columnId": 1,
    "assignedToId": 2,
    "assignedToName": "Jane Smith",
    "assignedToInitials": "JS",
    "priority": "HIGH",
    "dueDate": "2025-01-20",
    "createdById": 1,
    "createdByName": "John Doe",
    "createdAt": "2025-01-10T10:30:00Z",
    "updatedAt": "2025-01-10T10:30:00Z",
    "completedAt": null
  }
]
```

**Error Responses:**
- `401 Unauthorized` - Token missing or invalid
- `403 Forbidden` - User is not a member of the workspace
- `404 Not Found` - Project not found
- `500 Internal Server Error` - Server error

---

## 3. Get Single Task Detail
**Method:** `GET`  
**Endpoint:** `/tasks/{taskId}`  
**Description:** Retrieve detailed information about a specific task


**Path Parameters:**
- `taskId` (Long, required) - Task ID

**Request Headers:**
```
Authorization: Bearer {token} (required)
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "title": "Design homepage",
  "description": "Create mockups for homepage",
  "projectId": 1,
  "columnId": 1,
  "assignedToId": 2,
  "assignedToName": "Jane Smith",
  "assignedToInitials": "JS",
  "priority": "HIGH",
  "dueDate": "2025-01-20",
  "createdById": 1,
  "createdByName": "John Doe",
  "createdAt": "2025-01-10T10:30:00Z",
  "updatedAt": "2025-01-10T10:30:00Z",
  "completedAt": null
}
```

**Error Responses:**
- `401 Unauthorized` - Token missing or invalid
- `403 Forbidden` - User is not a member of the workspace
- `404 Not Found` - Task or project not found
- `500 Internal Server Error` - Server error

---

## 4. Update Task
**Method:** `PUT`  
**Endpoint:** `/tasks/{taskId}`  
**Description:** Update task details (title, description, assignee, priority, due date)


**Path Parameters:**
- `taskId` (Long, required) - Task ID

**Request Headers:**
```
Authorization: Bearer {token} (required)
```

**Request Body:**
```json
{
  "title": "string (optional)",
  "description": "string (optional)",
  "assignedToId": "Long (optional - user ID)",
  "priority": "LOW | MEDIUM | HIGH (optional)",
  "dueDate": "2025-01-15T23:59:59Z (optional, ISO 8601 format)"
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "title": "Design homepage - Updated",
  "description": "Create mockups for homepage with animations",
  "projectId": 1,
  "columnId": 1,
  "assignedToId": 2,
  "assignedToName": "Jane Smith",
  "assignedToInitials": "JS",
  "priority": "MEDIUM",
  "dueDate": "2025-01-25",
  "createdById": 1,
  "createdByName": "John Doe",
  "createdAt": "2025-01-10T10:30:00Z",
  "updatedAt": "2025-01-12T11:00:00Z",
  "completedAt": null
}
```

**Validation Rules:**
- Cannot update `projectId` or `columnId` via this endpoint (use move endpoint instead)
- All fields are optional; only include fields you want to update
- Can set `assignedToId` to null to unassign the task

**Error Responses:**
- `400 Bad Request` - Invalid input
- `401 Unauthorized` - Token missing or invalid
- `403 Forbidden` - User is not a member of the workspace
- `404 Not Found` - Task or project not found
- `500 Internal Server Error` - Server error

---

## 5. Delete Task
**Method:** `DELETE`  
**Endpoint:** `/tasks/{taskId}`  
**Description:** Delete a task (requires confirmation from frontend)

**Response:** `204 No Content`


**Path Parameters:**
- `taskId` (Long, required) - Task ID

**Request Headers:**
```
Authorization: Bearer {token} (required)
```

**Response:** `200 OK`
```json
"Task deleted successfully"
```

**Error Responses:**
- `401 Unauthorized` - Token missing or invalid
- `403 Forbidden` - User is not a member of the workspace
- `404 Not Found` - Task or project not found
- `500 Internal Server Error` - Server error

---

## 6. Move Task Between Columns (Drag & Drop)
**Method:** `PATCH`  
**Endpoint:** `/tasks/{taskId}/column/{columnId}`  
**Description:** Move a task to a different column (used for drag-drop functionality)


**Path Parameters:**
- `taskId` (Long, required) - Task ID
- `columnId` (Long, required) - Target Column ID

**Request Headers:**
```
Authorization: Bearer {token} (required)
```

**Request Body:** (optional, can be empty)
```json
{}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "title": "Design homepage",
  "description": "Create mockups for homepage",
  "projectId": 1,
  "columnId": 2,
  "assignedToId": 2,
  "assignedToName": "Jane Smith",
  "assignedToInitials": "JS",
  "priority": "HIGH",
  "dueDate": "2025-01-20",
  "createdById": 1,
  "createdByName": "John Doe",
  "createdAt": "2025-01-10T10:30:00Z",
  "updatedAt": "2025-01-10T10:35:00Z",
  "completedAt": null
}
```

**Validation Rules:**
- Target column must exist in the same project
- Updates the task's `columnId` and `updatedAt` timestamp
- Does not change `createdBy` or `createdAt`

**Error Responses:**
- `401 Unauthorized` - Token missing or invalid
- `403 Forbidden` - User is not a member of the workspace
- `404 Not Found` - Task, project, or column not found
- `409 Conflict` - Invalid column (not in same project)
- `500 Internal Server Error` - Server error

---

## Priority Enum Values
- `LOW` - Low priority
- `MEDIUM` - Medium priority (default)
- `HIGH` - High priority

## Task Object Structure

```json
{
  "id": "Long - Unique task identifier",
  "title": "String - Task name (required)",
  "description": "String - Optional task description",
  "projectId": "Long - Project this task belongs to",
  "columnId": "Long - Column this task is in (Backlog/To Do/In Progress/Done)",
  "assignedToId": "Long - ID of assigned user (nullable, means unassigned)",
  "assignedToName": "String - Name of assigned user",
  "assignedToInitials": "String - Initials of assigned user for avatar",
  "priority": "String - Priority level (LOW, MEDIUM, HIGH)",
  "dueDate": "String - Due date in YYYY-MM-DD format (nullable)",
  "createdById": "Long - ID of user who created the task",
  "createdByName": "String - Name of user who created the task",
  "createdAt": "Timestamp - When task was created",
  "updatedAt": "Timestamp - When task was last updated",
  "completedAt": "Timestamp - When task was marked done (nullable)"
}
```

---

## Important Notes for Frontend

1. **Task Creation:** Always defaults to Backlog column automatically (no need to specify)
2. **Drag & Drop:** Use the move endpoint (`PATCH /tasks/{taskId}/column/{columnId}`) to update task column
   - Get column IDs from: `GET /api/projects/{projectId}/columns`
3. **Validation:** Title is required; all other fields are optional
4. **Priority:** Defaults to MEDIUM if not specified
5. **Timestamps:** All timestamps are in ISO 8601 format (UTC)
6. **Dates:** Due dates should be sent in ISO 8601 format (YYYY-MM-DD or full timestamp)
7. **Assignee:** Can be null (unassigned tasks are allowed)
8. **Cannot Change:** Project and column must not be changed via update endpoint
   - To move to different project: create new task
   - To change column: use move endpoint
9. **Avatar Initials:** Use these to display assignee avatars on task cards

---

## Column Reference (for Move Task)

When calling the move endpoint, use these column names and typical IDs:

| Column | Typical ID | Position |
|--------|-----------|----------|
| BACKLOG | 1 | 1 |
| TO_DO | 2 | 2 |
| IN_PROGRESS | 3 | 3 |
| DONE | 4 | 4 |

*Note: Actual IDs depend on project; retrieve from GET /api/projects/{projectId}/columns endpoint*

