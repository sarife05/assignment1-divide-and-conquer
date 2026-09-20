package daa;

import java.util.Random;

/**
 * Randomized QuickSort with bounded stack depth.
 *
 * Design decisions required by the assignment:
 *  - randomized pivot: the adversary cannot construct a bad input,
 *    the expected running time is Theta(n log n) for ANY input;
 *  - in-place Hoare partitioning: no extra arrays, and equal keys are
 *    spread over both sides, so duplicate-heavy input does not degrade;
 *  - "recurse into the smaller part, iterate over the larger one":
 *    every real recursive call at least halves the sub-array, therefore
 *    the stack depth is O(log n) even in the worst case;
 *  - insertion-sort cutoff for tiny sub-arrays.
 *
 * Expected recurrence: T(n) = T(k) + T(n-1-k) + Theta(n) with random k,
 * which solves to Theta(n log n); the worst case is still O(n^2) but has
 * probability ~1/n! over the random pivot choices.
 */
public final class QuickSorter {

    public static final int CUTOFF = 16;

    private QuickSorter() {
    }

    public static void sort(int[] a) {
        sort(a, new Metrics("QuickSort"), new Random());
    }

    public static void sort(int[] a, Metrics m) {
        sort(a, m, new Random());
    }

    /** Deterministic variant for reproducible experiments/tests. */
    public static void sort(int[] a, Metrics m, Random random) {
        if (a == null || a.length < 2) {
            return;
        }
        quickSort(a, 0, a.length - 1, m, random);
    }

    private static void quickSort(int[] a, int lo, int hi, Metrics m, Random random) {
        m.enterRecursion();
        try {
            while (lo < hi) {
                if (hi - lo + 1 <= CUTOFF) {
                    MergeSorter.insertionSort(a, lo, hi, m);
                    return;
                }
                int p = partition(a, lo, hi, m, random);   // a[lo..p] <= a[p+1..hi]

                int leftSize = p - lo + 1;
                int rightSize = hi - p;
                if (leftSize < rightSize) {
                    quickSort(a, lo, p, m, random);        // recurse into smaller
                    lo = p + 1;                            // iterate over larger
                } else {
                    quickSort(a, p + 1, hi, m, random);
                    hi = p;
                }
            }
        } finally {
            m.exitRecursion();
        }
    }

    /**
     * Hoare partition around a randomly chosen pivot value.
     * Returns j such that every element of a[lo..j] is <= every element of a[j+1..hi].
     */
    private static int partition(int[] a, int lo, int hi, Metrics m, Random random) {
        int pivot = a[lo + random.nextInt(hi - lo + 1)];
        int i = lo - 1;
        int j = hi + 1;
        while (true) {
            do {
                i++;
                m.addComparison();
            } while (a[i] < pivot);
            do {
                j--;
                m.addComparison();
            } while (a[j] > pivot);
            if (i >= j) {
                return j;
            }
            swap(a, i, j, m);
        }
    }

    private static void swap(int[] a, int i, int j, Metrics m) {
        int t = a[i];
        a[i] = a[j];
        a[j] = t;
        m.addSwap();
    }
}
