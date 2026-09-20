package daa;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Runs the whole benchmark suite and writes the raw measurements to CSV.
 *
 * Methodology:
 *  - every (algorithm, input type, n) configuration is executed WARMUP times
 *    first so that the JIT compiler can reach steady state, and only the
 *    following TRIALS runs are recorded;
 *  - a fresh copy of the input is created before every run, outside the
 *    timed region;
 *  - timing uses System.nanoTime() around the algorithm call only.
 */
public final class Experiment {

    private static final int[] SORT_SIZES = {1_000, 5_000, 10_000, 50_000, 100_000, 200_000};
    private static final int[] SELECT_SIZES = {1_000, 5_000, 10_000, 50_000, 100_000, 200_000};
    private static final int[] CLOSEST_SIZES = {1_000, 5_000, 10_000, 50_000, 100_000};

    private static final int WARMUP = 3;
    private static final int TRIALS = 5;
    private static final long SEED = 20250920L;

    private Experiment() {
    }

    public static void run(Path csvPath) throws IOException {
        List<String> rows = new ArrayList<>();
        Random random = new Random(SEED);

        System.out.println("=== Sorting benchmarks ===");
        for (ArrayUtils.InputType type : ArrayUtils.InputType.values()) {
            for (int n : SORT_SIZES) {
                int[] base = ArrayUtils.generate(type, n, random);
                benchmarkSort("MergeSort", base, type, rows);
                benchmarkSort("QuickSort", base, type, rows);
                benchmarkSort("Arrays.sort", base, type, rows);
            }
        }

        System.out.println();
        System.out.println("=== Deterministic Select benchmarks ===");
        for (ArrayUtils.InputType type : ArrayUtils.InputType.values()) {
            for (int n : SELECT_SIZES) {
                int[] base = ArrayUtils.generate(type, n, random);
                benchmarkSelect(base, type, rows);
            }
        }

        System.out.println();
        System.out.println("=== Closest Pair benchmarks ===");
        for (int n : CLOSEST_SIZES) {
            Point[] points = ArrayUtils.randomPoints(n, random);
            benchmarkClosestPair(points, rows);
        }

        Files.createDirectories(csvPath.toAbsolutePath().getParent());
        try (PrintWriter out = new PrintWriter(
                Files.newBufferedWriter(csvPath, StandardCharsets.UTF_8))) {
            out.println(Metrics.csvHeader());
            for (String row : rows) {
                out.println(row);
            }
        }
        System.out.println();
        System.out.println("Saved " + rows.size() + " measurements to " + csvPath.toAbsolutePath());
    }

    private static void benchmarkSort(String algorithm, int[] base,
                                      ArrayUtils.InputType type, List<String> rows) {
        Random pivotRandom = new Random(SEED);
        for (int i = 0; i < WARMUP; i++) {
            runSortOnce(algorithm, base.clone(), new Metrics(algorithm), pivotRandom);
        }
        Metrics last = null;
        for (int trial = 1; trial <= TRIALS; trial++) {
            Metrics m = new Metrics(algorithm);
            m.setInputType(type.name());
            m.setInputSize(base.length);
            int[] copy = base.clone();
            runSortOnce(algorithm, copy, m, pivotRandom);
            if (!ArrayUtils.isSorted(copy)) {
                throw new IllegalStateException(algorithm + " produced an unsorted array");
            }
            rows.add(m.toCsvRow(trial));
            last = m;
        }
        System.out.println(last);
    }

    private static void runSortOnce(String algorithm, int[] a, Metrics m, Random pivotRandom) {
        switch (algorithm) {
            case "MergeSort":
                m.startTimer();
                MergeSorter.sort(a, m);
                m.stopTimer();
                break;
            case "QuickSort":
                m.startTimer();
                QuickSorter.sort(a, m, pivotRandom);
                m.stopTimer();
                break;
            case "Arrays.sort":
                m.startTimer();
                Arrays.sort(a);
                m.stopTimer();
                break;
            default:
                throw new IllegalArgumentException(algorithm);
        }
    }

    private static void benchmarkSelect(int[] base, ArrayUtils.InputType type, List<String> rows) {
        int k = base.length / 2;
        for (int i = 0; i < WARMUP; i++) {
            DeterministicSelector.select(base.clone(), k, new Metrics("DeterministicSelect"));
        }
        Metrics last = null;
        for (int trial = 1; trial <= TRIALS; trial++) {
            Metrics m = new Metrics("DeterministicSelect");
            m.setInputType(type.name());
            m.setInputSize(base.length);
            int[] copy = base.clone();
            m.startTimer();
            DeterministicSelector.select(copy, k, m);
            m.stopTimer();
            rows.add(m.toCsvRow(trial));
            last = m;
        }
        System.out.println(last);
    }

    private static void benchmarkClosestPair(Point[] points, List<String> rows) {
        for (int i = 0; i < WARMUP; i++) {
            ClosestPairSolver.solve(points, new Metrics("ClosestPair"));
        }
        Metrics last = null;
        for (int trial = 1; trial <= TRIALS; trial++) {
            Metrics m = new Metrics("ClosestPair");
            m.setInputType("RANDOM_POINTS");
            m.setInputSize(points.length);
            m.startTimer();
            ClosestPairSolver.solve(points, m);
            m.stopTimer();
            rows.add(m.toCsvRow(trial));
            last = m;
        }
        System.out.println(last);

        // O(n^2) reference for the small sizes only (used in the report for comparison).
        if (points.length <= 2_000) {
            Metrics m = new Metrics("ClosestPairBruteForce");
            m.setInputType("RANDOM_POINTS");
            m.setInputSize(points.length);
            m.startTimer();
            ClosestPairSolver.bruteForce(points, m);
            m.stopTimer();
            rows.add(m.toCsvRow(1));
            System.out.println(m);
        }
    }
}
