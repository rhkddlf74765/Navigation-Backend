package com.example.campus_navigation_backend.repository;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class JdbcGraphDataRepositorySqlTest {

    @Test
    void graphNodeQueryJoinsEntranceThroughExplicitEntranceId()
            throws IOException {

        String source =
                Files.readString(
                        Path.of(
                                "src/main/java/com/example/campus_navigation_backend/repository/JdbcGraphDataRepository.java"
                        )
                );

        assertThat(source)
                .contains(
                        "FROM routing.graph_nodes n"
                )
                .contains(
                        "LEFT JOIN spatial.entrances e"
                )
                .contains(
                        "e.id = n.entrance_id"
                )
                .contains(
                        "LEFT JOIN spatial.buildings b"
                )
                .contains(
                        "n.graph_version_id"
                )
                .doesNotContain(
                        "e.id = n.id"
                )
                .doesNotContain(
                        "public.final_nodes_3d"
                );
    }

    @Test
    void graphEdgeQueryUsesVersionedRoutingGraph()
            throws IOException {

        String source =
                Files.readString(
                        Path.of(
                                "src/main/java/com/example/campus_navigation_backend/repository/JdbcGraphDataRepository.java"
                        )
                );

        assertThat(source)
                .contains(
                        "FROM routing.graph_edges"
                )
                .contains(
                        "graph_version_id"
                )
                .contains(
                        "is_enabled = TRUE"
                )
                .doesNotContain(
                        "public.final_edges_split_3d"
                );
    }
}
