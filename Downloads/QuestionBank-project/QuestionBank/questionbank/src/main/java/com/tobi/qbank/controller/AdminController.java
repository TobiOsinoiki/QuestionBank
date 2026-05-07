package com.tobi.qbank.controller;

import com.tobi.qbank.entity.Question;
import com.tobi.qbank.entity.QuizAttempt;
import com.tobi.qbank.entity.User;
import com.tobi.qbank.repository.QuestionRepository;
import com.tobi.qbank.repository.QuizAttemptRepository;
import com.tobi.qbank.repository.UserRepository;
import com.tobi.qbank.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired private QuestionRepository    questionRepository;
    @Autowired private UserRepository        userRepository;
    @Autowired private QuizAttemptRepository attemptRepository;
    @Autowired private UserService           userService;

    // ── Auth guard ───────────────────────────────────────────────────────────

    private ResponseEntity<?> unauthorized() {
        return ResponseEntity.status(403).body(Map.of("message", "Admin access required."));
    }

    private boolean isAdmin(String token) {
        if (token == null) return false;
        return token.endsWith("|ROLE_ADMIN");
    }

    // ── Dashboard Analytics ──────────────────────────────────────────────────

    @GetMapping("/analytics")
    public ResponseEntity<?> getAnalytics(
            @RequestHeader(value = "X-Admin-Token", required = false) String token) {
        if (!isAdmin(token)) return unauthorized();

        List<QuizAttempt> allAttempts = attemptRepository.findAll();
        long totalQuizzes     = allAttempts.size();
        long totalUsers       = userRepository.count();
        long totalQuestions   = questionRepository.count();
        long flaggedQuestions = questionRepository.countByFlaggedTrue();

        double avgScore = allAttempts.isEmpty() ? 0 :
            allAttempts.stream().mapToDouble(QuizAttempt::getPercentage).average().orElse(0);

        long passCount  = allAttempts.stream().filter(QuizAttempt::isPassed).count();
        double passRate = totalQuizzes == 0 ? 0 : (passCount * 100.0) / totalQuizzes;

        Map<String, Long> quizzesByDay = allAttempts.stream()
            .filter(a -> a.getAttemptedAt() != null
                      && a.getAttemptedAt().isAfter(LocalDateTime.now().minusDays(14)))
            .collect(Collectors.groupingBy(
                a -> a.getAttemptedAt().toLocalDate().toString(),
                Collectors.counting()));

        Map<String, Long> attemptsByDifficulty = allAttempts.stream()
            .filter(a -> a.getDifficulty() != null && !a.getDifficulty().isBlank())
            .collect(Collectors.groupingBy(
                a -> a.getDifficulty().toUpperCase(),
                Collectors.counting()));

        Map<String, Double> avgByDifficulty = allAttempts.stream()
            .filter(a -> a.getDifficulty() != null && !a.getDifficulty().isBlank())
            .collect(Collectors.groupingBy(
                a -> a.getDifficulty().toUpperCase(),
                Collectors.averagingDouble(QuizAttempt::getPercentage)));

        List<Map<String, Object>> difficultyStats = questionRepository.getStatsByDifficulty().stream()
            .map(row -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("difficulty",    row[0] != null ? row[0].toString().toUpperCase() : "—");
                m.put("questionCount", ((Number) row[1]).longValue());
                double acc = row[2] != null ? ((Number) row[2]).doubleValue() : 0;
                m.put("avgAccuracy",   Math.round(acc * 1000.0) / 10.0);
                return m;
            }).collect(Collectors.toList());

        List<Map<String, Object>> topicBreakdown = questionRepository.getStatsByTopic().stream()
            .map(row -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("topic",         row[0] != null ? row[0].toString() : "—");
                m.put("questionCount", ((Number) row[1]).longValue());
                double acc = row[2] != null ? ((Number) row[2]).doubleValue() : 0;
                m.put("avgAccuracy",   Math.round(acc * 1000.0) / 10.0);
                return m;
            }).collect(Collectors.toList());

        // NEW: subject breakdown
        List<Map<String, Object>> subjectBreakdown = questionRepository.getStatsBySubject().stream()
            .map(row -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("subject",       row[0] != null ? row[0].toString() : "—");
                m.put("questionCount", ((Number) row[1]).longValue());
                double acc = row[2] != null ? ((Number) row[2]).doubleValue() : 0;
                m.put("avgAccuracy",   Math.round(acc * 1000.0) / 10.0);
                return m;
            }).collect(Collectors.toList());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("totalQuizzes",         totalQuizzes);
        response.put("totalUsers",           totalUsers);
        response.put("totalQuestions",       totalQuestions);
        response.put("flaggedQuestions",     flaggedQuestions);
        response.put("avgScore",             Math.round(avgScore * 10.0) / 10.0);
        response.put("passRate",             Math.round(passRate * 10.0) / 10.0);
        response.put("quizzesByDay",         quizzesByDay);
        response.put("attemptsByDifficulty", attemptsByDifficulty);
        response.put("avgByDifficulty",      avgByDifficulty);
        response.put("difficultyStats",      difficultyStats);
        response.put("topicBreakdown",       topicBreakdown);
        response.put("subjectBreakdown",     subjectBreakdown);
        return ResponseEntity.ok(response);
    }

    // ── Question Management ──────────────────────────────────────────────────

    /**
     * GET /api/admin/questions
     *
     * Optional query params: difficulty, topic, subject, flagged
     */
    @GetMapping("/questions")
    public ResponseEntity<?> getAllQuestions(
            @RequestHeader(value = "X-Admin-Token", required = false) String token,
            @RequestParam(required = false) String  difficulty,
            @RequestParam(required = false) String  topic,
            @RequestParam(required = false) String  subject,
            @RequestParam(required = false) Boolean flagged) {
        if (!isAdmin(token)) return unauthorized();

        List<Question> questions;

        boolean hasSubject    = subject    != null && !subject.isBlank();
        boolean hasDifficulty = difficulty != null && !difficulty.isBlank();
        boolean hasTopic      = topic      != null && !topic.isBlank();

        if (Boolean.TRUE.equals(flagged)) {
            questions = hasSubject
                ? questionRepository.findBySubjectAndFlagged(subject)
                : questionRepository.findByFlaggedTrue();
        } else if (hasSubject && hasDifficulty) {
            questions = questionRepository.findBySubjectAndDifficulty(subject, difficulty);
        } else if (hasSubject && hasTopic) {
            questions = questionRepository.findAll().stream()
                .filter(q -> subject.equalsIgnoreCase(q.getSubject())
                          && topic.equalsIgnoreCase(q.getTopic()))
                .collect(Collectors.toList());
        } else if (hasSubject) {
            questions = questionRepository.findBySubjectIgnoreCase(subject);
        } else if (hasDifficulty) {
            questions = questionRepository.findByDifficultyIgnoreCase(difficulty);
        } else if (hasTopic) {
            questions = questionRepository.findByTopicIgnoreCase(topic);
        } else {
            questions = questionRepository.findAll();
        }

        return ResponseEntity.ok(questions.stream().map(this::toQuestionMap).collect(Collectors.toList()));
    }

    @GetMapping("/questions/poorly-performing")
    public ResponseEntity<?> getPoorlyPerforming(
            @RequestHeader(value = "X-Admin-Token", required = false) String token) {
        if (!isAdmin(token)) return unauthorized();
        return ResponseEntity.ok(
            questionRepository.findPoorlyPerforming(0.5).stream()
                .map(this::toQuestionMap).collect(Collectors.toList()));
    }

    @GetMapping("/questions/most-missed")
    public ResponseEntity<?> getMostMissed(
            @RequestHeader(value = "X-Admin-Token", required = false) String token) {
        if (!isAdmin(token)) return unauthorized();
        return ResponseEntity.ok(
            questionRepository.findMostFrequentlyMissed().stream()
                .limit(20).map(this::toQuestionMap).collect(Collectors.toList()));
    }

    /**
     * GET /api/admin/questions/topic-stats?subject=JAVA
     * Returns accuracy and question count per topic for the given subject.
     */
    @GetMapping("/questions/topic-stats")
    public ResponseEntity<?> getTopicStats(
            @RequestHeader(value = "X-Admin-Token", required = false) String token,
            @RequestParam(required = false) String subject) {
        if (!isAdmin(token)) return unauthorized();

        List<Object[]> rows = (subject != null && !subject.isBlank())
            ? questionRepository.getStatsByTopicForSubject(subject)
            : questionRepository.getStatsByTopic();

        List<Map<String, Object>> result = rows.stream().map(row -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("topic",         row[0] != null ? row[0].toString() : "—");
            m.put("questionCount", ((Number) row[1]).longValue());
            double acc = row[2] != null ? ((Number) row[2]).doubleValue() : 0;
            m.put("avgAccuracy",   Math.round(acc * 1000.0) / 10.0);
            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @PostMapping("/questions")
    public ResponseEntity<?> createQuestion(
            @RequestHeader(value = "X-Admin-Token", required = false) String token,
            @RequestBody Map<String, String> body) {
        if (!isAdmin(token)) return unauthorized();
        Question q = buildQuestionFromBody(new Question(), body);
        questionRepository.save(q);
        return ResponseEntity.ok(Map.of("message", "Question created.", "id", q.getId()));
    }

    @PutMapping("/questions/{id}")
    public ResponseEntity<?> updateQuestion(
            @RequestHeader(value = "X-Admin-Token", required = false) String token,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        if (!isAdmin(token)) return unauthorized();
        return questionRepository.findById(id).map(q -> {
            buildQuestionFromBody(q, body);
            questionRepository.save(q);
            return ResponseEntity.ok(Map.of("message", "Question updated."));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/questions/{id}")
    public ResponseEntity<?> deleteQuestion(
            @RequestHeader(value = "X-Admin-Token", required = false) String token,
            @PathVariable Long id) {
        if (!isAdmin(token)) return unauthorized();
        if (!questionRepository.existsById(id)) return ResponseEntity.notFound().build();
        questionRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Question deleted."));
    }

    @PatchMapping("/questions/{id}/flag")
    public ResponseEntity<?> toggleFlag(
            @RequestHeader(value = "X-Admin-Token", required = false) String token,
            @PathVariable Long id) {
        if (!isAdmin(token)) return unauthorized();
        return questionRepository.findById(id).map(q -> {
            q.setFlagged(!q.isFlagged());
            questionRepository.save(q);
            return ResponseEntity.ok(Map.of("flagged", q.isFlagged()));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── CSV Bulk Upload ──────────────────────────────────────────────────────

    @PostMapping("/questions/upload-csv")
    public ResponseEntity<?> uploadCsv(
            @RequestHeader(value = "X-Admin-Token", required = false) String token,
            @RequestParam("file") MultipartFile file) {
        if (!isAdmin(token)) return unauthorized();

        List<String> errors  = new ArrayList<>();
        int imported = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                if (lineNum == 1 && line.toLowerCase().startsWith("subject")) continue;
                String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                if (parts.length < 9) {
                    errors.add("Line " + lineNum + ": expected 9+ columns, got " + parts.length);
                    continue;
                }
                try {
                    Question q = new Question();
                    q.setSubject(clean(parts[0]));
                    q.setTopic(clean(parts[1]));
                    q.setDifficulty(clean(parts[2]).toUpperCase());
                    q.setQuestionText(clean(parts[3]));
                    q.setOptionA(clean(parts[4]));
                    q.setOptionB(clean(parts[5]));
                    q.setOptionC(clean(parts[6]));
                    q.setOptionD(clean(parts[7]));
                    q.setCorrectAnswer(clean(parts[8]).toUpperCase());
                    if (parts.length > 9)  q.setExplanationA(clean(parts[9]));
                    if (parts.length > 10) q.setExplanationB(clean(parts[10]));
                    if (parts.length > 11) q.setExplanationC(clean(parts[11]));
                    if (parts.length > 12) q.setExplanationD(clean(parts[12]));
                    questionRepository.save(q);
                    imported++;
                } catch (Exception e) {
                    errors.add("Line " + lineNum + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Failed to read CSV: " + e.getMessage()));
        }

        return ResponseEntity.ok(Map.of("imported", imported, "errors", errors));
    }

    // ── User Management ──────────────────────────────────────────────────────

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(
            @RequestHeader(value = "X-Admin-Token", required = false) String token) {
        if (!isAdmin(token)) return unauthorized();

        List<Map<String, Object>> users = userRepository.findAll().stream().map(u -> {
            List<QuizAttempt> attempts = attemptRepository.findByUserIdOrderByAttemptedAtDesc(u.getId());
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",            u.getId());
            m.put("fullName",      u.getFullName());
            m.put("email",         u.getEmail());
            m.put("role",          u.getRole());
            m.put("emailVerified", u.isEmailVerified());
            m.put("totalAttempts", attempts.size());
            m.put("avgScore", attempts.isEmpty() ? 0 :
                Math.round(attempts.stream().mapToDouble(QuizAttempt::getPercentage)
                    .average().orElse(0) * 10.0) / 10.0);
            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(users);
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<?> updateUserRole(
            @RequestHeader(value = "X-Admin-Token", required = false) String token,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        if (!isAdmin(token)) return unauthorized();

        String newRole = body.get("role");
        if (!List.of("ROLE_USER", "ROLE_ADMIN").contains(newRole)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid role."));
        }

        return userRepository.findById(id).map(u -> {
            u.setRole(newRole);
            userRepository.save(u);
            return ResponseEntity.ok(Map.of("message", "Role updated."));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(
            @RequestHeader(value = "X-Admin-Token", required = false) String token,
            @PathVariable Long id) {
        if (!isAdmin(token)) return unauthorized();
        if (!userRepository.existsById(id)) return ResponseEntity.notFound().build();
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "User deleted."));
    }

    // ── Attempt Monitoring ───────────────────────────────────────────────────

    @GetMapping("/attempts")
    public ResponseEntity<?> getAttempts(
            @RequestHeader(value = "X-Admin-Token", required = false) String token,
            @RequestParam(required = false) Long   userId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        if (!isAdmin(token)) return unauthorized();

        List<QuizAttempt> attempts;
        if (userId != null) {
            attempts = attemptRepository.findByUserIdOrderByAttemptedAtDesc(userId);
        } else {
            attempts = attemptRepository.findAll();
            attempts.sort(Comparator.comparing(QuizAttempt::getAttemptedAt).reversed());
        }

        if (from != null) {
            LocalDateTime fromDate = LocalDateTime.parse(from + "T00:00:00");
            attempts = attempts.stream()
                .filter(a -> !a.getAttemptedAt().isBefore(fromDate))
                .collect(Collectors.toList());
        }
        if (to != null) {
            LocalDateTime toDate = LocalDateTime.parse(to + "T23:59:59");
            attempts = attempts.stream()
                .filter(a -> !a.getAttemptedAt().isAfter(toDate))
                .collect(Collectors.toList());
        }

        List<Map<String, Object>> result = attempts.stream().map(a -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",             a.getId());
            m.put("userId",         a.getUser().getId());
            m.put("userName",       a.getUser().getFullName());
            m.put("userEmail",      a.getUser().getEmail());
            m.put("mode",           a.getMode());
            m.put("difficulty",     a.getDifficulty());
            m.put("questionCount",  a.getQuestionCount());
            m.put("correctAnswers", a.getCorrectAnswers());
            m.put("percentage",     a.getPercentage());
            m.put("passed",         a.isPassed());
            m.put("weightedScore",  a.getWeightedScore());
            m.put("attemptedAt",    a.getAttemptedAt().toString());
            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // ── Leaderboard ──────────────────────────────────────────────────────────

    @GetMapping("/leaderboard")
    public ResponseEntity<?> getAdminLeaderboard(
            @RequestHeader(value = "X-Admin-Token", required = false) String token) {
        if (!isAdmin(token)) return unauthorized();

        List<Object[]> rows = attemptRepository.findLeaderboard();
        List<Map<String, Object>> result = rows.stream().map(row -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("userId",           row[0]);
            m.put("fullName",         row[1]);
            m.put("avgWeighted",      row[2]);
            m.put("bestWeighted",     row[3]);
            m.put("totalAttempts",    row[4]);
            m.put("avgPercentage",    row[5]);
            m.put("leaderboardScore", row[6]);
            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Map<String, Object> toQuestionMap(Question q) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",            q.getId());
        m.put("subject",       q.getSubject());
        m.put("topic",         q.getTopic());
        m.put("difficulty",    q.getDifficulty());
        m.put("questionText",  q.getQuestionText());
        m.put("optionA",       q.getOptionA());
        m.put("optionB",       q.getOptionB());
        m.put("optionC",       q.getOptionC());
        m.put("optionD",       q.getOptionD());
        m.put("correctAnswer", q.getCorrectAnswer());
        m.put("explanationA",  q.getExplanationA());
        m.put("explanationB",  q.getExplanationB());
        m.put("explanationC",  q.getExplanationC());
        m.put("explanationD",  q.getExplanationD());
        m.put("timesAnswered", q.getTimesAnswered());
        m.put("timesCorrect",  q.getTimesCorrect());
        m.put("accuracyRate",  Math.round(q.getAccuracyRate() * 10.0) / 10.0);
        m.put("flagged",       q.isFlagged());
        return m;
    }

    private Question buildQuestionFromBody(Question q, Map<String, String> body) {
        if (body.containsKey("subject"))       q.setSubject(body.get("subject"));
        if (body.containsKey("topic"))         q.setTopic(body.get("topic"));
        if (body.containsKey("difficulty"))    q.setDifficulty(body.get("difficulty").toUpperCase());
        if (body.containsKey("questionText"))  q.setQuestionText(body.get("questionText"));
        if (body.containsKey("optionA"))       q.setOptionA(body.get("optionA"));
        if (body.containsKey("optionB"))       q.setOptionB(body.get("optionB"));
        if (body.containsKey("optionC"))       q.setOptionC(body.get("optionC"));
        if (body.containsKey("optionD"))       q.setOptionD(body.get("optionD"));
        if (body.containsKey("correctAnswer")) q.setCorrectAnswer(body.get("correctAnswer").toUpperCase());
        if (body.containsKey("explanationA"))  q.setExplanationA(body.get("explanationA"));
        if (body.containsKey("explanationB"))  q.setExplanationB(body.get("explanationB"));
        if (body.containsKey("explanationC"))  q.setExplanationC(body.get("explanationC"));
        if (body.containsKey("explanationD"))  q.setExplanationD(body.get("explanationD"));
        return q;
    }

    private String clean(String s) {
        return s == null ? "" : s.trim().replaceAll("^\"|\"$", "");
    }
}