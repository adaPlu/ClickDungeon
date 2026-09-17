from pathlib import Path
import json
import re
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[1]
errors = []


def require(condition, message):
    if not condition:
        errors.append(message)


version_path = ROOT / "ProjectSettings/ProjectVersion.txt"
settings_path = ROOT / "ProjectSettings/ProjectSettings.asset"
manifest_path = ROOT / "Packages/manifest.json"
lock_path = ROOT / "Packages/packages-lock.json"

require(version_path.is_file(), f"missing {version_path.relative_to(ROOT)}")
require(settings_path.is_file(), f"missing {settings_path.relative_to(ROOT)}")
require(manifest_path.is_file(), f"missing {manifest_path.relative_to(ROOT)}")
require(lock_path.is_file(), f"missing {lock_path.relative_to(ROOT)}")

if version_path.is_file():
    match = re.search(r"m_EditorVersion:\s*(\S+)", version_path.read_text())
    require(bool(match), "ProjectVersion.txt has no m_EditorVersion")
    if match:
        require(match.group(1).startswith("6000."), f"not Unity 6: {match.group(1)}")
if settings_path.is_file():
    settings = settings_path.read_text()
    require("productName: ClickDungeon" in settings, "productName is not ClickDungeon")
    require("ClickDungeon2" not in settings, "forbidden ClickDungeon2 branding in ProjectSettings")

for package_path in (manifest_path, lock_path):
    if package_path.is_file():
        try:
            payload = json.loads(package_path.read_text())
        except json.JSONDecodeError as exc:
            errors.append(f"invalid JSON in {package_path.relative_to(ROOT)}: {exc}")
        else:
            require(isinstance(payload.get("dependencies"), dict),
                    f"{package_path.relative_to(ROOT)} has no dependency dictionary")

assets = ROOT / "Assets"
if assets.is_dir():
    for path in assets.rglob("*"):
        if path.is_file() and path.suffix != ".meta" and not path.name.startswith("."):
            meta = path.with_name(path.name + ".meta")
            require(meta.is_file(), f"missing Unity meta: {meta.relative_to(ROOT)}")

clickdungeon = assets / "ClickDungeon"
if clickdungeon.is_dir():
    for directory in [clickdungeon, *[p for p in clickdungeon.rglob("*") if p.is_dir()]]:
        meta = Path(str(directory) + ".meta")
        require(meta.is_file(), f"missing Unity directory meta: {meta.relative_to(ROOT)}")
for transient in ("Library", "Logs", "Temp", "obj", "UserSettings"):
    result = subprocess.run(
        ["git", "ls-files", transient],
        cwd=ROOT,
        text=True,
        capture_output=True,
        check=False,
    )
    require(not result.stdout.strip(), f"tracked transient Unity path: {transient}")

if errors:
    for error in errors:
        print(f"unity-project-error: {error}")
    sys.exit(1)

print("unity project contracts: PASS")
