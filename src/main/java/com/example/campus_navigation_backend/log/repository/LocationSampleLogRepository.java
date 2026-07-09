package com.example.campus_navigation_backend.log.repository;

import com.example.campus_navigation_backend.log.domain.LocationSampleLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * 위치 샘플 로그를 저장하고 사용자 또는 라우팅 세션 기준으로 조회하는 JPA 리포지토리이다.
 */
public interface LocationSampleLogRepository extends JpaRepository<LocationSampleLogEntity, Long> {

    /**
     * 특정 사용자가 전송한 위치 샘플 로그를 조회한다.
     */
    List<LocationSampleLogEntity> findByUserId(UUID userId);

    /**
     * 특정 라우팅 세션에 연결된 위치 샘플 로그를 조회한다.
     */
    List<LocationSampleLogEntity> findByRouteSessionId(UUID routeSessionId);
}
