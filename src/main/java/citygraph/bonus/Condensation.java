package citygraph.bonus;

import citygraph.Graph;
import citygraph.algorithms.DagShortestPath;
import citygraph.algorithms.Kahn;

import java.util.*;

public class Condensation {

    public static long findCriticalPath(Graph original, int[] sccId, int sccCount) {
        Graph condDag = new Graph(sccCount, true);
        Set<Long> addedEdges = new HashSet<>();

        for (Graph.Edge e : original.getAllEdges()) {
            int compU = sccId[e.u()];
            int compV = sccId[e.v()];
            if (compU != compV) {
                long key = (((long) compU) << 32) | (compV & 0xFFFFFFFFL);
                if (!addedEdges.contains(key)) {
                    addedEdges.add(key);
                    condDag.addEdge(compU, compV, -Math.abs(e.weight()));
                }
            }
        }

        Kahn.KahnResult kahn = Kahn.run(condDag);
        DagShortestPath.PathResult res = DagShortestPath.run(condDag, kahn.order);

        long minDistance = 0;
        for (long d : res.dist) {
            if (d < minDistance) minDistance = d;
        }
        return -minDistance;
    }
}