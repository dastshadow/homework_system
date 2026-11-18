package com.wuweiqiu.demo;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;
/**
 * 用户表（user）的数据操作接口。
 * 和 HomeworkMapper 一样：只有方法声明 + SQL，实现由 MyBatis 自动生成。
 */
@Mapper
public interface UserMapper {

    // 注册：往 user 表插入一条（用户名 + 加密后的密码 + 角色）
    @Insert("insert into user(username,password,role) values(#{username},#{password},#{role})")
    void register(@Param("username") String username, @Param("password") String password, @Param("role") String role);

    // 按用户名查密码（返回的是 BCrypt 加密后的密文）。查不到时返回 null。
    @Select("select password from user where username = #{username}")
    String findPasswordByUsername(@Param("username") String username);

    // 按用户名查角色（student / teacher）
    @Select("select role from user where username = #{username}")
    String findRoleByUsername(@Param("username") String username);

    @Select("select username from user where role='student'")
    List<String> findAllStudentNames();

    // 查某个学生选了哪个老师（返回老师用户名，没选则 null）
    @Select("select teacher from user where username = #{username}")
    String findTeacherByUsername(@Param("username") String username);

    // 学生选老师：把该学生的 teacher 字段更新成老师的用户名
    @Update("update user set teacher = #{teacher} where username = #{username}")
    void chooseTeacher(@Param("username") String username, @Param("teacher") String teacher);

    // 查某个老师名下的所有学生（选了该老师的学生用户名列表）
    @Select("select username from user where teacher = #{teacher} and role = 'student'")
    List<String> findStudentsByTeacher(@Param("teacher") String teacher);

    // 保存老师的 DeepSeek API Key
    @Update("update user set api_key = #{apiKey} where username = #{username}")
    void saveApiKey(@Param("username") String username, @Param("apiKey") String apiKey);

    // 查某个用户的 API Key（没设置则 null）
    @Select("select api_key from user where username = #{username}")
    String findApiKeyByUsername(@Param("username") String username);
}
