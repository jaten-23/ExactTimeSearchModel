package fyp;

import java.util.Arrays;

/**
 * StatisticsAnalyser
 *
 * Upgrades the benchmark harness from basic mean/min/max/stddev reporting
 * to publication-grade statistical analysis.
 *
 * Features:
 *   - IQR-based outlier removal (Tukey fences: Q1 - 1.5*IQR, Q3 + 1.5*IQR)
 *     Eliminates GC pause spikes and OS context-switch noise
 *   - 95% Confidence Interval on the mean using Student's t-distribution
 *     (correct for small samples where the normal approximation is inadequate)
 *   - Coefficient of Variation (CV = stdDev / mean)
 *     A CV > 0.10 indicates high timing instability
 *   - Median for robust central tendency (resistant to skew)
 *
 * Reference:
 *   Georges, A., Buytaert, D. and Eeckhout, L. (2007) 'Statistically rigorous
 *   Java performance evaluation', ACM SIGPLAN Notices, 42(10), pp. 57-76.
 */
public final class StatisticsAnalyser {

    private StatisticsAnalyser() {}

    // -----------------------------------------------------------------------
    //  Public result container
    // -----------------------------------------------------------------------

    public static final class Summary {
        public final int    sampleSize;
        public final int    outliersRemoved;
        public final double mean;
        public final double median;
        public final long   min;
        public final long   max;
        public final double stdDev;
        public final double coefficientOfVariation;
        public final double ciLow;
        public final double ciHigh;

        private Summary(int n, int removed, double mean, double median,
                        long min, long max, double sd, double cv,
                        double ciLow, double ciHigh) {
            this.sampleSize             = n;
            this.outliersRemoved        = removed;
            this.mean                   = mean;
            this.median                 = median;
            this.min                    = min;
            this.max                    = max;
            this.stdDev                 = sd;
            this.coefficientOfVariation = cv;
            this.ciLow                  = ciLow;
            this.ciHigh                 = ciHigh;
        }

        @Override
        public String toString() {
            return String.format(
                "n=%d  mean=%,.0f ns  median=%,.0f ns  95%% CI=[%,.0f, %,.0f]  CV=%.3f  outliers=%d",
                sampleSize, mean, median, ciLow, ciHigh, coefficientOfVariation, outliersRemoved);
        }
    }

    // -----------------------------------------------------------------------
    //  Main analysis pipeline
    // -----------------------------------------------------------------------

    /**
     * Runs the full statistical pipeline on raw timing data.
     * Pipeline: sort -> compute quartiles -> remove IQR outliers
     *           -> compute mean/median/stdDev -> compute 95% CI
     *
     * @param rawTimings array of nanosecond measurements from the benchmark
     * @return immutable Summary of the cleaned sample
     */
    public static Summary analyse(long[] rawTimings) {
        if (rawTimings == null || rawTimings.length == 0)
            throw new IllegalArgumentException("Empty timing sample.");

        long[] sorted = rawTimings.clone();
        Arrays.sort(sorted);

        // Quartiles on the full sample
        double q1  = percentile(sorted, 25.0);
        double q3  = percentile(sorted, 75.0);
        double iqr = q3 - q1;

        // Tukey fences
        double lowerFence = q1 - 1.5 * iqr;
        double upperFence = q3 + 1.5 * iqr;

        // Filter outliers
        long[] kept = Arrays.stream(sorted)
                            .filter(t -> t >= lowerFence && t <= upperFence)
                            .toArray();
        int removed = sorted.length - kept.length;

        // Safety fallback: if filtering is too aggressive, use full sample
        if (kept.length < 3) { kept = sorted; removed = 0; }

        double mean   = mean(kept);
        double median = percentile(kept, 50.0);
        double sd     = sampleStdDev(kept, mean);
        double cv     = (mean == 0) ? 0.0 : (sd / mean);

        // 95% CI using Student's t
        double se     = sd / Math.sqrt(kept.length);
        double t      = tCritical(kept.length - 1);
        double ciLow  = mean - t * se;
        double ciHigh = mean + t * se;

        return new Summary(kept.length, removed, mean, median,
                           kept[0], kept[kept.length - 1], sd, cv, ciLow, ciHigh);
    }

    // -----------------------------------------------------------------------
    //  Private helpers
    // -----------------------------------------------------------------------

    private static double mean(long[] v) {
        long sum = 0;
        for (long x : v) sum += x;
        return (double) sum / v.length;
    }

    private static double sampleStdDev(long[] v, double mean) {
        if (v.length < 2) return 0.0;
        double sq = 0;
        for (long x : v) { double d = x - mean; sq += d * d; }
        return Math.sqrt(sq / (v.length - 1));
    }

    /** Linear-interpolation percentile on a pre-sorted array. */
    private static double percentile(long[] sorted, double p) {
        if (sorted.length == 1) return sorted[0];
        double rank  = (p / 100.0) * (sorted.length - 1);
        int    lower = (int) Math.floor(rank);
        int    upper = (int) Math.ceil(rank);
        return sorted[lower] + (rank - lower) * (sorted[upper] - sorted[lower]);
    }

    /**
     * Two-tailed t critical value at 95% confidence.
     * Lookup table for df 1-29; asymptotes to z=1.96 for df >= 30.
     */
    private static double tCritical(int df) {
        if (df <= 0)  return Double.NaN;
        if (df >= 30) return 1.960;
        double[] tbl = {
            12.706, 4.303, 3.182, 2.776, 2.571, 2.447, 2.365, 2.306, 2.262, 2.228,
             2.201, 2.179, 2.160, 2.145, 2.131, 2.120, 2.110, 2.101, 2.093, 2.086,
             2.080, 2.074, 2.069, 2.064, 2.060, 2.056, 2.052, 2.048, 2.045
        };
        return tbl[df - 1];
    }
}
