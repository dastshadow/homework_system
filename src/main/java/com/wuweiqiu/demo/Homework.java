package com.wuweiqiu.demo;

/**
 * 实体类：对应数据库的 homework 表。
 *
 * 它是「数据库表」和「Java 对象」之间的桥梁：
 *  - 表的每一列（id、name、teacher...）对应这里的一个字段
 *  - 表的一行数据，对应一个 Homework 对象
 *
 * 字段是 private（私有），外面不能直接访问，只能通过 getter/setter 读写。
 * 这堆 getter/setter 看起来啰嗦，但很重要：
 *  - Jackson（返回 JSON 时）靠 getXxx 读取字段值
 *  - MyBatis（查询映射时）靠 setXxx 把数据库的值塞进对象
 * 没有它们，会出「返回空对象 {}」的问题。
 */
public class Homework {
    private int id;           // 数据库主键，唯一标识一条作业
    private String name;      // 作业名
    private String teacher;   // 布置老师（用户名）
    private String startTime; // 开始时间（发布日期）
    private String endTime;   // 截止时间
    private String fileName;   // 作业要求的附件文件名（可为空）

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTeacher() { return teacher; }
    public void setTeacher(String teacher) { this.teacher = teacher; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
}
