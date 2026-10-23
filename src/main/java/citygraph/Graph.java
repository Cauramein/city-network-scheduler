package citygraph;

import java.util.ArrayList;
import java.util.List;

public class Graph {
    public record Edge(int u, int v, int weight) implements Comparable<Edge> {
        @Override
        public int compareTo(Edge other) {
            return Integer.compare(this.weight, other.weight);
        }
    }

    private final int n;
    private final boolean directed;
    private final List<List<Edge>> adj;
    private final List<Edge> edges;

    public Graph(int n, boolean directed) {
        this.n = n;
        this.directed = directed;
        this.adj = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            this.adj.add(new ArrayList<>());
        }
        this.edges = new ArrayList<>();
    }

    public void addEdge(int u, int v, int weight) {
        Edge edge = new Edge(u, v, weight);
        adj.get(u).add(edge);
        edges.add(edge);
        if (!directed) {
            adj.get(v).add(new Edge(v, u, weight));
        }
    }

    public int getN() { return n; }
    public boolean isDirected() { return directed; }
    public List<Edge> getAdjacent(int u) { return adj.get(u); }
    public List<Edge> getAllEdges() { return edges; }
}