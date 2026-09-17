#!/usr/bin/env python3
from __future__ import annotations

from collections import deque
from hashlib import sha256
from pathlib import Path
import argparse
import json
import tempfile

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
CROP_MANIFEST = ROOT / "art/dungeon-tile-crops.json"
TILE_ROOT = Path("Assets/ClickDungeon/Art/Runtime/Tiles")
CHEST_ROOT = Path("Assets/ClickDungeon/Art/Runtime/Chest")
SIZE = 256
CONTENT_SIZE = 248
BACKGROUND_MAX = 24
EDGE_ALPHA_DEPTH = 10


def _edge_depth(x: int, y: int, width: int, height: int) -> int:
    return min(x, y, width - 1 - x, height - 1 - y)


def clear_edge_background(image: Image.Image) -> Image.Image:
    """Remove only near-black sheet background connected to the outer crop margin.

    The depth cap deliberately prevents dark gameplay pixels (pit interiors, open
    doorways, shadows, grout) from being mistaken for sheet background.
    """
    rgba = image.convert("RGBA")
    pixels = rgba.load()
    width, height = rgba.size
    queue = deque()
    for x in range(width):
        queue.append((x, 0))
        queue.append((x, height - 1))
    for y in range(height):
        queue.append((0, y))
        queue.append((width - 1, y))

    seen = set()
    while queue:
        x, y = queue.popleft()
        if x < 0 or y < 0 or x >= width or y >= height or (x, y) in seen:
            continue
        seen.add((x, y))
        if _edge_depth(x, y, width, height) > EDGE_ALPHA_DEPTH:
            continue
        r, g, b, a = pixels[x, y]
        if a == 0:
            queue.extend(((x - 1, y), (x + 1, y), (x, y - 1), (x, y + 1)))
            continue
        if max(r, g, b) > BACKGROUND_MAX:
            continue
        pixels[x, y] = (r, g, b, 0)
        queue.extend(((x - 1, y), (x + 1, y), (x, y - 1), (x, y + 1)))
    return rgba


def normalize(image: Image.Image) -> Image.Image:
    alpha = image.getchannel("A")
    bbox = alpha.getbbox()
    if bbox is None:
        raise ValueError("crop contains no visible tile pixels")
    trimmed = image.crop(bbox)
    trimmed.thumbnail((CONTENT_SIZE, CONTENT_SIZE), Image.Resampling.LANCZOS)
    canvas = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    canvas.alpha_composite(
        trimmed,
        ((SIZE - trimmed.width) // 2, (SIZE - trimmed.height) // 2),
    )
    return canvas


def _save_png(image: Image.Image, path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    image.save(path, format="PNG", optimize=False, compress_level=9)


def _digest(path: Path) -> str:
    return sha256(path.read_bytes()).hexdigest()


def extract_all(root: Path = ROOT, destination_root: Path | None = None) -> dict[str, str]:
    root = Path(root)
    destination_root = Path(destination_root) if destination_root is not None else root
    manifest_path = root / "art/dungeon-tile-crops.json"
    data = json.loads(manifest_path.read_text())
    if data.get("schema_version") != 1:
        raise ValueError("unsupported dungeon tile crop schema")

    sources = {}
    for key, relative_path in data["sources"].items():
        source_path = root / relative_path
        if not source_path.is_file():
            raise FileNotFoundError(f"missing tile source sheet: {relative_path}")
        sources[key] = Image.open(source_path).convert("RGBA")

    hashes: dict[str, str] = {}
    outputs = set()
    for entry in data["tiles"]:
        output_name = entry["output"]
        if output_name in outputs:
            raise ValueError(f"duplicate tile output: {output_name}")
        outputs.add(output_name)
        source_key = entry["source"]
        if source_key not in sources:
            raise ValueError(f"unknown tile source key: {source_key}")
        box = entry["box"]
        if not isinstance(box, list) or len(box) != 4:
            raise ValueError(f"invalid crop box for {output_name}")
        crop = sources[source_key].crop(tuple(int(value) for value in box))
        tile = normalize(clear_edge_background(crop))
        output_path = destination_root / TILE_ROOT / output_name
        _save_png(tile, output_path)
        hashes[output_name] = _digest(output_path)

    chest_roles = {
        "tile_chest_closed.png": "closed.png",
        "tile_chest_open.png": "open.png",
    }
    for tile_name, chest_name in chest_roles.items():
        source_path = destination_root / TILE_ROOT / tile_name
        if not source_path.is_file():
            raise ValueError(f"canonical chest tile missing from crop outputs: {tile_name}")
        chest_path = destination_root / CHEST_ROOT / chest_name
        chest_path.parent.mkdir(parents=True, exist_ok=True)
        chest_path.write_bytes(source_path.read_bytes())

    return hashes


def check_tracked_outputs(root: Path = ROOT) -> None:
    root = Path(root)
    data = json.loads((root / "art/dungeon-tile-crops.json").read_text())
    expected_names = [entry["output"] for entry in data["tiles"]]
    with tempfile.TemporaryDirectory(prefix="clickdungeon-tiles-") as temp_dir:
        generated_root = Path(temp_dir)
        extract_all(root, generated_root)
        relative_paths = [TILE_ROOT / name for name in expected_names]
        relative_paths.extend((CHEST_ROOT / "closed.png", CHEST_ROOT / "open.png"))
        for relative_path in relative_paths:
            tracked = root / relative_path
            generated = generated_root / relative_path
            if not tracked.is_file():
                raise SystemExit(f"dungeon-tile-extraction-error: missing tracked output: {relative_path}")
            if tracked.read_bytes() != generated.read_bytes():
                raise SystemExit(f"dungeon-tile-extraction-error: stale tracked output: {relative_path}")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--check",
        action="store_true",
        help="verify tracked runtime tiles exactly match deterministic extraction",
    )
    args = parser.parse_args()
    if args.check:
        check_tracked_outputs(ROOT)
        print("dungeon tile extraction: PASS (25 tiles + 2 chest roles)")
        return 0
    hashes = extract_all(ROOT)
    print(f"wrote {len(hashes)} canonical dungeon tiles and 2 chest role assets")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
