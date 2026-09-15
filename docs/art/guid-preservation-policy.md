# Unity GUID Preservation Policy

Production runtime art lives under `Assets/ClickDungeon/Art/Runtime/`. Once Unity imports a production asset, its matching `.meta` file is part of the asset contract because the GUID inside that file is how scenes, prefabs, animation clips, and other serialized Unity assets retain references.

When replacing artwork for an existing canonical asset, preserve the existing `.meta` file and therefore preserve its GUID whenever the logical asset identity has not changed. When the logical identity is unchanged, replace only the image payload unless the asset is intentionally being retired and migrated. Do not delete and regenerate `.meta` files as a convenience step.

If a canonical asset is renamed or moved, perform the move through Unity or move the file and its `.meta` together so the GUID remains stable. Any deliberate GUID migration must be documented and all serialized references must be validated before merge.

Composite reference sheets under `docs/reference/` are specifications, not runtime sprites. They never satisfy the production-art gate by themselves.
