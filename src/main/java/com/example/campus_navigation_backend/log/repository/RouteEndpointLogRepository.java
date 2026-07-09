package com.example.campus_navigation_backend.log.repository;

import com.example.campus_navigation_backend.log.domain.RouteEndpointLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 라우팅 요청의 출발지와 목적지 입력값을 저장하는 JPA 리포지토리이다.
 */
public interface RouteEndpointLogRepository extends JpaRepository<RouteEndpointLogEntity, Long> {
}
