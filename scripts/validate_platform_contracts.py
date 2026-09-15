#!/usr/bin/env python3
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
PLATFORM_ROOT = ROOT / "Assets/ClickDungeon/Platform"

REQUIRED_FILES = [
    PLATFORM_ROOT / "ClickDungeon.Platform.asmdef",
    PLATFORM_ROOT / "RuntimePlatformId.cs",
    PLATFORM_ROOT / "PlatformCapabilities.cs",
    PLATFORM_ROOT / "IPlatformService.cs",
    PLATFORM_ROOT / "CanonicalPlatformProfiles.cs",
]

FORBIDDEN_PATTERNS = {
    "reward authority": r"\bGrantReward\b",
    "damage authority": r"\bApplyDamage\b",
    "direct HP mutation": r"\bCurrentHp\s*=",
    "Unity RNG entropy": r"\bUnityEngine\.Random\b",
    "system RNG entropy": r"\bSystem\.Random\b",
}


def fail(message: str) -> None:
    print(f"PLATFORM CONTRACT ERROR: {message}", file=sys.stderr)
    raise SystemExit(1)


def require_file(path: Path) -> str:
    if not path.is_file():
        fail(f"missing required file: {path.relative_to(ROOT)}")
    return path.read_text()


def main() -> None:
    for path in REQUIRED_FILES:
        require_file(path)

    enum_text = require_file(PLATFORM_ROOT / "RuntimePlatformId.cs")
    for token in ("Unknown", "Windows", "Android", "IOS"):
        if not re.search(rf"\b{token}\b", enum_text):
            fail(f"RuntimePlatformId is missing {token}")

    capabilities_text = require_file(PLATFORM_ROOT / "PlatformCapabilities.cs")
    for field in (
        "HasTouch",
        "HasMouseKeyboard",
        "HasSystemBack",
        "RequiresSafeArea",
        "SupportsDesktopQuit",
    ):
        if f"public bool {field} {{ get; }}" not in capabilities_text:
            fail(f"PlatformCapabilities must expose read-only {field}")

    service_text = require_file(PLATFORM_ROOT / "IPlatformService.cs")
    for signature in (
        "RuntimePlatformId PlatformId { get; }",
        "PlatformCapabilities Capabilities { get; }",
    ):
        if signature not in service_text:
            fail(f"IPlatformService missing: {signature}")

    profiles_text = require_file(PLATFORM_ROOT / "CanonicalPlatformProfiles.cs")
    normalized = re.sub(r"\s+", "", profiles_text)
    expected_profiles = {
        "Windows": "Windows=newPlatformCapabilities(false,true,false,false,true)",
        "Android": "Android=newPlatformCapabilities(true,false,true,true,false)",
        "IOS": "IOS=newPlatformCapabilities(true,false,false,true,false)",
    }
    for platform, expected in expected_profiles.items():
        if expected not in normalized:
            fail(f"canonical {platform} capability profile does not match contract")

    for path in PLATFORM_ROOT.rglob("*.cs"):
        text = path.read_text()
        for description, pattern in FORBIDDEN_PATTERNS.items():
            if re.search(pattern, text):
                fail(f"{description} found in {path.relative_to(ROOT)}")

    print("Platform contracts: PASS (foundation/capabilities, no gameplay authority or RNG entropy)")


if __name__ == "__main__":
    main()
