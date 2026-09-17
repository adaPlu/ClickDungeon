#!/usr/bin/env python3
from pathlib import Path
import argparse
import json
import re

TARGETS = ("windows", "android-apk", "android-aab", "ios")
TEXT_SUFFIXES = {
    ".txt", ".json", ".xml", ".plist", ".pbxproj", ".gradle",
    ".properties", ".manifest", ".yaml", ".yml", ".ini", ".cfg", ".md",
}
FORBIDDEN = "ClickDungeon2"


def required_paths(target):
    if target == "windows":
        return ("ClickDungeon.exe", "ClickDungeon_Data")
    if target == "android-apk":
        return ("ClickDungeon.apk",)
    if target == "android-aab":
        return ("ClickDungeon.aab",)
    return ("Unity-iPhone.xcodeproj/project.pbxproj", "Classes")


def scan_forbidden(root):
    findings = []
    if not root.exists():
        return findings
    for path in root.rglob("*"):
        rel = path.relative_to(root).as_posix()
        if FORBIDDEN in rel:
            findings.append(f"forbidden branding in path: {rel}")
        if not path.is_file() or path.suffix.lower() not in TEXT_SUFFIXES:
            continue
        if path.stat().st_size > 2_000_000:
            continue
        text = path.read_text(errors="ignore")
        if FORBIDDEN in text:
            findings.append(f"forbidden branding in metadata: {rel}")
    return findings


def inspect(target, root, sha):
    findings = []
    if not re.fullmatch(r"[0-9a-fA-F]{40}", sha):
        findings.append("sha must be exactly 40 hexadecimal characters")
    if not root.is_dir():
        findings.append(f"artifact root missing: {root}")
    else:
        for relative in required_paths(target):
            path = root / relative
            if not path.exists():
                findings.append(f"missing artifact: {relative}")
        findings.extend(scan_forbidden(root))
    return {
        "schema_version": 1,
        "sha": sha,
        "target": target,
        "status": "PASS" if not findings else "FAIL",
        "findings": findings,
    }


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--target", choices=TARGETS, required=True)
    parser.add_argument("--root", type=Path, required=True)
    parser.add_argument("--sha", required=True)
    parser.add_argument("--out", type=Path, required=True)
    args = parser.parse_args()

    report = inspect(args.target, args.root, args.sha)
    args.out.parent.mkdir(parents=True, exist_ok=True)
    args.out.write_text(json.dumps(report, indent=2) + "\n")
    print(f"artifact inspection: {report['status']} ({args.target})")
    for finding in report["findings"]:
        print(f"- {finding}")
    return 0 if report["status"] == "PASS" else 1


if __name__ == "__main__":
    raise SystemExit(main())
