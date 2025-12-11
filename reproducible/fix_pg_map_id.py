#!/usr/bin/env python3
"""
Patch pg-map-id in classes*.dex and baseline.prof to a supplied value.

This supports modern, long R8 map IDs (32–64 hex chars) and rewrites the
DEX signature/checksum plus the baseline profile header checksums.

Usage:
    ./fix_pg_map_id.py input.apk output.apk <map-id-hex>
"""

from __future__ import annotations

import argparse
import hashlib
import os
import re
import struct
import zipfile
import zlib
from binascii import hexlify
from typing import Any, Dict, Match, Tuple

INFO_EMOJI = "ℹ️"
STEP_EMOJI = "➡️"
OK_EMOJI = "✅"
WARN_EMOJI = "⚠️"

DEX_MAGIC = b"dex\n"
DEX_MAGIC_RE = re.compile(rb"dex\n(\d{3})\x00")
PROF_MAGIC = b"pro\x00"
PROF_010_P = b"010\x00"
CLASSES_DEX_RE = re.compile(r"classes\d*\.dex")
ASSET_PROF = "assets/dexopt/baseline.prof"
# Accept long map ids; newer R8 emits 52+ hex chars.
PG_MAP_ID_RE = re.compile(rb'(pg-map-id":")(?!.*pg-map-id).*?([0-9a-f]{32,64})(")')
# R8 encodes map-id in source file names like r8-map-id-<hex>
R8_MAP_STRING_RE = re.compile(rb'(r8-map-id-)([0-9a-f]{32,64})')

ATTRS = (
    "compress_type",
    "create_system",
    "create_version",
    "date_time",
    "external_attr",
    "extract_version",
    "flag_bits",
)
LEVELS = (9, 6, 4, 1)


class Error(RuntimeError):
    pass


class ReproducibleZipInfo(zipfile.ZipInfo):
    """ZipInfo wrapper that preserves key attributes and compression level."""

    _compresslevel: int

    def __init__(self, zinfo: zipfile.ZipInfo, **override: Any) -> None:
        self._override: Dict[str, Any] = override
        for k in self.__slots__:
            if hasattr(zinfo, k):
                setattr(self, k, getattr(zinfo, k))

    def __getattribute__(self, name: str) -> Any:
        if name != "_override":
            try:
                return self._override[name]
            except KeyError:
                pass
        return object.__getattribute__(self, name)


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("input_apk", help="Source APK to patch")
    parser.add_argument("output_apk", help="Patched APK output path")
    parser.add_argument("map_id", help="Target pg-map-id (hex)")
    args = parser.parse_args()
    fix_pg_map_id_apk(args.input_apk, args.output_apk, args.map_id)


def fix_pg_map_id_apk(input_apk: str, output_apk: str, map_id: str) -> None:
    with open(input_apk, "rb") as fh_raw:
        with zipfile.ZipFile(input_apk) as zf_in:
            with zipfile.ZipFile(output_apk, "w") as zf_out:
                file_data: Dict[str, bytes] = {}
                for info in zf_in.infolist():
                    if re.fullmatch(CLASSES_DEX_RE, info.filename) or info.filename == ASSET_PROF:
                        print(f"{STEP_EMOJI} reading {info.filename!r}...")
                        file_data[info.filename] = zf_in.read(info)
                _fix_pg_map_id(file_data, map_id)
                for info in zf_in.infolist():
                    attrs = {attr: getattr(info, attr) for attr in ATTRS}
                    zinfo = ReproducibleZipInfo(info, **attrs)
                    if info.compress_type == 8:
                        fh_raw.seek(info.header_offset)
                        n, m = struct.unpack("<HH", fh_raw.read(30)[26:30])
                        fh_raw.seek(info.header_offset + 30 + m + n)
                        ccrc = 0
                        size = info.compress_size
                        while size > 0:
                            ccrc = zlib.crc32(fh_raw.read(min(size, 4096)), ccrc)
                            size -= 4096
                        with zf_in.open(info) as fh_in:
                            comps = {lvl: zlib.compressobj(lvl, 8, -15) for lvl in LEVELS}
                            ccrcs = {lvl: 0 for lvl in LEVELS}
                            while True:
                                data = fh_in.read(4096)
                                if not data:
                                    break
                                for lvl in LEVELS:
                                    ccrcs[lvl] = zlib.crc32(comps[lvl].compress(data), ccrcs[lvl])
                            for lvl in LEVELS:
                                if ccrc == zlib.crc32(comps[lvl].flush(), ccrcs[lvl]):
                                    zinfo._compresslevel = lvl
                                    break
                            else:
                                raise Error(f"Unable to determine compresslevel for {info.filename!r}")
                    elif info.compress_type != 0:
                        raise Error(f"Unsupported compress_type {info.compress_type}")
                    if re.fullmatch(CLASSES_DEX_RE, info.filename) or info.filename == ASSET_PROF:
                        print(f"{STEP_EMOJI} writing {info.filename!r}...")
                        zf_out.writestr(zinfo, file_data[info.filename])
                    else:
                        with zf_in.open(info) as fh_in:
                            with zf_out.open(zinfo, "w") as fh_out:
                                while True:
                                    data = fh_in.read(4096)
                                    if not data:
                                        break
                                    fh_out.write(data)


