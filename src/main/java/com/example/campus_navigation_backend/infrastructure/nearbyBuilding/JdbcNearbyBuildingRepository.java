package com.example.campus_navigation_backend.infrastructure.nearbyBuilding;

import com.example.campus_navigation_backend.config.NavigationProperties;
import com.example.campus_navigation_backend.domain.nearbyBuilding.NearbyBuilding;
import com.example.campus_navigation_backend.domain.nearbyBuilding.NearbyBuildingRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JdbcNearbyBuildingRepository
        implements NearbyBuildingRepository {

    private final NamedParameterJdbcTemplate jdbc;

    private final NavigationProperties properties;

    public JdbcNearbyBuildingRepository(
            NamedParameterJdbcTemplate jdbc,
            NavigationProperties properties
    ) {
        this.jdbc = jdbc;
        this.properties = properties;
    }

    @Override
    public List<NearbyBuilding> findNearby(
            double lat,
            double lon,
            double radiusMeters,
            int limit
    ) {
        String sql = """
        WITH user_point AS (
            SELECT ST_Transform(
                ST_SetSRID(
                    ST_MakePoint(
                        :lon,
                        :lat
                    ),
                    :requestSrid
                ),
                :metricSrid
            ) AS geom
        ),

        nearby AS (
            SELECT
                b.id,
                b.name,
                b.description,

                ST_PointOnSurface(
                    b.geom
                ) AS marker_geom,

                ST_Distance(
                    b.geom,
                    user_point.geom
                ) AS distance_meters

            FROM spatial.buildings b
            CROSS JOIN user_point

            WHERE b.is_operational = TRUE
              AND b.geom IS NOT NULL
              AND b.name IS NOT NULL
              AND BTRIM(b.name) <> ''

              AND ST_DWithin(
                  b.geom,
                  user_point.geom,
                  :radiusMeters
              )

            ORDER BY
                distance_meters ASC,
                b.id ASC

            LIMIT :limit
        )

        SELECT
            id,
            name,
            description,

            ST_Y(
                ST_Transform(
                    marker_geom,
                    :requestSrid
                )
            ) AS lat,

            ST_X(
                ST_Transform(
                    marker_geom,
                    :requestSrid
                )
            ) AS lon,

            distance_meters

        FROM nearby

        ORDER BY
            distance_meters ASC,
            id ASC
        """;

        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue(
                                "lat",
                                lat
                        )
                        .addValue(
                                "lon",
                                lon
                        )
                        .addValue(
                                "requestSrid",
                                properties.requestSrid()
                        )
                        .addValue(
                                "metricSrid",
                                properties.metricSrid()
                        )
                        .addValue(
                                "radiusMeters",
                                radiusMeters
                        )
                        .addValue(
                                "limit",
                                limit
                        );

        return jdbc.query(
                sql,
                params,
                (rs, rowNum) ->
                        new NearbyBuilding(
                                rs.getLong("id"),
                                rs.getString("name"),
                                rs.getString("description"),
                                rs.getDouble("lat"),
                                rs.getDouble("lon"),
                                null,
                                rs.getDouble(
                                        "distance_meters"
                                )
                        )
        );
    }
}
