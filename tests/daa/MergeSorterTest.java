package daa;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MergeSorterTest {

    private final Random random = new Random(1);

    @ParameterizedTest
    @EnumSource(ArrayUtils.InputType.class)
    void sortsEveryInputFamilyLikeArraysSort(ArrayUtils.InputType type) {
        for (int n : new int[]{0, 1, 2, 3, 17, 64, 1_000, 10_000}) {
            int[] input = ArrayUtils.generate(type, n, random);
            int[] expected = input.clone();
            Arrays.sort(expected);

            int[] actual = input.clone();
            MergeSorter.sort(actual, new Metrics("MergeSort"));

            assertArrayEquals(expected, actual, "failed for type=" + type + ", n=" + n);
        }
    }

    @Test
    void handlesEmptyAndSingleElementArrays() {
        int[] empty = {};
        assertDoesNotThrow(() -> MergeSorter.sort(empty));
        assertArrayEquals(new int[]{}, empty);

        int[] single = {42};
        MergeSorter.sort(single);
        assertArrayEquals(new int[]{42}, single);
    }

    @Test
    void handlesDuplicatesAndNegativeValues() {
        int[] a = {5, -3, 5, 0, -3, 5, 0, Integer.MIN_VALUE, Integer.MAX_VALUE};
        int[] expected = a.clone();
        Arrays.sort(expected);
        MergeSorter.sort(a);
        assertArrayEquals(expected, a);
    }

    @Test
    void recursionDepthIsLogarithmic() {
        int n = 100_000;
        int[] a = ArrayUtils.generate(ArrayUtils.InputType.RANDOM, n, random);
        Metrics m = new Metrics("MergeSort");
        MergeSorter.sort(a, m);

        int bound = 2 * (int) (Math.log(n) / Math.log(2)) + 4;
        assertTrue(m.maxDepth() <= bound,
                "depth " + m.maxDepth() + " exceeds the expected O(log n) bound " + bound);
    }

    @Test
    void allocatesTheAuxiliaryBufferOnlyOnce() {
        int n = 4_096;
        int[] a = ArrayUtils.generate(ArrayUtils.InputType.RANDOM, n, random);
        Metrics m = new Metrics("MergeSort");
        MergeSorter.sort(a, m);

        assertTrue(m.allocations() == n,
                "expected exactly one buffer of n cells, got " + m.allocations());
    }
}
