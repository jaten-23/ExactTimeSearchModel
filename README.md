
# Analysis of Optimal Search Algorithm Implementations by Exact-Time Modelling

**Author:** Jaten Tushar Chikhale | **SID:** 2314864  
**Supervisor:** Dr. Partha De  
**Institution:** Anglia Ruskin University — BSc (Hons) Computer Science  
**Year:** 2026

---

## Project Overview

This project empirically measures and models the exact execution time of four 
fundamental search algorithms in Java on a real-world dataset of 858,018 UK 
property sale prices from the HM Land Registry Price Paid dataset (2023).

Unlike Big-O notation which only describes theoretical growth, this project 
produces actual predictive equations such as T = a·log₂(n) + b that tell you 
exactly how long each algorithm will take for any dataset size.

---

## Algorithms Implemented

| Algorithm | Complexity | Class |
|---|---|---|
| Linear Search | O(n) | LinearSearch.java |
| Binary Search | O(log n) | BinarySearch.java |
| Jump Search | O(√n) | JumpSearch.java |
| Interpolation Search | O(log log n) | InterpolationSearch.java |

---

## Project Structure

```
ExactTimeSearchModel/
├── src/fyp/
│   ├── LinearSearch.java
│   ├── BinarySearch.java
│   ├── JumpSearch.java
│   ├── InterpolationSearch.java
│   ├── BenchmarkHarness.java
│   ├── DatasetLoader.java
│   ├── Main.java
│   ├── StatisticsAnalyser.java
│   ├── ExactTimeModel.java
│   └── SearchAlgorithmTests.java
├── charts/
├── data/               (not included — see Dataset section)
├── results/
├── ChartGenerator.py
└── .gitignore
```

---

## How to Run

### Prerequisites
- Java Development Kit (JDK) 21+
- Python 3.x with matplotlib, pandas, numpy
- HM Land Registry Price Paid dataset (pp-2023.csv)

### Step 1 — Get the dataset
Download pp-2023.csv from:  
https://www.gov.uk/government/statistical-data-sets/price-paid-data-downloads  
Place it at: `D:\ExactTimeSearchModel\data\pp-2023.csv`

### Step 2 — Compile
```bash
javac -d out src/fyp/*.java
```

### Step 3 — Run benchmark
```bash
java -cp out fyp.Main
```
This runs all 4 algorithms across 7 dataset sizes (1k to 1M),
writes results to `results/results.csv`, and fits regression models.

### Step 4 — Generate charts
```bash
pip install matplotlib pandas numpy
py ChartGenerator.py
```
Saves 5 publication-quality PNG graphs to `charts/`

### Step 5 — Run JUnit tests
Download junit-platform-console-standalone.jar from JUnit GitHub releases, then:
```bash
java -jar junit-platform-console-standalone.jar --scan-classpath --classpath out/
```

---

## Key Results

| Algorithm | n=1,000,000 Present (ns) | n=1,000,000 Absent (ns) | R² |
|---|---|---|---|
| Linear Search | 76,760 | 134,040 | 0.968 |
| Binary Search | 217 | 153 | 0.282 |
| Jump Search | 8,880 | 93 | 0.775 |
| Interpolation Search | 127 | 53 | 0.318 |

---

## New Modules Added

- **StatisticsAnalyser.java** — IQR outlier removal + 95% confidence intervals
- **ExactTimeModel.java** — OLS regression producing exact-time predictive equations
- **SearchAlgorithmTests.java** — JUnit 5 correctness test suite (30+ tests)
- **ChartGenerator.py** — 5 publication-quality benchmark graphs

---

## Dataset

The HM Land Registry Price Paid dataset is not included in this repository 
due to its size (150MB+). It is publicly available under Open Government 
Licence at:  
https://www.gov.uk/government/statistical-data-sets/price-paid-data-downloads

---

## License

This project is submitted as a Final Year Project at Anglia Ruskin University.
All code is original work by Jaten Tushar Chikhale (2314864).
```
