package com.wuweiqiu.demo;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring 的配置类：负责「注册」拦截器，让它真正生效。
 *
 * 拦截器本身（LoginInterceptor）只是个类，光写出来不会自动起作用。
 * 必须在这里注册，并告诉 Spring「哪些路径要拦、哪些路径放行」，它才会去拦截。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())                        // 注册拦截器
                .addPathPatterns("/homework/**", "/check", "/submit","/submissions","/score","/completion")           // 要拦的路径（这些需要登录）
                .excludePathPatterns("/login", "/register");                    // 放行的路径（登录/注册不用拦）
    }
}
