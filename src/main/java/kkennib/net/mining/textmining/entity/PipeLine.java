package kkennib.net.mining.textmining.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Table(name = "pipe_line")
@Entity
@Data
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class PipeLine {
  @Id
  @GeneratedValue(strategy= GenerationType.IDENTITY)
  private Long id;

  @Column(name="current_status")
  private String currentStatus;

  @CreationTimestamp
  @Column(name="create_date")
  private LocalDateTime createDate = LocalDateTime.now();

  @Column(name="mem_id", nullable = false)
  private Long memberId = 0L;

  @Column(name="project_id", nullable = false)
  private Long projectId = 0L;
}
