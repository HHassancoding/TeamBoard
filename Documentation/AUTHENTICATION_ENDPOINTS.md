# Authentication API Endpoints

## Base URL
```
/api/auth
```

## Overview
Authentication endpoints handle user registration, login, token refresh, and user profile retrieval.

---

## 1. User Registration

**Method:** `POST`  
**Endpoint:** `/api/auth/register`  
**Description:** Create a new user account

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
"User created successfully"
```

**Error Responses:**
- `400 Bad Request` - Invalid input, email already exists, or validation failed
  ```json
  "Registration failed: {error message}"
  ```

---

## 2. User Login

**Method:** `POST`  
**Endpoint:** `/api/auth/login`  
**Description:** Authenticate user and receive JWT token

**Request Body:**
```json
{
  "email": "string (required)",
  "password": "string (required)"
}
```

**Response:** `200 OK`
```json
{
  "token": "string (JWT token)",
  "refreshToken": "string (Refresh token)",
  "expiresIn": 3600,
  "tokenType": "Bearer"
}
```

**Error Responses:**
- `401 Unauthorized` - Invalid credentials
- `404 Not Found` - User not found

---

## 3. Refresh Token

**Method:** `POST`  
**Endpoint:** `/api/auth/refresh`  
**Description:** Generate a new access token using refresh token

**Request Headers:**
```
Authorization: Bearer {refresh_token}
```

**Response:** `200 OK`
```json
{
  "token": "string (new JWT token)",
  "refreshToken": "string (new refresh token)",
  "expiresIn": 3600,
  "tokenType": "Bearer"
}
```

**Error Responses:**
- `401 Unauthorized` - Invalid or expired refresh token
- `400 Bad Request` - Invalid authorization header format

---

## 4. Get Current User

**Method:** `POST`  
**Endpoint:** `/api/auth/me`  
**Description:** Retrieve current authenticated user details

**Request Headers:**
```
Authorization: Bearer {access_token}
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
- `401 Unauthorized` - Token missing or invalid
- `404 Not Found` - User not found

---

## Authentication Flow

### Typical Login Flow:
1. User submits email and password to `POST /api/auth/login`
2. Backend returns `token` (access token) and `refreshToken`
3. Frontend stores both tokens securely
4. Frontend includes `Authorization: Bearer {token}` in subsequent requests
5. When access token expires, use `POST /api/auth/refresh` with refresh token to get new access token
6. Use `POST /api/auth/me` to get current user details

### Token Expiration:
- Access tokens expire after `expiresIn` seconds (typically 3600 = 1 hour)
- Refresh tokens are valid for a longer period
- When access token expires, frontend should automatically call refresh endpoint

---

## Important Notes for Frontend

1. **Token Storage:** Store tokens securely (preferably in httpOnly cookies, not localStorage)
2. **Token Format:** Always include token as `Authorization: Bearer {token}` in request headers
3. **Token Refresh:** Implement automatic token refresh before expiration
4. **Error Handling:** 
   - 401 errors indicate token is invalid/expired - redirect to login
   - 400 errors indicate validation failures - show user-friendly message
5. **Security:** Never log tokens or send them in URLs
6. **CORS:** API allows requests from any origin (currently set to `*`)

---

## Status Codes Summary
| Code | Meaning |
|------|---------|
| 200 | Success |
| 400 | Bad request (validation error) |
| 401 | Unauthorized (invalid credentials or token) |
| 404 | User not found |
| 500 | Server error |


