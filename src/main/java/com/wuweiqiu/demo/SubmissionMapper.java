package com.wuweiqiu.demo;

import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 提交表（submission）的数据操作接口。
 * 记录「哪个学生、对哪个作业、交了啥、啥时候交的、得了多少分」。
 */
@Mapper
public interface SubmissionMapper {

    // 学生提交作业：插入一条提交记录
    @Insert("INSERT INTO submission(homework_id, student, content, submit_time, file_name) VALUES(#{homeworkId}, #{student}, #{content}, #{submitTime}, #{fileName})")
    void submit(@Param("homeworkId") int homeworkId, @Param("student") String student,
                @Param("content") String content, @Param("submitTime") String submitTime,
                @Param("fileName") String fileName);

    // 查所有提交记录（老师用来查看 + 评分）
    @Select("SELECT id, homework_id, student, content, submit_time, score FROM submission")
    List<Submission> findAll();

    // 给某条提交打分：按提交记录的 id，更新 score 字段
    @Update("UPDATE submission SET score = #{score} WHERE id = #{id}")
    void updateScore(@Param("id") int id, @Param("score") int score);

    @Select("select student from submission where homework_id=#{homeworkId}")
    List<String> findStudentsByHomeworkId(@Param("homeworkId") int homeworkId);

    //查某个学生的所有提交
    @Select("SELECT id, homework_id, student, content, submit_time, score FROM submission WHERE student = #{student}")
    List<Submission> findByStudent(@Param("student") String student);

}
