package com.example.campus_navigation_backend.log.repository;

import com.example.campus_navigation_backend.log.domain.RouteSessionLogEntity;
import com.example.campus_navigation_backend.log.domain.RouteSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * 라우팅 세션 로그를 저장하고 사용자 또는 세션 상태 기준으로 조회하는 JPA 리포지토리이다.
 */
public interface RouteSessionLogRepository extends JpaRepository<RouteSessionLogEntity, UUID> {

    /**
     * 특정 사용자의 라우팅 세션 로그 목록을 조회한다.
     */
    List<RouteSessionLogEntity> findByUserId(UUID userId);

    /**
     * 특정 상태에 해당하는 라우팅 세션 로그 목록을 조회한다.
     */
    List<RouteSessionLogEntity> findByStatus(RouteSessionStatus status);
}
