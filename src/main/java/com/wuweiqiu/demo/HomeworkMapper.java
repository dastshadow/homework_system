package com.wuweiqiu.demo;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;

import java.util.List;
@Mapper
public interface HomeworkMapper {
    @Select("select id,name,teacher,start_time,end_time from homework")
    List<Homework> findAll();

    @Insert("insert into homework(name,teacher,start_time,end_time) values(#{name},#{teacher},#{startTime},#{endTime})")
    void add(@Param("name") String name,@Param("teacher") String teacher,@Param("startTime") String startTime,@Param("endTime") String endTime);

    @Delete("delete from homework where id = #{id}")
    void delete(int id);

    @Update("UPDATE homework SET name = #{name} where id = #{id}")
    void update(@Param("id") int id,@Param("name") String name);
}
