package citygraph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class GraphIO {
    public static Graph readFromFile(String filePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String firstLine = br.readLine();
            if (firstLine == null) throw new IllegalArgumentException("File is empty");
            String[] header = firstLine.trim().split("\\s+");
            int n = Integer.parseInt(header[0]);
            int m = Integer.parseInt(header[1]);
            boolean directed = header[2].equalsIgnoreCase("directed");

            Graph g = new Graph(n, directed);
            for (int i = 0; i < m; i++) {
                String line = br.readLine();
                if (line == null) break;
                String[] parts = line.trim().split("\\s+");
                int u = Integer.parseInt(parts[0]);
                int v = Integer.parseInt(parts[1]);
                int w = Integer.parseInt(parts[2]);
                g.addEdge(u, v, w);
            }
            return g;
        }
    }
}