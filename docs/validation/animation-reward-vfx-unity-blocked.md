# Animation / Chest Rewards / VFX — Unity Validation Boundary

The Unity-dependent validation boundary is **blocked** in this execution environment because no Unity editor is installed. Unity compilation, EditMode, PlayMode, scene playback, player builds, and strict production art import checks are therefore **not claimed** as passing here.

The source-contract boundary is still testable without Unity. It verifies deterministic chest transaction IDs, exactly-once reward commitment before presentation, the eight chest presentation phases, the four approved visual beats from the reference art, input-lock semantics, and the rule that Presentation has no gameplay reward authority.

The **strict production art** gate remains separately blocked until isolated runtime sprites and their Unity `.meta` files exist. Composite reference sheets are design authority only and are not treated as runtime production sprites.