def _fix_pg_map_id(file_data: Dict[str, bytes], map_id: str) -> None:
    crcs: Dict[str, int] = {}
    for filename in file_data:
        if re.fullmatch(CLASSES_DEX_RE, filename):
            print(f"{STEP_EMOJI} fixing {filename!r}...")
            data, crc = _fix_dex_id_checksum(file_data[filename], map_id.encode())
            file_data[filename] = data
            crcs[filename] = crc
    if ASSET_PROF in file_data:
        print(f"{STEP_EMOJI} fixing {ASSET_PROF!r}...")
        file_data[ASSET_PROF] = _fix_prof_checksum(file_data[ASSET_PROF], crcs)


def _fix_dex_id_checksum(data: bytes, map_id: bytes) -> Tuple[bytes, int]:
    def repl(m: Match[bytes]) -> bytes:
        print(f"{STEP_EMOJI} fixing pg-map-id: {m.group(2)!r} -> {map_id!r}")
        return m.group(1) + map_id + m.group(3)

    magic = data[:8]
    if magic[:4] != DEX_MAGIC or not DEX_MAGIC_RE.fullmatch(magic):
        raise Error(f"Unsupported magic {magic!r}")
    print(f"{INFO_EMOJI} dex version={int(magic[4:7]):03d}")
    checksum, signature = struct.unpack("<I20s", data[8:32])
    body = data[32:]
    fixed_body = re.sub(PG_MAP_ID_RE, repl, body)

    # Also rewrite r8-map-id-* strings in the DEX string data if length matches.
    def replace_r8_string(match: Match[bytes]) -> bytes:
        old_id = match.group(2)
        if len(old_id) != len(map_id):
            # avoid corrupting string length if lengths differ
            return match.group(0)
        print(f"{STEP_EMOJI} fixing r8-map-id string: {old_id!r} -> {map_id!r}")
        return match.group(1) + map_id

    fixed_body = re.sub(R8_MAP_STRING_RE, replace_r8_string, fixed_body)

    if fixed_body == data[32:]:
        print(f"{WARN_EMOJI} (not modified)")
        return data, zlib.crc32(data)
    fixed_sig = hashlib.sha1(fixed_body).digest()
    print(f"{STEP_EMOJI} fixing signature: {hexlify(signature).decode()} -> {hexlify(fixed_sig).decode()}")
    fixed_data = fixed_sig + fixed_body
    fixed_checksum = zlib.adler32(fixed_data)
    print(f"{STEP_EMOJI} fixing checksum: 0x{checksum:x} -> 0x{fixed_checksum:x}")
    fixed_blob = magic + int.to_bytes(fixed_checksum, 4, "little") + fixed_data
    return fixed_blob, zlib.crc32(fixed_blob)


def _fix_prof_checksum(data: bytes, crcs: Dict[str, int]) -> bytes:
    magic, data = _split(data, 4)
    version, data = _split(data, 4)
    if magic == PROF_MAGIC:
        if version == PROF_010_P:
            print(f"{INFO_EMOJI} prof version=010 P")
            return PROF_MAGIC + PROF_010_P + _fix_prof_010_p_checksum(data, crcs)
        raise Error(f"Unsupported prof version {version!r}")
    raise Error(f"Unsupported magic {magic!r}")


def _fix_prof_010_p_checksum(data: bytes, crcs: Dict[str, int]) -> bytes:
    num_dex_files, uncompressed_size, compressed_size, data = _unpack("<BII", data)
    if len(data) != compressed_size:
        raise Error("Compressed data size does not match")
    data = zlib.decompress(data)
    if len(data) != uncompressed_size:
        raise Error("Uncompressed data size does not match")
    fixed_entries = []
    rest = data
    for _ in range(num_dex_files):
        profile_key_size, num_type_ids, hot_method_region_size, dex_checksum, num_method_ids, rest = _unpack(
            "<HHIII", rest
        )
        profile_key, rest = _split(rest, profile_key_size)
        filename = profile_key.decode()
        fixed_checksum = crcs.get(filename, dex_checksum)
        if fixed_checksum != dex_checksum:
            print(f"{STEP_EMOJI} fixing {filename!r} checksum: 0x{dex_checksum:x} -> 0x{fixed_checksum:x}")
        fixed_entries.append(
            struct.pack(
                "<HHIII", profile_key_size, num_type_ids, hot_method_region_size, fixed_checksum, num_method_ids
            )
            + profile_key
        )
    fixed_data = b"".join(fixed_entries) + rest
    fixed_cdata = zlib.compress(fixed_data, 1)
    fixed_hdr = struct.pack("<BII", num_dex_files, len(fixed_data), len(fixed_cdata))
    return fixed_hdr + fixed_cdata


def _unpack(fmt: str, data: bytes) -> Any:
    size = fmt.count("B") + 2 * fmt.count("H") + 4 * fmt.count("I")
    return struct.unpack(fmt, data[:size]) + (data[size:],)


def _split(data: bytes, size: int) -> Tuple[bytes, bytes]:
    return data[:size], data[size:]


if __name__ == "__main__":
    main()
