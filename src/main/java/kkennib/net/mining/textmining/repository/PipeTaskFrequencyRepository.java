package kkennib.net.mining.textmining.repository;

import kkennib.net.mining.textmining.entity.PipeTaskFrequency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PipeTaskFrequencyRepository extends JpaRepository<PipeTaskFrequency, Long> {
  List<PipeTaskFrequency> findAllByMemId(Long memId);

  List<PipeTaskFrequency> findAllByIdIn(List<Long> ids);

  void deleteAllInBatch(Iterable<PipeTaskFrequency> entities);

  List<PipeTaskFrequency> findAllByMemIdAndProjectId(Long memId, Long projectId);
}
