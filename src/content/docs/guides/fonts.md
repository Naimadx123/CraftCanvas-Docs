---
title: "Screen fonts"
---

Screens use `minecraft:default` unless their root `font` selects another font. `font` follows normal screen/theme inheritance. MiniMessage `<font:namespace:name>...</font>` tags override the base for individual fragments; nested tags and `</font>` restore the enclosing choice. This works without shaders.

```yaml
schema-version: 1
id: font_example
extends: frame
font: minecraft:default
components:
  heading:
    text: '<gold>Normal <font:minecraft:alt>ABC</font> normal'
```

The tag also wins when it surrounds the entire text. Without a font tag, labels, state text, controls, YAML notifications and tooltips inherit the screen font. Variables, PlaceholderAPI values and addon list fields stay literal: a value containing `<font:...>` does not select a font. API notifications also inherit the screen font; their literal Component API continues to reject per-fragment custom fonts and other unsupported styles.

Built-in bitmap choices include `minecraft:default`, `minecraft:alt` and `minecraft:illageralt`. The default uses Minecraft's original bitmap definitions and client textures, with their variable advances and accented characters, including Polish letters. It does not depend on a custom `assets/text.png` atlas or an installed system font. An old text.png file is ignored, not moved or migrated.

## Your own font

Place ordinary resource-pack assets under `plugins/CraftCanvas/resource-pack/assets/`. For example:

```text
resource-pack/assets/example/font/menu.json
resource-pack/assets/example/textures/font/menu.png
```

The font JSON uses Minecraft's `providers` format:

```json
{"providers":[{"type":"bitmap","file":"example:font/menu.png","height":8,"ascent":7,"chars":["AB"]}]}
```

For this example, menu.png must have two equal cells in one row containing A and B. Supply all letters and symbols needed by your real font. `bitmap`, `space` and `reference` providers are supported. A reference may include `minecraft:default` as a fallback. Native Nexo source assets at `plugins/Nexo/pack/assets/` are also discovered; CraftCanvas's resource-pack directory takes precedence for custom font files. Font JSON and referenced PNGs must be available to the compiler as well as the client. A tag naming a font which exists only in an inaccessible external client pack cannot provide the server with layout metrics and rejects validation with its source location.

Select the same key at screen level or inside MiniMessage:

```yaml
schema-version: 1
id: custom_font_example
extends: frame
font: example:menu
components:
  heading:
    text: 'AB <font:minecraft:default>Default text</font>'
```

Run `/cc validate` and `/cc reload`, then deliver the rebuilt pack. Font resources, metrics and positioned variants are prepared on the file executor. Rendering does not read files or generate a per-player pack. Changed font bytes invalidate pack reuse even when paths and dimensions stay the same. Invalid resources reject reload and preserve the running configuration.

## Layout and provider limits

Body text retains the book renderer's eight-unit text area and nine-unit line spacing. Choose bitmap height/ascent and component padding/size accordingly; larger decorative glyphs may extend beyond a normal text row. Width measurement, wrapping, truncation, centering and advance compensation use the selected font. Fractional space advances reserve the next whole GUI unit using invisible spacing glyphs.

Book positioning needs adjustable bitmap ascent. TrueType/OpenType (`ttf`) and `unihex` custom providers and client-option filters are currently rejected. The default includes vanilla bitmap characters rather than duplicating the complete Unicode fallback for every GUI position. Characters missing from the selected compiled font display a positioned missing-glyph box; they are no longer replaced with ASCII `?`. Native message/tooltips use the client's normal font handling. For additional body characters, provide an appropriate bitmap font and select it with `font` or MiniMessage.

The pack contains internal font copies per required text baseline; it preserves the selected font's bitmap textures and metrics and does not overwrite `minecraft:default`. These internal `craftcanvas:text/...` names are implementation details. Component `font` fields remain unsupported; use the screen field or inline MiniMessage tags.

With animation shaders enabled, ordinary Minecraft ASCII text retains smooth component motion. Custom-font or other unsupported motion glyphs use the existing discrete animation fallback to retain their chosen appearance. This does not require enabling shaders for ordinary screen fonts.
