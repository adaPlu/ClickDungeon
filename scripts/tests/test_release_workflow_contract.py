from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[2]
WORKFLOW = ROOT / ".github/workflows/release-validation.yml"


def workflow_text():
    assert WORKFLOW.is_file(), "missing release-validation.yml"
    return WORKFLOW.read_text()


def test_release_workflow_has_required_jobs_and_dependencies():
    text = workflow_text()
    for job in (
        "source-contracts:",
        "unity-tests:",
        "windows-release:",
        "android-release:",
        "ios-export:",
        "release-evidence:",
    ):
        assert job in text
    assert "needs: source-contracts" in text
    assert text.count("needs: unity-tests") >= 3
    assert "needs: [source-contracts, unity-tests, windows-release, android-release, ios-export]" in text


def test_release_workflow_has_evidence_and_mutation_guards():
    text = workflow_text()
    assert "actions/upload-artifact@v4" in text
    for token in (
        "ProjectSettings/ProjectVersion.txt",
        "git status --porcelain=v1",
        "git diff --exit-code",
        "git diff --check",
        "editmode-results.xml",
        "playmode-results.xml",
        "windows-smoke.json",
        "windows-artifact.json",
        "android-apk-evidence.json",
        "android-aab-evidence.json",
        "ios-evidence.json",
    ):
        assert token in text


def test_release_workflow_uses_secret_references_not_literal_credentials():
    text = workflow_text()
    assert "secrets.UNITY_LICENSE" in text
    forbidden = (
        "-----BEGIN PRIVATE KEY-----",
        "-----BEGIN CERTIFICATE-----",
        "keystorePass:",
        "keyaliasPass:",
        "provisioningProfile:",
    )
    for token in forbidden:
        assert token not in text
    assert not re.search(r"(?m)^\s*UNITY_LICENSE:\s*(?!\$\{\{\s*secrets\.)\S+", text)
