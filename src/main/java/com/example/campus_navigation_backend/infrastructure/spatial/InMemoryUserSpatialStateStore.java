package com.example.campus_navigation_backend.infrastructure.spatial;

import com.example.campus_navigation_backend.domain.spatial.UserSpatialState;
import com.example.campus_navigation_backend.domain.spatial.UserSpatialStateStore;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

@Component
public class InMemoryUserSpatialStateStore
        implements UserSpatialStateStore {

    private final ConcurrentMap<UUID, UserSpatialState>
            states =
            new ConcurrentHashMap<>();

    @Override
    public Optional<UserSpatialState> findByUserId(
            UUID userId
    ) {
        return Optional.ofNullable(
                states.get(userId)
        );
    }

    @Override
    public UserSpatialState update(
            UUID userId,
            Function<UserSpatialState, UserSpatialState>
                    updater
    ) {
        return states.compute(
                userId,
                (ignored, current) ->
                        updater.apply(current)
        );
    }
}
