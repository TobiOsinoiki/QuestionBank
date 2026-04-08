package com.tobi.qbank.repository;

import com.tobi.qbank.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query(value = "SELECT * FROM question ORDER BY RAND() LIMIT 10", nativeQuery = true)
    List<Question> findRandomQuestions();
}
