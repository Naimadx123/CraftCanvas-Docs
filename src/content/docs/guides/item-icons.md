---
title: "Automatic item models and icon overrides"
---

With Nexo automatic pack export enabled, CraftCanvas imports item definitions, model JSON and textures from Nexo's combined pack. It automatically prepares book glyph previews for inventory, crafting, Hand and result slots. No per-item `item-icons` rules, copied textures or manually rendered PNGs are required for supported models.

The actual ItemStack is preserved. Its item model key selects the item definition; its CustomModelData selects the prepared variant. The item is never rewritten into a different model just to display it in the book.

## Automatic setup with Nexo

1. Keep `pack.nexo-auto-export: true`, the shipped default.
2. Install CraftCanvas and restart. Run `/cc reload` after changing screen geometry.
3. CraftCanvas exports its screen resources and requests a Nexo build. During `NexoPostPackGenerateEvent`, it reads the combined resources, prepares previews and appends its generated fonts/textures before Nexo packages and uploads the pack.
4. Accept the newly generated Nexo pack and wait for it to finish loading.
5. Open `/cc open crafting`. Custom items in the player's inventory use their prepared model previews automatically.

Model processing runs on CraftCanvas's file worker while the Nexo build waits for completion. The initial build can take longer. Repeated identical model resources reuse the preparation cache. There is no filesystem access, model parsing or image baking during book rendering or slot clicks.

Rebuilding Nexo refreshes the model import, including changed textures, models and client-format overlays. Active screens are invalidated during generation. The generated preview resources are added to Nexo's final combined pack; they are not present in the independent CraftCanvas ZIP exported before Nexo's build.

## Supported models

| Feature | Behavior |
| --- | --- |
| Explicit item model | Resolves assets/namespace/items/path.json from ItemStack.item_model |
| CustomModelData | Resolves range_dispatch floats, select strings and condition flags, including configured component indices |
| Legacy overrides | Supports ordered custom_model_data-only predicates in item model JSON |
| Flat generated items | Combines texture layers into the preview |
| Cuboid models | Rasterizes model elements, face UVs, UV rotations and element rotations |
| Model inheritance | Resolves parent JSON and texture variables; detects cycles |
| GUI transform | Applies display.gui rotation, translation and scale |
| Common vanilla parents | Handles generated/handheld items and block/block, cube, cube_all, cube_column and orientable templates |
| Static composites | Combines supported static model layers |
| Constant tints | Applies configured constant tint colors |
| Dye and CustomModelData tint defaults | Prepares the declared default color; accepts items with no override or the same RGB color |
| Unused tint sources | Ignores sources not referenced by model faces or generated texture layers |
| Animated textures | Uses the first configured frame as a static preview |
| Resource overlays | Imports the resources active for the supported server/client pack format |

Previews are generated at 32 by 32 pixels and scaled by the existing slot fonts. Software side lighting approximates the GUI view. Oversized geometry can be clipped to the preview; this is not the client's native live model renderer. Enchantment glints, generated-item side extrusion, special entity renderers such as heads/banners/shields, dynamic composites and unsupported client-state predicates are not reproduced. These models produce a barrier placeholder and an explicit diagnostic rather than quietly looking like their base material.

Nexo's default `dye` tint with a white fallback is supported, including flat generated items. Cuboid faces only use their explicit `tintindex`; generated texture layers use their layer number. Tint colors accept packed RGB integers or three channels in the range 0..1. An item's actual dye or CustomModelData color that differs from the prepared default is rejected with a specific diagnostic; arbitrary new color variants are not generated during rendering. Referenced tint sources such as team or potion color remain unsupported. Unreferenced sources do not prevent importing a model.

Input limits are 4096 item definitions/previews, 32768 source files, 256 MB total source bytes, 16 MB per texture, 32 million decoded texture pixels, 2048 cuboids per model and 250000 processed faces per preparation. Parent/composite depth is limited to 32. Limit failures have diagnostics. Whole-import failures prevent the affected Nexo pack from being considered ready.

Models must reference textures available in the combined pack. A direct known vanilla model fallback can use its existing client sprite. Automatic processing of arbitrary external packs without the Nexo build adapter is not currently provided; the optional overrides below remain available in standalone setups.

## Find the model identifier

Hold the item and run `/cc debug <your-player-name>`. The `Item`, `Item model`, `CustomModelData` and `Item icon` fields show its material, explicit model key, component lists and preview diagnostic or override. `Item model: default` means the item has no explicit model override; the material's item definition may still select a custom variant through CustomModelData. `/cc info` shows import state and model/preview totals. Full import diagnostics are available in `/cc debug` and the server log.

