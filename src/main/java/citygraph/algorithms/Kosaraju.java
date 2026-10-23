package citygraph.algorithms;

import citygraph.Graph;
import citygraph.Metrics;

import java.util.ArrayList;
import java.util.List;

public class Kosaraju {

    public static Metrics run(Graph graph) {
        Metrics metrics = new Metrics();
        int n = graph.getN();

        List<List<Graph.Edge>> revAdj = new ArrayList<>(n);
        for (int i = 0; i < n; i++) revAdj.add(new ArrayList<>());
        for (int u = 0; u < n; u++) {
            for (Graph.Edge e : graph.getAdjacent(u)) {
                revAdj.get(e.v()).add(new Graph.Edge(e.v(), e.u(), e.weight()));
            }
        }

        boolean[] visited = new boolean[n];
        int[] finishOrder = new int[n];
        int finishIndex = 0;

        int[] stackV = new int[n];
        int[] edgeIdx = new int[n];

        for (int i = 0; i < n; i++) {
            if (visited[i]) continue;

            int top = 0;
            stackV[0] = i;
            edgeIdx[0] = 0;
            visited[i] = true;

            while (top >= 0) {
                if (top + 1 > metrics.maxDepth) {
                    metrics.maxDepth = top + 1;
                }
                int curr = stackV[top];
                List<Graph.Edge> neighbors = graph.getAdjacent(curr);

                if (edgeIdx[top] < neighbors.size()) {
                    Graph.Edge e = neighbors.get(edgeIdx[top]++);
                    metrics.edgeScans++;
                    int nxt = e.v();
                    if (!visited[nxt]) {
                        visited[nxt] = true;
                        top++;
                        stackV[top] = nxt;
                        edgeIdx[top] = 0;
                    }
                } else {
                    finishOrder[finishIndex++] = curr;
                    top--;
                }
            }
        }

        boolean[] revVisited = new boolean[n];
        int sccCount = 0;

        for (int i = n - 1; i >= 0; i--) {
            int root = finishOrder[i];
            if (revVisited[root]) continue;

            sccCount++;
            int top = 0;
            stackV[0] = root;
            edgeIdx[0] = 0;
            revVisited[root] = true;

            while (top >= 0) {
                if (top + 1 > metrics.maxDepth) {
                    metrics.maxDepth = top + 1;
                }
                int curr = stackV[top];
                List<Graph.Edge> neighbors = revAdj.get(curr);

                if (edgeIdx[top] < neighbors.size()) {
                    Graph.Edge e = neighbors.get(edgeIdx[top]++);
                    metrics.edgeScans++;
                    int nxt = e.v();
                    if (!revVisited[nxt]) {
                        revVisited[nxt] = true;
                        top++;
                        stackV[top] = nxt;
                        edgeIdx[top] = 0;
                    }
                } else {
                    top--;
                }
            }
        }

        metrics.result = sccCount;
        return metrics;
    }
}