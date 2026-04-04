package kkennib.net.mining.project.entity;


import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Table(name = "project")
@Entity
@Data
public class Project {
  @Id
  @GeneratedValue(strategy= GenerationType.IDENTITY)
  private Long id;

  private String name;
  private String description;

  @CreationTimestamp
  @Column(name="create_date")
  private LocalDateTime createDate = LocalDateTime.now();

  @Column(name="mem_id", nullable = false)
  private Long memberId = 0L;
}