Use the actual item model key, which can differ from a Nexo item ID, a texture name or a model JSON filename. CraftCanvas reads the public Paper ItemMeta API. Nexo, CraftEngine and ItemsAdder are not required for model-key or CustomModelData matching.

## Optional manual overrides

The following configuration deliberately overrides automatic selection for matching items. It is optional, useful for an alternative icon, standalone packs or unsupported models.

### Register the preview

For a flat override, reuse a copy of its square PNG texture. For a 3D override, supply a square transparent preview showing the desired view. A model's UV texture sheet is not a rendered preview. These steps are only needed when deliberately replacing automatic selection or covering an unsupported feature.

For example, place a 32 by 32 preview at `plugins/CraftCanvas/assets/items/ruby_sword.png` and create `assets/item_previews.yml`:

```yaml
schema-version: 1
images:
  craftcanvas:items/ruby_sword:
    file: items/ruby_sword.png
    width: 32
    height: 32
```

PNG paths are relative to `assets/`. Registered icons are scaled to the slot's inner size during pack generation. Their actual alpha bounds determine advance; no per-click image processing or pack generation occurs.

### Match an item model

Add this to `config.yml`, replacing the model key with the held item's actual value:

```yaml
item-icons:
  ruby_sword:
    match:
      item-model: nexo:ruby_sword
    asset:
      provider: native
      kind: image
      id: craftcanvas:items/ruby_sword
```

Restart or run `/cc reload`, then load the newly generated pack. With automatic Nexo export, let Nexo build and send its combined pack. No changes to the screen's slot definitions are needed.

### Match CustomModelData

For an integer CustomModelData value, include the material to distinguish items using the same number:

```yaml
item-icons:
  ruby_sword:
    match:
      material: PAPER
      custom-model-data: 1234
    asset: {provider: native, kind: image, id: 'craftcanvas:items/ruby_sword'}
```

The integer matches the first float of the modern component, as used by legacy CustomModelData. Other component entries do not affect this shorthand. For modern model data, specify the lists that identify the item:

```yaml
item-icons:
  ruby_variant:
    match:
      material: PAPER
      item-model: nexo:ruby_sword
      custom-model-data:
        floats: [42.5]
        strings: [ruby]
        flags: [true]
        colors: ['#12AB34']
    asset: {provider: native, kind: image, id: 'craftcanvas:items/ruby_sword'}
```

Every criterion in a rule must match. Specified lists match exactly, including their order and length. Omitted lists are unrestricted; an explicit empty list requires that list to be empty. At least one component list is required in the map form. The first matching rule in YAML declaration order wins; place specific variants above broad model/material rules. Renaming an item, changing its lore or changing the stack amount does not change these criteria.

| Field | Type/default | Rules |
| --- | --- | --- |
| item-icons | map, empty | At most 128 ordered rules |
| rule ID | string | 1..128 letters, digits, underscores, periods, colons or hyphens |
| match | required map | At least material, item-model or custom-model-data |
| match.material | optional string | Valid non-air item material, such as PAPER or minecraft:paper |
| match.item-model | optional string | Explicit namespaced item model key |
| match.custom-model-data | optional integer/map | Integer is signed 32-bit; map supports floats, strings, flags and colors |
| component list | optional list | At most 64 values; floats must be finite; strings at most 256 characters; colors use #RRGGBB |
| asset | required graphic reference | Unconditional registered native PNG or registered vanilla sprite |

CustomModelData criteria require material or item-model as well. External provider glyphs, permissions, fallbacks and overridden font metrics are rejected in icon rules: a slot needs a compiled graphic at each supported profile and position. Missing previews, unknown fields and invalid references reject reload while retaining the previous valid configuration and pack.

## Troubleshooting

- If automatic import is unavailable, check `pack.nexo-auto-export`, the Nexo generation/upload state and `/cc info`.
- If a barrier placeholder appears, read `/cc debug <player>` and the model import diagnostic. Missing textures, parent cycles and unsupported model types have separate reasons.
- If using an explicit override, compare the configured model key/material and component lists with the held-item diagnostic.
- If debug reports the correct rule but the icon is missing or incorrect, rebuild and reload the current pack and check the preview PNG and its manifest dimensions.
- If the image looks like an unfolded model, supply a rendered preview rather than its UV sheet.
- If several variants use one model key, add CustomModelData criteria before the broad model rule.

Import, variant selection, bitmap generation, transparency and the Nexo 1.29.1 public pack event contract are covered by automated tests. Actual appearance and custom-pack interactions still require testing in the target Minecraft client. This feature adds automatic model previews to the book renderer, without native inventory widgets, client mods or core shaders.
