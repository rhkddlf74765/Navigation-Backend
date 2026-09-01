package com.example.campus_navigation_backend.service.location;

import com.example.campus_navigation_backend.api.location.dto.request.LocationCoordinateRequest;
import com.example.campus_navigation_backend.api.location.dto.request.LocationSampleRequest;
import com.example.campus_navigation_backend.api.location.dto.response.LocationSampleResponse;
import com.example.campus_navigation_backend.log.service.LocationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * 위치 샘플 저장 use case를 처리하는 서비스이다.
 * <p>
 * GPS 추정 좌표와 실제 기준 좌표가 함께 전달된 경우 두 좌표 사이의 거리 오차를 계산한 뒤 저장소에 기록한다.
 */
@Service
@RequiredArgsConstructor
public class LocationSampleService {

    private static final double EARTH_RADIUS_METERS = 6_371_000.0;

    private final LocationLogService locationLogService;

    /**
     * 위치 샘플을 저장하고, 실제 좌표가 있는 경우 GPS 오차를 계산해 응답에 포함한다.
     */
    public LocationSampleResponse save(LocationSampleRequest request) {
        Instant receivedAt = Instant.now();
        Double errorMeters = calculateErrorMeters(request.gps(), request.actual());
        Long id = locationLogService.save(request, errorMeters, receivedAt);

        return new LocationSampleResponse(id, errorMeters, receivedAt);
    }

    /**
     * 실제 기준 좌표가 있는 경우에만 GPS 좌표와 실제 좌표 사이의 거리 오차를 계산한다.
     */
    private Double calculateErrorMeters(LocationCoordinateRequest gps, LocationCoordinateRequest actual) {
        if (actual == null) {
            return null;
        }

        return haversineMeters(
                gps.lat(),
                gps.lon(),
                actual.lat(),
                actual.lon()
        );
    }

    /**
     * 위경도 두 점 사이의 지표면 근사 거리를 계산해 GPS 오차를 미터 단위로 저장할 수 있게 한다.
     */
    private double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);

        return EARTH_RADIUS_METERS * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
