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
                        "e.id = n.entrance_id"
                );

        assertThat(source)
                .doesNotContain(
                        "e.id = n.id"
                );
    }
}
