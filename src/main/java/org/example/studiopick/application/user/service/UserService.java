package org.example.studiopick.application.user.service;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.user.dto.UserSignupRequestDto;
import org.example.studiopick.domain.user.entity.User;
import org.example.studiopick.domain.common.enums.UserRole;
import org.example.studiopick.domain.common.enums.UserStatus;
import org.example.studiopick.domain.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signup(UserSignupRequestDto dto) {

        // 중복 검사
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        if (userRepository.findByPhone(dto.getPhone()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 전화번호입니다.");
        }

        if (userRepository.findByNickname(dto.getNickname()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        // User 엔티티 생성
        User user = User.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))  // 암호화 OK
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
        return !userRepository.existsByEmail(email);  // true: 사용 가능 / false: 이미 존재
    }

    // 정규식 조건 메서드
    private boolean isValidPassword(String password) {
        String pattern = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()])[A-Za-z\\d!@#$%^&*()]{8,}$";
        return password != null && password.matches(pattern);
    }
}
