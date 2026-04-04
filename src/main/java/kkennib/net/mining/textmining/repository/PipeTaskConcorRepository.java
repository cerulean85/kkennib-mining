package kkennib.net.mining.textmining.repository;

import kkennib.net.mining.textmining.entity.PipeTaskConcor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PipeTaskConcorRepository extends JpaRepository<PipeTaskConcor, Long> {
  List<PipeTaskConcor> findAllByMemId(Long memId);

  List<PipeTaskConcor> findAllByIdIn(List<Long> ids);

  void deleteAllInBatch(Iterable<PipeTaskConcor> entities);

  List<PipeTaskConcor> findAllByMemIdAndProjectId(Long memId, Long projectId);
}
