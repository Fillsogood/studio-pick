package org.example.studiopick.domain.report;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.studiopick.domain.common.BaseEntity;
import org.example.studiopick.domain.common.enums.ReportStatus;
import org.example.studiopick.domain.common.enums.ReportType;
import org.example.studiopick.domain.user.entity.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "\"Report\"")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report extends BaseEntity {
    
    @Enumerated(EnumType.STRING)
    @Column(name = "reported_type", nullable = false)
    private ReportType reportedType;
    
    @Column(name = "reported_id", nullable = false)
    private Long reportedId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "reason", length = 255)
    private String reason;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReportStatus status = ReportStatus.PENDING;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private User admin;
    
    @Column(name = "admin_comment", length = 255)
    private String adminComment;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    @Builder
    public Report(ReportType reportedType, Long reportedId, User user, String reason, 
                  ReportStatus status, User admin, String adminComment) {
        this.reportedType = reportedType;
        this.reportedId = reportedId;
        this.user = user;
        this.reason = reason;
        this.status = status != null ? status : ReportStatus.PENDING;
        this.admin = admin;
        this.adminComment = adminComment;
    }
    
    public void updateReason(String reason) {
        this.reason = reason;
    }
    
    public void changeStatus(ReportStatus status) {
        this.status = status;
    }
    
    public void process(User admin, ReportStatus status, String adminComment) {
        this.admin = admin;
        this.status = status;
        this.adminComment = adminComment;
        this.processedAt = LocalDateTime.now();
    }
    
    public void autoHide() {
        this.status = ReportStatus.AUTO_HIDDEN;
        this.processedAt = LocalDateTime.now();
    }
    
    public void markAsReviewed(User admin, String adminComment) {
        this.admin = admin;
        this.status = ReportStatus.REVIEWED;
        this.adminComment = adminComment;
        this.processedAt = LocalDateTime.now();
    }
    
    public void restore(User admin, String adminComment) {
        this.admin = admin;
        this.status = ReportStatus.RESTORED;
        this.adminComment = adminComment;
        this.processedAt = LocalDateTime.now();
    }
    
    public void delete(User admin, String adminComment) {
        this.admin = admin;
        this.status = ReportStatus.DELETED;
        this.adminComment = adminComment;
        this.processedAt = LocalDateTime.now();
    }
    
    public boolean isPending() {
        return this.status == ReportStatus.PENDING;
    }
    
    public boolean isProcessed() {
        return this.processedAt != null;
    }
    
    public boolean isAutoHidden() {
        return this.status == ReportStatus.AUTO_HIDDEN;
    }
    
    public boolean isReviewed() {
        return this.status == ReportStatus.REVIEWED;
    }
    
    public boolean isRestored() {
        return this.status == ReportStatus.RESTORED;
    }
    
    public boolean isDeleted() {
        return this.status == ReportStatus.DELETED;
    }
}
