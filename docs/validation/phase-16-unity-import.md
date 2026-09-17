# Phase 16 Unity Import Evidence

## Tested state

- Branch: `new`
- Source SHA: `312684f445f0c2c311b20b42fc1ca8a2e49e4b36`
- Unity editor: `6000.5.9f1 (b57deb96f08d)`
- Host: Windows 11 Pro x64
- Product name: `ClickDungeon`

## Import command

The committed tree was reopened using the installed Unity 6 editor in batch mode:

```text
Unity.exe -batchmode -nographics -quit -projectPath C:\Users\plugu\ClickDungeon-current -logFile build\phase16\unity-import-clean.log
```

Remote execution supplied the standard Windows `PROGRAMDATA=C:\ProgramData` and `ALLUSERSPROFILE=C:\ProgramData` environment variables required by Unity Package Manager.
## Results

| Gate | Status | Evidence |
| --- | --- | --- |
| Unity metadata authenticity | PASS | `ProjectVersion.txt` identifies Unity 6000.5.9f1; Unity generated project/package state |
| Package resolution | PASS | `manifest.json` and `packages-lock.json` exist and parse with dependency dictionaries |
| Unity import | PASS | clean batch import exited with code 0 |
| Script compilation | PASS | Unity completed import with no compiler errors and exited successfully |
| Product branding | PASS | `ProjectSettings.asset` contains `productName: ClickDungeon` and no `ClickDungeon2` |
| Unity metadata validator | PASS | `scripts/validate_unity_project_contracts.py` |
| Unity project tests | PASS | `2 passed` in `test_unity_project_contracts.py` |
| Mutation guard | PASS | clean batch re-import produced no tracked changes |
| Strict production art | FAIL | next missing asset is `Assets/ClickDungeon/Art/Runtime/Chest/complete.png` |
| EditMode / PlayMode | BLOCKED | not run at this import-only gate |
| Player builds / signing | BLOCKED | later Phase-16 gates |

The strict-art failure is preserved as the next repository-controlled Phase-16 blocker; this import evidence does not weaken or bypass that requirement.
