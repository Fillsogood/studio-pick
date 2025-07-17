package org.example.studiopick.application.user.service;

import org.example.studiopick.application.auth.dto.UserSignupRequestDto;
import org.example.studiopick.application.user.dto.UserProfileResponseDto;
import org.example.studiopick.application.user.dto.UserProfileUpdateRequestDto;
import org.example.studiopick.application.user.dto.UserProfileUpdateResponseDto;
import org.example.studiopick.domain.user.User;
import org.springframework.web.multipart.MultipartFile;

/**
 * 사용자 관리 비즈니스 로직을 처리하는 서비스 인터페이스
 */
public interface UserService {
    
    /**
     * 사용자 ID로 사용자 조회
     */
    User getById(Long id);
    
    /**
     * 이메일로 사용자 조회
     */
    User findByEmail(String email);
    
    /**
     * 사용자 생성 (회원가입)
     */
    void createUser(UserSignupRequestDto dto);
    
    /**
     * 이메일 중복 검사
     */
    boolean validateEmail(String email);
    
    /**
     * 휴대폰 번호 중복 검사
     */
    boolean validatePhone(String phone);
    
    /**
     * 사용자 프로필 조회
     */
    UserProfileResponseDto getUserProfile(Long userId);
    
    /**
     * 사용자 프로필 수정
     */
    UserProfileUpdateResponseDto updateUserProfile(Long userId, UserProfileUpdateRequestDto dto);
    
    /**
     * 비밀번호 변경
     */
    void changePassword(Long userId, String currentPassword, String newPassword);
    
    /**
     * 프로필 이미지 업로드
     */
    String uploadProfileImage(Long userId, MultipartFile image);


    void sendPasswordResetEmail(String email);

    void resetPasswordByToken(String token, String newPassword);

}
