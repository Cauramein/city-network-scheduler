package citygraph;

public class DSU {
    private final int[] parent;
    private final int[] rank;
    private int components;
    private final Metrics metrics;

    public DSU(int n, Metrics metrics) {
        this.parent = new int[n];
        this.rank = new int[n];
        this.components = n;
        this.metrics = metrics;
        for (int i = 0; i < n; i++) {
            parent[i] = i;
            rank[i] = 0;
        }
    }

    public int find(int x) {
        if (metrics != null) metrics.dsuOps++;
        int root = x;
        while (root != parent[root]) {
            root = parent[root];
        }
        int curr = x;
        while (curr != root) {
            int next = parent[curr];
            parent[curr] = root;
            curr = next;
        }
        return root;
    }

    public boolean union(int x, int y) {
        if (metrics != null) metrics.dsuOps++;
        int rootX = find(x);
        int rootY = find(y);
        if (rootX == rootY) return false;

        if (rank[rootX] < rank[rootY]) {
            parent[rootX] = rootY;
        } else if (rank[rootX] > rank[rootY]) {
            parent[rootY] = rootX;
        } else {
            parent[rootY] = rootX;
            rank[rootX]++;
        }
        components--;
        return true;
    }

    public int getComponents() { return components; }
}