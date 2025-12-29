# WorkspaceMember API Quick Reference

## Member Management Endpoints

### 1. Add Member to Workspace
```
POST /api/workspaces/{id}/members
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

Request Body:
{
  "userId": 2,
  "role": "MEMBER"  // ADMIN, MEMBER, or VIEWER
}

Response (201 Created):
{
  "id": 1,
  "userId": 2,
  "userEmail": "member@example.com",
  "userName": "Member User",
  "role": "MEMBER",
  "joinedAt": "2025-12-28T10:30:00",
  "updatedAt": "2025-12-28T10:30:00"
}

Error Responses:
- 400 Bad Request: Invalid user ID, role, or user already member
- 403 Forbidden: User is not workspace owner
- 404 Not Found: Workspace or user not found
- 500 Internal Server Error: Server error
```

### 2. Remove Member from Workspace
```
DELETE /api/workspaces/{id}/members/{userId}
Authorization: Bearer <JWT_TOKEN>

Response (204 No Content):
[empty body]

Error Responses:
- 400 Bad Request: Member not found, cannot remove owner
- 403 Forbidden: User is not workspace owner
- 404 Not Found: Workspace not found
- 500 Internal Server Error: Server error
```

### 3. List All Members of Workspace
```
GET /api/workspaces/{id}/members

Response (200 OK):
[
  {
    "id": 1,
    "userId": 1,
    "userEmail": "owner@example.com",
    "userName": "Owner User",
    "role": "ADMIN",
    "joinedAt": "2025-12-28T10:00:00",
    "updatedAt": "2025-12-28T10:00:00"
  },
  {
    "id": 2,
    "userId": 2,
    "userEmail": "member@example.com",
    "userName": "Member User",
    "role": "MEMBER",
    "joinedAt": "2025-12-28T10:30:00",
    "updatedAt": "2025-12-28T10:30:00"
  }
]

Error Responses:
- 404 Not Found: Workspace not found
- 500 Internal Server Error: Server error
```

## Roles and Permissions

### Role Types
- **ADMIN**: Full control over workspace and members
- **MEMBER**: Can access and modify workspace content
- **VIEWER**: Read-only access to workspace

### Current Permission Model
- Only workspace **owner** can add/remove members
- Owner cannot be removed from members list
- Members are automatically added when workspace is created (owner as ADMIN)

## Workflow Example

```
1. User registers → Create account
   POST /api/auth/register

2. User logs in → Get JWT token
   POST /api/auth/login

3. User creates workspace → Auto-becomes ADMIN member
   POST /api/workspaces
   (Owner automatically added as ADMIN)

4. Owner adds team members
   POST /api/workspaces/{id}/members
   (Multiple times for each member)

5. Owner views all members
   GET /api/workspaces/{id}/members

6. Owner can remove members
   DELETE /api/workspaces/{id}/members/{userId}
   (Cannot remove self/owner)
```

## Testing Notes

### Using WorkspaceMemberRequests.http
The HTTP request file includes:
- User registration for 3 users
- Login to get JWT token
- Workspace creation
- Adding members with different roles
- Listing members
- Removing members
- Error case testing

### Using WorkspaceMemberImpTests.java
Run with:
```bash
mvn test -Dtest=WorkspaceMemberImpTests
```

Tests cover:
- ✅ Add member (success, various roles)
- ✅ Add member (failures: user not found, duplicate, workspace not found)
- ✅ Remove member (success, not found, owner protection)
- ✅ List members (success, workspace not found)
- ✅ Get user workspaces (success, user not found)
- ✅ Get single member (success, not found)
- ✅ Update member role (success, not found)

## Integration Notes

### Database
- Auto-migrates to v3 on startup (Flyway)
- Creates workspace_members table with proper indexes
- Cascading deletes when workspace or user is deleted

### JWT Authentication
All member modification endpoints require:
- Valid JWT token in Authorization header
- Token must belong to workspace owner
- Token claims are extracted to verify ownership

### Error Handling
- Validation errors return 400 Bad Request with message
- Permission errors return 403 Forbidden with message
- Not found errors return 404 Not Found with message
- All errors include descriptive messages

## Future Enhancements

### Planned Features
1. Member invitation system (pending acceptance)
2. Member role update endpoint (PATCH)
3. Member search and filter
4. Bulk member import/export
5. Activity logging for member changes
6. Member activity tracking
7. Notification on member addition
8. Permission-based endpoints (role checking)

### Known Limitations
1. No support for member invitations yet
2. No role-based endpoint access control yet
3. No pagination on member list
4. No search/filter on member list
5. Workspace owner can only be changed by system admin (not implemented)

