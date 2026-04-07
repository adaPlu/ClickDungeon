*** Begin Patch
*** Add File: docs/store_assets/README.md
+# Store Assets — placement and naming
+
+Drop artwork here before creating PR A. This directory documents the
+assets needed for the Play Store submission and the expected file names.
+
+Required files (examples):
+- ic_launcher_foreground.png — foreground layer (1024×1024 recommended)
+- ic_launcher_background.png — background layer (1024×1024 recommended)
+- eature_graphic.png — feature graphic (1024×500)
+- screenshots/screenshot_1.png — phone screenshot (1080×1920)
+- screenshots/screenshot_2.png
+
+Per-density launcher files (optional if using adaptive icon):
+- mipmap-mdpi/ic_launcher.png (48×48)
+- mipmap-hdpi/ic_launcher.png (72×72)
+- mipmap-xhdpi/ic_launcher.png (96×96)
+- mipmap-xxhdpi/ic_launcher.png (144×144)
+- mipmap-xxxhdpi/ic_launcher.png (192×192)
+
+Notes:
+- Prefer adaptive icon layers (ic_launcher_foreground + ic_launcher_background).
+- Keep originals in a separate design/ folder (not committed) for source PSD/AI files.
+- Use consistent naming and include a short README in the PR describing who provided the assets.
+
*** End Patch

