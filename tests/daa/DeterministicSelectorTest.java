package daa;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeterministicSelectorTest {

    private static final Random RANDOM = new Random(3);

    /** 100 independent random trials compared against Arrays.sort(a)[k]. */
    @RepeatedTest(100)
    void matchesArraysSortOnRandomInput() {
        int n = 1 + RANDOM.nextInt(500);
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = RANDOM.nextInt(1_000) - 500;
        }
        int[] sorted = a.clone();
        Arrays.sort(sorted);

        int k = RANDOM.nextInt(n);
        int actual = DeterministicSelector.select(a.clone(), k, new Metrics("DeterministicSelect"));

        assertEquals(sorted[k], actual, "n=" + n + ", k=" + k);
    }

    @Test
    void findsMinimumMedianAndMaximum() {
        int n = 10_001;
        int[] a = ArrayUtils.generate(ArrayUtils.InputType.RANDOM, n, RANDOM);
        int[] sorted = a.clone();
        Arrays.sort(sorted);

        assertEquals(sorted[0], DeterministicSelector.select(a.clone(), 0));
        assertEquals(sorted[n / 2], DeterministicSelector.select(a.clone(), n / 2));
        assertEquals(sorted[n - 1], DeterministicSelector.select(a.clone(), n - 1));
    }

    @Test
    void worksOnDuplicateHeavyAndSortedInput() {
        for (ArrayUtils.InputType type : ArrayUtils.InputType.values()) {
            int[] a = ArrayUtils.generate(type, 5_000, RANDOM);
            int[] sorted = a.clone();
            Arrays.sort(sorted);
            for (int k : new int[]{0, 1, 1234, 2500, 4999}) {
                assertEquals(sorted[k], DeterministicSelector.select(a.clone(), k),
                        "type=" + type + ", k=" + k);
            }
        }
    }

    @Test
    void singleElementArray() {
        assertEquals(9, DeterministicSelector.select(new int[]{9}, 0));
    }

    @Test
    void rejectsInvalidArguments() {
        assertThrows(IllegalArgumentException.class,
                () -> DeterministicSelector.select(new int[]{}, 0));
        assertThrows(IndexOutOfBoundsException.class,
                () -> DeterministicSelector.select(new int[]{1, 2, 3}, 3));
        assertThrows(IndexOutOfBoundsException.class,
                () -> DeterministicSelector.select(new int[]{1, 2, 3}, -1));
    }

    /** The median-of-medians recursion must stay shallow (O(log n) frames). */
    @Test
    void recursionDepthIsSmall() {
        int n = 200_000;
        int[] a = ArrayUtils.generate(ArrayUtils.InputType.RANDOM, n, RANDOM);
        Metrics m = new Metrics("DeterministicSelect");
        DeterministicSelector.select(a, n / 2, m);
        assertTrue(m.maxDepth() <= 40, "unexpected depth " + m.maxDepth());
    }

    /** Linear worst case: doubling n must not multiply the comparisons by much more than 2. */
    @Test
    void comparisonCountGrowsLinearly() {
        long c1 = comparisonsFor(100_000);
        long c2 = comparisonsFor(200_000);
        assertTrue(c2 < 3 * c1, "growth is not linear: " + c1 + " -> " + c2);
    }

    private long comparisonsFor(int n) {
        int[] a = ArrayUtils.generate(ArrayUtils.InputType.RANDOM, n, RANDOM);
        Metrics m = new Metrics("DeterministicSelect");
        DeterministicSelector.select(a, n / 2, m);
        return m.comparisons();
    }
}
