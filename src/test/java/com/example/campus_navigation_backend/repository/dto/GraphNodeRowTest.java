package com.example.campus_navigation_backend.repository.dto;

import com.example.campus_navigation_backend.domain.graph.GraphNodeType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GraphNodeRowTest {

    @Test
    void normalizedEntranceBecomesEntranceType() {
        GraphNodeRow row =
                new GraphNodeRow(
                        1L,
                        " entrance ",
                        null,
                        10L,
                        "Building",
                        0.0,
                        0.0,
                        0.0
                );

        assertThat(row.graphNodeType())
                .isEqualTo(
                        GraphNodeType.ENTRANCE
                );
    }

    @Test
    void legacyEntranceSubtypesDoNotBecomeSeparateGraphTypes() {
        assertThat(
                new GraphNodeRow(
                        1L,
                        "main_entrance",
                        null,
                        0.0,
                        0.0,
                        0.0
                ).graphNodeType()
        ).isEqualTo(
                GraphNodeType.BASE
        );

        assertThat(
                new GraphNodeRow(
                        2L,
                        "sub_entrance",
                        null,
                        0.0,
                        0.0,
                        0.0
                ).graphNodeType()
        ).isEqualTo(
                GraphNodeType.BASE
        );
    }
}
