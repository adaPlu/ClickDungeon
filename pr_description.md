Summary
This PR adds Play Store marketing copy, a draft privacy policy, a README describing required store assets, and a small PowerShell helper to prepare assets for Play Console submission. This is PR-A for launch Gate 4. No runtime app code is changed.

Files
- docs/STORE_COPY.md
- docs/PRIVACY_POLICY.md
- docs/store_assets/README.md
- scripts/prepare_store_assets.ps1

Why
These docs/scripts centralize the copy and asset requirements needed to open the Play Console listing and hand off to design/legal.

Testing / Validation
- Local unit tests and build verification should be run before merging (see checklist).
- The helper script is intended as a convenience for Windows designers; verify outputs on a Win machine.

Checklist
- [ ] **Docs:** Review store copy in docs/STORE_COPY.md
- [ ] **Privacy:** Legal review & approve docs/PRIVACY_POLICY.md
- [ ] **Design:** Attach finalized screenshots / promotional assets and confirm names/sizes in docs/store_assets/README.md
- [ ] **Script:** Run scripts/prepare_store_assets.ps1 and confirm expected output (zip or folder)
- [ ] **Tests:** Run ./gradlew :app:testDebugUnitTest and confirm green
- [ ] **Lint/A11y:** Run ./gradlew :app:lint and fix any high-severity findings
- [ ] **No runtime changes:** Confirm this PR contains docs/scripts only
- [ ] **Release notes:** Add Play Console release notes + AAB script in follow-up PR (option C)
- [ ] **Approvals:** PM, Legal/Privacy, UX/Design sign-off
- [ ] **Merge:** Squash & merge after CI and approvals

Suggested reviewers
- Product/PM, Legal / Privacy, UX / Design

Suggested labels
- docs, release-prep, no-runtime-change

Optional: create the PR via GitHub CLI

gh pr create --base main --head scaffold/pr-store-assets --title "docs(store): scaffold Play Store copy, privacy policy, and store-assets README (PR-A)" --body-file pr_description.md --label "docs,release-prep"
