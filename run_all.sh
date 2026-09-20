#!/usr/bin/env bash
# One command that reproduces everything: tests -> benchmarks -> plots.
set -e

echo "==> 1/4 unit tests"
mvn -q test

echo "==> 2/4 demo run"
mvn -q exec:java -Dexec.args="demo" | tee docs/demo-output.txt

echo "==> 3/4 benchmarks (this takes a few minutes)"
mvn -q exec:java -Dexec.args="all results/results.csv" | tee docs/bench-output.txt

echo "==> 4/4 plots and summary tables"
python3 docs/plot_results.py

echo "Done. See results/results.csv, results/summary.md and docs/plots/*.png"
