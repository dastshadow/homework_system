package com.wuweiqiu.demo;

/**
 * 实体类：对应数据库的 submission 表（提交记录）。
 * 一行提交 = 一个 Submission 对象。
 */
public class Submission {
    private int id;            // 提交记录的主键
    private int homeworkId;    // 对应哪个作业（homework 表的 id）
    private String student;    // 哪个学生提交的
    private String content;    // 提交的内容
    private String submitTime; // 提交时间
    private int score;         // 得分（老师评分后才有）
    private String fileName;   // 上传的附件名

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

    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
