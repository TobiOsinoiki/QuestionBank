package com.tobi.qbank.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "question")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String subject;
    private String topic;
    private String difficulty;

    @Column(name = "question_text", columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "option_a")
    private String optionA;

    @Column(name = "option_b")
    private String optionB;

    @Column(name = "option_c")
    private String optionC;

    @Column(name = "option_d")
    private String optionD;

    @Column(name = "correct_answer")
    private String correctAnswer;

    public Question() {}

    public Long getId() { return id; }
    public String getSubject() { return subject; }
    public String getTopic() { return topic; }
    public String getDifficulty() { return difficulty; }
    public String getQuestionText() { return questionText; }
    public String getOptionA() { return optionA; }
    public String getOptionB() { return optionB; }
    public String getOptionC() { return optionC; }
    public String getOptionD() { return optionD; }
    public String getCorrectAnswer() { return correctAnswer; }

    public void setId(Long id) { this.id = id; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setTopic(String topic) { this.topic = topic; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public void setOptionA(String optionA) { this.optionA = optionA; }
    public void setOptionB(String optionB) { this.optionB = optionB; }
    public void setOptionC(String optionC) { this.optionC = optionC; }
    public void setOptionD(String optionD) { this.optionD = optionD; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
}
