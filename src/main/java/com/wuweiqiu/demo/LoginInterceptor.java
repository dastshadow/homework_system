package com.wuweiqiu.demo;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.crypto.SecretKey;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

import io.jsonwebtoken.Claims;

/**
 * 登录拦截器 —— 也就是「门卫」。
 *
 * 每一个 HTTP 请求，在进入 Controller（主管）之前，都要先经过它检查。
 * 它负责两件事：
 *   1. 你有没有带有效的 token（证明你登录过）—— 没有就 401 拦下
 *   2. 你有没有权限干这件事（比如学生不能增删改作业）—— 没权限就 403 拦下
 *
 * 它实现了 HandlerInterceptor 接口，核心是下面的 preHandle 方法。
 */
public class LoginInterceptor implements HandlerInterceptor {

    // 密钥：验证 token 用的「防伪章」，必须和登录时生成 token 用的那个一模一样。
    // 生成时 signWith(key) 盖章，验证时 verifyWith(key) 验章，章对得上才算有效。
    private static final String SECRET = "this-is-my-very-long-secret-key-for-jwt-1234567890";

    /**
     * preHandle = 「在到达 Controller 之前处理」。
     * 这个方法在请求到达 Controller 之前执行，返回：
     *   - true  = 放行（请求继续走，进 Controller）
     *   - false = 拦下（请求到此为止，不会进 Controller）
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // ① 从请求头里取 token（前端 fetch 时放在 Authorization 里发过来的）
        String token = request.getHeader("Authorization");

        // ② 没带 token，或格式不对（不是 "Bearer xxx" 开头），直接 401 拦下
        if (token == null || !token.startsWith("Bearer ")) {
            response.setStatus(401);   // 401 = 未登录 / 未授权
            return false;
        }

        // ③ 去掉 "Bearer " 前缀（正好 7 个字符），留下纯 token
        token = token.substring(7);

        try {
            // ④ 验 token：用「同一个密钥」验章，解析出令牌里的信息。
            //    如果这行没抛异常，说明 token 是真的、没过期（章对得上）。
            SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

            // ⑤ 从 token 里取出「角色」（登录时塞进去的 claim）
            String role = claims.get("role", String.class);

            String method = request.getMethod();          // 请求方法：GET/POST/PUT/DELETE
            String uri = request.getRequestURI();         // 请求路径：如 /homework、/check

            // ⑥ 权限判断 1：学生不能增删改作业（只针对 /homework 路径的 POST/PUT/DELETE）
            if ("student".equals(role) && uri.startsWith("/homework")&&(uri.startsWith("/submissions") || uri.startsWith("/score") || uri.startsWith("/completion"))
                    && ("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method))) {
                response.setStatus(403);   // 403 = 已登录但没权限
                return false;
            }

            // ⑦ 权限判断 2：学生不能用 AI 检查
            if ("student".equals(role) && uri.startsWith("/check")) {
                response.setStatus(403);
                return false;
            }

            // ⑧ 验证通过，把「用户名」存进 request，方便后面的 Controller 用 @RequestAttribute 取
            String username = claims.getSubject();   // subject = 登录时塞进 token 的用户名
            request.setAttribute("username", username);
            return true;   // 放行

        } catch (Exception e) {
            // ⑨ 验签抛异常 = token 无效 / 被篡改 / 已过期 → 401 拦下
            response.setStatus(401);
            return false;
        }
    }
}
