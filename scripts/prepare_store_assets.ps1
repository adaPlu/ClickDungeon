*** Begin Patch
*** Add File: scripts/prepare_store_assets.ps1
+<#
+Simple validator for expected store asset files.
+Run from repository root: ./scripts/prepare_store_assets.ps1
+Exits non-zero when required files are missing.
+#>
+
+ = @()
+
+function check() {
+    if (-not (Test-Path )) {  +=  }
+}
+
+check  docs/store_assets/ic_launcher_foreground.png
+check docs/store_assets/ic_launcher_background.png
+check docs/store_assets/feature_graphic.png
+check docs/store_assets/screenshots/screenshot_1.png
+check docs/store_assets/screenshots/screenshot_2.png
+
+if (.Count -gt 0) {
+    Write-Host Missing store asset files: -ForegroundColor Yellow
+     | ForEach-Object { Write-Host  -  }
+    exit 2
+}
+
+Write-Host All required store assets present. -ForegroundColor Green
+exit 0
+
*** End Patch

