---
title: "Inventory workbenches"
---

The `crafting` example displays a 5 by 5 crafting board beside the player's storage inventory. Icons, counts, transfers and recipe results are session-owned book components. Core shaders and external plugins are not required.

## Install and open

A fresh installation includes the example automatically. Existing screens are never overwritten. For an existing installation, replace `screens/crafting.yml` and copy `templates/item_slot.yml`, `assets/crafting.yml` and `assets/crafting_slot_frame.png` from `src/main/resources/` into the same paths under `plugins/CraftCanvas/`. The previous palette example's `templates/crafting_cell.yml`, `actions/clear_crafting_board.yml` and `actions/load_crafting_recipe.yml` are no longer used.

Run `/cc reload` and load the newly generated pack through your configured sender. With Nexo auto-export, rebuild/send Nexo's combined pack after CraftCanvas reloads. Open the screen with:

```text
/cc open crafting
```

## Move items

- Click an inventory slot to pick up its displayed stack into Hand.
- Click an empty crafting slot to place one item. Further clicks add one item when the stack is similar and has room.
- With an empty Hand, click a crafting slot to pick up its contents.
- Clicking a different occupied slot swaps the stacks.
- Click an inventory slot to return, combine or swap the held stack there.
- Click Hand to return the held stack to available inventory space.
- Return items cancels the whole board and held stack.
- Click the result or Craft to make one recipe.

Hand is a fixed preview on the panel. Its icon does not follow the mouse. The book provides click actions rather than native inventory dragging, right-click splitting or shift-click transfers.

The right-hand storage view displays all 36 slots together in a 9-column, 4-row grid. The first three rows show inventory slots 9..35; the last row shows hotbar slots 0..8. Armor and offhand are excluded. There are no inventory pages or recipe hints in the example.

## Reservations and item safety

Picking up and placing items changes a virtual allocation of the player's actual ItemStacks. The displayed inventory subtracts these reservations, while real storage remains untouched until Craft succeeds. No item is deposited into a hidden inventory, dropped into the world or replaced with a temporary book.

Closing with X, Escape, navigation, disconnecting, timing out or disabling the plugin cannot strand reserved items: they are still in real storage. Rearranging the displayed inventory is temporary; this screen is a workbench rather than a persistent inventory sorter.

Before an action, CraftCanvas compares storage against the original snapshot, including amounts, metadata and positions. An external change resets reservations and rejects that click. It never restores an old inventory snapshot over another plugin's changes.

Craft rechecks the current pattern, ingredient identity, amounts, recipe permission, output availability and output capacity. It prepares an inventory exchange on cloned storage and commits only after all checks pass. A full inventory leaves the board and materials unchanged. A successful craft grants the output to real storage and releases all remaining reservations. Return or place the held stack before crafting.

Matching uses `ItemStack.isSimilar`. Names, lore, enchantments, damage and custom data are retained on reserved stacks; modified items do not automatically match plain vanilla ingredients. External plugin side effects and process crashes are outside a general cross-plugin transaction guarantee.

## Configure recipes

Add `workbench` at the root of a screen using `book.update: resend`:

```yaml
workbench:
  columns: 5
  rows: 5
  recipes:
    reinforced_pickaxe:
      pattern:
        - IIIII
        - I.D.I
        - I.D.I
        - I.D.I
        - IIIII
      ingredients:
        I: {asset: {provider: vanilla, kind: item, id: IRON_INGOT}, amount: 1}
        D: {asset: {provider: vanilla, kind: item, id: DIAMOND}, amount: 1}
      result: {asset: {provider: vanilla, kind: item, id: DIAMOND_PICKAXE}, amount: 1}
```

This recipe consumes 16 iron ingots and 3 diamonds. Each occupied cell must contain at least its symbol's amount; dots must be empty. Larger stacks are permitted, and only the recipe's quantities are consumed. Recipes match their exact configured positions; there is no implicit rotation, mirroring or shapeless matching.

