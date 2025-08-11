package com.example.hamkae.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 웹 애플리케이션 전역 설정을 담당하는 설정 클래스
 * CORS 설정, 인터셉터 설정 등을 관리합니다.
 * 
 * @author 개발팀
 * @version 1.0
 * @since 2024-12-19
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * CORS(Cross-Origin Resource Sharing) 설정을 구성합니다.
     * 프론트엔드 애플리케이션에서 백엔드 API에 접근할 수 있도록 허용합니다.
     * 
     * @param registry CORS 설정을 등록하는 레지스트리
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")                    // 모든 경로에 대해 CORS 허용
                .allowedOrigins("http://localhost:3000") // React 개발 서버 주소 허용
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 허용할 HTTP 메서드
                .allowedHeaders("*")                   // 모든 헤더 허용
                .allowCredentials(true)                // 쿠키, 인증 헤더 등 자격 증명 허용
                .maxAge(3600);                        // CORS 프리플라이트 요청 캐시 시간 (1시간)
    }
}