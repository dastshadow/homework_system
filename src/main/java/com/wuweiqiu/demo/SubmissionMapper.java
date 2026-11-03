package com.wuweiqiu.demo;

import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
@Mapper
public interface SubmissionMapper {

    @Insert("INSERT INTO submission(homework_id, student, content, submit_time) VALUES(#{homeworkId}, #{student}, #{content}, #{submitTime})")
    void submit(@Param("homeworkId") int homeworkId, @Param("student") String student,
                @Param("content") String content, @Param("submitTime") String submitTime);

    @Select("SELECT id, homework_id, student, content, submit_time, score FROM submission")
    List<Submission> findAll();

    @Update("UPDATE submission SET score = #{score} WHERE id = #{id}")
    void updateScore(@Param("id") int id, @Param("score") int score);
}
