package fyp;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * BenchmarkHarness  (v2 — integrated with StatisticsAnalyser)
 *
 * Measures the exact execution time of all four search algorithms
 * using System.nanoTime() for nanosecond precision.
 *
 * Improvements over v1:
 *   - Delegates statistical analysis to StatisticsAnalyser, which adds:
 *       * IQR-based outlier removal (eliminates GC pause spikes)
 *       * 95% Confidence Interval on the mean using Student's t-distribution
 *       * Coefficient of Variation (CV) for stability assessment
 *       * Median as a robust measure of central tendency
 *   - Extended CSV output includes CI bounds and CV column
 *   - Warns if CV > 0.10 (high timing instability detected)
 *
 * Design decisions:
 *   - JVM warm-up: 5 warm-up runs before timing begins (eliminates JIT bias)
 *   - Repeated runs: 30 timed runs per test (statistical reliability)
 *   - Tests both PRESENT and ABSENT targets
 *   - Dataset sizes: 1k, 5k, 10k, 50k, 100k, 500k, 1M elements
 *   - Results exported to CSV for regression modelling and graph generation
 */
public class BenchmarkHarness {

    private static final int    WARMUP_RUNS  = 5;
    private static final int    TIMED_RUNS   = 30;
    private static final String RESULTS_PATH = "D:\\ExactTimeSearchModel\\results\\results.csv";
    private static final int[]  SIZES        = {1000, 5000, 10000, 50000, 100000, 500000, 1000000};

    public static void runAll() {
        System.out.println("=== Exact-Time Search Algorithm Benchmark (v2) ===\n");

        int[] fullRealData = DatasetLoader.loadRealDataset();

        try (PrintWriter writer = new PrintWriter(new FileWriter(RESULTS_PATH))) {

            // Extended CSV header includes CI bounds and CV
            writer.println("Algorithm,DatasetSize,TargetType,MeanNs,MedianNs,MinNs,MaxNs,"
                         + "StdDevNs,CILowNs,CIHighNs,CV,OutliersRemoved");

            for (int size : SIZES) {
                System.out.println("--- Testing n = " + size + " ---");

                int[] data;
                if (size <= fullRealData.length) {
                    data = DatasetLoader.getSubset(fullRealData, size);
                } else {
                    data = DatasetLoader.generateSyntheticDataset(size, 10_000_000);
                    System.out.println("  (synthetic data — real dataset has "
                                       + fullRealData.length + " records)");
                }

                int presentTarget = DatasetLoader.getPresentTarget(data);
                int absentTarget  = DatasetLoader.getAbsentTarget(data);

                benchmarkAndRecord("Linear",        data, presentTarget, "present", writer);
                benchmarkAndRecord("Linear",        data, absentTarget,  "absent",  writer);
                benchmarkAndRecord("Binary",        data, presentTarget, "present", writer);
                benchmarkAndRecord("Binary",        data, absentTarget,  "absent",  writer);
                benchmarkAndRecord("Jump",          data, presentTarget, "present", writer);
                benchmarkAndRecord("Jump",          data, absentTarget,  "absent",  writer);
                benchmarkAndRecord("Interpolation", data, presentTarget, "present", writer);
                benchmarkAndRecord("Interpolation", data, absentTarget,  "absent",  writer);

                System.out.println();
            }

        } catch (IOException e) {
            System.err.println("Could not write results: " + e.getMessage());
            System.err.println("Ensure folder exists: D:\\ExactTimeSearchModel\\results\\");
        }

        System.out.println("Benchmark complete. Results saved to: " + RESULTS_PATH);
    }

    private static void benchmarkAndRecord(String algorithmName, int[] data,
                                            int target, String targetType,
                                            PrintWriter writer) {
        long[] times = new long[TIMED_RUNS];

        // JVM warm-up: allows JIT to optimise before timing begins
        for (int i = 0; i < WARMUP_RUNS; i++) {
            runAlgorithm(algorithmName, data, target);
        }

        // Timed phase
        for (int i = 0; i < TIMED_RUNS; i++) {
            long start = System.nanoTime();
            runAlgorithm(algorithmName, data, target);
            long end   = System.nanoTime();
            times[i]   = end - start;
        }

        // Statistical analysis (includes IQR outlier removal and 95% CI)
        StatisticsAnalyser.Summary stats = StatisticsAnalyser.analyse(times);

        // Console output
        String warning = stats.coefficientOfVariation > 0.10 ? " [HIGH VARIANCE - CV > 0.10]" : "";
        System.out.printf("  %-15s %-8s  %s%s%n",
                algorithmName, targetType, stats, warning);

        // Extended CSV output
        writer.printf("%s,%d,%s,%.0f,%.0f,%d,%d,%.0f,%.0f,%.0f,%.4f,%d%n",
                algorithmName, data.length, targetType,
                stats.mean, stats.median, stats.min, stats.max,
                stats.stdDev, stats.ciLow, stats.ciHigh,
                stats.coefficientOfVariation, stats.outliersRemoved);
    }

    private static int runAlgorithm(String name, int[] data, int target) {
        switch (name) {
            case "Linear":        return LinearSearch.search(data, target);
            case "Binary":        return BinarySearch.search(data, target);
            case "Jump":          return JumpSearch.search(data, target);
            case "Interpolation": return InterpolationSearch.search(data, target);
            default: throw new IllegalArgumentException("Unknown algorithm: " + name);
        }
    }
}
