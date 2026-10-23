package citygraph.algorithms;

import citygraph.DSU;
import citygraph.Graph;
import citygraph.Metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class MST {

    public static Metrics kruskal(Graph graph) {
        Metrics metrics = new Metrics();
        List<Graph.Edge> edges = new ArrayList<>(graph.getAllEdges());

        edges.sort((e1, e2) -> {
            metrics.comparisons++;
            return Integer.compare(e1.weight(), e2.weight());
        });

        DSU dsu = new DSU(graph.getN(), metrics);
        long totalWeight = 0;
        int edgesTaken = 0;

        for (Graph.Edge edge : edges) {
            metrics.edgeScans++;
            if (dsu.union(edge.u(), edge.v())) {
                totalWeight += edge.weight();
                edgesTaken++;
                if (edgesTaken == graph.getN() - 1) {
                    break; // Как только дерево построено, прекращаем сканирование!
                }
            }
        }

        metrics.result = totalWeight;
        return metrics;
    }

    public static Metrics prim(Graph graph) {
        Metrics metrics = new Metrics();
        int n = graph.getN();
        boolean[] inMST = new boolean[n];
        long totalWeight = 0;

        PriorityQueue<Graph.Edge> pq = new PriorityQueue<>((e1, e2) -> {
            metrics.comparisons++;
            return Integer.compare(e1.weight(), e2.weight());
        });

        for (int start = 0; start < n; start++) {
            if (inMST[start]) continue;

            inMST[start] = true;
            for (Graph.Edge e : graph.getAdjacent(start)) {
                metrics.edgeScans++;
                pq.add(e);
            }

            while (!pq.isEmpty()) {
                Graph.Edge minEdge = pq.poll();
                int v = minEdge.v();
                if (inMST[v]) continue;

                inMST[v] = true;
                totalWeight += minEdge.weight();

                for (Graph.Edge nextEdge : graph.getAdjacent(v)) {
                    metrics.edgeScans++;
                    if (!inMST[nextEdge.v()]) {
                        pq.add(nextEdge);
                    }
                }
            }
        }

        metrics.result = totalWeight;
        return metrics;
    }
}