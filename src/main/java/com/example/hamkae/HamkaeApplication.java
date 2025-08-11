package com.example.hamkae;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * 함께줍줍 프로젝트 메인 애플리케이션 클래스
 * 
 * @author 개발팀
 * @version 1.0
 * @since 2024-12-19
 */
@SpringBootApplication
@EnableJpaAuditing // JPA Auditing 기능 활성화 (생성일시, 수정일시 자동 관리)
public class HamkaeApplication {

    /**
     * 애플리케이션 진입점
     * Spring Boot 애플리케이션을 시작합니다.
     * 
     * @param args 명령행 인수
     */
    public static void main(String[] args) {
        SpringApplication.run(HamkaeApplication.class, args);
    }

}
