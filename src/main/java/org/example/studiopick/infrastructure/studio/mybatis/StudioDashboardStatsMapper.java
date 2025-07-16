package org.example.studiopick.infrastructure.studio.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Mapper
public interface StudioDashboardStatsMapper {

    long countTodayReservations(@Param("studioId") Long studioId, @Param("today") LocalDate today);

    long countMonthReservations(@Param("studioId") Long studioId,
                                @Param("start") LocalDate start,
                                @Param("end") LocalDate end);

    Long sumTodayRevenue(@Param("studioId") Long studioId, @Param("today") LocalDate today);

    Long sumMonthRevenue(@Param("studioId") Long studioId,
                         @Param("start") LocalDate start,
                         @Param("end") LocalDate end);

    long countNewCustomers(@Param("start") LocalDateTime start,
                           @Param("end") LocalDateTime end);

    long countClassesByStudioId(@Param("studioId") Long studioId);
}

