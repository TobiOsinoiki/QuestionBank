package com.tobi.qbank.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_attempt")
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String mode; // EXAM, PRACTICE, STUDY

    @Column(nullable = false)
    private String difficulty; // EASY, MEDIUM, HARD

    @Column(nullable = false)
    private int questionCount;

    @Column(nullable = false)
    private int correctAnswers;

    @Column(nullable = false)
    private double percentage;

    @Column(nullable = false)
    private boolean passed;

    // Weighted score: correct * difficulty multiplier
    @Column(nullable = false)
    private double weightedScore;

    @Column(nullable = false)
    private LocalDateTime attemptedAt;

    public QuizAttempt() {}

    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getMode() { return mode; }
    public String getDifficulty() { return difficulty; }
    public int getQuestionCount() { return questionCount; }
    public int getCorrectAnswers() { return correctAnswers; }
    public double getPercentage() { return percentage; }
    public boolean isPassed() { return passed; }
    public double getWeightedScore() { return weightedScore; }
    public LocalDateTime getAttemptedAt() { return attemptedAt; }

    public void setId(Long id) { this.id = id; }
    public void setUser(User user) { this.user = user; }
    public void setMode(String mode) { this.mode = mode; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public void setQuestionCount(int questionCount) { this.questionCount = questionCount; }
    public void setCorrectAnswers(int correctAnswers) { this.correctAnswers = correctAnswers; }
    public void setPercentage(double percentage) { this.percentage = percentage; }
    public void setPassed(boolean passed) { this.passed = passed; }
    public void setWeightedScore(double weightedScore) { this.weightedScore = weightedScore; }
    public void setAttemptedAt(LocalDateTime attemptedAt) { this.attemptedAt = attemptedAt; }
}
