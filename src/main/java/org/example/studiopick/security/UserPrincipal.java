package org.example.studiopick.security;

import lombok.Getter;
import org.example.studiopick.domain.user.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Getter
public class UserPrincipal implements OAuth2User, UserDetails {

    private final User user;
    private Map<String, Object> attributes;

    public Long getId() {
        return user.getId();
    }

    public UserPrincipal(User user) {
        this.user = user;
    }

    // OAuth2User용 create 메서드
    public static UserPrincipal create(User user, Map<String, Object> attributes) {
        UserPrincipal userPrincipal = new UserPrincipal(user);
        userPrincipal.setAttributes(attributes);
        return userPrincipal;
    }

    // 일반 로그인용 create
    public static UserPrincipal create(User user) {
        return new UserPrincipal(user);
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    // ✅ 여기에 추가됨
    public Long getUserId() {
        return user.getId();
    }

    // OAuth2User 필수
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    // OAuth2User 필수
    @Override
    public String getName() {
        return String.valueOf(user.getId());
    }

    // Spring Security 권한 정보
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(() -> "ROLE_" + user.getRole().name());
    }

    @Override
    public String getUsername() {
        return user.getEmail(); // 로그인 ID
    }

    @Override
    public String getPassword() {
        return user.getPassword(); // 비밀번호
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getStatus().name().equals("ACTIVE");
    }
}
