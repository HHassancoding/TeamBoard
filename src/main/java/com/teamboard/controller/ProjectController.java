package com.teamboard.controller;

import com.teamboard.DTO.ProjectCreateRequestDTO;
import com.teamboard.entity.Project;
import com.teamboard.entity.User;
import com.teamboard.entity.Workspace;
import com.teamboard.entity.WorkspaceMember;
import com.teamboard.service.ProjectService;
import com.teamboard.service.UserService;
import com.teamboard.service.WorkspaceMemberService;
import com.teamboard.service.WorkspaceService;
import com.teamboard.util.JwtUtil;
import org.apache.tomcat.util.http.parser.Authorization;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workspaces")
@CrossOrigin(origins = "*")
public class ProjectController {

  private final ProjectService projectService;
  private WorkspaceService workspaceService;
  private JwtUtil jwtUtil;
  private WorkspaceMemberService workspaceMemberService;
  private UserService userService;

  public ProjectController(WorkspaceService workspaceService, JwtUtil jwtUtil, WorkspaceMemberService workspaceMemberService, UserService userService, ProjectService projectService) {
    this.workspaceService = workspaceService;
    this.jwtUtil = jwtUtil;
    this.workspaceMemberService = workspaceMemberService;
    this.userService = userService;
    this.projectService = projectService;
  }


  @PostMapping("/workspace/{workspaceId}/project")
  public ResponseEntity<?> createProject(
      @PathVariable Long workspaceId,
      @RequestHeader("Authorization") String bearerToken,
      @RequestBody ProjectCreateRequestDTO projectRequestDTO
      ){
    try{
      String token = bearerToken.substring(7);
      String email = jwtUtil.extractUsername(token);
      User currentUser = userService.findByEmail(email);

      //verifies workspace exists
      Workspace workspace = workspaceService.getWorkspace(workspaceId);
      if(workspace == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Workspace not found");
      }
      WorkspaceMember member = workspaceMemberService.getMember(currentUser.getId(), workspace.getId());
      if(member == null) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body("Your not a member of this workspace");
      }
      Project project = new Project();
      project.setName(projectRequestDTO.getName());
      project.setDescription(projectRequestDTO.getDescription());
      project.setWorkspace(workspace);
      project.setCreatedBy(currentUser);

      Project createdProject = projectService.createProject(project);

    }
    // Implementation for creating a project
    return ResponseEntity.ok().body(null);
  }



}
