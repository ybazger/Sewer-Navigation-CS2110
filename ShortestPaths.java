package graph;

import datastructures.PQueue;
import datastructures.SlowPQueue;
import java.util.*;

/**
 * This object computes and remembers shortest paths through a weighted, directed graph with
 * nonnegative weights. The implementation of some parts of this class is owned by someone else and
 * has been omitted. Only the 'singleSourceDistances' method is original and owned by the uploader.
 */
public class ShortestPaths<Vertex, Edge> {

    private final WeightedDigraph<Vertex, Edge> graph;
    private Map<Vertex, Double> distances;
    private Map<Vertex, Edge> bestEdges;

    public ShortestPaths(WeightedDigraph<Vertex, Edge> graph) {
        this.graph = graph;
    }
     /**
     * Effect: Computes the best paths from a given source vertex, which can then be queried using
     * bestPath().
     */
    public void singleSourceDistances(Vertex source) {
        // Implementation constraint: use Dijkstra's single-source shortest paths algorithm.
        PQueue<Vertex> frontier = new SlowPQueue<>();
        distances = new HashMap<>();
        bestEdges = new HashMap<>();
        // Initialize source distance to 0
        frontier.add(source, 0);
        distances.put(source, 0.0);

        while (!frontier.isEmpty()) {
            Vertex currentVertex = frontier.extractMin();
            for (Edge way : graph.outgoingEdges(currentVertex)) {
                if (!distances.containsKey(graph.dest(way))) {
                    distances.put(graph.dest(way), graph.weight(way) + distances.get(currentVertex));
                    bestEdges.put(graph.dest(way), way);
                    frontier.add(graph.dest(way), distances.get(graph.dest(way)));

                } else if (graph.weight(way) + distances.get(currentVertex) < distances.get(graph.dest(way))) {
                    distances.put(graph.dest(way), graph.weight(way) + distances.get(currentVertex));
                    bestEdges.put(graph.dest(way), way);
                    frontier.changePriority(graph.dest(way), graph.weight(way) + distances.get(currentVertex));
                }
            }
        }
    }

    // Other methods are stubs and their implementations are owned by someone else.
    public double getDistance(Vertex v) {
        // Implementation omitted due to ownership by another party.
        throw new UnsupportedOperationException("Method not implemented");
    }

    public List<Edge> bestPath(Vertex target) {
        // Implementation omitted due to ownership by another party.
        throw new UnsupportedOperationException("Method not implemented");
    }
}