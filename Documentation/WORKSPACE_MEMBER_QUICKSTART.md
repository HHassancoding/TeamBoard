# WorkspaceMember - Quick Start Guide

## What Was Implemented

The WorkspaceMember feature enables team collaboration by allowing workspace owners to add, remove, and manage team members with different roles (ADMIN, MEMBER, VIEWER).

### Key Features
✅ Add members to workspace  
✅ Remove members from workspace  
✅ List all workspace members  
✅ Automatic owner membership as ADMIN  
✅ Role-based access (ADMIN, MEMBER, VIEWER)  
✅ Owner protection (cannot be removed)  
✅ Full error handling and validation  

## File Structure

```
teamboard-backend/
├── src/main/
│   ├── java/com/teamboard/
│   │   ├── entity/
│   │   │   ├── WorkspaceMember.java      ← New entity
│   │   │   └── MemberRole.java           ← New enum
│   │   ├── repository/
│   │   │   └── WorkspaceMemberRepository.java  ← New repo
│   │   ├── service/
│   │   │   ├── WorkspaceMemberService.java     ← New interface
│   │   │   ├── WorkspaceMemberImp.java         ← New impl
│   │   │   └── WorkspaceImp.java              ← Modified
│   │   ├── controller/
│   │   │   └── WorkspaceController.java       ← Modified
│   │   └── DTO/
│   │       ├── WorkspaceMemberRequestDTO.java  ← New
│   │       └── WorkspaceMemberResponseDTO.java ← New
│   └── resources/db/migration/
│       └── V3__Create_workspace_members_table.sql  ← New
├── src/test/java/com/teamboard/
│   └── WorkspaceMemberImpTests.java           ← New tests
└── WorkspaceMemberRequests.http               ← New HTTP tests
```

## Quick Start

### 1. Compile the Project
```bash
mvn clean compile -DskipTests
```

### 2. Run Unit Tests
```bash
mvn test -Dtest=WorkspaceMemberImpTests
```

Expected: **14 tests pass**

### 3. Start the Application
```bash
mvn spring-boot:run
```

or

```bash
mvn clean package
java -jar target/teamboard-backend-*.jar
```

### 4. Test with HTTP Requests

Open `WorkspaceMemberRequests.http` in IntelliJ and:
1. Run the register requests to create test users
2. Run login to get JWT token
3. Update the `@token` variable with the token
4. Run the workspace creation request
5. Run member management requests

## API Endpoints

### Add Member
```
POST /api/workspaces/{id}/members
Authorization: Bearer <TOKEN>
Content-Type: application/json

{
  "userId": 2,
  "role": "MEMBER"
}
```

### List Members
```
GET /api/workspaces/{id}/members
```

### Remove Member
```
DELETE /api/workspaces/{id}/members/{userId}
Authorization: Bearer <TOKEN>
```

## Core Classes

### Entity
- **WorkspaceMember**: Represents a user's membership in a workspace with role

### Service
- **WorkspaceMemberService**: Interface defining member management operations
- **WorkspaceMemberImp**: Implementation with validation and error handling

### Repository
- **WorkspaceMemberRepository**: Database access with queries for workspace, user, and membership lookups

### Controller
- **WorkspaceController**: REST endpoints for member operations

## Database

**Table**: `workspace_members`

| Column | Type | Notes |
|--------|------|-------|
| id | BIGSERIAL | Primary key |
| workspace_id | BIGINT | FK to workspaces |
| user_id | BIGINT | FK to users |
| role | VARCHAR(50) | ADMIN, MEMBER, VIEWER |
| joined_at | TIMESTAMP | Auto-populated |
| updated_at | TIMESTAMP | Auto-updated |

**Constraints**:
- Unique (workspace_id, user_id) - prevents duplicate memberships
- Foreign keys with CASCADE delete

## Testing

### Unit Tests
File: `src/test/java/com/teamboard/WorkspaceMemberImpTests.java`

14 test methods covering:
- Adding members (success, errors, different roles)
- Removing members (success, protection, errors)
- Listing members (success, errors)
- Getting user workspaces
- Getting single member
- Updating member role

Run with:
```bash
mvn test -Dtest=WorkspaceMemberImpTests
```

### HTTP Tests
File: `WorkspaceMemberRequests.http`

Complete test scenarios:
- User registration
- Login
- Workspace creation
- Member management (add/list/remove)
- Error cases

## Workflow Example

```
1. Admin registers 3 users
   POST /api/auth/register

2. Admin logs in (gets JWT token)
   POST /api/auth/login

3. Admin creates workspace (owner auto-added as ADMIN)
   POST /api/workspaces

4. Admin adds team members
   POST /api/workspaces/{id}/members (repeat for each member)

5. Admin views team
   GET /api/workspaces/{id}/members

6. Admin removes member
   DELETE /api/workspaces/{id}/members/{userId}
```

## Roles Explained

- **ADMIN**: Full control, can manage members and workspace settings
- **MEMBER**: Can access and modify workspace content
- **VIEWER**: Read-only access to workspace

*Note: Role-based endpoint restrictions coming in future updates*

## Error Handling

All endpoints return meaningful error messages:

| Status | Scenario |
|--------|----------|
| 201 | Member added successfully |
| 204 | Member removed successfully |
| 400 | Invalid input or duplicate member |
| 403 | User not authorized (not owner) |
| 404 | Resource not found |
| 500 | Server error |

## Common Operations

### Add a member with MEMBER role
```bash
curl -X POST http://localhost:8080/api/workspaces/1/members \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"userId":2,"role":"MEMBER"}'
```

### List all members
```bash
curl http://localhost:8080/api/workspaces/1/members
```

### Remove a member
```bash
curl -X DELETE http://localhost:8080/api/workspaces/1/members/2 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## Troubleshooting

### Compilation Errors
```bash
mvn clean compile -q
# Check output for errors
```

### Tests Failing
```bash
mvn test -Dtest=WorkspaceMemberImpTests -v
# Check test output for specific failures
```

### Application Won't Start
1. Check if port 8080 is available
2. Check PostgreSQL is running
3. Check Flyway migrations are up to date
4. Check logs for error messages

### Member Operations Return 403
- Ensure you're logged in as workspace owner
- Check JWT token is valid
- Verify user ID matches the workspace owner

## Next Steps

After verifying this implementation:

1. **Integration Tests**: Add controller-level tests
2. **Member Invitations**: Add invite acceptance workflow
3. **Role Validation**: Implement role checks in other endpoints
4. **Pagination**: Add to member list endpoint
5. **Audit Logging**: Track member changes

## Documentation Files

- **WORKSPACE_MEMBER_IMPLEMENTATION.md**: Detailed implementation design
- **WORKSPACE_MEMBER_API.md**: Complete API reference
- **WORKSPACE_MEMBER_VERIFICATION.md**: Verification checklist
- **WORKSPACE_MEMBER_QUICKSTART.md**: This file

## Support

For detailed information:
1. Check implementation docs for design decisions
2. Review test cases for usage examples
3. Check HTTP test file for request examples
4. Review controller code for endpoint details

---

**Implementation Status**: ✅ Complete and Ready for Testing

