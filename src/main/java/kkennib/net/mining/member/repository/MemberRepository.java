package kkennib.net.mining.member.repository;

import kkennib.net.mining.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmailAndPasswordAndAccountType(String email, String password, String accountType);
    Optional<Member> findByEmailAndAccountType(String email, String accountType);
    boolean existsByEmail(String email);

    Optional<Member> findByEmail(String email);
}
