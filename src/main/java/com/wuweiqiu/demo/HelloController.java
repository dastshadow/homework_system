package com.wuweiqiu.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.ArrayNode;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

import org.springframework.web.multipart.MultipartFile;
import java.io.File;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;

import org.springframework.data.redis.core.StringRedisTemplate;
import java.time.Duration;

import jakarta.servlet.http.HttpServletRequest;
/**
 * 作业管理系统的「接口层」（Controller）。
 *
 * 这个类是整个后端的入口：浏览器/Postman 发来的 HTTP 请求，都先到这里，
 * 再由它调用 Mapper（数据库操作）去真正干活。
 *
 * 关键注解：
 *  - @RestController：告诉 Spring「这个类负责处理 HTTP 请求，并直接返回数据」。
 *  - @GetMapping / @PostMapping / @PutMapping / @DeleteMapping：
 *    分别对应 HTTP 的「查 / 增 / 改 / 删」四种操作。
 */
@RestController
public class HelloController {

    // ===== 下面是「依赖注入」的字段 =====
    // @Autowired = 让 Spring 自动把实现类塞进来，不用自己 new。
    // 这些 Mapper 接口本身没有实现类，实现是 MyBatis 在运行时自动生成的。

    @Autowired
    private HomeworkMapper homeworkMapper;      // 作业表的数据操作

    @Autowired
    private UserMapper userMapper;              // 用户表的数据操作

    @Autowired
    private SubmissionMapper submissionMapper;  // 提交表的数据操作

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    // @Value = 从 application.properties 里读出配置项的值。
    // "${deepseek.api-key}" 会被替换成配置文件里 deepseek.api-key= 后面的值（即 API Key）。
    @Value("${deepseek.api-key}")
    private String apiKey;

    @Value("${upload.dir}")
    private String uploadDir;
    // BCrypt 加密工具：注册时加密密码、登录时验证密码，都用它。
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 测试接口：访问 /hello 返回一句话，用来验证服务器能正常响应。
     */
    @GetMapping("/hello")
    public String hello() {
        return "Hello World";
    }

    /**
     * 查看所有作业。
     * 返回类型是 List<Homework>，Spring 会自动把它转成 JSON 数组给前端。
     */
    @GetMapping("/homework")
    public List<Homework> homework(@RequestAttribute String username) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // 先确定"这次要查哪个老师的作业"：学生 = 他选的老师；老师 = 他自己
        String role = userMapper.findRoleByUsername(username);
        String targetTeacher;
        if ("student".equals(role)) {
            targetTeacher = userMapper.findTeacherByUsername(username);
            if (targetTeacher == null) {
                return new ArrayList<>();   // 学生还没选老师 → 空列表
            }
        } else {
            targetTeacher = username;
        }

        // 缓存 key 用"目标老师"（老师和选他的学生共享同一份缓存）
        String cacheKey = "homework:list:" + targetTeacher;

