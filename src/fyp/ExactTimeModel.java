package fyp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ExactTimeModel
 *
 * Closes the gap between benchmarking and modelling by fitting a regression
 * equation to empirical timing data for each algorithm.
 *
 * This is the module that directly addresses the project title:
 * "Analysis of Optimal Search Algorithm Implementations by Exact-Time Modelling"
 *
 * Model forms chosen to match theoretical time complexity:
 * +-----------------+---------------------+-------------------------------+
 * | Algorithm       | Complexity          | Regression model              |
 * +-----------------+---------------------+-------------------------------+
 * | Linear Search   | O(n)                | T = a*n + b                   |
 * | Binary Search   | O(log n)            | T = a*log2(n) + b             |
 * | Jump Search     | O(sqrt(n))          | T = a*sqrt(n) + b             |
 * | Interpolation   | O(log log n)        | T = a*log2(log2(n)) + b       |
 * +-----------------+---------------------+-------------------------------+
 *
 * Fitting method: Ordinary Least Squares (OLS) after linearising the predictor.
 * For a model T = a*f(n) + b, substitute x_i = f(n_i) and fit the linear
 * regression T = a*x + b using the normal equations.
 *
 * Goodness-of-fit: R² (coefficient of determination).
 *   R² = 1 means perfect fit; R² > 0.95 indicates the model explains
 *   the observed timing pattern well.
 */
public final class ExactTimeModel {

    private ExactTimeModel() {}

    // -----------------------------------------------------------------------
    //  Public result container
    // -----------------------------------------------------------------------

    public static final class ModelResult {
        public final String algorithm;
        public final String targetType;
        public final String modelForm;   // e.g. "T = a*log2(n) + b"
        public final double a;           // slope coefficient
        public final double b;           // intercept
        public final double rSquared;    // goodness-of-fit

        private ModelResult(String algorithm, String targetType,
                            String modelForm, double a, double b, double r2) {
            this.algorithm  = algorithm;
            this.targetType = targetType;
            this.modelForm  = modelForm;
            this.a          = a;
            this.b          = b;
            this.rSquared   = r2;
        }

        @Override
        public String toString() {
            return String.format(
                "%-15s %-8s  %s  =>  T = %.4f * f(n) + %.4f  [R² = %.4f]",
                algorithm, targetType, modelForm, a, b, rSquared);
        }

        /**
         * Predicts execution time in nanoseconds for a given dataset size.
         * Uses the fitted coefficients and the model's transform of n.
         */
        public double predict(int n) {
            double x = transformN(algorithm, n);
            return a * x + b;
        }
    }

    // -----------------------------------------------------------------------
    //  Public API
    // -----------------------------------------------------------------------

    /**
     * Reads the benchmark results CSV, groups rows by algorithm and target type,
     * fits a regression model to each group, prints the equations, and returns all results.
     *
     * @param csvPath path to the results CSV produced by BenchmarkHarness
     * @return list of fitted ModelResult objects
     */
    public static List<ModelResult> fitAll(String csvPath) {
        List<ModelResult> results = new ArrayList<>();

        // Algorithms and target types to process
        String[] algorithms  = {"Linear", "Binary", "Jump", "Interpolation"};
        String[] targetTypes = {"present", "absent"};

        for (String algo : algorithms) {
            for (String target : targetTypes) {
                List<double[]> points = readPoints(csvPath, algo, target);
                if (points.size() < 2) continue;

                double[] ns    = points.stream().mapToDouble(p -> p[0]).toArray();
                double[] times = points.stream().mapToDouble(p -> p[1]).toArray();

                // Transform n according to theoretical complexity
                double[] xs = new double[ns.length];
                for (int i = 0; i < ns.length; i++) {
                    xs[i] = transformN(algo, (int) ns[i]);
                }

                double[] coeffs = olsRegression(xs, times);
                double   r2     = rSquared(xs, times, coeffs[0], coeffs[1]);
                String   form   = modelForm(algo);

                ModelResult model = new ModelResult(algo, target, form,
                                                    coeffs[0], coeffs[1], r2);
                results.add(model);
                System.out.println(model);
            }
        }

        printPredictions(results);
        return results;
    }

    // -----------------------------------------------------------------------
    //  Private helpers
    // -----------------------------------------------------------------------

