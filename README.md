# 作业管理系统（Homework System）

[作者：run喉糖],[邮箱：dustshadow233@163.com]
**该项目是作者本人大二上的第一份全栈项目，前端做到后面的时候偷了点懒，AI 生成的代码，所以页面可能有点ai味太重了，但是如果没有接管的话前端页面又太简陋了，前面有好几个是一点点写的，丑到我不忍直视。希望该项目能帮到在做的各位，有任何意见或建议都可以发邮箱。暂时收笔时间为2026年11月20日。**
这是一个前后端分离的在线作业管理系统，支持教师/学生两种角色。教师发布作业、管理学生、评分；学生选课（选老师）、提交作业（含附件）、查看分数。集成了 **DeepSeek AI 题库检查** 和 **Redis 缓存/黑名单/限流**，已部署上线、公网可访问。

> 技术栈：Spring Boot 4 + MyBatis + MySQL + Redis + JWT + 原生 HTML/JS（Bootstrap）

---

## 一、功能特性

### 用户与认证
- 注册 / 登录（密码 **BCrypt 加密**存储，不存明文）
- **JWT** 令牌认证，登录后签发 token，拦截器统一校验
- **注销黑名单**：退出登录后 token 立即失效（Redis 黑名单）
- **限流**：每个用户 60 秒最多 N 次请求（Redis 计数器 + 429）

### 角色权限（RBAC）
- 教师 / 学生两种角色，注册默认学生，教师账号手动提升（防"自封教师"）
- 前端按角色显示不同功能 + **后端拦截器强制校验**（学生越权返回 403，未登录 401）
- API Key 用 **AES 可逆加密**存储（区别于密码的 BCrypt 单向加密）

### 作业管理（教师）
- 增删改查、批量删除、导出 CSV
- 作业归属老师、开始/截止时间、**逾期标红提醒**
- 作业可附**作业要求文档**（附件上传 + 下载）

### 师生关系（多对多）
- 学生可选**多个**老师，老师也可主动选学生（双向选择，关联表建模）
- 学生只看自己老师的作业，老师只看自己学生 + 自己布置的作业

### 学生提交与评分
- 学生提交作业（文本框 + 附件），提交后可在"我的提交"查看**分数**
- 教师查看提交列表（**JOIN 显示作业名**）、单个/批量评分
- 作业完成情况查询（谁交了谁没交，点行展开）

### AI 题库检查（DeepSeek）
- 教师粘贴题目，AI 检查"答案与选项是否匹配、公式是否乱码、格式是否规范"
- 每个教师用**自己的 DeepSeek API Key**（存数据库，AES 加密），不共用

### Redis 应用（三个经典场景）
1. **缓存**：作业列表缓存（Cache Aside 模式——读缓存 + 写删缓存）
2. **黑名单**：注销后 token 立即失效
3. **限流**：防刷接口

---

## 二、技术栈

| 分类 | 技术 |
|---|---|
| 后端框架 | Spring Boot 4.1.1 |
| 持久层 | MyBatis（注解方式 + 驼峰映射） |
| 数据库 | MySQL |
| 缓存 | Redis（StringRedisTemplate） |
| 认证 | JWT（jjwt 0.12.6）+ BCrypt（spring-security-crypto） |
| 加密 | AES（API Key 可逆加密）、BCrypt（密码单向加密） |
| AI | DeepSeek Chat API（HttpClient + Jackson） |
| 前端 | 原生 HTML/CSS/JS + Bootstrap 5（CDN） |
| 部署 | 腾讯云 Ubuntu + Nginx 反向代理 + 域名 |

---

## 三、系统架构（分层）

```
浏览器（index.html）
   ↓ HTTP 请求（带 JWT token）
拦截器（LoginInterceptor）—— 验 token + 查黑名单 + 限流 + 权限校验
   ↓ 放行
Controller（HelloController）—— 接收请求、调用 Mapper
   ↓
Mapper（MyBatis）—— 执行 SQL
   ↓
MySQL（持久化） + Redis（缓存/黑名单/限流） + DeepSeek（AI）
```

---

## 四、快速开始

### 1. 环境要求
- JDK 17+（开发环境使用 JDK 21）
- MySQL 5.7+ / 8.0+
- Redis（Windows 可用 `tporadowski/redis` 免安装版，Linux 用 `apt install redis-server`）

### 2. 初始化数据库

```bash
mysql -u root -p < init.sql
```

### 3. 配置
### 这点非常重要，不然会导致很多功能用不了
在 `src/main/resources/application.properties` 填写：

```properties
# 数据库
spring.datasource.url=jdbc:mysql://localhost:3306/homework_system?serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=你的密码

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379

# 文件上传目录（本地）
upload.dir=D:/你的路径/uploads

# AES 密钥（16 字节，加密 API Key 用，自己换成随机值）
api-key.aes-secret=你的16位随机密钥
```

> 注意：`application.properties` 含敏感信息，已加入 `.gitignore`，不会提交到仓库。

### 4. 启动

```bash
mvn spring-boot:run
# 或打包后运行
mvn package -DskipTests
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

访问 `http://localhost:8080/index.html`

---

## 五、项目亮点（面试可讲）

1. **完整的认证体系**：JWT 签发/校验 + BCrypt 密码加密 + AES 密钥加密 + 注销黑名单 + 限流
2. **RBAC 权限**：前端按角色渲染 + 后端拦截器强制校验（401 未登录 / 403 越权）
3. **Redis 三大场景**：缓存（Cache Aside 缓存一致性）、黑名单、限流
4. **多对多关系建模**：师生关系用关联表，而非字段拼逗号
5. **AI 集成**：调用 DeepSeek API，且每个用户用自己的 key（AES 加密存储）
6. **文件上传/下载**：MultipartFile + FormData + 时间戳防覆盖 + 带 token 的安全下载
7. **前后端分离**：原生 fetch + JSON 接口，后端返回结构化数据

---

## 六、目录结构

```
demo/
├── src/main/java/com/wuweiqiu/demo/
│   ├── DemoApplication.java          # 启动类
│   ├── HelloController.java          # 接口层（所有 Controller）
│   ├── LoginInterceptor.java         # 拦截器（认证+权限+黑名单+限流）
│   ├── WebConfig.java                # 配置类（注册拦截器）
│   ├── Homework.java                 # 作业实体类
│   ├── Submission.java               # 提交实体类
│   ├── HomeworkMapper.java           # 作业 Mapper
│   ├── UserMapper.java               # 用户 Mapper
│   ├── SubmissionMapper.java         # 提交 Mapper
│   └── StudentTeacherMapper.java     # 师生关系 Mapper
├── src/main/resources/
│   ├── application.properties        # 配置（gitignore，不入库）
│   └── static/index.html             # 前端页面
├── init.sql                          # 数据库初始化脚本
└── pom.xml
```

---

## 七、说明

- 本项目为**个人学习项目**，从命令行 CRUD 一路演进到完整的上线系统，记录了 Java 后端的完整学习路径。
- 前端使用原生 HTML/JS + Bootstrap，未使用 Vue/React，重点在后端能力的完整覆盖。
- 部署于腾讯云轻量服务器 + Nginx 反向代理，公网可访问。
