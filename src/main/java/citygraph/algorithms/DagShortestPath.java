package citygraph.algorithms;

import citygraph.Graph;
import citygraph.Metrics;

import java.util.Arrays;
import java.util.List;

public class DagShortestPath {

    public static class PathResult {
        public final Metrics metrics;
        public final long[] dist;
        public final int[] parent;

        public PathResult(Metrics metrics, long[] dist, int[] parent) {
            this.metrics = metrics;
            this.dist = dist;
            this.parent = parent;
        }
    }

    public static PathResult run(Graph graph, List<Integer> topoOrder) {
        Metrics metrics = new Metrics();
        int n = graph.getN();
        long[] dist = new long[n];
        int[] parent = new int[n];
        int[] inDegree = new int[n];

        Arrays.fill(dist, Long.MAX_VALUE / 2);
        Arrays.fill(parent, -1);

        for (int u = 0; u < n; u++) {
            for (Graph.Edge e : graph.getAdjacent(u)) {
                inDegree[e.v()]++;
            }
        }

        for (int i = 0; i < n; i++) {
            if (inDegree[i] == 0) {
                dist[i] = 0;
            }
        }

        for (int u : topoOrder) {
            if (dist[u] == Long.MAX_VALUE / 2) continue;

            for (Graph.Edge e : graph.getAdjacent(u)) {
                metrics.edgeScans++;
                int v = e.v();
                if (dist[u] + e.weight() < dist[v]) {
                    dist[v] = dist[u] + e.weight();
                    parent[v] = u;
                }
            }
        }

        long sumDistances = 0;
        for (int i = 0; i < n; i++) {
            sumDistances += dist[i];
        }

        metrics.result = sumDistances;
        return new PathResult(metrics, dist, parent);
    }
}