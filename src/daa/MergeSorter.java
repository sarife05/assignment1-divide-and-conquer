package daa;

/**
 * Top-down MergeSort.
 *
 * Design decisions required by the assignment:
 *  - linear merge of two sorted halves;
 *  - ONE reusable auxiliary buffer allocated once per sort() call
 *    (not per recursive call), so extra memory is exactly Theta(n);
 *  - small-input cutoff: sub-arrays of length <= CUTOFF are sorted
 *    with insertion sort, which removes the deepest (and most
 *    expensive) levels of the recursion tree;
 *  - a[mid] <= a[mid+1] fast path: if the halves are already in order
 *    the merge is skipped (makes sorted input nearly linear).
 *
 * Recurrence: T(n) = 2T(n/2) + Theta(n)  =>  Theta(n log n).
 */
public final class MergeSorter {

    /** Sub-arrays of this size or smaller are sorted by insertion sort. */
    public static final int CUTOFF = 16;

    private MergeSorter() {
    }

    public static void sort(int[] a) {
        sort(a, new Metrics("MergeSort"));
    }

    public static void sort(int[] a, Metrics m) {
        if (a == null || a.length < 2) {
            return;
        }
        int[] buffer = new int[a.length];   // reusable auxiliary buffer
        m.addAllocation(a.length);
        sort(a, buffer, 0, a.length - 1, m);
    }

    private static void sort(int[] a, int[] buffer, int lo, int hi, Metrics m) {
        m.enterRecursion();
        try {
            if (hi - lo + 1 <= CUTOFF) {
                insertionSort(a, lo, hi, m);
                return;
            }
            int mid = lo + (hi - lo) / 2;
            sort(a, buffer, lo, mid, m);
            sort(a, buffer, mid + 1, hi, m);

            m.addComparison();
            if (a[mid] <= a[mid + 1]) {
                return;                      // already sorted, nothing to merge
            }
            merge(a, buffer, lo, mid, hi, m);
        } finally {
            m.exitRecursion();
        }
    }

    /** Linear merge of a[lo..mid] and a[mid+1..hi] through the shared buffer. */
    private static void merge(int[] a, int[] buffer, int lo, int mid, int hi, Metrics m) {
        System.arraycopy(a, lo, buffer, lo, hi - lo + 1);
        m.addSwaps(hi - lo + 1);

        int i = lo;
        int j = mid + 1;
        for (int k = lo; k <= hi; k++) {
            if (i > mid) {
                a[k] = buffer[j++];
            } else if (j > hi) {
                a[k] = buffer[i++];
            } else {
                m.addComparison();
                if (buffer[j] < buffer[i]) {
                    a[k] = buffer[j++];
                } else {
                    a[k] = buffer[i++];
                }
            }
            m.addSwaps(1);
        }
    }

    static void insertionSort(int[] a, int lo, int hi, Metrics m) {
        for (int i = lo + 1; i <= hi; i++) {
            int key = a[i];
            int j = i - 1;
            while (j >= lo) {
                m.addComparison();
                if (a[j] <= key) {
                    break;
                }
                a[j + 1] = a[j];
                m.addSwap();
                j--;
            }
            a[j + 1] = key;
        }
    }
}
