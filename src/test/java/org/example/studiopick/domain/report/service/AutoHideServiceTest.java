package org.example.studiopick.domain.report.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutoHideServiceTest {

    @Mock
    private ArtworkRepository artworkRepository;
    
    @Mock
    private ClassRepository classRepository;
    
    @Mock
    private ReviewRepository reviewRepository;
    
    @InjectMocks
    private AutoHideService autoHideService;
    
    @Test
    void autoHideArtwork_Success() {
        // Given
        Long artworkId = 1L;
        Artwork artwork = mock(Artwork.class);
        
        when(artworkRepository.findById(artworkId)).thenReturn(Optional.of(artwork));
        when(artworkRepository.save(any(Artwork.class))).thenReturn(artwork);
        
        // When
        boolean result = autoHideService.autoHideContent(ReportType.ARTWORK, artworkId);
        
        // Then
        assertTrue(result);
        verify(artwork).changeStatus(ArtworkStatus.REPORTED);
        verify(artworkRepository).save(artwork);
    }
    
    @Test
    void autoHideArtwork_NotFound() {
        // Given
        Long artworkId = 1L;
        when(artworkRepository.findById(artworkId)).thenReturn(Optional.empty());
        
        // When
        boolean result = autoHideService.autoHideContent(ReportType.ARTWORK, artworkId);
        
        // Then
        assertFalse(result);
        verify(artworkRepository, never()).save(any());
    }
    
    @Test
    void autoHideClass_Success() {
        // Given
        Long classId = 1L;
        ClassEntity classEntity = mock(ClassEntity.class);
        
        when(classRepository.findById(classId)).thenReturn(Optional.of(classEntity));
        when(classRepository.save(any(ClassEntity.class))).thenReturn(classEntity);
        
        // When
        boolean result = autoHideService.autoHideContent(ReportType.CLASS, classId);
        
        // Then
        assertTrue(result);
        verify(classEntity).changeStatus(ClassStatus.REPORTED);
        verify(classRepository).save(classEntity);
    }
    
    @Test
    void autoHideReview_Success() {
        // Given
        Long reviewId = 1L;
        Review review = mock(Review.class);
        
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        
        // When
        boolean result = autoHideService.autoHideContent(ReportType.REVIEW, reviewId);
        
        // Then
        assertTrue(result);
        verify(review).changeStatus(ReviewStatus.REPORTED);
        verify(reviewRepository).save(review);
    }
    
    @Test
    void restoreArtwork_Success() {
        // Given
        Long artworkId = 1L;
        Artwork artwork = mock(Artwork.class);
        
        when(artworkRepository.findById(artworkId)).thenReturn(Optional.of(artwork));
        when(artwork.getStatus()).thenReturn(ArtworkStatus.REPORTED);
        when(artworkRepository.save(any(Artwork.class))).thenReturn(artwork);
        
        // When
        boolean result = autoHideService.restoreContent(ReportType.ARTWORK, artworkId);
        
        // Then
        assertTrue(result);
        verify(artwork).changeStatus(ArtworkStatus.PUBLIC);
        verify(artworkRepository).save(artwork);
    }
    
    @Test
    void restoreArtwork_NotReported() {
        // Given
        Long artworkId = 1L;
        Artwork artwork = mock(Artwork.class);
        
        when(artworkRepository.findById(artworkId)).thenReturn(Optional.of(artwork));
        when(artwork.getStatus()).thenReturn(ArtworkStatus.PUBLIC); // 이미 공개 상태
        
        // When
        boolean result = autoHideService.restoreContent(ReportType.ARTWORK, artworkId);
        
        // Then
        assertFalse(result);
        verify(artwork, never()).changeStatus(any());
        verify(artworkRepository, never()).save(any());
    }
}