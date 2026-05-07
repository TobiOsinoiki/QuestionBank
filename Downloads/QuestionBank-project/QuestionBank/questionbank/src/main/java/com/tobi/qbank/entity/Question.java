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

   
    @Column(nullable = false)
    private int timesAnswered = 0;

    @Column(nullable = false)
    private int timesCorrect = 0;


    @Column(nullable = false)
    private boolean flagged = false;
    
    @Column(name = "explanation_a", columnDefinition = "TEXT")
    private String explanationA;

    @Column(name = "explanation_b", columnDefinition = "TEXT")
    private String explanationB;

    @Column(name = "explanation_c", columnDefinition = "TEXT")
    private String explanationC;

    @Column(name = "explanation_d", columnDefinition = "TEXT")
    private String explanationD;

    
    public Question() {}

    public Long getId()                            { return id; }
    public String getSubject()                     { return subject; }
    public String getTopic()                       { return topic; }
    public String getDifficulty()                  { return difficulty; }
    public String getQuestionText()                { return questionText; }
    public String getOptionA()                     { return optionA; }
    public String getOptionB()                     { return optionB; }
    public String getOptionC()                     { return optionC; }
    public String getOptionD()                     { return optionD; }
    public String getCorrectAnswer()               { return correctAnswer; }
//    public String getExplanation()                 { return explanation; }
   public int getTimesAnswered()                  { return timesAnswered; }
    public int getTimesCorrect()                   { return timesCorrect; }
    public boolean isFlagged()                     { return flagged; }

    public void setId(Long id)                     { this.id = id; }
    public void setSubject(String subject)         { this.subject = subject; }
    public void setTopic(String topic)             { this.topic = topic; }
    public void setDifficulty(String difficulty)   { this.difficulty = difficulty; }
    public void setQuestionText(String t)          { this.questionText = t; }
    public void setOptionA(String optionA)         { this.optionA = optionA; }
    public void setOptionB(String optionB)         { this.optionB = optionB; }
    public void setOptionC(String optionC)         { this.optionC = optionC; }
    public void setOptionD(String optionD)         { this.optionD = optionD; }
    public void setCorrectAnswer(String c)         { this.correctAnswer = c; }
    //public void setExplanation(String explanation) { this.explanation = explanation; }
    public void setTimesAnswered(int n)            { this.timesAnswered = n; }
    public void setTimesCorrect(int n)             { this.timesCorrect = n; }
    public void setFlagged(boolean flagged)        { this.flagged = flagged; }
    public String getExplanationA() { return explanationA; }
    public String getExplanationB() { return explanationB; }
    public String getExplanationC() { return explanationC; }
    public String getExplanationD() { return explanationD; }

    public void setExplanationA(String s) { this.explanationA = s; }
    public void setExplanationB(String s) { this.explanationB = s; }
    public void setExplanationC(String s) { this.explanationC = s; }
    public void setExplanationD(String s) { this.explanationD = s; }

    public String getExplanationFor(String letter) {
        if (letter == null) return null;
        return switch (letter.toUpperCase()) {
            case "A" -> explanationA;
            case "B" -> explanationB;
            case "C" -> explanationC;
            case "D" -> explanationD;
            default  -> null;
        };
    }
    public double getAccuracyRate() {
        return timesAnswered == 0 ? 0 : (timesCorrect * 100.0) / timesAnswered;
    }
}