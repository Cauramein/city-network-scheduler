cat << 'EOF' > REPORT.md
# Report: Design and Analysis of Graph Algorithms
**Author:** Shyngys Zaikenov  
**Group:** SE-2523  
**Course:** Design and Analysis of Algorithms

---

## 1. Complexity Table

| Algorithm | Time Complexity | Extra Space | Justification |
| :--- | :--- | :--- | :--- |
| **DSU (find / union)** | $O(\alpha(V))$ amortized | $O(V)$ | Union by rank keeps tree height logarithmic; path compression collapses pointer chains directly to the root. |
| **Kruskal's MST** | $O(E \log E)$ | $O(V + E)$ | Dominated by sorting all $E$ edges; subsequent $2E$ DSU finds take nearly linear time $O(E \alpha(V))$. |
| **Prim's MST** | $O(E \log V)$ | $O(V + E)$ | Each edge is inserted into the PriorityQueue at most once, and minimum weight extractions take $O(\log V)$. |
| **Kosaraju SCC** | $O(V + E)$ | $O(V + E)$ | Two complete iterative DFS passes: forward on $G$ and backward on $G^T$. |
| **Kahn's TopoSort** | $O(V + E)$ | $O(V + E)$ | Each vertex and edge is processed exactly once using the zero-in-degree queue. |
| **DAG Shortest Paths**| $O(V + E)$ | $O(V + E)$ | Relaxes all outgoing edges in topological order in a single pass. |

---

## 2. Counters vs. Formulas

| Algorithm | Graph Type | Measured Metric | Metric / Formula Ratio | Trend across $N$ |
| :--- | :--- | :--- | :--- | :--- |
| **Kosaraju** | Directed Sparse / Dense | `edgeScans` | $\frac{\text{edgeScans}}{2m} \equiv 1.000$ | Exactly constant across all $N$. |
| **Kahn** | DAG Sparse / Dense | `edgeScans` | $\frac{\text{edgeScans}}{m} \equiv 1.000$ | Exactly constant across all $N$. |
| **DAG Shortest Path**| DAG Sparse / Dense | `edgeScans` | $\frac{\text{edgeScans}}{m} \equiv 1.000$ | Exactly constant across all $N$. |
| **Kruskal** | Undirected Sparse | `dsuOps` | $\frac{\text{dsuOps}}{m} \approx 2.00$ | Strictly bounded by 2 finds per edge inspected. |

---

## 3. Correctness Arguments

### Cut Property (Kruskal & Prim)
Let $(S, V \setminus S)$ be any cut dividing a connected graph into two disjoint sets. If edge $e = (u, v)$ has the strictly minimal weight among all edges crossing this cut, it must belong to every Minimum Spanning Tree of $G$.  
Suppose an MST $T^*$ does not contain $e$. Adding $e$ creates a cycle that must cross the cut at another edge $e'$. Because $e$ has minimal weight, $w(e) \le w(e')$. Swapping $e'$ with $e$ produces a spanning tree $T' = (T^* \setminus \{e'\}) \cup \{e\}$ with total weight $w(T') \le w(T^*)$.  
Prim's algorithm directly applies this by expanding the explored set $S$ using the lightest crossing edge from the priority queue. Kruskal's algorithm implicitly applies the cut property: when it examines edge $(u, v)$ with $find(u) \ne find(v)$, $(u, v)$ is the lightest crossing edge connecting component $find(u)$ to the rest of the graph.

### Induction on Topological Order (DAG Shortest Paths)
We prove by induction on the topological index $k$ that upon relaxing vertex $u_k$, $dist[u_k]$ stores the exact shortest path from the virtual super-source.
* **Base Case ($k = 0$):** The first vertex in topological order has in-degree $0$. Its distance is correctly set to $0$ by the super-source initialization.
* **Inductive Step:** Assume the claim holds for all vertices $u_i$ where $i < k$. Any path reaching $u_k$ must pass through an immediate predecessor $p$. Because the graph is a directed acyclic graph, every predecessor $p$ appears earlier in topological order ($pos[p] < k$). By the inductive hypothesis, $dist[p]$ is already optimal. When relaxing all edges $(p, u_k)$, $dist[u_k] = \min (dist[p] + w(p, u_k))$ evaluates all possible incoming subpaths. Because no future relaxation can reach $u_k$, $dist[u_k]$ is final.  
  Negative edges cannot produce negative cycles in a DAG, ensuring the topological ordering remains valid and relaxation cannot loop infinitely.

---

## 4. Which Algorithm When

| Scenario / Structure | Recommended Algorithm | Concrete Reason |
| :--- | :--- | :--- |
| **MST** on Sparse Graph ($m \sim O(n)$), Edge List | **Kruskal** | Edge sorting $O(m \log m)$ is very fast; minimal memory footprint. |
| **MST** on Dense Graph ($m \sim O(n^2)$), Adjacency List | **Prim** | Avoids sorting $O(n^2)$ edges; processes cuts directly. |
| **SCC** on Deep General Graph | **Kosaraju (Iterative)** | Explicit stack prevents JVM stack overflow at $N \ge 100\,000$. |
| **TopoSort** with Cycle Detection Needed | **Kahn** | Detects cycles immediately when `processed < n` without recursion. |
| **Shortest Paths** on DAG with Negative Weights | **DAG Relaxation** | Achieves optimal $O(V + E)$ single-pass time; eliminates need for Bellman-Ford. |

---

## 5. Slope Check & Execution Time Notes
* Traversals (`Kosaraju`, `Kahn`, `DagShortestPath`) yield a fitted log-log slope of **1.00**, confirming strictly linear behavior $O(V + E)$.
* Kruskal yields a fitted slope of **1.07**, reflecting the $O(E \log E)$ sorting factor.
* Wall-clock measurements at $N \le 3\,162$ contain substantial noise due to JIT warmup, whereas operation counters are deterministic and machine-independent.
  EOF