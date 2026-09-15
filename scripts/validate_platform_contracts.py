#!/usr/bin/env python3
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
PLATFORM_ROOT = ROOT / "Assets/ClickDungeon/Platform"
INPUT_ROOT = PLATFORM_ROOT / "Input"
LIFECYCLE_ROOT = PLATFORM_ROOT / "Lifecycle"

REQUIRED_FILES = [
    PLATFORM_ROOT / "ClickDungeon.Platform.asmdef",
    PLATFORM_ROOT / "RuntimePlatformId.cs",
    PLATFORM_ROOT / "PlatformCapabilities.cs",
    PLATFORM_ROOT / "IPlatformService.cs",
    PLATFORM_ROOT / "CanonicalPlatformProfiles.cs",
]

INPUT_FILES = [
    INPUT_ROOT / "PlatformInputAction.cs",
    INPUT_ROOT / "PlatformInputEvent.cs",
    INPUT_ROOT / "IPlayerInputAdapter.cs",
    INPUT_ROOT / "WindowsInputAdapter.cs",
    INPUT_ROOT / "TouchInputAdapter.cs",
]

LIFECYCLE_FILES = [
    LIFECYCLE_ROOT / "AppLifecycleEvent.cs",
    LIFECYCLE_ROOT / "PlatformLifecycleRequest.cs",
    LIFECYCLE_ROOT / "MobileLifecycleAdapter.cs",
    LIFECYCLE_ROOT / "LifecyclePersistenceCoordinator.cs",
]

FORBIDDEN_PATTERNS = {
    "reward authority": r"\bGrantReward\b",
    "damage authority": r"\bApplyDamage\b",
    "direct HP mutation": r"\bCurrentHp\s*=",
    "Unity RNG entropy": r"\bUnityEngine\.Random\b",
    "system RNG entropy": r"\bSystem\.Random\b",
}

INPUT_FORBIDDEN_PATTERNS = {
    "combat resolver authority": r"\bCombatResolver\b",
    "reward service authority": r"\bRewardGrantService\b",
    "damage resolution": r"\bApplyDamage\b",
    "direct HP mutation": r"\bCurrentHp\s*=",
}

LIFECYCLE_FORBIDDEN_PATTERNS = {
    "reward transaction authority": r"\bRewardTransaction\b",
    "combat resolver authority": r"\bCombatResolver\b",
    "damage resolution": r"\bApplyDamage\b",
    "direct HP mutation": r"\bCurrentHp\s*=",
    "profile save DTO coupling": r"\bProfileSave\b",
    "run save DTO coupling": r"\bRunSave\b",
    "save repository coupling": r"\bSaveRepository\b",
    "direct process termination": r"\bApplication\.Quit\b",
}


def fail(message: str) -> None:
    print(f"PLATFORM CONTRACT ERROR: {message}", file=sys.stderr)
    raise SystemExit(1)


def require_file(path: Path) -> str:
    if not path.is_file():
        fail(f"missing required file: {path.relative_to(ROOT)}")
    return path.read_text()


def main() -> None:
    for path in REQUIRED_FILES + INPUT_FILES + LIFECYCLE_FILES:
        require_file(path)

    asmdef_text = require_file(PLATFORM_ROOT / "ClickDungeon.Platform.asmdef")
    for assembly in ("ClickDungeon.Application", "ClickDungeon.Core", "ClickDungeon.Dungeon"):
        if assembly not in asmdef_text:
            fail(f"platform assembly must reference {assembly}")

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

    actions_text = require_file(INPUT_ROOT / "PlatformInputAction.cs")
    for action in (
        "MoveUp",
        "MoveDown",
        "MoveLeft",
        "MoveRight",
        "SelectCell",
        "ActivateAction",
        "Interact",
        "OpenInventory",
        "PauseOrCancel",
    ):
        if not re.search(rf"\b{action}\b", actions_text):
            fail(f"PlatformInputAction is missing {action}")

    interface_text = require_file(INPUT_ROOT / "IPlayerInputAdapter.cs")
    if "TryTranslate" not in interface_text or "PlayerCommand" not in interface_text:
        fail("IPlayerInputAdapter must translate platform events to PlayerCommand")

    for adapter_name in ("WindowsInputAdapter.cs", "TouchInputAdapter.cs"):
        adapter_path = INPUT_ROOT / adapter_name
        adapter_text = require_file(adapter_path)
        if "PlayerCommand" not in adapter_text:
            fail(f"{adapter_name} must target PlayerCommand")
        for description, pattern in INPUT_FORBIDDEN_PATTERNS.items():
            if re.search(pattern, adapter_text):
                fail(f"{description} found in {adapter_path.relative_to(ROOT)}")

    lifecycle_events = require_file(LIFECYCLE_ROOT / "AppLifecycleEvent.cs")
    for event_name in ("Paused", "Backgrounded", "Resumed", "SystemBack"):
        if not re.search(rf"\b{event_name}\b", lifecycle_events):
            fail(f"AppLifecycleEvent is missing {event_name}")

    lifecycle_requests = require_file(LIFECYCLE_ROOT / "PlatformLifecycleRequest.cs")
    for request_name in ("None", "AutosaveCheckpoint", "PauseOrCancel"):
        if not re.search(rf"\b{request_name}\b", lifecycle_requests):
            fail(f"PlatformLifecycleRequest is missing {request_name}")

    lifecycle_adapter = require_file(LIFECYCLE_ROOT / "MobileLifecycleAdapter.cs")
    for token in ("Paused", "Backgrounded", "Resumed", "SystemBack", "AutosaveCheckpoint", "PauseOrCancel"):
        if token not in lifecycle_adapter:
            fail(f"MobileLifecycleAdapter does not route {token}")

    lifecycle_coordinator = require_file(LIFECYCLE_ROOT / "LifecyclePersistenceCoordinator.cs")
    if "GameSessionPersistenceOrchestrator" not in lifecycle_coordinator:
        fail("LifecyclePersistenceCoordinator must use the existing application persistence boundary")
    if "OnLifecyclePauseOrBackground" not in lifecycle_coordinator:
        fail("LifecyclePersistenceCoordinator must request the lifecycle autosave checkpoint")

    for path in (LIFECYCLE_ROOT / "MobileLifecycleAdapter.cs", LIFECYCLE_ROOT / "LifecyclePersistenceCoordinator.cs"):
        text = require_file(path)
        for description, pattern in LIFECYCLE_FORBIDDEN_PATTERNS.items():
            if re.search(pattern, text):
                fail(f"{description} found in {path.relative_to(ROOT)}")

    for path in PLATFORM_ROOT.rglob("*.cs"):
        text = path.read_text()
        for description, pattern in FORBIDDEN_PATTERNS.items():
            if re.search(pattern, text):
                fail(f"{description} found in {path.relative_to(ROOT)}")

    print("Platform contracts: PASS (capabilities/input/lifecycle, no gameplay authority or RNG entropy)")


if __name__ == "__main__":
    main()
