# Board Column API Endpoints

## Base URL
```
/api/projects/{projectId}/columns
```

## Overview
Board Column endpoints provide read-only access to Kanban board columns. Columns are automatically created when a project is created and cannot be manually created, updated, or deleted. Each project has exactly 4 columns that represent task status stages.

---

## Column Types

Every project automatically has these 4 columns:

| Column | Position | Name in Code | Description |
|--------|----------|--------------|-------------|
| **Backlog** | 1 | BACKLOG | Initial column, unstarted tasks |
| **To Do** | 2 | TO_DO | Ready to start |
| **In Progress** | 3 | IN_PROGRESS | Currently being worked on |
| **Done** | 4 | DONE | Completed tasks |

---

## 1. Get All Columns for Project

**Method:** `GET`  
**Endpoint:** `/api/projects/{projectId}/columns`  
**Description:** Retrieve all columns for a project (ordered by position)

**Path Parameters:**
- `projectId` (Long, required) - Project ID

**Request Headers:**
```
Authorization: Bearer {token} (required - must be workspace member)
```

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "name": "BACKLOG",
    "position": 1,
    "projectId": 1,
    "createdAt": "2025-01-10T10:30:00Z"
  },
  {
    "id": 2,
    "name": "TO_DO",
    "position": 2,
    "projectId": 1,
    "createdAt": "2025-01-10T10:30:00Z"
  },
  {
    "id": 3,
    "name": "IN_PROGRESS",
    "position": 3,
    "projectId": 1,
    "createdAt": "2025-01-10T10:30:00Z"
  },
  {
    "id": 4,
    "name": "DONE",
    "position": 4,
    "projectId": 1,
    "createdAt": "2025-01-10T10:30:00Z"
  }
]
```

**Response Details:**
- Always returns 4 columns in position order (1, 2, 3, 4)
- Column names are enum values (BACKLOG, TO_DO, IN_PROGRESS, DONE)
- Each column has a unique ID for referencing in other operations (e.g., moving tasks)

**Error Responses:**
- `401 Unauthorized` - Token missing or invalid
- `403 Forbidden` - User is not a member of the workspace
- `404 Not Found` - Project not found
- `500 Internal Server Error` - Server error

---

## BoardColumn Object Structure

```json
{
  "id": "Long - Unique column identifier (use for task operations)",
  "name": "String - Column name enum (BACKLOG, TO_DO, IN_PROGRESS, DONE)",
  "position": "Integer - Order on board (1=first, 4=last)",
  "projectId": "Long - Project this column belongs to",
  "createdAt": "Timestamp - When column was created (when project was created)"
}
```

---

## Important Notes for Frontend

1. **Read-Only:** Columns are automatically created and cannot be manually created, updated, or deleted
2. **Fixed Order:** Columns are always ordered by position (1-4), no custom ordering
3. **Fixed Names:** The 4 column names are always: BACKLOG, TO_DO, IN_PROGRESS, DONE
4. **Use Column IDs:** When moving tasks between columns, use the column `id` field (not the name)
5. **Auto-Created:** Columns are created automatically when a project is created
6. **Default Column:** New tasks are placed in the BACKLOG column by default
7. **Task Movement:** Use the Move Task endpoint to change a task's column

---

## Related Endpoints

### Get Tasks in a Project
- **Method:** `GET`
- **Endpoint:** `/api/projects/{projectId}/tasks`
- **Description:** Get all tasks in a project (can be filtered by column)
- **See:** Task API documentation

### Get Columns for Kanban Display
This endpoint is specifically for populating your Kanban board UI. Here's the typical usage:

```
1. User opens a project
2. Frontend calls: GET /api/projects/{projectId}/columns
3. Frontend receives 4 columns with their IDs
4. Frontend calls: GET /api/projects/{projectId}/tasks
5. Frontend renders tasks grouped by their columnId
```

### Move Task to Different Column
- **Method:** `PATCH`
- **Endpoint:** `/api/tasks/{taskId}/column/{columnId}`
- **Description:** Move a task to a different column
- **Path Parameters:** Use the column `id` from this endpoint
- **See:** Task API documentation

---

## Example Usage Flow

### Step 1: Create Project (auto-creates columns)
```
POST /api/workspaces/1/projects
{
  "name": "Website Redesign"
}
→ Returns: Project with ID 1
→ Auto-creates: 4 columns for project 1
```

### Step 2: Get Columns for Kanban Board
```
GET /api/projects/1/columns
→ Returns:
[
  { id: 1, name: "BACKLOG", position: 1, ... },
  { id: 2, name: "TO_DO", position: 2, ... },
  { id: 3, name: "IN_PROGRESS", position: 3, ... },
  { id: 4, name: "DONE", position: 4, ... }
]
```

### Step 3: Display Kanban Board
Create columns UI with headers: BACKLOG, TO_DO, IN_PROGRESS, DONE

### Step 4: Create Task (goes to Backlog)
```
POST /api/projects/1/tasks
{
  "title": "Design homepage"
}
→ Task automatically goes to Backlog column (id: 1)
```

### Step 5: Move Task via Drag & Drop
```
PATCH /api/tasks/5/column/2
→ Moves task 5 to TO_DO column (id: 2)
```

---

## Status Codes Summary
| Code | Meaning |
|------|---------|
| 200 | Success |
| 401 | Unauthorized |
| 403 | Forbidden (not workspace member) |
| 404 | Project not found |
| 500 | Server error |


