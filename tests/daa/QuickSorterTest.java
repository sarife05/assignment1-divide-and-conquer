package daa;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuickSorterTest {

    private final Random random = new Random(2);

    @ParameterizedTest
    @EnumSource(ArrayUtils.InputType.class)
    void sortsEveryInputFamilyLikeArraysSort(ArrayUtils.InputType type) {
        for (int n : new int[]{0, 1, 2, 3, 17, 64, 1_000, 10_000}) {
            int[] input = ArrayUtils.generate(type, n, random);
            int[] expected = input.clone();
            Arrays.sort(expected);

            int[] actual = input.clone();
            QuickSorter.sort(actual, new Metrics("QuickSort"), random);

            assertArrayEquals(expected, actual, "failed for type=" + type + ", n=" + n);
        }
    }

    @Test
    void handlesEmptyAndSingleElementArrays() {
        int[] empty = {};
        assertDoesNotThrow(() -> QuickSorter.sort(empty));

        int[] single = {-7};
        QuickSorter.sort(single);
        assertArrayEquals(new int[]{-7}, single);
    }

    @Test
    void handlesArraysOfEqualElements() {
        int[] a = new int[10_000];
        Arrays.fill(a, 13);
        QuickSorter.sort(a);
        assertTrue(ArrayUtils.isSorted(a));
        assertArrayEquals(new int[]{13, 13}, new int[]{a[0], a[a.length - 1]});
    }

    /**
     * "Recurse into the smaller half" guarantees a stack depth of at most
     * floor(log2(n)) + O(1) regardless of the pivot quality.
     */
    @Test
    void recursionDepthStaysLogarithmicOnAllInputFamilies() {
        int n = 200_000;
        int bound = 2 * (int) (Math.floor(Math.log(n) / Math.log(2))) + 10;
        for (ArrayUtils.InputType type : ArrayUtils.InputType.values()) {
            int[] a = ArrayUtils.generate(type, n, random);
            Metrics m = new Metrics("QuickSort");
            QuickSorter.sort(a, m, random);
            assertTrue(m.maxDepth() <= bound,
                    "type=" + type + " depth=" + m.maxDepth() + " > bound=" + bound);
        }
    }

    @Test
    void isInPlace() {
        int[] a = ArrayUtils.generate(ArrayUtils.InputType.RANDOM, 10_000, random);
        Metrics m = new Metrics("QuickSort");
        QuickSorter.sort(a, m, random);
        assertTrue(m.allocations() == 0, "QuickSort must not allocate auxiliary arrays");
    }
}
