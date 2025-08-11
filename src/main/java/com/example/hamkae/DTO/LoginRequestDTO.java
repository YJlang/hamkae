package com.example.hamkae.DTO;

import lombok.Data;

/**
 * 로그인 요청을 위한 DTO 클래스
 * 클라이언트로부터 로그인 정보를 받습니다.
 * 
 * @author 개발팀
 * @version 1.0
 * @since 2024-12-19
 */
@Data
public class LoginRequestDTO {
    
    /**
     * 사용자 로그인 아이디
     * 필수 입력 항목
     */
    private String username;
    
    /**
     * 사용자 비밀번호
     * BCrypt로 검증되며, 필수 입력 항목
     */
    private String password;
}