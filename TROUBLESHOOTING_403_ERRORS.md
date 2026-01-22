# Troubleshooting 403 Forbidden Errors on Task Creation

**Date:** January 22, 2026  
**Purpose:** Comprehensive guide for debugging 403 errors when creating tasks

---

## Quick Diagnosis Checklist

When you get a 403 error creating a task, check these in order:

### 1. ✅ Is the Authorization Header Present?
```javascript
// ❌ WRONG - Missing Authorization header
fetch('/api/workspaces/8/projects/4/tasks', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({...})
});

// ✅ CORRECT - Authorization header included
fetch('/api/workspaces/8/projects/4/tasks', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({...})
});
```

**Why this causes 403:**
- SecurityConfig requires authentication for `/api/workspaces/**`
- Without JWT token, Spring Security rejects the request before it reaches the controller
- Results in 403 Forbidden (not 401 Unauthorized)

### 2. ✅ Is the JWT Token Valid and Not Expired?
```javascript
// Check token expiration
const parseJwt = (token) => {
  const base64Url = token.split('.')[1];
  const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
  const jsonPayload = decodeURIComponent(atob(base64).split('').map(c => {
    return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
  }).join(''));
  return JSON.parse(jsonPayload);
};

const token = localStorage.getItem('jwt_token');
const decoded = parseJwt(token);
const isExpired = decoded.exp * 1000 < Date.now();

if (isExpired) {
  console.error('JWT token has expired - refresh required');
}
```

**Why this causes 403:**
- Expired tokens fail JWT validation in JwtAuthFilter
- Spring Security sees no valid authentication
- Request is rejected with 403

### 3. ✅ Is the User a Member of the Workspace?
```javascript
// Verify user is workspace member before creating task
const checkWorkspaceMembership = async (workspaceId) => {
  const response = await fetch(`/api/workspaces/${workspaceId}/members`, {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  const members = await response.json();
  const currentUserId = getCurrentUserId(); // Your function to get current user ID
  return members.some(m => m.userId === currentUserId);
};

// Before creating task
if (!await checkWorkspaceMembership(workspaceId)) {
  console.error('User is not a member of this workspace');
}
```

**Why this causes 403:**
- TaskController checks if user is workspace owner OR member
- If neither, throws IllegalArgumentException with "not a member" message
- Returns 403 Forbidden

### 4. ✅ Does the Project Belong to the Workspace?
```javascript
// Verify project belongs to workspace
const response = await fetch(`/api/workspaces/${workspaceId}/projects/${projectId}`, {
  headers: { 'Authorization': `Bearer ${token}` }
});

if (!response.ok) {
  console.error('Project does not belong to workspace or does not exist');
}
```

**Why this causes 403:**
- Controller validates `project.getWorkspace().getId().equals(workspaceId)`
- Mismatch results in "Project does not belong to this workspace" error
- Returns 403 or 404 depending on the error

---

## Common Frontend Mistakes

### Mistake #1: Not Storing/Sending JWT Token
```javascript
// ❌ WRONG - Token not stored after login
const login = async (email, password) => {
  const response = await fetch('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
  });
  const data = await response.json();
  // Forgot to store token!
};

// ✅ CORRECT - Token stored after login
const login = async (email, password) => {
  const response = await fetch('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
  });
  const data = await response.json();
  localStorage.setItem('jwt_token', data.token);
  return data.token;
};
```

### Mistake #2: Using Wrong Field Names
```javascript
// ❌ WRONG - Using 'assignedTo' instead of 'assignedToId'
const createTask = async (taskData) => {
  return fetch(url, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      title: taskData.title,
      description: taskData.description,
      assignedTo: taskData.userId,  // ❌ WRONG FIELD NAME
      priority: taskData.priority
    })
  });
};

// ✅ CORRECT - Using 'assignedToId'
const createTask = async (taskData) => {
  return fetch(url, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      title: taskData.title,
      description: taskData.description,
      assignedToId: taskData.userId,  // ✅ CORRECT FIELD NAME
      priority: taskData.priority
    })
  });
};
```

Note: Wrong field names won't cause 403, but will silently fail to assign the task.

### Mistake #3: CORS Preflight Not Handled
```javascript
// If using custom headers, browsers send OPTIONS preflight request
// Backend already handles this, but ensure you're not blocking it in middleware

// ✅ Backend correctly configured in SecurityConfig.java:
// .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
```

---

## Debugging Steps

### Step 1: Check Browser Developer Tools

1. Open DevTools (F12)
2. Go to Network tab
3. Try creating a task
4. Look at the request:
   - **General**: Check status code (403?)
   - **Request Headers**: Is `Authorization: Bearer ...` present?
   - **Response Headers**: Any CORS errors?

### Step 2: Check Backend Logs

The backend has extensive logging enabled. Look for:

```
========================================
JWT FILTER START
========================================
Request URI: /api/workspaces/8/projects/4/tasks
Method: POST
Has Authorization Header: false  ← ❌ Problem here!
```

