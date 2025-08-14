package com.example.hamkae.DTO;

import lombok.Data;

/**
 * 프로필 수정 요청 DTO
 * 사용자 이름 변경을 요청합니다.
 */
@Data
public class UserProfileUpdateRequestDTO {

	/**
	 * 변경할 사용자 이름
	 */
	private String name;
}



