import java.io.*;
import java.util.*;

/**
 * PepsiVsCoke.java
 * 
 * Analyzes stock market data from the Kaggle "Pepsi vs Coke" dataset
 * to determine which company is the better investment.
 * 
 * Dataset: https://www.kaggle.com/datasets/sandytcs/pepsi-vs-coke
 * 
 * Metrics Compared:
 *   1. Average Closing Price
 *   2. Total Stock Return (% gain from first to last close)
 *   3. Average Daily Trading Volume
 *   4. Price Volatility (Standard Deviation of closing prices)
 *   5. Total Dividends Paid Per Share
 *   6. Highest & Lowest Closing Prices
 */
public class PepsiVsCoke {

    // ─── Inner class to hold one row of stock data ───────────────────────────
    static class StockRecord {
        String date;
        double open, high, low, close;
        long volume;

        StockRecord(String date, double open, double high, double low, double close, long volume) {
            this.date   = date;
            this.open   = open;
            this.high   = high;
            this.low    = low;
            this.close  = close;
            this.volume = volume;
        }
    }

    // ─── Inner class to hold one dividend record ─────────────────────────────
    static class DividendRecord {
        String date;
        double dividend;

        DividendRecord(String date, double dividend) {
            this.date     = date;
            this.dividend = dividend;
        }
    }

