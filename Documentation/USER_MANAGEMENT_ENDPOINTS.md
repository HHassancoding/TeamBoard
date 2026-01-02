# User Management API Endpoints

## Base URL
```
/api/users
```

## Overview
User management endpoints allow you to retrieve, create, update, and delete user accounts (mostly admin functions or user profile management).

---

## 1. Get All Users

**Method:** `GET`  
**Endpoint:** `/api/users`  
**Description:** Retrieve a list of all users in the system (admin only)

**Request Headers:**
```
Authorization: Bearer {token} (optional but recommended)
```

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "avatarInitials": "JD",
    "createdAt": "2025-01-10T10:30:00Z",
    "updatedAt": "2025-01-10T10:30:00Z"
  },
  {
    "id": 2,
    "name": "Jane Smith",
    "email": "jane@example.com",
    "avatarInitials": "JS",
    "createdAt": "2025-01-11T14:20:00Z",
    "updatedAt": "2025-01-11T14:20:00Z"
  }
]
```

---

## 2. Get Single User

**Method:** `GET`  
**Endpoint:** `/api/users/{id}`  
**Description:** Retrieve details of a specific user

**Path Parameters:**
- `id` (Long, required) - User ID

**Request Headers:**
```
Authorization: Bearer {token} (optional but recommended)
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "avatarInitials": "JD",
  "createdAt": "2025-01-10T10:30:00Z",
  "updatedAt": "2025-01-10T10:30:00Z"
}
```

**Error Responses:**
- `404 Not Found` - User with given ID doesn't exist

---

## 3. Create User

**Method:** `POST`  
**Endpoint:** `/api/users/create`  
**Description:** Create a new user account (alternative to registration endpoint)

**Request Body:**
```json
{
  "name": "string (required)",
  "email": "string (required, must be unique)",
  "password": "string (required)",
  "avatarInitials": "string (optional)"
}
```

**Response:** `200 OK`
```json
{
  "id": 3,
  "name": "Bob Johnson",
  "email": "bob@example.com",
  "avatarInitials": "BJ",
  "createdAt": "2025-01-12T09:15:00Z",
  "updatedAt": "2025-01-12T09:15:00Z"
}
```

**Error Responses:**
- `400 Bad Request` - Email already exists or validation failed
- `500 Internal Server Error` - Server error

---

## 4. Update User

**Method:** `PUT`  
**Endpoint:** `/api/users/update/{id}`  
**Description:** Update user details (name, email, avatar initials, password)

**Path Parameters:**
- `id` (Long, required) - User ID to update

**Request Body:**
```json
{
  "name": "string (optional)",
  "email": "string (optional, must be unique)",
  "password": "string (optional)",
  "avatarInitials": "string (optional)"
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "name": "John Doe Updated",
  "email": "john.updated@example.com",
  "avatarInitials": "JDU",
  "createdAt": "2025-01-10T10:30:00Z",
  "updatedAt": "2025-01-12T10:45:00Z"
}
```

**Error Responses:**
- `400 Bad Request` - Email already exists or validation failed
- `404 Not Found` - User not found
- `500 Internal Server Error` - Server error

---

## 5. Delete User

**Method:** `DELETE`  
**Endpoint:** `/api/users/delete/{id}`  
**Description:** Delete a user account and all associated data

**Path Parameters:**
- `id` (Long, required) - User ID to delete

**Request Headers:**
```
Authorization: Bearer {token} (optional but recommended)
```

**Response:** `204 No Content` (empty body on success)

**Error Responses:**
- `404 Not Found` - User not found
- `500 Internal Server Error` - Server error

---

## User Object Structure

```json
{
  "id": "Long - Unique user identifier",
  "name": "String - User's full name",
  "email": "String - User's email address (unique)",
  "avatarInitials": "String - User's avatar initials (e.g., 'JD')",
  "createdAt": "Timestamp - When user was created",
  "updatedAt": "Timestamp - When user was last updated"
}
```

---

## Important Notes for Frontend

1. **Authorization:** While these endpoints don't strictly require authentication, it's recommended to include JWT token for security
2. **Avatar Initials:** Usually derived from the user's name (first letter of first and last name)
3. **Email Uniqueness:** System prevents duplicate emails
4. **Password:** Passwords are hashed server-side (never send or display them)
5. **User IDs:** Use numeric IDs (Long) for all user references in other endpoints
6. **Admin Functions:** Endpoints like "Get All Users" and "Delete User" should typically be restricted to admin role

---

## Status Codes Summary
| Code | Meaning |
|------|---------|
| 200 | Success |
| 204 | Deleted successfully |
| 400 | Bad request (validation error) |
| 404 | User not found |
| 500 | Server error |


