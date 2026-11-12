---
title: "Animations"
---

For position/box-scale animations of individual UI components, see [component animations](/guides/interactive-ui/#component-animations). They use smooth client motion with `shaders.animations: true` and discrete updates otherwise, separately from the image-frame playback below.

`animate` plays a finite sequence of frame IDs stored in a session string variable. Bind that variable to `asset-select` to animate an image. Resources are prepared when building the pack. Playback follows `shaders.animations` in `config.yml`:

| Setting | Playback |
| --- | --- |
| `false` | Changes the variable and reopens the book for each frame |
| `true` (shipped configuration) | Displays a texture atlas and lets the client shader select frames; sends the start and final state |

Both modes use the same action, timing, cycles and server-selected `stop-at`. Both require `book.update: resend`; `local-pages` is rejected. Unrelated state updates can still resend a book during atlas playback without resetting its start time.

Screen IDs, variable names, frame IDs and resource references are supplied by YAML. Several components can follow the same variable to change together. The [standalone color-pulse example](https://github.com/Naimadx123/CraftCanvas/blob/master/examples/animation.yml) demonstrates a reusable image sequence; [the YAML guide](/reference/yaml/#animating-your-own-components) explains adding your own artwork.

| Field | Type | Default | Limits and meaning |
| --- | --- | --- | --- |
| variable | string | required | Declared session string variable, optionally prefixed `session.` |
| frames | string list | required | 2..128 unique nonempty frame IDs, each at most 128 characters; selector options must cover them unless a fallback is declared |
| cycles | integer | 1 | 1..8 total passes; cycles minus one full passes, then a final pass ending at stop-at |
| interval-ticks | integer | 3 | 2..40 initial ticks per frame; at most 10 requested frame changes per second at 20 TPS |
| slowdown-ticks | integer | 0 | 0..40 extra ticks, increasing linearly across playback |
| stop-at | string | last frame | A declared frame ID or an exact placeholder for a declared string variable; resolved once when playback starts |

The longest possible playback must contain at most 256 displayed frames and 1200 nominal ticks. One owner-scheduled task is active at a time, counted against the session task limit. Server book updates obey the whole-screen render budget; atlas frames between these updates do not consume that budget. Playback waits for pending server renders and abandons playback after its nominal duration plus 100 ticks of tolerance. Server rendering is not a client acknowledgement. Low TPS or competing renders can delay or abort playback. The sequence remains busy while playing, blocking other actions except the unconditional X; following actions run once after the final frame has been held. Stop-at values outside frames fail without scheduling playback.

```yaml
- type: animate
  variable: animation_frame
  frames: [frame_01, frame_02, frame_03]
  interval-ticks: 4
```

Declare `animation_frame` as a session string variable and bind it to graphics. This plays once at a fixed interval and ends on `frame_03`. Slowdown, cycles and `stop-at` are optional. One action timeline plays per session; use passive `autoplay` images for independent decorative loops. For interpolated movement/box scaling, use [component animation](/guides/interactive-ui/#component-animations). With `shaders.animations: false`, text can also display the changing frame variable.

For randomized playback, first use `random-variable` on a separate session string variable, then supply it as `stop-at: '{result}'`. Every possible result must be a declared frame ID. `reward_wheel` combines these actions with a 48-frame, six-prize example with equal server-selected probabilities. It grants one item before playback. SPIN and the physical A button share `cooldown: {seconds: 60, key: reward_wheel}` for each player, so reopening cannot grant another item during the minute. The temporary `spun` flag is cleared after playback, allowing another spin in the same screen after the cooldown expires. The deadline starts before delivery; a full inventory or later action failure still consumes that attempt. With `persistent-player-data: true`, it survives reconnects and restarts. For paid spins, daily quotas or exact-once rewards, use an addon-owned transaction ledger. See [button cooldowns](/guides/components/#button-cooldowns).

With `shaders.animations: false`, each frame uses `openBook` and can flicker or reset focus. When enabled, the atlas runs locally between the start and final book updates. X, `/cc close`, disconnect, navigation, reload and disable cancel pending tasks. Close-only buttons retain valid close keys across redraws; other keys remain single-use. Escape does not reliably notify the server: a later frame in resend mode, or the final update in atlas mode, can reopen the book until gameplay resumes or the session is retired.

## First-open startup animation

The bundled `home` screen plays a 50-tick startup sequence and `craftcanvas:handheld_startup` sound on its first successful render for each player connection. Reopening home in the same connection skips it. Reconnect and use `/cc open home` to replay it. Queued, cancelled and failed opens do not consume the first opening. Closing a running intro stops its sound and does not replay it during that connection.

Use `first-open` on another book screen in the same way. This complete example inherits the bundled frame and sound:

```yaml
schema-version: 1
id: startup_demo
extends: frame
first-open:
  variable: booting
  duration-ticks: 50
  sound: craftcanvas:handheld_startup
  volume: 0.7
  animations: [boot_logo]
variables:
  booting: {type: boolean, default: false}
components:
  heading:
    text: '<aqua>Ready'
    conditions:
      visible: {variable-equals: {name: booting, value: false}}
  boot_logo:
    type: text
    position: {x: 112, y: 128}
    size: {width: 288, height: 20}
    layer: 20
    align: center
    vertical-align: center
    text: '<aqua><bold>CRAFTCANVAS'
    conditions:
      visible: {variable-equals: {name: booting, value: true}}
    animation:
      on-open: false
      duration-ticks: 30
      interval-ticks: 2
      easing: ease-out
      from: {y: 12, scale: 1.0}
```

The variable must be a session boolean with default `false`. `duration-ticks` accepts 2..1200 (default 50), `volume` accepts 0..1 (default 0.7), and `sound` is an optional namespaced sound ID. Native sounds must exist in an asset manifest. Each ID in `animations` must name a component with `animation.on-open: false`; its animation duration must fit inside the intro duration. Normal actions are blocked during the intro, while the unconditional close control remains usable. Shader animation is smooth when enabled and uses the discrete fallback otherwise.

## Enable atlas playback

```yaml
shaders:
  hover: false
  animations: true
```

This requires Paper/Folia and a matching vanilla client **1.21.11 through 26.3**. Reload CraftCanvas and load the rebuilt pack. Default-file updates migrate the legacy `experimental.core-shaders` and `experimental.shader-hover` settings into `shaders.hover` and `shaders.animations`, preserving their value for both flags. Explicit new values take precedence; the compiler also accepts legacy files before migration. `shaders.animations` controls atlas playback and supported component motion independently of [hover overlays](/guides/shader-hover/), which use `shaders.hover`.

Atlas followers must be passive `image` components with an `asset-select` bound to the animated variable. Frames require native PNG image/glyph/sprite assets. Static component colors and layout profiles are applied when generating the atlas. Text, actions, tooltips, shadows and state overrides on these images are rejected. Keep labels and clickable controls in separate components. Text and conditions cannot depend on the animated variable in this mode; use a separate status variable and change it before/after playback. Unsupported bindings fail validation rather than silently freezing or falling back to resends.

Image tiles are packed into 240 x 240 textures that fit Minecraft's 256 x 256 font atlas. The generated pack allows at most 512 animation tiles across its screens and profiles. Reduce dimensions, frames or profiles if this limit is exceeded. GIFs and `.png.mcmeta` frame declarations are not read by the generator.

## Autoplay images

For looping cards, animated backgrounds inside a menu or several previews playing together, set `autoplay` on a passive `image`. Its `frames` list contains 2..128 native PNG resource references, and `interval-ticks` is 2..40 (default 2, or 10 FPS at 20 TPS). See [the complete YAML fragment](/reference/yaml/#looping-images-on-screen-open).

Each image loops independently using the client clock. It starts with the book's initial render and has no busy lock, completion actions, variable updates or server frame tasks. Buttons remain usable while it runs. A normal redraw preserves the clock; no periodic redraw is scheduled. Closing, navigation, reload and disconnect discard the session clock. Session timeout must be at most 12000 ticks. Core shaders and a matching supported vanilla client (1.21.11 through 26.3) are required; unsupported configurations fail validation.

Keep interaction in separate components. Moving shader pixels do not provide corresponding hit areas. A fixed button can sit underneath an opaque animation; for transparent artwork, place the control outside the image. This feature does not make `animate` sequences run concurrently or execute actions when particular frames are displayed.

The shader reads Minecraft's normal `GameTime` clock. Start time and final choice travel in marker glyphs; CraftCanvas does not send fake time packets or change world time. The server owns rewards, eligibility and action completion. Network delay, low TPS or a modified client can affect visible timing; exact frame display is not acknowledged by the client. Verify appearance, GUI scales, timing and closing in the actual game before deployment.

## Connect frames to artwork

Add these fields to a screen that extends `frame`. This uses existing UI assets; replace the option resources with your own [manifest images](/resources/resource-pack/#add-a-png).

```yaml
variables:
  animation_frame: {type: string, default: dim}
components:
  indicator:
    type: image
    position: {x: 80, y: 100}
    size: {width: 64, height: 64}
    layer: 20
    asset-select:
      variable: animation_frame
      options:
        dim: {provider: native, kind: image, id: 'craftcanvas:slider_track'}
        bright: {provider: native, kind: image, id: 'craftcanvas:slider_fill'}
  play:
    type: button
    position: {x: 80, y: 190}
    size: {width: 240, height: 32}
    layer: 20
    text: '<green>Play'
    asset: {provider: native, kind: image, id: 'craftcanvas:button'}
    actions:
      click:
        - type: animate
          variable: animation_frame
          frames: [dim, bright]
          interval-ticks: 8
          cycles: 2
        - {type: message, text: '<green>Animation finished.'}
```

Reload, publish the pack and click Play. Several images can follow the same variable. The [complete pulse example](https://github.com/Naimadx123/CraftCanvas/blob/master/examples/animation.yml) and the shipped `reward_wheel` screen work in both modes without changing their animation actions.

## Randomize the final frame

Declare a second session string variable, for example `animation_result`, and add:

```yaml
- type: random-variable
  name: animation_result
  values: [dim, bright]
- type: animate
  variable: animation_frame
  frames: [dim, bright]
  cycles: 3
  interval-ticks: 3
  slowdown-ticks: 6
  stop-at: '{animation_result}'
```

The server chooses a value uniformly before playback. Every possible result must be present in `frames`. Reward delivery and eligibility are separate actions or addon callbacks; animation alone does not give items.

## Fullscreen cinematics

`backend: cinematic` displays a noninteractive HUD overlay through Adventure titles. It does not open a book or inventory, change held items, pause gameplay or capture input. It requires `shaders.animations: true`, supported Paper/Folia 1.21.11 through 26.3 and a matching vanilla client with the current pack. Profiles do not resize it: the shader fits the viewport.

Copy [examples/cinematics](https://github.com/Naimadx123/CraftCanvas/tree/master/examples/cinematics) into `plugins/CraftCanvas/screens/cinematics/`, reload and load the pack. Open a preset with `/cc open cinematic_<effect>`, such as `/cc open cinematic_portal`. Presets are supplied for every effect below; use `cinematic_wipe_left` for `wipe-left`, and `cinematic_clip` for `frames`. Transition variants inherit `cinematic_eyes`, so retain that file. Its inherited actions play sounds and teleport the player; change the teleport destination or replace the action lists before using them on your server. The clip uses the bundled wheel artwork as a frame-sequence example.

```yaml
schema-version: 1
id: arrival
backend: cinematic
session:
  timeout-ticks: 400
cinematic:
  effect: eyes
  close-ticks: 12
  hold-ticks: 60
  open-ticks: 20
  color: '#000000'
  softness: 0.008
  actions:
    start:
      - {type: sound, id: minecraft:block.beacon.deactivate}
    uncovering:
      - {type: sound, id: minecraft:block.amethyst_block.chime}
    complete:
      - {type: message, text: '<green>Welcome back.'}
```

Closing/opening use smooth easing evaluated per client render. `start` runs once immediately after the initial HUD title is sent, during the beginning of closing or movie playback. Lease renewals and world-clock rebasing do not repeat it. `covered` runs once after the server closing interval and after start actions finish. `uncovering` runs once when opening starts, at `close-ticks + hold-ticks`, after covered actions finish. `complete` runs once after the full interval and after earlier lifecycle actions finish, then the session closes. Opening another configured screen from complete actions chains effects. Conditions and registration ownership use the normal action engine. Closing/replacing the session cancels remaining work; a failed lifecycle action closes the cinematic.

Use `uncovering` for a sound or actionbar while eyes, curtains or another transition open. It accepts `delay` to place actions later within opening:

```yaml
cinematic:
  effect: eyes
  close-ticks: 12
  hold-ticks: 60
  open-ticks: 20
  actions:
    covered:
      - {type: console-command, command: 'tp {player.name} -558 81 -273'}
    uncovering:
      - type: delay
        ticks: 5
        actions:
          - {type: sound, id: minecraft:block.amethyst_block.chime, volume: 0.5, pitch: 0.8}
```

Here the teleport runs while covered; the sound runs about five server ticks after opening starts. Delays do not pause or extend the shader animation. A long delay can run after the visual effect has finished; complete actions wait for it, subject to the session timeout. Start and covered actions still reject delays. Use `hold-ticks` for the covered interval and `uncovering` for actions during the opening interval. Slow asynchronous actions can postpone later lifecycle callbacks; the client animation continues on its own clock.

| Effect | Appearance |
| --- | --- |
| eyes | Curved upper and lower eyelids |
| fade | Whole-screen color fade |
| iris | Circular opening shrinks to the center, then expands |
| diamond | Diamond-shaped opening shrinks, then expands |
| curtain | Left and right curtains close toward the center |
| wipe-left | Cover advances from right to left |
| wipe-right | Cover advances from left to right |
| wipe-up | Cover advances from bottom to top |
| wipe-down | Cover advances from top to bottom |
| frames | Numbered PNG frames played from tiled atlases |
| portal | Shrinking circular aperture with rotating rune bands and luminous rings |
| spiral | Three twisting arms spread across the viewport and unwind on reveal |
| ink | An irregular, noise-shaped ink front flows across the viewport |
| glitch | Staggered moving bands with colored fringes and intermittent overlay stripes |
| shutter | A rotating six-blade polygon aperture with blade seams |
| tiles | Square tiles grow with staggered arrival times, then shrink on reveal |
| seal | Rings, runes and a rotating six-point seal draw in before a radial cover and accent pulse |
| comic | Alternating diagonal panels slide in with borders and halftone dots |

| Cinematic field | Type | Default | Range and availability |
| --- | --- | --- | --- |
| effect | enum above | eyes | All cinematics |
| close-ticks | integer | 10 | 0..12000; transitions only |
| hold-ticks | integer | 40 transitions, 0 frames | 0..12000; fully covered transition or final movie frame |
| open-ticks | integer | 20 | 0..12000; transitions only |
| color | string | '#000000' | #RRGGBB; transition color or movie background |
| accent-color | string | '#C6A2FF' | #RRGGBB; rings, borders and decorative details; the eight effects from portal through comic only |
| direction | enum | clockwise for portal/spiral/shutter/seal, right for ink/glitch/tiles/comic | clockwise/counterclockwise for rotating effects; right/left/up/down for directional effects; rejected on other effects |
| intensity | number | 0.7 | 0..1; detail strength and effect-specific variation; the eight effects from portal through comic only |
| softness | number | 0.008 | 0..0.25 of the shorter GUI dimension; transition edge softness; unavailable for fade/frames; fancy effects also use derivative-based edge antialiasing |
| frames | string list | required for frames | 2..256 native craftcanvas: PNG IDs in playback order; equal dimensions |
| fps | integer | 24 | 1..60; frames only; presentation depends on client FPS and world-clock updates |
| fit | enum | contain | frames only; contain adds background borders, cover crops, stretch fills independently on each axis |
| actions.start | action list | empty | Transitions and movies; fires once when the initial HUD title is sent; delay is unavailable here |
| actions.covered | action list | empty | Transitions only; fires after closing; delay is unavailable here |
| actions.uncovering | action list | empty | Transitions with open-ticks greater than zero; fires when opening starts; supports session-owned delays |
| actions.complete | action list | empty | Fires after playback; delays remain subject to the session timeout |

Total duration must be 1..12000 ticks. For movies it is `ceil(frame-count * 20 / fps) + hold-ticks`. Session timeout must exceed duration by at least 20 ticks; allow extra time for delayed uncovering or complete actions. Normal ID, permission, title metadata, variables, requirements and session settings are supported. Menu themes, layout, components, book and workbench fields are rejected. Capabilities are cinematic, fullscreen, atlas-animation, procedural-transitions and no-blur; there are no clicks/tooltips. Title, input, confirm and animate lifecycle actions are rejected because they replace the HUD channel or require menu components.

## Customize a procedural transition

The eight decorative effects need no artwork or frame sequence. Create a standalone `screens/arrival_portal.yml`:

```yaml
schema-version: 1
id: arrival_portal
backend: cinematic
session:
  timeout-ticks: 400
cinematic:
  effect: portal
  close-ticks: 32
  hold-ticks: 40
  open-ticks: 32
  color: '#090413'
  accent-color: '#BB86FF'
  direction: clockwise
  intensity: 0.85
  softness: 0.008
  actions:
    start:
      - {type: sound, id: minecraft:block.beacon.deactivate, pitch: 0.8}
    uncovering:
      - {type: sound, id: minecraft:block.amethyst_block.chime, volume: 0.5}
```

Run `/cc reload`, load the rebuilt pack and use `/cc open arrival_portal`. Changing an effect, its colors, direction, intensity or timing requires rebuilding and loading the pack. The same lifecycle actions work for all transition effects. The viewport is fully transparent at the beginning and end, and fully opaque during the covered interval. Decorative details fade away at full cover.

Intensity controls rune/glow brightness for portal, arm twist and edge color for spiral, front irregularity for ink, band displacement/density and color for glitch, blade decoration for shutter, tile arrival randomness for tiles, seal brightness/pulse for seal, and panel staggering/decoration for comic. A value of zero simplifies the appearance; it does not skip the transition. Direction controls rotation or cover travel; revealing reverses coverage while time-based decorative motion continues.

These are overlay effects. Glitch does not distort or recolor the world's rendered image; tiles and comic panels do not capture fragments of the world. Custom images belong in `effect: frames`. Shader extensions use versioned resource-pack overlays for matching supported vanilla clients from 1.21.11 through 26.3.

## Import a movie

Convert movies to PNG sequences before pack generation. MP4 playback, streaming and embedded audio are unavailable. At 24 FPS, 256 frames cover about 10.7 seconds. Reduce FPS or split longer footage into separate screens. Start with small frames such as 160 x 90: frame count/resolution increase pack size and GPU memory. The combined cinematic limit is 512 tiles, separate from menu animation tiles.

Create a `frames` directory and export a short clip using an external tool such as FFmpeg. Its [fps and scale filters](https://ffmpeg.org/ffmpeg-filters.html) control frame rate and dimensions:

```text
ffmpeg -n -i intro.mp4 -t 8 -vf "fps=24,scale=160:-2" frames/frame_%04d.png
python tools/import_cinematic.py --frames frames --name intro --fps 24 --fit contain --output exported-intro
```

The [importer](https://github.com/Naimadx123/CraftCanvas/blob/master/tools/import_cinematic.py) needs Python 3 without extra packages. It checks frame count, dimensions and the per-clip tile budget, gives files English numbered names, and writes a manifest plus screen YAML under `exported-intro/assets/` and `exported-intro/screens/`. Existing output resources are protected. Copy both directories into `plugins/CraftCanvas/`, reload, load the pack and run `/cc open cinematic_intro`. Reload checks the combined tile limit and decodes PNGs. Transparent movie pixels are composited over the background color. Use `actions.start` to start a sound with the movie. Sounds are not extracted from video or synchronized automatically.

## Cinematic limitations

The title channel is shared with other plugins; competing titles can replace the overlay. CraftCanvas renews its lease every 20 ticks without restarting the clock. The lease lasts 60 client ticks, so an overlay expires when updates stop, even if cleanup cannot reach the player. Normal close clears it immediately. F1, other GUIs, client pause/render behavior and modified clients can hide or affect it. This is not an enforced blackout or movement lock.

The server schedules lifecycle actions but receives no confirmation of a displayed frame or full opacity. Network delay and TPS affect synchronization. World changes rebase the clock on the next HUD update, preserving server elapsed time, but are not guaranteed seamless. Do not treat visible coverage as an acknowledgement of a teleport or reward. Reload, resource reload, navigation, disconnect, timeout and `/cc close` cancel the session. Players can keep moving and opening other interfaces.

The fragment shader handles marked cinematic glyphs; unmarked text retains its normal path. With Nexo 1.29.1, both stages compose with its verified GIF shader pair. Unknown shaders are rejected. Test fullscreen coverage, GUI Scale, title ordering, F1, closing, reconnecting and competing title plugins in a vanilla client before deployment. Standalone GPU checks do not verify those Minecraft behaviors.

If transitions are invisible and movie borders contain repeated image fragments with Nexo, verify that the current plugin and rebuilt merged pack are loaded. `/cc debug capabilities` should include `cinematic masks active` in the shader diagnostic. Nexo imports font JSON into its typed font registry; CraftCanvas checks that registry and raw files in active overlays before composing the fragment shader. A missing expected font stops generation instead of silently omitting masks.
