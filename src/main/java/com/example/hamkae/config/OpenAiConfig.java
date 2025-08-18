package com.example.hamkae.config;

import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

/**
 * OpenAI GPT API 설정 클래스
 * GPT API 클라이언트를 설정하고 Bean으로 등록합니다.
 * 
 * @author 윤준하
 * @version 1.0
 * @since 2025-08-15
 */
@Configuration
public class OpenAiConfig {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.timeout:30000}")
    private Long timeout;

    /**
     * OpenAI 서비스 Bean을 생성합니다.
     * 
     * @return OpenAiService 인스턴스
     */
    @Bean
    public OpenAiService openAiService() {
        return new OpenAiService(apiKey, Duration.ofMillis(timeout));
    }

    /**
     * OpenAI HTTP 클라이언트 (멀티모달 호출용)
     */
    @Bean
    public WebClient openAiWebClient() {
        return WebClient.builder()
                .baseUrl("https://api.openai.com")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
