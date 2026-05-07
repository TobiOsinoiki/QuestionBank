package com.tobi.qbank.controller;

import com.tobi.qbank.entity.QuizAttempt;
import com.tobi.qbank.repository.QuizAttemptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class AttemptController {

    @Autowired
    private QuizAttemptRepository attemptRepository;

    @GetMapping("/history/{userId}")
    public ResponseEntity<?> getHistory(@PathVariable Long userId) {
        List<QuizAttempt> attempts = attemptRepository.findByUserIdOrderByAttemptedAtDesc(userId);

        List<Map<String, Object>> result = attempts.stream().map(a -> Map.<String, Object>of(
            "id", a.getId(),
            "mode", a.getMode(),
            "difficulty", a.getDifficulty(),
            "questionCount", a.getQuestionCount(),
            "correctAnswers", a.getCorrectAnswers(),
            "percentage", a.getPercentage(),
            "passed", a.isPassed(),
            "weightedScore", a.getWeightedScore(),
            "attemptedAt", a.getAttemptedAt().toString()
        )).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<?> getLeaderboard() {
        List<Object[]> rows = attemptRepository.findLeaderboard();

        List<Map<String, Object>> result = rows.stream().map(row -> Map.<String, Object>of(
        	    "userId", row[0],
        	    "fullName", row[1],
        	    "avgWeighted", row[2],
        	    "bestWeighted", row[3],
        	    "totalAttempts", row[4],
        	    "avgPercentage", row[5],
        	    "leaderboardScore", row[6]
        	)).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
}
