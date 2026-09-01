package com.example.campus_navigation_backend.infrastructure.building;

import com.example.campus_navigation_backend.config.NavigationProperties;
import com.example.campus_navigation_backend.domain.building.BuildingRef;
import com.example.campus_navigation_backend.domain.building.BuildingSpatialRepository;
import com.example.campus_navigation_backend.domain.building.ContainingBuilding;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.OptionalDouble;

@Repository
public class JdbcBuildingSpatialRepository
        implements BuildingSpatialRepository {

    private final NamedParameterJdbcTemplate jdbc;

    private final NavigationProperties properties;

    public JdbcBuildingSpatialRepository(
            NamedParameterJdbcTemplate jdbc,
            NavigationProperties properties
    ) {
        this.jdbc = jdbc;
        this.properties = properties;
    }

    @Override
    public Optional<BuildingRef> findByName(
            String buildingName
    ) {
        String sql = """
                SELECT
                    id,
                    name
                FROM public.buildings
                WHERE name IS NOT NULL
                  AND BTRIM(name) <> ''
                  AND LOWER(BTRIM(name)) =
                      LOWER(BTRIM(:buildingName))
                LIMIT 1
                """;

        return jdbc.query(
                        sql,
                        new MapSqlParameterSource()
                                .addValue(
                                        "buildingName",
                                        buildingName
                                ),
                        (rs, rowNum) ->
                                new BuildingRef(
                                        rs.getLong("id"),
                                        rs.getString("name")
                                )
                )
                .stream()
                .findFirst();
    }

    @Override
    public Optional<ContainingBuilding> findContaining(
            double lat,
            double lon
    ) {
        String sql = """
                WITH user_point AS (
                    SELECT ST_Transform(
                        ST_SetSRID(
                            ST_MakePoint(:lon, :lat),
                            :requestSrid
                        ),
                        :buildingSrid
                    ) AS geom
                )
                SELECT
                    b.id,
                    b.name,
                    ST_Distance(
                        ST_Boundary(b.geom),
                        user_point.geom
                    ) AS boundary_distance
                FROM public.buildings b
                CROSS JOIN user_point
                WHERE b.geom IS NOT NULL
                  AND b.name IS NOT NULL
                  AND BTRIM(b.name) <> ''
                  AND ST_Covers(
                      b.geom,
                      user_point.geom
                  )
                ORDER BY boundary_distance DESC, b.id ASC
                LIMIT 1
                """;

        return jdbc.query(
                        sql,
                        coordinateParams(
                                lat,
                                lon
                        ),
                        (rs, rowNum) ->
                                new ContainingBuilding(
                                        rs.getLong("id"),
                                        rs.getString("name"),
                                        rs.getDouble(
                                                "boundary_distance"
                                        )
                                )
                )
                .stream()
                .findFirst();
    }

    @Override
    public OptionalDouble findNearestDistanceMeters(
            double lat,
            double lon
    ) {
        String sql = """
                WITH user_point AS (
                    SELECT ST_Transform(
                        ST_SetSRID(
                            ST_MakePoint(:lon, :lat),
                            :requestSrid
                        ),
                        :buildingSrid
                    ) AS geom
                )
                SELECT ST_Distance(
                    b.geom,
                    user_point.geom
                ) AS distance_meters
                FROM public.buildings b
                CROSS JOIN user_point
                WHERE b.geom IS NOT NULL
                  AND b.name IS NOT NULL
                  AND BTRIM(b.name) <> ''
                ORDER BY distance_meters ASC, b.id ASC
                LIMIT 1
                """;

        return jdbc.query(
                        sql,
                        coordinateParams(
                                lat,
                                lon
                        ),
                        (rs, rowNum) ->
                                rs.getDouble(
                                        "distance_meters"
                                )
                )
                .stream()
                .findFirst()
                .map(OptionalDouble::of)
                .orElseGet(
                        OptionalDouble::empty
                );
    }

    private MapSqlParameterSource coordinateParams(
            double lat,
            double lon
    ) {
        return new MapSqlParameterSource()
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
                        "buildingSrid",
                        properties.buildingSrid()
                );
    }
}
