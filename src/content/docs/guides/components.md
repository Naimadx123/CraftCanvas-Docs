---
title: "Build a screen from components"
---

For a 5 by 5 crafting board displaying the player's actual inventory, with click transfers and recipes, see [inventory workbenches](/guides/crafting/).

Each example below is a fragment for a screen extending `frame`. Add component maps under `components` and variables under the screen's root `variables`. Keep the inherited background and X. Full field types and ranges are in the [YAML reference](/reference/yaml/).

## Text, images and tooltips

```yaml
components:
  description:
    type: text
    position: {x: 44, y: 90}
    size: {width: 400, height: 32}
    layer: 20
    text: '<gold>Travel guide<newline><white>Choose your next destination.'
    overflow: wrap
  map_icon:
    type: image
    position: {x: 44, y: 140}
    size: {width: 48, height: 48}
    layer: 20
    asset: {provider: native, kind: image, id: 'craftcanvas:archer'}
    tooltip: ['<gold>Travel guide', '<gray>Inspect the available routes.']
```

Replace the example icon with your [own PNG](/resources/resource-pack/#add-a-png) or a [vanilla sprite](/guides/sprites/), such as `{provider: vanilla, kind: sprite, id: 'minecraft:item/diamond'}`. `image`, `glyph` and `icon` use explicit graphic references. Image type does not convert an item model into a bitmap. Add `tooltip-item: {provider: vanilla, kind: item, id: MAP}` for a native item tooltip instead of the text tooltip. An addon can fill a declared `tooltip-item-slot` dynamically.

## Buttons and selection

```yaml
variables:
  destination: {type: string, default: spawn}
components:
  market:
    type: button
    position: {x: 44, y: 140}
    size: {width: 240, height: 32}
    padding: 8
    layer: 20
    text: '<white>Market'
    asset: {provider: native, kind: image, id: 'craftcanvas:button'}
    tooltip: ['<gray>Select the market.']
    states:
      selected:
        when: {variable-equals: {name: destination, value: market}}
        overlay: {provider: native, kind: image, id: 'craftcanvas:selected_frame'}
      disabled:
        color: '#555555'
        tooltip: ['<gray>You cannot use this route.']
    conditions:
      enabled: {permission: server.travel.market}
    actions:
      click: [{type: set-variable, name: destination, value: market}]
```

The overlay persists after selection. Tooltips and cursor changes are native local hover behavior. Optional [shader hover](/guides/shader-hover/) can add a temporary `hover-overlay` on supported vanilla clients from 1.21.11 through 26.3; `hover-image` and `on-hover` remain unsupported. A PNG-backed button has a rectangular font hitbox, including transparent pixels and its advance edge. A text-only button uses the text's area instead of the whole declared box.

## Groups, rows, columns and grids

```yaml
components:
  routes:
    type: grid
    position: {x: 44, y: 90}
    size: {width: 424, height: 100}
    layer: 20
    layout: {mode: grid, columns: 2, gap: 8}
    children:
      north:
        type: text
        size: {width: 200, height: 24}
        text: '<white>North route'
      south:
        type: text
        size: {width: 200, height: 24}
        text: '<white>South route'
```

`list` arranges children in a column. A `group` or `panel` can use `layout.mode: row`, `column`, `grid` or `manual`; panel/group backgrounds use their own asset. Child coordinates are relative to the parent. Groups do not clip or scroll children. Use a [scroll viewport](/guides/interactive-ui/) for complete rows, optionally filtered by a search control. `skill-card` is a composition container for an icon, description and other child components; it uses the same fields.

## Progress and statistics

```yaml
variables:
  progress: {type: number, default: 35.0}
components:
  meter:
    type: progress
    position: {x: 44, y: 100}
    size: {width: 360, height: 24}
    layer: 20
    text: '<white>Progress: '
    data: {value: '{progress}', max: 100, segments: 20, filled: '|', empty: '|'}
  divider:
    type: separator
    position: {x: 44, y: 140}
    size: {width: 360, height: 16}
    layer: 20
```

`stat` uses the same segmented bar data. Values clamp to the displayed range; changing a variable redraws it through the normal update mode. A separator with no text draws hyphens. These are text/glyph compositions with discrete segments.

## Tabs, pages and carousels

A `tabs` component contains buttons with `actions.click: [{type: tab, value: equipment}]`. Mark its content with `data: {tab: equipment}`. To use pages, add `data: {page: 0}` or `data: {page: 1}` to content and a `pagination` button with `actions.click: [{type: page, value: next}]`.

A `carousel` contains image/card children and displays the child at the current page modulo child count. Page actions control which child appears. Both navigation styles use regular buttons and server state; they do not provide swipe or wheel input. See the [catalog](https://github.com/Naimadx123/CraftCanvas/blob/master/src/main/resources/screens/catalog.yml) for complete page buttons and [classes](https://github.com/Naimadx123/CraftCanvas/blob/master/src/main/resources/screens/classes.yml) for composed portraits, stats and skill tooltips.

## Confirmation and close

`confirmation` is a button with a configured `confirm` action. Its `on-confirm` list performs the guarded operation; Cancel returns to the prior screen. See the [purchase example](/configuration/actions/#confirm-an-economy-purchase).

The root X is a button with only `actions.click: [{type: close}]`. The inherited `frame` includes one. It must remain unconditional so a busy, disabled or locked screen can still close.

## Input controls

| Component | Use | Example |
| --- | --- | --- |
| toggle | Boolean preference | [Settings notifications](https://github.com/Naimadx123/CraftCanvas/blob/master/src/main/resources/screens/settings.yml) |
| choice | Cycle through declared strings | [Settings difficulty](https://github.com/Naimadx123/CraftCanvas/blob/master/src/main/resources/screens/settings.yml) |
| stepper | Increase a bounded number, wrapping at the maximum | `data: {variable: amount, min: 1, max: 10, step: 1}` with an integer variable |
| slider | Choose a numeric cell by clicking | [Slider fields and example](/reference/yaml/#click-slider) |
| text-input | Edit a string in a separate native dialog | [Text editor fields and example](/reference/yaml/#native-text-editor) |

Controls perform their built-in mutation before their configured click actions. Settings variables belong to CraftCanvas or your addon; they do not directly change vanilla client options. Slider dragging, inline typing in the book, raw keyboard input and mouse-button-specific triggers are unavailable.
