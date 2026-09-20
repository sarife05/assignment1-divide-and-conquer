package daa;

/**
 * Deterministic selection of the k-th order statistic (0-based)
 * using the Median-of-Medians (BFPRT) algorithm.
 *
 * Design decisions required by the assignment:
 *  - groups of 5, each group sorted by insertion sort in place;
 *  - the median of each group is moved to the front of the range, and the
 *    pivot is obtained by a recursive select() on those n/5 medians;
 *  - in-place 3-way (Dutch national flag) partitioning, which also makes
 *    the algorithm immune to duplicate-heavy inputs;
 *  - only ONE side is processed further, and the "tail" is handled by a
 *    loop instead of a recursive call, so the extra stack is only the
 *    median-of-medians recursion.
 *
 * Recurrence: T(n) <= T(n/5) + T(7n/10) + Theta(n).
 * Since 1/5 + 7/10 = 9/10 < 1, the work per level decreases geometrically
 * and the total is Theta(n) in the WORST case (Akra-Bazzi intuition:
 * the sum of the subproblem fractions is < 1, so the root term dominates).
 */
public final class DeterministicSelector {

    private DeterministicSelector() {
    }

    /** Returns the k-th smallest element (k is 0-based). The array is permuted in place. */
    public static int select(int[] a, int k) {
        return select(a, k, new Metrics("DeterministicSelect"));
    }

    public static int select(int[] a, int k, Metrics m) {
        if (a == null || a.length == 0) {
            throw new IllegalArgumentException("array must be non-empty");
        }
        if (k < 0 || k >= a.length) {
            throw new IndexOutOfBoundsException("k=" + k + " out of range for n=" + a.length);
        }
        return select(a, 0, a.length - 1, k, m);
    }

    private static int select(int[] a, int lo, int hi, int k, Metrics m) {
        m.enterRecursion();
        try {
            while (true) {
                if (lo == hi) {
                    return a[lo];
                }
                if (hi - lo + 1 <= 5) {
                    MergeSorter.insertionSort(a, lo, hi, m);
                    return a[k];
                }
                int pivot = medianOfMedians(a, lo, hi, m);

                // a[lo..lt-1] < pivot, a[lt..gt] == pivot, a[gt+1..hi] > pivot
                int[] bounds = partition3(a, lo, hi, pivot, m);
                int lt = bounds[0];
                int gt = bounds[1];

                if (k < lt) {
                    hi = lt - 1;          // iterate instead of recursing
                } else if (k > gt) {
                    lo = gt + 1;
                } else {
                    return a[k];          // k landed inside the pivot block
                }
            }
        } finally {
            m.exitRecursion();
        }
    }

    /** Median of the group medians of a[lo..hi]. */
    private static int medianOfMedians(int[] a, int lo, int hi, Metrics m) {
        int n = hi - lo + 1;
        int groups = (n + 4) / 5;
        for (int g = 0; g < groups; g++) {
            int groupLo = lo + g * 5;
            int groupHi = Math.min(groupLo + 4, hi);
            MergeSorter.insertionSort(a, groupLo, groupHi, m);
            int median = groupLo + (groupHi - groupLo) / 2;
            swap(a, lo + g, median, m);           // collect medians at a[lo..lo+groups-1]
        }
        return select(a, lo, lo + groups - 1, lo + (groups - 1) / 2, m);
    }

    /** In-place 3-way partition; returns {lt, gt}. */
    private static int[] partition3(int[] a, int lo, int hi, int pivot, Metrics m) {
        int lt = lo;
        int i = lo;
        int gt = hi;
        while (i <= gt) {
            m.addComparison();
            if (a[i] < pivot) {
                swap(a, i++, lt++, m);
            } else if (a[i] > pivot) {
                m.addComparison();
                swap(a, i, gt--, m);
            } else {
                m.addComparison();
                i++;
            }
        }
        m.addAllocation(2);
        return new int[]{lt, gt};
    }

    private static void swap(int[] a, int i, int j, Metrics m) {
        if (i == j) {
            return;
        }
        int t = a[i];
        a[i] = a[j];
        a[j] = t;
        m.addSwap();
    }
}
