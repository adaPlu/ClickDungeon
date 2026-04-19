#!/bin/bash
# Validates that required Play Console store assets are present
PASS=0; FAIL=0
check() { if [ -f "$1" ]; then echo "PASS: $1"; ((PASS++)); else echo "FAIL: $1 MISSING"; ((FAIL++)); fi; }
check "docs/PRIVACY_POLICY.md"
check "docs/STORE_COPY.md"
check "docs/screenshots/SCREENSHOT_SPEC.md"
check "docs/FEATURE_GRAPHIC_SPEC.md"
check "app/src/main/res/mipmap-xxxhdpi/ic_launcher.png"
check "app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png"
echo ""; echo "Results: $PASS passed, $FAIL failed"
[ $FAIL -eq 0 ] && exit 0 || exit 1
