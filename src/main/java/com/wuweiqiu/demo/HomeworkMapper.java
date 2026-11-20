package com.wuweiqiu.demo;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 作业表（homework）的数据操作接口 —— 也就是「Mapper」。
 *
 * 关键理解：这是一个「接口」，只有方法声明 + SQL 注解，没有实现类。
 * 真正的实现是 MyBatis 在运行时自动生成的（它看到 @Select 等注解，就知道怎么执行 SQL）。
 * 所以 Controller 里 @Autowired 注入的，就是 MyBatis 帮我们生成的那个实现。
 *
 * 每个方法对应的注解：
 *  @Select  = 查询（SELECT）
 *  @Insert  = 新增（INSERT）
 *  @Delete  = 删除（DELETE）
 *  @Update  = 修改（UPDATE）
 * SQL 里的 #{xxx} 是「占位符」，会自动对应到方法的参数（防 SQL 注入）。
 */
@Mapper   // 告诉 MyBatis：这是一个 Mapper 接口，扫描我
public interface HomeworkMapper {

    // 查所有作业。返回 List<Homework>，MyBatis 自动把每行数据装进 Homework 对象。
    @Select("select id,name,teacher,start_time,end_time,file_name from homework")
    List<Homework> findAll();

    // 查某个老师布置的所有作业（学生看作业时按老师过滤）
    @Select("select id, name, teacher, start_time, end_time, file_name from homework where teacher = #{teacher}")
    List<Homework> findByTeacher(@Param("teacher") String teacher);

    // 新增一条作业。#{name}、#{teacher} 等占位符，对应下面 @Param 标注的参数。
    @Insert("insert into homework(name,teacher,start_time,end_time,file_name) values(#{name},#{teacher},#{startTime},#{endTime},#{fileName})")
    void add(@Param("name") String name, @Param("teacher") String teacher,
             @Param("startTime") String startTime, @Param("endTime") String endTime,
             @Param("fileName") String fileName);

    // 按 id 删除。只有一个参数时，可以不写 @Param（MyBatis 自动对应 #{id}）。
    @Delete("delete from homework where id = #{id}")
    void delete(int id);

    // 按 id 修改作业名。
    @Update("UPDATE homework SET name = #{name} where id = #{id}")
    void update(@Param("id") int id, @Param("name") String name);


}
