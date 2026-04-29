# Store Assets

This folder documents the expected asset names for Play Store submission work.
It is a staging area for release-prep assets, not a source-art archive.

## Expected Files
- `ic_launcher_foreground.png`
- `ic_launcher_background.png`
- `feature_graphic.png`
- `screenshots/screenshot_1.png`
- `screenshots/screenshot_2.png`

## Recommended Sizes
- `ic_launcher_foreground.png`: 1024x1024
- `ic_launcher_background.png`: 1024x1024
- `feature_graphic.png`: 1024x500
- screenshots: phone portrait captures sized for Play Console upload

## Feature Graphic
- Generated artifact: `feature_graphic.png`
- Source process: run `./scripts/generate_feature_graphic.ps1` from the repo root.
- Source art: committed dungeon/class art under `app/src/main/res/drawable-nodpi/`.
- Validation: run `bash scripts/validate_store_assets.sh` from the repo root.

## Optional Density Exports
- `mipmap-mdpi/ic_launcher.png`
- `mipmap-hdpi/ic_launcher.png`
- `mipmap-xhdpi/ic_launcher.png`
- `mipmap-xxhdpi/ic_launcher.png`
- `mipmap-xxxhdpi/ic_launcher.png`

## Notes
- Prefer adaptive launcher icon layers when possible.
- Keep editable PSD/AI/source files outside the repo.
- Final asset review should confirm filenames, dimensions, and export quality.
