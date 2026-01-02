# Frontend Implementation Guide - What to Build

This guide outlines exactly what you need to build on the frontend based on the available API endpoints.

---

## 🎯 High-Level Architecture

```
Frontend App
├── Auth Pages
│   ├── Register
│   ├── Login
│   └── Profile
├── Dashboard/Navigation
│   ├── Workspace Selector
│   └── Main Navigation
├── Workspace Views
│   ├── Workspace Settings
│   ├── Members Management
│   └── Project List
├── Project Views
│   ├── Kanban Board
│   │   ├── Backlog Column
│   │   ├── To Do Column
│   │   ├── In Progress Column
│   │   └── Done Column
│   ├── Project Settings
│   └── Task List
└── Task Views
    ├── Task Detail Modal
    ├── Task Create Modal
    └── Task Edit Modal
```

---

## 📄 Pages/Screens to Build

### 1. Authentication (Pages)

#### Register Page
**Endpoint:** `POST /api/auth/register`
**Form Fields:**
- Name (text input)
- Email (email input)
- Password (password input)
- Confirm Password (password input)

**On Success:**
- Save credentials
- Redirect to login page

**On Error:**
- Show error message (email exists, validation failed, etc.)

---

#### Login Page
**Endpoint:** `POST /api/auth/login`
**Form Fields:**
- Email (email input)
- Password (password input)
- Remember Me (checkbox, optional)

**On Success:**
- Save access token to state/localStorage
- Save refresh token to secure storage
- Redirect to dashboard

**On Error:**
- Show error message (invalid credentials)

---

#### User Profile Page (Optional)
**Endpoints:**
- `POST /api/auth/me` - Get current user
- `PUT /api/users/update/{id}` - Update profile

**Editable Fields:**
- Name
- Email
- Avatar Initials
- Password (optional)

---

### 2. Dashboard/Home Page

#### Features:
- Show welcome message with current user name
- List all workspaces user has access to
- Quick action: "Create New Workspace"
- Switch between workspaces

**Endpoints:**
- `GET /api/auth/me` - Current user
- `GET /api/workspaces` - List workspaces
- `POST /api/workspaces` - Create workspace
- `GET /api/workspaces/owner/{ownerId}` - Filter by owner (optional)

---

### 3. Workspace Management

#### Workspace List View
**Show:**
- Workspace name
- Description
- Owner name
- Number of members
- Last updated date
- "Enter Workspace" button

**Endpoints:**
- `GET /api/workspaces` - List workspaces
- `GET /api/workspaces/{id}` - Get details

---

#### Workspace Detail Page
**Show:**
- Workspace name, description, owner
- Members list
- Projects list
- Settings (owner only)

**Editable (Owner Only):**
- Workspace name
- Description

**Endpoints:**
- `GET /api/workspaces/{id}` - Get workspace
- `PUT /api/workspaces/{id}` - Update workspace
- `DELETE /api/workspaces/{id}` - Delete workspace

---

#### Members Management Page
**Show Table:**
- User name
- User email
- Role (ADMIN/MEMBER/VIEWER)
- Joined date
- Delete button (owner only)

**Features:**
- Add member (modal with user ID/email + role dropdown)
- Remove member (with confirmation)
- Change member role (maybe in future version)

**Endpoints:**
- `GET /api/workspaces/{id}/members` - List members
- `POST /api/workspaces/{id}/members` - Add member
- `DELETE /api/workspaces/{id}/members/{userId}` - Remove member

---

### 4. Project Management

#### Project List View
**Show:**
- Project name
- Description
- Created by (name)
- Created date
- "View Board" button
- "Edit" button
- "Delete" button

**Endpoints:**
- `GET /api/workspaces/{id}/projects` - List projects
- `POST /api/workspaces/{id}/projects` - Create project (form modal)
- `PUT /api/workspaces/{id}/projects/{id}` - Update project
- `DELETE /api/workspaces/{id}/projects/{id}` - Delete project

---

### 5. Kanban Board (Main Feature!)

#### Board Layout
```
┌─────────────────────────────────────────────────────┐
│ Project: Website Redesign                 [Settings] │
├──────────┬──────────┬──────────┬──────────┐
│ BACKLOG  │ TO DO    │ IN PROG  │ DONE     │
├──────────┼──────────┼──────────┼──────────┤
│          │          │          │          │
│ [Task 1] │ [Task 2] │ [Task 3] │ [Task 4] │
│ [Task 5] │          │          │ [Task 6] │
│          │          │          │          │
└──────────┴──────────┴──────────┴──────────┘
```

#### Task Card Display
**Show:**
- Task title
- Description (first 100 chars)
- Assignee (avatar with initials)
- Priority badge (color: Low=Green, Medium=Yellow, High=Red)
- Due date (with color if overdue)
- Column indicator

**Interactions:**
- Click to open detail modal
- Drag to move between columns
- Delete button (with confirmation)

---

#### Kanban Board Workflow

