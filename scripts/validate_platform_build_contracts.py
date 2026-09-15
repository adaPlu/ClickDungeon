#!/usr/bin/env python3
import os
from pathlib import Path
import re
import sys

ROOT = Path(
    os.environ.get("CLICKDUNGEON_VALIDATION_ROOT", str(Path(__file__).resolve().parents[1]))
).resolve()
BUILD = ROOT / "Assets/ClickDungeon/Platform/Build"
CANONICAL = BUILD / "CanonicalBuildTargets.cs"
TARGET = BUILD / "BuildTargetContract.cs"

SECRET_LITERAL = re.compile(
    r"(?i)\b(?:password|secret|token|keystorepassword|keyaliaspassword|signingpassword)\b\s*[:=]\s*[\"'][^\"'$]{3,}[\"']"
)


def fail(message: str) -> None:
    print(f"PLATFORM BUILD CONTRACT ERROR: {message}", file=sys.stderr)
    raise SystemExit(1)


def require(path: Path) -> str:
    if not path.is_file():
        fail(f"missing required file: {path.relative_to(ROOT)}")
    return path.read_text(errors="ignore")


def main() -> None:
    target = require(TARGET)
    canonical = require(CANONICAL)

    for token in ("PlatformId", "Architecture", "ScriptingBackend", "ArtifactKinds", "RequiresExternalSigning"):
        if token not in target:
            fail(f"BuildTargetContract missing {token}")

    platform_requirements = {
        "Windows": ("RuntimePlatformId.Windows", "x64", "WindowsPlayer"),
        "Android": ("RuntimePlatformId.Android", "ARM64", "IL2CPP", "APK", "AAB"),
        "IOS": ("RuntimePlatformId.IOS", "ARM64", "IL2CPP", "XcodeExport"),
    }
    for name, required in platform_requirements.items():
        for token in required:
            if token not in canonical:
                fail(f"{name} build target missing {token}")

    if "private const bool RequiresExternalSigning = true;" not in canonical:
        fail("all canonical release targets must require external signing material")

    scan_roots = [BUILD, ROOT / "ProjectSettings", ROOT / ".github/workflows"]
    violations = []
    for scan_root in scan_roots:
        if not scan_root.exists():
            continue
        for path in scan_root.rglob("*"):
            if not path.is_file() or path.suffix.lower() not in {".cs", ".json", ".yml", ".yaml", ".asset", ".txt"}:
                continue
            text = path.read_text(errors="ignore")
            if SECRET_LITERAL.search(text):
                violations.append(str(path.relative_to(ROOT)))

    if violations:
        fail("embedded signing/auth secret literal detected: " + ", ".join(sorted(violations)))

    print("Platform build contracts: PASS (Windows x64; Android ARM64 IL2CPP APK/AAB; iOS IL2CPP Xcode export; external signing)")


if __name__ == "__main__":
    main()
