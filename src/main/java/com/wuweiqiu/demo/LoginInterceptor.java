package com.wuweiqiu.demo;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.crypto.SecretKey;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

import io.jsonwebtoken.Claims;
public class LoginInterceptor implements HandlerInterceptor{
    private static final String SECRET ="this-is-my-very-long-secret-key-for-jwt-1234567890";
    @Override
    public boolean preHandle(HttpServletRequest request,HttpServletResponse response,Object handler)throws Exception{
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")){
            response.setStatus(401);
            return false;
        }
        token = token.substring(7);
        try{
            SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            String role = claims.get("role",String.class);

            String method = request.getMethod();
            if("student".equals(role)&&("POST".equals(method)||"PUT".equals(method)||"DELETE".equals(method))){
                response.setStatus(403);
                return false;
            }
            return true;
        }catch (Exception e){
            response.setStatus(401);
            return false;
        }

    }
}
