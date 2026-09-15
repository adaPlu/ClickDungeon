from pathlib import Path
import json
import sys

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "scripts"))

from release_evidence import GateResult, GateStatus, ReleaseEvidenceReport


def test_gate_status_vocabulary_is_exact():
    assert [s.value for s in GateStatus] == ["PASS", "FAIL", "BLOCKED"]


def test_report_round_trips_exact_sha_and_gate_results(tmp_path):
    report = ReleaseEvidenceReport(
        head_sha="0123456789abcdef0123456789abcdef01234567",
        gates=[
            GateResult("source", GateStatus.PASS, "source contracts green"),
            GateResult("unity_compile", GateStatus.BLOCKED, "Unity evidence unavailable"),
        ],
    )
    target = tmp_path / "evidence.json"
    report.write_json(target)
    payload = json.loads(target.read_text())
    assert payload["head_sha"] == report.head_sha
    assert payload["gates"][0]["status"] == "PASS"
    assert payload["gates"][1]["status"] == "BLOCKED"


def test_report_rejects_non_full_sha():
    try:
        ReleaseEvidenceReport(head_sha="abc123", gates=[])
    except ValueError as exc:
        assert "40-character" in str(exc)
    else:
        raise AssertionError("short SHA must be rejected")


def test_any_fail_makes_report_unsuccessful_but_blocked_does_not_become_pass():
    blocked = ReleaseEvidenceReport(
        "0123456789abcdef0123456789abcdef01234567",
        [GateResult("signing", GateStatus.BLOCKED, "external credential required")],
    )
    failed = ReleaseEvidenceReport(
        "0123456789abcdef0123456789abcdef01234567",
        [GateResult("runtime", GateStatus.FAIL, "Dungeon Ready not reached")],
    )
    assert blocked.has_failures is False
    assert blocked.is_release_clean is False
    assert failed.has_failures is True
    assert failed.is_release_clean is False
