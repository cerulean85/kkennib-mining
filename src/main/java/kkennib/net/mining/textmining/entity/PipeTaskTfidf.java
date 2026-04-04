package kkennib.net.mining.textmining.entity;

import jakarta.persistence.*;
import kkennib.net.mining.enums.PipeTaskState;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Table(name = "pipe_task_tfidf")
@Entity
@Data
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class PipeTaskTfidf implements StatefulTask, AnalysisTask {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "pipe_line_id", nullable = false)
  private Long pipeLineId = 0L;

  @Column(name = "current_state")
  private String currentState = PipeTaskState.PREPARING.getValue();

  @Column(name = "worker_ip")
  private String workerIp;

  @Column(name = "start_date")
  private LocalDateTime startDate;

  @Column(name = "end_date")
  private LocalDateTime endDate;

  @CreationTimestamp
  @Column(name = "create_date", nullable = false)
  private LocalDateTime createDate;

  @Column(name = "s3_url")
  private String s3Url;

  @Column(name = "file_size")
  private float fileSize;

  @Column(name = "mem_id")
  private Long memId = 0L;

  @Column(name = "search_task_id")
  private Long searchTaskId;

  @Column(name = "clean_task_id")
  private Long cleanTaskId;

  @Column(name = "project_id", nullable = false)
  private Long projectId = 0L;
}