| Field | Type/default | Rules |
| --- | --- | --- |
| columns, rows | integers, both 5 | Each 1..5 |
| recipes | required map | 1..64 named definitions, tested in declaration order |
| pattern | required string list | Exactly rows strings of columns characters; A..Z ingredients and . for empty |
| ingredients | required map | Exactly the symbols used in the pattern |
| ingredient.asset | required item reference | kind: item; vanilla or an available item provider |
| ingredient.amount | integer, 1 | 1..64 per occupied cell |
| result.asset | required item reference | Output ItemStack provider/ID |
| result.amount | integer, 1 | 1..64; split into legal output stacks |
| permission | optional string | The first matching recipe must be permitted |

Change the workbench dimensions, grid layout, slot bindings and pattern together. Multiple recipe IDs can share a board. Optional Nexo, CraftEngine and ItemsAdder item references use their existing adapters; missing resources prevent that recipe from becoming ready.

## Configure slots

This fragment belongs under `components` in a workbench screen extending `frame`:

```yaml
input:
  type: item-slot
  position: {x: 32, y: 116}
  size: {width: 24, height: 24}
  padding: 2
  layer: 20
  asset: {provider: native, kind: image, id: 'craftcanvas:crafting_slot_frame'}
  data: {source: grid, index: 0}
```

Use the shipped `item_slot` template to avoid repeating the frame, size and sound settings.

| data.source | Binding |
| --- | --- |
| inventory | index 0..35: storage slots 9..35, followed by hotbar slots 0..8 |
| grid | index 0..columns*rows-1 in row-major order |
| cursor | Hand preview and return action; no index |
| result | Matching recipe output and craft action; no index |

Slots use a square size of 18..128 GUI units in every enabled profile. Padding defaults to 2 reference units, must remain at least one scaled unit and must leave an icon at least 8 units wide. Frames must be unconditional native PNGs with opaque outer edges. Counts appear at the lower right; tooltips show a literal item name, shortened to 24 characters, and the amount. This keeps the inventory inside the signed-book component budget.

The component owns its icon, count and tooltip. It rejects text, children, asset-select, states, hover-overlay, tooltip overrides, align, overflow and child-layout settings. Position, size, layer, padding, color, shadow, conditions, click/denied sounds, cooldown and additional click actions remain available. The built-in transfer runs before additional click actions.

With Nexo automatic pack export, [automatic item models](/guides/item-icons/) prepare flat and cuboid model previews from the combined pack and select them using the ItemStack's item model and CustomModelData. No per-item YAML or manual PNG is needed for supported models. Unsupported custom models show a barrier placeholder and a diagnostic. Without the Nexo import, slots use registered flat vanilla item/block textures; unknown textures use paper. Explicit item icon rules remain available as optional overrides. Resource packs overriding vanilla texture geometry can change sprite metrics and need testing.

## Actions and conditions

| Action | Fields | Behavior |
| --- | --- | --- |
| workbench-clear | none | Cancel the board and Hand; resync displayed storage |
| workbench-craft | none | Craft one currently matching permitted recipe |

These actions require a screen with a workbench definition. Item-slot transfers are registered internally; clients send only the active session's opaque action ticket, never an arbitrary item, quantity or storage index.

Conditions accept `workbench: ready` or `workbench: holding`. For example:

```yaml
conditions:
  enabled: {workbench: ready}
actions:
  click: [{type: workbench-craft}]
```


## Rendering and testing

All registered static sprite choices are prepared when the resource pack is built, in fonts for the required slot positions. New inventory contents do not trigger pack generation during rendering. Counts, sprites and the frame share one action style; the renderer keeps positive positioning offsets outside it.

Edits resend the signed book and can flicker. Appearance, active areas, native cursor behavior and GUI Scale still need verification on your target vanilla clients. The implementation does not add cursor tracking or native container packets.
