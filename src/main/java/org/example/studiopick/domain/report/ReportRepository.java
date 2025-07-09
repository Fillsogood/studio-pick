package org.example.studiopick.domain.report;

import org.example.studiopick.domain.common.enums.ReportStatus;
import org.example.studiopick.domain.common.enums.ReportType;
import org.example.studiopick.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ReportRepository extends JpaRepository<Report, Long> {

    boolean existsByUserAndReportedTypeAndReportedId(User user, ReportType reportedType, Long reportedId);
    long countByReportedTypeAndReportedIdAndStatus(ReportType reportedType, Long reportedId, ReportStatus status);
}
