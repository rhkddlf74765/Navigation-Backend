package com.example.campus_navigation_backend.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JdbcGraphVersionRepository
        implements GraphVersionRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcGraphVersionRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }

    @Override
    public String findStatus(
            long graphVersionId
    ) {

        return jdbc.queryForObject(
                """
                SELECT status
                FROM routing.graph_versions
                WHERE id = :id
                """,
                new MapSqlParameterSource()
                        .addValue("id", graphVersionId),
                String.class
        );
    }

    @Override
    public void markValidating(
            long graphVersionId
    ) {

        jdbc.update(
                """
                UPDATE routing.graph_versions
                SET status = 'VALIDATING'
                WHERE id = :id
                  AND status = 'BUILDING'
                """,
                new MapSqlParameterSource()
                        .addValue("id", graphVersionId)
        );
    }

    @Override
    public void markFailed(
            long graphVersionId
    ) {

        jdbc.update(
                """
                UPDATE routing.graph_versions
                SET status = 'FAILED',
                    validated_at = NOW()
                WHERE id = :id
                """,
                new MapSqlParameterSource()
                        .addValue("id", graphVersionId)
        );
    }

    @Override
    @Transactional
    public void activateReplacingCurrent(
            long graphVersionId
    ) {

        jdbc.update(
                """
                UPDATE routing.graph_versions
                SET status = 'RETIRED'
                WHERE status = 'ACTIVE'
                  AND id <> :id
                """,
                new MapSqlParameterSource()
                        .addValue("id", graphVersionId)
        );

        int updated =
                jdbc.update(
                        """
                        UPDATE routing.graph_versions
                        SET
                            status = 'ACTIVE',
                            validated_at =
                                COALESCE(
                                    validated_at,
                                    NOW()
                                ),
                            activated_at = NOW()
                        WHERE id = :id
                          AND status = 'VALIDATING'
                        """,
                        new MapSqlParameterSource()
                                .addValue(
                                        "id",
                                        graphVersionId
                                )
                );

        if (updated != 1) {
            throw new IllegalStateException(
                    "Failed to activate graph version. id="
                            + graphVersionId
            );
        }
    }
}
