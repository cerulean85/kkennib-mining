package kkennib.net.mining.textmining.repository;


import kkennib.net.mining.textmining.entity.PipeTaskClean;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PipeTaskCleanRepository extends JpaRepository<PipeTaskClean, Long> {
  List<PipeTaskClean> findByMemId(Long memId);

  List<PipeTaskClean> findAllByMemId(Long memId);

  List<PipeTaskClean> findAllByIdIn(List<Long> ids);

  void deleteAllInBatch(Iterable<PipeTaskClean> entities);

  List<PipeTaskClean> findAllByMemIdAndProjectId(Long memId, Long projectId);
}
