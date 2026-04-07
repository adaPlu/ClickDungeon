*** Begin Patch
*** Add File: docs/PRIVACY_POLICY.md
+# Privacy Policy — ClickDungeon (draft)
+
+Effective date: 2026-04-07
+
+Summary
+ClickDungeon is an offline-first mobile game. We do not collect personal
+information from players by default. Local save data is stored on-device
+and protected using encryption and integrity checks.
+
+Data We Collect
+- Local save data: encrypted game save slots (no external transfer by default).
+- Analytics & crash reports: optional Crashlytics/Analytics may be enabled
+  to help diagnose crashes. These services may collect device identifiers
+  and non-personal diagnostic data.
+
+How We Use Data
+- Saves: stored locally to persist player progress.
+- Crash reports: used to improve app stability. No PII is intentionally
+  collected or transmitted.
+
+Third-Party Services
+- Firebase Crashlytics and Firebase Analytics may be used for crash
+  reporting and aggregated usage statistics. See their privacy policies
+  for details.
+
+Security
+Local save data is encrypted (Tink + Android Keystore) and protected with
+an HMAC. Keys are stored in platform-provided secure storage where available.
+
+Children
+This app is not directed to children under 13. If you believe your child has
+provided personal information, please contact us.
+
+Contact
+For privacy questions contact: privacy@yourdomain.example
+
+Hosting the policy
+- To host this policy on GitHub Pages, create docs/PRIVACY_POLICY.md and
+  enable GitHub Pages to serve the docs/ folder. Use the resulting URL
+  in the Play Console.
+
*** End Patch

