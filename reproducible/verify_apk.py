#!/usr/bin/env python3
"""
Rebuild a tagged release in Docker, patch map-id if needed, and compare against an official APK.

Usage: ./verify_apk.py <git-tag-or-branch> <path-to-official-apk> [--stage all|build|diff]
"""

from __future__ import annotations

import argparse
import hashlib
import os
import re
import shutil
import subprocess
import sys
import tempfile
import zipfile
from pathlib import Path

REPO_URL_DEFAULT = "https://github.com/gemwalletcom/gem-android.git"
BUNDLE_TASK_DEFAULT = "clean :app:bundleGoogleRelease assembleUniversalRelease"
APK_SUBDIR_DEFAULT = "app/build/outputs/apk/universal/release"
BASE_IMAGE_DEFAULT = "ghcr.io/gemwalletcom/gem-android-base"
APP_IMAGE_DEFAULT = "gem-android-app-verify"
DOCKER_PLATFORM_DEFAULT = "linux/amd64"
GRADLE_WORKERS_MAX_DEFAULT = "4"

INFO_EMOJI = "ℹ️"
STEP_EMOJI = "➡️"
WARN_EMOJI = "⚠️"
OK_EMOJI = "✅"
FAIL_EMOJI = "❌"


def run(cmd: list[str], check: bool = True, capture_output: bool = False, env: dict | None = None) -> subprocess.CompletedProcess:
    return subprocess.run(cmd, check=check, text=True, capture_output=capture_output, env=env)


def need_cmd(name: str) -> None:
    if shutil.which(name) is None:
        sys.stderr.write(f"Missing required command: {name}\n")
        sys.exit(1)


def sanitize(value: str) -> str:
    return re.sub(r"[^a-zA-Z0-9_.-]", "-", value)


def sha256_file(path: Path) -> str:
    h = hashlib.sha256()
    with path.open("rb") as fh:
        for chunk in iter(lambda: fh.read(8192), b""):
            h.update(chunk)
    return h.hexdigest()


def strip_signing_artifacts(directory: Path) -> None:
    meta = directory / "META-INF"
    if meta.exists():
        for pattern in ["*.RSA", "*.DSA", "*.EC", "*.SF", "*.MF", "*.DSIG", "CERT.*", "CHANGES.*", "MANIFEST.MF", "SIGNATURE.SF"]:
            for p in meta.glob(pattern):
                p.unlink(missing_ok=True)
        (meta / "com" / "android" / "metadata").unlink(missing_ok=True)
    for pattern in ["**/stamp-cert-sha256", "**/*.idsig"]:
        for p in directory.glob(pattern):
            p.unlink(missing_ok=True)


def reset_cache_dir(path: Path, label: str) -> Path:
    resolved = path.expanduser().resolve()
    root_path = Path(resolved.anchor)
    if resolved == root_path:
        sys.stderr.write(f"Refusing to clean {label} cache at root path: {resolved}\n")
        sys.exit(1)
    if resolved.exists():
        print(f"Removing existing {label} cache at {resolved}")
        shutil.rmtree(resolved, ignore_errors=True)
    resolved.mkdir(parents=True, exist_ok=True)
    return resolved


def remove_cache_dir(path: Path, label: str) -> None:
    resolved = path.expanduser().resolve()
    root_path = Path(resolved.anchor)
    if resolved == root_path:
        sys.stderr.write(f"Refusing to remove {label} cache at root path: {resolved}\n")
        sys.exit(1)
    print(f"Removing {label} cache at {resolved}")
    shutil.rmtree(resolved, ignore_errors=True)


def copy_signing_block(official_apk: Path, rebuilt_apk: Path, output_apk: Path) -> str | None:
    try:
        import apksigcopier
    except ImportError:
        print(f"{WARN_EMOJI} apksigcopier not installed; skipping signature copy.")
        return None

    output_apk.unlink(missing_ok=True)
    output_apk.parent.mkdir(parents=True, exist_ok=True)
    try:
        apksigcopier.do_copy(str(official_apk), str(rebuilt_apk), str(output_apk))
    except Exception as exc:  # noqa: BLE001
        print(f"{WARN_EMOJI} apksigcopier failed: {exc}")
        output_apk.unlink(missing_ok=True)
        return None
    return sha256_file(output_apk)


