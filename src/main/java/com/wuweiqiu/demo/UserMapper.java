package com.wuweiqiu.demo;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
@Mapper
public interface UserMapper {
    @Insert("insert into user(username,password) values(#{username},#{password}) ")
    void register(@Param("username") String username,@Param("password") String password);

    @Select("select password from user where username = #{username}")
    String findPasswordByUsername(@Param("username") String username);
}
