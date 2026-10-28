-- 建库
CREATE DATABASE IF NOT EXISTS homework_system;
USE homework_system;

-- 建表
CREATE TABLE IF NOT EXISTS homework (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS user (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50),
    password VARCHAR(100),
    role VARCHAR(20)
);

-- 初始数据（测试用）
INSERT INTO homework(name) VALUES('示例作业1');
INSERT INTO homework(name) VALUES('示例作业2');
