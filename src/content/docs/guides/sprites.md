---
title: "Vanilla item and block sprites"
---

Use the client's existing textures for reward previews, shop icons and buttons. CraftCanvas writes positioned bitmap-font definitions referencing those textures; it does not copy item/block PNGs into your pack. This works with the normal shader-free book renderer and manual profiles.

## Display a reward icon

Add this component to a screen that extends `frame`:

```yaml
components:
  reward:
    type: button
    position: {x: 40, y: 112}
    size: {width: 32, height: 32}
    layer: 20
    asset:
      provider: vanilla
      kind: sprite
      id: minecraft:item/diamond
    tooltip-item: {provider: vanilla, kind: item, id: DIAMOND}
    actions:
      click: [{type: message, text: '<aqua>Diamond selected.'}]
```

`minecraft:item/diamond` identifies the graphic. `DIAMOND` in `tooltip-item` identifies the real item used for its tooltip. An item reference alone does not select or render its texture.

Sprite boxes must be square and remain between 1 and 128 GUI units in every allowed profile. A 32 by 32 icon becomes 24 by 24 in `compact` and 40 by 40 in `large`. Positions, ascent and advance use the same book layout as local artwork. Do not supply `font`, `character` or `metrics` overrides.

The registered catalog includes static square item and block textures with matching dimensions and advance metrics in official clients 1.21.11, 26.1.2, 26.2 and 26.3. Unknown IDs, animated textures and unsupported dimensions fail validation. Block sprites show a flat texture, not the block's inventory model. Custom item models, enchantment glint, durability bars and stack counts are not automatically drawn.

## Change an icon with a variable

Use the existing asset selector to choose previously compiled sprites:

```yaml
variables:
  reward: {type: string, default: emerald}
components:
  reward_preview:
    type: image
    position: {x: 40, y: 112}
    size: {width: 32, height: 32}
    layer: 20
    asset-select:
      variable: reward
      options:
        emerald: {provider: vanilla, kind: sprite, id: 'minecraft:item/emerald'}
        diamond: {provider: vanilla, kind: sprite, id: 'minecraft:item/diamond'}
    tooltip: ['<white>Reward: {reward}']
```

Set `reward` through an action or the session API and refresh the screen. This changes the displayed graphic without generating a new PNG. The bundled `reward_wheel` uses this for its result preview. Its rotating disc remains the existing PNG frame animation; separate sprite previews do not automatically rotate with that disc.

## MiniMessage sprite tags

Administrator-defined text and text tooltips can contain:

```yaml
text: '<white>Reward: <sprite:"minecraft:items":"minecraft:item/diamond"> Diamond'
tooltip:
  - '<white><sprite:"minecraft:items":"minecraft:item/emerald"> Emerald'
  - '<gray>Click to select.'
```

Use atlas `minecraft:items` for registered item sprites and `minecraft:blocks` for block sprites, for example `<sprite:"minecraft:blocks":"minecraft:block/stone">`. A wrong atlas or unknown sprite is an error. Explicitly use white when you want the original texture colors; surrounding text color can tint the icon.

Inline icons occupy 8 GUI units and ignore bold sizing. In positioned book body text the compiler converts the sprite object to an equivalent font glyph referencing the client texture, retaining exact row positioning and wrapping. In native text tooltips and messages the Adventure sprite object is preserved. To display a larger icon, use a graphical component with `asset.provider: vanilla` and `asset.kind: sprite`.

Tags must be present in trusted screen definitions, actions or language entries when the pack is compiled. Variables and PlaceholderAPI values remain literal text and cannot inject sprite tags. Rebuild and distribute the pack after adding a new icon, position or size, because the font definitions change even though no item PNG is exported.

## Try the complete example

Run `/cc open sprites` on a fresh installation. It shows six reward icons, item and text tooltips, clickable sprites and inline item/block icons. The home screen includes a link to it. On an existing installation copy [screens/sprites.yml](https://github.com/Naimadx123/CraftCanvas/blob/master/src/main/resources/screens/sprites.yml) from the new JAR or source distribution into your screens directory and run `/cc reload`. Existing example screens are not automatically rewritten. Copy the updated `reward_wheel.yml` separately if you want its new result preview.

The catalog verifies source texture metrics. A different client pack that removes or changes those textures' padding/dimensions can change the font advance or show a missing texture. Use matching vanilla clients and check your merged pack in game, including manual profiles, hitboxes and tooltips.

## Developer catalog

Addons select a compiled sprite by updating the configured asset selector variable with `CanvasService.setVariablesAsync`. Supply real item tooltips separately with `setItemTooltip`, or declare a fixed `tooltip-item` reference. Include sprite references in the screen YAML installed by your addon bundle before reloading. `/cc assets vanilla` lists registered sprite examples.

To regenerate the source catalog, run `tools/generate_sprite_catalog.py` with Pillow installed and repeat `--client version=/path/to/client.jar` for each supported client. The tool checks official Mojang client hashes and retains only direct static square textures with identical size/ink-width metrics across all supplied clients. It writes metadata without distributing vanilla textures.
