package com.example.hamkae.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 사용자 정보를 담는 엔티티 클래스
 * 
 * @author 개발팀
 * @version 1.0
 * @since 2024-12-19
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class) // JPA Auditing을 위한 리스너 등록
public class User {

    /**
     * 사용자 고유 식별자 (Primary Key)
     * 자동 증가하는 Long 타입
     */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 사용자 실명
     * 필수 입력 항목, 최대 100자
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 사용자 로그인 아이디
     * 고유값, 필수 입력 항목, 최대 100자
     */
    @Column(unique = true, nullable = false, length = 100)
    private String username;

    /**
     * 사용자 비밀번호
     * BCrypt로 암호화되어 저장, 필수 입력 항목
     */
    @Column(nullable = false)
    private String password;

    /**
     * 사용자 보유 포인트
     * 기본값 0, 정수형
     */
    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer points = 0;

    /**
     * 계정 생성일시
     * 자동 생성되며 수정 불가
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 계정 정보 수정일시
     * 엔티티 수정 시 자동 업데이트
     */
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}