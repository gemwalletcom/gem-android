#!/usr/bin/env python3
"""
Generate a dexdump diff for classes.dex between two APKs.

Usage:
    ./diff_dexdump.py <official-apk> <rebuilt-apk> [--out-dir DIR] [--dexdump PATH]

Outputs:
    - official_classes.dump
    - rebuilt_classes.dump
    - dexdump.diff (unified diff)

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


def extract_classes(apk_path: Path, dest: Path) -> None:
    with zipfile.ZipFile(apk_path) as zf:
        dest.write_bytes(zf.read("classes.dex"))


def run_dexdump(dexdump: Path, src: Path, dst: Path) -> None:
    with dst.open("w") as fh:
        subprocess.run([str(dexdump), "-d", str(src)], check=True, stdout=fh)


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("official_apk")
    parser.add_argument("rebuilt_apk")
    parser.add_argument("--out-dir", default=None, help="Output directory (default artifacts/reproducible/dexdump)")
    parser.add_argument("--dexdump", default=None, help="Path to dexdump (otherwise uses PATH)")
    args = parser.parse_args()

    official_apk = Path(args.official_apk).resolve()
    rebuilt_apk = Path(args.rebuilt_apk).resolve()
    out_dir = Path(args.out_dir) if args.out_dir else Path.cwd() / "artifacts" / "reproducible" / "dexdump"
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
        official_dex = tmp_dir / "official.dex"
        rebuilt_dex = tmp_dir / "rebuilt.dex"
        extract_classes(official_apk, official_dex)
        extract_classes(rebuilt_apk, rebuilt_dex)

        official_dump = out_dir / "official_classes.dump"
        rebuilt_dump = out_dir / "rebuilt_classes.dump"

        run_dexdump(dexdump_path, official_dex, official_dump)
        run_dexdump(dexdump_path, rebuilt_dex, rebuilt_dump)

        diff_path = out_dir / "dexdump.diff"
        with diff_path.open("w") as fh:
            subprocess.run(["diff", str(official_dump), str(rebuilt_dump)], stdout=fh, check=False)

    print(f"dexdump diff written to {diff_path}")
    print(f"official dump: {official_dump}")
    print(f"rebuilt dump:  {rebuilt_dump}")


if __name__ == "__main__":
    main()