def copy_reports(root_dir: Path, work_dir: Path, tag_safe: str, names: list[str]) -> None:
    reports_dir = root_dir / "artifacts" / "reproducible" / "reports" / tag_safe
    reports_dir.mkdir(parents=True, exist_ok=True)
    for name in names:
        src = work_dir / name
        if src.exists():
            shutil.copy2(src, reports_dir / name)


def resolve_ref(ref: str, repo_url: str) -> str:
    heads = run(["git", "ls-remote", "--exit-code", "--heads", repo_url, ref], check=False, capture_output=True)
    tags = run(["git", "ls-remote", "--exit-code", "--tags", repo_url, ref], check=False, capture_output=True)
    lines = (heads.stdout + tags.stdout).splitlines()
    peeled_sha: str | None = None
    direct_sha: str | None = None
    tag_ref = f"refs/tags/{ref}"
    tag_peeled_ref = f"{tag_ref}^{{}}"
    head_ref = f"refs/heads/{ref}"
    for line in lines:
        parts = line.strip().split()
        if len(parts) != 2:
            continue
        sha, name = parts
        if name in (tag_peeled_ref, f"{ref}^{{}}"):  # Annotated tag peeled to commit
            peeled_sha = sha
        elif name in (tag_ref, head_ref, ref):
            direct_sha = sha

    if not peeled_sha and direct_sha:
        # Try to resolve the peeled commit explicitly for annotated tags
        peeled = run(["git", "ls-remote", "--exit-code", "--tags", repo_url, f"{ref}^{{}}"], check=False, capture_output=True)
        if peeled.returncode == 0:
            parts = peeled.stdout.strip().split()
            if len(parts) >= 1:
                peeled_sha = parts[0]

    if peeled_sha:
        print(f"{INFO_EMOJI} Resolving ref {ref} -> {peeled_sha} (peeled from tag object {direct_sha or 'unknown'})")
        return ref
    if direct_sha:
        print(f"{INFO_EMOJI} Resolving ref {ref} -> {direct_sha}")
        return ref

    sys.stderr.write(f"Failed to find branch/tag '{ref}' in {repo_url}\n")
    sys.exit(1)


def ensure_base_image(base_image: str, base_tag: str, pull: bool, platform: str) -> None:
    image_ref = f"{base_image}:{base_tag}"
    env = os.environ.copy()
    if platform:
        env["DOCKER_DEFAULT_PLATFORM"] = env.get("DOCKER_DEFAULT_PLATFORM", platform)
    if pull:
        print(f"{INFO_EMOJI} Pulling base image {image_ref} ...")
        pull_cmd = ["docker", "pull", image_ref]
        if platform:
            pull_cmd = ["docker", "pull", "--platform", platform, image_ref]
        pulled = run(pull_cmd, check=False, env=env)
        if pulled.returncode == 0:
            print(f"{OK_EMOJI} Pulled base image {image_ref}")
            return
        print(f"{WARN_EMOJI} Pull failed ({pulled.returncode}); falling back to local image or rebuild.")

    result = run(["docker", "image", "inspect", image_ref], check=False)
    if result.returncode == 0:
        print(f"{OK_EMOJI} Using existing base image {image_ref}")
        return

    print(f"Building base image {image_ref}...")
    env["DOCKER_BUILDKIT"] = env.get("DOCKER_BUILDKIT", "1")
    run(["docker", "build", "--platform", platform, "-t", image_ref, "."], check=True, env=env)


def build_app_image(tag: str, base_image: str, base_tag: str, gradle_task: str, map_id_seed: str, app_image: str, platform: str) -> None:
    env = os.environ.copy()
    env["DOCKER_BUILDKIT"] = "1"
    env["DOCKER_DEFAULT_PLATFORM"] = env.get("DOCKER_DEFAULT_PLATFORM", platform)
    seed_arg = map_id_seed or ""
    cmd = [
        "docker",
        "build",
        "--platform",
        platform,
        "-t",
        app_image,
        "--build-arg",
        f"TAG={tag}",
        "--build-arg",
        "SKIP_SIGN=true",
        "--build-arg",
        f"BASE_IMAGE={base_image}",
        "--build-arg",
        f"BASE_IMAGE_TAG={base_tag}",
        "--build-arg",
        f"BUNDLE_TASK={gradle_task}",
        "--build-arg",
        f"R8_MAP_ID_SEED={seed_arg}",
        "-f",
        "reproducible/Dockerfile",
        ".",
    ]
    print(f"Building app image for tag {tag} using task {gradle_task}...")
    run(cmd, env=env)