    // ─── Read stock price CSV ────────────────────────────────────────────────
    public static List<StockRecord> readStockCSV(String filePath) throws IOException {
        List<StockRecord> records = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String line = br.readLine(); // skip header

        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",");
            if (parts.length >= 6) {
                records.add(new StockRecord(
                    parts[0].trim(),
                    Double.parseDouble(parts[1].trim()),
                    Double.parseDouble(parts[2].trim()),
                    Double.parseDouble(parts[3].trim()),
                    Double.parseDouble(parts[4].trim()),
                    Long.parseLong(parts[5].trim())
                ));
            }
        }
        br.close();
        return records;
    }

    // ─── Read dividend CSV ───────────────────────────────────────────────────
    public static List<DividendRecord> readDividendCSV(String filePath) throws IOException {
        List<DividendRecord> records = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String line = br.readLine(); // skip header

        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",");
            if (parts.length >= 2) {
                records.add(new DividendRecord(
                    parts[0].trim(),
                    Double.parseDouble(parts[1].trim())
                ));
            }
        }
        br.close();
        return records;
    }

    // ─── Calculate average closing price ─────────────────────────────────────
    public static double averageClose(List<StockRecord> records) {
        double sum = 0;
        for (StockRecord r : records) {
            sum += r.close;
        }
        return sum / records.size();
    }

    // ─── Calculate total return (%) ──────────────────────────────────────────
    public static double totalReturn(List<StockRecord> records) {
        double first = records.get(0).close;
        double last  = records.get(records.size() - 1).close;
        return ((last - first) / first) * 100.0;
    }

    // ─── Calculate average volume ────────────────────────────────────────────
    public static double averageVolume(List<StockRecord> records) {
        long sum = 0;
        for (StockRecord r : records) {
            sum += r.volume;
        }
        return (double) sum / records.size();
    }

    // ─── Calculate standard deviation of closing prices (volatility) ─────────
    public static double volatility(List<StockRecord> records) {
        double mean = averageClose(records);
        double sumSq = 0;
        for (StockRecord r : records) {
            sumSq += Math.pow(r.close - mean, 2);
        }
        return Math.sqrt(sumSq / records.size());
    }

    // ─── Find maximum closing price ──────────────────────────────────────────
    public static double maxClose(List<StockRecord> records) {
        double max = Double.MIN_VALUE;
        for (StockRecord r : records) {
            if (r.close > max) max = r.close;
        }
        return max;
    }

    // ─── Find minimum closing price ──────────────────────────────────────────
    public static double minClose(List<StockRecord> records) {
        double min = Double.MAX_VALUE;
        for (StockRecord r : records) {
            if (r.close < min) min = r.close;
        }
        return min;
    }

    // ─── Calculate total dividends paid per share ────────────────────────────
    public static double totalDividends(List<DividendRecord> records) {
        double sum = 0;
        for (DividendRecord r : records) {
            sum += r.dividend;
        }
        return sum;
    }

    // ─── Print a divider line ────────────────────────────────────────────────
    public static void printDivider() {
        System.out.println("═══════════════════════════════════════════════════════════════════");
    }

    // ─── Print a section header ──────────────────────────────────────────────
    public static void printHeader(String title) {
        printDivider();
        System.out.println("  " + title);
        printDivider();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  MAIN METHOD
    // ═══════════════════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        try {
            // ── File paths ───────────────────────────────────────────────────
            String koStockFile  = "data/KO_stock_price.csv";
            String pepStockFile = "data/PEP_stock_price.csv";
            String koDivFile    = "data/KO_stock_dividend.csv";
            String pepDivFile   = "data/PEP_stock_dividend.csv";

            // ── Read data ────────────────────────────────────────────────────
            List<StockRecord>    koRecords   = readStockCSV(koStockFile);
            List<StockRecord>    pepRecords  = readStockCSV(pepStockFile);
            List<DividendRecord> koDividends = readDividendCSV(koDivFile);
            List<DividendRecord> pepDividends = readDividendCSV(pepDivFile);

            System.out.println();
            printHeader("PEPSI vs COCA-COLA  —  STOCK MARKET ANALYSIS");
            System.out.println("  Dataset : Kaggle 'Pepsi vs Coke' by Sandeep Kulkarni");
            System.out.println("  Period  : " + koRecords.get(0).date + " to " +
                               koRecords.get(koRecords.size() - 1).date);
            System.out.println("  Records : Coca-Cola=" + koRecords.size() +
                               "  PepsiCo=" + pepRecords.size());
            printDivider();

            // ── Compute all metrics ──────────────────────────────────────────
            double koAvgClose  = averageClose(koRecords);
            double pepAvgClose = averageClose(pepRecords);

            double koReturn    = totalReturn(koRecords);
            double pepReturn   = totalReturn(pepRecords);

            double koAvgVol    = averageVolume(koRecords);
            double pepAvgVol   = averageVolume(pepRecords);

            double koVol       = volatility(koRecords);
            double pepVol      = volatility(pepRecords);

            double koMaxClose  = maxClose(koRecords);
            double pepMaxClose = maxClose(pepRecords);

            double koMinClose  = minClose(koRecords);
            double pepMinClose = minClose(pepRecords);

            double koTotalDiv  = totalDividends(koDividends);
            double pepTotalDiv = totalDividends(pepDividends);

            // ── Scoring system: who wins each metric ─────────────────────────
            int koScore  = 0;
            int pepScore = 0;

            // 1) Higher average close → stronger price
            if (pepAvgClose > koAvgClose)  pepScore++; else koScore++;
            // 2) Higher total return → better growth
            if (pepReturn > koReturn)      pepScore++; else koScore++;
            // 3) Higher average volume → more liquidity
            if (pepAvgVol > koAvgVol)      pepScore++; else koScore++;
            // 4) Lower volatility → more stable (less risky)
            if (pepVol < koVol)            pepScore++; else koScore++;
            // 5) Higher max close → higher peak
            if (pepMaxClose > koMaxClose)  pepScore++; else koScore++;
            // 6) Higher total dividends → better income
            if (pepTotalDiv > koTotalDiv)  pepScore++; else koScore++;

            // ══════════════════════════════════════════════════════════════════
            //  DETAILED RESULTS
            // ══════════════════════════════════════════════════════════════════
            System.out.println();
            printHeader("1. AVERAGE CLOSING PRICE");
            System.out.printf("  Coca-Cola (KO)  : $%.2f%n", koAvgClose);
            System.out.printf("  PepsiCo   (PEP) : $%.2f%n", pepAvgClose);
            System.out.println("  Winner          : " + (pepAvgClose > koAvgClose ? "PepsiCo ✓" : "Coca-Cola ✓"));

            System.out.println();
            printHeader("2. TOTAL STOCK RETURN (2019 – 2023)");
            System.out.printf("  Coca-Cola (KO)  : %.2f%%%n", koReturn);
            System.out.printf("  PepsiCo   (PEP) : %.2f%%%n", pepReturn);
            System.out.println("  Winner          : " + (pepReturn > koReturn ? "PepsiCo ✓" : "Coca-Cola ✓"));

            System.out.println();
            printHeader("3. AVERAGE MONTHLY TRADING VOLUME");
            System.out.printf("  Coca-Cola (KO)  : %,.0f shares%n", koAvgVol);
            System.out.printf("  PepsiCo   (PEP) : %,.0f shares%n", pepAvgVol);
            System.out.println("  Winner          : " + (pepAvgVol > koAvgVol ? "PepsiCo ✓" : "Coca-Cola ✓"));

            System.out.println();
            printHeader("4. PRICE VOLATILITY (Std Dev of Close)");
            System.out.printf("  Coca-Cola (KO)  : $%.2f%n", koVol);
            System.out.printf("  PepsiCo   (PEP) : $%.2f%n", pepVol);
            System.out.println("  Winner (lower)  : " + (pepVol < koVol ? "PepsiCo ✓" : "Coca-Cola ✓"));

            System.out.println();
            printHeader("5. HIGHEST & LOWEST CLOSING PRICES");
            System.out.printf("  Coca-Cola (KO)  : High = $%.2f  |  Low = $%.2f%n", koMaxClose, koMinClose);
            System.out.printf("  PepsiCo   (PEP) : High = $%.2f  |  Low = $%.2f%n", pepMaxClose, pepMinClose);
            System.out.println("  Higher Peak     : " + (pepMaxClose > koMaxClose ? "PepsiCo ✓" : "Coca-Cola ✓"));

            System.out.println();
            printHeader("6. TOTAL DIVIDENDS PAID PER SHARE");
            System.out.printf("  Coca-Cola (KO)  : $%.2f%n", koTotalDiv);
            System.out.printf("  PepsiCo   (PEP) : $%.2f%n", pepTotalDiv);
            System.out.println("  Winner          : " + (pepTotalDiv > koTotalDiv ? "PepsiCo ✓" : "Coca-Cola ✓"));

            // ══════════════════════════════════════════════════════════════════
            //  FINAL VERDICT
            // ══════════════════════════════════════════════════════════════════
            System.out.println();
            printDivider();
            printHeader("FINAL SCORECARD");
            System.out.println("  Coca-Cola (KO)  : " + koScore  + " / 6 metrics won");
            System.out.println("  PepsiCo   (PEP) : " + pepScore + " / 6 metrics won");
            System.out.println();

            if (pepScore > koScore) {
                System.out.println("  ★  VERDICT: PepsiCo (PEP) is the BETTER stock!  ★");
                System.out.println("     PepsiCo wins with higher returns, stronger price,");
                System.out.println("     and more generous dividends over the 2019-2023 period.");
            } else if (koScore > pepScore) {
                System.out.println("  ★  VERDICT: Coca-Cola (KO) is the BETTER stock!  ★");
                System.out.println("     Coca-Cola wins with stronger fundamentals and");
                System.out.println("     better overall performance over the 2019-2023 period.");
            } else {
                System.out.println("  ★  VERDICT: It's a TIE!  ★");
                System.out.println("     Both companies perform equally well across the metrics.");
            }

            printDivider();
            System.out.println();

            // ── Save results to output file ──────────────────────────────────
            PrintWriter pw = new PrintWriter(new FileWriter("analysis_output.txt"));
            pw.println("PEPSI vs COCA-COLA — STOCK ANALYSIS RESULTS");
            pw.println("Period: " + koRecords.get(0).date + " to " + koRecords.get(koRecords.size()-1).date);
            pw.println();
            pw.printf("Avg Close   — KO: $%.2f  |  PEP: $%.2f%n", koAvgClose, pepAvgClose);
            pw.printf("Total Return— KO: %.2f%%  |  PEP: %.2f%%%n", koReturn, pepReturn);
            pw.printf("Avg Volume  — KO: %,.0f  |  PEP: %,.0f%n", koAvgVol, pepAvgVol);
            pw.printf("Volatility  — KO: $%.2f  |  PEP: $%.2f%n", koVol, pepVol);
            pw.printf("Max Close   — KO: $%.2f  |  PEP: $%.2f%n", koMaxClose, pepMaxClose);
            pw.printf("Min Close   — KO: $%.2f  |  PEP: $%.2f%n", koMinClose, pepMinClose);
            pw.printf("Total Divs  — KO: $%.2f  |  PEP: $%.2f%n", koTotalDiv, pepTotalDiv);
            pw.println();
            pw.println("Score: KO=" + koScore + " PEP=" + pepScore);
            if (pepScore > koScore) pw.println("WINNER: PepsiCo (PEP)");
            else if (koScore > pepScore) pw.println("WINNER: Coca-Cola (KO)");
            else pw.println("RESULT: TIE");
            pw.close();
            System.out.println("  Results saved to: analysis_output.txt");
            System.out.println();

        } catch (IOException e) {
            System.err.println("Error reading data files: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
