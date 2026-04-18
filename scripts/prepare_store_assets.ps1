<#
Validates that the expected Play Store asset files are present.

Run from the repository root:
  ./scripts/prepare_store_assets.ps1

This script reports missing files and exits non-zero when required assets are
not yet available.
#>

$requiredFiles = @(
    "docs/store_assets/ic_launcher_foreground.png",
    "docs/store_assets/ic_launcher_background.png",
    "docs/store_assets/feature_graphic.png",
    "docs/store_assets/screenshots/screenshot_1.png",
    "docs/store_assets/screenshots/screenshot_2.png"
)

$missingFiles = @()

foreach ($path in $requiredFiles) {
    if (-not (Test-Path $path)) {
        $missingFiles += $path
    }
}

if ($missingFiles.Count -gt 0) {
    Write-Host "Missing store asset files:" -ForegroundColor Yellow
    foreach ($path in $missingFiles) {
        Write-Host " - $path"
    }
    exit 2
}

Write-Host "All required store assets are present." -ForegroundColor Green
exit 0
