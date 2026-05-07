package com.tobi.qbank.controller;

import com.tobi.qbank.entity.Question;
import com.tobi.qbank.entity.QuizAttempt;
import com.tobi.qbank.repository.QuestionRepository;
import com.tobi.qbank.repository.QuizAttemptRepository;
import com.tobi.qbank.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class QuestionController {

    @Autowired private QuestionRepository    questionRepository;
    @Autowired private QuizAttemptRepository attemptRepository;
    @Autowired private UserRepository        userRepository;

    private static final Set<Integer> VALID_COUNTS       = Set.of(10, 20, 30, 40, 50);
    private static final Set<String>  VALID_DIFFICULTIES = Set.of("EASY", "MEDIUM", "HARD", "MIXED");

    /**
     * GET /api/questions/random
     *
     * Params:
     *   difficulty  – EASY | MEDIUM | HARD | MIXED  (default MIXED)
     *   count       – 10 | 20 | 30 | 40 | 50         (default 10)
     *   subject     – e.g. JAVA, CPP, REACT …        (optional)
     *   topic       – e.g. Variables, Loops …        (optional; only used when subject is also provided)
     */
    @GetMapping("/questions/random")
    public ResponseEntity<?> getRandomQuestions(
            @RequestParam(defaultValue = "MIXED") String difficulty,
            @RequestParam(defaultValue = "10")    int    count,
            @RequestParam(required = false)        String subject,
            @RequestParam(required = false)        String topic) {

        String diff = difficulty.toUpperCase();
        if (!VALID_DIFFICULTIES.contains(diff))
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid difficulty."));
        if (!VALID_COUNTS.contains(count))
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid count. Use 10, 20, 30, 40, or 50."));

        List<Question> questions;

        boolean hasSubject = subject != null && !subject.isBlank();
        boolean hasTopic   = topic   != null && !topic.isBlank();

        if (hasSubject && hasTopic) {
            // Subject + topic (+ optional non-MIXED difficulty)
            if (diff.equals("MIXED")) {
                questions = questionRepository.findRandomBySubjectAndTopic(subject, topic, count);
            } else {
                questions = questionRepository.findRandomBySubjectTopicAndDifficulty(subject, topic, diff, count);
            }
        } else if (hasSubject) {
            // Subject only
            if (diff.equals("MIXED")) {
                questions = questionRepository.findRandomBySubject(subject, count);
            } else {
                questions = questionRepository.findRandomBySubjectAndDifficulty(subject, diff, count);
            }
        } else {
            // No subject filter — original behaviour
            if (diff.equals("MIXED")) {
                questions = questionRepository.findRandomMixed(count);
            } else {
                questions = questionRepository.findRandomByDifficulty(diff, count);
            }
        }

        if (questions.isEmpty()) return ResponseEntity.ok(List.of());

        List<Map<String, Object>> response = questions.stream().map(q -> {
            Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("id",           q.getId());
            m.put("text",         q.getQuestionText());
            m.put("subject",      q.getSubject());
            m.put("topic",        q.getTopic());
            m.put("difficulty",   q.getDifficulty());
            m.put("correctAnswer",q.getCorrectAnswer());
            m.put("explanationA", q.getExplanationA());
            m.put("explanationB", q.getExplanationB());
            m.put("explanationC", q.getExplanationC());
            m.put("explanationD", q.getExplanationD());
            m.put("options", List.of(
                Map.of("label", q.getOptionA(), "value", "A"),
                Map.of("label", q.getOptionB(), "value", "B"),
                Map.of("label", q.getOptionC(), "value", "C"),
                Map.of("label", q.getOptionD(), "value", "D")
            ));
            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/submit")
    @Transactional
    public ResponseEntity<?> submitQuiz(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> answers = (List<Map<String, Object>>) body.get("answers");
        String mode       = body.getOrDefault("mode",       "EXAM").toString().toUpperCase();
        String difficulty = body.getOrDefault("difficulty", "MIXED").toString().toUpperCase();
        Long   userId     = body.get("userId") != null
            ? Long.parseLong(body.get("userId").toString()) : null;

        if (answers == null || answers.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("message", "No answers submitted."));

        int total = answers.size();
        List<Map<String, Object>> reviewData = answers.stream().map(answer -> {
            Long     questionId = Long.parseLong(answer.get("questionId").toString());
            String   submitted  = answer.get("answer") != null ? answer.get("answer").toString() : "";
            Question q          = questionRepository.findById(questionId).orElse(null);
            boolean  isCorrect  = q != null && q.getCorrectAnswer().equalsIgnoreCase(submitted);

            if (q != null) {
                q.setTimesAnswered(q.getTimesAnswered() + 1);
                if (isCorrect) q.setTimesCorrect(q.getTimesCorrect() + 1);
                if (q.getTimesAnswered() >= 10 && q.getAccuracyRate() < 30.0) q.setFlagged(true);
                questionRepository.save(q);
            }

            String submittedExp = q != null ? q.getExplanationFor(submitted) : null;
            String correctExp   = q != null ? q.getExplanationFor(q.getCorrectAnswer()) : null;

            if ((correctExp == null || correctExp.isBlank()) && q != null) {
                correctExp = q.getCorrectAnswer() + ": " + optionTextFor(q, q.getCorrectAnswer());
            }
            if ((submittedExp == null || submittedExp.isBlank()) && q != null && !submitted.isBlank()) {
                submittedExp = "No explanation available for option " + submitted + ".";
            }

            Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("questionId",            questionId);
            m.put("questionText",          q != null ? q.getQuestionText() : "");
            m.put("submitted",             submitted);
            m.put("correctAnswer",         q != null ? q.getCorrectAnswer() : "");
            m.put("isCorrect",             isCorrect);
            m.put("optionA",               q != null ? q.getOptionA() : "");
            m.put("optionB",               q != null ? q.getOptionB() : "");
            m.put("optionC",               q != null ? q.getOptionC() : "");
            m.put("optionD",               q != null ? q.getOptionD() : "");
            m.put("submittedExplanation",  submittedExp);
            m.put("correctExplanation",    correctExp);
            m.put("explanation",           correctExp); // backward compat
            return m;
        }).collect(Collectors.toList());

        int correct = (int) reviewData.stream().filter(r -> (boolean) r.get("isCorrect")).count();
        double percentage = total > 0 ? (correct * 100.0) / total : 0;
        boolean passed    = percentage >= 50;

        double difficultyMultiplier = switch (difficulty) {
            case "EASY"   -> 1.0;
            case "MEDIUM" -> 1.5;
            case "HARD"   -> 2.0;
            case "MIXED"  -> 1.7;
            default       -> 1.0;
        };

        double sizeMultiplier = switch (total) {
            case 10 -> 1.0;
            case 20 -> 1.2;
            case 30 -> 1.4;
            case 40 -> 1.6;
            case 50 -> 1.8;
            default -> 1.0;
        };

        double weightedScore = Math.round(
            percentage * difficultyMultiplier * sizeMultiplier * 10.0
        ) / 10.0;

        final int finalCorrect = correct;

        // STUDY mode: userId is sent as null from the frontend — no history recorded
        if (userId != null) {
            userRepository.findById(userId).ifPresent(user -> {
                QuizAttempt attempt = new QuizAttempt();
                attempt.setUser(user);
                attempt.setMode(mode);
                attempt.setDifficulty(difficulty);
                attempt.setQuestionCount(total);
                attempt.setCorrectAnswers(finalCorrect);
                attempt.setPercentage(percentage);
                attempt.setPassed(passed);
                attempt.setWeightedScore(weightedScore);
                attempt.setAttemptedAt(LocalDateTime.now());
                attemptRepository.save(attempt);
            });
        }

        return ResponseEntity.ok(Map.of(
            "correctAnswers", correct,
            "totalQuestions", total,
            "percentage",     percentage,
            "passed",         passed,
            "weightedScore",  weightedScore,
            "mode",           mode,
            "message",        passed ? "Great work. You passed the quiz." : "Keep practicing and try again.",
            "review",         "EXAM".equals(mode) ? List.of() : reviewData
        ));
    }

    private String optionTextFor(Question q, String letter) {
        if (q == null || letter == null) return "";
        return switch (letter.toUpperCase()) {
            case "A" -> q.getOptionA();
            case "B" -> q.getOptionB();
            case "C" -> q.getOptionC();
            case "D" -> q.getOptionD();
            default  -> "";
        };
    }
}