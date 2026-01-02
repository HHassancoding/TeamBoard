# Workspace API Endpoints

## Base URL
```
/api/workspaces
```

## Overview
Workspace endpoints allow users to create, manage, and collaborate in workspaces. Workspaces are containers for projects and team members. Only the workspace owner can manage workspace settings and members.

---

## 1. Create Workspace

**Method:** `POST`  
**Endpoint:** `/api/workspaces`  
**Description:** Create a new workspace (current user becomes owner)

**Request Headers:**
```
Authorization: Bearer {token} (required)
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
  "name": "Marketing Team",
  "description": "Marketing projects and tasks",
  "ownerId": 1,
  "ownerName": "John Doe",
  "ownerEmail": "john@example.com",
  "createdAt": "2025-01-10T10:30:00Z",
  "updatedAt": "2025-01-10T10:30:00Z"
}
```

**Validation Rules:**
- Name is required and cannot be empty
- Name must be unique per owner (same user can't have two workspaces with same name)
- Max name length: 255 characters

**Error Responses:**
- `400 Bad Request` - Workspace name is required or already exists
  ```json
  "Workspace with this name already exists"
  ```
- `401 Unauthorized` - Token missing or invalid
- `500 Internal Server Error` - Server error

---

## 2. Get All Workspaces

**Method:** `GET`  
**Endpoint:** `/api/workspaces`  
**Description:** Retrieve all workspaces (currently returns all, later will filter by membership)

**Request Headers:**
```
Authorization: Bearer {token} (optional)
```

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "name": "Marketing Team",
    "description": "Marketing projects and tasks",
    "ownerId": 1,
    "ownerName": "John Doe",
    "ownerEmail": "john@example.com",
    "createdAt": "2025-01-10T10:30:00Z",
    "updatedAt": "2025-01-10T10:30:00Z"
  },
  {
    "id": 2,
    "name": "Engineering Team",
    "description": "Backend and frontend development",
    "ownerId": 2,
    "ownerName": "Jane Smith",
    "ownerEmail": "jane@example.com",
    "createdAt": "2025-01-11T14:20:00Z",
    "updatedAt": "2025-01-11T14:20:00Z"
  }
]
```

---

## 3. Get Single Workspace

**Method:** `GET`  
**Endpoint:** `/api/workspaces/{id}`  
**Description:** Retrieve details of a specific workspace

**Path Parameters:**
- `id` (Long, required) - Workspace ID

**Request Headers:**
```
Authorization: Bearer {token} (optional)
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "name": "Marketing Team",
  "description": "Marketing projects and tasks",
  "ownerId": 1,
  "ownerName": "John Doe",
  "ownerEmail": "john@example.com",
  "createdAt": "2025-01-10T10:30:00Z",
  "updatedAt": "2025-01-10T10:30:00Z"
}
```

**Error Responses:**
- `404 Not Found` - Workspace not found

---

## 4. Update Workspace

**Method:** `PUT`  
**Endpoint:** `/api/workspaces/{id}`  
**Description:** Update workspace details (owner-only)

**Path Parameters:**
- `id` (Long, required) - Workspace ID

**Request Headers:**
```
Authorization: Bearer {token} (required)
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
  "name": "Marketing Team Updated",
  "description": "Updated marketing projects",
  "ownerId": 1,
  "ownerName": "John Doe",
  "ownerEmail": "john@example.com",
  "createdAt": "2025-01-10T10:30:00Z",
  "updatedAt": "2025-01-12T11:00:00Z"
}
```

**Authorization:**
- Only the workspace owner can update the workspace
- All fields are required in the request body

**Error Responses:**
- `400 Bad Request` - Workspace name is required
- `401 Unauthorized` - Token missing or invalid
- `403 Forbidden` - Only workspace owner can update it
- `404 Not Found` - Workspace not found
- `500 Internal Server Error` - Server error

---

## 5. Delete Workspace

**Method:** `DELETE`  
**Endpoint:** `/api/workspaces/{id}`  
**Description:** Delete a workspace and all its contents (owner-only)

**Path Parameters:**
- `id` (Long, required) - Workspace ID

**Request Headers:**
```
Authorization: Bearer {token} (required)
```

**Response:** `204 No Content` (empty body)

**Authorization:**
- Only the workspace owner can delete the workspace
- Deleting a workspace will delete all projects, tasks, and members associated with it

**Error Responses:**
- `401 Unauthorized` - Token missing or invalid
- `403 Forbidden` - Only workspace owner can delete it
- `404 Not Found` - Workspace not found
- `500 Internal Server Error` - Server error

---

## 6. Get Workspaces by Owner

**Method:** `GET`  
**Endpoint:** `/api/workspaces/owner/{ownerId}`  
**Description:** Get all workspaces owned by a specific user (admin/debugging)

**Path Parameters:**
- `ownerId` (Long, required) - Owner's user ID

**Request Headers:**
```
Authorization: Bearer {token} (optional)
```

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "name": "Marketing Team",
    "description": "Marketing projects and tasks",
    "ownerId": 1,
    "ownerName": "John Doe",
    "ownerEmail": "john@example.com",
    "createdAt": "2025-01-10T10:30:00Z",
    "updatedAt": "2025-01-10T10:30:00Z"
  },
  {
    "id": 3,
    "name": "Sales Team",
    "description": "Sales campaigns and leads",
    "ownerId": 1,
    "ownerName": "John Doe",
    "ownerEmail": "john@example.com",
    "createdAt": "2025-01-12T09:15:00Z",
    "updatedAt": "2025-01-12T09:15:00Z"
  }
]
```

