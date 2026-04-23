"""
ChartGenerator.py
=================
Reads the benchmark results CSV produced by BenchmarkHarness and generates
publication-quality PNG graphs for inclusion in the final year project report.

Requirements:
    pip install matplotlib pandas numpy

Run from the project root:
    python ChartGenerator.py

Outputs (saved to D:\\ExactTimeSearchModel\\charts\\):
    1. mean_times_present.png  - Mean execution time vs n (target present), all algorithms
    2. mean_times_absent.png   - Mean execution time vs n (target absent), all algorithms
    3. loglog_present.png      - Log-log plot for present target (reveals complexity class)
    4. stddev_comparison.png   - Standard deviation comparison (timing stability)
    5. linear_regression.png   - Empirical data + fitted regression lines
"""

import os
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import matplotlib.ticker as ticker

# ── Configuration ─────────────────────────────────────────────────────────────
CSV_PATH    = r"D:\ExactTimeSearchModel\results\results.csv"
OUTPUT_DIR  = r"D:\ExactTimeSearchModel\charts"

ALGORITHMS  = ["Linear", "Binary", "Jump", "Interpolation"]
COLOURS     = {"Linear": "#e63946", "Binary": "#2a9d8f",
               "Jump":   "#f4a261", "Interpolation": "#457b9d"}
MARKERS     = {"Linear": "o", "Binary": "s", "Jump": "^", "Interpolation": "D"}

NS_TO_US    = 1_000   # divide ns by this to get microseconds for readability

plt.rcParams.update({
    "font.family":  "serif",
    "font.size":    11,
    "axes.titlesize": 13,
    "axes.labelsize": 12,
    "legend.fontsize": 10,
    "figure.dpi":   150,
})

os.makedirs(OUTPUT_DIR, exist_ok=True)

# ── Load data ─────────────────────────────────────────────────────────────────
df = pd.read_csv(CSV_PATH)
df.columns = df.columns.str.strip()

sizes = sorted(df["DatasetSize"].unique())

def get(algo, target_type, col):
    sub = df[(df["Algorithm"] == algo) & (df["TargetType"] == target_type)]
    sub = sub.sort_values("DatasetSize")
    return sub["DatasetSize"].values, sub[col].values


# ── Figure 1: Mean execution time — target PRESENT ───────────────────────────
fig, ax = plt.subplots(figsize=(8, 5))
for algo in ALGORITHMS:
    ns, means = get(algo, "present", "MeanNs")
    ax.plot(ns, means / NS_TO_US, label=algo,
            color=COLOURS[algo], marker=MARKERS[algo], linewidth=1.8, markersize=6)

ax.set_xlabel("Dataset size (n)")
ax.set_ylabel("Mean execution time (μs)")
ax.set_title("Mean Execution Time vs Dataset Size — Target Present")
ax.legend(title="Algorithm")
ax.xaxis.set_major_formatter(ticker.FuncFormatter(lambda x, _: f"{int(x):,}"))
ax.grid(True, linestyle="--", alpha=0.5)
plt.tight_layout()
plt.savefig(os.path.join(OUTPUT_DIR, "mean_times_present.png"))
plt.close()
print("Saved: mean_times_present.png")


# ── Figure 2: Mean execution time — target ABSENT ────────────────────────────
fig, ax = plt.subplots(figsize=(8, 5))
for algo in ALGORITHMS:
    ns, means = get(algo, "absent", "MeanNs")
    ax.plot(ns, means / NS_TO_US, label=algo,
            color=COLOURS[algo], marker=MARKERS[algo], linewidth=1.8, markersize=6)

ax.set_xlabel("Dataset size (n)")
ax.set_ylabel("Mean execution time (μs)")
ax.set_title("Mean Execution Time vs Dataset Size — Target Absent")
ax.legend(title="Algorithm")
ax.xaxis.set_major_formatter(ticker.FuncFormatter(lambda x, _: f"{int(x):,}"))
ax.grid(True, linestyle="--", alpha=0.5)
plt.tight_layout()
plt.savefig(os.path.join(OUTPUT_DIR, "mean_times_absent.png"))
plt.close()
print("Saved: mean_times_absent.png")


