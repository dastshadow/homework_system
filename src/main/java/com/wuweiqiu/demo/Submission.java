package com.wuweiqiu.demo;

public class Submission {
    private int id;
    private int homeworkId;
    private String student;
    private String content;
    private String submitTime;
    private int score;
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getHomeworkId() { return homeworkId; }
    public void setHomeworkId(int homeworkId) { this.homeworkId = homeworkId; }

    public String getStudent() { return student; }
    public void setStudent(String student) { this.student = student; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getSubmitTime() { return submitTime; }
    public void setSubmitTime(String submitTime) { this.submitTime = submitTime; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
}
