package org.example.studiopick.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.studiopick.domain.common.BaseEntity;
import org.example.studiopick.domain.common.enums.UserRole;
import org.example.studiopick.domain.common.enums.UserStatus;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "\"User\"")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Column(name = "email", unique = true, nullable = false, length = 30)
    private String email;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "name", nullable = false, length = 7)
    private String name;

    @Column(name = "phone", unique = true, nullable = false, length = 11)
    private String phone;

    @Column(name = "nickname", unique = true, nullable = false, length = 10)
    private String nickname;

    @Column(name = "is_studio_owner", nullable = false)
    private Boolean isStudioOwner = false;

    @Column(name = "login_fail_count", nullable = false)
    private Short loginFailCount = 0;

    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

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
                Boolean isStudioOwner, UserStatus status, UserRole role) {
        this.email = email;
        this.password = password;
        this.name = name != null ? name : "소셜회원"; // ✅ 기본값
        this.phone = phone != null ? phone : "00000000000"; // ✅ 기본값
        this.nickname = nickname;
        this.isStudioOwner = isStudioOwner != null ? isStudioOwner : false;
        this.status = status != null ? status : UserStatus.ACTIVE;
        this.role = role != null ? role : UserRole.USER;
    }

    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    public void updateProfile(String name, String phone, String nickname) {
        this.name = name;
        this.phone = phone;
        this.nickname = nickname;
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
}