**1. Load Board:**
```
1. GET /api/projects/{projectId}/columns
   → Get the 4 columns with their IDs
   
2. GET /api/projects/{projectId}/tasks
   → Get all tasks
   
3. Group tasks by their columnId
   
4. Render 4 columns with tasks
```

**2. Create Task:**
```
Click "Add Task" button
Show form modal with fields:
- Title (required)
- Description (optional)
- Assignee (dropdown, optional)
- Priority (dropdown: LOW/MEDIUM/HIGH)
- Due date (date picker, optional)

On Submit:
POST /api/projects/{projectId}/tasks

On Success:
- Add task to Backlog column
- Close modal
- Refresh task list
```

**3. Edit Task:**
```
Click on task card
Show modal with current values
Show edit form with same fields as create

On Submit:
PUT /api/tasks/{taskId}

On Success:
- Update task in UI
- Close modal
```

**4. Move Task (Drag & Drop):**
```
When user drags task to different column:

PATCH /api/tasks/{taskId}/column/{newColumnId}

On Success:
- Move task card to new column
- Update updatedAt timestamp
```

**5. Delete Task:**
```
Show delete button in task detail modal
On click:
Show confirmation dialog: "Delete this task?"

If confirmed:
DELETE /api/tasks/{taskId}

On Success:
- Remove task from UI
- Close modal
```

---

### 6. Task Management

#### Task Detail Modal
**Display:**
- Title
- Description
- Project name
- Current column
- Assignee (name + avatar)
- Priority (badge)
- Due date
- Created by
- Created at
- Updated at

**Editable Fields:**
- Title
- Description
- Assignee (dropdown)
- Priority (dropdown)
- Due date (date picker)

**Buttons:**
- Save (PUT request)
- Delete (DELETE request with confirmation)
- Close

**Endpoints:**
- `GET /api/tasks/{taskId}` - Get task
- `PUT /api/tasks/{taskId}` - Update task
- `DELETE /api/tasks/{taskId}` - Delete task
- `PATCH /api/tasks/{taskId}/column/{columnId}` - Move task

---

## 🎨 UI Component Checklist

### Reusable Components

- [ ] `Button` - Standard button with variants
- [ ] `Input` - Text input with validation
- [ ] `Select` - Dropdown with options
- [ ] `Modal` - Dialog with form/content
- [ ] `Card` - Container component
- [ ] `Avatar` - User avatar with initials
- [ ] `Badge` - Status/priority badge
- [ ] `Table` - Data table (for members list)
- [ ] `Navigation` - Top/sidebar navigation
- [ ] `Breadcrumb` - Navigation breadcrumb
- [ ] `Toast/Alert` - Success/error messages
- [ ] `Loading` - Loading spinner/skeleton
- [ ] `DatePicker` - Date selection
- [ ] `Dropdown/Menu` - User menu
- [ ] `TaskCard` - Kanban task card
- [ ] `Column` - Kanban column container

### Pages/Views

- [ ] RegisterPage
- [ ] LoginPage
- [ ] DashboardPage
- [ ] WorkspaceListPage
- [ ] WorkspaceDetailPage
- [ ] WorkspaceMembersPage
- [ ] ProjectListPage
- [ ] KanbanBoardPage
- [ ] TaskDetailModal
- [ ] TaskCreateModal
- [ ] ProjectSettingsPage
- [ ] UserProfilePage

---

## 🔄 State Management

### Global State (Redux/Zustand/Context)

**Auth:**
- currentUser
- accessToken
- refreshToken
- isAuthenticated
- loading

**Workspace:**
- selectedWorkspace
- workspaces (list)
- members (list)
- loading

**Project:**
- selectedProject
- projects (list)
- columns (list)
- tasks (list)
- loading

**UI:**
- modals (open/close states)
- notifications
- currentPage

---

## 🛠️ API Integration Pattern

### Example: Load Kanban Board
```javascript
// In component mount/useEffect

const loadBoard = async () => {
  try {
    // 1. Get columns
    const columns = await GET('/api/projects/{projectId}/columns', token);
    
    // 2. Get tasks
    const tasks = await GET('/api/projects/{projectId}/tasks', token);
    
    // 3. Store in state
    setColumns(columns);
    setTasks(tasks);
    
    // 4. Group tasks by column for rendering
    const tasksByColumn = {};
    columns.forEach(col => {
      tasksByColumn[col.id] = tasks.filter(t => t.columnId === col.id);
    });
    
    setTasksByColumn(tasksByColumn);
  } catch (error) {
    showError(error.message);
  }
};
```

---

## 📋 Feature Implementation Priority

### Phase 1 (MVP)
- [ ] Authentication (login/register)
- [ ] Workspace CRUD
- [ ] Project CRUD
- [ ] Kanban board (view)
- [ ] Task CRUD
- [ ] Drag & drop tasks

### Phase 2
- [ ] Workspace members management
- [ ] Role-based UI (show/hide based on role)
- [ ] User profile management
- [ ] Workspace switching

### Phase 3
- [ ] Task comments/activity
- [ ] Task labels
- [ ] Search/filter
- [ ] Notifications
- [ ] Dark mode

