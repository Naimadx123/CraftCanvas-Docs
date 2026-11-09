---
title: "Experimental shader hover"
---

Shader hover adds a temporary PNG overlay to a button while the client's native text hover is active. It is an optional extension of `book-glyph`, disabled by default. The shader variants target **Paper/Folia 1.21.11 through 26.3 with matching vanilla clients**, including 26.1, 26.1.1 and 26.1.2. ViaVersion and modified rendering clients are not covered.

## Enable and try the example

1. In `plugins/CraftCanvas/config.yml`, set:

   ```yaml
   shaders:
     hover: true
     animations: false
   ```

2. Copy [the complete example](https://github.com/Naimadx123/CraftCanvas/blob/master/examples/shader_hover.yml) to `plugins/CraftCanvas/screens/shader_hover.yml`. Keep the bundled `frame` screen and the standard `button`, `selected_frame`, `close` and `book_panel` PNGs/manifests. The example is deliberately outside the default screens so normal installations do not require shaders.
3. Run `/cc reload`. A successful reload compiles the screen and builds a new pack. Unsupported server versions reject the setting. `/cc debug capabilities` shows whether the mode is enabled.
4. Load the newly generated pack. With automatic Nexo export, CraftCanvas replaces its managed ZIP and requests a Nexo rebuild; wait for Nexo's merged pack to load. With standalone hosting, replace the hosted ZIP and send the new version through your configured sender.
5. Run `/cc open shader_hover`. Move over FIRST and SECOND, click to change the persistent selection, and compare with LOCKED. The disabled button has its ordinary tooltip but no hover overlay or server action. The inherited X closes the screen.

Turning the option on or off changes pack contents. It is not an immediate per-player shader switch. To disable hover overlays, remove `hover-overlay` fields or remove screens using them, set `shaders.hover: false`, reload and distribute the updated pack. The shared shader remains if `shaders.animations` is enabled. A failed reload preserves the previous active configuration.

## Add an overlay to a button

Add a transparent PNG to an [asset manifest](/resources/resource-pack/#add-a-png). Use an outline or translucent decoration when text and selected graphics should remain visible beneath it. The overlay covers the button's complete box. Nine-slice scaling preserves the corners and edge thickness instead of widening the left and right edges of a square frame on a rectangular button.

```yaml
components:
  destination:
    type: button
    position: {x: 40, y: 114}
    size: {width: 144, height: 48}
    layer: 20
    padding: 12
    text: '<white>Open catalog'
    asset: {provider: native, kind: image, id: 'craftcanvas:button'}
    hover-overlay: {provider: native, kind: image, id: 'craftcanvas:selected_frame'}
    hover-overlay-border: 4
    tooltip: ['<gold>Catalog', '<gray>Browse the available items.']
    actions:
      click: [{type: open, screen: catalog}]
```

`hover-overlay` accepts a native image/glyph/sprite reference without a fallback. It requires an actionable `button` and a nonempty default text tooltip. Normal/selected tooltip overrides must also remain nonempty. An optional asset permission controls whether that player receives the overlay. Item tooltips and session item tooltip slots cannot share this effect; they retain their usual behavior on other components. Invalid combinations fail validation rather than dropping configured behavior.

`hover-overlay-border` defaults to `4` source pixels on each edge, including any transparent gap inside that border. The bundled 36 by 36 `selected_frame` uses this setting. Corners scale uniformly with the manually selected layout profile; only the middle sections stretch. The source border must occupy less than half each PNG dimension and must fit the button in every allowed profile. Set it to `0` for an overlay that should stretch as a whole image.

## How it works

The existing button graphics retain their original font metrics, click area and authorized session tickets. Their native `show_text` tooltip contains small marker glyphs with zero net advance, followed by the ordinary configured tooltip text. Core shader code recognizes marker pixels in the glyph atlas and relocates their PNG payload to a position compiled from the screen layout. The target uses the same native book origin, manual profile and unscaled offsets as the normal renderer. Larger overlays are tiled into at most 120 by 120 payloads in 128 by 128 marker textures.

The normal text tooltip remains beside the cursor, including its native frame. Its text uses a namespaced font referencing the vanilla 1.21.11 bitmap and Unicode providers, with matching bundled advance metrics. Shader hover tooltips wrap at 180 GUI units before reaching the client; colors, bold, Unicode and line breaks are retained. This font is independent of the client's Force Unicode Font option and does not replace fonts used by other tooltips.

Each overlay tile is split into four nonoverlapping pieces around the tooltip's visible rectangle. Its dimensions travel in the marker color, and its actual position comes from the marker vertices after the client positions the tooltip. The shader clips the overlay out of that rectangle, keeping the native tooltip background and text unobstructed even when it moves near a screen edge. This is geometric exclusion, not a Z-depth change. Advanced item tooltips (F3+H) do not add item ID/component lines because the effect uses `show_text`, not an item tooltip.

Hover does not create a server event or modify a session variable. Selected graphics remain a separate, persistent state. Disabled, locked and loading buttons do not receive the shader overlay. Click sounds/actions still run only after a click. There is no `on-hover`, hover sound, drag tracking or arbitrary hover animation.

## Pack scope and limitations

The standalone pack supplies `rendertype_text.vsh` for 1.21.11 and overlays for 26.1.x (format 84), 26.2 (88) and 26.3 (97). The newer 26.2/26.3 pipelines use `text.vsh`. Namespaced includes share the hover, animation and cinematic algorithms; 26.3 uses explicit stage locations, `#include` and `gl_VertexIndex`. [Atlas image animation](/guides/animations/) is controlled independently by `shaders.animations`. The pack leaves vanilla tooltip sprites, book textures and default fonts alone. Unmarked glyphs follow the stock shader path. It declares formats 75.0 through 97.1 with either shader option enabled. Merging providers must preserve the overlay metadata and files.

Core shader files are global pack resources. During `NexoPostPackGenerateEvent`, CraftCanvas checks the effective text shader, including active overlays for formats 75, 84, 88 and 97. For the verified Nexo 1.29.1 GIF shader templates for each release family, it preserves `applyGifEffect()` and adds the hover and atlas hooks in memory before serialization/obfuscation. No Nexo source files are changed. `/cc debug capabilities` reports integration status. Unknown vertex or fragment shaders reject composition. Shader-free book animation does not require this adapter.

Other providers' text shaders are conflicts, not something CraftCanvas automatically merges. Existing Nexo/CraftEngine/ItemsAdder export conflict checks remain active. Do not overwrite another shader to dismiss an export error. Mojang considers core-shader overrides an unsupported internal mechanism that can change between releases.

The overlay renders in the tooltip layer, above the menu, rather than participating in the menu's normal layer ordering. Exclusion uses stock tooltip sprite padding and pinned vanilla font metrics, with the 26.3 multiline show-text spacing correction; replacements that change their padding or glyph advances are not covered. Transparent image pixels keep the underlying button's rectangular font hitbox; shader displacement changes pixels, not the original hit-test geometry. An ordinary tooltip may overlap part of the panel as it does without shaders. The server cannot confirm that a modified client's rendering pipeline has executed the shader just because the pack loaded successfully.

Check the example in vanilla before using it on a live server: test F3+H both on and off, multiple GUI Scale settings, both ends of each button, all manual profiles, tooltip repositioning near screen edges, two players selecting independently, disabled actions, reload and X/Escape. Shader compilation or a standalone OpenGL check alone does not confirm Minecraft book rendering or hitboxes.

## Developer check

The Gradle integration test `ShaderHoverIntegrationTest` builds `build/shader-hover-example/craftcanvas.zip`. `tools/check_hover_shader.py` can compare its displaced pixels in a standalone OpenGL 3.3 context against the compiled metadata. Install Python packages `moderngl` and `glcontext` into a temporary environment, then run:

```shell
python tools/check_hover_shader.py --client /path/to/official-1.21.11-client.jar --pack build/shader-hover-example/craftcanvas.zip
```

The tool verifies the official client SHA-1, resolves the pack's active overlays and shader imports, and falls back to stock client resources where the pack supplies no override. It compares overlay pixels and tooltip exclusion at several viewport sizes and marker positions, and reports the effective vertex/fragment paths. It requires a working OpenGL context and does not start or control a Minecraft client.

Use `--glyph-pack build/shader-hover-example/craftcanvas.zip` to check freshly generated marker tiles against the effective shaders of a separate merged pack passed as `--pack`.

To regenerate the pinned font definition and compressed Unicode advance table, use `tools/generate_hover_tooltip_metrics.py --client /path/to/official-1.21.11-client.jar --asset-index /path/to/1.21.11-assets.json`. This developer tool requires Pillow, checks the client SHA-1 and downloaded font asset hashes, and reads Mojang's versioned font providers. The plugin does not download fonts at runtime or distribute vanilla PNG/HEX font files.

The optional API integration tests can use an administrator-supplied Nexo JAR without adding Nexo as a runtime dependency:

```shell
gradlew.bat integrationTest --tests "*NexoShaderApiTest" -PnexoTestJar=/path/to/nexo-1.29.1.jar
```

An additional `-PnexoTestPack=/path/to/merged.zip` enables the captured-pack test, which writes a composed copy under `build/shader-hover-example/` for the GPU check. It leaves the supplied JAR and ZIP unchanged. These tests are skipped when no Nexo JAR is supplied.
