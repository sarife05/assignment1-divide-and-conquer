package daa;

import java.util.Comparator;
import java.util.Locale;

/** Immutable 2-D point. */
public final class Point {

    public static final Comparator<Point> BY_X =
            Comparator.comparingDouble((Point p) -> p.x).thenComparingDouble(p -> p.y);

    public static final Comparator<Point> BY_Y =
            Comparator.comparingDouble((Point p) -> p.y).thenComparingDouble(p -> p.x);

    public final double x;
    public final double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double distanceTo(Point other) {
        double dx = x - other.x;
        double dy = y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Point)) {
            return false;
        }
        Point p = (Point) o;
        return Double.compare(x, p.x) == 0 && Double.compare(y, p.y) == 0;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(x) * 31 + Double.hashCode(y);
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "(%.3f, %.3f)", x, y);
    }
}
