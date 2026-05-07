package com.tobi.qbank.repository;

import com.tobi.qbank.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    // ── Random question fetching ──────────────────────────────────────────────

    @Query(value = "SELECT * FROM question ORDER BY RAND() LIMIT 10", nativeQuery = true)
    List<Question> findRandomQuestions();

    /**
     * Random by difficulty only (no subject filter).
     */
    @Query(value = "SELECT * FROM question WHERE UPPER(difficulty) = UPPER(?1) ORDER BY RAND() LIMIT ?2", nativeQuery = true)
    List<Question> findRandomByDifficulty(@Param("difficulty") String difficulty, @Param("count") int count);

    /**
     * Random mixed (no subject filter).
     */
    @Query(value = "SELECT * FROM question ORDER BY RAND() LIMIT ?1", nativeQuery = true)
    List<Question> findRandomMixed(@Param("count") int count);

    /**
     * Random by subject only, any difficulty (MIXED mode for a subject).
     */
    @Query(value = "SELECT * FROM question WHERE UPPER(subject) = UPPER(?1) ORDER BY RAND() LIMIT ?2", nativeQuery = true)
    List<Question> findRandomBySubject(@Param("subject") String subject, @Param("count") int count);

    /**
     * Random by subject AND difficulty.
     */
    @Query(value = "SELECT * FROM question WHERE UPPER(subject) = UPPER(?1) AND UPPER(difficulty) = UPPER(?2) ORDER BY RAND() LIMIT ?3", nativeQuery = true)
    List<Question> findRandomBySubjectAndDifficulty(
            @Param("subject") String subject,
            @Param("difficulty") String difficulty,
            @Param("count") int count);

    /**
     * Random by subject AND topic.
     */
    @Query(value = "SELECT * FROM question WHERE UPPER(subject) = UPPER(?1) AND LOWER(topic) = LOWER(?2) ORDER BY RAND() LIMIT ?3", nativeQuery = true)
    List<Question> findRandomBySubjectAndTopic(
            @Param("subject") String subject,
            @Param("topic") String topic,
            @Param("count") int count);

    /**
     * Random by subject AND topic AND difficulty.
     */
    @Query(value = "SELECT * FROM question WHERE UPPER(subject) = UPPER(?1) AND LOWER(topic) = LOWER(?2) AND UPPER(difficulty) = UPPER(?3) ORDER BY RAND() LIMIT ?4", nativeQuery = true)
    List<Question> findRandomBySubjectTopicAndDifficulty(
            @Param("subject") String subject,
            @Param("topic") String topic,
            @Param("difficulty") String difficulty,
            @Param("count") int count);

    // ── Admin filters ─────────────────────────────────────────────────────────

    List<Question> findByFlaggedTrue();
    List<Question> findByDifficultyIgnoreCase(String difficulty);
    List<Question> findByTopicIgnoreCase(String topic);
    List<Question> findBySubjectIgnoreCase(String subject);

    @Query("SELECT q FROM Question q WHERE UPPER(q.subject) = UPPER(:subject) AND UPPER(q.difficulty) = UPPER(:difficulty)")
    List<Question> findBySubjectAndDifficulty(
            @Param("subject") String subject,
            @Param("difficulty") String difficulty);

    @Query("SELECT q FROM Question q WHERE UPPER(q.subject) = UPPER(:subject) AND q.flagged = true")
    List<Question> findBySubjectAndFlagged(@Param("subject") String subject);

    // ── Performance queries ───────────────────────────────────────────────────

    @Query("SELECT q FROM Question q WHERE q.timesAnswered >= 5 AND (q.timesCorrect * 1.0 / q.timesAnswered) < :threshold ORDER BY (q.timesCorrect * 1.0 / q.timesAnswered) ASC")
    List<Question> findPoorlyPerforming(@Param("threshold") double threshold);

    @Query("SELECT q FROM Question q WHERE q.timesAnswered > 0 ORDER BY (q.timesAnswered - q.timesCorrect) DESC")
    List<Question> findMostFrequentlyMissed();

    // ── Analytics ─────────────────────────────────────────────────────────────

    @Query("SELECT q.difficulty, COUNT(q), AVG(CASE WHEN q.timesAnswered > 0 THEN q.timesCorrect * 1.0 / q.timesAnswered ELSE 0 END) FROM Question q GROUP BY q.difficulty")
    List<Object[]> getStatsByDifficulty();

    @Query("SELECT q.topic, COUNT(q), AVG(CASE WHEN q.timesAnswered > 0 THEN q.timesCorrect * 1.0 / q.timesAnswered ELSE 0 END) FROM Question q GROUP BY q.topic ORDER BY q.topic")
    List<Object[]> getStatsByTopic();

    /**
     * Topic stats filtered by subject — used by admin analytics panel.
     */
    @Query("SELECT q.topic, COUNT(q), AVG(CASE WHEN q.timesAnswered > 0 THEN q.timesCorrect * 1.0 / q.timesAnswered ELSE 0 END) FROM Question q WHERE UPPER(q.subject) = UPPER(:subject) GROUP BY q.topic ORDER BY q.topic")
    List<Object[]> getStatsByTopicForSubject(@Param("subject") String subject);

    /**
     * Subject-level stats — overall accuracy and count per subject.
     */
    @Query("SELECT q.subject, COUNT(q), AVG(CASE WHEN q.timesAnswered > 0 THEN q.timesCorrect * 1.0 / q.timesAnswered ELSE 0 END) FROM Question q GROUP BY q.subject ORDER BY q.subject")
    List<Object[]> getStatsBySubject();

    long countByDifficultyIgnoreCase(String difficulty);
    long countByFlaggedTrue();
}