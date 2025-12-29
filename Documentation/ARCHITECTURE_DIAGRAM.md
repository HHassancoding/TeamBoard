# WorkspaceMember - Architecture Diagram

## 🏗️ System Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          CLIENT REQUESTS                                    │
│                     (HTTP / REST API)                                       │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                    ┌───────────────┼───────────────┐
                    │               │               │
                    ▼               ▼               ▼
         ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
         │  POST            │  │  DELETE          │  │  GET             │
         │  /workspaces/    │  │  /workspaces/    │  │  /workspaces/    │
         │  {id}/members    │  │  {id}/members/   │  │  {id}/members    │
         │                  │  │  {userId}        │  │                  │
         │ Add Member       │  │ Remove Member    │  │ List Members     │
         └──────────────────┘  └──────────────────┘  └──────────────────┘
                    │               │                       │
                    └───────────────┼───────────────────────┘
                                    │
                    ┌───────────────▼───────────────┐
                    │   WORKSPACE CONTROLLER        │
                    │   (REST Endpoints)            │
                    │                               │
                    │  - JWT Authentication         │
                    │  - Owner Validation           │
                    │  - Request/Response DTO       │
                    │  - Error Handling             │
                    └───────────────┬───────────────┘
                                    │
                    ┌───────────────▼───────────────┐
                    │  WORKSPACE MEMBER SERVICE     │
                    │  (Business Logic)             │
                    │                               │
                    │  addMember()                  │
                    │  removeMember()               │
                    │  getMembersOfWorkspace()      │
                    │  getUserWorkspaces()          │
                    │  getMember()                  │
                    │  updateMemberRole()           │
                    │                               │
                    │  - User Validation            │
                    │  - Workspace Validation       │
                    │  - Duplicate Prevention       │
                    │  - Owner Protection           │
                    │  - Error Handling             │
                    └───────────────┬───────────────┘
                                    │
                    ┌───────────────▼───────────────┐
                    │ WORKSPACE MEMBER REPOSITORY   │
                    │ (Data Access)                 │
                    │                               │
                    │  findByWorkspaceId()          │
                    │  findByUserId()               │
                    │  findByUserIdAndWorkspaceId() │
                    │  save()                       │
                    │  deleteById()                 │
                    └───────────────┬───────────────┘
                                    │
                    ┌───────────────▼───────────────┐
                    │     DATABASE (PostgreSQL)     │
                    │                               │
                    │  workspace_members table      │
                    │  ┌───────────────────────┐    │
                    │  │ id: BIGSERIAL         │    │
                    │  │ workspace_id: BIGINT  │    │
                    │  │ user_id: BIGINT       │    │
                    │  │ role: VARCHAR(50)     │    │
                    │  │ joined_at: TIMESTAMP  │    │
                    │  │ updated_at: TIMESTAMP │    │
                    │  └───────────────────────┘    │
                    │  Constraints:                 │
                    │  - PK: id                     │
                    │  - FK: workspace_id,user_id   │
                    │  - UK: (workspace_id,user_id) │
                    │  - Indexes: workspace_id,     │
                    │    user_id                    │
                    └───────────────────────────────┘
```

## 📊 Data Model

```
User (1) ─────────────┐
                      │
                      │ (N) WorkspaceMember (N)
                      │
Workspace (1) ────────┘
```

### Relationships
```
User
  │
  ├─ (1:N) ─── WorkspaceMember
  │
  └─ (1:N) ─── Workspace (as owner)

Workspace
  │
  ├─ (1:N) ─── WorkspaceMember
  │
  └─ (1:N) ─── User (as owner)

WorkspaceMember
  │
  ├─ (N:1) ─── User
  │
  └─ (N:1) ─── Workspace
