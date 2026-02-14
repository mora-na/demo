#!/usr/bin/env python3
"""
Project stats: file types, counts, LOC, size, and other high-level metrics.

Usage:
  python3 scripts/project_stats.py
  python3 scripts/project_stats.py --top 30
"""

from __future__ import annotations

import argparse
import os
from collections import Counter, defaultdict
from pathlib import Path
from typing import Dict, Iterable, List, Tuple

# Directories to exclude (relative path segments)
EXCLUDE_DIRS = {
    ".git",
    ".idea",
    ".vscode",
    ".DS_Store",
    "node_modules",
    "target",
    "dist",
    "build",
    "out",
    "logs",
    ".cache",
    ".gradle",
    ".mvn",
    "vendor",
    "coverage",
    "__pycache__",
}

# File patterns to ignore
EXCLUDE_FILES = {
    "package-lock.json",
    "yarn.lock",
    "pnpm-lock.yaml",
    "npm-shrinkwrap.json",
}

# Treat these as text for LOC counting
TEXT_EXTS = {
    ".java", ".kt", ".kts", ".groovy",
    ".xml", ".yml", ".yaml", ".properties",
    ".md", ".txt",
    ".sql",
    ".js", ".ts", ".tsx", ".jsx", ".vue",
    ".css", ".scss", ".sass", ".less",
    ".html", ".htm",
    ".json",
    ".py", ".sh", ".bat", ".ps1",
    ".gradle", ".mvn",
}

# Group extensions into higher-level buckets
EXT_GROUPS = {
    "java": {".java"},
    "kotlin": {".kt", ".kts"},
    "xml": {".xml"},
    "yaml": {".yml", ".yaml"},
    "properties": {".properties"},
    "markdown": {".md"},
    "sql": {".sql"},
    "frontend": {".js", ".ts", ".tsx", ".jsx", ".vue", ".css", ".scss", ".sass", ".less", ".html", ".htm"},
    "json": {".json"},
    "python": {".py"},
    "shell": {".sh", ".bat", ".ps1"},
}


def is_excluded(path: Path) -> bool:
    parts = set(path.parts)
    if parts & EXCLUDE_DIRS:
        return True
    if path.name in EXCLUDE_FILES:
        return True
    return False


def iter_files(root: Path) -> Iterable[Path]:
    for p in root.rglob("*"):
        if p.is_dir():
            # skip excluded dirs early
            if p.name in EXCLUDE_DIRS:
                # Prevent descending into excluded directory
                # by modifying rglob behavior: just skip
                continue
            continue
        if is_excluded(p):
            continue
        yield p


def extension_of(path: Path) -> str:
    if path.name.startswith(".") and path.suffix == "":
        return path.name
    return path.suffix.lower()


def group_for_ext(ext: str) -> str:
    for group, exts in EXT_GROUPS.items():
        if ext in exts:
            return group
    return "other"


def count_loc(path: Path) -> Tuple[int, int, int]:
    """Return (lines, non_empty_lines, bytes) for text files only."""
    try:
        data = path.read_bytes()
    except Exception:
        return (0, 0, 0)
    size = len(data)
    ext = extension_of(path)
    if ext not in TEXT_EXTS:
        return (0, 0, size)
    try:
        text = data.decode("utf-8", errors="ignore")
    except Exception:
        return (0, 0, size)
    lines = text.splitlines()
    non_empty = sum(1 for line in lines if line.strip())
    return (len(lines), non_empty, size)


def human_size(num: int) -> str:
    for unit in ["B", "KB", "MB", "GB", "TB"]:
        if num < 1024:
            return f"{num:.0f}{unit}" if unit == "B" else f"{num:.1f}{unit}"
        num /= 1024.0
    return f"{num:.1f}PB"


def print_table(title: str, rows: List[Tuple[str, int, int, int]], top: int | None = None) -> None:
    if not rows:
        return
    if top:
        rows = rows[:top]
    name_w = max(len(r[0]) for r in rows)
    print(f"\n{title}")
    print("-" * (name_w + 36))
    print(f"{'Type'.ljust(name_w)}  {'Files':>6}  {'LOC':>8}  {'Non-Empty':>10}  {'Size':>8}")
    print("-" * (name_w + 36))
    for name, files, loc, non_empty, size in rows:
        print(f"{name.ljust(name_w)}  {files:>6}  {loc:>8}  {non_empty:>10}  {human_size(size):>8}")


def main() -> int:
    parser = argparse.ArgumentParser(description="Project statistics")
    parser.add_argument("--top", type=int, default=30, help="show top N file types by LOC")
    args = parser.parse_args()

    root = Path.cwd()

    ext_counts = Counter()
    ext_loc = Counter()
    ext_non_empty = Counter()
    ext_size = Counter()

    group_counts = Counter()
    group_loc = Counter()
    group_non_empty = Counter()
    group_size = Counter()

    total_files = 0
    total_loc = 0
    total_non_empty = 0
    total_size = 0

    largest_files: List[Tuple[int, Path]] = []

    for path in iter_files(root):
        total_files += 1
        ext = extension_of(path)
        group = group_for_ext(ext)
        lines, non_empty, size = count_loc(path)

        ext_counts[ext] += 1
        ext_loc[ext] += lines
        ext_non_empty[ext] += non_empty
        ext_size[ext] += size

        group_counts[group] += 1
        group_loc[group] += lines
        group_non_empty[group] += non_empty
        group_size[group] += size

        total_loc += lines
        total_non_empty += non_empty
        total_size += size

        largest_files.append((size, path))

    largest_files.sort(reverse=True, key=lambda x: x[0])
    largest_files = largest_files[:10]

    print("Project Statistics")
    print("==================")
    print(f"Root: {root}")
    print(f"Total files: {total_files}")
    print(f"Total LOC: {total_loc}")
    print(f"Total non-empty LOC: {total_non_empty}")
    print(f"Total size: {human_size(total_size)}")

    # By group
    group_rows = sorted(
        [(k or "(no_ext)", group_counts[k], group_loc[k], group_non_empty[k], group_size[k]) for k in group_counts],
        key=lambda r: r[2],
        reverse=True,
    )
    print_table("By Group", group_rows)

    # By extension
    ext_rows = sorted(
        [(k or "(no_ext)", ext_counts[k], ext_loc[k], ext_non_empty[k], ext_size[k]) for k in ext_counts],
        key=lambda r: r[2],
        reverse=True,
    )
    print_table("By Extension (Top by LOC)", ext_rows, top=args.top)

    print("\nLargest Files")
    print("-------------")
    for size, path in largest_files:
        rel = path.relative_to(root)
        print(f"{human_size(size):>8}  {rel}")

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
