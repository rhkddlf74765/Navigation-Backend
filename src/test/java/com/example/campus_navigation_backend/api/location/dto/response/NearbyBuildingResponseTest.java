package com.example.campus_navigation_backend.api.location.dto.response;

import com.example.campus_navigation_backend.domain.nearbyBuilding.NearbyBuilding;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NearbyBuildingResponseTest {

    @Test
    void mapsBuildingNameAndDescriptionAsSeparateMeanings() {
        NearbyBuilding building =
                new NearbyBuilding(
                        10L,
                        "Engineering Building",
                        "Computer software laboratories and lecture rooms.",
                        37.0,
                        127.0,
                        null,
                        12.5
                );

        NearbyBuildingResponse response =
                NearbyBuildingResponse.from(
                        building
                );

        assertThat(response.name())
                .isEqualTo(
                        "Engineering Building"
                );
        assertThat(response.description())
                .isNotNull();
        assertThat(response.description().text())
                .isEqualTo(
                        "Computer software laboratories and lecture rooms."
                );
    }

    @Test
    void keepsNullBuildingDescriptionAsNull() {
        NearbyBuilding building =
                new NearbyBuilding(
                        10L,
                        "Engineering Building",
                        null,
                        37.0,
                        127.0,
                        null,
                        12.5
                );

        NearbyBuildingResponse response =
                NearbyBuildingResponse.from(
                        building
                );

        assertThat(response.description())
                .isNull();
    }
}
