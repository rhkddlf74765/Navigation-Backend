package com.example.campus_navigation_backend.domain.navigation;

/**
 * 네비게이션 도착점을 표현하는 값 객체이다.
 *
 * @param buildingName 도착 건물명
 */
public record DestPoint(String buildingName) {

    /**
     * 도착 건물명이 비어 있지 않은지 검증한다.
     *
     * @param buildingName 도착 건물명
     */
    public DestPoint {
        if (buildingName == null || buildingName.isBlank()) {
            throw new IllegalArgumentException("Destination building name must not be blank.");
        }
    }
}
