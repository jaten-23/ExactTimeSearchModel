package fyp;

/**
 * Main
 *
 * Entry point for the Exact-Time Search Algorithm Benchmark.
 * Runs the complete benchmark suite across all algorithms and dataset sizes,
 * then fits exact-time regression models to the results.
 *
 * Usage: java fyp.Main
 */
public class Main {

    public static void main(String[] args) {
        // Run the full benchmark suite (outputs results.csv)
        BenchmarkHarness.runAll();

        // Fit exact-time regression models to the generated CSV
        System.out.println("\n=== Fitting Exact-Time Models ===\n");
        ExactTimeModel.fitAll("D:\\ExactTimeSearchModel\\results\\results.csv");
    }
}
