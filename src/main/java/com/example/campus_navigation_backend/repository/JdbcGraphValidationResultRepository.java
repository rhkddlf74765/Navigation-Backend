package com.example.campus_navigation_backend.repository;

import com.example.campus_navigation_backend.domain.graph.validation.GraphValidationIssue;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class JdbcGraphValidationResultRepository
        implements GraphValidationResultRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcGraphValidationResultRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }

    @Override
    @Transactional
    public void replaceResults(
            long graphVersionId,
            List<GraphValidationIssue> issues
    ) {

        jdbc.update(
                """
                DELETE FROM
                    routing.graph_validation_results
                WHERE graph_version_id =
                      :graphVersionId
                """,
                new MapSqlParameterSource()
                        .addValue(
                                "graphVersionId",
                                graphVersionId
                        )
        );

        String insertSql = """
                INSERT INTO
                    routing.graph_validation_results
                (
                    graph_version_id,
                    rule_code,
                    severity,
                    entity_type,
                    entity_id,
                    details
                )
                VALUES
                (
                    :graphVersionId,
                    :ruleCode,
                    :severity,
                    :entityType,
                    :entityId,

                    jsonb_build_object(
                        'message',
                        :message
                    )
                )
                """;

        for (GraphValidationIssue issue
                : issues) {

            jdbc.update(
                    insertSql,
                    new MapSqlParameterSource()
                            .addValue(
                                    "graphVersionId",
                                    graphVersionId
                            )
                            .addValue(
                                    "ruleCode",
                                    issue.ruleCode()
                            )
                            .addValue(
                                    "severity",
                                    issue.severity()
                                            .name()
                            )
                            .addValue(
                                    "entityType",
                                    issue.entityType()
                            )
                            .addValue(
                                    "entityId",
                                    issue.entityId()
                            )
                            .addValue(
                                    "message",
                                    issue.message()
                            )
            );
        }
    }
}