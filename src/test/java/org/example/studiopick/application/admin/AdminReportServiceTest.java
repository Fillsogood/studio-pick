package org.example.studiopick.application.admin;

import org.example.studiopick.application.admin.dto.report.*;
import org.example.studiopick.domain.common.enums.ReportStatus;
import org.example.studiopick.domain.common.enums.ReportType;
import org.example.studiopick.domain.report.Report;
import org.example.studiopick.domain.report.ReportRepository;
import org.example.studiopick.domain.user.entity.User;
import org.example.studiopick.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminReportServiceTest {

    @Mock
    private ReportRepository reportRepository;
    
    @Mock
    private org.example.studiopick.domain.report.ReportService reportService;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private org.example.studiopick.infrastructure.artwork.ArtworkRepository artworkRepository;
    
    @Mock
    private org.example.studiopick.infrastructure.classes.ClassRepository classRepository;
    
    @Mock
    private org.example.studiopick.infrastructure.review.ReviewRepository reviewRepository;
    
    @InjectMocks
    private AdminReportService adminReportService;
    
    private Report mockReport;
    private User mockUser;
    
    @BeforeEach
    void setUp() {
        mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(1L);
        when(mockUser.getNickname()).thenReturn("testUser");
        when(mockUser.getName()).thenReturn("테스트사용자");
        when(mockUser.getDisplayName()).thenReturn("testUser");
        when(mockUser.getEmail()).thenReturn("test@example.com");
        
        mockReport = mock(Report.class);
        when(mockReport.getId()).thenReturn(1L);
        when(mockReport.getReportedType()).thenReturn(ReportType.ARTWORK);
        when(mockReport.getReportedId()).thenReturn(1L);
        when(mockReport.getUser()).thenReturn(mockUser);
        when(mockReport.getReason()).thenReturn("부적절한 콘텐츠");
        when(mockReport.getStatus()).thenReturn(ReportStatus.PENDING);
        when(mockReport.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(mockReport.getProcessedAt()).thenReturn(null);
        when(mockReport.getAdmin()).thenReturn(null);
    }
    
    @Test
    void getReportList_Success() {
        // Given
        AdminReportSearchCriteria criteria = new AdminReportSearchCriteria(
                null, ReportStatus.PENDING, null, null, 
                null, null, null, 0, 20, "createdAt", "desc"
        );
        
        Page<Report> mockPage = new PageImpl<>(List.of(mockReport));
        when(reportRepository.findByStatusOrderByCreatedAtDesc(any(ReportStatus.class), any(Pageable.class)))
                .thenReturn(mockPage);
        when(artworkRepository.findById(1L)).thenReturn(Optional.of(mock(org.example.studiopick.domain.artwork.Artwork.class)));
        
        // When
        Page<AdminReportListResponse> result = adminReportService.getReportList(criteria);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(reportRepository).findByStatusOrderByCreatedAtDesc(any(), any());
    }
    
    @Test
    void processReport_Success() {
        // Given
        Long reportId = 1L;
        Long adminId = 2L;
        AdminReportProcessCommand command = new AdminReportProcessCommand(
                ReportStatus.REVIEWED, 
                "검토 완료"
        );
        
        // When
        adminReportService.processReport(reportId, adminId, command);
        
        // Then
        verify(reportService).processReport(reportId, adminId, ReportStatus.REVIEWED, "검토 완료");
    }
    
    @Test
    void getPendingReportCount_Success() {
        // Given
        long expectedCount = 10L;
        when(reportRepository.countByStatus(ReportStatus.PENDING)).thenReturn(expectedCount);
        
        // When
        long result = adminReportService.getPendingReportCount();
        
        // Then
        assertEquals(expectedCount, result);
        verify(reportRepository).countByStatus(ReportStatus.PENDING);
    }
}