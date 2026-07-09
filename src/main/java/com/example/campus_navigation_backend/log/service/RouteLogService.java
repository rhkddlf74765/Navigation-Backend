package com.example.campus_navigation_backend.log.service;

import com.example.campus_navigation_backend.application.dto.RouteEndpointRequest;
import com.example.campus_navigation_backend.application.dto.RoutePoint;
import com.example.campus_navigation_backend.application.dto.RouteRequest;
import com.example.campus_navigation_backend.application.dto.RouteResponse;
import com.example.campus_navigation_backend.log.domain.RouteEndpointLogEntity;
import com.example.campus_navigation_backend.log.domain.RouteEndpointRole;
import com.example.campus_navigation_backend.log.domain.RouteEventLogEntity;
import com.example.campus_navigation_backend.log.domain.RouteEventType;
import com.example.campus_navigation_backend.log.domain.RoutePathPointLog;
import com.example.campus_navigation_backend.log.domain.RouteResultLogEntity;
import com.example.campus_navigation_backend.log.domain.RouteSessionLogEntity;
import com.example.campus_navigation_backend.log.repository.RouteEndpointLogRepository;
import com.example.campus_navigation_backend.log.repository.RouteEventLogRepository;
import com.example.campus_navigation_backend.log.repository.RouteResultLogRepository;
import com.example.campus_navigation_backend.log.repository.RouteSessionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * 라우팅 요청과 응답 결과를 세션, 입력 지점, 결과, 이벤트 로그로 나누어 저장하는 서비스이다.
 */
@Service
@RequiredArgsConstructor
public class RouteLogService {

    private final RouteSessionLogRepository routeSessionLogRepository;
    private final RouteEndpointLogRepository routeEndpointLogRepository;
    private final RouteResultLogRepository routeResultLogRepository;
    private final RouteEventLogRepository routeEventLogRepository;
    private final ObjectMapper objectMapper;

    /**
     * 라우팅이 정상 반환되었을 때 세션 상태, 요청 지점, 반환 경로, 반환 이벤트를 함께 기록한다.
     *
     * @param request 라우팅 요청
     * @param response 라우팅 응답
     * @param requestedAt 라우팅 요청 수신 시각
     * @param respondedAt 라우팅 응답 생성 시각
     */
    @Transactional
    public void saveRouteReturned(RouteRequest request, RouteResponse response, Instant requestedAt, Instant respondedAt) {
        RouteSessionLogEntity session = RouteSessionLogEntity.requested(
                response.routeSessionId(),
                request.userId(),
                requestedAt
        );
        session.markRouteReturned(respondedAt);

        routeSessionLogRepository.save(session);
        routeEndpointLogRepository.save(toEndpoint(response.routeSessionId(), RouteEndpointRole.START, request.start()));
        routeEndpointLogRepository.save(toEndpoint(response.routeSessionId(), RouteEndpointRole.DESTINATION, request.destination()));

        routeResultLogRepository.save(RouteResultLogEntity.create(
                response.routeSessionId(),
                response.destinationBuildingName(),
                response.selectedEntranceId(),
                response.totalDistanceMeters(),
                response.approachDistanceMeters(),
                response.graphDistanceMeters(),
                toPathJson(response.path()),
                respondedAt
        ));

        routeEventLogRepository.save(RouteEventLogEntity.create(
                response.routeSessionId(),
                request.userId(),
                RouteEventType.ROUTE_RETURNED,
                null,
                respondedAt
        ));
    }

    /**
     * 라우팅 처리에 실패했을 때 실패한 세션, 가능한 요청 지점, 실패 이벤트를 기록한다.
     *
     * @param request 라우팅 요청
     * @param routeSessionId 실패한 라우팅 세션 ID
     * @param requestedAt 라우팅 요청 수신 시각
     * @param respondedAt 실패 응답 생성 시각
     * @param cause 실패 원인 예외
     */
    @Transactional
    public void saveRouteFailed(RouteRequest request, UUID routeSessionId, Instant requestedAt, Instant respondedAt, Exception cause) {
        RouteSessionLogEntity session = RouteSessionLogEntity.requested(routeSessionId, request.userId(), requestedAt);
        session.markRouteFailed(cause.getMessage(), respondedAt);

        routeSessionLogRepository.save(session);
        saveEndpointIfPresent(routeSessionId, RouteEndpointRole.START, request.start());
        saveEndpointIfPresent(routeSessionId, RouteEndpointRole.DESTINATION, request.destination());
        routeEventLogRepository.save(RouteEventLogEntity.create(
                routeSessionId,
                request.userId(),
                RouteEventType.ROUTE_FAILED,
                cause.getMessage(),
                respondedAt
        ));
    }

    /**
     * 요청 지점 정보가 존재할 때만 지점 로그를 저장한다.
     */
    private void saveEndpointIfPresent(UUID routeSessionId, RouteEndpointRole role, RouteEndpointRequest endpoint) {
        if (endpoint == null || endpoint.type() == null) {
            return;
        }
        routeEndpointLogRepository.save(toEndpoint(routeSessionId, role, endpoint));
    }

    /**
     * 라우팅 요청 지점을 로그 저장용 엔티티로 변환한다.
     */
    private RouteEndpointLogEntity toEndpoint(UUID routeSessionId, RouteEndpointRole role, RouteEndpointRequest endpoint) {
        return switch (endpoint.type()) {
            case COORDINATE -> RouteEndpointLogEntity.coordinate(
                    routeSessionId,
                    role,
                    endpoint.lon(),
                    endpoint.lat(),
                    endpoint.ele()
            );
            case BUILDING -> RouteEndpointLogEntity.building(
                    routeSessionId,
                    role,
                    endpoint.buildingName()
            );
        };
    }

    /**
     * 응답 경로의 좌표 한 점을 로그 저장용 값 객체로 변환한다.
     */
    private String toPathJson(List<RoutePoint> points) {
        try {
            List<RoutePathPointLog> path = points.stream()
                    .map(this::toPathPoint)
                    .toList();
            return objectMapper.writeValueAsString(path);
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to serialize route path log.", e);
        }
    }

    private RoutePathPointLog toPathPoint(RoutePoint point) {
        return RoutePathPointLog.of(point.lon(), point.lat(), point.ele());
    }
}