def build_outputs_in_container(app_image: str, container_name: str, gradle_task: str, map_id_seed: str, gradle_cache: Path, maven_cache: Path, platform: str, workers_max: str) -> None:
    run(["docker", "rm", "-f", container_name], check=False)
    seed_env = map_id_seed or ""
    cmd = [
        "docker",
        "run",
        "--platform",
        platform,
        "--name",
        container_name,
        "-e",
        "SKIP_SIGN=true",
        "-e",
        f"BUNDLE_TASK={gradle_task}",
        "-e",
        f"R8_MAP_ID_SEED={seed_env}",
        "-e",
        f"GRADLE_WORKERS_MAX={workers_max}",
        "-v",
        f"{gradle_cache}:/root/.gradle",
        "-v",
        f"{maven_cache}:/root/.m2",
        app_image,
        "bash",
        "-lc",
        "cd /root/gem-android && ./gradlew ${BUNDLE_TASK} --no-daemon --build-cache -Dorg.gradle.workers.max=${GRADLE_WORKERS_MAX}",
    ]
    run(cmd)


def extract_apk_outputs(container_name: str, work_dir: Path, apk_subdir: str) -> Path:
    dest = work_dir / "apk"
    shutil.rmtree(dest, ignore_errors=True)
    dest.mkdir(parents=True, exist_ok=True)
    run(["docker", "cp", f"{container_name}:/root/gem-android/{apk_subdir}/.", str(dest)])
    apk = next(dest.rglob("*.apk"), None)
    if not apk:
        sys.stderr.write(f"Failed to locate APK inside {apk_subdir}\n")
        sys.exit(1)
    return apk


def run_diffoscope_report(rebuilt_apk: Path, official_apk: Path, work_dir: Path, suffix: str = "") -> None:
    if os.environ.get("VERIFY_SKIP_DIFFOSCOPE", "false").lower() == "true":
        sys.stderr.write("Skipping diffoscope because VERIFY_SKIP_DIFFOSCOPE=true.\n")
        return
    if shutil.which("diffoscope") is None:
        sys.stderr.write("diffoscope not found; install it or set VERIFY_SKIP_DIFFOSCOPE=true to silence this message.\n")
        return

    diff_dir = work_dir / f"diffoscope{suffix}"
    rebuilt_dir = diff_dir / "rebuilt"
    official_dir = diff_dir / "official"
    shutil.rmtree(diff_dir, ignore_errors=True)
    rebuilt_dir.mkdir(parents=True, exist_ok=True)
    official_dir.mkdir(parents=True, exist_ok=True)

    with zipfile.ZipFile(rebuilt_apk) as zf:
        zf.extractall(rebuilt_dir)
    with zipfile.ZipFile(official_apk) as zf:
        zf.extractall(official_dir)

    strip_signing_artifacts(rebuilt_dir)
    strip_signing_artifacts(official_dir)

    report = work_dir / f"diffoscope{suffix}.html"
    diffoscope_args = ["--exclude-directory-metadata=yes", "--output-empty"]
    extra = os.environ.get("DIFFOSCOPE_ARGS", "")
    dex_only = os.environ.get("DIFFOSCOPE_DEX_ONLY", "false").lower() == "true"
    if extra:
        diffoscope_args.extend(extra.split())
    elif dex_only:
        diffoscope_args.extend(["--exclude-files=res/*", "--exclude-files=META-INF/*", "--exclude-files=assets/*"])

    cmd = ["diffoscope", *diffoscope_args, "--html", str(report), str(rebuilt_dir), str(official_dir)]
    result = run(cmd, check=False)
    if result.returncode == 0:
        print(f"diffoscope report (no differences) written to {report}")
    elif result.returncode == 1:
        print(f"diffoscope detected differences; see {report}", file=sys.stderr)
    else:
        print(f"diffoscope exited with status {result.returncode}. Report (if any): {report}", file=sys.stderr)


