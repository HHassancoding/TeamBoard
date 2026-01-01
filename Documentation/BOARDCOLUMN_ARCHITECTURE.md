# BoardColumn Architecture Diagram

## System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                     Client (Frontend)                            │
│                  React/Vue Application                           │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             │ HTTP/REST API
                             │
┌────────────────────────────▼────────────────────────────────────┐
│                   BoardColumnController                          │
│  GET /api/projects/{projectId}/columns                          │
│  - JWT Authentication                                            │
│  - Workspace membership validation                               │
│  - Returns: List<BoardColumnResponseDTO>                        │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                    BoardColumnService                            │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ createDefaultColumns(projectId)                            │ │
│  │  - Called automatically when project created              │ │
│  │  - Creates 4 columns: BACKLOG, TO_DO, IN_PROGRESS, DONE   │ │
│  │                                                             │ │
│  │ getColumnsByProjectId(projectId)                           │ │
│  │  - Returns columns ordered by position                    │ │
│  │                                                             │ │
│  │ getColumnById(columnId)                                    │ │
│  │  - Fetches single column by ID                            │ │
│  └────────────────────────────────────────────────────────────┘ │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│              BoardColumnRepository                               │
│  extends JpaRepository<BoardColumn, Long>                       │
│                                                                  │
│  Custom Query:                                                  │
│  - findByProjectIdOrderByPosition(projectId)                    │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                 PostgreSQL Database                              │
│                                                                  │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │           board_columns Table                             │ │
│  ├───────────────────────────────────────────────────────────┤ │
│  │ id (BIGSERIAL PK)                                         │ │
│  │ name (VARCHAR - ENUM: BACKLOG,TO_DO,IN_PROGRESS,DONE)   │ │
│  │ position (INTEGER - 1,2,3,4)                             │ │
│  │ project_id (BIGINT FK → projects.id)                     │ │
│  │ created_at (TIMESTAMP DEFAULT CURRENT_TIMESTAMP)         │ │
│  │                                                            │ │
│  │ UNIQUE(project_id, position)                             │ │
│  │ INDEX: idx_board_columns_project                         │ │
│  └───────────────────────────────────────────────────────────┘ │
│                                                                  │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │           projects Table                                  │ │
│  ├───────────────────────────────────────────────────────────┤ │
│  │ id (BIGSERIAL PK)                                         │ │
│  │ name, description, workspace_id, created_by, ...         │ │
│  └───────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

## Entity Relationships

```
┌──────────────┐
│  Workspace   │
└──────┬───────┘
       │
       │ (1:N)
       │
       ▼
┌──────────────┐
│  Project     │
└──────┬───────┘
       │
       │ (1:N)
       │
       ▼
┌──────────────┐
│ BoardColumn  │  ◄─ NEW ENTITY
│ - BACKLOG    │
│ - TO_DO      │
│ - IN_PROGRESS│
│ - DONE       │
└──────┬───────┘
       │
       │ (1:N) [Future]
       │
       ▼
┌──────────────┐
│   Task       │  ◄─ FUTURE ENTITY
└──────────────┘
```

## Project Creation Flow

```
1. User Creates Project
   └─ POST /api/workspaces/{workspaceId}/projects
      
2. ProjectController receives request
   ├─ Validates JWT token
   ├─ Validates workspace access
   └─ Calls ProjectService.createProject(project)
   
3. ProjectImp.createProject(project)
   ├─ Validates workspace exists
   ├─ Saves project to database
   │  └─ Project gets id = 123
   │
   ├─ ✨ Calls boardColumnService.createDefaultColumns(123)
   │  │
   │  └─ BoardColumnImp.createDefaultColumns(123)
   │     ├─ Validates project exists
   │     │
   │     ├─ Creates BoardColumn 1
   │     │  ├─ name: BACKLOG
   │     │  ├─ position: 1
   │     │  └─ project_id: 123
   │     │
   │     ├─ Creates BoardColumn 2
   │     │  ├─ name: TO_DO
   │     │  ├─ position: 2
   │     │  └─ project_id: 123
   │     │
   │     ├─ Creates BoardColumn 3
   │     │  ├─ name: IN_PROGRESS
   │     │  ├─ position: 3
   │     │  └─ project_id: 123
   │     │
   │     └─ Creates BoardColumn 4
   │        ├─ name: DONE
   │        ├─ position: 4
   │        └─ project_id: 123
   │
   └─ Returns Project with id 123

4. Frontend receives Project response
   ├─ Displays project details
   │
   └─ Fetches columns via:
      GET /api/projects/123/columns
      
5. BoardColumnController.getColumns(123)
   ├─ Validates JWT token
   ├─ Validates workspace access
   └─ Calls BoardColumnService.getColumnsByProjectId(123)
   
6. Returns List of 4 BoardColumns ordered by position
   ├─ [1] BACKLOG
   ├─ [2] TO_DO
   ├─ [3] IN_PROGRESS
   └─ [4] DONE

7. Frontend renders Kanban board
   ├─ Column: BACKLOG
   ├─ Column: TO_DO
   ├─ Column: IN_PROGRESS
   └─ Column: DONE
```

