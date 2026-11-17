package com.wuweiqiu.demo;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.crypto.SecretKey;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component   // 交给 Spring 管理，才能 @Autowired 注入依赖
public class LoginInterceptor implements HandlerInterceptor {

    private static final String SECRET = "this-is-my-very-long-secret-key-for-jwt-1234567890";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;   // 注入 Redis，用来查黑名单 + 限流

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            response.setStatus(401);
            return false;
        }
        token = token.substring(7);

        try {
            SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

            // 查黑名单：注销过的 token 在 Redis 黑名单里，查到就拒绝
            String blacklisted = stringRedisTemplate.opsForValue().get("blacklist:" + token);
            if (blacklisted != null) {
                response.setStatus(401);   // 已注销，token 作废
                return false;
            }

            String username = claims.getSubject();

            // 限流：每个用户 60 秒内最多 10 次请求，防止有人疯狂刷接口
            String rateKey = "rate:" + username;
            Long count = stringRedisTemplate.opsForValue().increment(rateKey);   // INCR：每次 +1，返回新值
            if (count == 1) {
                stringRedisTemplate.expire(rateKey, Duration.ofSeconds(60));   // 第一次访问，设 60 秒窗口
            }
            if (count > 10) {
                response.setStatus(429);   // 429 = 请求太频繁，请歇会
                return false;
            }

            String role = claims.get("role", String.class);
            String method = request.getMethod();
            String uri = request.getRequestURI();

            // 权限判断 1：学生不能增删改作业
            if ("student".equals(role) && uri.startsWith("/homework")
                    && ("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method))) {
                response.setStatus(403);
                return false;
            }

            // 权限判断 2：学生不能用老师专属接口
            if ("student".equals(role)
                    && (uri.startsWith("/submissions") || uri.startsWith("/score") || uri.startsWith("/completion"))) {
                response.setStatus(403);
                return false;
            }

            // 权限判断 3：学生不能用 AI 检查
            if ("student".equals(role) && uri.startsWith("/check")) {
                response.setStatus(403);
                return false;
            }

            request.setAttribute("username", username);
            return true;
        } catch (Exception e) {
            response.setStatus(401);
            return false;
        }
    }
}
