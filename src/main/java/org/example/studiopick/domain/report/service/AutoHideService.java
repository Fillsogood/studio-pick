package org.example.studiopick.domain.report.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studiopick.domain.artwork.Artwork;
import org.example.studiopick.domain.class_entity.ClassEntity;
import org.example.studiopick.domain.common.enums.ArtworkStatus;
import org.example.studiopick.domain.common.enums.ClassStatus;
import org.example.studiopick.domain.common.enums.ReportType;
import org.example.studiopick.domain.common.enums.ReviewStatus;
import org.example.studiopick.domain.review.Review;
import org.example.studiopick.infrastructure.artwork.ArtworkRepository;
import org.example.studiopick.infrastructure.classes.ClassRepository;
import org.example.studiopick.infrastructure.review.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutoHideService {
    
    private final ArtworkRepository artworkRepository;
    private final ClassRepository classRepository;
    private final ReviewRepository reviewRepository;
    
    /**
     * 신고 횟수에 따라 자동으로 컨텐츠를 비공개 처리합니다.
     * 
     * @param reportType 신고 대상 타입
     * @param reportedId 신고 대상 ID
     * @return 비공개 처리 성공 여부
     */
    @Transactional
    public boolean autoHideContent(ReportType reportType, Long reportedId) {
        try {
            switch (reportType) {
                case ARTWORK:
                    return hideArtwork(reportedId);
                case CLASS:
                    return hideClass(reportedId);
                case REVIEW:
                    return hideReview(reportedId);
                default:
                    log.warn("Unknown report type: {}", reportType);
                    return false;
            }
        } catch (Exception e) {
            log.error("Failed to auto hide content. Type: {}, ID: {}", reportType, reportedId, e);
            return false;
        }
    }
    
    /**
     * 작품을 신고 상태로 변경합니다.
     */
    private boolean hideArtwork(Long artworkId) {
        return artworkRepository.findById(artworkId)
                .map(artwork -> {
                    artwork.changeStatus(ArtworkStatus.REPORTED);
                    artworkRepository.save(artwork);
                    log.info("Artwork {} has been auto-hidden due to reports", artworkId);
                    return true;
                })
                .orElse(false);
    }
    
    /**
     * 클래스를 신고 상태로 변경합니다.
     */
    private boolean hideClass(Long classId) {
        return classRepository.findById(classId)
                .map(classEntity -> {
                    classEntity.changeStatus(ClassStatus.REPORTED);
                    classRepository.save(classEntity);
                    log.info("Class {} has been auto-hidden due to reports", classId);
                    return true;
                })
                .orElse(false);
    }
    
    /**
     * 리뷰를 신고 상태로 변경합니다.
     */
    private boolean hideReview(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .map(review -> {
                    review.changeStatus(ReviewStatus.REPORTED);
                    reviewRepository.save(review);
                    log.info("Review {} has been auto-hidden due to reports", reviewId);
                    return true;
                })
                .orElse(false);
    }
    
    /**
     * 신고로 인해 비공개된 컨텐츠를 복원합니다.
     */
    @Transactional
    public boolean restoreContent(ReportType reportType, Long reportedId) {
        try {
            switch (reportType) {
                case ARTWORK:
                    return restoreArtwork(reportedId);
                case CLASS:
                    return restoreClass(reportedId);
                case REVIEW:
                    return restoreReview(reportedId);
                default:
                    log.warn("Unknown report type: {}", reportType);
                    return false;
            }
        } catch (Exception e) {
            log.error("Failed to restore content. Type: {}, ID: {}", reportType, reportedId, e);
            return false;
        }
    }
    
    /**
     * 작품을 공개 상태로 복원합니다.
     */
    private boolean restoreArtwork(Long artworkId) {
        return artworkRepository.findById(artworkId)
                .map(artwork -> {
                    if (artwork.getStatus() == ArtworkStatus.REPORTED) {
                        artwork.changeStatus(ArtworkStatus.PUBLIC);
                        artworkRepository.save(artwork);
                        log.info("Artwork {} has been restored", artworkId);
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }
    
    /**
     * 클래스를 공개 상태로 복원합니다.
     */
    private boolean restoreClass(Long classId) {
        return classRepository.findById(classId)
                .map(classEntity -> {
                    if (classEntity.getStatus() == ClassStatus.REPORTED) {
                        classEntity.changeStatus(ClassStatus.OPEN);
                        classRepository.save(classEntity);
                        log.info("Class {} has been restored", classId);
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }
    
    /**
     * 리뷰를 공개 상태로 복원합니다.
     */
    private boolean restoreReview(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .map(review -> {
                    if (review.getStatus() == ReviewStatus.REPORTED) {
                        review.changeStatus(ReviewStatus.VISIBLE);
                        reviewRepository.save(review);
                        log.info("Review {} has been restored", reviewId);
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }
}