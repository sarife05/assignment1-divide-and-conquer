package daa;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClosestPairSolverTest {

    private static final double EPS = 1e-9;
    private final Random random = new Random(4);

    @Test
    void matchesBruteForceOnManySmallRandomSets() {
        for (int test = 0; test < 200; test++) {
            int n = 2 + random.nextInt(80);
            Point[] points = new Point[n];
            for (int i = 0; i < n; i++) {
                points[i] = new Point(random.nextInt(200) - 100, random.nextInt(200) - 100);
            }
            double fast = ClosestPairSolver.solve(points).distance;
            double slow = ClosestPairSolver.bruteForce(points).distance;
            assertEquals(slow, fast, EPS, "n=" + n);
        }
    }

    @Test
    void matchesBruteForceForN2000() {
        Point[] points = ArrayUtils.randomPoints(2_000, random);
        double fast = ClosestPairSolver.solve(points).distance;
        double slow = ClosestPairSolver.bruteForce(points).distance;
        assertEquals(slow, fast, EPS);
    }

    @Test
    void handlesCoincidentPoints() {
        Point[] points = {
                new Point(1, 1), new Point(5, 5), new Point(1, 1), new Point(9, 2)
        };
        assertEquals(0.0, ClosestPairSolver.solve(points).distance, EPS);
    }

    @Test
    void handlesCollinearPoints() {
        Point[] points = new Point[1_000];
        for (int i = 0; i < points.length; i++) {
            points[i] = new Point(i * 3.0, 0.0);
        }
        assertEquals(3.0, ClosestPairSolver.solve(points).distance, EPS);
    }

    @Test
    void doesNotModifyTheInputArray() {
        Point[] points = ArrayUtils.randomPoints(500, random);
        Point[] copy = points.clone();
        ClosestPairSolver.solve(points);
        for (int i = 0; i < points.length; i++) {
            assertEquals(copy[i], points[i], "input array was reordered at index " + i);
        }
    }

    @Test
    void rejectsTooSmallInput() {
        assertThrows(IllegalArgumentException.class,
                () -> ClosestPairSolver.solve(new Point[]{new Point(0, 0)}));
        assertThrows(IllegalArgumentException.class,
                () -> ClosestPairSolver.solve(new Point[]{}));
    }

    @Test
    void returnsTheActualClosestPair() {
        Point[] points = {
                new Point(0, 0), new Point(100, 100), new Point(10, 10), new Point(10.5, 10.5)
        };
        ClosestPairSolver.Result r = ClosestPairSolver.solve(points);
        double expected = new Point(10, 10).distanceTo(new Point(10.5, 10.5));
        assertEquals(expected, r.distance, EPS);
        assertEquals(expected, r.a.distanceTo(r.b), EPS);
    }

    /** 100k points must finish quickly, which an O(n^2) solution could not. */
    @Test
    void scalesToLargeInput() {
        Point[] points = ArrayUtils.randomPoints(100_000, random);
        Metrics m = new Metrics("ClosestPair");
        m.startTimer();
        ClosestPairSolver.Result r = ClosestPairSolver.solve(points, m);
        m.stopTimer();
        assertTrue(r.distance >= 0);
        assertTrue(m.maxDepth() <= 60, "unexpected depth " + m.maxDepth());
        assertTrue(m.elapsedMillis() < 5_000, "too slow: " + m.elapsedMillis() + " ms");
    }
}
