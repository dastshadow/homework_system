package com.wuweiqiu.demo;

public class Homework {
    private int id;
    private String name;
    private String teacher;
    private String startTime;
    private String endTime;

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id=id;
    }

    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }

    public String getTeacher(){
        return teacher;
    }
    public void setTeacher(String teacher){
        this.teacher=teacher;
    }

    public String getStartTime(){
        return startTime;
    }
    public void setStartTime(String startTime){
        this.startTime=startTime;
    }

    public String getEndTime(){
        return endTime;
    }
    public void setEndTime(String endTime){
        this.endTime=endTime;
    }
}
