package org.example.studiopick.security;

import lombok.Getter;
import org.example.studiopick.domain.common.enums.UserRole;
import org.example.studiopick.domain.user.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Getter
public class UserPrincipal implements OAuth2User, UserDetails {

    private final User user;
    private final Long tokenUserId;  // 토큰에서 추출한 사용자 ID
    private final String tokenEmail; // 토큰에서 추출한 이메일
    private final UserRole userRole;
    private Map<String, Object> attributes;


    public Long getId() {
        return user != null ? user.getId() : tokenUserId;
    }
    public Object getRole() { return user != null ? user.getRole() : userRole; }


    // 기존 User 엔티티 기반 생성자
    public UserPrincipal(User user, UserRole userRole) {
        this.user = user;
      this.userRole = userRole;
      this.tokenUserId = null;
        this.tokenEmail = null;
    }

    // 토큰 정보만으로 생성하는 생성자 (DB 조회 없음)
    private UserPrincipal(Long userId, String email, UserRole userRole) {
      this.userRole = userRole;
      this.user = null;
        this.tokenUserId = userId;
        this.tokenEmail = email;
    }

    // OAuth2User용 create 메서드
    public static UserPrincipal create(User user,UserRole role, Map<String, Object> attributes) {
        UserPrincipal userPrincipal = new UserPrincipal(user,role);
        userPrincipal.setAttributes(attributes);
        return userPrincipal;
    }

    // 일반 로그인용 create (User 엔티티 기반)
    public static UserPrincipal create(User user, UserRole role) {
        return new UserPrincipal(user, role);
    }

    // 토큰 정보만으로 생성 (DB 조회 없음)
    public static UserPrincipal createFromToken(Long userId, String email, UserRole role) {
        return new UserPrincipal(userId, email, role);
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    // 사용자 ID 반환 (컨트롤러에서 사용)
    public Long getUserId() {
        return user != null ? user.getId() : tokenUserId;
    }

    // OAuth2User 필수
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    // OAuth2User 필수
    @Override
    public String getName() {
        return String.valueOf(getUserId());
    }

    // Spring Security 권한 정보
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (user != null) {
            return Collections.singleton(() -> "ROLE_" + user.getRole().name());
        }
        return Collections.singleton(() -> "ROLE_USER"); // 기본 권한
    }

    @Override
    public String getUsername() {
        return user != null ? user.getEmail() : tokenEmail;
    }

    @Override
    public String getPassword() {
        return user != null ? user.getPassword() : null;
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
        return user != null ? user.getStatus().name().equals("ACTIVE") : true;
    }
}
