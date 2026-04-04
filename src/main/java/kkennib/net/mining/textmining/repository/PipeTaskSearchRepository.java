package kkennib.net.mining.textmining.repository;

import kkennib.net.mining.textmining.dto.JoinedPipeTask;
import kkennib.net.mining.textmining.entity.PipeTaskSearch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PipeTaskSearchRepository extends JpaRepository<PipeTaskSearch, Long> {
  List<PipeTaskSearch> findByMemId(Long memId);

  List<PipeTaskSearch> findAllByMemId(Long memId);

  List<PipeTaskSearch> findAllByIdIn(List<Long> ids);

  List<PipeTaskSearch> findAllByMemIdAndProjectId(Long memId, Long projectId);

  @Query("SELECT new kkennib.net.mining.textmining.dto.JoinedPipeTask(" +
          "s.id, s.searchKeyword, s.site, s.channel, s.searchStartDate, s.searchEndDate, s.currentState, " +
          "c.id, c.currentState, c.extractNoun, c.extractAdjective, c.extractVerb, " +
          "f.id, f.currentState, f.s3Url, " +
          "t.id, t.currentState, t.s3Url, " +
          "co.id, co.currentState, co.s3Url) " +
          "FROM PipeTaskSearch s " +
          "LEFT JOIN PipeTaskClean c ON s.id = c.searchTaskId " +
          "LEFT JOIN PipeTaskFrequency f ON s.id = f.searchTaskId " +
          "LEFT JOIN PipeTaskTfidf t ON s.id = t.searchTaskId " +
          "LEFT JOIN PipeTaskConcor co ON s.id = co.searchTaskId " +
          "WHERE s.memId = :memId AND s.projectId = :projectId " +
          "ORDER BY s.createDate DESC")
  List<JoinedPipeTask> findTop20JoinedTasksByMemIdAndProjectId(@Param("memId") Long memId, @Param("projectId") Long projectId, Pageable pageable);
}
