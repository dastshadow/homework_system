package com.wuweiqiu.demo;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface StudentTeacherMapper {

    // 学生选老师：往关联表插入一条记录
    @Insert("insert into student_teacher(student, teacher) values(#{student}, #{teacher})")
    void addRelation(@Param("student") String student, @Param("teacher") String teacher);

    // 查学生选了哪些老师（返回老师用户名列表）
    @Select("select teacher from student_teacher where student = #{student}")
    List<String> findTeachersByStudent(@Param("student") String student);

    // 查老师有哪些学生（返回学生用户名列表）
    @Select("select student from student_teacher where teacher = #{teacher}")
    List<String> findStudentsByTeacher(@Param("teacher") String teacher);

    // 查学生是否已经选了某老师（用于防重复选）
    @Select("select count(*) from student_teacher where student = #{student} and teacher = #{teacher}")
    int countRelation(@Param("student") String student, @Param("teacher") String teacher);
}