# ── Figure 3: Log-log plot — reveals complexity class ────────────────────────
fig, ax = plt.subplots(figsize=(8, 5))
for algo in ALGORITHMS:
    ns, means = get(algo, "present", "MeanNs")
    ax.loglog(ns, means, label=algo,
              color=COLOURS[algo], marker=MARKERS[algo], linewidth=1.8, markersize=6)

ax.set_xlabel("Dataset size (n)  [log scale]")
ax.set_ylabel("Mean execution time (ns)  [log scale]")
ax.set_title("Log-Log Plot — Empirical Complexity (Target Present)")
ax.legend(title="Algorithm")
ax.grid(True, which="both", linestyle="--", alpha=0.4)
plt.tight_layout()
plt.savefig(os.path.join(OUTPUT_DIR, "loglog_present.png"))
plt.close()
print("Saved: loglog_present.png")


# ── Figure 4: Standard deviation — timing stability ──────────────────────────
fig, ax = plt.subplots(figsize=(8, 5))
for algo in ALGORITHMS:
    ns, sds = get(algo, "present", "StdDevNs")
    ax.semilogy(ns, sds, label=algo,
                color=COLOURS[algo], marker=MARKERS[algo], linewidth=1.8, markersize=6)

ax.set_xlabel("Dataset size (n)")
ax.set_ylabel("Standard deviation (ns)  [log scale]")
ax.set_title("Timing Stability — Standard Deviation vs Dataset Size")
ax.legend(title="Algorithm")
ax.xaxis.set_major_formatter(ticker.FuncFormatter(lambda x, _: f"{int(x):,}"))
ax.grid(True, which="both", linestyle="--", alpha=0.4)
plt.tight_layout()
plt.savefig(os.path.join(OUTPUT_DIR, "stddev_comparison.png"))
plt.close()
print("Saved: stddev_comparison.png")


# ── Figure 5: Regression lines overlaid on empirical data ────────────────────
def fit_linear(x, y):
    """OLS regression: returns (a, b) for y = a*x + b."""
    A = np.vstack([x, np.ones_like(x)]).T
    return np.linalg.lstsq(A, y, rcond=None)[0]

transforms = {
    "Linear":        lambda n: n,
    "Binary":        lambda n: np.log2(n),
    "Jump":          lambda n: np.sqrt(n),
    "Interpolation": lambda n: np.log2(np.log2(np.clip(n, 2, None))),
}
model_labels = {
    "Linear":        "T = a·n + b",
    "Binary":        "T = a·log₂(n) + b",
    "Jump":          "T = a·√n + b",
    "Interpolation": "T = a·log₂(log₂(n)) + b",
}

fig, axes = plt.subplots(2, 2, figsize=(12, 8))
axes = axes.flatten()

for idx, algo in enumerate(ALGORITHMS):
    ax  = axes[idx]
    ns, means = get(algo, "present", "MeanNs")
    xs  = transforms[algo](ns.astype(float))
    a, b = fit_linear(xs, means)
    r2  = 1 - np.sum((means - (a * xs + b))**2) / np.sum((means - means.mean())**2)

    n_fit  = np.linspace(ns.min(), ns.max(), 300)
    x_fit  = transforms[algo](n_fit)
    t_fit  = a * x_fit + b

    ax.scatter(ns, means / NS_TO_US, color=COLOURS[algo], zorder=5,
               label="Empirical", s=50)
    ax.plot(n_fit, t_fit / NS_TO_US, color=COLOURS[algo], linestyle="--",
            linewidth=1.8, label=f"Fit (R²={r2:.3f})")

    ax.set_title(f"{algo} Search\n{model_labels[algo]}", fontsize=11)
    ax.set_xlabel("n")
    ax.set_ylabel("Mean time (μs)")
    ax.legend(fontsize=9)
    ax.xaxis.set_major_formatter(ticker.FuncFormatter(lambda x, _: f"{int(x):,}"))
    ax.grid(True, linestyle="--", alpha=0.4)

plt.suptitle("Exact-Time Regression Models — Target Present", fontsize=13, y=1.01)
plt.tight_layout()
plt.savefig(os.path.join(OUTPUT_DIR, "linear_regression.png"), bbox_inches="tight")
plt.close()
print("Saved: linear_regression.png")

print(f"\nAll charts saved to: {OUTPUT_DIR}")
