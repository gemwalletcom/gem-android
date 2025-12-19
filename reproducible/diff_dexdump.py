#!/usr/bin/env python3
"""
Generate dexdump diffs for all classes*.dex between two APKs.

Usage:
    ./diff_dexdump.py <official-apk> <rebuilt-apk> [--out-dir DIR] [--dexdump PATH] [--tag TAG]

Outputs (per dex):
    - official_<name>.dump
    - rebuilt_<name>.dump
    - dexdump_<name>.diff (unified diff)

Note: This script does NOT patch map-id; it simply compares the two APKs as-is.
"""

from __future__ import annotations

import argparse
import shutil
import subprocess
import sys
import tempfile
import zipfile
from pathlib import Path


def extract_dex_files(apk_path: Path, dest_dir: Path) -> set[str]:
    dex_names: set[str] = set()
    with zipfile.ZipFile(apk_path) as zf:
        for member in zf.infolist():
            if member.filename.startswith("classes") and member.filename.endswith(".dex"):
                dest = dest_dir / Path(member.filename).name
                dest.parent.mkdir(parents=True, exist_ok=True)
                dest.write_bytes(zf.read(member))
                dex_names.add(dest.name)
    return dex_names


def run_dexdump(dexdump: Path, src: Path, dst: Path) -> None:
    with dst.open("w") as fh:
        subprocess.run([str(dexdump), str(src)], check=True, stdout=fh)


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("official_apk")
    parser.add_argument("rebuilt_apk")
    parser.add_argument("--out-dir", default=None, help="Output directory (default artifacts/reproducible/<tag>/dexdump if --tag is set, otherwise artifacts/reproducible/dexdump)")
    parser.add_argument("--dexdump", default=None, help="Path to dexdump (otherwise uses PATH)")
    parser.add_argument("--tag", default=None, help="Tag name to place outputs under artifacts/reproducible/<tag>/dexdump")
    args = parser.parse_args()

    official_apk = Path(args.official_apk).resolve()
    rebuilt_apk = Path(args.rebuilt_apk).resolve()
    if args.out_dir:
        out_dir = Path(args.out_dir)
    elif args.tag:
        out_dir = Path.cwd() / "artifacts" / "reproducible" / args.tag / "dexdump"
    else:
        out_dir = Path.cwd() / "artifacts" / "reproducible" / "dexdump"
    out_dir.mkdir(parents=True, exist_ok=True)

    dexdump_path = Path(args.dexdump) if args.dexdump else None
    if not dexdump_path:
        found = shutil.which("dexdump")
        if found:
            dexdump_path = Path(found)
    if not dexdump_path or not dexdump_path.exists():
        print("dexdump not found; set --dexdump or add it to PATH.", file=sys.stderr)
        sys.exit(1)

    with tempfile.TemporaryDirectory() as tmp:
        tmp_dir = Path(tmp)
        official_dir = tmp_dir / "official"
        rebuilt_dir = tmp_dir / "rebuilt"
        official_dir.mkdir()
        rebuilt_dir.mkdir()
        official_dex = extract_dex_files(official_apk, official_dir)
        rebuilt_dex = extract_dex_files(rebuilt_apk, rebuilt_dir)

        all_dex = sorted(official_dex | rebuilt_dex)
        if official_dex != rebuilt_dex:
            missing_official = rebuilt_dex - official_dex
            missing_rebuilt = official_dex - rebuilt_dex
            if missing_official:
                print(f"Warning: missing in official: {', '.join(sorted(missing_official))}", file=sys.stderr)
            if missing_rebuilt:
                print(f"Warning: missing in rebuilt: {', '.join(sorted(missing_rebuilt))}", file=sys.stderr)

        diff_paths: list[Path] = []
        for name in all_dex:
            off_path = official_dir / name
            reb_path = rebuilt_dir / name
            if not off_path.exists() or not reb_path.exists():
                continue
            official_dump = out_dir / f"official_{name}.dump"
            rebuilt_dump = out_dir / f"rebuilt_{name}.dump"
            run_dexdump(dexdump_path, off_path, official_dump)
            run_dexdump(dexdump_path, reb_path, rebuilt_dump)

            diff_path = out_dir / f"dexdump_{name}.diff"
            with diff_path.open("w") as fh:
                subprocess.run(["diff", str(official_dump), str(rebuilt_dump)], stdout=fh, check=False)
            diff_paths.append(diff_path)

    if diff_paths:
        print("dexdump diffs written:")
        for p in diff_paths:
            print(f"  {p}")
    else:
        print("No matching classes*.dex files found to diff.")


if __name__ == "__main__":
    main()
