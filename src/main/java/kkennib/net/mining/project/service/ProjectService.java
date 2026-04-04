package kkennib.net.mining.project.service;

import kkennib.net.mining.dto.ServiceResponse;
import kkennib.net.mining.project.entity.Project;
import kkennib.net.mining.project.repository.ProjectRepository;
import kkennib.net.mining.textmining.dto.ProjectPaginationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {
  private final ProjectRepository projectRepository;
  public ServiceResponse<ProjectPaginationResponse> getProjectList(Long memId, int page, int size) {
    ServiceResponse<ProjectPaginationResponse> response = new ServiceResponse<>();
    ProjectPaginationResponse paginationResponse = new ProjectPaginationResponse();

    try {
      int totalCount = (int) projectRepository.countByMemberId(memId);
      paginationResponse.setTotalCount(totalCount);
      paginationResponse.setItemCount(size);

      int offset = (page - 1) * size;
      List<Project> list = projectRepository.findByMemberId(memId)
              .stream()
              .skip(offset)
              .limit(size)
              .toList();
      paginationResponse.setList(list);
      paginationResponse.setItemCount(list.size());

      response.setData(paginationResponse);
      response.setSuccess(true);
    } catch (Exception e) {
      log.error("Error fetching project list for member ID: " + memId, e);
      response.setSuccess(false);
      response.setMessage("Failed to fetch project list: " + e.getMessage());
    }

    return response;
  }

  public ServiceResponse<Project> createProject(Long memId, String projectName, String description) {
    ServiceResponse<Project> response = new ServiceResponse<>();
    try {
      Project project = new Project();
      project.setMemberId(memId);
      project.setName(projectName);
      project.setDescription(description);
      project = projectRepository.save(project);
      response.setData(project);
      response.setSuccess(true);
    } catch (Exception e) {
      log.error("Error creating project for member ID: " + memId, e);
      response.setSuccess(false);
      response.setMessage("Failed to create project: " + e.getMessage());
    }
    return response;
  }

  public ServiceResponse<Project> updateProject(Long memId, Long projectId, String projectName, String description) {
    ServiceResponse<Project> response = new ServiceResponse<>();
    try {
      Project project = projectRepository.findByIdAndMemberId(projectId, memId)
          .orElseThrow(() -> new RuntimeException("Project not found with ID: " + projectId + " for member: " + memId));
      project.setName(projectName);
      project.setDescription(description);
      project = projectRepository.save(project);
      response.setData(project);
      response.setSuccess(true);
    } catch (Exception e) {
      log.error("Error updating project for ID: " + projectId + " and member: " + memId, e);
      response.setSuccess(false);
      response.setMessage("Failed to update project: " + e.getMessage());
    }
    return response;
  }

  @Transactional
  public ServiceResponse<Boolean> updateProjectName(Long memId, Long projectId, String newName) {
    ServiceResponse<Boolean> response = new ServiceResponse<>();
    try {
      Project project = projectRepository.findByIdAndMemberId(projectId, memId)
          .orElseThrow(() -> new RuntimeException("Project not found with ID: " + projectId + " for member: " + memId));
      project.setName(newName);
      projectRepository.save(project);
      response.setData(true);
      response.setSuccess(true);
    } catch (Exception e) {
      log.error("Error updating project name for ID: " + projectId + " and member: " + memId, e);
      response.setSuccess(false);
      response.setMessage("Failed to update project name: " + e.getMessage());
    }
    return response;
  }

  @Transactional
  public ServiceResponse<Boolean> deleteProject(Long memId, Long projectId) {
    ServiceResponse<Boolean> response = new ServiceResponse<>();
    try {
      // 먼저 해당 회원의 프로젝트가 존재하는지 확인
      if (projectRepository.findByIdAndMemberId(projectId, memId).isEmpty()) {
        throw new RuntimeException("Project not found with ID: " + projectId + " for member: " + memId);
      }
      projectRepository.deleteByIdAndMemberId(projectId, memId);
      response.setData(true);
      response.setSuccess(true);
    } catch (Exception e) {
      log.error("Error deleting project with ID: " + projectId + " for member: " + memId, e);
      response.setSuccess(false);
      response.setMessage("Failed to delete project: " + e.getMessage());
    }
    return response;
  }
}
