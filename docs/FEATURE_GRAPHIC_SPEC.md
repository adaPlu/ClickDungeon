# ClickDungeon Feature Graphic Specification

## Canvas

| Property | Value |
|----------|-------|
| Width | 1024 px |
| Height | 500 px |
| Color mode | RGB (no alpha) |
| Export format | PNG |
| File name | `feature_graphic.png` |
| Destination | `docs/store_assets/feature_graphic.png` |

Google Play requires exactly **1024×500 px**. Do not add letterboxing or
transparent borders — export must be flat RGB with no alpha channel.

---

## Color Palette

| Name | Hex | Usage |
|------|-----|-------|
| Dark background | `#1A0A00` | Full canvas base color |
| Gold text | `#FFF3D5` | Logo, tagline, any text |
| Health green | `#4CAF50` | Optional accent (HP bar motif) |
| Mana blue | `#2196F3` | Optional accent (ability/mana motif) |
| Deep shadow | `#0D0500` | Drop-shadow under logo, vignette edges |

---

## Background

- Fill the entire canvas with `#1A0A00`.
- Use the **`bg_screen_dungeon`** gradient palette as the base treatment.
- Overlay the committed **`crypt`** dungeon art scaled/cropped to fill the canvas
  at reduced opacity (~40-55%) so it reads as a texture, not a photo.
- Apply a subtle vignette: radial gradient from transparent center to
  `#0D0500` at all four edges, ~15% opacity.

---

## Left Side — Knight Class Icon

- Asset: `icon_knight` drawable (source PNG at `app/src/main/res/drawable-nodpi/`).
- Position: vertically centered, left edge starting at ~40 px from the left
  canvas edge.
- Size: approximately **180×180 px** (scale proportionally).
- Apply a soft drop shadow (`#0D0500`, 8 px blur, 4 px offset down-right).
- Optional: faint gold glow / outer glow at 20% opacity to tie into logo color.

---

## Center — Logo

- Text: **"ClickDungeon"**
- Font: bold fantasy/display typeface. Suggested free options:
  - *MedievalSharp* (Google Fonts)
  - *Cinzel Decorative* (Google Fonts, bold weight)
  - *Uncial Antiqua* (Google Fonts)
- Font size: approximately **96–112 px** (adjust until text occupies ~60% of
  canvas width).
- Color: `#FFF3D5` (gold).
- Horizontal alignment: centered on the canvas.
- Vertical alignment: slightly above canvas center (~210 px from top).
- Text decoration: hard drop shadow `#0D0500` at 3 px offset, 6 px blur.
- Optional: thin gold outline stroke (`#FFF3D5` at 30% opacity, 1 px).

---

## Right Side — Wizard Class Icon

- Asset: `icon_wizard` drawable (source PNG at `app/src/main/res/drawable-nodpi/`).
- Position: vertically centered, right edge ending at ~40 px from the right
  canvas edge.
- Size: approximately **180×180 px** (scale proportionally, match Knight icon
  height exactly).
- Apply the same drop shadow and optional glow as the Knight icon.

---

## Bottom — Tagline

- Text: **"Tap. Fight. Survive."**
- Font: same typeface family as logo, regular or light weight.
- Font size: approximately **36–42 px**.
- Color: `#FFF3D5`.
- Horizontal alignment: centered on the canvas.
- Vertical position: approximately **420–440 px** from top (near bottom edge,
  leave at least 30 px margin to canvas bottom).
- Optional: subtle letter-spacing (tracking) of +2–4 px for readability.

---

## Layout Diagram (approximate)

```
+----------------------------------------------------------+
|  [vignette overlay on stone texture, dark edges]         |
|                                                          |
|   [Knight]       ClickDungeon          [Wizard]          |
|   icon ~180px    (bold fantasy font)   icon ~180px       |
|                  gold #FFF3D5                            |
|                                                          |
|              Tap. Fight. Survive.                        |
+----------------------------------------------------------+
```

---

## Production Checklist

| Step | Status |
|------|--------|
| Canvas created at 1024×500 px | DONE |
| Background filled `#1A0A00` | DONE |
| Stone texture overlay applied at ~50% opacity | DONE |
| Vignette gradient applied | DONE |
| Knight icon placed left, drop shadow applied | DONE |
| Wizard icon placed right, drop shadow applied | DONE |
| Logo text set in fantasy font, gold `#FFF3D5` | DONE |
| Tagline set below logo | DONE |
| Exported as PNG, no alpha | DONE |
| File placed at `docs/store_assets/feature_graphic.png` | DONE |
| Uploaded to Google Play Console | PENDING |
