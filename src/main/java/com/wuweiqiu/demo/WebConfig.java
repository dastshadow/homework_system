package com.wuweiqiu.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;   // 注入拦截器（不再是 new）

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/homework/**", "/check", "/submit", "/submissions/**", "/score", "/completion", "/download", "/choose-teacher", "/my-students", "/export/homework", "/logout", "/save-api-key", "/my-submissions", "/teachers", "/students", "/choose-student")
                .excludePathPatterns("/login", "/register");
    }
}
