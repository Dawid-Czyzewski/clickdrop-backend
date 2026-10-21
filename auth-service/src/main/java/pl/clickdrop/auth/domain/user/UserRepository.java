package pl.clickdrop.auth.domain.user;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    List<User> findByEnabledTrueOrderByLevelDescExperienceDesc(Pageable pageable);

    @Query("""
        SELECT COUNT(u) FROM User u
        WHERE u.enabled = true
          AND (
               u.level > :level
            OR (u.level = :level AND u.experience > :experience)
          )
        """)
    long countRankedAbove(
            @Param("level") Integer level,
            @Param("experience") Integer experience
    );
}
