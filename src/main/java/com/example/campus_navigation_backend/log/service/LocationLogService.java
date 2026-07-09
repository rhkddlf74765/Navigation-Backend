package com.example.campus_navigation_backend.log.service;

import com.example.campus_navigation_backend.api.location.dto.LocationCoordinateRequest;
import com.example.campus_navigation_backend.api.location.dto.LocationSampleRequest;
import com.example.campus_navigation_backend.log.domain.LocationSampleLogEntity;
import com.example.campus_navigation_backend.log.domain.PointLog;
import com.example.campus_navigation_backend.log.repository.LocationSampleLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * 위치 샘플 요청을 위치 로그 엔티티로 변환해 저장하는 서비스이다.
 */
@Service
@RequiredArgsConstructor
public class LocationLogService {

    private final LocationSampleLogRepository locationSampleLogRepository;

    /**
     * 클라이언트가 전송한 GPS 좌표와 실제 좌표를 위치 샘플 로그로 저장한다.
     *
     * @param request 위치 샘플 요청
     * @param errorMeters GPS 추정 좌표와 실제 좌표 사이의 거리 오차
     * @param receivedAt 서버가 요청을 수신한 시각
     * @return 저장된 위치 샘플 로그 ID
     */
    public Long save(LocationSampleRequest request, Double errorMeters, Instant receivedAt) {
        LocationCoordinateRequest gps = request.gps();
        LocationCoordinateRequest actual = request.actual();

        LocationSampleLogEntity saved = locationSampleLogRepository.save(LocationSampleLogEntity.create(
                request.userId(),
                request.routeSessionId(),
                PointLog.of(gps.lon(), gps.lat(), gps.ele()),
                actual == null ? null : PointLog.of(actual.lon(), actual.lat(), actual.ele()),
                errorMeters,
                request.recordedAt(),
                receivedAt
        ));

        return saved.getId();
    }
}
