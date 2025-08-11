package com.example.hamkae.repository;

import com.example.hamkae.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 사용자 정보를 데이터베이스에서 조회/저장하는 Repository 인터페이스
 * JpaRepository를 상속받아 기본적인 CRUD 기능을 제공합니다.
 * 
 * @author 개발팀
 * @version 1.0
 * @since 2024-12-19
 */
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 사용자명으로 사용자를 조회합니다.
     * 
     * @param username 조회할 사용자명
     * @return 사용자 정보를 담은 Optional 객체 (없으면 empty)
     */
    Optional<User> findByUsername(String username);

    /**
     * 사용자명이 이미 존재하는지 확인합니다.
     * 
     * @param username 확인할 사용자명
     * @return true: 존재함, false: 존재하지 않음
     */
    boolean existsByUsername(String username);
}