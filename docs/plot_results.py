
import csv
import os
import statistics
import sys
from collections import defaultdict

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
CSV = sys.argv[1] if len(sys.argv) > 1 else os.path.join(ROOT, "results", "results.csv")
PLOTS = os.path.join(ROOT, "docs", "plots")
SUMMARY = os.path.join(ROOT, "results", "summary.md")
README = os.path.join(ROOT, "README.md")


def load(path):
    """(algorithm, input_type, n) -> dict of median metrics."""
    if not os.path.exists(path):
        sys.exit("No CSV at %s — run `mvn exec:java -Dexec.args=\"bench\"` first." % path)
    raw = defaultdict(list)
    with open(path, newline="", encoding="utf-8") as f:
        for row in csv.DictReader(f):
            key = (row["algorithm"], row["input_type"], int(row["n"]))
            raw[key].append(row)
    agg = {}
    for key, rows in raw.items():
        agg[key] = {
            "time_ms": statistics.median(float(r["time_ms"]) for r in rows),
            "max_depth": max(int(r["max_depth"]) for r in rows),
            "comparisons": statistics.median(int(r["comparisons"]) for r in rows),
            "swaps": statistics.median(int(r["swaps"]) for r in rows),
            "recursive_calls": statistics.median(int(r["recursive_calls"]) for r in rows),
            "trials": len(rows),
        }
    return agg


def series(agg, algorithm, input_type, field):
    points = sorted((n, v[field]) for (a, t, n), v in agg.items()
                    if a == algorithm and t == input_type)
    return [p[0] for p in points], [p[1] for p in points]


def plot_time_vs_n(agg):
    plt.figure(figsize=(9, 6))
    combos = [
        ("MergeSort", "RANDOM", "o-"),
        ("QuickSort", "RANDOM", "s-"),
        ("Arrays.sort", "RANDOM", "^-"),
        ("DeterministicSelect", "RANDOM", "d-"),
        ("ClosestPair", "RANDOM_POINTS", "v-"),
    ]
    for algorithm, input_type, style in combos:
        xs, ys = series(agg, algorithm, input_type, "time_ms")
        if xs:
            plt.plot(xs, ys, style, label=algorithm)

    xs, ys = series(agg, "MergeSort", "RANDOM", "time_ms")
    if xs:
        scale = ys[-1] / (xs[-1] * (xs[-1].bit_length()))
        plt.plot(xs, [scale * x * x.bit_length() for x in xs], "k--",
                 alpha=0.5, label="reference c·n·log n")

    plt.xscale("log")
    plt.yscale("log")
    plt.xlabel("input size n")
    plt.ylabel("median time, ms")
    plt.title("Execution time vs n (random input, log-log)")
    plt.grid(True, which="both", alpha=0.3)
    plt.legend()
    plt.tight_layout()
    plt.savefig(os.path.join(PLOTS, "time_vs_n.png"), dpi=150)
    plt.close()


def plot_depth_vs_n(agg):
    plt.figure(figsize=(9, 6))
    combos = [
        ("MergeSort", "RANDOM", "o-"),
        ("QuickSort", "RANDOM", "s-"),
        ("QuickSort", "DUPLICATE_HEAVY", "s--"),
        ("DeterministicSelect", "RANDOM", "d-"),
        ("ClosestPair", "RANDOM_POINTS", "v-"),
    ]
    for algorithm, input_type, style in combos:
        xs, ys = series(agg, algorithm, input_type, "max_depth")
        if xs:
            label = algorithm if input_type in ("RANDOM", "RANDOM_POINTS") \
                else "%s (%s)" % (algorithm, input_type.lower())
            plt.plot(xs, ys, style, label=label)

    xs, _ = series(agg, "MergeSort", "RANDOM", "max_depth")
    if xs:
        plt.plot(xs, [x.bit_length() for x in xs], "k--", alpha=0.5, label="log2(n)")

    plt.xscale("log")
    plt.xlabel("input size n")
    plt.ylabel("maximum recursion depth")
    plt.title("Recursion depth vs n")
    plt.grid(True, which="both", alpha=0.3)
    plt.legend()
    plt.tight_layout()
    plt.savefig(os.path.join(PLOTS, "depth_vs_n.png"), dpi=150)
    plt.close()


def plot_input_types(agg):
    types = ["RANDOM", "SORTED", "REVERSE_SORTED", "DUPLICATE_HEAVY"]
    plt.figure(figsize=(9, 6))
    for algorithm, marker in (("MergeSort", "o-"), ("QuickSort", "s--")):
        for input_type in types:
            xs, ys = series(agg, algorithm, input_type, "time_ms")
            if xs:
                plt.plot(xs, ys, marker, alpha=0.8,
                         label="%s / %s" % (algorithm, input_type.lower()))
    plt.xscale("log")
    plt.yscale("log")
    plt.xlabel("input size n")
    plt.ylabel("median time, ms")
    plt.title("Effect of input structure on sorting time")
    plt.grid(True, which="both", alpha=0.3)
    plt.legend(fontsize=8)
    plt.tight_layout()
    plt.savefig(os.path.join(PLOTS, "time_by_input_type.png"), dpi=150)
    plt.close()


