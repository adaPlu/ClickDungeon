#!/usr/bin/env python3
from pathlib import Path
import os
import subprocess
import sys

from release_evidence import GateResult, GateStatus, ReleaseEvidenceReport

ROOT = Path(__file__).resolve().parents[1]
OUTPUT = ROOT / "build/phase16/source-readiness.json"

REFERENCE_PATHS = (
    "docs/reference/latest/09-title-main-menu.png",
    "docs/reference/latest/10-core-gameplay.png",
    "docs/reference/latest/11-dungeon-tiles-a.png",
    "docs/reference/latest/12-dungeon-tiles-b.png",
    "docs/reference/latest/16-sir-clickington.png",
    "docs/reference/latest/21-hero-roster.png",
)

UNITY_METADATA_PATHS = (
    "ProjectSettings/ProjectVersion.txt",
    "Packages/manifest.json",
    "Packages/packages-lock.json",
)


class ReadinessError(RuntimeError):
    pass


def resolve_head_sha(root: Path) -> str:
    github_sha = os.environ.get("GITHUB_SHA", "").strip().lower()
    if github_sha:
        return github_sha
    result = subprocess.run(
        ["git", "rev-parse", "HEAD"],
        cwd=root,
        capture_output=True,
        text=True,
        check=False,
    )
    if result.returncode != 0:
        raise ReadinessError(result.stderr.strip() or "unable to resolve git HEAD")
    return result.stdout.strip().lower()


def classify_unity_metadata(root: Path) -> GateResult:
    missing = [relative for relative in UNITY_METADATA_PATHS if not (root / relative).is_file()]
    if missing:
        return GateResult(
            "unity_metadata",
            GateStatus.BLOCKED,
            "missing authentic Unity metadata: " + ", ".join(missing),
        )
    return GateResult(
        "unity_metadata",
        GateStatus.PASS,
        "required Unity project metadata is present; authenticity is validated by the Unity-project gate",
    )


def _classify_brand(root: Path) -> GateResult:
    path = root / "Assets/ClickDungeon/Core/Brand/ProductBrand.cs"
    if not path.is_file():
        return GateResult("runtime_brand", GateStatus.FAIL, f"missing {path.relative_to(root)}")
    text = path.read_text()
    if 'PlayerFacingName = "ClickDungeon"' not in text:
        return GateResult("runtime_brand", GateStatus.FAIL, "ProductBrand.PlayerFacingName is not ClickDungeon")
    if 'PlayerFacingName = "ClickDungeon2"' in text:
        return GateResult("runtime_brand", GateStatus.FAIL, "forbidden ClickDungeon2 player-facing brand detected")
    return GateResult("runtime_brand", GateStatus.PASS, "ProductBrand.PlayerFacingName is ClickDungeon")


def _classify_references(root: Path) -> GateResult:
    missing = [relative for relative in REFERENCE_PATHS if not (root / relative).is_file()]
    if missing:
        return GateResult("canonical_references", GateStatus.FAIL, "missing canonical references: " + ", ".join(missing))
    return GateResult(
        "canonical_references",
        GateStatus.PASS,
        "title, gameplay, tile, Sir Clickington, and hero-roster canonical references are pinned",
    )


def _run_production_art(root: Path, strict: bool) -> GateResult:
    args = [sys.executable, str(root / "scripts/validate_production_art.py")]
    if strict:
        args.append("--strict")
    result = subprocess.run(args, cwd=root, capture_output=True, text=True, check=False)
    detail = (result.stdout.strip() or result.stderr.strip() or f"exit code {result.returncode}").splitlines()[-1]
    gate = "production_art_strict" if strict else "production_art_source"
    if result.returncode == 0:
        return GateResult(gate, GateStatus.PASS, detail)
    return GateResult(gate, GateStatus.FAIL, detail)


def build_report(root: Path) -> ReleaseEvidenceReport:
    head_sha = resolve_head_sha(root)
    gates = [
        _classify_brand(root),
        _classify_references(root),
        _run_production_art(root, strict=False),
        classify_unity_metadata(root),
        _run_production_art(root, strict=True),
        GateResult("windows_build", GateStatus.BLOCKED, "source-only readiness does not prove a Windows player build"),
        GateResult("android_apk", GateStatus.BLOCKED, "source-only readiness does not prove an Android APK"),
        GateResult("android_aab", GateStatus.BLOCKED, "source-only readiness does not prove an Android AAB"),
        GateResult("ios_xcode_export", GateStatus.BLOCKED, "source-only readiness does not prove an iOS Xcode export"),
        GateResult("signing", GateStatus.BLOCKED, "authorized external signing credentials are not evaluated by source-only readiness"),
    ]
    return ReleaseEvidenceReport(head_sha=head_sha, gates=gates)


def main() -> int:
    try:
        report = build_report(ROOT)
    except (ReadinessError, ValueError) as exc:
        print(f"phase16-readiness-error: {exc}", file=sys.stderr)
        return 2

    report.write_json(OUTPUT)
    for gate in report.gates:
        print(f"{gate.gate}: {gate.status.value} - {gate.detail}")
    print(f"phase 16 source readiness evidence: {OUTPUT.relative_to(ROOT)}")
    return 1 if report.has_failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
