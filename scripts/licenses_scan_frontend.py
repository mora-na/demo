#!/usr/bin/env python3
import json
from collections import Counter
from pathlib import Path


def main() -> None:
    root = Path("demo-ui/node_modules")
    if not root.exists():
        raise SystemExit("demo-ui/node_modules not found. Run npm install first.")

    records = []
    for pkg_json in root.rglob("package.json"):
        try:
            data = json.loads(pkg_json.read_text(encoding="utf-8"))
        except Exception:
            continue
        name = data.get("name")
        version = data.get("version")
        if not name or not version:
            continue
        license_value = data.get("license") or data.get("licenses")
        if isinstance(license_value, list):
            license_value = ", ".join(sorted({str(x) for x in license_value}))
        if license_value is None:
            license_value = "UNKNOWN"
        records.append({"name": name, "version": version, "license": str(license_value)})

    unique = {}
    for rec in records:
        key = f"{rec['name']}@{rec['version']}"
        unique[key] = rec

    summary = Counter(rec["license"] for rec in unique.values())

    out_dir = Path("target/licenses")
    out_dir.mkdir(parents=True, exist_ok=True)

    (out_dir / "frontend-licenses.json").write_text(
        json.dumps(sorted(unique.values(), key=lambda x: (x["name"], x["version"])), ensure_ascii=False, indent=2),
        encoding="utf-8",
    )

    lines = [f"Total packages: {len(unique)}"]
    for lic, cnt in summary.most_common():
        lines.append(f"{lic}: {cnt}")
    unknown = [k for k, v in unique.items() if v["license"] == "UNKNOWN"]
    if unknown:
        lines.append("")
        lines.append("UNKNOWN licenses (sample):")
        for item in unknown[:20]:
            lines.append(item)

    (out_dir / "frontend-licenses-summary.txt").write_text("\n".join(lines), encoding="utf-8")
    print(f"Wrote: {out_dir / 'frontend-licenses.json'}")
    print(f"Wrote: {out_dir / 'frontend-licenses-summary.txt'}")


if __name__ == "__main__":
    main()