```

## 🔄 Request Flow Diagram

### Add Member Flow
```
Request: POST /api/workspaces/1/members
         { userId: 2, role: "MEMBER" }
         Authorization: Bearer JWT_TOKEN
         │
         ▼
    Extract JWT Token
         │
         ▼
    Find Current User from JWT
         │
         ▼
    Get Workspace (1)
         │
         ▼
    Check: Current User is Owner? ──NO──> 403 Forbidden
         │ YES
         ▼
    WorkspaceMemberService.addMember()
         │
         ▼
    Validate User Exists? ──NO──> 400 Bad Request
         │ YES
         ▼
    Validate Workspace Exists? ──NO──> 404 Not Found
         │ YES
         ▼
    Check Duplicate Member? ──YES──> 400 Bad Request
         │ NO
         ▼
    Create WorkspaceMember Entity
         │
         ▼
    Save to Database
         │
         ▼
    Convert to DTO
         │
         ▼
    Response: 201 Created
             { id, userId, userEmail, userName, role, joinedAt, updatedAt }
```

### Remove Member Flow
```
Request: DELETE /api/workspaces/1/members/2
         Authorization: Bearer JWT_TOKEN
         │
         ▼
    Extract JWT Token
         │
         ▼
    Find Current User from JWT
         │
         ▼
    Get Workspace (1)
         │
         ▼
    Check: Current User is Owner? ──NO──> 403 Forbidden
         │ YES
         ▼
    WorkspaceMemberService.removeMember()
         │
         ▼
    Find Member (userId=2, workspaceId=1)
         │
         ▼
    Member Exists? ──NO──> 400 Bad Request
         │ YES
         ▼
    Check: Is Member the Owner? ──YES──> 400 Bad Request
         │ NO
         ▼
    Delete Member from Database
         │
         ▼
    Response: 204 No Content
```

### List Members Flow
```
Request: GET /api/workspaces/1/members
         │
         ▼
    Get Workspace (1)
         │
         ▼
    Workspace Exists? ──NO──> 404 Not Found
         │ YES
         ▼
    Get All Members of Workspace (1)
         │
         ▼
    Convert Each to DTO
         │
         ▼
    Response: 200 OK
             [ { member1 }, { member2 }, ... ]
```

## 🔐 Security Flow

```
Client Request
    │
    ├─ JWT Token in Authorization Header
    │
    ▼
JwtUtil.extractUsername(token)
    │
    ▼
Find User by Email
    │
    ▼
Is User Found? ──NO──> 401 Unauthorized
    │ YES
    ▼
Is Token Valid? ──NO──> 403 Forbidden
    │ YES
    ▼
Get Workspace
    │
    ▼
Is User the Owner? ──NO──> 403 Forbidden
    │ YES
    ▼
Proceed with Operation
    │
    ▼
Response (201/204/200 or Error)
```

## 🧪 Test Coverage

```
WorkspaceMemberImpTests (14 tests)
│
├─ addMember() (5 tests)
│  ├─ Success Case
│  ├─ With Different Roles
│  ├─ User Not Found
│  ├─ Workspace Not Found
│  └─ Duplicate Member
│
├─ removeMember() (3 tests)
│  ├─ Success Case
│  ├─ Member Not Found
│  └─ Owner Protection
│
├─ getMembersOfWorkspace() (2 tests)
│  ├─ Success Case
│  └─ Workspace Not Found
│
├─ getUserWorkspaces() (2 tests)
│  ├─ Success Case
│  └─ User Not Found
│
├─ getMember() (2 tests)
│  ├─ Success Case
│  └─ Not Found Case
│
└─ updateMemberRole() (2 tests)
   ├─ Success Case
   └─ Not Found Case
```

## 📦 Dependency Injection

```
WorkspaceController
    │
    ├─ Inject: WorkspaceService
    ├─ Inject: WorkspaceMemberService ◄──── NEW
    ├─ Inject: JwtUtil
    └─ Inject: UserImp

WorkspaceMemberImp
    │
    ├─ Inject: WorkspaceMemberRepository
    ├─ Inject: WorkspaceService
    └─ Inject: UserService

