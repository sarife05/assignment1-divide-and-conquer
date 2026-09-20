package daa;

/**
 * Collects performance counters for a single algorithm run:
 * execution time, maximum recursion depth and algorithmic operations
 * (comparisons, swaps/moves, array allocations, recursive calls).
 *
 * One instance = one measured run. Call {@link #reset()} to reuse it.
 */
public final class Metrics {

    private final String algorithm;
    private String inputType = "n/a";
    private int inputSize;

    private long comparisons;
    private long swaps;          // element swaps / moves
    private long allocations;    // number of allocated array cells
    private long recursiveCalls;

    private int currentDepth;
    private int maxDepth;

    private long startNs;
    private long elapsedNs;

    public Metrics(String algorithm) {
        this.algorithm = algorithm;
    }

    /** Clears all counters but keeps the algorithm name. */
    public void reset() {
        inputType = "n/a";
        inputSize = 0;
        comparisons = 0;
        swaps = 0;
        allocations = 0;
        recursiveCalls = 0;
        currentDepth = 0;
        maxDepth = 0;
        startNs = 0;
        elapsedNs = 0;
    }

    // ---------- timing ----------

    public void startTimer() {
        startNs = System.nanoTime();
    }

    public void stopTimer() {
        elapsedNs = System.nanoTime() - startNs;
    }

    public long elapsedNanos() {
        return elapsedNs;
    }

    public double elapsedMillis() {
        return elapsedNs / 1_000_000.0;
    }

    // ---------- recursion depth ----------

    /** Must be called at the very beginning of every recursive method. */
    public void enterRecursion() {
        recursiveCalls++;
        currentDepth++;
        if (currentDepth > maxDepth) {
            maxDepth = currentDepth;
        }
    }

    /** Must be called in a {@code finally} block of every recursive method. */
    public void exitRecursion() {
        currentDepth--;
    }

    public int maxDepth() {
        return maxDepth;
    }

    public long recursiveCalls() {
        return recursiveCalls;
    }

    // ---------- operation counters ----------

    public void addComparison() {
        comparisons++;
    }

    public void addComparisons(long count) {
        comparisons += count;
    }

    public void addSwap() {
        swaps++;
    }

    public void addSwaps(long count) {
        swaps += count;
    }

    /** Registers an allocation of {@code cells} array cells. */
    public void addAllocation(long cells) {
        allocations += cells;
    }

    public long comparisons() {
        return comparisons;
    }

    public long swaps() {
        return swaps;
    }

    public long allocations() {
        return allocations;
    }

    // ---------- run description ----------

    public String algorithm() {
        return algorithm;
    }

    public String inputType() {
        return inputType;
    }

    public void setInputType(String inputType) {
        this.inputType = inputType;
    }

    public int inputSize() {
        return inputSize;
    }

    public void setInputSize(int inputSize) {
        this.inputSize = inputSize;
    }

    /** CSV header matching {@link #toCsvRow(int)}. */
    public static String csvHeader() {
        return "algorithm,input_type,n,trial,time_ns,time_ms,max_depth,"
                + "comparisons,swaps,allocations,recursive_calls";
    }

    public String toCsvRow(int trial) {
        return algorithm + "," + inputType + "," + inputSize + "," + trial + ","
                + elapsedNs + "," + String.format(java.util.Locale.ROOT, "%.4f", elapsedMillis()) + ","
                + maxDepth + "," + comparisons + "," + swaps + "," + allocations + ","
                + recursiveCalls;
    }

    @Override
    public String toString() {
        return String.format(java.util.Locale.ROOT,
                "%-22s n=%-8d type=%-14s time=%9.3f ms  depth=%-4d comparisons=%-12d swaps=%-12d calls=%d",
                algorithm, inputSize, inputType, elapsedMillis(), maxDepth,
                comparisons, swaps, recursiveCalls);
    }
}
