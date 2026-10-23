package citygraph.benchmark;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.List;

public class ChartGenerator {

    record Row(String algo, String density, int n, int m, long edgeScans) {}

    private static final int W = 1000;
    private static final int H = 650;
    private static final int PAD_L = 120;
    private static final int PAD_R = 210;
    private static final int PAD_T = 80;
    private static final int PAD_B = 90;

    private static final int PLOT_W = W - PAD_L - PAD_R;
    private static final int PLOT_H = H - PAD_T - PAD_B;

    private static final int[] N_VALS = {1000, 3162, 10000, 31623, 100000};
    private static final String[] N_LABELS = {"1k", "3.16k", "10k", "31.6k", "100k"};

    public static void main(String[] args) throws Exception {
        new File("results/plots").mkdirs();
        List<Row> rows = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader("results/results.csv"))) {
            String headerLine = br.readLine();
            if (headerLine == null) return;
            String[] headers = headerLine.split(",");
            int idxAlgo = -1, idxGraph = -1, idxN = -1, idxM = -1, idxScans = -1;
            for (int i = 0; i < headers.length; i++) {
                String h = headers[i].trim().toLowerCase();
                if (h.equals("algorithm")) idxAlgo = i;
                else if (h.equals("graph")) idxGraph = i;
                else if (h.equals("n")) idxN = i;
                else if (h.equals("m")) idxM = i;
                else if (h.equals("edge_scans")) idxScans = i;
            }

            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] p = line.split(",");
                rows.add(new Row(
                        p[idxAlgo].trim(),
                        p[idxGraph].trim().toLowerCase(),
                        Integer.parseInt(p[idxN].trim()),
                        Integer.parseInt(p[idxM].trim()),
                        Long.parseLong(p[idxScans].trim())
                ));
            }
        }

        renderPlot1(rows);
        renderPlot2(rows);
        renderPlot3(rows);

        System.out.println("Plots refreshed successfully in results/plots/!");
    }

    private static void renderPlot1(List<Row> rows) throws Exception {
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = initGraphics(img);

        double yMinLog = 3.0; // 10^3
        double yMaxLog = 7.0; // 10^7

        drawFrame(g, "Operations (Edge Scans) vs N on Log-Log Scale",
                "Number of vertices (N, log scale)", "Edge Scans (log scale)");

        // Сетка Y
        g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        for (int exp = 3; exp <= 7; exp++) {
            double frac = (exp - yMinLog) / (yMaxLog - yMinLog);
            int y = PAD_T + PLOT_H - (int) (frac * PLOT_H);
            g.setColor(new Color(230, 233, 238));
            g.drawLine(PAD_L, y, PAD_L + PLOT_W, y);
            g.setColor(new Color(70, 75, 85));
            g.drawString("10^" + exp, PAD_L - 45, y + 4);
        }

        // Сетка X
        for (int i = 0; i < N_VALS.length; i++) {
            double xFrac = (Math.log10(N_VALS[i]) - 3.0) / 2.0;
            int x = PAD_L + (int) (xFrac * PLOT_W);
            g.setColor(new Color(230, 233, 238));
            g.drawLine(x, PAD_T, x, PAD_T + PLOT_H);
            g.setColor(new Color(70, 75, 85));
            g.drawString(N_LABELS[i], x - 12, PAD_T + PLOT_H + 20);
        }

        Map<String, Color> colorMap = new LinkedHashMap<>();
        colorMap.put("Kosaraju", new Color(40, 167, 69));      // Зеленый (2m)
        colorMap.put("Prim", new Color(0, 123, 255));          // Синий
        colorMap.put("Kruskal", new Color(220, 53, 69));       // Красный
        colorMap.put("Kahn", new Color(111, 66, 193));         // Фиолетовый
        colorMap.put("DagShortestPath", new Color(253, 126, 20));// Оранжевый

        int legendY = PAD_T + 10;
        int algoIndex = 0;
        for (String algo : colorMap.keySet()) {
            Color c = colorMap.get(algo);
            for (String dens : List.of("sparse", "dense")) {
                List<Point2D.Double> pts = new ArrayList<>();
                for (int nVal : N_VALS) {
                    int finalAlgoIndex = algoIndex;
                    rows.stream()
                            .filter(r -> r.algo.equalsIgnoreCase(algo) && r.density.equalsIgnoreCase(dens) && r.n == nVal)
                            .findFirst()
                            .ifPresent(r -> {
                                double xFrac = (Math.log10(r.n) - 3.0) / 2.0;
                                double scans = Math.max(1, r.edgeScans);
                                double yFrac = (Math.log10(scans) - yMinLog) / (yMaxLog - yMinLog);
                                double px = PAD_L + (xFrac * PLOT_W);
                                // Микросмещение на 1-2 пикселя, чтобы накладывающиеся линии не перекрывали друг друга
                                double py = PAD_T + PLOT_H - (yFrac * PLOT_H) + (finalAlgoIndex - 2) * 1.5;
                                pts.add(new Point2D.Double(px, py));
                            });
                }
                drawSeries(g, pts, c, dens.equals("dense"), algoIndex);
            }

            g.setColor(c);
            g.fillRect(PAD_L + PLOT_W + 15, legendY, 14, 14);
            g.setColor(new Color(40, 44, 52));
            g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g.drawString(algo, PAD_L + PLOT_W + 35, legendY + 12);
            legendY += 24;
            algoIndex++;
        }

        g.setColor(new Color(100, 105, 115));
        g.drawString("— Solid: dense (m=20n)", PAD_L + PLOT_W + 15, legendY + 10);
        g.drawString("-- Dashed: sparse (m=4n)", PAD_L + PLOT_W + 15, legendY + 28);

        g.dispose();
        ImageIO.write(img, "png", new File("results/plots/ops_vs_n.png"));
    }

    private static void renderPlot2(List<Row> rows) throws Exception {
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = initGraphics(img);

        drawFrame(g, "Scans per Edge vs N (Linear Work Confirmation)",
                "Number of vertices (N)", "edgeScans / m");

        // Ось Y от 0.0 до 2.5
        g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        for (double v = 0.0; v <= 2.5; v += 0.5) {
            int y = PAD_T + PLOT_H - (int) ((v / 2.5) * PLOT_H);
            g.setColor(new Color(230, 233, 238));
            g.drawLine(PAD_L, y, PAD_L + PLOT_W, y);
            g.setColor(new Color(70, 75, 85));
            g.drawString(String.format(Locale.US, "%.1f", v), PAD_L - 35, y + 4);
        }

        for (int i = 0; i < N_VALS.length; i++) {
            double xFrac = (Math.log10(N_VALS[i]) - 3.0) / 2.0;
            int x = PAD_L + (int) (xFrac * PLOT_W);
            g.setColor(new Color(230, 233, 238));
            g.drawLine(x, PAD_T, x, PAD_T + PLOT_H);
            g.setColor(new Color(70, 75, 85));
            g.drawString(N_LABELS[i], x - 12, PAD_T + PLOT_H + 20);
        }

        Map<String, Color> colorMap = new LinkedHashMap<>();
        colorMap.put("Kosaraju", new Color(0, 123, 255));        // Линия на 2.0
        colorMap.put("Kahn", new Color(220, 53, 69));           // Линия на 1.0
        colorMap.put("DagShortestPath", new Color(40, 167, 69)); // Линия на 1.0

        int legendY = PAD_T + 20;
        int shapeIdx = 0;
        for (String algo : colorMap.keySet()) {
            Color c = colorMap.get(algo);
            for (String dens : List.of("sparse", "dense")) {
                List<Point2D.Double> pts = new ArrayList<>();
                for (int nVal : N_VALS) {
                    rows.stream()
                            .filter(r -> r.algo.equalsIgnoreCase(algo) && r.density.equalsIgnoreCase(dens) && r.n == nVal)
                            .findFirst()
                            .ifPresent(r -> {
                                double ratio = ((double) r.edgeScans) / ((double) r.m);
                                double xFrac = (Math.log10(r.n) - 3.0) / 2.0;
                                double px = PAD_L + (xFrac * PLOT_W);
                                double py = PAD_T + PLOT_H - ((ratio / 2.5) * PLOT_H);
                                pts.add(new Point2D.Double(px, py));
                            });
                }
                drawSeries(g, pts, c, dens.equals("dense"), shapeIdx);
            }

            g.setColor(c);
            g.fillRect(PAD_L + PLOT_W + 15, legendY, 14, 14);
            g.setColor(new Color(40, 44, 52));
            g.drawString(algo, PAD_L + PLOT_W + 35, legendY + 12);
            legendY += 26;
            shapeIdx++;
        }

        g.setColor(new Color(100, 105, 115));
        g.drawString("— Solid: dense", PAD_L + PLOT_W + 15, legendY + 10);
        g.drawString("-- Dashed: sparse", PAD_L + PLOT_W + 15, legendY + 28);

        g.dispose();
        ImageIO.write(img, "png", new File("results/plots/scans_per_edge.png"));
    }

    private static void renderPlot3(List<Row> rows) throws Exception {
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = initGraphics(img);

        drawFrame(g, "Share of Edges Examined by Kruskal",
                "Number of vertices (N)", "Fraction of Edges Scanned (edgeScans / m)");

        g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        for (double v = 0.0; v <= 1.0; v += 0.2) {
            int y = PAD_T + PLOT_H - (int) (v * PLOT_H);
            g.setColor(new Color(230, 233, 238));
            g.drawLine(PAD_L, y, PAD_L + PLOT_W, y);
            g.setColor(new Color(70, 75, 85));
            g.drawString(String.format(Locale.US, "%.1f", v), PAD_L - 35, y + 4);
        }

        for (int i = 0; i < N_VALS.length; i++) {
            double xFrac = (Math.log10(N_VALS[i]) - 3.0) / 2.0;
            int x = PAD_L + (int) (xFrac * PLOT_W);
            g.setColor(new Color(230, 233, 238));
            g.drawLine(x, PAD_T, x, PAD_T + PLOT_H);
            g.setColor(new Color(70, 75, 85));
            g.drawString(N_LABELS[i], x - 12, PAD_T + PLOT_H + 20);
        }

        Color sparseColor = new Color(220, 53, 69);
        Color denseColor = new Color(0, 123, 255);

        for (String dens : List.of("sparse", "dense")) {
            List<Point2D.Double> pts = new ArrayList<>();
            for (int nVal : N_VALS) {
                rows.stream()
                        .filter(r -> r.algo.equalsIgnoreCase("Kruskal") && r.density.equalsIgnoreCase(dens) && r.n == nVal)
                        .findFirst()
                        .ifPresent(r -> {
                            double ratio = ((double) r.edgeScans) / ((double) r.m);
                            double xFrac = (Math.log10(r.n) - 3.0) / 2.0;
                            double px = PAD_L + (xFrac * PLOT_W);
                            double py = PAD_T + PLOT_H - (Math.min(1.0, ratio) * PLOT_H);
                            pts.add(new Point2D.Double(px, py));
                        });
            }
            drawSeries(g, pts, dens.equals("sparse") ? sparseColor : denseColor, true, dens.equals("sparse") ? 0 : 1);
        }

        g.setColor(sparseColor);
        g.fillRect(PAD_L + PLOT_W + 15, PAD_T + 20, 14, 14);
        g.setColor(new Color(40, 44, 52));
        g.drawString("Sparse (m = 4n)", PAD_L + PLOT_W + 35, PAD_T + 32);

        g.setColor(denseColor);
        g.fillRect(PAD_L + PLOT_W + 15, PAD_T + 50, 14, 14);
        g.setColor(new Color(40, 44, 52));
        g.drawString("Dense (m = 20n)", PAD_L + PLOT_W + 35, PAD_T + 62);

        g.dispose();
        ImageIO.write(img, "png", new File("results/plots/kruskal_edge_share.png"));
    }

    private static Graphics2D initGraphics(BufferedImage img) {
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, img.getWidth(), img.getHeight());
        return g;
    }

    private static void drawFrame(Graphics2D g, String title, String xLabel, String yLabel) {
        g.setColor(new Color(33, 37, 41));
        g.setFont(new Font("Segoe UI", Font.BOLD, 18));
        g.drawString(title, PAD_L, 45);

        g.setColor(new Color(200, 205, 215));
        g.drawRect(PAD_L, PAD_T, PLOT_W, PLOT_H);

        g.setColor(new Color(80, 85, 95));
        g.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        g.drawString(xLabel, PAD_L + (PLOT_W / 2) - 60, PAD_T + PLOT_H + 50);

        Graphics2D gRotated = (Graphics2D) g.create();
        gRotated.rotate(-Math.PI / 2);
        gRotated.drawString(yLabel, -(PAD_T + (PLOT_H / 2) + 50), PAD_L - 65);
        gRotated.dispose();
    }

    private static void drawSeries(Graphics2D g, List<Point2D.Double> pts, Color c, boolean solid, int shapeIdx) {
        if (pts.isEmpty()) return;
        g.setColor(c);
        if (solid) {
            g.setStroke(new BasicStroke(2.2f));
        } else {
            g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{6.0f, 5.0f}, 0.0f));
        }

        for (int i = 0; i < pts.size() - 1; i++) {
            g.draw(new Line2D.Double(pts.get(i).x, pts.get(i).y, pts.get(i + 1).x, pts.get(i + 1).y));
        }

        for (Point2D.Double p : pts) {
            g.setColor(Color.WHITE);
            g.fill(new Ellipse2D.Double(p.x - 4, p.y - 4, 8, 8));
            g.setColor(c);
            g.draw(new Ellipse2D.Double(p.x - 4, p.y - 4, 8, 8));
        }
    }
}