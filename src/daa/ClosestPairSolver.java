package daa;

import java.util.Arrays;
import java.util.Locale;

/**
 * Closest pair of points in the plane, O(n log n) divide and conquer.
 *
 * Scheme:
 *  1. sort all points by x once (Theta(n log n));
 *  2. split at the median x, solve both halves recursively;
 *  3. merge the halves by y (as in MergeSort), so every recursive call
 *     leaves its range sorted by y at no extra asymptotic cost;
 *  4. build the strip |x - xMid| < d and scan it in y-order; for each
 *     point only the next few neighbours with (y_j - y_i) < d are checked
 *     (at most 7 by the classic packing argument), so the strip costs O(n).
 *
 * Recurrence: T(n) = 2T(n/2) + Theta(n)  =>  Theta(n log n)
 * (Master Theorem, case 2: a = 2, b = 2, f(n) = Theta(n) = Theta(n^log_2 2)).
 */
public final class ClosestPairSolver {

    private ClosestPairSolver() {
    }

    /** Result of a closest-pair query. */
    public static final class Result {
        public final Point a;
        public final Point b;
        public final double distance;

        Result(Point a, Point b, double distance) {
            this.a = a;
            this.b = b;
            this.distance = distance;
        }

        @Override
        public String toString() {
            return String.format(Locale.ROOT, "%s - %s : d = %.6f", a, b, distance);
        }
    }

    /** Mutable holder of the best pair found so far. */
    private static final class Best {
        Point a;
        Point b;
        double d = Double.POSITIVE_INFINITY;

        double consider(Point p, Point q, Metrics m) {
            double dist = p.distanceTo(q);
            m.addComparison();
            if (dist < d) {
                d = dist;
                a = p;
                b = q;
            }
            return dist;
        }
    }

    public static Result solve(Point[] points) {
        return solve(points, new Metrics("ClosestPair"));
    }

    public static Result solve(Point[] points, Metrics m) {
        require(points);
        Point[] byX = points.clone();
        Point[] buffer = new Point[byX.length];
        m.addAllocation(2L * byX.length);
        Arrays.sort(byX, Point.BY_X);

        Best best = new Best();
        closest(byX, 0, byX.length - 1, buffer, best, m);
        return new Result(best.a, best.b, best.d);
    }

    /** O(n^2) reference implementation used by the tests. */
    public static Result bruteForce(Point[] points) {
        return bruteForce(points, new Metrics("ClosestPairBruteForce"));
    }

    public static Result bruteForce(Point[] points, Metrics m) {
        require(points);
        Best best = new Best();
        for (int i = 0; i < points.length; i++) {
            for (int j = i + 1; j < points.length; j++) {
                best.consider(points[i], points[j], m);
            }
        }
        return new Result(best.a, best.b, best.d);
    }

    private static void require(Point[] points) {
        if (points == null || points.length < 2) {
            throw new IllegalArgumentException("at least two points are required");
        }
    }

    /**
     * Solves p[lo..hi] (sorted by x on entry) and leaves p[lo..hi] sorted by y.
     * Returns the smallest distance inside the range.
     */
    private static double closest(Point[] p, int lo, int hi, Point[] buffer, Best best, Metrics m) {
        m.enterRecursion();
        try {
            int n = hi - lo + 1;
            if (n <= 3) {
                double d = Double.POSITIVE_INFINITY;
                for (int i = lo; i <= hi; i++) {
                    for (int j = i + 1; j <= hi; j++) {
                        d = Math.min(d, best.consider(p[i], p[j], m));
                    }
                }
                insertionSortByY(p, lo, hi, m);
                return d;
            }

            int mid = lo + (hi - lo) / 2;
            double midX = p[mid].x;                 // taken before the halves are reordered

            double dLeft = closest(p, lo, mid, buffer, best, m);
            double dRight = closest(p, mid + 1, hi, buffer, best, m);
            double d = Math.min(dLeft, dRight);

            mergeByY(p, buffer, lo, mid, hi, m);    // p[lo..hi] is now sorted by y

            int stripSize = 0;
            for (int i = lo; i <= hi; i++) {
                m.addComparison();
                if (Math.abs(p[i].x - midX) < d) {
                    buffer[stripSize++] = p[i];
                }
            }
            for (int i = 0; i < stripSize; i++) {
                for (int j = i + 1; j < stripSize; j++) {
                    m.addComparison();
                    if (buffer[j].y - buffer[i].y >= d) {
                        break;                      // at most ~7 neighbours are examined
                    }
                    d = Math.min(d, best.consider(buffer[i], buffer[j], m));
                }
            }
            return d;
        } finally {
            m.exitRecursion();
        }
    }

    private static void mergeByY(Point[] p, Point[] buffer, int lo, int mid, int hi, Metrics m) {
        System.arraycopy(p, lo, buffer, lo, hi - lo + 1);
        m.addSwaps(hi - lo + 1);
        int i = lo;
        int j = mid + 1;
        for (int k = lo; k <= hi; k++) {
            if (i > mid) {
                p[k] = buffer[j++];
            } else if (j > hi) {
                p[k] = buffer[i++];
            } else {
                m.addComparison();
                if (buffer[j].y < buffer[i].y) {
                    p[k] = buffer[j++];
                } else {
                    p[k] = buffer[i++];
                }
            }
        }
    }

    private static void insertionSortByY(Point[] p, int lo, int hi, Metrics m) {
        for (int i = lo + 1; i <= hi; i++) {
            Point key = p[i];
            int j = i - 1;
            while (j >= lo) {
                m.addComparison();
                if (p[j].y <= key.y) {
                    break;
                }
                p[j + 1] = p[j];
                m.addSwap();
                j--;
            }
            p[j + 1] = key;
        }
    }
}
