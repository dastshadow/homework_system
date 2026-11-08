package com.wuweiqiu.demo;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
}
