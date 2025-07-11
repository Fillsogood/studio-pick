package org.example.studiopick.domain.report;

import org.example.studiopick.domain.common.enums.ReportStatus;
import org.example.studiopick.domain.common.enums.ReportType;
import org.example.studiopick.domain.report.dto.ReportRequestDto;
import org.example.studiopick.domain.report.dto.ReportResponseDto;
import org.example.studiopick.domain.user.entity.User;
import org.example.studiopick.domain.user.repository.UserRepository;
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
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private org.example.studiopick.domain.report.service.AutoHideService autoHideService;
    
    @InjectMocks
    private ReportService reportService;
    
    private User testUser;
    private ReportRequestDto reportRequest;
    
    @BeforeEach
    void setUp() {
        testUser = mock(User.class);
        reportRequest = new ReportRequestDto(
            ReportType.ARTWORK,
            1L,
            "부적절한 콘텐츠입니다."
        );
    }
    
    @Test
    void createReport_Success() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(reportRepository.existsByUserAndReportedTypeAndReportedId(any(), any(), any()))
            .thenReturn(false);
        when(reportRepository.countByReportedTypeAndReportedIdAndStatus(any(), any(), any()))
            .thenReturn(1L); // 임계값 미만
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> {
            Report report = invocation.getArgument(0);
            // Mock ID 설정
            when(report.getId()).thenReturn(1L);
            return report;
        });
        
        // When
        ReportResponseDto response = reportService.createReport(userId, reportRequest);
        
        // Then
        assertNotNull(response);
        assertEquals("신고가 접수되었습니다.", response.message());
        verify(reportRepository).save(any(Report.class));
        verify(autoHideService, never()).autoHideContent(any(), any());
    }
    
    @Test
    void createReport_AlreadyReported() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(reportRepository.existsByUserAndReportedTypeAndReportedId(
            testUser, reportRequest.reportedType(), reportRequest.reportedId()))
            .thenReturn(true);
        
        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> reportService.createReport(userId, reportRequest));
        
        assertEquals("이미 신고하셨습니다.", exception.getMessage());
        verify(reportRepository, never()).save(any());
    }
    
    @Test
    void createReport_AutoHideTriggered() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(reportRepository.existsByUserAndReportedTypeAndReportedId(any(), any(), any()))
            .thenReturn(false);
        when(reportRepository.countByReportedTypeAndReportedIdAndStatus(any(), any(), any()))
            .thenReturn(3L); // 임계값 도달
        when(autoHideService.autoHideContent(any(), any())).thenReturn(true);
        
        Report mockReport = mock(Report.class);
        when(mockReport.getId()).thenReturn(1L);
        when(reportRepository.save(any(Report.class))).thenReturn(mockReport);
        
        // When
        ReportResponseDto response = reportService.createReport(userId, reportRequest);
        
        // Then
        assertNotNull(response);
        assertTrue(response.message().contains("자동으로 비공개 처리되었습니다"));
        verify(autoHideService).autoHideContent(reportRequest.reportedType(), reportRequest.reportedId());
        verify(mockReport).autoHide();
    }
    
    @Test
    void createReport_UserNotFound() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> reportService.createReport(userId, reportRequest));
        
        assertEquals("사용자 정보를 찾을 수 없습니다.", exception.getMessage());
        verify(reportRepository, never()).save(any());
    }
    
    @Test
    void processReport_Success() {
        // Given
        Long reportId = 1L;
        Long adminId = 2L;
        String adminComment = "처리 완료";
        
        Report mockReport = mock(Report.class);
        User mockAdmin = mock(User.class);
        
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(mockReport));
        when(userRepository.findById(adminId)).thenReturn(Optional.of(mockAdmin));
        when(mockReport.getReportedType()).thenReturn(ReportType.ARTWORK);
        when(mockReport.getReportedId()).thenReturn(1L);
        
        // When
        reportService.processReport(reportId, adminId, ReportStatus.RESTORED, adminComment);
        
        // Then
        verify(mockReport).restore(mockAdmin, adminComment);
        verify(autoHideService).restoreContent(ReportType.ARTWORK, 1L);
        verify(reportRepository).save(mockReport);
    }
    
    @Test
    void getReportCount_Success() {
        // Given
        ReportType reportType = ReportType.ARTWORK;
        Long reportedId = 1L;
        ReportStatus status = ReportStatus.PENDING;
        long expectedCount = 5L;
        
        when(reportRepository.countByReportedTypeAndReportedIdAndStatus(reportType, reportedId, status))
            .thenReturn(expectedCount);
        
        // When
        long actualCount = reportService.getReportCount(reportType, reportedId, status);
        
        // Then
        assertEquals(expectedCount, actualCount);
    }
    
    @Test
    void getTotalReportCount_Success() {
        // Given
        ReportType reportType = ReportType.ARTWORK;
        Long reportedId = 1L;
        long expectedCount = 10L;
        
        when(reportRepository.countByReportedTypeAndReportedId(reportType, reportedId))
            .thenReturn(expectedCount);
        
        // When
        long actualCount = reportService.getTotalReportCount(reportType, reportedId);
        
        // Then
        assertEquals(expectedCount, actualCount);
    }
}