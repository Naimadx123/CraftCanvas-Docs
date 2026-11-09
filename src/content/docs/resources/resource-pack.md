---
title: "Resource pack"
---

## Addon bundles

Addons can install namespaced screen YAML, asset manifests and PNG/OGG resources through `CanvasService.installBundle`. CraftCanvas tracks file ownership and checksums, rejects conflicts and leaves existing installations unchanged during normal addon startup. Updates require an explicit administrator workflow; local edits are protected. See [the addon bundle API](/developers/bundles/) for paths, namespaces, limits and results.

Installation alone does not build or send a pack. Run `/cc validate` and `/cc reload` afterward, or call the [asynchronous reload API](/developers/reload/). Its result confirms configuration application and CraftCanvas pack generation/export; asynchronous hosting/merging and player acceptance remain separate. Installed addon resources enter the same compiler and generated pack as administrator-authored assets, including the automatic Nexo export described below. Keep `.data/addon-bundles/` in backups. Deleting an addon-owned screen is respected by automatic installation; an explicit bundle UPDATE can restore files listed by that request.

## Automatic Nexo workflow

With enabled Nexo, `pack.nexo-auto-export: true` (the default) writes the generated pack to `plugins/Nexo/pack/external_packs/craftcanvas.zip` on startup and successful configuration/pack builds. It requests regeneration through Nexo's public API after any current generation finishes. Nexo owns merging, uploading and sending; CraftCanvas sends no competing pack. Configure Nexo's pack generation and pack server normally. Sending after regeneration follows Nexo's reload-send settings; reconnect if those settings disable sending to existing players.

CraftCanvas recognizes the generated/uploaded combined pack through Nexo's public events and `packInfo()` UUID/hash. It still waits for SUCCESSFULLY_LOADED from the player. Neither the existence of a ZIP nor an unrelated loaded pack proves readiness. `/cc info` shows the effective sender and `/cc debug <player>` shows the expected merged UUID and statuses.

Nexo can load its pack before the player enters the world. CraftCanvas records these configuration-phase acknowledgements as well as game-phase `PlayerResourcePackStatusEvent` acknowledgements. A narrow version-selected Paper adapter observes decoded resource-pack response packets without changing or resending them, because the Bukkit event only covers the game phase. It uses the `handle` connection field on 1.21.11 and `packetListener` on 26.x. Tracking starts before asynchronous CraftCanvas preparation and survives the transition into the world. Reconnecting resets connection-specific status; declined, failed and unrelated pack UUIDs never authorize a screen. Nexo remains the sole sender. If this version-specific observer cannot attach, the server log reports the failure; updating Nexo's pack in game can still produce the normal game-phase event.

The path `plugins/Nexo/pack/external_packs/craftcanvas.zip` is reserved for CraftCanvas and is replaced automatically whenever its generated content changes, including after a plugin update or removal of the CraftCanvas data folder. Replacement is atomic where supported; identical ZIPs retain their modification time. No ownership file is required, and older `.data/generated-pack/nexo-export.json` files are no longer used. Put custom artwork in CraftCanvas's `assets/` directory rather than editing the exported ZIP. Other Nexo files and configuration are not rewritten. Source fonts/textures/sounds and text shader conflicts with other packs are still checked before export.

Set `pack.nexo-auto-export: false` to use manual workflows below. This does not delete the existing owned ZIP from Nexo. Automatic API handling targets stable Nexo 1.29.1; older/different API contracts may require manual configuration. Running Nexo generation, upload and client rendering still require testing on your installation.

## Standalone workflow

1. Run `/cc pack build` or `./gradlew.bat examplePack` for the shipped examples.
2. Take `plugins/CraftCanvas/.data/generated-pack/craftcanvas.zip` and its `sha1.txt`. The Gradle example task writes them under `build/examples/.data/generated-pack/`. A content-addressed immutable copy also lives under `.data/generated-pack/releases/<sha1>/`.
3. Host the ZIP at a direct HTTP(S) URL that clients can reach. CraftCanvas does not silently start a web server or upload to a third party.
4. Set `pack.sender: craftcanvas`, `pack.url` and optionally `pack.sha1`. An empty hash uses the generated pack hash. Choose the desired missing-resource policy.
5. Reload. The plugin sends one named pack and records statuses. Wait for the expected pack UUID to reach `SUCCESSFULLY_LOADED`; `/cc debug <player>` shows identities and statuses.
6. Open `home` or `selection` and choose a fitting profile with `/cc profile compact|normal|large`.

