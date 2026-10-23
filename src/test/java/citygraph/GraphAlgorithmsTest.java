package citygraph;

import citygraph.algorithms.DagShortestPath;
import citygraph.algorithms.Kahn;
import citygraph.algorithms.Kosaraju;
import citygraph.algorithms.MST;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class GraphAlgorithmsTest {

    @Test
    @DisplayName("DSU: verification against naive sets")
    void testDSUOperations() {
        int n = 100;
        DSU dsu = new DSU(n, new Metrics());
        Map<Integer, Set<Integer>> naive = new HashMap<>();
        for (int i = 0; i < n; i++) {
            naive.put(i, new HashSet<>(List.of(i)));
        }

        Random rnd = new Random(42);
        for (int k = 0; k < 300; k++) {
            int u = rnd.nextInt(n);
            int v = rnd.nextInt(n);

            boolean merged = dsu.union(u, v);
            Set<Integer> setU = naive.get(u);
            Set<Integer> setV = naive.get(v);

            if (setU == setV) {
                assertFalse(merged);
            } else {
                assertTrue(merged);
                setU.addAll(setV);
                for (int node : setV) {
                    naive.put(node, setU);
                }
            }
            assertEquals(dsu.find(u) == dsu.find(v), naive.get(u) == naive.get(v));
        }
        long uniqueNaiveSets = naive.values().stream().distinct().count();
        assertEquals(uniqueNaiveSets, dsu.getComponents());
    }

    @Test
    @DisplayName("MST: Kruskal and Prim produce identical weights across 100 random graphs")
    void testMSTEquivalence() {
        for (int seed = 0; seed < 100; seed++) {
            Graph g = GraphGenerator.generateUndirected(50, 150);
            Metrics kruskal = MST.kruskal(g);
            Metrics prim = MST.prim(g);
            assertEquals(kruskal.result, prim.result);
        }
    }

    @Test
    @DisplayName("SCC: Verify Kosaraju against BFS reachability on N <= 200")
    void testSCCBruteForce() {
        int n = 60;
        int m = 180;
        Graph g = GraphGenerator.generateDirected(n, m);

        boolean[][] reach = new boolean[n][n];
        for (int i = 0; i < n; i++) {
            Queue<Integer> q = new ArrayDeque<>();
            q.add(i);
            reach[i][i] = true;
            while (!q.isEmpty()) {
                int curr = q.poll();
                for (var edge : g.getAdjacent(curr)) {
                    if (!reach[i][edge.v()]) {
                        reach[i][edge.v()] = true;
                        q.add(edge.v());
                    }
                }
            }
        }

        int expectedSCC = 0;
        boolean[] accounted = new boolean[n];
        for (int i = 0; i < n; i++) {
            if (!accounted[i]) {
                expectedSCC++;
                for (int j = 0; j < n; j++) {
                    if (reach[i][j] && reach[j][i]) {
                        accounted[j] = true;
                    }
                }
            }
        }

        Metrics metrics = Kosaraju.run(g);
        assertEquals(expectedSCC, metrics.result);
    }

    @Test
    @DisplayName("TopoSort: Valid ordering and cycle detection")
    void testKahnTopoSort() throws Exception {
        Graph dag = GraphIO.readFromFile("data/graph_dag.txt");
        Kahn.KahnResult dagRes = Kahn.run(dag);
        assertFalse(dagRes.hasCycle);

        int[] pos = new int[dag.getN()];
        for (int i = 0; i < dagRes.order.size(); i++) {
            pos[dagRes.order.get(i)] = i;
        }
        for (var edge : dag.getAllEdges()) {
            assertTrue(pos[edge.u()] < pos[edge.v()]);
        }

        Graph cyclic = GraphIO.readFromFile("data/graph_cyclic.txt");
        Kahn.KahnResult cyclicRes = Kahn.run(cyclic);
        assertTrue(cyclicRes.hasCycle);
    }

    @Test
    @DisplayName("DAG Paths: Verification against Bellman-Ford on negative edge weights")
    void testDagShortestPathVsBellmanFord() {
        Graph dag = GraphGenerator.generateDAG(80, 300);
        Kahn.KahnResult kahn = Kahn.run(dag);
        DagShortestPath.PathResult pathRes = DagShortestPath.run(dag, kahn.order);

        int n = dag.getN();
        long[] bfDist = new long[n];
        int[] inDegree = new int[n];
        for (var e : dag.getAllEdges()) inDegree[e.v()]++;
        Arrays.fill(bfDist, Long.MAX_VALUE / 2);
        for (int i = 0; i < n; i++) {
            if (inDegree[i] == 0) bfDist[i] = 0;
        }

        for (int i = 0; i < n - 1; i++) {
            for (var e : dag.getAllEdges()) {
                if (bfDist[e.u()] != Long.MAX_VALUE / 2 && bfDist[e.u()] + e.weight() < bfDist[e.v()]) {
                    bfDist[e.v()] = bfDist[e.u()] + e.weight();
                }
            }
        }

        assertArrayEquals(bfDist, pathRes.dist);
    }

    @Test
    @DisplayName("Stack Safety: Path graph with 100,000 vertices completes safely")
    void testIterativeDfsStackSafety() {
        int n = 100_000;
        Graph pathGraph = new Graph(n, true);
        for (int i = 0; i < n - 1; i++) {
            pathGraph.addEdge(i, i + 1, 1);
        }
        assertDoesNotThrow(() -> {
            Metrics metrics = Kosaraju.run(pathGraph);
            assertEquals(n, metrics.result);
            assertEquals(n, metrics.maxDepth);
        });
    }
}