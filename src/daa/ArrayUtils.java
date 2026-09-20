package daa;

import java.util.Random;

public final class ArrayUtils {

    public enum InputType {
        RANDOM, SORTED, REVERSE_SORTED, DUPLICATE_HEAVY
    }

    private ArrayUtils() {
    }

    public static int[] generate(InputType type, int n, Random random) {
        int[] a = new int[n];
        switch (type) {
            case RANDOM:
                for (int i = 0; i < n; i++) {
                    a[i] = random.nextInt();
                }
                break;
            case SORTED:
                for (int i = 0; i < n; i++) {
                    a[i] = i;
                }
                break;
            case REVERSE_SORTED:
                for (int i = 0; i < n; i++) {
                    a[i] = n - i;
                }
                break;
            case DUPLICATE_HEAVY:
                int distinct = Math.max(1, n / 1000);
                for (int i = 0; i < n; i++) {
                    a[i] = random.nextInt(distinct);
                }
                break;
            default:
                throw new IllegalArgumentException("unknown type: " + type);
        }
        return a;
    }

    public static Point[] randomPoints(int n, Random random) {
        Point[] points = new Point[n];
        for (int i = 0; i < n; i++) {
            points[i] = new Point(random.nextDouble() * 1_000_000.0,
                    random.nextDouble() * 1_000_000.0);
        }
        return points;
    }

    public static boolean isSorted(int[] a) {
        for (int i = 1; i < a.length; i++) {
            if (a[i - 1] > a[i]) {
                return false;
            }
        }
        return true;
    }
}
