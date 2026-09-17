# Phase 16 Windows Built-Player Smoke

- Evidence source SHA: `fd12eee99d294540b5006d87520728a6ee38fbf4`
- Unity editor: `6000.5.9f1 (b57deb96f08d)`
- Target: Windows x64
- Build output: `build/phase16/windows/ClickDungeon.exe`
- Unity build result: PASS (`Build Finished, Result: Success`)
- Artifact inspection: PASS
- Artifact report: `build/phase16/windows-artifact.json`
- Built-player smoke: PASS
- Player exit code: 0
- Smoke report: `build/phase16/windows-smoke.json`
- Player log: `build/phase16/windows-player.log`

Ordered runtime markers observed:

1. `CD_SMOKE_BOOT product=ClickDungeon`
2. `CD_SMOKE_MAIN_MENU`
3. `CD_SMOKE_START_GAME`
4. `CD_SMOKE_DUNGEON_READY`
5. `CD_SMOKE_COMPLETE`

The smoke runner enforces a 45-second timeout, rejects missing or out-of-order markers, and exits nonzero on player failure.
The artifact inspection report is structural only and does not claim Windows code signing.
The repository had no tracked-file mutation after the build or built-player smoke.
