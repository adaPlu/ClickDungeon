from pathlib import Path
import json
import re

ROOT = Path(__file__).resolve().parents[2]


def test_authentic_unity_project_metadata_exists_and_targets_unity_6():
    version = ROOT / "ProjectSettings/ProjectVersion.txt"
    manifest = ROOT / "Packages/manifest.json"
    lock = ROOT / "Packages/packages-lock.json"
    assert version.is_file()
    assert manifest.is_file()
    assert lock.is_file()
    text = version.read_text()
    match = re.search(r"m_EditorVersion:\s*(\S+)", text)
    assert match, text
    assert match.group(1).startswith("6000."), match.group(1)
    assert isinstance(json.loads(manifest.read_text()).get("dependencies"), dict)
    assert isinstance(json.loads(lock.read_text()).get("dependencies"), dict)


def test_project_name_and_company_do_not_reintroduce_clickdungeon2_runtime_branding():
    settings = (ROOT / "ProjectSettings/ProjectSettings.asset").read_text()
    assert "productName: ClickDungeon" in settings
    assert "ClickDungeon2" not in settings