        // 先查缓存
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return mapper.readValue(cached, new TypeReference<List<Homework>>() {});
        }

        // 未命中，查数据库
        List<Homework> list = homeworkMapper.findByTeacher(targetTeacher);

        // 写回缓存（1 分钟过期）
        stringRedisTemplate.opsForValue().set(cacheKey, mapper.writeValueAsString(list), Duration.ofMinutes(1));

        return list;
    }



    /**
     * 添加作业（老师用）。
     *
     * @param name     作业名（来自 URL 的 ?name=xxx）
     * @param endTime  截止时间（来自 URL 的 ?endTime=xxx）
     * @param username 布置老师 —— 由拦截器从 token 里取出、放进 request，
     *                 这里用 @RequestAttribute 把它拿出来
     */
    @PostMapping("/homework/add")
    public String add(@RequestParam String name, @RequestParam String endTime, @RequestAttribute String username) {
        String startTime = LocalDate.now().toString();
        if (endTime == null || endTime.isEmpty()) {
            endTime = LocalDate.now().plusDays(7).toString();
        }
        homeworkMapper.add(name, username, startTime, endTime);
        stringRedisTemplate.delete("homework:list:" + username);   // 数据变了，删缓存
        return "添加成功:" + name;
    }


    /**
     * 删除作业（老师用）。
     *
     * @param no 用户看到的「序号」（1、2、3……），不是数据库里的 id。
     */
    @DeleteMapping("/homework/{no}")
    public String delete(@PathVariable int no, @RequestAttribute String username) {
        List<Homework> list = homeworkMapper.findByTeacher(username);   // 改：按老师自己的作业定位（原来 findAll 是 bug）
        if (no < 1 || no > list.size()) {
            return "序号无效，请重新输入";
        }
        Homework target = list.get(no - 1);
        homeworkMapper.delete(target.getId());
        stringRedisTemplate.delete("homework:list:" + username);   // 删缓存
        return "删除成功:" + target.getName();
    }

    /**
     * 修改作业名（老师用）。
     *
     * @param no   要改的作业「序号」
     * @param name 新名字
     */
    @PutMapping("/homework/{no}")
    public String update(@PathVariable int no, @RequestParam String name, @RequestAttribute String username) {
        List<Homework> list = homeworkMapper.findByTeacher(username);   // 改：按老师自己的作业定位
        if (no < 1 || no > list.size()) {
            return "序号无效，请重新输入";
        }
        Homework target = list.get(no - 1);
        homeworkMapper.update(target.getId(), name);
        stringRedisTemplate.delete("homework:list:" + username);   // 删缓存
        return "修改成功:" + name;
    }


    /**
     * 注册。
     * 注意：角色写死为 "student" —— 任何人都只能注册成学生，
     * 教师账号要手动去数据库改，防止有人「自封教师」。
     */
    @PostMapping("/register")
    public String register(@RequestParam String username, @RequestParam String password) {
        // 密码先用 BCrypt 加密成乱码再存，数据库泄露也看不出明文。
        userMapper.register(username, passwordEncoder.encode(password), "student");
        return "注册成功:" + username;
    }

    /**
     * 登录。
     * 核对账号密码 → 对了就发一个 JWT 令牌（token），和角色一起返回。
     */
    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password) {
        // ① 按用户名查出库里存的密码（密文）。
        String dbPassword = userMapper.findPasswordByUsername(username);
        if (dbPassword == null) {
            return "用户不存在";
        }

        // ② matches(用户输入的明文, 库里的密文) —— BCrypt 验证密码对不对。
        if (passwordEncoder.matches(password, dbPassword)) {
            String role = userMapper.findRoleByUsername(username);   // 查出角色

            // ③ 生成 JWT：把用户名(subject)和角色(claim)装进令牌，设 24 小时过期。
            String secret = "this-is-my-very-long-secret-key-for-jwt-1234567890";
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            String token = Jwts.builder()
                    .subject(username)                                    // 令牌里装「用户名」
                    .claim("role", role)                                  // 令牌里装「角色」
                    .expiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24)) // 24 小时后过期
                    .signWith(key)                                        // 用密钥签名（防伪造）
                    .compact();

            // ④ 把 token 和角色用 | 拼一起返回，前端再拆开分别存。
            return token + "|" + role;
        } else {
            return "密码错误";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            // 把 token 加入黑名单，TTL 24 小时（= token 有效期，过期后黑名单也自动清理）
            stringRedisTemplate.opsForValue().set("blacklist:" + token, "1", Duration.ofHours(24));
        }
        return "已退出登录";
    }

    /**
     * AI 题库检查（老师用）。
     * 把用户粘贴的题目 + 一段「审核员 prompt」打包成 JSON，发给 DeepSeek，
     * 再把 AI 的回复从返回的 JSON 里抠出来返回。
     */
    @PostMapping("/check")
    public String check(@RequestParam String question) throws Exception {
        // ① 拼 prompt：告诉 AI「你是题库审核员，检查下面题目」。
        String prompt = "你是一位严谨的题库审核员。请检查下面的题目，重点检查：\n" +
                "1. 答案选项是否在选项中\n" +
                "2. 公式、特殊符号是否乱码或错位\n" +
                "3. 格式是否规范\n\n" +
                "题目内容：\n" + question + "\n\n" +
                "请逐条列出问题；没问题请回复未发现问题。";

        // ② 用 Jackson 构建请求体 JSON（别手动拼字符串，会因换行/引号出错）。
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode message = mapper.createObjectNode();   // 一条消息对象
        message.put("role", "user");
        message.put("content", prompt);
        ArrayNode messages = mapper.createArrayNode();    // 消息数组
        messages.add(message);
        ObjectNode body = mapper.createObjectNode();      // 整个请求体
        body.put("model", "deepseek-chat");
        body.set("messages", messages);
        String jsonBody = mapper.writeValueAsString(body);

        // ③ 用 HttpClient 把 JSON 发给 DeepSeek（带 Key 认证）。
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.deepseek.com/chat/completions"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // ④ 解析返回 JSON，按路径 choices → [0] → message → content 抠出 AI 回答。
        JsonNode root = mapper.readTree(response.body());
        return root.path("choices").get(0).path("message").path("content").stringValue();
    }

    /**
     * 学生提交作业。
     *
     * @param no       作业「序号」（学生看到列表里的序号）
     * @param content  提交的内容
     * @param username 提交人 —— 从 token 取出
     */
    @PostMapping("/submit")
    public String submit(@RequestParam int no,
                         @RequestParam String content,
                         @RequestParam(value = "file", required = false) MultipartFile file,
                         @RequestAttribute String username) throws Exception {
        // 学生提交，序号要按"自己老师的作业"定位（原来 findAll 会定位到所有作业，序号错位）
        String teacher = userMapper.findTeacherByUsername(username);
        if (teacher == null) {
            return "请先选择老师";
        }
        List<Homework> list = homeworkMapper.findByTeacher(teacher);
        if (no < 1 || no > list.size()) {
            return "序号无效，请重新输入";
        }
        Homework target = list.get(no - 1);
        String submitTime = LocalDate.now().toString();

        // 处理附件：传了文件就保存到 uploads/ 目录，记录文件名
        String fileName = null;
        if (file != null && !file.isEmpty()) {
            // 时间戳 + 原文件名，保证唯一不覆盖
            fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            file.transferTo(new File(dir, fileName));
        }
        submissionMapper.submit(target.getId(), username, content, submitTime, fileName);
        return "提交成功：" + target.getName();
    }

    /**
     * 查看所有提交记录（老师用）。
     */
    @GetMapping("/submissions")
    public List<Submission> submissions() {
        return submissionMapper.findAll();
    }
    @GetMapping("/submissions/by-student")
    public List<Submission> submissionsByStudent(@RequestParam String student){
        return submissionMapper.findByStudent(student);
    }
    /**
     * 评分（老师用）。
     *
     * @param id    提交记录的 id
     * @param score 分数
     */
    @PostMapping("/score")
    public String score(@RequestParam int id, @RequestParam int score) {
        submissionMapper.updateScore(id, score);
        return "评分成功";
    }
    @GetMapping("/completion")
    public String completion(@RequestParam int no, @RequestAttribute String username) {
        // 作业列表改成"只看自己布置的"（否则序号和自己看到的列表对不上）
        List<Homework> list = homeworkMapper.findByTeacher(username);
        if (no < 1 || no > list.size()) {
            return "序号无效，请重新输入";
        }
        Homework target = list.get(no - 1);
        int homeworkId = target.getId();

        // 改成"只查选了当前老师的学生"，而不是所有学生
        List<String> allStudents = userMapper.findStudentsByTeacher(username);

        // 查这个作业已交的学生（这行不用动）
        List<String> submitted = submissionMapper.findStudentsByHomeworkId(homeworkId);

        String result = "作业「" + target.getName() + "」完成情况：\n";
        for (String s : allStudents) {
            if (submitted.contains(s)) {
                result += s + "：已交\n";
            } else {
                result += s + "：未交\n";
            }
        }
        return result;
    }

    @DeleteMapping("/homework/batch/{ids}")
    public String batchDelete(@PathVariable String ids, @RequestAttribute String username) {
        String[] idArray = ids.split(",");
        int count = 0;
        for (String s : idArray) {
            homeworkMapper.delete(Integer.parseInt(s));
            count++;
        }
        stringRedisTemplate.delete("homework:list:" + username);   // 删缓存
        return "批量删除成功，共删除" + count + "条";
    }

    @PostMapping("/score/batch")
    public String batchScore(@RequestParam String ids, @RequestParam int score) {
        // ids 是 "1,2,3"，score 是统一分数
        String[] idArray = ids.split(",");
        int count = 0;
        for (String s : idArray) {
            submissionMapper.updateScore(Integer.parseInt(s), score);   // 复用已有的 updateScore
            count++;
        }
        return "批量评分成功，共 " + count + " 条";
    }
    // 导出作业（老师只导出自己的作业）
    @GetMapping("/export/homework")
    public String exportHomework(@RequestAttribute String username) {
        List<Homework> list = homeworkMapper.findByTeacher(username);   // 只导出自己的作业
        String csv = "\uFEFF" + "序号,作业名,布置老师,开始时间,截止时间\n";   // 顺手把"布置作业"改成"布置老师"
        int num = 1;
        for (Homework h : list) {
            csv += num + "," + h.getName() + "," + h.getTeacher() + "," + h.getStartTime() + "," + h.getEndTime() + "\n";
            num++;
        }
        return csv;
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> download(@RequestParam String fileName) {
        File file = new File(uploadDir, fileName);   // 从配置的上传目录找这个文件
        if (!file.exists()) {
            return ResponseEntity.notFound().build();   // 文件不存在 → 404
        }
        Resource resource = new FileSystemResource(file);
        // 返回文件，并设置"下载"响应头
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }
    @PostMapping("/choose-teacher")
    public String chooseTeacher(@RequestParam String teacher, @RequestAttribute String username) {
        String role = userMapper.findRoleByUsername(teacher);   // 查这个"老师"是不是真的老师
        if (role == null) {
            return "该用户不存在";
        }
        if (!"teacher".equals(role)) {
            return "该用户不是老师";
        }
        userMapper.chooseTeacher(username, teacher);   // 把这个学生和老师绑定
        return "选择老师成功：" + teacher;
    }
    @GetMapping("/my-students")
    public List<String> myStudents(@RequestAttribute String username) {
        return userMapper.findStudentsByTeacher(username);
    }
    @GetMapping("/redis/test")
    public String redisTest() {
        // 存一个键值对：key=hello，value=world，60 秒后过期
        stringRedisTemplate.opsForValue().set("hello", "world", Duration.ofSeconds(60));
        // 再读出来
        String value = stringRedisTemplate.opsForValue().get("hello");
        return "从 Redis 读到的 hello = " + value;
    }


}
