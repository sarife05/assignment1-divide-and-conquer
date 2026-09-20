package daa;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

/**
 * Entry point.
 *
 *   java daa.Main demo              small readable demonstration of all four algorithms
 *   java daa.Main verify            correctness self-check without JUnit
 *   java daa.Main bench [file.csv]  full benchmark suite -> results/results.csv
 *   java daa.Main all               verify + bench
 */
public final class Main {

    public static void main(String[] args) throws IOException {
        String mode = args.length == 0 ? "demo" : args[0].toLowerCase(Locale.ROOT);
        switch (mode) {
            case "demo":
                demo();
                break;
            case "verify":
                verify();
                break;
            case "bench":
                Experiment.run(csvPath(args));
                break;
            case "all":
                verify();
                System.out.println();
                Experiment.run(csvPath(args));
                break;
            default:
                System.out.println("Unknown mode: " + mode);
                System.out.println("Usage: java daa.Main [demo|verify|bench|all] [results.csv]");
        }
    }

    private static Path csvPath(String[] args) {
        return args.length > 1 ? Paths.get(args[1]) : Paths.get("results", "results.csv");
    }

    // ------------------------------------------------------------------ demo

    private static void demo() {
        Random random = new Random(7);
        int[] data = ArrayUtils.generate(ArrayUtils.InputType.RANDOM, 20, random);
        for (int i = 0; i < data.length; i++) {
            data[i] = Math.floorMod(data[i], 100);
        }

        System.out.println("Input                : " + Arrays.toString(data));

        Metrics mergeMetrics = new Metrics("MergeSort");
        mergeMetrics.setInputSize(data.length);
        mergeMetrics.setInputType("RANDOM");
        int[] merged = data.clone();
        mergeMetrics.startTimer();
        MergeSorter.sort(merged, mergeMetrics);
        mergeMetrics.stopTimer();
        System.out.println("MergeSort            : " + Arrays.toString(merged));

        Metrics quickMetrics = new Metrics("QuickSort");
        quickMetrics.setInputSize(data.length);
        quickMetrics.setInputType("RANDOM");
        int[] quicked = data.clone();
        quickMetrics.startTimer();
        QuickSorter.sort(quicked, quickMetrics, random);
        quickMetrics.stopTimer();
        System.out.println("QuickSort            : " + Arrays.toString(quicked));

        int k = data.length / 2;
        Metrics selectMetrics = new Metrics("DeterministicSelect");
        selectMetrics.setInputSize(data.length);
        selectMetrics.setInputType("RANDOM");
        selectMetrics.startTimer();
        int kth = DeterministicSelector.select(data.clone(), k, selectMetrics);
        selectMetrics.stopTimer();
        System.out.println("Select k=" + k + " (0-based) : " + kth
                + "   (reference: " + merged[k] + ")");

        Point[] points = ArrayUtils.randomPoints(12, random);
        Metrics cpMetrics = new Metrics("ClosestPair");
        cpMetrics.setInputSize(points.length);
        cpMetrics.setInputType("RANDOM_POINTS");
        cpMetrics.startTimer();
        ClosestPairSolver.Result fast = ClosestPairSolver.solve(points, cpMetrics);
        cpMetrics.stopTimer();
        ClosestPairSolver.Result slow = ClosestPairSolver.bruteForce(points);
        System.out.println("Closest pair (D&C)   : " + fast);
        System.out.println("Closest pair (brute) : " + slow);

        System.out.println();
        System.out.println("Metrics:");
        System.out.println("  " + mergeMetrics);
        System.out.println("  " + quickMetrics);
        System.out.println("  " + selectMetrics);
        System.out.println("  " + cpMetrics);
    }

    // ---------------------------------------------------------------- verify

    private static void verify() {
        System.out.println("=== Correctness self-check ===");
        Random random = new Random(123);
        boolean ok = true;

        ok &= check("MergeSort / QuickSort vs Arrays.sort (all input families)",
                sortingCheck(random));
        ok &= check("Sorting edge cases (empty, single, two, all-equal)",
                edgeCaseCheck());
        ok &= check("DeterministicSelect: 100 random arrays vs Arrays.sort(a)[k]",
                selectCheck(random));
        ok &= check("ClosestPair vs O(n^2) brute force (n <= 2000)",
                closestPairCheck(random));

        System.out.println();
        System.out.println(ok ? "ALL CHECKS PASSED" : "SOME CHECKS FAILED");
        if (!ok) {
            throw new AssertionError("self-check failed");
        }
    }

    private static boolean check(String name, boolean passed) {
        System.out.printf("[%s] %s%n", passed ? "PASS" : "FAIL", name);
        return passed;
    }

    private static boolean sortingCheck(Random random) {
        for (ArrayUtils.InputType type : ArrayUtils.InputType.values()) {
            for (int n : new int[]{0, 1, 2, 17, 100, 1_000, 10_000}) {
                int[] base = ArrayUtils.generate(type, n, random);
                int[] expected = base.clone();
                Arrays.sort(expected);

                int[] a = base.clone();
                MergeSorter.sort(a, new Metrics("MergeSort"));
                if (!Arrays.equals(a, expected)) {
                    return false;
                }

                int[] b = base.clone();
                QuickSorter.sort(b, new Metrics("QuickSort"), random);
                if (!Arrays.equals(b, expected)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean edgeCaseCheck() {
        int[][] cases = {{}, {42}, {2, 1}, {1, 2}, {7, 7, 7, 7, 7}, {-1, 0, -1, 0}};
        for (int[] c : cases) {
            int[] expected = c.clone();
            Arrays.sort(expected);
            int[] a = c.clone();
            MergeSorter.sort(a);
            int[] b = c.clone();
            QuickSorter.sort(b);
            if (!Arrays.equals(a, expected) || !Arrays.equals(b, expected)) {
                return false;
            }
        }
        return true;
    }

    private static boolean selectCheck(Random random) {
        for (int test = 0; test < 100; test++) {
            int n = 1 + random.nextInt(300);
            int[] a = new int[n];
            for (int i = 0; i < n; i++) {
                a[i] = random.nextInt(50);      // many duplicates on purpose
            }
            int[] sorted = a.clone();
            Arrays.sort(sorted);
            int k = random.nextInt(n);
            int got = DeterministicSelector.select(a.clone(), k, new Metrics("DeterministicSelect"));
            if (got != sorted[k]) {
                return false;
            }
        }
        return true;
    }

    private static boolean closestPairCheck(Random random) {
        for (int test = 0; test < 50; test++) {
            int n = 2 + random.nextInt(60);
            Point[] points = new Point[n];
            for (int i = 0; i < n; i++) {
                points[i] = new Point(random.nextInt(100), random.nextInt(100));
            }
            double fast = ClosestPairSolver.solve(points).distance;
            double slow = ClosestPairSolver.bruteForce(points).distance;
            if (Math.abs(fast - slow) > 1e-9) {
                return false;
            }
        }
        Point[] big = ArrayUtils.randomPoints(2_000, random);
        double fast = ClosestPairSolver.solve(big).distance;
        double slow = ClosestPairSolver.bruteForce(big).distance;
        return Math.abs(fast - slow) <= 1e-9;
    }
}
