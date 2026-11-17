-- ============ 作业管理系统 · 数据库初始化脚本 ============
-- 用法：mysql -u root -p < init.sql
-- 注意：脚本里是 IF NOT EXISTS，重复执行不会报错；
--       要「彻底重建」，先 DROP DATABASE homework_system; 再执行本脚本。

CREATE DATABASE IF NOT EXISTS homework_system;
USE homework_system;

-- 作业表：一条作业
CREATE TABLE IF NOT EXISTS homework (
    id INT PRIMARY KEY AUTO_INCREMENT,   -- 主键，唯一标识一条作业
    name VARCHAR(100),                   -- 作业名
    teacher VARCHAR(50),                 -- 布置老师（用户名）
    start_time VARCHAR(50),              -- 开始时间（发布日期）
    end_time VARCHAR(50)                 -- 截止时间
);

-- 用户表：学生 / 教师
CREATE TABLE IF NOT EXISTS user (
    id INT PRIMARY KEY AUTO_INCREMENT,   -- 主键
    username VARCHAR(50),                -- 用户名
    password VARCHAR(100),               -- 密码（BCrypt 加密后的密文）
    role VARCHAR(20),                    -- 角色：student / teacher
    teacher VARCHAR(50)                  -- 学生选了哪个老师（老师用户名；老师角色不用）
);

-- 提交表：学生提交记录
CREATE TABLE IF NOT EXISTS submission (
    id INT PRIMARY KEY AUTO_INCREMENT,   -- 主键，一条提交
    homework_id INT,                     -- 对应哪个作业（homework 表的 id）
    student VARCHAR(50),                 -- 哪个学生提交的（用户名）
    content TEXT,                        -- 提交内容
    submit_time VARCHAR(50),             -- 提交时间
    score INT,                           -- 得分（老师评分后才有）
    file_name VARCHAR(200)               -- 附件文件名（时间戳_原名，可为空）
);
