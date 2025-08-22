package com.example.hamkae.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.http.CacheControl;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.servlet.resource.PathResourceResolver;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 웹 관련 설정을 담당하는 설정 클래스
 * CORS, 파일 업로드, 정적 리소스 핸들링 등을 설정합니다.
 * 
 * @author 윤준하
 * @version 1.0
 * @since 2025-08-13
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * CORS 설정
     * 서버 배포 환경에서 프론트엔드 도메인만 허용합니다.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                    "https://hamkae.sku-sku.com",           // HTTPS 프로덕션 도메인 (메인)
                    "https://43.202.43.20",                 // HTTPS IP 주소 (직접 접속용)
                    "http://localhost:3000",                // 로컬 개발용
                    "http://localhost:5173"                 // Vite 개발 서버
                )
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 정적 리소스 핸들링 설정
     * 업로드된 이미지 파일들을 정적 리소스로 제공합니다.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 업로드된 이미지 파일들을 /images/** 경로로 접근 가능하도록 설정
        // 새로운 공개 디렉토리 경로로 변경 (nginx 사용자 접근 가능)
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:/var/www/hamkae/images/")
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS))
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource resource = super.getResource(resourcePath, location);
                        if (resource != null && resource.exists()) {
                            return resource;
                        }
                        // 파일이 존재하지 않으면 기본 이미지 반환
                        return new ClassPathResource("static/tresh.png");
                    }
                });
    }

    /**
     * MultipartResolver 설정
     * 파일 업로드를 위한 설정입니다.
     */
    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }
}