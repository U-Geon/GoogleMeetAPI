package test.googlemeetapi.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import test.googlemeetapi.domain.Member;

import java.util.Optional;

public interface AuthRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
}
