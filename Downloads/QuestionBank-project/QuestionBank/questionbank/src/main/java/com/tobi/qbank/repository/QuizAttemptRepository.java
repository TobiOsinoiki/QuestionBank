package com.tobi.qbank.repository;

import com.tobi.qbank.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    List<QuizAttempt> findByUserIdOrderByAttemptedAtDesc(Long userId);

    @Query(value = """
SELECT 
    u.id as userId,
    u.full_name as fullName,

    ROUND(AVG(qa.weighted_score), 1) as avgWeighted,
    MAX(qa.weighted_score) as bestWeighted,
    COUNT(qa.id) as totalAttempts,
    ROUND(AVG(qa.percentage), 1) as avgPercentage,

    ROUND(
        (0.6 * AVG(qa.weighted_score)) +
        (0.3 * MAX(qa.weighted_score)) +
        (0.1 * AVG(qa.weighted_score) * LOG10(COUNT(*) + 1)), 1) as leaderboardScore

FROM quiz_attempt qa
JOIN users u ON qa.user_id = u.id
GROUP BY u.id, u.full_name
ORDER BY leaderboardScore DESC
LIMIT 20;
        """, nativeQuery = true)
    List<Object[]> findLeaderboard();
}
