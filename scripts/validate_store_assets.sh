#!/bin/bash
# Validates that required Play Console store assets are present
PASS=0; FAIL=0
check() { if [ -f "$1" ]; then echo "PASS: $1"; ((PASS++)); else echo "FAIL: $1 MISSING"; ((FAIL++)); fi; }
check_png_rgb() {
  local path="$1"
  local expected_width="$2"
  local expected_height="$3"
  local output
  local python_bin

  if [ ! -f "$path" ]; then
    echo "FAIL: $path MISSING"
    ((FAIL++))
    return
  fi

  if command -v python3 >/dev/null 2>&1; then
    python_bin="python3"
  elif command -v python >/dev/null 2>&1; then
    python_bin="python"
  else
    echo "FAIL: $path Python is required to validate PNG dimensions"
    ((FAIL++))
    return
  fi

  if output=$("$python_bin" - "$path" "$expected_width" "$expected_height" <<'PY' 2>&1
import struct
import sys

path = sys.argv[1]
expected_width = int(sys.argv[2])
expected_height = int(sys.argv[3])

with open(path, "rb") as handle:
    if handle.read(8) != b"\x89PNG\r\n\x1a\n":
        raise SystemExit("not a PNG file")

    length_bytes = handle.read(4)
    chunk_type = handle.read(4)
    if len(length_bytes) != 4 or chunk_type != b"IHDR":
        raise SystemExit("missing PNG IHDR chunk")

    length = struct.unpack(">I", length_bytes)[0]
    data = handle.read(length)
    if length != 13 or len(data) != 13:
        raise SystemExit("invalid PNG IHDR chunk")

    width, height, bit_depth, color_type, _, _, _ = struct.unpack(">IIBBBBB", data)

if (width, height) != (expected_width, expected_height):
    raise SystemExit(
        "expected {0}x{1}, found {2}x{3}".format(
            expected_width,
            expected_height,
            width,
            height,
        )
    )

if bit_depth != 8 or color_type != 2:
    raise SystemExit(
        "expected 8-bit RGB PNG without alpha, found bit_depth={0} color_type={1}".format(
            bit_depth,
            color_type,
        )
    )
PY
  ); then
    echo "PASS: $path ${expected_width}x${expected_height} RGB PNG"
    ((PASS++))
  else
    echo "FAIL: $path $output"
    ((FAIL++))
  fi
}
check "docs/PRIVACY_POLICY.md"
check "docs/STORE_COPY.md"
check "docs/screenshots/SCREENSHOT_SPEC.md"
check "docs/FEATURE_GRAPHIC_SPEC.md"
check_png_rgb "docs/store_assets/feature_graphic.png" 1024 500
check "app/src/main/res/mipmap-xxxhdpi/ic_launcher.png"
check "app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png"
echo ""; echo "Results: $PASS passed, $FAIL failed"
[ $FAIL -eq 0 ] && exit 0 || exit 1
