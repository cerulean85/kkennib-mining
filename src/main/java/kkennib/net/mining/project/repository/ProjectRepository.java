package kkennib.net.mining.project.repository;

import kkennib.net.mining.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
  List<Project> findByMemberId(Long memberId);
  long countByMemberId(Long memberId);
  Optional<Project> findByIdAndMemberId(Long id, Long memberId);
  void deleteByIdAndMemberId(Long id, Long memberId);
}
