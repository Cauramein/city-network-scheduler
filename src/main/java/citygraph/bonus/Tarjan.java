package citygraph.bonus;

import citygraph.Graph;
import citygraph.Metrics;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

public class Tarjan {
    private final Graph graph;
    private final Metrics metrics;
    private final int[] index;
    private final int[] lowlink;
    private final boolean[] onStack;
    private final Deque<Integer> stack;
    private int time = 0;
    private int sccCount = 0;

    public Tarjan(Graph graph) {
        this.graph = graph;
        this.metrics = new Metrics();
        int n = graph.getN();
        this.index = new int[n];
        this.lowlink = new int[n];
        this.onStack = new boolean[n];
        this.stack = new ArrayDeque<>();
        Arrays.fill(index, -1);
    }

    public static Metrics run(Graph graph) {
        Tarjan t = new Tarjan(graph);
        for (int i = 0; i < graph.getN(); i++) {
            if (t.index[i] == -1) {
                t.dfs(i);
            }
        }
        t.metrics.result = t.sccCount;
        return t.metrics;
    }

    private void dfs(int u) {
        index[u] = lowlink[u] = time++;
        stack.push(u);
        onStack[u] = true;

        for (Graph.Edge e : graph.getAdjacent(u)) {
            metrics.edgeScans++;
            int v = e.v();
            if (index[v] == -1) {
                dfs(v);
                lowlink[u] = Math.min(lowlink[u], lowlink[v]);
            } else if (onStack[v]) {
                lowlink[u] = Math.min(lowlink[u], index[v]);
            }
        }

        if (lowlink[u] == index[u]) {
            sccCount++;
            while (true) {
                int w = stack.pop();
                onStack[w] = false;
                if (w == u) break;
            }
        }
    }
}