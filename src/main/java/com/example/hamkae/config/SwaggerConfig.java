package com.example.hamkae.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI 설정 클래스
 * API 문서화를 위한 설정을 담당합니다.
 * 
 * @author 윤준하
 * @version 1.0
 * @since 2025-08-14
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                    new Server().url("http://localhost:8080").description("개발 서버"),
                    new Server().url("https://your-domain.com").description("운영 서버")
                ));
    }

    private Info apiInfo() {
        return new Info()
                .title("함께줍줍 API")
                .description("안양시 쓰레기 문제 해결을 위한 시민 참여형 플랫폼 API 명세서")
                .version("1.0.0")
                .contact(new Contact()
                        .name("함께줍줍 개발팀")
                        .email("dev@hamkae.com")
                        .url("https://github.com/YJlang/hackthon3-BE"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }
}
