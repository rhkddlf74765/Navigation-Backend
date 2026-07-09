package com.example.campus_navigation_backend.log.repository;

import com.example.campus_navigation_backend.log.domain.RouteArrivalLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * 사용자의 목적지 도착 확정 로그를 저장하는 JPA 리포지토리이다.
 */
public interface RouteArrivalLogRepository extends JpaRepository<RouteArrivalLogEntity, UUID> {
}
