# Phase 16 Artifact Contract

Release artifact structural validation is independent from signing, device, and store readiness.
A structural PASS means the expected unsigned build/export files exist, match the canonical ClickDungeon branding, and are tied to the supplied exact 40-character commit SHA.
It does not imply that any artifact is signed, installable on a protected device, uploaded to a store, or production-approved by a platform provider.

Expected output roots are:

- Windows x64: `build/phase16/windows/ClickDungeon.exe` plus `build/phase16/windows/ClickDungeon_Data/`
- Android APK: `build/phase16/android-apk/ClickDungeon.apk`
- Android AAB: `build/phase16/android-aab/ClickDungeon.aab`
- iOS Xcode export: `build/phase16/ios/Unity-iPhone.xcodeproj/project.pbxproj` plus `build/phase16/ios/Classes/`

The artifact inspector reports schema version 1, exact SHA, target, PASS/FAIL, and concrete findings.
Missing structural artifacts are FAIL, never BLOCKED.
Forbidden `ClickDungeon2` branding in artifact paths or text-readable metadata is FAIL.
External signing credentials, provisioning profiles, device authorization, and store credentials are not committed or inferred by this contract.