### Phase 4+
- [ ] Mobile app
- [ ] Real-time updates (WebSocket)
- [ ] Collaborative features
- [ ] Advanced analytics

---

## 🌐 HTTP Client Setup

### Headers Pattern
```javascript
const headers = {
  'Content-Type': 'application/json',
  'Authorization': `Bearer ${accessToken}`
};
```

### Request/Response Handling
```javascript
async function makeRequest(method, endpoint, data = null) {
  try {
    const response = await fetch(`${API_URL}${endpoint}`, {
      method,
      headers: getHeaders(),
      body: data ? JSON.stringify(data) : null
    });
    
    if (response.status === 401) {
      // Token expired, refresh or redirect to login
      await refreshToken();
      return makeRequest(method, endpoint, data); // Retry
    }
    
    if (!response.ok) {
      const error = await response.text();
      throw new Error(error);
    }
    
    return await response.json();
  } catch (error) {
    showError(error.message);
    throw error;
  }
}
```

---

## 🧪 Testing Checklist

- [ ] Register new user
- [ ] Login with credentials
- [ ] Create workspace
- [ ] Add member to workspace
- [ ] Create project (columns auto-created)
- [ ] View Kanban board with 4 columns
- [ ] Create task in Backlog
- [ ] Move task between columns
- [ ] Update task details
- [ ] Delete task
- [ ] Delete project
- [ ] Delete workspace
- [ ] Logout
- [ ] Token refresh works

---

## 📱 Responsive Design

### Mobile (< 768px)
- Single column layout
- Stack kanban columns vertically
- Drawer navigation
- Full-width modals

### Tablet (768px - 1024px)
- 2 columns
- Side-by-side panels
- Touch-friendly buttons

### Desktop (> 1024px)
- Full layout
- Horizontal kanban board
- Multi-column panels

---

## 🎨 Design System Recommendations

### Colors
- Primary: Brand color
- Success: Green (#10b981)
- Error: Red (#ef4444)
- Warning: Yellow (#f59e0b)
- Info: Blue (#3b82f6)

### Priority Badges
- LOW: Green background
- MEDIUM: Yellow background
- HIGH: Red background

### Role Colors
- ADMIN: Purple
- MEMBER: Blue
- VIEWER: Gray

---

## ⚡ Performance Tips

1. **Debounce** search/filter inputs
2. **Lazy load** project lists
3. **Cache** user and workspace data
4. **Virtualize** long task lists
5. **Optimize** drag & drop with react-beautiful-dnd
6. **Preload** images (avatars)

---

## 🔒 Security Considerations

1. **HTTPS only** in production
2. **httpOnly cookies** for tokens
3. **CSRF protection** for form submissions
4. **XSS prevention** - sanitize user input
5. **Rate limiting** - implement on frontend
6. **No sensitive data** in localStorage
7. **Validate input** before sending to API

---

## 📞 Common Issues & Solutions

### Issue: Drag & drop not working
**Solution:** 
- Install drag-drop library (react-beautiful-dnd)
- Ensure PATCH endpoint returns updated task
- Update UI optimistically before API call

### Issue: Token expired during use
**Solution:**
- Implement token refresh interceptor
- Check token expiration before each request
- Redirect to login on 401 response

### Issue: Kanban board shows empty columns
**Solution:**
- Verify column IDs match task's columnId
- Check task response includes columnId
- Log API response to debug

### Issue: Workspace members won't load
**Solution:**
- Ensure user is workspace member (has access)
- Check authorization header is correct
- Verify workspace ID is valid

---

## 🚀 Deployment Checklist

- [ ] Update API_URL to production endpoint
- [ ] Enable HTTPS
- [ ] Setup environment variables
- [ ] Configure CORS origin
- [ ] Test all endpoints in production
- [ ] Setup monitoring/logging
- [ ] Configure error tracking (Sentry)
- [ ] Performance testing
- [ ] Security audit
- [ ] Mobile responsiveness test

---

## 📚 Recommended Libraries

**State:** Redux, Zustand, or Context API  
**HTTP:** Axios or Fetch API with custom wrapper  
**Forms:** React Hook Form, Formik  
**UI:** Material-UI, Tailwind CSS, shadcn/ui  
**Drag & Drop:** react-beautiful-dnd, dnd-kit  
**Date Picker:** React DatePicker, Day.js  
**Notifications:** React-Toastify, Notistack  
**Testing:** Jest, React Testing Library  
**Build:** Vite, Create React App, Next.js

---

## 🎯 Success Criteria

Your frontend is ready when:
- [ ] All authentication flows work
- [ ] Kanban board displays all 4 columns
- [ ] Users can create/edit/delete tasks
- [ ] Drag & drop between columns works
- [ ] Workspace member management works
- [ ] Role-based UI works correctly
- [ ] All API error messages display
- [ ] Mobile responsive
- [ ] Token refresh works automatically

---

**Last Updated:** January 2025  
**API Version:** 1.0


