package org.example.studiopick.domain.user;

import jakarta.persistence.*;
import lombok.*;
import org.example.studiopick.domain.common.BaseEntity;
import org.example.studiopick.domain.common.enums.UserRole;
import org.example.studiopick.domain.common.enums.UserStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "\"User\"")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "phone", unique = true, length = 15)
    private String phone;

    @Column(name = "nickname", unique = true, length = 50)
    private String nickname;

    @Column(name = "is_studio_owner", nullable = false)
    private Boolean isStudioOwner = false;

    @Column(name = "is_workshop_owner", nullable = false)
    private Boolean isWorkShopOwner = false;

    @Column(name = "login_fail_count", nullable = false)
    private Short loginFailCount = 0;

    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

  // 비밀번호 재설정용 토큰 setter 추가
  @Setter
  @Column(name = "reset_token")
    private String resetToken;

    @Setter
    @Column(name = "reset_token_expires_at")
    private LocalDateTime resetTokenExpiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role = UserRole.USER;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SocialAccount> socialAccounts = new ArrayList<>();

    @Builder
    public User(String email, String password, String name, String phone, String nickname,
                String profileImageUrl, Boolean isStudioOwner, UserStatus status, UserRole role) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.isStudioOwner = isStudioOwner != null ? isStudioOwner : false;
        this.status = status != null ? status : UserStatus.ACTIVE;
        this.role = role != null ? role : UserRole.USER;
    }

    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    public void updateProfile(String name, String phone, String nickname) {
        if (name != null) this.name = name;
        if (phone != null) this.phone = phone;
        if (nickname != null) this.nickname = nickname;
    }

    public void increaseLoginFailCount() {
        this.loginFailCount++;
    }

    public void resetLoginFailCount() {
        this.loginFailCount = 0;
    }

    public void verifyEmail() {
        this.emailVerified = true;
    }

    public void changeStatus(UserStatus status) {
        this.status = status;
    }

    public void promoteToStudioOwner() {
        this.isStudioOwner = true;
        this.role = UserRole.STUDIO_OWNER;
    }

    public void changeRole(UserRole role) {
        this.role = role;
    }

    public void updateProfileImage(String imageUrl) {
        this.profileImageUrl = imageUrl;
    }

    // 관리자용 업데이트 메서드
    public void updateName(String name) {
        if (name != null && !name.isEmpty()) {
            this.name = name;
        }
    }

    public void updatePhone(String phone) {
        if (phone != null && !phone.isEmpty()) {
            this.phone = phone;
        }
    }

    public void updatePassword(String password) {
        if (password != null && !password.isEmpty()) {
            this.password = password;
        }
    }

  // 상태 확인 메서드들
    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    public void suspend() {
        this.status = UserStatus.LOCKED;
    }

    public void deactivate() {
        this.status = UserStatus.INACTIVE;
    }

    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }

    public boolean isSuspended() {
        return this.status == UserStatus.LOCKED;
    }

    public boolean isStudioOwner() {
        return this.role == UserRole.STUDIO_OWNER || this.isStudioOwner;
    }

    public boolean isWorkShopOwner() {
        return this.role == UserRole.WORKSHOP_OWNER || this.isWorkShopOwner;
    }

    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }

    /**
     * 화면에 표시할 사용자 이름을 반환
     */
    public String getDisplayName() {
        return this.nickname != null && !this.nickname.isEmpty() ? this.nickname : this.name;
    }

    public boolean isWorkshopOwner() {
        return this.role == UserRole.WORKSHOP_OWNER || this.isWorkShopOwner;
    }

    public void setWorkshopOwner(boolean b) {
        this.role = UserRole.WORKSHOP_OWNER;
    }
}
