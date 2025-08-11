package com.example.hamkae.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * JWT(JSON Web Token) 생성 및 검증을 위한 유틸리티 클래스
 * 사용자 인증 및 세션 관리를 담당합니다.
 * 
 * @author 개발팀
 * @version 1.0
 * @since 2024-12-19
 */
@Component
public class JwtUtil {
    
    /**
     * JWT 서명에 사용되는 비밀키
     * 실제 운영 환경에서는 환경변수나 설정 파일에서 관리해야 합니다.
     */
    private final String SECRET_KEY = "rkfcx9/znAqW7LiEWC/R51xN1ga593fCkNm363leREQ=";
    
    /**
     * JWT 토큰의 만료 시간 (밀리초)
     * 현재 설정: 24시간 (1000 * 60 * 60 * 24)
     */
    private final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; //24시간

    /**
     * 사용자명을 기반으로 JWT 토큰을 생성합니다.
     * 
     * @param username 토큰에 포함할 사용자명
     * @return 생성된 JWT 토큰 문자열
     */
    public String createToken(String username) {
        return Jwts.builder()
                .setSubject(username)                    // 토큰 주체 (사용자명)
                .setIssuedAt(new Date())                // 토큰 발급 시간
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // 토큰 만료 시간
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY) // HS256 알고리즘으로 서명
                .compact();                             // 토큰 생성 완료
    }

    /**
     * JWT 토큰을 검증하고 사용자명을 추출합니다.
     * 
     * @param token 검증할 JWT 토큰
     * @return 토큰이 유효하면 사용자명, 유효하지 않으면 null
     */
    public String validateAndGetUsername(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(SECRET_KEY)          // 서명 키 설정
                    .parseClaimsJws(token)              // 토큰 파싱 및 서명 검증
                    .getBody()                          // 토큰 본문 추출
                    .getSubject();                      // 사용자명 반환
        } catch (Exception e) {
            return null; // 유효하지 않은 토큰
        }
    }
}