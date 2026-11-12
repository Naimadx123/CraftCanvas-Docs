---
title: "YAML reference"
---

Use this page to look up field types, defaults and renderer limits. For a complete menu, start with [1st steps: assets and your first screen](/getting-started/first-screen/). Practical guides cover [components](/guides/components/), [actions](/configuration/actions/), [conditions](/configuration/conditions/), [variables](/configuration/variables/) and [reusable definitions](/configuration/reuse/).

Screen files live recursively under `screens/`. Registries also load `themes/`, `templates/`, `actions/`, `assets/` and `languages/`. Loading is sorted and rejects paths outside the data directory, duplicate keys and YAML collection aliases. Definitions are immutable and shared; state belongs to the player's session. Strict mode rejects unknown fields. Invalid values and unsupported features fail even when strict mode is off. Errors identify file, screen, component and field. Failed reloads retain the previous valid configuration.

At startup, an invalid screen is skipped while valid screens remain available. The console reports each rejected screen; `/cc inspect <screen>` shows its recorded error. For inherited settings, the diagnostic names the affected child, the file containing the invalid value and `inherited-from=<parent ID>`. A child that replaces an invalid parent setting with a valid value can still load. Duplicate IDs reject all definitions with that ID. Errors in shared registries, global configuration or shared pack resources can still prevent startup. Reloads remain atomic: any error rejects the reload and preserves the running configuration and sessions.

Bundled YAML files outside `screens/` gain missing default keys on startup and reload. Maps are traversed recursively; existing scalar values, explicit nulls and complete lists are preserved, along with comments and custom keys. Defaults do not overwrite a scalar/list with a map. This synchronization applies only to paths listed in the JAR's `defaults.index`, not arbitrary user files. Screen files are excluded: examples are seeded once into a fresh empty data directory, and neither removed screens nor new release examples are installed into an existing setup. See [installation behavior](/start/installation/).