---

## 7. Add Member to Workspace

**Method:** `POST`  
**Endpoint:** `/api/workspaces/{workspaceId}/members`  
**Description:** Add a user to a workspace with a specific role (owner-only)

**Path Parameters:**
- `workspaceId` (Long, required) - Workspace ID

**Request Headers:**
```
Authorization: Bearer {token} (required - must be workspace owner)
```

**Request Body:**
```json
{
  "userId": 2,
  "role": "MEMBER"
}
```

**Roles:** `ADMIN`, `MEMBER`, `VIEWER`
- `ADMIN` - Full access (can manage projects, tasks, and members)
- `MEMBER` - Can create and edit projects/tasks
- `VIEWER` - Read-only access

**Response:** `201 Created`
```json
{
  "id": 1,
  "userId": 2,
  "userEmail": "jane@example.com",
  "userName": "Jane Smith",
  "role": "MEMBER",
  "joinedAt": "2025-01-12T10:30:00Z",
  "updatedAt": "2025-01-12T10:30:00Z"
}
```

**Validation Rules:**
- User ID is required
- Role defaults to MEMBER if not specified
- User cannot be added twice to same workspace
- User must exist in the system

**Error Responses:**
- `400 Bad Request` - User ID is required or invalid role
  ```json
  "Invalid role. Must be one of: ADMIN, MEMBER, VIEWER"
  ```
- `401 Unauthorized` - Token missing or invalid
- `403 Forbidden` - Only workspace owner can add members
- `404 Not Found` - Workspace or user not found
- `409 Conflict` - User already a member of this workspace
- `500 Internal Server Error` - Server error

---

## 8. Remove Member from Workspace

**Method:** `DELETE`  
**Endpoint:** `/api/workspaces/{workspaceId}/members/{userId}`  
**Description:** Remove a user from a workspace (owner-only)

**Path Parameters:**
- `workspaceId` (Long, required) - Workspace ID
- `userId` (Long, required) - User ID to remove

**Request Headers:**
```
Authorization: Bearer {token} (required - must be workspace owner)
```

**Response:** `204 No Content` (empty body)

**Authorization:**
- Only the workspace owner can remove members
- Cannot remove the workspace owner themselves

**Error Responses:**
- `401 Unauthorized` - Token missing or invalid
- `403 Forbidden` - Only workspace owner can remove members
- `404 Not Found` - Workspace or user not found
- `500 Internal Server Error` - Server error

---

## 9. List Workspace Members

**Method:** `GET`  
**Endpoint:** `/api/workspaces/{workspaceId}/members`  
**Description:** Get all members of a workspace (public, no auth required but recommended)

**Path Parameters:**
- `workspaceId` (Long, required) - Workspace ID

**Request Headers:**
```
Authorization: Bearer {token} (optional but recommended)
```

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "userId": 1,
    "userEmail": "john@example.com",
    "userName": "John Doe",
    "role": "ADMIN",
    "joinedAt": "2025-01-10T10:30:00Z",
    "updatedAt": "2025-01-10T10:30:00Z"
  },
  {
    "id": 2,
    "userId": 2,
    "userEmail": "jane@example.com",
    "userName": "Jane Smith",
    "role": "MEMBER",
    "joinedAt": "2025-01-12T10:30:00Z",
    "updatedAt": "2025-01-12T10:30:00Z"
  },
  {
    "id": 3,
    "userId": 3,
    "userEmail": "bob@example.com",
    "userName": "Bob Johnson",
    "role": "VIEWER",
    "joinedAt": "2025-01-13T14:15:00Z",
    "updatedAt": "2025-01-13T14:15:00Z"
  }
]
```

**Error Responses:**
- `404 Not Found` - Workspace not found
- `500 Internal Server Error` - Server error

---

## Workspace Object Structure

```json
{
  "id": "Long - Unique workspace identifier",
  "name": "String - Workspace name",
  "description": "String - Optional workspace description",
  "ownerId": "Long - ID of the workspace owner",
  "ownerName": "String - Name of the workspace owner",
  "ownerEmail": "String - Email of the workspace owner",
  "createdAt": "Timestamp - When workspace was created",
  "updatedAt": "Timestamp - When workspace was last updated"
}
```

## WorkspaceMember Object Structure

```json
{
  "id": "Long - Unique membership identifier",
  "userId": "Long - ID of the member",
  "userEmail": "String - Email of the member",
  "userName": "String - Name of the member",
  "role": "String - Role (ADMIN, MEMBER, VIEWER)",
  "joinedAt": "Timestamp - When member joined workspace",
  "updatedAt": "Timestamp - When membership was last updated"
}
```

---

## Important Notes for Frontend

1. **Workspace Ownership:** Only the owner can create, update, or delete workspaces and manage members
2. **Role-Based Access:** Different roles have different permissions:
   - ADMIN: Full control
   - MEMBER: Can create/edit content
   - VIEWER: Read-only
3. **Uniqueness:** Workspace names must be unique per owner (same user can't have duplicates)
4. **Timestamps:** All timestamps are in ISO 8601 format (UTC)
5. **Member Management:** Always check current user's role before allowing management actions
6. **Member List:** Public endpoint, but consider adding authorization checks based on workspace membership

---

## Status Codes Summary
| Code | Meaning |
|------|---------|
| 201 | Created successfully |
| 200 | Success |
| 204 | Deleted successfully |
| 400 | Bad request (validation error) |
| 401 | Unauthorized |
| 403 | Forbidden (not owner) |
| 404 | Workspace or user not found |
| 409 | Conflict (duplicate member) |
| 500 | Server error |


