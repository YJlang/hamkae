package com.example.hamkae.DTO;

import lombok.Data;

/**
 * 회원가입 요청을 위한 DTO 클래스
 * 클라이언트로부터 회원가입 정보를 받습니다.
 * 
 * @author 개발팀
 * @version 1.0
 * @since 2024-12-19
 */
@Data
public class RegisterRequestDTO {
    
    /**
     * 사용자 실명
     * 필수 입력 항목
     */
    private String name;
    
    /**
     * 사용자 로그인 아이디
     * 고유값이어야 하며, 필수 입력 항목
     */
    private String username;
    
    /**
     * 사용자 비밀번호
     * BCrypt로 암호화되어 저장되며, 필수 입력 항목
     */
    private String password;
}
