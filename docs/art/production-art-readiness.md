# Production Art Readiness

`art/production-art-manifest.json` is generated from canonical content definitions plus explicit UI, VFX, and chest presentation contracts.

Two validation modes exist:

- **Source mode** (`python scripts/validate_production_art.py`) validates manifest schema, deterministic generation, unique asset IDs, unique runtime paths, and allowed runtime folders. It does not claim that production sprites exist.
- **Strict mode** (`python scripts/validate_production_art.py --strict`) additionally requires every required runtime file and its matching Unity `.meta` file. This mode is expected to fail until isolated production sprites have been imported into Unity.

Reference composites remain under `docs/reference/latest/`. They must not be copied into runtime folders as fake stand-ins for isolated sprites.