WorkspaceImp
    │
    ├─ Inject: WorkspaceRepository
    └─ Inject: WorkspaceMemberService ◄──── MODIFIED
```

## 🗄️ Database Schema

```sql
CREATE TABLE workspace_members (
    ┌─────────────────────────────┐
    │ id: BIGSERIAL               │ ◄── Primary Key
    │ workspace_id: BIGINT        │ ◄── Foreign Key
    │ user_id: BIGINT             │ ◄── Foreign Key
    │ role: VARCHAR(50)           │ ◄── Enum: ADMIN, MEMBER, VIEWER
    │ joined_at: TIMESTAMP        │ ◄── Auto-set on creation
    │ updated_at: TIMESTAMP       │ ◄── Auto-updated
    └─────────────────────────────┘
    
    CONSTRAINT workspace_members_pkey
        PRIMARY KEY (id)
    
    CONSTRAINT fk_workspace_id
        FOREIGN KEY (workspace_id)
        REFERENCES workspaces(id)
        ON DELETE CASCADE
    
    CONSTRAINT fk_user_id
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
    
    CONSTRAINT uk_workspace_user
        UNIQUE (workspace_id, user_id)
    
    CREATE INDEX idx_workspace_members_workspace
        ON workspace_members(workspace_id)
    
    CREATE INDEX idx_workspace_members_user
        ON workspace_members(user_id)
);
```

## 🎯 Request/Response Examples

### 1. Add Member
```
POST /api/workspaces/1/members
Authorization: Bearer eyJhbGci...

REQUEST:
{
  "userId": 2,
  "role": "MEMBER"
}

RESPONSE (201 Created):
{
  "id": 1,
  "userId": 2,
  "userEmail": "member@example.com",
  "userName": "Member User",
  "role": "MEMBER",
  "joinedAt": "2025-12-28T10:30:00",
  "updatedAt": "2025-12-28T10:30:00"
}
```

### 2. Remove Member
```
DELETE /api/workspaces/1/members/2
Authorization: Bearer eyJhbGci...

RESPONSE (204 No Content):
[empty body]
```

### 3. List Members
```
GET /api/workspaces/1/members

RESPONSE (200 OK):
[
  {
    "id": 1,
    "userId": 1,
    "userEmail": "owner@example.com",
    "userName": "Owner",
    "role": "ADMIN",
    "joinedAt": "2025-12-28T10:00:00",
    "updatedAt": "2025-12-28T10:00:00"
  },
  {
    "id": 2,
    "userId": 2,
    "userEmail": "member@example.com",
    "userName": "Member",
    "role": "MEMBER",
    "joinedAt": "2025-12-28T10:30:00",
    "updatedAt": "2025-12-28T10:30:00"
  }
]
```

## 🔀 Processing Pipeline

```
API Request
    │
    ▼
┌─────────────────────────┐
│ WorkspaceController     │
│ (HTTP Layer)            │
│ - Parse Request         │
│ - Extract JWT           │
│ - Validate Authorization│
│ - Call Service          │
│ - Handle Response       │
└──────────┬──────────────┘
           │
           ▼
┌─────────────────────────┐
│ WorkspaceMemberService  │
│ (Business Logic)        │
│ - Validate Inputs       │
│ - Check Permissions     │
│ - Call Repository       │
│ - Return Entity         │
└──────────┬──────────────┘
           │
           ▼
┌─────────────────────────┐
│ WorkspaceMemberRepository
│ (Data Access)           │
│ - Query Database        │
│ - Save/Update/Delete    │
│ - Return Data           │
└──────────┬──────────────┘
           │
           ▼
┌─────────────────────────┐
│ PostgreSQL Database     │
│ (Data Store)            │
│ - Execute SQL           │
│ - Persist Data          │
│ - Return Results        │
└──────────┬──────────────┘
           │
           ▼
Response to Client
```

---

**This diagram shows the complete system architecture, data flow, and component interactions for the WorkspaceMember feature.**

