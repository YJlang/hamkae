package com.example.hamkae.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API 응답을 위한 공통 응답 형식 클래스
 * 모든 API 응답에서 일관된 형식을 보장합니다.
 * 
 * @author 개발팀
 * @version 1.0
 * @since 2024-12-19
 * @param <T> 응답 데이터의 타입
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    
    /**
     * 요청 처리 성공 여부
     * true: 성공, false: 실패
     */
    private boolean success;
    
    /**
     * 응답 메시지
     * 성공/실패에 대한 설명 메시지
     */
    private String message;
    
    /**
     * 응답 데이터
     * 실제 반환할 데이터 객체
     */
    private T data;

    /**
     * 성공 응답을 생성하는 정적 메서드
     * 
     * @param message 성공 메시지
     * @param data 응답 데이터
     * @return 성공 응답 객체
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * 데이터가 없는 성공 응답을 생성하는 정적 메서드
     * 
     * @param message 성공 메시지
     * @return 데이터가 없는 성공 응답 객체
     */
    public static <T> ApiResponse<T> success(String message) {
        return success(message, null);
    }

    /**
     * 에러 응답을 생성하는 정적 메서드
     * 
     * @param message 에러 메시지
     * @return 에러 응답 객체
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}
