package citygraph.benchmark;

import citygraph.Graph;
import citygraph.GraphGenerator;
import citygraph.Metrics;
import citygraph.algorithms.DagShortestPath;
import citygraph.algorithms.Kahn;
import citygraph.algorithms.Kosaraju;
import citygraph.algorithms.MST;
import java.util.Locale;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Arrays;

public class Benchmark {
    private static final int[][] SPECS = {
            {1000, 4000}, {1000, 20000},
            {3162, 12648}, {3162, 63240},
            {10000, 40000}, {10000, 200000},
            {31623, 126492}, {31623, 632460},
            {100000, 400000}, {100000, 2000000}
    };

    public static void main(String[] args) throws Exception {
        new File("results").mkdirs();
        try (PrintWriter writer = new PrintWriter(new FileWriter("results/results.csv"))) {
            writer.println("algorithm,graph,n,m,time_ms,dsu_ops,edge_scans,comparisons,max_depth,result");

            for (int[] spec : SPECS) {
                int n = spec[0];
                int m = spec[1];
                String density = (m == 4 * n) ? "sparse" : "dense";
                System.out.printf("Running Benchmark for N=%d, M=%d (%s)...%n", n, m, density);

                // 1. Kruskal
                Graph undirKruskal = GraphGenerator.generateUndirected(n, m);
                Metrics kruskalM = measure(writer, "Kruskal", density, n, m, () -> MST.kruskal(undirKruskal));

                // 2. Prim
                Graph undirPrim = GraphGenerator.generateUndirected(n, m);
                Metrics primM = measure(writer, "Prim", density, n, m, () -> MST.prim(undirPrim));

                if (kruskalM.result != primM.result) {
                    throw new IllegalStateException("MST mismatch: " + kruskalM.result + " vs " + primM.result);
                }

                // 3. Kosaraju
                Graph dirG = GraphGenerator.generateDirected(n, m);
                measure(writer, "Kosaraju", density, n, m, () -> Kosaraju.run(dirG));

                // 4. Kahn & 5. DagShortestPath
                Graph dagG = GraphGenerator.generateDAG(n, m);
                Kahn.KahnResult kahnRes = Kahn.run(dagG);
                if (kahnRes.hasCycle) throw new IllegalStateException("Cycle detected in DAG");
                writeRow(writer, "Kahn", density, n, m, 0.0, kahnRes.metrics);

                DagShortestPath.PathResult pathRes = DagShortestPath.run(dagG, kahnRes.order);
                writeRow(writer, "DagShortestPath", density, n, m, 0.0, pathRes.metrics);
            }
        }
        System.out.println("Benchmark finished. results/results.csv successfully populated.");
    }

    private static Metrics measure(PrintWriter writer, String algo, String density, int n, int m, Task task) {
        long[] times = new long[5];
        Metrics metrics = null;
        for (int i = 0; i < 5; i++) {
            long start = System.nanoTime();
            metrics = task.execute();
            times[i] = System.nanoTime() - start;
        }
        Arrays.sort(times);
        double medianMs = times[2] / 1e6;
        writeRow(writer, algo, density, n, m, medianMs, metrics);
        return metrics;
    }

    private static void writeRow(PrintWriter writer, String algo, String density, int n, int m, double timeMs, Metrics mtr) {
        writer.printf(Locale.US, "%s,%s,%d,%d,%.3f,%d,%d,%d,%d,%d%n",
                algo, density, n, m, timeMs, mtr.dsuOps, mtr.edgeScans, mtr.comparisons, mtr.maxDepth, mtr.result);
        writer.flush();
    }


    @FunctionalInterface
    interface Task {
        Metrics execute();
    }
}