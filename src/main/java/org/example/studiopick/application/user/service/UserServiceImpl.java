package org.example.studiopick.application.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studiopick.application.auth.dto.UserSignupRequestDto;
import org.example.studiopick.application.user.dto.UserProfileResponseDto;
import org.example.studiopick.application.user.dto.UserProfileUpdateRequestDto;
import org.example.studiopick.application.user.dto.UserProfileUpdateResponseDto;
import org.example.studiopick.domain.common.enums.UserRole;
import org.example.studiopick.domain.common.enums.UserStatus;
import org.example.studiopick.domain.user.User;
import org.example.studiopick.infrastructure.User.JpaUserRepository;
import org.example.studiopick.infrastructure.s3.S3Uploader;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import org.example.studiopick.common.exception.UserNotFoundException;
import org.example.studiopick.common.exception.DuplicateResourceException;

import org.springframework.dao.DataIntegrityViolationException;



/**
 * 사용자 관리 비즈니스 로직을 처리하는 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final JpaUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Uploader s3Uploader;

    /**
     * 사용자 ID로 사용자 조회
     */
    @Override
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. ID: " + id));
    }

    /**
     * 이메일로 사용자 조회
     */
    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. Email: " + email));
    }

    /**
     * 사용자 생성 (회원가입)
     */
    @Override
    @Transactional
    public void createUser(UserSignupRequestDto dto) {
        log.info("사용자 생성 요청: email={}", dto.getEmail());
        
        // 이메일 중복 검사
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("이메일");
        }
        
        // 휴대폰 번호 중복 검사  
        if (userRepository.existsByPhone(dto.getPhone())) {
            throw new DuplicateResourceException("휴대폰 번호");
        }
        
        try {
            User user = User.builder()
                    .email(dto.getEmail())
                    .password(passwordEncoder.encode(dto.getPassword()))
                    .name(dto.getName())
                    .phone(dto.getPhone())
                    .nickname(generateUniqueNickname(dto.getName()))
                    .isStudioOwner(false)
                    .status(UserStatus.ACTIVE)
                    .role(UserRole.USER)
                    .build();

            userRepository.save(user);
            log.info("사용자 생성 완료: email={}, userId={}", dto.getEmail(), user.getId());
            
        } catch (DataIntegrityViolationException e) {
            log.error("데이터 무결성 위반: {}", e.getMessage());
            throw new DuplicateResourceException("중복된 데이터");
        }
    }

    /**
     * 이메일 중복 검사
     */
    @Override
    public boolean validateEmail(String email) {
        return !userRepository.existsByEmail(email);
    }

    /**
     * 휴대폰 번호 중복 검사
     */
    @Override
    public boolean validatePhone(String phone) {
        return !userRepository.existsByPhone(phone);
    }

    /**
     * 사용자 프로필 조회
     */
    @Override
    public UserProfileResponseDto getUserProfile(Long userId) {
        User user = getById(userId);

        return UserProfileResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .isStudioOwner(user.isStudioOwner())
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * 사용자 프로필 수정
     */
    @Override
    @Transactional
    public UserProfileUpdateResponseDto updateUserProfile(Long userId, UserProfileUpdateRequestDto dto) {
        User user = getById(userId);

        // 닉네임 중복 검사 (현재 사용자 제외)
        if (dto.getNickname() != null && !dto.getNickname().equals(user.getNickname())) {
            if (userRepository.existsByNicknameAndIdNot(dto.getNickname(), userId)) {
                throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
            }
        }

        user.updateProfile(dto.getName(), dto.getPhone(), dto.getNickname());

        return UserProfileUpdateResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .build();
    }

    /**
     * 비밀번호 변경
     */
    @Override
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = getById(userId);

        if (user.getPassword() == null || !passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        if (!isValidPassword(newPassword)) {
            throw new IllegalArgumentException("비밀번호는 영문, 숫자, 특수문자를 포함한 8자 이상이어야 합니다.");
        }

        user.updatePassword(passwordEncoder.encode(newPassword));
        log.info("비밀번호 변경 완료: userId={}", userId);
    }

    /**
     * 프로필 이미지 업로드
     */
    @Override
    @Transactional
    public String uploadProfileImage(Long userId, MultipartFile image) {
        User user = getById(userId);

        String imageUrl = s3Uploader.upload(image, "profile");
        user.updateProfileImage(imageUrl);
        
        log.info("프로필 이미지 업로드 완료: userId={}, imageUrl={}", userId, imageUrl);
        return imageUrl;
    }

    /**
     * 비밀번호 유효성 검사
     */
    private boolean isValidPassword(String password) {
        String pattern = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()])[A-Za-z\\d!@#$%^&*()]{8,}$";
        return password != null && password.matches(pattern);
    }

    /**
     * 중복되지 않는 닉네임 생성
     */
    private String generateUniqueNickname(String baseName) {
        String baseNickname = baseName != null ? baseName : "사용자";
        String nickname = baseNickname;
        int suffix = 1;
        
        while (userRepository.existsByNickname(nickname)) {
            nickname = baseNickname + suffix++;
        }
        
        return nickname;
    }
}
