package kkennib.net.mining.textmining.repository;

import kkennib.net.mining.textmining.entity.PipeTaskTfidf;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PipeTaskTfidfRepository extends JpaRepository<PipeTaskTfidf, Long> {
  List<PipeTaskTfidf> findAllByMemId(Long memId);

  List<PipeTaskTfidf> findAllByIdIn(List<Long> ids);

  void deleteAllInBatch(Iterable<PipeTaskTfidf> entities);

  List<PipeTaskTfidf> findAllByMemIdAndProjectId(Long memId, Long projectId);
}