    /**
     * Applies the theoretical complexity transform to n so the regression
     * can be carried out in the linearised space.
     */
    private static double transformN(String algorithm, int n) {
        switch (algorithm) {
            case "Linear":        return n;
            case "Binary":        return Math.log(n) / Math.log(2);           // log2(n)
            case "Jump":          return Math.sqrt(n);                         // sqrt(n)
            case "Interpolation": {
                double log2n = Math.log(n) / Math.log(2);
                return (log2n > 1) ? Math.log(log2n) / Math.log(2) : 1.0;     // log2(log2(n))
            }
            default: return n;
        }
    }

    /** Returns the human-readable model form string for each algorithm. */
    private static String modelForm(String algorithm) {
        switch (algorithm) {
            case "Linear":        return "T = a*n + b";
            case "Binary":        return "T = a*log2(n) + b";
            case "Jump":          return "T = a*sqrt(n) + b";
            case "Interpolation": return "T = a*log2(log2(n)) + b";
            default:              return "T = a*f(n) + b";
        }
    }

    /**
     * Reads (n, meanNs) data points for a specific algorithm and target type
     * from the benchmark results CSV.
     */
    private static List<double[]> readPoints(String csvPath, String algo, String target) {
        List<double[]> points = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(csvPath))) {
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                if (first) { first = false; continue; } // skip header
                String[] cols = line.split(",");
                if (cols.length < 4) continue;
                if (!cols[0].trim().equals(algo))   continue;
                if (!cols[2].trim().equals(target)) continue;
                double n    = Double.parseDouble(cols[1].trim());
                double mean = Double.parseDouble(cols[3].trim());
                points.add(new double[]{n, mean});
            }
        } catch (IOException e) {
            System.err.println("Could not read CSV: " + e.getMessage());
        }
        return points;
    }

    /**
     * Ordinary Least Squares linear regression: fits T = a*x + b.
     * Returns [a, b] using the closed-form normal equations.
     *
     * Formulae:
     *   a = (n*Σ(x_i*y_i) - Σx_i * Σy_i) / (n*Σ(x_i²) - (Σx_i)²)
     *   b = (Σy_i - a * Σx_i) / n
     */
    private static double[] olsRegression(double[] x, double[] y) {
        int    n   = x.length;
        double sx  = 0, sy  = 0, sxy = 0, sxx = 0;
        for (int i = 0; i < n; i++) {
            sx  += x[i];
            sy  += y[i];
            sxy += x[i] * y[i];
            sxx += x[i] * x[i];
        }
        double denom = n * sxx - sx * sx;
        if (Math.abs(denom) < 1e-12) return new double[]{0, sy / n};
        double a = (n * sxy - sx * sy) / denom;
        double b = (sy - a * sx) / n;
        return new double[]{a, b};
    }

    /**
     * Computes R² (coefficient of determination) for the fitted model.
     * R² = 1 - SS_res / SS_tot
     */
    private static double rSquared(double[] x, double[] y, double a, double b) {
        double yMean = 0;
        for (double yi : y) yMean += yi;
        yMean /= y.length;

        double ssTot = 0, ssRes = 0;
        for (int i = 0; i < x.length; i++) {
            double predicted = a * x[i] + b;
            ssRes += (y[i] - predicted) * (y[i] - predicted);
            ssTot += (y[i] - yMean)     * (y[i] - yMean);
        }
        if (ssTot == 0) return 1.0;
        return 1.0 - ssRes / ssTot;
    }

    /** Prints a table of predictions at representative dataset sizes. */
    private static void printPredictions(List<ModelResult> models) {
        int[] sizes = {1000, 10000, 100000, 1000000};
        System.out.println("\n--- Predicted execution times (ns) ---");
        System.out.printf("%-15s %-8s %10s %10s %10s %10s%n",
                          "Algorithm", "Target", "n=1k", "n=10k", "n=100k", "n=1M");
        for (ModelResult m : models) {
            System.out.printf("%-15s %-8s %10.0f %10.0f %10.0f %10.0f%n",
                m.algorithm, m.targetType,
                m.predict(sizes[0]), m.predict(sizes[1]),
                m.predict(sizes[2]), m.predict(sizes[3]));
        }
    }
}
