package com.example.hamkae.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 사진 업로드 완료 이벤트
 * AFTER 사진 업로드 완료 시 AI 검증을 트리거하기 위해 사용됩니다.
 * 
 * @author 윤준하
 * @version 1.0
 * @since 2025-08-20
 */
@Getter
@AllArgsConstructor
public class PhotoUploadedEvent {
    private final Long markerId;
    private final Long userId;
    private final String photoType;
}
