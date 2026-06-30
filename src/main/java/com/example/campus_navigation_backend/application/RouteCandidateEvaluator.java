package com.example.campus_navigation_backend.application;

import com.example.campus_navigation_backend.domain.path.AStarPathFinder;
import com.example.campus_navigation_backend.domain.path.PathResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 출발 endpoint 후보와 도착 endpoint 후보의 모든 조합을 평가해 최저 비용 경로를 선택한다.
 * <p>
 * 각 후보 조합은 출발 접근 경로, A* 그래프 내부 경로, 도착 접근 경로를 순서대로 합쳐
 * 하나의 완전한 경로 후보가 된다.
 */
@Component
@RequiredArgsConstructor
public class RouteCandidateEvaluator {

    private final AStarPathFinder pathFinder;

    /**
     * 모든 출발/도착 endpoint 후보 조합에 대해 A*를 실행하고 총 비용이 가장 낮은 경로를 반환한다.
     *
     * @param startAccessPaths 출발 좌표에서 그래프 endpoint까지의 접근 후보
     * @param destinationAccessPaths 그래프 endpoint에서 도착 좌표까지의 접근 후보
     * @return 최저 비용의 완성된 라우팅 후보
     */
    public RouteCandidateResult findBestRoute(List<EndpointAccessPath> startAccessPaths,
                                              List<EndpointAccessPath> destinationAccessPaths) {
        RouteCandidateResult best = null;

        for (EndpointAccessPath startAccessPath : startAccessPaths) {
            for (EndpointAccessPath destinationAccessPath : destinationAccessPaths) {
                PathResult pathResult = pathFinder.findPath(
                        startAccessPath.endpointNodeId(),
                        destinationAccessPath.endpointNodeId()
                );
                if (!pathResult.found()) {
                    continue;
                }

                RoutePath graphPath = RoutePath.from(pathResult);
                RouteCandidateResult candidate = getRouteCandidateResult(startAccessPath, destinationAccessPath, graphPath);

                if (best == null || candidate.totalCost() < best.totalCost()) {
                    best = candidate;
                }
            }
        }

        if (best == null) {
            throw new IllegalStateException("No reachable route found.");
        }
        return best;
    }

    private static RouteCandidateResult getRouteCandidateResult(EndpointAccessPath startAccessPath, EndpointAccessPath destinationAccessPath, RoutePath graphPath) {
        RoutePath fullPath = startAccessPath.routePath()
                .append(graphPath)
                .append(destinationAccessPath.routePath());

        return new RouteCandidateResult(
                destinationAccessPath.endpointNodeId(),
                startAccessPath.routePath().cost(),
                graphPath.cost(),
                destinationAccessPath.routePath().cost(),
                fullPath
        );
    }
}
