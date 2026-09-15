from pathlib import Path
import os
import shutil
import subprocess

ROOT = Path(__file__).resolve().parents[2]
PROJECT = ROOT / "scripts" / "bot" / "ClickDungeon.BotSoak.csproj"


def test_engine_free_bot_project_exists():
    assert PROJECT.is_file(), "missing engine-free bot soak project"


def test_engine_free_bot_plays_many_seeded_runs_against_production_csharp():
    dotnet = shutil.which("dotnet")
    assert dotnet, "dotnet SDK is required for the engine-free C# bot soak"

    result = subprocess.run(
        [dotnet, "run", "--project", str(PROJECT), "--configuration", "Release", "--", "--runs", "50"],
        cwd=ROOT,
        text=True,
        capture_output=True,
        timeout=120,
        env={**os.environ, "DOTNET_NOLOGO": "1", "DOTNET_CLI_TELEMETRY_OPTOUT": "1"},
    )

    assert result.returncode == 0, (
        "bot soak failed\nSTDOUT:\n" + result.stdout + "\nSTDERR:\n" + result.stderr
    )
    assert "BOT_SOAK_PASS runs=50" in result.stdout
    assert "determinism=PASS" in result.stdout
    assert "reward_idempotency=PASS" in result.stdout
    assert "chest_idempotency=PASS" in result.stdout
