package com.example.campus_navigation_backend.service.graph.validation;

import com.example.campus_navigation_backend.domain.graph.CampusGraph;
import com.example.campus_navigation_backend.domain.graph.GraphNode;
import com.example.campus_navigation_backend.domain.graph.validation.GraphValidationIssue;
import com.example.campus_navigation_backend.domain.graph.validation.GraphValidationSeverity;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class JavaGraphValidator {

    public List<GraphValidationIssue> validate(
            CampusGraph graph
    ) {

        List<GraphValidationIssue> issues =
                new ArrayList<>();

        validateIsolatedNodes(
                graph,
                issues
        );

        validateConnectivity(
                graph,
                issues
        );

        return List.copyOf(
                issues
        );
    }

    private void validateIsolatedNodes(
            CampusGraph graph,
            List<GraphValidationIssue> issues
    ) {

        for (GraphNode node
                : graph.getNodes()) {

            if (!graph.getBaseAdjacency(
                    node.id()
            ).isEmpty()) {

                continue;
            }

            issues.add(
                    new GraphValidationIssue(
                            "ISOLATED_GRAPH_NODE",
                            GraphValidationSeverity.ERROR,
                            "GRAPH_NODE",
                            node.id(),
                            "Graph node has no routing edge."
                    )
            );
        }
    }

    private void validateConnectivity(
            CampusGraph graph,
            List<GraphValidationIssue> issues
    ) {

        List<GraphNode> nodes =
                graph.getNodes();

        if (nodes.isEmpty()) {
            return;
        }

        long startNodeId =
                nodes.get(0).id();

        Set<Long> visited =
                new HashSet<>();

        ArrayDeque<Long> queue =
                new ArrayDeque<>();

        visited.add(
                startNodeId
        );

        queue.add(
                startNodeId
        );

        while (!queue.isEmpty()) {

            long nodeId =
                    queue.removeFirst();

            graph.getBaseAdjacency(
                            nodeId
                    )
                    .forEach(
                            edge -> {

                                long next =
                                        edge.toNodeId();

                                if (visited.add(
                                        next
                                )) {

                                    queue.addLast(
                                            next
                                    );
                                }
                            }
                    );
        }

        if (visited.size()
                == nodes.size()) {

            return;
        }

        issues.add(
                new GraphValidationIssue(
                        "GRAPH_DISCONNECTED",
                        GraphValidationSeverity.ERROR,
                        "GRAPH",
                        null,
                        "Routing graph is disconnected. reachable="
                                + visited.size()
                                + ", total="
                                + nodes.size()
                )
        );
    }
}