def get_map_id(apk: Path) -> str | None:
    with zipfile.ZipFile(apk) as zf:
        data = zf.read("classes.dex")
    m = re.search(rb'pg-map-id\":\"([0-9a-f]+)\"', data)
    return m.group(1).decode() if m else None


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("tag", help="Git tag or branch to build")
    parser.add_argument("official_apk", help="Path to the official APK")
    parser.add_argument("--stage", choices=["all", "build", "diff"], default="all", help="Run build+diff, build only, or diff only (default: all)")
    parser.add_argument("--work-dir", default=None, help="Override output dir (default artifacts/reproducible/<tag>)")
    args = parser.parse_args()

    need_cmd("docker")
    need_cmd("java")
    need_cmd("unzip")
    need_cmd("curl")

    root_dir = Path(__file__).resolve().parent.parent
    os.chdir(root_dir)

    if not Path("local.properties").exists():
        sys.stderr.write("local.properties is required to access GitHub packages.\n")
        sys.exit(1)

    official_apk_path = Path(args.official_apk).resolve()
    if not official_apk_path.exists():
        sys.stderr.write(f"Official APK not found: {official_apk_path}\n")
        sys.exit(1)

    tag_safe = sanitize(args.tag) or "latest"
    map_id_seed = os.environ.get("R8_MAP_ID_SEED") or ""
    repo_url = os.environ.get("VERIFY_REPO_URL", REPO_URL_DEFAULT)
    resolved_tag = resolve_ref(args.tag, repo_url)
    base_tag_file = Path(__file__).with_name("base_image_tag.txt")
    docker_platform = os.environ.get("VERIFY_DOCKER_PLATFORM", DOCKER_PLATFORM_DEFAULT)
    if docker_platform:
        os.environ.setdefault("DOCKER_DEFAULT_PLATFORM", docker_platform)

    work_dir = Path(args.work_dir) if args.work_dir else root_dir / "artifacts" / "reproducible" / tag_safe
    if args.stage != "diff":
        shutil.rmtree(work_dir, ignore_errors=True)
        work_dir.mkdir(parents=True, exist_ok=True)
    else:
        work_dir.mkdir(parents=True, exist_ok=True)

    official_copy = work_dir / "official.apk"
    rebuilt_apk = work_dir / "rebuilt.apk"
    r8_patched_apk = work_dir / "r8_patched.apk"

    # Stage: build
    if args.stage in ("all", "build"):
        shutil.copy2(official_apk_path, official_copy)
        base_image = os.environ.get("VERIFY_BASE_IMAGE", BASE_IMAGE_DEFAULT)
        env_base_tag = os.environ.get("VERIFY_BASE_TAG")
        file_base_tag = base_tag_file.read_text().strip() if base_tag_file.exists() else None
        base_tag = env_base_tag or file_base_tag
        if not base_tag:
            sys.stderr.write("Missing base image tag; set VERIFY_BASE_TAG or create reproducible/base_image_tag.txt\n")
            sys.exit(1)
        pull_base = os.environ.get("VERIFY_PULL_BASE", "true").lower() == "true"
        gradle_task = os.environ.get("VERIFY_GRADLE_TASK", BUNDLE_TASK_DEFAULT)
        apk_subdir = os.environ.get("VERIFY_APK_SUBDIR", APK_SUBDIR_DEFAULT)
        workers_max = os.environ.get("GRADLE_WORKERS_MAX", GRADLE_WORKERS_MAX_DEFAULT)

        app_image_tag = sanitize(args.tag.lower()) or "latest"
        app_image = os.environ.get("VERIFY_APP_IMAGE", APP_IMAGE_DEFAULT) + f":{app_image_tag}"
        app_container = f"gem-android-app-build-{tag_safe}"

        gradle_cache = reset_cache_dir(Path(os.environ.get("VERIFY_GRADLE_CACHE", tempfile.mkdtemp())), f"{STEP_EMOJI} Gradle")
        maven_cache = reset_cache_dir(Path(os.environ.get("VERIFY_M2_CACHE", tempfile.mkdtemp())), f"{STEP_EMOJI} Maven")

        try:
            print(f"{STEP_EMOJI} Building base image (or reusing) and app image...")
            ensure_base_image(base_image, base_tag, pull_base, docker_platform)
            print(
                f"{INFO_EMOJI} Build parameters: base_image={base_image}:{base_tag}, "
                f"platform={docker_platform}, gradle_task='{gradle_task}', R8_MAP_ID_SEED='{map_id_seed}', workers_max={workers_max}"
            )
            build_app_image(resolved_tag, base_image, base_tag, gradle_task, map_id_seed, app_image, docker_platform)
            build_outputs_in_container(app_image, app_container, gradle_task, map_id_seed, gradle_cache, maven_cache, docker_platform, workers_max)
            built_apk = extract_apk_outputs(app_container, work_dir, apk_subdir)
            shutil.copy2(built_apk, rebuilt_apk)
        finally:
            run(["docker", "rm", "-f", app_container], check=False)
            remove_cache_dir(gradle_cache, "Gradle")
            remove_cache_dir(maven_cache, "Maven")

        if args.stage == "build":
            print(f"{OK_EMOJI} Build complete. Artifacts in {work_dir}")
            return

    # Stage: diff (can be run standalone if artifacts already exist)
    if not official_copy.exists() and official_apk_path.exists():
        shutil.copy2(official_apk_path, official_copy)
    if not official_copy.exists() or not rebuilt_apk.exists():
        sys.stderr.write("Missing official.apk or rebuilt.apk; run with --stage build first.\n")
        sys.exit(1)

    rebuilt_hash = sha256_file(rebuilt_apk)
    official_hash = sha256_file(official_copy)
    print(f"{INFO_EMOJI} Rebuilt APK SHA-256 : {rebuilt_hash}")
    print(f"{INFO_EMOJI} Official APK SHA-256: {official_hash}")

    if rebuilt_hash == official_hash:
        print(f"{OK_EMOJI} Success: APKs match.")
        return

    official_map = get_map_id(official_copy)
    rebuilt_map = get_map_id(rebuilt_apk)
    if official_map and rebuilt_map and official_map != rebuilt_map:
        print(f"{STEP_EMOJI} Patching map-id {rebuilt_map} -> {official_map} ...")
        run([sys.executable, str(Path(__file__).with_name("fix_pg_map_id.py")), str(rebuilt_apk), str(r8_patched_apk), official_map], check=True)
        print(f"{OK_EMOJI} Map-id patched to {official_map}")
    elif official_map and rebuilt_map == official_map:
        print(f"{OK_EMOJI} Map-id already matches official; skipping patch.")
    else:
        print(f"{WARN_EMOJI} Map-id not found; skipping patch.")

    # Reattach official signing block after map-id patch to confirm payload identity.
    rebuilt_for_sig = r8_patched_apk if r8_patched_apk.exists() else rebuilt_apk
    rebuilt_signed = work_dir / "rebuilt_signed.apk"
    signed_hash = copy_signing_block(official_copy, rebuilt_for_sig, rebuilt_signed)
    if signed_hash:
        print(f"{INFO_EMOJI} Rebuilt (signature copied) SHA-256: {signed_hash}")
        if signed_hash == official_hash:
            print(f"{OK_EMOJI} APK matches official releases with the signing block is copied.")
            copy_reports(root_dir, work_dir, tag_safe, ["official.apk", "rebuilt.apk", "r8_patched.apk", "rebuilt_signed.apk", "diffoscope.html", "diffoscope_patched.html"])
            return
    else:
        print(f"{WARN_EMOJI} Signature copy skipped or failed; proceeding with diffoscope.")

    print(f"{FAIL_EMOJI} Mismatch: APKs differ.", file=sys.stderr)
    run_diffoscope_report(rebuilt_for_sig, official_copy, work_dir)
    if r8_patched_apk.exists():
        run_diffoscope_report(r8_patched_apk, official_copy, work_dir, suffix="_patched")
    # Copy a subset into reports folder for convenience
    copy_reports(root_dir, work_dir, tag_safe, ["official.apk", "rebuilt.apk", "rebuilt_signed.apk", "r8_patched.apk", "diffoscope.html", "diffoscope_patched.html"])

    if os.environ.get("VERIFY_ALLOW_MISMATCH", "false").lower() == "true":
        return
    sys.exit(2)


if __name__ == "__main__":
    main()
