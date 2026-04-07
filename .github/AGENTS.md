# Agents

This file registers workspace-level agents for common workflows.

- `cloudsave-agent`: Scaffolds cloud save mock tests and run instructions. Applies to `app/src/online/**`.
- `billing-agent`: Scaffolds billing test doubles and expands `BillingManager` tests. Applies to `app/**`.
 - `code-reviewer`: Reviews code changes for correctness and maintainability. Applies to `app/**`.
 - `code-simplifier`: Simplifies overly complex code and suggests small refactors. Applies to `app/src/main/java/**`.
 - `security-reviewer`: Audits security issues around persistence, billing, and network code. Applies to `app/**`.
 - `tech-lead`: Provides architectural guidance and release-readiness checklists. Applies to `.github/**`.
 - `ux-reviewer`: Reviews UI and accessibility issues in `res/` and layout files. Applies to `app/src/main/res/**`.
