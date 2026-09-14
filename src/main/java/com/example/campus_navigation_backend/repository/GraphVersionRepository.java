package com.example.campus_navigation_backend.repository;

public interface GraphVersionRepository {

    String findStatus(
            long graphVersionId
    );

    void markValidating(
            long graphVersionId
    );

    void markFailed(
            long graphVersionId
    );

    void activateReplacingCurrent(
            long graphVersionId
    );
}