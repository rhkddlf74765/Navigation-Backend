package com.example.campus_navigation_backend.log.repository;

import com.example.campus_navigation_backend.log.domain.RouteEventLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * 라우팅 세션 중 발생한 이벤트 로그를 저장하고 세션 기준으로 조회하는 JPA 리포지토리이다.
 */
public interface RouteEventLogRepository extends JpaRepository<RouteEventLogEntity, Long> {

    /**
     * 특정 라우팅 세션에서 발생한 이벤트 로그 목록을 조회한다.
     */
    List<RouteEventLogEntity> findByRouteSessionId(UUID routeSessionId);
}
