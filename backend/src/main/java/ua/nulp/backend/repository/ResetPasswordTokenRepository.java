package ua.nulp.backend.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.nulp.backend.entity.user.token.ResetPasswordToken;
import ua.nulp.backend.entity.user.user.User;

import java.util.Optional;

@Repository
public interface ResetPasswordTokenRepository extends JpaRepository<ResetPasswordToken, Long> {
    @EntityGraph(attributePaths = {"user"})
    Optional<ResetPasswordToken> findByToken(String token);
    void deleteAllByUser(User user);
}