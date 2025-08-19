package com.example.hamkae.service;

import com.example.hamkae.domain.User;
import com.example.hamkae.DTO.RegisterRequestDTO;
import com.example.hamkae.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 사용자 관련 비즈니스 로직을 처리하는 서비스 클래스
 * 회원가입, 로그인 검증 등의 기능을 제공합니다.
 * 
 * @author 개발팀
 * @version 1.0
 * @since 2024-12-19
 */
@Service
@RequiredArgsConstructor
public class UserService {

    /**
     * 사용자 정보를 데이터베이스에서 조회/저장하는 Repository
     */
    private final UserRepository userRepository;
    
    /**
     * 비밀번호 암호화/검증을 위한 BCrypt 인코더
     */
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 새로운 사용자를 등록합니다.
     * 
     * @param request 회원가입 요청 정보 (이름, 아이디, 비밀번호)
     * @return 등록된 사용자 정보
     * @throws RuntimeException 이미 존재하는 아이디인 경우
     */
    public User register(RegisterRequestDTO request) {
        // 아이디 중복 검사
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("이미 존재하는 아이디입니다.");
        }
        
        // 새로운 사용자 생성 (비밀번호는 BCrypt로 암호화)
        User user = User.builder()
                .name(request.getName())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        
        // 데이터베이스에 저장하고 반환
        return userRepository.save(user);
    }

    /**
     * 사용자 로그인 정보를 검증합니다.
     * 
     * @param username 사용자 아이디
     * @param password 사용자 비밀번호 (평문)
     * @return 검증 성공 시 사용자 정보, 실패 시 null
     */
    public User validateUser(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .orElse(null);
    }

    /**
     * 사용자명으로 사용자를 조회합니다.
     * 
     * @param username 조회할 사용자명
     * @return 사용자 정보
     * @throws RuntimeException 사용자를 찾을 수 없는 경우
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + username));
    }

    /**
     * 사용자 정보를 저장합니다.
     * 
     * @param user 저장할 사용자 정보
     * @return 저장된 사용자 정보
     */
    public User save(User user) {
        return userRepository.save(user);
    }
}
