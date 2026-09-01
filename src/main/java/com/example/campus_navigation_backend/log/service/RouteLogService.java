package com.example.campus_navigation_backend.log.service;

import com.example.campus_navigation_backend.application.dto.RoutePoint;
import com.example.campus_navigation_backend.application.dto.RouteRequest;
import com.example.campus_navigation_backend.log.domain.RouteSessionLogEntity;
import com.example.campus_navigation_backend.log.repository.RouteSessionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RouteLogService {

    private final RouteSessionLogRepository routeSessionLogRepository;
    private final ObjectMapper objectMapper;

    /*
     * 경로 요청을 받는 즉시 세션과 원본 요청을 저장한다.
     *
     * 라우팅 실패와 무관하게 반드시 남아야 하므로
     * 별도 트랜잭션으로 커밋한다.
     */
    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public void saveRouteRequested(
            UUID routeSessionId,
            RouteRequest request,
            Instant requestedAt
    ) {
        String requestJson =
                toJson(request);

        RouteSessionLogEntity session =
                RouteSessionLogEntity.requested(
                        routeSessionId,
                        request.userId(),
                        requestedAt,
                        requestJson
                );

        routeSessionLogRepository.save(session);
    }

    /*
     * 경로 계산 성공 결과를 동일한 session row에 기록한다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveRouteReturned(
            UUID routeSessionId,
            double totalDistanceMeters,
            double totalCost,
            long expectedTimeSeconds,
            List<RoutePoint> path,
            Instant respondedAt
    ) {
        RouteSessionLogEntity session =
                findSession(routeSessionId);

        session.markRouteReturned(
                totalDistanceMeters,
                totalCost,
                expectedTimeSeconds,
                toJson(path),
                respondedAt
        );
    }

    /*
     * 경로 계산 실패를 기존 session row에 기록한다.
     */
    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public void saveRouteFailed(
            UUID routeSessionId,
            Instant respondedAt,
            Exception cause
    ) {
        RouteSessionLogEntity session =
                findSession(routeSessionId);

        String message =
                cause.getMessage() == null
                        ? cause.getClass().getSimpleName()
                        : cause.getMessage();

        session.markRouteFailed(
                message,
                respondedAt
        );
    }

    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public void markNavigationStarted(
            UUID routeSessionId
    ) {
        RouteSessionLogEntity session =
                findSession(routeSessionId);

        session.startNavigation();
    }

    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public void markArrived(
            UUID routeSessionId,
            Instant arrivedAt
    ) {
        RouteSessionLogEntity session =
                findSession(routeSessionId);

        session.markArrived(arrivedAt);
    }

    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public void markCancelled(
            UUID routeSessionId,
            Instant cancelledAt
    ) {
        RouteSessionLogEntity session =
                findSession(routeSessionId);

        session.markCancelled(cancelledAt);
    }

    private RouteSessionLogEntity findSession(
            UUID routeSessionId
    ) {
        return routeSessionLogRepository
                .findById(routeSessionId)
                .orElseThrow(
                        () -> new IllegalStateException(
                                "Route session log not found. routeSessionId="
                                        + routeSessionId
                        )
                );
    }

    private String toJson(
            Object value
    ) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Failed to serialize route log.",
                    exception
            );
        }
    }
}