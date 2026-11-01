package com.wuweiqiu.demo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.Date;
import java.util.Scanner;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.JsonNode;

import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.ArrayNode;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
@RestController
public class HelloController {
    Connection conn =null;
    @Autowired
    private HomeworkMapper homeworkMapper;
    @Autowired
    private UserMapper userMapper;
    @Value("${deepseek.api-key}")
    private String apiKey;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @GetMapping("/hello")
    public String hello(){
        return "Hello World";
    }
    //查看
    @GetMapping("/homework")
    public List<Homework> homework(){
        return homeworkMapper.findAll();
    }
    //添加.@RequestParam String name:接收 URL 里 ? 后面那个叫 name 的参数
    @PostMapping("/homework/add")
    public String add(@RequestParam String name,@RequestAttribute String username) {
        homeworkMapper.add(name,username);
        return "添加成功:"+name;
    }
    //删除
    @DeleteMapping("/homework/{no}")
    public String delete(@PathVariable int no) {
        List<Homework> list = homeworkMapper.findAll();
        if(no<1||no>list.size()){
            return "序号无效，请重新输入";
        }
        Homework target = list.get(no-1);
        homeworkMapper.delete(target.getId());
        return "删除成功:"+target.getName();
    }
    //修改
    @PutMapping("/homework/{no}")
    public String update(@PathVariable int no,@RequestParam String name){
        List<Homework> list =homeworkMapper.findAll();
        if(no<1||no>list.size()){
            return "序号无效，请重新输入";
        }
        Homework target = list.get(no-1);
        homeworkMapper.update(target.getId(),name);
        return "修改成功:"+name;
    }
    //注册
    @PostMapping("/register")
    public String register(@RequestParam String username,@RequestParam String password){
        userMapper.register(username,passwordEncoder.encode(password),"student");
        return "注册成功:"+username;
    }
    //登录
    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password) {
        String dbPassword = userMapper.findPasswordByUsername(username);
        if (dbPassword == null) {
            return "用户不存在";
        }
        if (passwordEncoder.matches(password, dbPassword)) {
            String role = userMapper.findRoleByUsername(username);   // 查角色
            String secret = "this-is-my-very-long-secret-key-for-jwt-1234567890";
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            String token = Jwts.builder()
                    .subject(username)
                    .claim("role",role)
                    .expiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24))
                    .signWith(key)
                    .compact();
            return token + "|" + role;   // token 和角色拼一起返回
        } else {
            return "密码错误";
        }
    }
    @PostMapping("/check")
    public String check(@RequestParam String question) throws Exception {
        String prompt = "你是一位严谨的题库审核员。请检查下面的题目，重点检查：\n" +
                "1. 答案选项是否在选项中\n" +
                "2. 公式、特殊符号是否乱码或错位\n" +
                "3. 格式是否规范\n\n" +
                "题目内容：\n" + question + "\n\n" +
                "请逐条列出问题；没问题请回复未发现问题。";

        ObjectMapper mapper = new ObjectMapper();

        ObjectNode message = mapper.createObjectNode();
        message.put("role", "user");
        message.put("content", prompt);

        ArrayNode messages = mapper.createArrayNode();
        messages.add(message);

        ObjectNode body = mapper.createObjectNode();
        body.put("model", "deepseek-chat");
        body.set("messages", messages);

        String jsonBody = mapper.writeValueAsString(body);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.deepseek.com/chat/completions"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JsonNode root = mapper.readTree(response.body());
        return root.path("choices").get(0).path("message").path("content").stringValue();
    }



}
