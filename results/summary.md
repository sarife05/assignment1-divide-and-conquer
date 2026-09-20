### Execution time (median of 5 trials, ms)

#### Sorting

| algorithm | input type | n=1000 | n=5000 | n=10000 | n=50000 | n=100000 | n=200000 |
|---|---|---|---|---|---|---|---|
| MergeSort | random | 0.11 | 0.76 | 0.54 | 3.25 | 6.70 | 14.26 |
| MergeSort | sorted | 0.01 | 0.02 | 0.03 | 0.14 | 0.28 | 0.34 |
| MergeSort | reverse_sorted | 0.02 | 0.09 | 0.19 | 1.16 | 2.43 | 4.85 |
| MergeSort | duplicate_heavy | 0.00 | 0.11 | 0.31 | 2.05 | 4.56 | 10.01 |
| QuickSort | random | 0.38 | 0.42 | 0.88 | 2.75 | 5.85 | 12.43 |
| QuickSort | sorted | 0.01 | 0.06 | 0.12 | 0.72 | 1.47 | 3.10 |
| QuickSort | reverse_sorted | 0.01 | 0.06 | 0.13 | 0.81 | 1.54 | 3.17 |
| QuickSort | duplicate_heavy | 0.01 | 0.10 | 0.26 | 1.56 | 3.45 | 7.90 |
| Arrays.sort | random | 0.12 | 0.29 | 0.65 | 2.96 | 4.63 | 10.03 |
| Arrays.sort | sorted | 0.00 | 0.01 | 0.02 | 0.12 | 0.02 | 0.04 |
| Arrays.sort | reverse_sorted | 0.02 | 0.02 | 0.04 | 0.08 | 0.06 | 0.12 |
| Arrays.sort | duplicate_heavy | 0.01 | 0.20 | 0.13 | 0.88 | 2.06 | 4.75 |

#### Deterministic Select (k = n/2)

| algorithm | input type | n=1000 | n=5000 | n=10000 | n=50000 | n=100000 | n=200000 |
|---|---|---|---|---|---|---|---|
| DeterministicSelect | random | 0.09 | 0.24 | 0.51 | 1.53 | 2.65 | 5.48 |
| DeterministicSelect | sorted | 0.01 | 0.05 | 0.11 | 0.67 | 1.37 | 2.83 |
| DeterministicSelect | reverse_sorted | 0.01 | 0.06 | 0.13 | 0.73 | 1.54 | 2.99 |
| DeterministicSelect | duplicate_heavy | 0.00 | 0.03 | 0.11 | 0.62 | 1.10 | 2.04 |

#### Closest Pair

| algorithm | input type | n=1000 | n=5000 | n=10000 | n=50000 | n=100000 |
|---|---|---|---|---|---|---|
| ClosestPair | random_points | 0.62 | 4.64 | 3.18 | 30.13 | 29.82 |
| ClosestPairBruteForce | random_points | 6.13 | — | — | — | — |

#### Maximum recursion depth

| algorithm | input type | n=1000 | n=5000 | n=10000 | n=50000 | n=100000 | n=200000 |
|---|---|---|---|---|---|---|---|
| MergeSort | random | 7 | 10 | 11 | 13 | 14 | 15 |
| MergeSort | sorted | 7 | 10 | 11 | 13 | 14 | 15 |
| MergeSort | reverse_sorted | 7 | 10 | 11 | 13 | 14 | 15 |
| MergeSort | duplicate_heavy | 7 | 10 | 11 | 13 | 14 | 15 |
| QuickSort | random | 6 | 7 | 8 | 10 | 10 | 11 |
| QuickSort | sorted | 6 | 7 | 8 | 10 | 10 | 11 |
| QuickSort | reverse_sorted | 5 | 7 | 8 | 10 | 10 | 11 |
| QuickSort | duplicate_heavy | 7 | 9 | 9 | 11 | 12 | 12 |
| DeterministicSelect | random | 5 | 6 | 6 | 7 | 8 | 8 |
| DeterministicSelect | sorted | 5 | 6 | 6 | 7 | 8 | 8 |
| DeterministicSelect | reverse_sorted | 5 | 6 | 6 | 7 | 8 | 8 |
| DeterministicSelect | duplicate_heavy | 5 | 6 | 6 | 7 | 8 | 8 |
| ClosestPair | random_points | 10 | 12 | 13 | 16 | 17 | — |

#### Scaling check: time(2n) / time(n) (Θ(n log n) predicts ≈ 2.1)

| algorithm | ratio 10k→100k (predicted 11.7) | ratio 100k→200k (predicted 2.1) |
|---|---|---|
| MergeSort | 12.34 | 2.13 |
| QuickSort | 6.63 | 2.13 |
| Arrays.sort | 7.08 | 2.17 |
| DeterministicSelect | 5.14 | 2.07 |

