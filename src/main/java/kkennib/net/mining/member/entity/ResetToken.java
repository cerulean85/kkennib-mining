package kkennib.net.mining.member.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Table(name = "reset_token")
@Entity
@Data
public class ResetToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long userId;  // 또는 @ManyToOne 관계 설정 가능

  private String token;

  private LocalDateTime expiresAt;

  private boolean used;

  // 생성자
  public ResetToken(Long userId, String token, LocalDateTime expiresAt, boolean used) {
    this.userId = userId;
    this.token = token;
    this.expiresAt = expiresAt;
    this.used = used;
  }

  protected ResetToken() {}  // JPA 기본 생성자
}