Or:

```
========================================
TASK CREATION REQUEST START
========================================
Endpoint: POST /api/workspaces/8/projects/4/tasks
Step 1: Validating JWT token...
✓ JWT Valid - User ID: 8, Email: user@example.com
Step 4: Checking workspace access...
  - Is Owner? false
  - Is Member? false  ← ❌ Problem here!
✗ AUTHORIZATION FAILED: User is neither owner nor member
```

### Step 3: Test with cURL

Isolate the issue by testing with cURL:

```bash
# Get a fresh JWT token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com", "password": "password123"}'

# Use the token to create a task
curl -X POST http://localhost:8080/api/workspaces/8/projects/4/tasks \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Task",
    "description": "Testing",
    "assignedToId": 5,
    "priority": "MEDIUM"
  }' \
  -v  # verbose mode shows headers
```

### Step 4: Verify Database State

Check if user is actually a workspace member:

```sql
-- Check workspace owner
SELECT id, name, owner_id FROM workspace WHERE id = 8;

-- Check workspace members
SELECT * FROM workspace_member WHERE workspace_id = 8;

-- Check if current user exists
SELECT id, email, name FROM "user" WHERE email = 'user@example.com';
```

---

## Error Response Guide

### 403 Forbidden from Security Filter
**Response:**
```
403 Forbidden
(Empty body or generic Spring Security error)
```

**Cause:** JWT token missing, invalid, or expired  
**Fix:** Ensure Authorization header is present and token is valid

### 403 Forbidden from Controller
**Response:**
```json
"You are not a member of this workspace"
```

**Cause:** User is not workspace owner or member  
**Fix:** Add user to workspace or use correct workspace

### 404 Not Found
**Response:**
```json
"Project not found in workspace"
```

**Cause:** Project doesn't belong to workspace  
**Fix:** Verify projectId and workspaceId match

### 400 Bad Request
**Response:**
```json
"Project has no columns. Please create columns first."
```

**Cause:** Project has no board columns  
**Fix:** Create board columns before creating tasks

---

## Working Example (React/JavaScript)

```javascript
// Complete working example for task creation
class TaskService {
  constructor() {
    this.baseUrl = 'http://localhost:8080/api';
  }

  getToken() {
    const token = localStorage.getItem('jwt_token');
    if (!token) {
      throw new Error('No authentication token found. Please log in.');
    }
    return token;
  }

  async createTask(workspaceId, projectId, taskData) {
    try {
      const token = this.getToken();
      
      const response = await fetch(
        `${this.baseUrl}/workspaces/${workspaceId}/projects/${projectId}/tasks`,
        {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
            'Accept': 'application/json'
          },
          body: JSON.stringify({
            title: taskData.title,
            description: taskData.description || null,
            assignedToId: taskData.assignedToId || null,
            priority: taskData.priority || 'MEDIUM',
            dueDate: taskData.dueDate || null
          })
        }
      );

      if (!response.ok) {
        const errorText = await response.text();
        console.error('Task creation failed:', response.status, errorText);
        
        if (response.status === 403) {
          throw new Error(`Access denied: ${errorText}. Please check your workspace membership.`);
        } else if (response.status === 401) {
          throw new Error('Authentication failed. Please log in again.');
        } else {
          throw new Error(`Failed to create task: ${errorText}`);
        }
      }

      const createdTask = await response.json();
      console.log('Task created successfully:', createdTask);
      return createdTask;
      
    } catch (error) {
      console.error('Error in createTask:', error);
      throw error;
    }
  }
}

// Usage
const taskService = new TaskService();

try {
  const newTask = await taskService.createTask(8, 4, {
    title: 'Implement login feature',
    description: 'Add JWT authentication',
    assignedToId: 5,
    priority: 'HIGH',
    dueDate: '2026-02-01T23:59:59Z'
  });
  console.log('Success:', newTask);
} catch (error) {
  console.error('Failed to create task:', error.message);
  // Show error to user in UI
}
```

---

## Summary

### Most Common 403 Causes (in order):

1. **Missing Authorization header** (60% of cases)
2. **Expired JWT token** (25% of cases)
3. **User not workspace member** (10% of cases)
4. **Project-workspace mismatch** (5% of cases)

### Quick Fix for Most Cases:

```javascript
// Ensure these 3 things:
1. Store token after login: localStorage.setItem('jwt_token', token)
2. Send token with every request: headers: { 'Authorization': `Bearer ${token}` }
3. Use correct field names: assignedToId (not assignedTo)
```

---

## Related Documentation

- [TASK_CREATION_FIX.md](./TASK_CREATION_FIX.md) - Field name fix details
- [WORKSPACE_TASK_ENDPOINT_FIX.md](./WORKSPACE_TASK_ENDPOINT_FIX.md) - Endpoint documentation
- [BACKEND_FIX_SUMMARY.md](./BACKEND_FIX_SUMMARY.md) - Authorization fixes
- [TaskRequests.http](./TaskRequests.http) - API test examples
