from pathlib import Path
import os
import re
import shutil
import subprocess

ROOT = Path(__file__).resolve().parents[2]
PROJECT = ROOT / "scripts" / "bot" / "ClickDungeon.BotSoak.csproj"


def run_bot(runs):
    dotnet = shutil.which("dotnet")
    assert dotnet, "dotnet SDK is required for the engine-free C# bot soak"
    return subprocess.run(
        [dotnet, "run", "--project", str(PROJECT), "--configuration", "Release", "--", "--runs", str(runs)],
        cwd=ROOT,
        text=True,
        capture_output=True,
        timeout=120,
        env={**os.environ, "DOTNET_NOLOGO": "1", "DOTNET_CLI_TELEMETRY_OPTOUT": "1"},
    )


def test_engine_free_bot_project_exists():
    assert PROJECT.is_file(), "missing engine-free bot soak project"


def test_engine_free_bot_plays_many_seeded_runs_against_production_csharp():
    result = run_bot(50)

    assert result.returncode == 0, (
        "bot soak failed\nSTDOUT:\n" + result.stdout + "\nSTDERR:\n" + result.stderr
    )
    assert "BOT_SOAK_PASS runs=50" in result.stdout
    assert "determinism=PASS" in result.stdout
    assert "reward_idempotency=PASS" in result.stdout
    assert "chest_idempotency=PASS" in result.stdout


def test_bot_soak_reports_encounter_room_invariants():
    result = run_bot(50)
    assert result.returncode == 0, result.stdout + result.stderr
    line = result.stdout.strip().splitlines()[-1]
    for token in (
        "BOT_SOAK_PASS", "rooms=", "closed=", "locked=", "room_rewards=",
        "determinism=PASS", "room_idempotency=PASS",
    ):
        assert token in line

    values = {
        name: int(value)
        for name, value in re.findall(r"\b(rooms|closed|locked|room_rewards)=(\d+)\b", line)
    }
    assert values["rooms"] > 0
    assert values["closed"] > 0
    assert values["locked"] > 0
    assert values["room_rewards"] > 0
