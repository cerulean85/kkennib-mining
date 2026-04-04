package kkennib.net.mining.textmining.entity;

import jakarta.persistence.*;
import kkennib.net.mining.enums.PipeTaskState;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Table(name = "pipe_task_search")
@Entity
@Data
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class PipeTaskSearch implements StatefulTask {
  @Id
  @GeneratedValue(strategy= GenerationType.IDENTITY)
  private Long id;

  @Column(name="pipe_line_id", nullable = false)
  private Long pipeLineId = 0L;

  @Column(name="site", nullable = false)
  private String site = "";

  @Column(name="channel", nullable = false)
  private String channel = "";

  @Column(name="current_state")
  private String currentState = PipeTaskState.PREPARING.getValue();

  @Column(name="search_keyword")
  private String searchKeyword;

  @Column(name="search_start_date")
  private LocalDateTime searchStartDate;

  @Column(name="search_end_date")
  private LocalDateTime searchEndDate;

  @Column(name="worker_ip")
  private String workerIp;

  @Column(name="start_date")
  private LocalDateTime startDate;

  @Column(name="end_date")
  private LocalDateTime endDate;

  @CreationTimestamp
  @Column(name="create_date", nullable = false)
  private LocalDateTime createDate;

  @Column(name="count")
  private Long count = 0L;

  @Column(name="s3_url")
  private String s3Url;

  @Column(name="file_size")
  private float fileSize;

  @Column(name="mem_id")
  private Long memId = 0L;

  @Column(name="project_id")
  private Long projectId = 0L;
}