All geometry uses integer GUI units, not desktop pixels. The client applies GUI Scale. Profiles are chosen manually; the server cannot query display resolution. Use the shipped `frame` screen as an inherited base. Start with `/cc open home` to browse examples; `selection` demonstrates application-defined option values. `classes`, `simple` and the class-based local-page demo are optional examples. [The annotated example](https://github.com/Naimadx123/CraftCanvas/blob/master/examples/annotated.yml) can be copied into `screens/`.

## General screen examples

A screen is defined entirely by its YAML ID, geometry, components, variables, conditions and actions. Screen state is defined by your own variables and callbacks. Artwork and themes are configurable resources.

- `home`: two-page handheld app launcher, configurable greeting, and a startup animation/sound on the first successful opening per connection.
- `selection`: generic option selection, selected appearance, confirmation and back navigation.
- `settings`: persistent player settings and supported input controls.
- `reward_wheel`: 48-frame animation, six server-selected rewards, and a shared 60-second player cooldown for SPIN and the console A button.
- `sprites`: scalable client item textures, inline sprite tags and native tooltips.
- `crafting`: 5 by 5 crafting board, all 36 inventory slots, held stacks and shaped recipes; see [inventory workbenches](/guides/crafting/).
- `catalog`: pagination, permissions and item tooltips.
- `providers` and `provider_*`: optional resource adapters.
- `classes`: RPG class selection as a showcase; `simple` and `variants` are related demonstrations.

The `navigation_button` template takes `screen`, `label`, `x` and `y`. The `selection_button` template takes `variable`, `value`, `label`, `asset`, `x` and `y`; declare its target string variable in the screen. `class_icon` remains a specialized template used only by the RPG example. Template fields are ordinary parameters, not reserved engine settings.

## Main configuration

The Default column describes parser fallbacks when a field is omitted. The shipped `config.yml` enables `shaders.hover`, `shaders.animations` and `pack.hide-vanilla-book`, and selects `pack.missing: wait`. Existing configuration values are preserved when missing defaults are synchronized.

| Field | Type | Default | Range or behavior |
| --- | --- | --- | --- |
| schema-version | integer | 1 | 1; schema 0 migrates in memory |
| strict | boolean | true | Reject unknown keys |
| persistent-player-data | boolean | true | Enables player variables marked persistent |
| item-icons | ordered map | empty | Optional overrides for item slot icons; Nexo model previews are automatic without rules; see [automatic item models](/guides/item-icons/) |
| shaders.hover | boolean | false | Enable hover overlays; Paper/Folia with matching vanilla clients 1.21.11 through 26.3; rebuild and load the pack after changes |
| shaders.default-hover-overlay | native image ID or empty string | craftcanvas:selected_frame when registered | Automatic outline for eligible text-tooltip controls; empty disables automatic assignment; see [shader hover](/guides/shader-hover/) |
| shaders.animations | boolean | false | Client image atlases and smooth native PNG/text component motion; false uses discrete book updates. Requires a matching vanilla 1.21.11 through 26.3 client; independent of shaders.hover |
| limits.components | integer | 256 | 1..1024 per screen |
| limits.tasks-per-session | integer | 16 | 1..128 delayed jobs |
| limits.updates-per-tick | integer | 10 | 1..100 whole-screen renders |
| profiles | number map | compact: .75, normal: 1, large: 1.25 | Finite scales .5..1.5; supplied map replaces defaults |
| pack.hide-vanilla-book | boolean | false | Replace the shared vanilla book.png with a transparent 256 x 256 texture; shipped file selects true; native controls and dimming remain |
| pack.sender | enum | external | craftcanvas or external; fallback routing when automatic Nexo handling is inactive |
| pack.nexo-auto-export | boolean | true | Enabled Nexo receives an owned ZIP, rebuilds the merged pack and becomes the sole sender |
| pack.id | UUID string | 5d7a408e-fd36-4f13-a1bb-c9acde1b42c8 | Base UUID for standalone, actual sender UUID for external |
| pack.url | string | empty | Direct HTTP(S) ZIP URL used by CraftCanvas sender |
| pack.sha1 | string | empty | Generated hash when empty, otherwise 40 hexadecimal characters |
| pack.missing | enum | deny | deny or wait; shipped file selects wait |

Readiness requires SUCCESSFULLY_LOADED for the expected UUID. With automatic Nexo handling, UUID/hash come from its verified generated/uploaded pack through the public API; the configured standalone URL/hash and external UUID are not used. Set `pack.nexo-auto-export: false` for manual routing. `wait` queues an opening until loading or cancellation. When the effective sender is `craftcanvas` and `pack.url` is empty, opening fails immediately with `PACK_UNAVAILABLE` instead of queuing. Startup logs and administrator command replies point to `plugins/CraftCanvas/craftcanvas.zip` and the pack configuration. Pack-free screen fallbacks are rejected because every menu needs glyph resources. Asset-level fallbacks remain supported.

## Screen fields

`font` is a font resource key, defaulting to `minecraft:default`. It inherits through screen/theme defaults; `<font:namespace:name>` MiniMessage tags override individual fragments. See [font resources and supported providers](/guides/fonts/).

| Field | Type | Default | Meaning |
| --- | --- | --- | --- |
| schema-version | integer | 1 | Schema validation |
| id | string | required | Registry ID independent of filename |
| extends | screen ID | absent | Parent definition |
| theme | theme ID | absent | Shared defaults before local overrides |
| title | formatted string | screen ID | Signed-book title metadata; put a text component inside the panel for a visible heading |
| font | font key | minecraft:default | Base font; MiniMessage font tags override individual fragments |
| backend | enum | book-glyph | book-glyph or auto for menus; cinematic for fullscreen shader playback |
| permission | permission string | absent | Required to open and execute actions |
| requires | string list | empty | Required capabilities; unsupported names/combinations fail |
| layout | map | below | Reference geometry and manual profiles |
| session.history | boolean | true | Up to 64 navigation frames |
| session.refresh-interval-ticks | integer | 0 | Must remain zero: automatic book refresh is unsupported |
| session.timeout-ticks | integer | 12000 | 20..72000 activity timeout |
| variables | declaration map | empty | Typed initial values |
| components | component map | empty | Stable component IDs |
| first-open | optional map | absent | One intro per screen/player connection after the first successful render; see [startup animation](/guides/animations/#first-open-startup-animation) |
| workbench | optional map | absent | Player-inventory reservations, board dimensions and recipes; see [inventory workbenches](/guides/crafting/) |
| book.update | enum | resend | resend or local-pages |
| book.variants | map of variable maps | empty | 1..12 variants for local-pages; forbidden with resend |
| cinematic | map | required for cinematic | Effect/clip timing, assets and lifecycle actions; see [cinematics](/guides/animations/#fullscreen-cinematics) |

Capabilities are glyph-rendering, vanilla-sprites, inventory-slots, component-click, component-tooltip, item-tooltip, asset-selection, session-item-tooltip, selection, absolute-layout, layers, refresh, frame-animation, text, images, no-blur, toggle, choice, stepper, slider, text-input, scroll, search, notification and component-animation. `shaders.animations` provides atlas-animation and smooth-component-animation; `shaders.hover` provides shader-hover-overlay. Requirements are inferred from components and actions. Sliders use discrete clicks without dragging; refresh is action-driven, not polling. `animate` uses an image atlas with `shaders.animations: true` and book resends otherwise. Both require resend update mode. See [renderer constraints](/reference/book-renderer/), [animation limits](/guides/animations/) and [component motion](/guides/interactive-ui/).

Schema 1 uses `backend` and `components`, structured resource references, and declared variables. `renderer`, `elements`, root `state`, shorthand asset strings and `on-click` are not schema 1 aliases. Schema 0 migrates renderer/elements only; use schema 1 for new files.

## Layout

| Field | Type | Default | Range or behavior |
| --- | --- | --- | --- |
| mode | enum | manual | manual, row, column, grid |
| reference-width | integer | 320 | 64..1024 |
| reference-height | integer | 180 | 18..768 |
| columns | integer | 2 | 1..9 |
| gap | integer | 4 | 0..128 |
| profile | profile ID | normal | Must exist in main profiles |
| profiles | profile ID list | empty | Empty allows all; otherwise must include default |
| overlap | enum | allow | allow or reject-interactive |
| anchor | enum | horizontal-center | horizontal-center or book-origin |
| offset-x | integer | 0 | -1024..1024, unscaled book-origin offset |
| offset-y | integer | -30 | -768..768; fixed unless scale-offset-y is true |
| scale-offset-y | boolean | false | Scale the vertical offset relative to the native book top (-30); the shipped frame uses true with offset-y: -48 |

Inherit `frame` or provide an unconditional native background and close control. Backgrounds may be transparent and need not cover native book controls. All allowed profiles are checked for legal bitmap ascent and component bounds. Nested component layouts support only mode, columns and gap. A list forces column children and a grid forces grid children. Row/column/grid placement uses declared sizes. Groups do not clip their children and provide no scroll viewport. No percentages, flex layout, clipping masks or automatic client fit are implied.

## Variables and text

| Declaration field | Type | Default | Constraints |
| --- | --- | --- | --- |
| type | enum | string | string, integer, number, boolean |
| scope | enum | session | session, player, global |
| default | typed scalar | empty string, 0, 0.0, false | String at most 4096 UTF-16 units; finite numbers; integral integers |
| persistent | boolean | false | Only player scope; main persistence must also be enabled |

Variable names begin with a letter and contain letters, digits or underscores. Shared player/global declarations must agree on type. Player variables span that player's screens; global variables span players and remain in memory. Persistent player data uses asynchronous SQL storage configured in `storage.yml` (SQLite by default). See [storage configuration](/configuration/storage/). Navigation restores compatible session variables, selected element, tab and page.

Text placeholders include `{session.name}`, `{player.name}`, `{global.name}`, unqualified declared variables and `{lang.key}`. Reserved placeholders are `{player.name}`, `{player.uuid}`, `{session.selected}`, `{session.tab}`, `{session.page}`, `{session.screen}`; avoid declaring conflicting names. Missing variables become empty text; missing translations use their key. PlaceholderAPI `%...%` values and player input are inserted literally, without MiniMessage reparsing or action authority.

Administrator body text supports MiniMessage colors, bold and registered `<sprite:atlas:sprite>` icons; unsupported formatting fails validation. Tooltip/message formatting can use the supported MiniMessage decorations and native sprite objects. Inline sprites occupy 8 GUI units; larger icons use a vanilla sprite asset. The screen `font` defaults to `minecraft:default`; MiniMessage font tags override fragments. See [screen fonts](/guides/fonts/) for measured advances, resources and glyph/provider limits. External visual glyphs use their own explicit font and metrics. See [vanilla sprites](/guides/sprites/).

## Component fields

Map keys are stable IDs starting with a letter, unique throughout the screen including nested children. Registry IDs accept letters, digits, underscore, period, hyphen and colon.

| Field | Type | Default | Constraints or behavior |
| --- | --- | --- | --- |
| type | enum | text | Types below |
| template | template ID | absent | Expand a reusable component |
| parameters | map | template defaults | Only declared parameters |
| position.x / y | integer | 0 | x 0..1024, y 0..768 |
| position.offset-x / offset-y | integer | 0 | x -1024..1024, y -768..768 |
| position.anchor | enum | top-left | top-left, top-right, bottom-left, bottom-right, center |
| size.width / height | integer | 120 / 18 | width 1..1024, height 1..768 |
| margin / padding | integer | 0 | 0..128; padding must leave positive content area |
| layer | integer | 0 | -1000..1000, stable ascending draw order |
| text | formatted string | empty | Label/content; may contain line breaks |
| search-text | formatted string | component text | Search words for an entry in a scroll container; labels can differ from matching terms |
| animation | component motion map | absent | Finite relative movement/box scaling; duration-ticks, interval-ticks, from, easing, on-open; shaders optional; see [interactive UI](/guides/interactive-ui/) |
| tooltip | formatted string list | empty | Native client text tooltip |
| tooltip-item | item reference | absent | Native item tooltip; mutually exclusive with text tooltip |
| tooltip-item-slot | string | absent | Session item tooltip supplied through the API; text tooltip is the fallback while empty; mutually exclusive with tooltip-item |
| asset | resource reference | absent | Native PNG, registered vanilla sprite or explicit external single glyph; see [sprites](/guides/sprites/) |
| hover-overlay | native resource reference or false | automatic when eligible | Explicit PNG overlay requires shaders.hover and a nonempty text tooltip; false opts this component out of automatic hover; item tooltips and resource fallbacks are unsupported |
| hover-overlay-border | integer | 4 | Source PNG border in pixels, 0..64; nine-slice scaling keeps all four edges at this thickness multiplied by the manual profile; 0 stretches the entire overlay; requires hover-overlay |
| asset-select | selector map | absent | Choose a previously compiled graphic through a declared string variable; mutually exclusive with asset and states.normal.asset |
| autoplay | animation map | absent | Passive image loop: frames (2..128 native PNG references), interval-ticks (2..40, default 2); requires shaders.animations and session.timeout-ticks <= 12000 |
| slider.track / slider.fill / slider.thumb | resource references | required for slider | Opaque native PNGs for inactive cells, filled cells and the selected cell |
| states | appearance map | empty | Overrides below |
| conditions.visible | condition | true | Also controls descendants |
| conditions.enabled | condition | true | Controls action availability |
| conditions.execute | condition | true | Rechecked when activated |
| actions.click | action list | empty | Generic activation only; no mouse-button metadata |
| children | component map | empty | Nested components |
| layout | nested layout | manual, 2 columns, gap 4 | Child composition |
| data | type-specific map | empty | Fields below; unknown entries fail |
| cooldown-ticks | integer | 4 | 0..1200 tick click debounce per component/session |
| cooldown | map | absent | Player cooldown in real seconds, optionally shared by key; see [button cooldowns](/guides/components/#button-cooldowns) |
| cooldown.seconds | integer | 60 | 1..2592000 (30 days) when cooldown is declared |
| cooldown.key | string | screen ID + component ID | Optional shared key: 1..128 letters, digits, underscores, dots, colons or hyphens |
| cooldown.message | formatted string | Try again in {remaining}s. | Blocked-click feedback; {remaining} rounds seconds up; empty string suppresses the message |
| click-sound / denied-sound | namespaced sound key | absent | Click-time sound or denial |
| align | enum | left | left, center, right |
| vertical-align | enum | top | top, center, bottom within the padded text area |
| overflow | enum | truncate | wrap, clip, truncate using whole-character metrics |
| shadow | boolean | false | Native glyph/text shadow |
| color | hex string | absent | Base text color/bitmap tint |
| font | string | absent | Unsupported for book body text; use asset.font for external glyphs |
| refresh-interval-ticks | integer | 0 | Must remain zero |

Anchors refer to the parent box; x/y, offsets and margin apply before anchor transformation. Right/bottom insets move inward; center adds half remaining space. Profiles round geometry after scaling. Text clipping is whole-character layout truncation, not a shader mask. A text-only button has text hitboxes; use an asset for a rectangular button. All elements must draw at or after the background. Use `align: center` and `vertical-align: center` to center a label inside its padded button area.

### Selecting a compiled graphic

`asset-select` contains `variable` (required declared string variable, optionally scope-qualified), `options` (required map of 1..64 string keys to complete graphical resource references), and `fallback` (optional key from options). Keys contain 1..256 characters and may be resource IDs when quoted in YAML. The variable's default must match an option unless a fallback is declared. Each option, including unused ones and resource fallbacks, is validated and compiled at the component's dimensions and position in every allowed profile.

```yaml
variables:
  preview_icon: {type: string, default: available}
components:
  preview:
    type: image
    position: {x: 40, y: 100}
    size: {width: 36, height: 36}
    layer: 20
    asset-select:
      variable: session.preview_icon
      fallback: available
      options:
        available: {provider: native, kind: image, id: 'craftcanvas:slider_fill'}
        claimed: {provider: native, kind: image, id: 'craftcanvas:slider_thumb'}
    tooltip-item-slot: reward
    tooltip: ['<gray>No reward item supplied.']
```

This is a component fragment; retain the screen's background and close control. Change `preview_icon` with set-variable or the public API and use the normal book update mechanism. An unmapped value uses the explicit fallback; without one, rendering fails with the screen, component, variable and value. No placeholder substitution occurs in resource IDs, and an unknown ID cannot request a new bitmap or bypass a provider permission. State-specific selected/disabled/locked/loading assets take priority over the selector; overlays still apply. The root background must remain static; transparent pixels are allowed. External glyph choices retain their exact font, permissions and fixed metrics and may require a single layout profile. Item models cannot be selector options.

`tooltip-item-slot` names an API-populated slot, starting with a letter and containing letters, digits, underscore, period, colon or hyphen, at most 128 characters. Components can share a slot within a screen. Empty slots use the current text tooltip, including state overrides. A populated slot uses the native item tooltip instead of text. Slots are session-local, are cleared when the session ends and are not persisted or copied into navigation history. See [the presentation API](/developers/sessions/#dynamic-graphics-and-session-item-tooltips). The bundled `selection` example demonstrates an asset selector and the `selection_preview` tooltip slot.

### Animating your own components

The `animate` action advances a session string variable through an administrator-defined list of frame IDs. Bind an image's `asset-select.variable` to it and map each ID to a registered graphic. Multiple components can follow the same variable; a text component can also display it with a placeholder. Use your own PNG sequences for opening graphics, changing portraits, blinking icons or other frame-based effects. Geometry remains fixed; movement must be drawn into the frames.

The complete [color-pulse example](https://github.com/Naimadx123/CraftCanvas/blob/master/examples/animation.yml) uses existing UI assets. Copy it to `plugins/CraftCanvas/screens/animation.yml`, retain the bundled `frame` screen and UI assets, reload, let the updated pack load, then run `/cc open animation`. Its Play button can be used again after playback. Replace its selector entries with your own manifest-registered PNGs to use different artwork.

The minimal action is:

```yaml
- type: animate
  variable: animation_frame
  frames: [frame_01, frame_02, frame_03]
  interval-ticks: 4
```

Declare `animation_frame` as a session string variable and provide the corresponding selector options. With `shaders.animations: false`, text can also display its changing value. Atlas playback requires passive images with native PNG assets; text and conditions must use a separate status variable. Frame IDs are arbitrary names, not filenames. `cycles` defaults to one, slowdown defaults to zero, and playback ends on the last frame unless `stop-at` is supplied. Use `random-variable` when playback should end on a server-selected frame.

Action playback is finite, holds the session busy, and permits the close-only X while other actions are blocked. One action timeline runs per session. Passive images can separately use `autoplay` for independent looping atlas animation without a busy lock or book updates. Hover-triggered animation and interpolation of x/y/size are unavailable. Resending the book can flicker, and Escape alone is not reliably observable. See [animation fields, limits and cancellation](/guides/animations/).

### Looping images on screen open

```yaml
components:
  preview:
    type: image
    position: {x: 80, y: 100}
    size: {width: 96, height: 96}
    layer: 20
    autoplay:
      interval-ticks: 2
      frames:
        - {provider: native, kind: image, id: 'craftcanvas:slider_track'}
        - {provider: native, kind: image, id: 'craftcanvas:slider_fill'}
```

Retain the screen's static background and X. With `shaders.animations: true`, the image loops as soon as the book is rendered. Several images can use different frame lists and intervals simultaneously. No session variable, click action or scheduled frame task is required. Images share their session's initial clock; redraws preserve it, while a redraw in another world restarts it. Use a timeout of at most 12000 ticks to stay inside the client clock window.

Autoplay accepts native PNG references without permissions, external font overrides or fallbacks. The image has no asset, selector, text, tooltip, actions, state overrides, children, padding, shadow, enabled/execute condition or click sounds. Color and visibility are supported. Shader displacement does not move native text hitboxes; provide a separate button for clicks and tooltips. An opaque looping image may cover a fixed button at the same position. Transparent images should use a separate control outside the image. All atlas tiles count toward the shared 512-tile limit. Autoplay is rejected when shaders are disabled; it does not fall back to repeated book opens.

## Component types

| Type | Behavior | Data |
| --- | --- | --- |
| panel, group | Background/text plus children | page, tab |
| text | One or multiple text rows | page, tab |
| image, glyph, icon | Explicit graphic | page, tab |
| button | Text/graphic with registered actions | page, tab |
| item-slot | Live inventory, crafting, Hand or result slot | source, index |
| separator | Label or repeated hyphens | page, tab |
| progress, stat | Discrete clamped segment bar | value, max, segments, filled, empty |
| tabs | Child buttons with configured tab actions | page, tab |
| list, grid | Column/grid composition | page, tab |
| pagination | Button(s) with configured page actions | page, tab |
| carousel | Child at page modulo child count | page, tab |
| skill-card | Composed picture/text/children | page, tab |
| confirmation | Button with a configured confirm action | page, tab |
| toggle | Flip bound boolean on click | variable |
| choice | Cycle through string options | variable, options |
| stepper | Increase bounded numeric value; wrap maximum to minimum | variable, min, max, step |
| slider | Click a track cell to choose a numeric value in either direction | variable, min, max, step |
| text-input | Open a native text editor, or explicitly request private chat, then resume book | variable, max-length, mode, prompt |
| search | Edit a session string query through the same dialog/chat input | variable, max-length, mode, prompt, placeholder |
| scroll | Vertical viewport of 1..16 complete rows; entries can be groups | row-height (24), gap (4), optional search-variable |
| notification | Passive slot shown by notify until expiry or dismissal | page, tab; see [interactive UI](/guides/interactive-ui/) |

Composite types use actual child components/actions; they do not create arbitrary native widgets. Tooltip is a field, not a component type. Image buttons are `type: button` with an asset. A close control is a button with a sole unconditional close action.

`item-slot` requires a root `workbench` definition and resend updates. Bind `data.source` to `inventory`, `grid`, `cursor` or `result`; inventory/grid also require an integer `data.index`. Padding defaults to 2. Icons, counts, text tooltips and transfers are generated from the session's reserved ItemStacks. See [inventory workbenches](/guides/crafting/) for slot restrictions, recipes and complete examples.

`data.page` is an integer 0..10000 visibility filter; `data.tab` is a string filter. Progress defaults: value 0, max 100, segments 10, filled `|`, empty `|`. Value may be numeric or dynamic text producing a number; max is positive and segments 1..64. Invalid dynamic numeric output becomes zero. Bars have discrete segments, not client animations.

Controls require a declared variable of matching type/scope. Toggle uses boolean; choice/text-input use string; stepper and slider use integer or number. Choice needs 1..64 unique string options including the default. Text-input max-length defaults to 128, range 1..256 UTF-16 code units. Its mode defaults to dialog; chat explicitly retains private chat replies. Optional prompt is a formatted administrator string, defaulting to Enter a new value. Stepper defaults min 0, max 100, step 1; min < max, step positive and default in range. Integer stepper bounds/steps must be integral. Controls perform their built-in action before any configured click sequence. They cannot also use local-page. See `screens/settings.yml`.

### Native text editor

```yaml
display_name:
  type: text-input
  position: {x: 44, y: 132}
  size: {width: 424, height: 30}
  padding: 8
  layer: 20
  text: '<white>Display name'
  asset: {provider: native, kind: image, id: 'craftcanvas:button'}
  tooltip: ['<gray>Open the text field to edit your display name.']
  data:
    variable: player.display_name
    mode: dialog
    max-length: 32
    prompt: '<white>Display name'
```

Declare display_name as a string variable in the matching scope. The button opens a separate native dialog with the screen's title, the prompt as its field label, the current value, and Save/Cancel. Only the initial field display is trimmed to max-length and stripped of control characters; the variable is untouched until Save. Empty strings are allowed. Oversized input and control characters are rejected by the server. Save then runs actions.click; Cancel skips that sequence and returns without changing the variable. `/cc cancel` works in both modes. The native field is 300 GUI units wide; position/size configure the book button, not a field inside the book. This auxiliary editor has normal dialog styling and blur. See [renderer input limits](/reference/book-renderer/). For private chat replies, use `mode: chat`.

### Click slider

Slider defaults are min 0, max 100, step 5. Bounds and step must be finite, min < max, step > 0, and the variable default must be in range. The range includes both endpoints; if the step does not divide the range, the final interval is shorter. There must be 2..64 selectable values; integer variables require all values to fit a signed 64-bit integer. A persisted value between positions is displayed with the nearest thumb position until the next click. No variable change happens just from opening the screen.

```yaml
volume:
  type: slider
  position: {x: 44, y: 216}
  size: {width: 424, height: 44}
  padding: 8
  layer: 20
  text: '<white>Volume'
  slider:
    track: {provider: native, kind: image, id: 'craftcanvas:slider_track'}
    fill: {provider: native, kind: image, id: 'craftcanvas:slider_fill'}
    thumb: {provider: native, kind: image, id: 'craftcanvas:slider_thumb'}
  tooltip: ['<gray>Click a position to set the value.']
  click-sound: minecraft:ui.button.click
  data: {variable: volume, min: 0, max: 100, step: 5}
```

Declare `volume` as an integer or number variable on the screen. Slider assets must be fully opaque native PNGs with no permission or fallback. They are rescaled into cells by the pack generator; external fixed glyphs cannot supply these variable-width cells. The label occupies one 8-unit text row, followed by a 4-unit gap. After scaled padding, track height must be 4..128 GUI units and width must allow at least three GUI units per selectable value in every allowed profile. Each cell reserves its font advance edge, keeping adjacent hit areas separate. Disabled, locked and loading sliders register no value actions; their cells use a gray tint unless an explicit state/base color overrides it. The `slider` map accepts only track, fill and thumb, and sliders cannot have children.

Each cell has an opaque session-bound action token and a native tooltip showing its value. Clicking rechecks permissions, conditions and the component cooldown, sets the bound variable, then runs `actions.click`. The existing book update mode displays the changed fill/thumb; `resend` reopens the book and can flicker. No drag, mouse-motion, wheel or right-click interaction is exposed. The settings example changes CraftCanvas's `player.volume` variable; it does not change Minecraft's client audio settings.

## Appearance states

Names are normal, selected, disabled, locked and loading, with priority locked, disabled, loading, selected, normal. Overrides: text, color, asset, overlay, tooltip and when. `when` applies only to selected/locked. Overlays use the same supported graphical resource kinds. Defaults fill absent overrides. Locked/disabled/loading elements register no server action. Selected defaults to the session's selected component ID unless a condition is provided.

Hover/pressed states, hover-image, hover-sound, on-hover, drag/scroll/right/shift/double-click callbacks and automatic refresh are rejected. Native tooltips and cursor behavior are distinct from persistent selection. Component-level `animation`/`animations` fields remain invalid; use the finite [`animate` action](/guides/animations/).

## Themes, templates, actions and languages

Theme fields: schema-version, id, optional extends, screen-defaults and component-defaults. Template fields: schema-version, id, optional extends, parameters and component. Exact `{{parameter}}` tokens preserve scalar type; embedded tokens stringify it. Missing/undeclared parameters fail.

Action-file fields: schema-version, id and actions. `{ref: action_id}` expands a shared list. Language fields: schema-version, locale and entries; player locale is selected with en_US fallback. Entries use trusted administrator formatting.

Maps merge recursively with local values winning; lists replace completely. References use registry IDs, not arbitrary file paths. Inheritance, template expansion, shared-action reference cycles fail. See the [action examples](/configuration/actions/), [conditions](/configuration/conditions/) and [reusable definitions](/configuration/reuse/).

## Resources

A reference contains provider (default native), kind (default glyph), required id, optional font, character, metrics, permission and fallback. Kinds: glyph, image, sprite, item, font, sound. Providers: native, vanilla, nexo, craftengine, itemsadder or a registered API provider. Visual fallbacks may interchange image/glyph/sprite; item/font/sound preserve kind.

Metrics fields: width 1..1024 (default 16), height 1..768 (default 16), ascent -768..768 (default 7; cannot exceed height), advance 1..1025 (default width+1). Native metrics are generated and overrides rejected. External glyphs require fixed metrics matching component width/height and effective y = 7-ascent; profiles cannot rescale their fonts. Provider restrictions are in [INTEGRATIONS.md](/resources/integrations/).

Manifest fields: schema-version, images, sounds. Image entries contain file, width and height (defaults 16, bounds 1..2048); actual PNG dimensions must match. Sound entries contain an OGG file. Paths remain inside assets/ and native IDs use `craftcanvas:`. A native sprite is a static local PNG. A vanilla sprite references a registered client texture, for example `{provider: vanilla, kind: sprite, id: 'minecraft:item/diamond'}`; square size 1..128 in every profile, no font/character/metrics overrides or local PNG required. Items appear in tooltip-item or item actions; arbitrary item models are not automatically converted into bitmaps.

Built-in fonts are craftcanvas:canvas, craftcanvas:space and generated craftcanvas:text/y<row>. Screen fonts supply measured body metrics; internal positioned font names are generated automatically. See [PACK.md](/resources/resource-pack/) for adding graphics, preserving allocation registries, generating ZIPs and selecting one sender.
