package org.example.studiopick.security;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.domain.common.enums.UserRole;
import org.example.studiopick.domain.user.entity.User;
import org.example.studiopick.domain.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // 로그인 시 이메일로 유저 정보 조회
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("해당 이메일을 가진 사용자가 존재하지 않습니다: " + email));
        UserRole role = user.getRole();

        return new UserPrincipal(user, role);
    }
}
