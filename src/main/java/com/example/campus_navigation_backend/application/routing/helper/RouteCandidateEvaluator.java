package com.example.campus_navigation_backend.application.routing.helper;

import com.example.campus_navigation_backend.domain.path.AStarPathFinder;
import com.example.campus_navigation_backend.domain.path.PathResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 도달 가능한 모든 출발 endpoint와 도착 endpoint 조합을 평가한다.
 * <p>
 * 각 후보는 출발 접근 경로, A* 그래프 경로, 도착 접근 경로 순서로 결합되며,
 * 평가기는 전체 비용이 가장 작은 결합 후보를 반환한다.
 */
@Component
@RequiredArgsConstructor
public class RouteCandidateEvaluator {

    private final AStarPathFinder pathFinder;

    /**
     * 임의 좌표가 여러 인접 그래프 endpoint에 투영될 수 있고 최종 선택은 전체 결합 비용으로만 판단할 수 있으므로
     * 모든 출발/도착 접근 경로 쌍에 대해 A*를 수행한다.
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

    /**
     * 후보 비교가 클라이언트에 반환될 결합 경로와 같은 기준으로 수행되도록 세 개의 경로 부분을 하나의 결과 객체로 합친다.
     */
    private static RouteCandidateResult getRouteCandidateResult(EndpointAccessPath startAccessPath,
                                                                EndpointAccessPath destinationAccessPath,
                                                                RoutePath graphPath) {
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
