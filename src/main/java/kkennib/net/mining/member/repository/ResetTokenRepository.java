package kkennib.net.mining.member.repository;

import kkennib.net.mining.member.entity.ResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ResetTokenRepository extends JpaRepository<ResetToken, Long> {

  Optional<ResetToken> findByToken(String token);

  Optional<ResetToken> findByUserId(Long userId);

  void deleteByExpiresAtBefore(LocalDateTime time); // 만료된 토큰 정리용
}

