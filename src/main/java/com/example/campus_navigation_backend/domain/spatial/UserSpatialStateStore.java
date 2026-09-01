package com.example.campus_navigation_backend.domain.spatial;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public interface UserSpatialStateStore {

    Optional<UserSpatialState> findByUserId(
            UUID userId
    );

    UserSpatialState update(
            UUID userId,
            Function<UserSpatialState, UserSpatialState>
                    updater
    );
}