## Transaction Safety

```
ProjectImp.createProject() [TRANSACTIONAL]
│
├─ BEGIN TRANSACTION
│
├─ projectRepo.save(project)
│  └─ ✅ Project saved
│
├─ boardColumnService.createDefaultColumns(projectId) [TRANSACTIONAL]
│  ├─ boardColumnRepository.save(column1)
│  ├─ boardColumnRepository.save(column2)
│  ├─ boardColumnRepository.save(column3)
│  └─ boardColumnRepository.save(column4)
│
├─ ✅ COMMIT (all or nothing)
│  OR
├─ ❌ ROLLBACK (if any error occurs)
│
└─ END TRANSACTION
```

## API Response Format

```
GET /api/projects/1/columns
Authorization: Bearer {valid_jwt_token}

RESPONSE: 200 OK
[
  {
    "id": 1,
    "name": "BACKLOG",
    "position": 1,
    "projectId": 1,
    "createdAt": "2026-01-01T14:00:00Z"
  },
  {
    "id": 2,
    "name": "TO_DO",
    "position": 2,
    "projectId": 1,
    "createdAt": "2026-01-01T14:00:00Z"
  },
  {
    "id": 3,
    "name": "IN_PROGRESS",
    "position": 3,
    "projectId": 1,
    "createdAt": "2026-01-01T14:00:00Z"
  },
  {
    "id": 4,
    "name": "DONE",
    "position": 4,
    "projectId": 1,
    "createdAt": "2026-01-01T14:00:00Z"
  }
]
```

## Error Scenarios

```
1. Project Not Found
   POST /api/projects/999/columns
   Response: 400 Bad Request
   Error: "Project not found with id: 999"

2. User Not Authenticated
   GET /api/projects/1/columns
   (No Authorization header)
   Response: 403 Forbidden
   Error: "Invalid authorization header"

3. User Not a Workspace Member
   GET /api/projects/1/columns
   (Valid JWT but user not in project's workspace)
   Response: 403 Forbidden
   Error: "You are not a member of this workspace"

4. Internal Server Error
   GET /api/projects/1/columns
   (Database connection error, etc.)
   Response: 500 Internal Server Error
   Error: "An error occurred while fetching columns: ..."
```

## Data Consistency Rules

```
1. Every Project MUST have exactly 4 columns
   ├─ Created automatically on project creation
   └─ Never manually created/deleted/modified

2. Column positions MUST be unique per project
   └─ UNIQUE(project_id, position) constraint

3. Column names MUST be one of the enum values
   ├─ BACKLOG (position 1)
   ├─ TO_DO (position 2)
   ├─ IN_PROGRESS (position 3)
   └─ DONE (position 4)

4. Deleting a project cascades delete to all columns
   └─ ON DELETE CASCADE enforced in FK
```

## Deployment Timeline

```
Day 1: Development & Testing
  ├─ Implement BoardColumn classes ✅
  ├─ Write unit tests ✅
  ├─ Local testing on H2 database ✅
  └─ Build & verify compilation ✅

Day 2: Deployment to Render
  ├─ Commit code to master branch
  ├─ CI/CD Pipeline triggers
  ├─ Application builds
  ├─ Flyway migration V5 runs
  │  └─ Creates board_columns table on PostgreSQL
  ├─ Application starts
  ├─ ✅ BoardColumn feature live
  │
  └─ First project created
     └─ 4 columns auto-created in PostgreSQL

Day 3+: Monitor & Extend
  ├─ Verify columns appear for new projects
  ├─ Monitor database logs
  └─ Plan Task entity next
```

