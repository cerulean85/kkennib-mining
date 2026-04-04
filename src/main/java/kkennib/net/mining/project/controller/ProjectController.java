package kkennib.net.mining.project.controller;

import kkennib.net.mining.dto.ServiceResponse;
import kkennib.net.mining.project.entity.Project;
import kkennib.net.mining.project.service.ProjectService;
import kkennib.net.mining.textmining.dto.ProjectPaginationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/project")
@RequiredArgsConstructor
public class ProjectController {
  private final ProjectService projectService;

  @GetMapping("/list")
  public ServiceResponse<ProjectPaginationResponse> getProjectList(
          @RequestParam("memId") Long memId,
          @RequestParam("page")int page,
          @RequestParam("size") int size) {
    return projectService.getProjectList(memId, page, size);
  }

  @PostMapping("/create")
  public ServiceResponse<Project> createProject(@RequestBody Map<String, Object> request) {
    Long memId = Long.parseLong(request.get("memId").toString());
    String projectName = request.get("name").toString();
    String description = request.get("desc").toString();
    return projectService.createProject(memId, projectName, description);
  }

  @PutMapping("/update")
  public ServiceResponse<Project> updateProject(@RequestBody Map<String, Object> request) {
    Long memId = Long.parseLong(request.get("memId").toString());
    Long projectId = Long.parseLong(request.get("projectId").toString());
    String projectName = request.get("name").toString();
    String description = request.get("desc") != null ? request.get("desc").toString() : null;
    return projectService.updateProject(memId, projectId, projectName, description);
  }

  @PutMapping("/update-name")
  public ServiceResponse<Boolean> updateProjectName(@RequestBody Map<String, Object> request) {
    Long memId = Long.parseLong(request.get("memId").toString());
    Long projectId = Long.parseLong(request.get("projectId").toString());
    String newName = request.get("name").toString();
    return projectService.updateProjectName(memId, projectId, newName);
  }

  @DeleteMapping("/delete")
  public ServiceResponse<Boolean> deleteProject(@RequestBody Map<String, Object> request) {
    Long memId = Long.parseLong(request.get("memId").toString());
    Long projectId = Long.parseLong(request.get("projectId").toString());
    return projectService.deleteProject(memId, projectId);
  }
}
