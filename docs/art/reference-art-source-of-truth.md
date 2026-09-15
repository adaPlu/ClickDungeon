# ClickDungeon Reference Art Source-of-Truth Policy

**Status:** Approved 2026-09-15  
**Applies to:** all player-facing ClickDungeon implementation work  
**Canonical reference set:** `docs/reference/latest/`

## Authority

The approved reference imagery is the **primary product source of truth** for how ClickDungeon should look and, where the imagery depicts interaction or state progression, how the corresponding in-game feature should function.

Implementation precedence is:

1. approved reference art;
2. approved gameplay/system contracts;
3. the current implementation.

Existing code is not evidence that a visual or interaction mismatch is acceptable. When the implementation conflicts with the approved imagery, the implementation is the repair target unless one of the explicit exceptions below applies.

## Explicit exceptions

- **Branding:** the shipping/player-facing title is `ClickDungeon`. Any legacy/reference occurrence of `ClickDungeon2` is non-authoritative text and must not appear in runtime UI.
- **Deterministic authority:** reference imagery may describe presentation and intended interaction, but simulation, combat, rewards, progression, RNG, and canonical-save authority remain in their approved deterministic layers.
- **Platform/accessibility constraints:** safe-area, input, accessibility, and platform requirements may reflow presentation while preserving visual hierarchy and function.
- **Scope:** imagery does not automatically pull every depicted future feature into the current phase. When a depicted subsystem is implemented, its corresponding reference becomes binding for that subsystem.

## Core fidelity requirements

The following are implementation contracts, not mood-board suggestions:

- **Title/main menu:** preserve the dominant hero showcase, strong ClickDungeon branding, Continue/Play hierarchy, Daily Reward presence, and access to Hero Select, Inventory, Talents, Shop, Settings, and desktop Quit where supported.
- **Core gameplay:** preserve the readable 5×5 dungeon board, top HUD identity/resources, bottom action row, stone-room framing, and clear tile boundaries.
- **Dungeon semantics:** traps, keys, doors, stairs, chests, hazards, teleports, fountains, terrain, and overlays must remain visually distinguishable and match their simulation meaning.
- **Heroes:** preserve hero identity, class silhouette, portrait/roster/gameplay hierarchy, equipment affinity, and readable idle/attack/hit/victory/defeat states.
- **Monsters/bosses:** preserve strong silhouettes, class/threat readability, attack telegraphing, and state readability.
- **Items/equipment:** preserve category silhouettes, rarity framing, equipment readability, and chest-reward visual language.
- **Chest flow:** preserve the depicted sequence: chest found → repeated-click/open progress → special-key gate when applicable → reward burst/loot reveal.

## Art-fidelity completion gate

A player-facing task is not complete until all three checks pass for the affected surface:

1. **Composition:** hierarchy/layout/silhouette matches the applicable canonical reference closely enough to read as the same product design.
2. **Function:** depicted controls, states, and transitions that are in scope actually work; decorative lookalikes do not count.
3. **Asset fidelity:** approved production assets are used when available; placeholders or generic substitutes are rejected unless the task explicitly documents why no approved asset exists.

The machine-readable companion contract is `docs/reference/latest/implementation-contract.json`. Tests and validators may enforce this policy, but the reference images themselves remain the primary visual authority.
