package com.example.campus_navigation_backend.log.repository;

import com.example.campus_navigation_backend.log.domain.RouteResultLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * 라우팅 결과 요약과 반환된 경로 좌표를 저장하는 JPA 리포지토리이다.
 */
public interface RouteResultLogRepository extends JpaRepository<RouteResultLogEntity, UUID> {
}
