package edu.cuhk.languee;

import java.util.List;

public class QuizSet {

    private String setID;
    private String title;

    private int questionCount;
    private int correctCount;
    private int sourceCount;
    private long datetime;

    private List<String> sources;
    private List<String> questions;
    private List<String> correctAnswers;
    private List<String> userAnswers;



    public QuizSet() {
        this.setID = "";
        this.title = "";
        this.sources = null;
        this.questionCount = 0;
        this.correctCount = 0;
        this.sourceCount = 0;
        this.datetime = 0;
        this.questions = null;
        this.correctAnswers = null;
        this.userAnswers = null;
    }

    public QuizSet(
            String setID, String title, List<String> sources,
            int questionCount, int correctCount, int sourceCount, long datetime,
            List<String> questions, List<String> correctAnswers, List<String> userAnswers) {
        this.setID = setID;
        this.title = title;
        this.sources = sources;
        this.questionCount = questionCount;
        this.correctCount = correctCount;
        this.sourceCount = sourceCount;
        this.datetime = datetime;
        this.questions = questions;
        this.correctAnswers = correctAnswers;
        this.userAnswers = userAnswers;
    }

    public String getSetID() {
        return setID;
    }

    public void setSetID(String setID) {
        this.setID = setID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getSources() {
        return sources;
    }

    public void setSources(List<String> sources) {
        this.sources = sources;
    }

    public int getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(int questionCount) {
        this.questionCount = questionCount;
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public void setCorrectCount(int correctCount) {
        this.correctCount = correctCount;
    }

    public int getSourceCount() {
        return sourceCount;
    }

    public void setSourceCount(int sourceCount) {
        this.sourceCount = sourceCount;
    }

    public long getDatetime() {
        return datetime;
    }

    public void setDatetime(long datetime) {
        this.datetime = datetime;
    }

    public List<String> getQuestions() {
        return questions;
    }

    public void setQuestions(List<String> questions) {
        this.questions = questions;
    }

    public List<String> getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(List<String> correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public List<String> getUserAnswers() {
        return userAnswers;
    }

    public void setUserAnswers(List<String> userAnswers) {
        this.userAnswers = userAnswers;
    }


}
