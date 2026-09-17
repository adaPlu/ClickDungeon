from pathlib import Path
import json
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[2]
SCRIPT = ROOT / "scripts/inspect_release_artifacts.py"
SHA = "a" * 40


def run_inspector(tmp_path, target, artifact_root):
    out = tmp_path / f"{target}.json"
    result = subprocess.run(
        [sys.executable, str(SCRIPT), "--target", target,
         "--root", str(artifact_root), "--sha", SHA,
         "--out", str(out)],
        cwd=ROOT,
        capture_output=True,
        text=True,
    )
    report = json.loads(out.read_text()) if out.is_file() else None
    return result, report


def test_windows_requires_exe_and_data_directory(tmp_path):
    artifacts = tmp_path / "windows"
    artifacts.mkdir()
    (artifacts / "ClickDungeon.exe").write_bytes(b"MZ")
    (artifacts / "ClickDungeon_Data").mkdir()
    result, report = run_inspector(tmp_path, "windows", artifacts)
    assert result.returncode == 0
    assert report["status"] == "PASS"
    assert report["sha"] == SHA
    assert report["target"] == "windows"


def test_missing_windows_data_is_fail_not_blocked(tmp_path):
    artifacts = tmp_path / "windows"
    artifacts.mkdir()
    (artifacts / "ClickDungeon.exe").write_bytes(b"MZ")
    result, report = run_inspector(tmp_path, "windows", artifacts)
    assert result.returncode != 0
    assert report["status"] == "FAIL"
    assert "BLOCKED" not in json.dumps(report)


def test_android_apk_and_aab_paths_are_exact(tmp_path):
    apk = tmp_path / "apk"
    apk.mkdir()
    (apk / "ClickDungeon.apk").write_bytes(b"PK")
    result, report = run_inspector(tmp_path, "android-apk", apk)
    assert result.returncode == 0 and report["status"] == "PASS"

    aab = tmp_path / "aab"
    aab.mkdir()
    (aab / "ClickDungeon.aab").write_bytes(b"PK")
    result, report = run_inspector(tmp_path, "android-aab", aab)
    assert result.returncode == 0 and report["status"] == "PASS"


def test_ios_requires_xcode_project_and_classes(tmp_path):
    artifacts = tmp_path / "ios"
    (artifacts / "Unity-iPhone.xcodeproj").mkdir(parents=True)
    (artifacts / "Unity-iPhone.xcodeproj/project.pbxproj").write_text("// ClickDungeon")
    (artifacts / "Classes").mkdir()
    result, report = run_inspector(tmp_path, "ios", artifacts)
    assert result.returncode == 0
    assert report["status"] == "PASS"


def test_forbidden_clickdungeon2_filename_fails(tmp_path):
    artifacts = tmp_path / "windows"
    artifacts.mkdir()
    (artifacts / "ClickDungeon.exe").write_bytes(b"MZ")
    (artifacts / "ClickDungeon_Data").mkdir()
    (artifacts / "ClickDungeon2-debug.txt").write_text("legacy")
    result, report = run_inspector(tmp_path, "windows", artifacts)
    assert result.returncode != 0
    assert report["status"] == "FAIL"
    assert any("ClickDungeon2" in finding for finding in report["findings"])


def test_release_build_entry_points_and_platform_contracts_exist():
    path = ROOT / "Assets/ClickDungeon/Editor/ReleaseBuild.cs"
    assert path.is_file(), "missing ReleaseBuild.cs"
    text = path.read_text()
    for token in (
        "public static void BuildWindows()",
        "public static void BuildAndroidApk()",
        "public static void BuildAndroidAab()",
        "public static void ExportIos()",
        "BuildTarget.StandaloneWindows64",
        "AndroidArchitecture.ARM64",
        "ScriptingImplementation.IL2CPP",
        "EditorUserBuildSettings.buildAppBundle = false",
        "EditorUserBuildSettings.buildAppBundle = true",
        "PlayerSettings.productName = ProductBrand.PlayerFacingName",
        "BuildOptions.None",
    ):
        assert token in text
    for forbidden in ("keystorePass", "keyaliasPass", "ClickDungeon2"):
        assert forbidden not in text


def test_editor_assembly_references_release_build_dependencies():
    path = ROOT / "Assets/ClickDungeon/Editor/ClickDungeon.Editor.asmdef"
    data = json.loads(path.read_text())
    assert data["references"] == [
        "ClickDungeon.Runtime",
        "ClickDungeon.Core",
        "ClickDungeon.Platform",
    ]
