import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.geom.*;
import javax.imageio.ImageIO;

/**
 * PepsiVsCokeReport.java
 * 
 * Generates a visual bar-chart comparison and a full report document
 * showing the results of the Pepsi vs Coca-Cola stock analysis.
 * 
 * Outputs:
 * 1. pepsi_vs_coke_chart.png — bar chart image comparing key metrics
 * 2. Console output — full report with code snippets & results
 * 
 * Dataset: https://www.kaggle.com/datasets/sandytcs/pepsi-vs-coke
 */
public class PepsiVsCokeReport {

    // ─── Stock record (same as main program) ─────────────────────────────────
    static class StockRecord {
        String date;
        double open, high, low, close;
        long volume;

        StockRecord(String d, double o, double h, double l, double c, long v) {
            date = d;
            open = o;
            high = h;
            low = l;
            close = c;
            volume = v;
        }
    }

    // ─── Dividend record ─────────────────────────────────────────────────────
    static class DividendRecord {
        String date;
        double dividend;

        DividendRecord(String d, double div) {
            date = d;
            dividend = div;
        }
    }

    // ─── CSV readers ─────────────────────────────────────────────────────────
    static List<StockRecord> readStockCSV(String path) throws IOException {
        List<StockRecord> list = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(path));
        br.readLine(); // header
        String line;
        while ((line = br.readLine()) != null) {
            String[] p = line.split(",");
            if (p.length >= 6)
                list.add(new StockRecord(p[0].trim(),
                        Double.parseDouble(p[1].trim()), Double.parseDouble(p[2].trim()),
                        Double.parseDouble(p[3].trim()), Double.parseDouble(p[4].trim()),
                        Long.parseLong(p[5].trim())));
        }
        br.close();
        return list;
    }

    static List<DividendRecord> readDividendCSV(String path) throws IOException {
        List<DividendRecord> list = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(path));
        br.readLine();
        String line;
        while ((line = br.readLine()) != null) {
            String[] p = line.split(",");
            if (p.length >= 2)
                list.add(new DividendRecord(p[0].trim(), Double.parseDouble(p[1].trim())));
        }
        br.close();
        return list;
    }

    // ─── Metric calculators ──────────────────────────────────────────────────
    static double avgClose(List<StockRecord> r) {
        double s = 0;
        for (StockRecord x : r)
            s += x.close;
        return s / r.size();
    }

    static double totalReturn(List<StockRecord> r) {
        return ((r.get(r.size() - 1).close - r.get(0).close) / r.get(0).close) * 100;
    }

    static double avgVolume(List<StockRecord> r) {
        long s = 0;
        for (StockRecord x : r)
            s += x.volume;
        return (double) s / r.size();
    }

    static double volatility(List<StockRecord> r) {
        double m = avgClose(r), s = 0;
        for (StockRecord x : r)
            s += Math.pow(x.close - m, 2);
        return Math.sqrt(s / r.size());
    }

    static double totalDividends(List<DividendRecord> r) {
        double s = 0;
        for (DividendRecord x : r)
            s += x.dividend;
        return s;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CHART GENERATION — Creates a professional bar chart as a PNG image
    // ═══════════════════════════════════════════════════════════════════════════
    static void generateChart(String[] labels, double[] koValues, double[] pepValues,
            String title, String outputFile) throws IOException {

        int width = 1200;
        int height = 750;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        // Anti-aliasing for smooth rendering
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // ── Background gradient ──────────────────────────────────────────────
        GradientPaint bg = new GradientPaint(0, 0, new Color(20, 20, 40),
                width, height, new Color(40, 40, 70));
        g.setPaint(bg);
        g.fillRect(0, 0, width, height);

        // ── Title ────────────────────────────────────────────────────────────
        g.setFont(new Font("SansSerif", Font.BOLD, 28));
        g.setColor(Color.WHITE);
        FontMetrics titleFM = g.getFontMetrics();
        int titleX = (width - titleFM.stringWidth(title)) / 2;
        g.drawString(title, titleX, 45);

        // ── Subtitle ─────────────────────────────────────────────────────────
        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g.setColor(new Color(180, 180, 200));
        String subtitle = "Data from Kaggle 'Pepsi vs Coke' dataset (2019-2023)";
        FontMetrics subFM = g.getFontMetrics();
        g.drawString(subtitle, (width - subFM.stringWidth(subtitle)) / 2, 70);

        // ── Chart area ───────────────────────────────────────────────────────
        int chartLeft = 80;
        int chartRight = width - 40;
        int chartTop = 100;
        int chartBottom = height - 120;
        int chartWidth = chartRight - chartLeft;
        int chartHeight = chartBottom - chartTop;

        // ── Grid background ──────────────────────────────────────────────────
        g.setColor(new Color(30, 30, 55));
        g.fillRoundRect(chartLeft - 10, chartTop - 10, chartWidth + 20, chartHeight + 20, 15, 15);

        // ── Find max value for scaling ───────────────────────────────────────
        double maxVal = 0;
        for (int i = 0; i < labels.length; i++) {
            maxVal = Math.max(maxVal, Math.max(koValues[i], pepValues[i]));
        }
        maxVal *= 1.15; // 15% headroom

        // ── Draw horizontal grid lines ───────────────────────────────────────
        g.setColor(new Color(60, 60, 90));
        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        int gridLines = 5;
        for (int i = 0; i <= gridLines; i++) {
            int y = chartBottom - (int) (chartHeight * i / (double) gridLines);
            g.setColor(new Color(60, 60, 90));
            g.drawLine(chartLeft, y, chartRight, y);
            g.setColor(new Color(150, 150, 170));
            double val = maxVal * i / gridLines;
            String valStr = String.format("%.1f", val);
            g.drawString(valStr, chartLeft - g.getFontMetrics().stringWidth(valStr) - 8, y + 4);
        }

        // ── Draw bars ────────────────────────────────────────────────────────
        int numGroups = labels.length;
        int groupWidth = chartWidth / numGroups;
        int barWidth = (int) (groupWidth * 0.30);
        int gap = (int) (groupWidth * 0.08);

        // Coca-Cola color: classic red
        Color koColor = new Color(220, 40, 40);
        Color koColorDark = new Color(180, 30, 30);
        // PepsiCo color: Pepsi blue
        Color pepColor = new Color(40, 100, 220);
        Color pepColorDark = new Color(30, 80, 180);

        g.setFont(new Font("SansSerif", Font.BOLD, 12));

        for (int i = 0; i < numGroups; i++) {
            int groupX = chartLeft + i * groupWidth + (groupWidth - 2 * barWidth - gap) / 2;

            // KO bar
            int koH = (int) (chartHeight * (koValues[i] / maxVal));
            int koX = groupX;
            int koY = chartBottom - koH;
            GradientPaint koGrad = new GradientPaint(koX, koY, koColor, koX + barWidth, koY + koH, koColorDark);
            g.setPaint(koGrad);
            g.fillRoundRect(koX, koY, barWidth, koH, 6, 6);
            // KO value label
            g.setColor(new Color(255, 180, 180));
            String koLabel = formatValue(koValues[i]);
            FontMetrics barFM = g.getFontMetrics();
            g.drawString(koLabel, koX + (barWidth - barFM.stringWidth(koLabel)) / 2, koY - 6);

            // PEP bar
            int pepH = (int) (chartHeight * (pepValues[i] / maxVal));
            int pepX = groupX + barWidth + gap;
            int pepY = chartBottom - pepH;
            GradientPaint pepGrad = new GradientPaint(pepX, pepY, pepColor, pepX + barWidth, pepY + pepH, pepColorDark);
            g.setPaint(pepGrad);
            g.fillRoundRect(pepX, pepY, barWidth, pepH, 6, 6);
            // PEP value label
            g.setColor(new Color(180, 200, 255));
            String pepLabel = formatValue(pepValues[i]);
            g.drawString(pepLabel, pepX + (barWidth - barFM.stringWidth(pepLabel)) / 2, pepY - 6);

            // Category label
            g.setColor(new Color(200, 200, 220));
            g.setFont(new Font("SansSerif", Font.PLAIN, 11));
            FontMetrics catFM = g.getFontMetrics();
            int catX = groupX + (2 * barWidth + gap - catFM.stringWidth(labels[i])) / 2;
            g.drawString(labels[i], catX, chartBottom + 18);
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
        }

        // ── Legend ───────────────────────────────────────────────────────────
        int legendX = width / 2 - 120;
        int legendY = height - 55;

        g.setColor(koColor);
        g.fillRoundRect(legendX, legendY, 20, 14, 4, 4);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        g.drawString("Coca-Cola (KO)", legendX + 26, legendY + 12);

        g.setColor(pepColor);
        g.fillRoundRect(legendX + 170, legendY, 20, 14, 4, 4);
        g.setColor(Color.WHITE);
        g.drawString("PepsiCo (PEP)", legendX + 196, legendY + 12);

        // ── Border ───────────────────────────────────────────────────────────
        g.setColor(new Color(80, 80, 120));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(2, 2, width - 4, height - 4, 20, 20);

        g.dispose();
        ImageIO.write(image, "png", new File(outputFile));
    }

    static String formatValue(double v) {
        if (v >= 1_000_000)
            return String.format("%.1fM", v / 1_000_000);
        if (v >= 1_000)
            return String.format("%.1fK", v / 1_000);
        if (v == (long) v)
            return String.format("%.0f", v);
        return String.format("%.2f", v);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MAIN — Generate chart + print report
    // ═══════════════════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        try {
            // ── Load data ────────────────────────────────────────────────────
            List<StockRecord> ko = readStockCSV("data/KO_stock_price.csv");
            List<StockRecord> pep = readStockCSV("data/PEP_stock_price.csv");
            List<DividendRecord> koD = readDividendCSV("data/KO_stock_dividend.csv");
            List<DividendRecord> peD = readDividendCSV("data/PEP_stock_dividend.csv");

            // ── Calculate metrics ────────────────────────────────────────────
            double koAvg = avgClose(ko), pepAvg = avgClose(pep);
            double koRet = totalReturn(ko), pepRet = totalReturn(pep);
            double koVolm = avgVolume(ko), pepVolm = avgVolume(pep);
            double koStd = volatility(ko), pepStd = volatility(pep);
            double koDiv = totalDividends(koD), pepDiv = totalDividends(peD);

            // ── Generate the bar chart ───────────────────────────────────────
            String[] labels = { "Avg Close ($)", "Return (%)", "Volume (K)", "Volatility ($)", "Dividends ($)" };
            double[] koVals = { koAvg, koRet, koVolm / 1000, koStd, koDiv };
            double[] pepVals = { pepAvg, pepRet, pepVolm / 1000, pepStd, pepDiv };

            String chartFile = "pepsi_vs_coke_chart.png";
            generateChart(labels, koVals, pepVals,
                    "Pepsi vs Coca-Cola — Stock Performance Comparison", chartFile);

            // ══════════════════════════════════════════════════════════════════
            // CONSOLE REPORT
            // ══════════════════════════════════════════════════════════════════
            System.out.println();
            System.out.println("╔══════════════════════════════════════════════════════════════════════╗");
            System.out.println("║          PEPSI vs COCA-COLA  —  ANALYSIS REPORT                    ║");
            System.out.println("║   Dataset: Kaggle 'Pepsi vs Coke' by Sandeep Kulkarni              ║");
            System.out.println("╚══════════════════════════════════════════════════════════════════════╝");

            // ── Section 1: Important Code Snippets ───────────────────────────
            System.out.println();
            System.out.println("┌──────────────────────────────────────────────────────────────────────┐");
            System.out.println("│  IMPORTANT CODE SNIPPETS                                            │");
            System.out.println("└──────────────────────────────────────────────────────────────────────┘");

            System.out.println();
            System.out.println("  [Snippet 1] Reading CSV data with BufferedReader:");
            System.out.println("  ─────────────────────────────────────────────────");
            System.out.println("    public static List<StockRecord> readStockCSV(String filePath)");
            System.out.println("            throws IOException {");
            System.out.println("        List<StockRecord> records = new ArrayList<>();");
            System.out.println("        BufferedReader br = new BufferedReader(new FileReader(filePath));");
            System.out.println("        String line = br.readLine(); // skip header");
            System.out.println("        while ((line = br.readLine()) != null) {");
            System.out.println("            String[] parts = line.split(\",\");");
            System.out.println("            records.add(new StockRecord(parts[0], ...));");
            System.out.println("        }");
            System.out.println("        return records;");
            System.out.println("    }");

            System.out.println();
            System.out.println("  [Snippet 2] Calculating total stock return:");
            System.out.println("  ────────────────────────────────────────────");
            System.out.println("    public static double totalReturn(List<StockRecord> records) {");
            System.out.println("        double first = records.get(0).close;");
            System.out.println("        double last  = records.get(records.size() - 1).close;");
            System.out.println("        return ((last - first) / first) * 100.0;");
            System.out.println("    }");

            System.out.println();
            System.out.println("  [Snippet 3] Volatility (Standard Deviation):");
            System.out.println("  ─────────────────────────────────────────────");
            System.out.println("    public static double volatility(List<StockRecord> records) {");
            System.out.println("        double mean = averageClose(records);");
            System.out.println("        double sumSq = 0;");
            System.out.println("        for (StockRecord r : records)");
            System.out.println("            sumSq += Math.pow(r.close - mean, 2);");
            System.out.println("        return Math.sqrt(sumSq / records.size());");
            System.out.println("    }");

            System.out.println();
            System.out.println("  [Snippet 4] Generating bar chart with Java AWT Graphics2D:");
            System.out.println("  ──────────────────────────────────────────────────────────");
            System.out.println("    BufferedImage image = new BufferedImage(1200, 750,");
            System.out.println("                              BufferedImage.TYPE_INT_ARGB);");
            System.out.println("    Graphics2D g = image.createGraphics();");
            System.out.println("    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,");
            System.out.println("                       RenderingHints.VALUE_ANTIALIAS_ON);");
            System.out.println("    // ... draw bars, labels, legend ...");
            System.out.println("    ImageIO.write(image, \"png\", new File(outputFile));");

            // ── Section 2: Metric Results ────────────────────────────────────
            System.out.println();
            System.out.println("┌──────────────────────────────────────────────────────────────────────┐");
            System.out.println("│  ANALYSIS RESULTS                                                   │");
            System.out.println("└──────────────────────────────────────────────────────────────────────┘");
            System.out.println();
            System.out.println("  ┌──────────────────────┬────────────────┬────────────────┬──────────┐");
            System.out.println("  │ Metric               │ Coca-Cola (KO) │ PepsiCo (PEP)  │ Winner   │");
            System.out.println("  ├──────────────────────┼────────────────┼────────────────┼──────────┤");
            System.out.printf("  │ Avg Close Price ($)  │ %14.2f │ %14.2f │ %-8s │%n",
                    koAvg, pepAvg, pepAvg > koAvg ? "PEP" : "KO");
            System.out.printf("  │ Total Return (%%)     │ %13.2f%% │ %13.2f%% │ %-8s │%n",
                    koRet, pepRet, pepRet > koRet ? "PEP" : "KO");
            System.out.printf("  │ Avg Volume           │ %,14.0f │ %,14.0f │ %-8s │%n",
                    koVolm, pepVolm, koVolm > pepVolm ? "KO" : "PEP");
            System.out.printf("  │ Volatility ($)       │ %14.2f │ %14.2f │ %-8s │%n",
                    koStd, pepStd, pepStd < koStd ? "PEP" : "KO");
            System.out.printf("  │ Total Dividends ($)  │ %14.2f │ %14.2f │ %-8s │%n",
                    koDiv, pepDiv, pepDiv > koDiv ? "PEP" : "KO");
            System.out.println("  └──────────────────────┴────────────────┴────────────────┴──────────┘");

            // ── Section 3: Scoring ───────────────────────────────────────────
            int koScore = 0, pepScore = 0;
            if (pepAvg > koAvg)
                pepScore++;
            else
                koScore++;
            if (pepRet > koRet)
                pepScore++;
            else
                koScore++;
            if (koVolm > pepVolm)
                koScore++;
            else
                pepScore++;
            if (pepStd < koStd)
                pepScore++;
            else
                koScore++;
            if (pepDiv > koDiv)
                pepScore++;
            else
                koScore++;

            System.out.println();
            System.out.println("  FINAL SCORE:  Coca-Cola = " + koScore + "    PepsiCo = " + pepScore);
            System.out.println();

            if (pepScore > koScore) {
                System.out.println("  ╔═══════════════════════════════════════════════════════════════╗");
                System.out.println("  ║  ★  WINNER: PepsiCo (PEP) is the BETTER investment!  ★      ║");
                System.out.println("  ║                                                             ║");
                System.out.println("  ║  PepsiCo outperforms Coca-Cola with a higher average stock  ║");
                System.out.println("  ║  price, stronger total return, and more generous dividend    ║");
                System.out.println("  ║  payments over the 2019-2023 period. While Coca-Cola has    ║");
                System.out.println("  ║  higher trading volume, PepsiCo delivers superior value     ║");
                System.out.println("  ║  for long-term investors.                                   ║");
                System.out.println("  ╚═══════════════════════════════════════════════════════════════╝");
            } else if (koScore > pepScore) {
                System.out.println("  ╔═══════════════════════════════════════════════════════════════╗");
                System.out.println("  ║  ★  WINNER: Coca-Cola (KO) is the BETTER investment!  ★     ║");
                System.out.println("  ║                                                             ║");
                System.out.println("  ║  Coca-Cola outperforms PepsiCo across the majority of key   ║");
                System.out.println("  ║  investment metrics during the 2019-2023 period.            ║");
                System.out.println("  ╚═══════════════════════════════════════════════════════════════╝");
            } else {
                System.out.println("  ╔═══════════════════════════════════════════════════════════════╗");
                System.out.println("  ║  ★  RESULT: It's a TIE!  Both are strong investments.  ★    ║");
                System.out.println("  ╚═══════════════════════════════════════════════════════════════╝");
            }

            // ── Chart file notice ────────────────────────────────────────────
            System.out.println();
            System.out.println("  ✓ Bar chart saved to: " + chartFile);
            System.out.println("  ✓ Open the PNG file to view the visual comparison.");
            System.out.println();

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