def plot_closest_pair(agg):
    xs1, ys1 = series(agg, "ClosestPair", "RANDOM_POINTS", "time_ms")
    xs2, ys2 = series(agg, "ClosestPairBruteForce", "RANDOM_POINTS", "time_ms")
    if not xs1:
        return
    plt.figure(figsize=(9, 6))
    plt.plot(xs1, ys1, "o-", label="divide and conquer, Θ(n log n)")
    if xs2:
        plt.plot(xs2, ys2, "s-", label="brute force, Θ(n²)")
        scale = ys2[-1] / (xs2[-1] ** 2)
        big = xs1
        plt.plot(big, [scale * x * x for x in big], "k--", alpha=0.5,
                 label="extrapolated Θ(n²)")
    plt.xscale("log")
    plt.yscale("log")
    plt.xlabel("number of points n")
    plt.ylabel("median time, ms")
    plt.title("Closest pair: divide and conquer vs brute force")
    plt.grid(True, which="both", alpha=0.3)
    plt.legend()
    plt.tight_layout()
    plt.savefig(os.path.join(PLOTS, "closest_pair_vs_bruteforce.png"), dpi=150)
    plt.close()


def table(agg, algorithms, input_types, title):
    sizes = sorted({n for (a, t, n) in agg if a in algorithms and t in input_types})
    lines = ["#### %s" % title, "",
             "| algorithm | input type | " + " | ".join("n=%d" % n for n in sizes) + " |",
             "|---|---|" + "---|" * len(sizes)]
    for algorithm in algorithms:
        for input_type in input_types:
            cells = []
            present = False
            for n in sizes:
                v = agg.get((algorithm, input_type, n))
                if v:
                    present = True
                    cells.append("%.2f" % v["time_ms"])
                else:
                    cells.append("—")
            if present:
                lines.append("| %s | %s | %s |" % (algorithm, input_type.lower(), " | ".join(cells)))
    lines.append("")
    return lines


def depth_table(agg):
    sizes = sorted({n for (_, _, n) in agg})
    lines = ["#### Maximum recursion depth", "",
             "| algorithm | input type | " + " | ".join("n=%d" % n for n in sizes) + " |",
             "|---|---|" + "---|" * len(sizes)]
    for algorithm in ("MergeSort", "QuickSort", "DeterministicSelect", "ClosestPair"):
        for input_type in ("RANDOM", "SORTED", "REVERSE_SORTED", "DUPLICATE_HEAVY", "RANDOM_POINTS"):
            cells, present = [], False
            for n in sizes:
                v = agg.get((algorithm, input_type, n))
                if v:
                    present = True
                    cells.append(str(v["max_depth"]))
                else:
                    cells.append("—")
            if present:
                lines.append("| %s | %s | %s |" % (algorithm, input_type.lower(), " | ".join(cells)))
    lines.append("")
    return lines


def ratio_table(agg):
    lines = ["#### Scaling check: time(2n) / time(n) (Θ(n log n) predicts ≈ 2.1)", "",
             "| algorithm | ratio 10k→100k (predicted 11.7) | ratio 100k→200k (predicted 2.1) |",
             "|---|---|---|"]
    for algorithm, input_type in (("MergeSort", "RANDOM"), ("QuickSort", "RANDOM"),
                                  ("Arrays.sort", "RANDOM"), ("DeterministicSelect", "RANDOM")):
        def ms(n):
            v = agg.get((algorithm, input_type, n))
            return v["time_ms"] if v else None
        a, b, c = ms(10_000), ms(100_000), ms(200_000)
        r1 = "%.2f" % (b / a) if a and b and a > 0 else "—"
        r2 = "%.2f" % (c / b) if b and c and b > 0 else "—"
        lines.append("| %s | %s | %s |" % (algorithm, r1, r2))
    lines.append("")
    return lines


def main():
    os.makedirs(PLOTS, exist_ok=True)
    agg = load(CSV)

    plot_time_vs_n(agg)
    plot_depth_vs_n(agg)
    plot_input_types(agg)
    plot_closest_pair(agg)

    block = ["### Execution time (median of 5 trials, ms)", ""]
    block += table(agg, ["MergeSort", "QuickSort", "Arrays.sort"],
                   ["RANDOM", "SORTED", "REVERSE_SORTED", "DUPLICATE_HEAVY"],
                   "Sorting")
    block += table(agg, ["DeterministicSelect"],
                   ["RANDOM", "SORTED", "REVERSE_SORTED", "DUPLICATE_HEAVY"],
                   "Deterministic Select (k = n/2)")
    block += table(agg, ["ClosestPair", "ClosestPairBruteForce"], ["RANDOM_POINTS"],
                   "Closest Pair")
    block += depth_table(agg)
    block += ratio_table(agg)
    text = "\n".join(block)

    with open(SUMMARY, "w", encoding="utf-8") as f:
        f.write(text + "\n")
    print("wrote", SUMMARY)

    if os.path.exists(README):
        with open(README, encoding="utf-8") as f:
            readme = f.read()
        start, end = "<!-- RESULTS:START -->", "<!-- RESULTS:END -->"
        if start in readme and end in readme:
            head = readme.split(start)[0]
            tail = readme.split(end)[1]
            readme = head + start + "\n" + text + "\n" + end + tail
            with open(README, "w", encoding="utf-8") as f:
                f.write(readme)
            print("updated tables in", README)

    print("wrote plots to", PLOTS)


if __name__ == "__main__":
    main()
