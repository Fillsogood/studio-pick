package org.example.studiopick.infrastructure.reservation.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.studiopick.application.admin.dto.reservation.AdminReservationResponse;
import org.example.studiopick.application.reservation.dto.UserReservationResponse;
import org.example.studiopick.infrastructure.reservation.mybatis.dto.ReservationSearchCriteria;
import org.example.studiopick.infrastructure.reservation.mybatis.dto.UserReservationSearchCriteria;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 복잡한 예약 검색용 MyBatis Mapper
 * - 다중 조건 필터링
 * - 동적 쿼리 처리
 * - 페이징 지원
 */
@Mapper
@Repository
public interface ReservationSearchMapper {
    
    /**
     * 관리자용 복잡한 예약 검색
     * @param criteria 검색 조건
     * @return 예약 목록
     */
    List<AdminReservationResponse> searchReservations(@Param("criteria") ReservationSearchCriteria criteria);
    
    /**
     * 관리자용 예약 검색 총 개수
     * @param criteria 검색 조건
     * @return 총 개수
     */
    long countReservations(@Param("criteria") ReservationSearchCriteria criteria);
    
    /**
     * 사용자별 예약 목록 (필터링 포함)
     * @param criteria 검색 조건
     * @return 사용자 예약 목록
     */
    List<UserReservationResponse> searchUserReservations(UserReservationSearchCriteria criteria);

    /**
     * 사용자별 예약 총 개수
     * @param criteria 검색 조건
     * @return 총 개수
     */
    long countUserReservations(@Param("criteria") UserReservationSearchCriteria criteria);
}