The default generated ZIP declares resource-pack formats 75.0 through 97.1, covering Minecraft 1.21.11, 26.1.x, 26.2 and 26.3. Its bitmap/space font providers use the shared format supported by these clients. `.data/generated-pack/manifest.json` records `min-pack-format` and `max-pack-format`. A provider merging this ZIP controls its final pack metadata and must also support the server/client version. By default no core shaders, global default-font overwrite or client mod is included. Enabling `shaders.animations` for [atlas playback](/guides/animations/) or `shaders.hover` for [hover overlays](/guides/shader-hover/) adds the text shader hooks and version overlays while keeping the complete 75.0 through 97.1 pack range. Book-glyph covers native chrome with its validated opaque panel and intercepts mouse clicks. Native keyboard focus remains; see BOOK_GLYPH.md.

`pack.id` is a base UUID in standalone mode; the actual pushed ID incorporates the expected hash, rejecting old load notifications after a new pack is generated. In external mode it must match the responsible sender's actual UUID. A locally selected resource pack has no server download-status acknowledgment. Do not assume an arbitrary successfully loaded pack contains CraftCanvas resources.

Cinematic screens additionally include a text fragment shader for procedural masks and fullscreen video tiles. Nexo composition preserves its verified GIF shader pair; other active shader conflicts are rejected. See [cinematic assets and limits](/guides/animations/#fullscreen-cinematics).

After switching from the TextDisplay renderer to title/HUD, rebuild and load the pack, including the merged Nexo pack when Nexo is the sender. Cinematics no longer export see-through text shader overrides.

## Add a PNG

Inventory and crafting slots can import [item model previews automatically](/guides/item-icons/) during the Nexo combined-pack build. Supported custom item models do not require a separate manual icon manifest. The steps below are for screen artwork and optional icon overrides.

Put the file under `assets/`, preserving transparency, and add an entry in an asset manifest:

```yaml
schema-version: 1
images:
  craftcanvas:portrait_knight:
    file: portraits/knight.png
    width: 144
    height: 180
```

Reference it from a book-glyph component:

```yaml
asset:
  provider: native
  kind: image
  id: craftcanvas:portrait_knight
```

The manifest dimensions describe the actual PNG. The component's box defines requested display dimensions. Generation resizes with nearest-neighbor sampling, splits into at most 128 × 128 tiles, generates positioned font providers, measures each tile's advance and assigns characters from the stable registry.

For registered vanilla item/block icons, use [vanilla sprites](/guides/sprites/). Their font definitions reference client textures directly, so no item PNG or local asset manifest is required. The generated font definitions still need to be rebuilt and sent when their layout changes.

Keep `.data/generated-pack/characters.yml`. It records allocation hashes and private-use codepoints. Allocations are stable across layout/resource updates and retained when no longer used, avoiding reassignment to a different image. Invalid, duplicate or exhausted assignments fail generation. Space characters use a separate font, so using the same numeric codepoint in two distinct fonts is not a collision.

Click sliders use `slider.track`, `slider.fill` and `slider.thumb` PNG references. The supplied manifest is `assets/sliders.yml`. Keep each PNG fully opaque so generated cells preserve their rectangular font metrics. The compiler generates the widths/heights needed by each allowed profile, including a one-unit advance reservation between cell images. Changing slider dimensions, profiles or graphics requires rebuilding and loading the resource pack.

Text defaults to Minecraft bitmap font definitions and client textures; no custom body atlas or system font is required. Root screen `font` and MiniMessage font tags choose alternatives. Supply custom bitmap font JSON and PNGs under `resource-pack/assets/`; see [screen fonts](/guides/fonts/). Rebuilding prepares the selected resources and positioned font variants. An old `assets/text.png` is ignored and left untouched.

Generation is deterministic for the same definitions, asset bytes and character registry. Immutable release paths prevent a queued export of an older catalog from accidentally reading a newer ZIP. PNG decoding, compilation, pack building, player persistence and export run on the file executor rather than the server thread.

## Add a sound

```yaml
schema-version: 1
sounds:
  craftcanvas:select:
    file: audio/select.ogg
```

```yaml
actions:
  click:
    - type: sound
      asset: {provider: native, kind: sound, id: 'craftcanvas:select'}
      volume: 0.7
      pitch: 1.0
```

Native sounds produce namespaced `sounds.json` entries and copy the OGG file. For existing sounds from an external pack, use the explicit namespaced sound key in an action's `id`. The three item integrations do not invent a sound-ID lookup API.

## External workflows

`/cc pack export <target>` stages a content-addressed export under `.data/generated-pack/exports/<target>/<sha1>/`. Standalone includes metadata; integration exports contain the `assets/` tree. They do not overwrite another plugin's configuration or resource files. When a target plugin is present, the exporter scans its resource files/ZIPs for conflicting generated paths and external text core shaders. Conflicting fonts, textures, sounds or shader assumptions refuse the export. This detects local conflicts, not every future provider-generated conflict; inspect the final merged pack as well.

Choose one responsible sender. Automatic Nexo handling takes precedence over the fallback `pack.sender` setting. If automatic handling is disabled and Nexo, CraftEngine or ItemsAdder sends the merged pack, use `pack.sender: external` and its actual UUID. If CraftCanvas sends, disable competing sends in those plugins' supported settings. CraftCanvas does not rewrite them or silently assume that several packs merge correctly.

| Workflow | Copy the staged assets into | Finish |
| --- | --- | --- |
| Nexo | A dedicated directory under `plugins/Nexo/pack/external_packs/`, retaining the assets tree | Rebuild and send through Nexo |
| CraftEngine | `plugins/CraftEngine/resources/craftcanvas/resourcepack/`; create the pack through its resource-create workflow first | Rebuild through the chosen CraftEngine workflow |
| ItemsAdder | `plugins/ItemsAdder/contents/craftcanvas/resourcepack/` | Rebuild/upload/send the merged pack through ItemsAdder |

These destinations follow [Nexo's external-pack workflow](https://docs.nexomc.com/configuration/resourcepack), [CraftEngine's resource-pack structure](https://xiao-momi.github.io/craft-engine-wiki/getting_start/project_structure/), and [ItemsAdder's merging workflow](https://wiki.itemsadder.com/adding-content/merge-resourcepacks/). Check the final merged pack for conflicts and load it before opening screens.

A successful CraftCanvas reload keeps loaded status if pack content/identity is unchanged. Changed exports require a rebuilt and resent external pack; old loaded state is invalidated. External senders should bind their pack UUID to the merged content version.

## Diagnose graphics

For frame animation, register each PNG as a separate image, list the resources in `asset-select.options`, and bind the selector to the session variable written by `animate`. The bundled wheel demonstrates 48 static 192 x 192 frames and a fixed pointer. With `shaders.animations: true`, the generator builds tiled atlases and the client selects frames locally. Otherwise the server resends the book per frame. GIFs and `.png.mcmeta` font animation metadata are not interpreted. Rebuild and send the pack after changing artwork. See [playback and limitations](/guides/animations/).

- `/cc validate` reports bad fields, references, renderer combinations and PNG dimensions.
- `/cc inspect classes` reports the actual backend and its limits.
- `/cc assets native` lists server-native resource IDs.
- `/cc debug <player>` distinguishes missing pack status from missing provider IDs.
- Client logs reveal failed font providers, missing textures, pack-format failures or atlas issues.
- Use the exact generated ZIP and matching hash, and rebuild after adding a positioned image or text row.
- Choose a smaller manual profile if the reference panel exceeds the client's available GUI units. No automatic screen-size inference is performed.

Shader animation, arbitrary client hover styling and general texture-animation declarations are unavailable in the baseline. A known Nexo ShaderAnimatedGlyph is refused and can use an explicit supported fallback. Other external shader assumptions require manual compatibility verification rather than being advertised as default-renderer features.

Fresh installations allocate a character registry during pack generation. Keep existing registries and host the pack built for that installation. Never merge different registries into the same font namespace. Global vanilla textures and language keys are not replaced. Core shader overrides are included only when explicitly enabled.

## Versioned shader overlays

With either shader option enabled, the base pack supplies the 1.21.11 shaders. `craftcanvas_26_1`, `craftcanvas_26_2` and `craftcanvas_26_3` override the text pipelines for resource formats 84, 88 and 97 respectively. The overall pack range remains 75.0?97.1. These overlays are not separate packs and require no manual client selection. Shader settings still default to disabled.

26.1 uses the version's lightmap sampler. 26.2 uses the merged text pipeline with its GUI, see-through and grayscale defines. 26.3 also uses ShaderC imports, explicit stage locations and the changed uniform blocks. Its hover overlay accounts for the removal of the item-title gap in multiline show-text tooltips. Ordinary text and world/OIT branches retain their vanilla behavior; CraftCanvas's newer pipeline hooks run only for non-grayscale GUI text carrying its encoded glyph markers. Cinematic fragment hooks are supplied only when cinematic screens are present.

The version differences are documented in Mojang's [26.2 release notes](https://www.minecraft.net/en-us/article/minecraft-java-edition-26-2) and [26.3 release notes](https://www.minecraft.net/en-us/article/minecraft-java-edition-26-3). The shader entry points are adapted from the official client files. Match the server/client versions; ViaVersion and third-party shader engines are not covered by these overlays.

Generated resources are written only under `.data/generated-pack/`. An older top-level `generated-pack/` directory is ignored and left untouched.
