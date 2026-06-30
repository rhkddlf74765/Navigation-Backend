package com.example.campus_navigation_backend.api.location;

import com.example.campus_navigation_backend.api.location.dto.LocationSampleRequest;
import com.example.campus_navigation_backend.api.location.dto.LocationSampleResponse;
import com.example.campus_navigation_backend.service.location.LocationSampleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 프론트에서 측정한 위치 샘플을 서버에 저장하는 HTTP API 컨트롤러이다.
 * <p>
 * GPS 추정 좌표는 필수로 받고, 실제 기준 좌표가 있는 경우에는 함께 받아 위치 오차 계산까지 수행한다.
 */
@RestController
@RequestMapping("/api/location-samples")
@RequiredArgsConstructor
public class LocationSampleController {

    private final LocationSampleService locationSampleService;

    /**
     * 위치 샘플 저장 요청을 응용 서비스에 위임하고 저장된 로그 식별자와 계산된 오차를 반환한다.
     */
    @PostMapping
    public LocationSampleResponse save(@Valid @RequestBody LocationSampleRequest request) {
        return locationSampleService.save(request);
    }
}
