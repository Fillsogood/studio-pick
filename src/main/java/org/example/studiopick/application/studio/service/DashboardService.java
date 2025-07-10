package org.example.studiopick.application.studio.service;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.studio.dto.DashboardResponseDto;
import org.example.studiopick.domain.user.repository.UserRepository;
import org.example.studiopick.domain.studio.repository.DashboardClassRepository;
import org.example.studiopick.infrastructure.reservation.JpaReservationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final JpaReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final DashboardClassRepository classRepository;

    public DashboardResponseDto getStudioDashboard(Long studioId) {
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.now();
        LocalDate monthStart = currentMonth.atDay(1);
        LocalDate monthEnd = currentMonth.atEndOfMonth();

//        // 예약 수
//        long todayReservationCount = reservationRepository.countByReservationDateBetween(today, today);
//        long monthReservationCount = reservationRepository.countByReservationDateBetween(monthStart, monthEnd);
//
//        // 매출
//        long todayRevenue = reservationRepository
//                .sumTotalAmountByReservationDate(today) != null ?
//                reservationRepository.sumTotalAmountByReservationDate(today) : 0L;
//
//        long monthRevenue = reservationRepository
//                .sumTotalAmountByReservationDateBetween(monthStart, monthEnd) != null ?
//                reservationRepository.sumTotalAmountByReservationDateBetween(monthStart, monthEnd) : 0L;
//
//        // 신규 고객 (이번 달 가입)
//        long newCustomerCount = userRepository.countByCreatedAtBetween(monthStart.atStartOfDay(), monthEnd.atTime(23, 59, 59));
//
//        // 클래스 수
//        long classCount = classRepository.countByStudioId(studioId);
//
//        return DashboardResponseDto.builder()
//                .todayReservationCount(todayReservationCount)
//                .monthReservationCount(monthReservationCount)
//                .todayRevenue(todayRevenue)
//                .monthRevenue(monthRevenue)
//                .newCustomerCount(newCustomerCount)
//                .classCount(classCount)
//                .build();
//    }
        return null;
    }
}

