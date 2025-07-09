package org.example.studiopick.application.user.service;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.user.dto.UserProfileResponseDto;
import org.example.studiopick.application.user.dto.UserSignupRequestDto;
import org.example.studiopick.domain.common.enums.SocialProvider;
import org.example.studiopick.domain.common.enums.UserRole;
import org.example.studiopick.domain.common.enums.UserStatus;
import org.example.studiopick.domain.user.entity.SocialAccount;
import org.example.studiopick.domain.user.entity.User;
import org.example.studiopick.domain.user.repository.SocialAccountRepository;
import org.example.studiopick.domain.user.repository.UserRepository;
import org.example.studiopick.infrastructure.User.JpaUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final JpaUserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signup(UserSignupRequestDto dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 이메일 입니다.");
        }
        if (userRepository.findByPhone(dto.getPhone()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 휴대폰 번호 입니다.");
        }
        if (userRepository.findByNickname(dto.getNickname()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임 입니다.");
        }

        User user = User.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .name(dto.getName())
                .phone(dto.getPhone())
                .nickname(dto.getNickname())
                .isStudioOwner(false)
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .build();

        userRepository.save(user);
    }

    public boolean validateEmail(String email) {
        return !userRepository.existsByEmail(email);
    }

    public boolean validatePhone(String phone) {
        return !userRepository.existsByPhone(phone);
    }

    private boolean isValidPassword(String password) {
        String pattern = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()])[A-Za-z\\d!@#$%^&*()]{8,}$";
        return password != null && password.matches(pattern);
    }

    /**
     * 카카오 등 소셜 로그인 처리 메서드
     */
    @Transactional
    public User loginOrRegisterSocialUser(String provider, String socialId, String email, String nickname, String profileImage) {
        SocialProvider providerEnum = SocialProvider.valueOf(provider.toUpperCase());
        Optional<User> existingUser = userRepository
                .findBySocialAccountsProviderAndSocialAccountsSocialId(providerEnum, socialId);

        if (existingUser.isPresent()) {
            return existingUser.get(); // 기존 유저면 바로 반환
        }

        // 신규 유저 생성
        User newUser = User.builder()
                .email(email != null ? email : UUID.randomUUID().toString() + "@social.com")
                .password(null) // 소셜 로그인은 비밀번호 없음
                .nickname(nickname)
                .isStudioOwner(false)
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .build();

        userRepository.save(newUser);

        // 소셜 계정 정보 저장
        SocialAccount socialAccount = SocialAccount.builder()
                .provider(SocialProvider.valueOf(provider))
                .socialId(socialId)
                .user(newUser)
                .build();

        socialAccountRepository.save(socialAccount);

        return newUser;
    }

    @Transactional(readOnly = true)
    public UserProfileResponseDto getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));

        return UserProfileResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .isStudioOwner(user.isStudioOwner())
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt())
                .build();
    }

}
