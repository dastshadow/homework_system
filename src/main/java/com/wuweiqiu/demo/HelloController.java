package com.wuweiqiu.demo;
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
@RestController
public class HelloController {
    Connection conn =null;
    @Value("${deepseek.api-key}")
    private String apiKey;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @GetMapping("/hello")
    public String hello(){
        return "Hello World";
    }
    //查看
    @GetMapping("/homework")
    public String homework(){
        String result="";
        try {
            conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/homework_system?serverTimezone=Asia/Shanghai",
                    "root",
                    "123456"
            );
            PreparedStatement pstmt = conn.prepareStatement("SELECT id,name from homework");
            ResultSet rs=pstmt.executeQuery();
            while (rs.next()){
                result=result+rs.getInt("id")+"."+rs.getString("name")+" ";
            }
            conn.close();
        }catch (SQLException e){
            System.out.println("查询失败，错误为:"+e.getMessage());
        }
        return result;

    }
    //添加.@RequestParam String name:接收 URL 里 ? 后面那个叫 name 的参数
    @PostMapping("/homework/add")
    public String add(@RequestParam String name) {
        try {
            conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/homework_system?serverTimezone=Asia/Shanghai",
                    "root",
                    "123456"
            );
            PreparedStatement pstmt = conn.prepareStatement("insert into homework(name) values(?) ");
            pstmt.setString(1, name);
            pstmt.executeUpdate();
            conn.close();
            return "添加成功：" + name;
        } catch (SQLException e) {
            return "添加失败:" + e.getMessage();
        }
    }
    //删除
    @DeleteMapping("/homework/{id}")
    public String delete(@PathVariable int id) {
        try {
            conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/homework_system?serverTimezone=Asia/Shanghai",
                    "root",
                    "123456"
            );
            PreparedStatement pstmt = conn.prepareStatement("delete from homework where id = ?");
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            conn.close();
            return "删除成功,id=" + id;
        } catch (SQLException e) {
            return "删除失败:" + e.getMessage();
        }
    }
    //修改
    @PutMapping("/homework/{id}")
    public String update(@PathVariable int id,@RequestParam String name){
        try {
            conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/homework_system?serverTimezone=Asia/Shanghai",
                    "root",
                    "123456"
            );
            PreparedStatement pstmt = conn.prepareStatement("update homework set name = ? where id = ?");
            pstmt.setString(1,name);
            pstmt.setInt(2,id);
            pstmt.executeUpdate();
            conn.close();
            return "修改成功:id ="+id;
        }catch (SQLException e){
            return "修改失败:"+e.getMessage();
        }
    }
    //注册
    @PostMapping("/register")
    public String register(@RequestParam String username,@RequestParam String password){
        try {
            conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/homework_system?serverTimezone=Asia/Shanghai",
                    "root",
                    "123456"
            );
            PreparedStatement pstmt =conn.prepareStatement("insert into user(username,password) values(?,?)");
            pstmt.setString(1,username);
            pstmt.setString(2,passwordEncoder.encode(password));
            pstmt.executeUpdate();
            conn.close();
            return "注册成功:"+username;
        }catch (SQLException e){
            return "注册失败:"+e;
        }
    }
    //登录
    @PostMapping("/login")
    public String login(@RequestParam String username,@RequestParam String password){
        // ① 密钥：一串够长的秘密字符串（至少 32 个字符）
        String secret = "this-is-my-very-long-secret-key-for-jwt-1234567890";
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        try {
            conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/homework_system?serverTimezone=Asia/Shanghai",
                    "root",
                    "123456"
            );
            PreparedStatement pstmt=conn.prepareStatement("select password from user where username=?");
            pstmt.setString(1,username);
            ResultSet rs=pstmt.executeQuery();
            if(rs.next()){
                String dbPassword = rs.getString("password");
                if(passwordEncoder.matches(password,dbPassword)){
                    String token = Jwts.builder()
                            .subject(username)    // 把"用户名"装进令牌
                            .expiration(new Date(System.currentTimeMillis()+1000L*60*60*24))
                            .signWith(key)        // 用密钥"盖章"（签名，防伪造）
                            .compact();           // 生成最终的 token 字符串
                    return token;
                }else{
                    return "登录错误";
                }
            }else {
                return "用户不存在";
            }
        }catch (SQLException e){
            return "登录失败:"+e.getMessage();
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
