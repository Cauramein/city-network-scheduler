package citygraph.algorithms;

import citygraph.Graph;
import citygraph.Metrics;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class Kahn {

    public static class KahnResult {
        public final Metrics metrics;
        public final List<Integer> order;
        public final boolean hasCycle;

        public KahnResult(Metrics metrics, List<Integer> order, boolean hasCycle) {
            this.metrics = metrics;
            this.order = order;
            this.hasCycle = hasCycle;
        }
    }

    public static KahnResult run(Graph graph) {
        Metrics metrics = new Metrics();
        int n = graph.getN();
        int[] inDegree = new int[n];

        for (int u = 0; u < n; u++) {
            for (Graph.Edge e : graph.getAdjacent(u)) {
                inDegree[e.v()]++;
            }
        }

        Queue<Integer> queue = new ArrayDeque<>();
        for (int i = 0; i < n; i++) {
            if (inDegree[i] == 0) {
                queue.add(i);
            }
        }

        List<Integer> order = new ArrayList<>(n);
        while (!queue.isEmpty()) {
            int u = queue.poll();
            order.add(u);

            for (Graph.Edge e : graph.getAdjacent(u)) {
                metrics.edgeScans++;
                inDegree[e.v()]--;
                if (inDegree[e.v()] == 0) {
                    queue.add(e.v());
                }
            }
        }

        boolean hasCycle = order.size() < n;
        metrics.result = order.size();
        return new KahnResult(metrics, order, hasCycle);
    }
}