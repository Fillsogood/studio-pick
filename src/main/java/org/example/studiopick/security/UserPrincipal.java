package org.example.studiopick.security;

import lombok.Getter;
import org.example.studiopick.domain.user.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
public class UserPrincipal implements UserDetails {

    private final User user;

    public UserPrincipal(User user) {
        this.user = user;
    }

    // 권한 반환 (ROLE_접두사 필수)
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(() -> "ROLE_" + user.getRole().name()); // 예: ROLE_USER
    }

    // 로그인 ID로 사용할 값 (email)
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    // 로그인 시 비교할 비밀번호
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 계정 만료 여부: false면 로그인 불가
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 계정 잠김 여부
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 자격 증명(비밀번호) 만료 여부
    }

    @Override
    public boolean isEnabled() {
        return user.getStatus().name().equals("ACTIVE"); // UserStatus.ACTIVE 상태만 로그인 허용
    }
}
