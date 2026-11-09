---
title: "Scrolling, search, component motion and notifications"
---

These four features work with `shaders.hover: false` and `shaders.animations: false`. They use the normal resource pack and `book.update: resend`. With `shaders.animations: true`, supported component motion runs smoothly on the client; scrolling, search and notification lifetime remain server state. Enabling only `shaders.hover` does not enable motion shaders.

Copy [interactive_ui.yml](https://github.com/Naimadx123/CraftCanvas/blob/master/examples/interactive_ui.yml) into `plugins/CraftCanvas/screens/`, keep the bundled `frame` screen and its assets, run `/cc reload`, load the new pack, then run `/cc open interactive_ui`. New positions and motion sizes are compiled into the pack; adding or changing them requires a rebuilt pack.

## Scroll containers

`type: scroll` is a vertical viewport of complete fixed-height rows. Its direct children are the entries; an entry can be a button or a group containing several components. Entries beyond the viewport are neither rendered nor given click tickets. Filtered and conditionally hidden entries do not reserve rows. Each player has an independent scroll offset.

```yaml
results:
  type: scroll
  position: {x: 24, y: 120}
  size: {width: 432, height: 108}
  data: {row-height: 24, gap: 4, search-variable: query}
  children:
    sword:
      type: button
      size: {width: 432, height: 24}
      padding: 4
      layer: 20
      text: '<aqua>Diamond sword'
      search-text: 'diamond sword weapon'
      asset: {provider: native, kind: image, id: 'craftcanvas:button'}
      actions:
        click: [{type: select, value: sword}]
```

This viewport holds four rows. `row-height` defaults to 24 and accepts 8..768; `gap` defaults to 4 and accepts 0..64. The viewport must fit 1..16 rows after its own padding. Every entry, descendant and motion frame must fit wholly within its row; arbitrary pixel clipping, partial rows, nested scroll containers and mouse-wheel input are not supported. A scroll container owns its row layout; do not set another child layout or put click actions on the container itself.

Use ordinary buttons with `{type: scroll, target: results, rows: 1}` and `rows: -1`. The signed nonzero count is limited to -128..128. Offsets clamp at the first/last full viewport. Buttons remain available at the bounds; the scroll action then leaves the offset unchanged. `{scroll.results.offset}`, `{scroll.results.total}` and `{scroll.results.visible}` expose the current zero-based offset, matching entry count and visible entry count in component text.

Moving graphics must be native PNGs or vanilla sprites. Explicit external glyph metrics describe a fixed position and cannot be reused at arbitrary rows. Larger entry sets, profiles and motion frames increase compiled resource counts; the existing glyph/page limits still apply.

## Search

Declare a session string variable, bind a `search` control to it and reference the same variable from the scroll container's `data.search-variable`:

```yaml
variables:
  query: {type: string, scope: session, default: ''}
components:
  search:
    type: search
    position: {x: 24, y: 80}
    size: {width: 320, height: 28}
    padding: 6
    layer: 20
    text: '<white>Search'
    data:
      variable: query
      placeholder: All items
      prompt: Search items
      max-length: 128
      mode: dialog
```

Clicking opens the native text editor; `mode: chat` uses the existing private chat input instead. Input is applied on submit, not after each keystroke. Cancel preserves the query. A separate button can reset it with `{type: set-variable, name: query, value: ''}`. Queries use session variables so one player's search does not alter another player's query.

Matching is literal, ignores case and combining accents, and requires every whitespace-separated search word. It does not execute regular expressions or interpret player input as MiniMessage. An entry's `search-text` is optional and supports the normal administrator-configured text placeholders; otherwise its own `text` is used. Set `search-text` on group entries to describe their children. An empty query shows every authorized entry. A changed query resets the offset, and zero matches leave the viewport empty. Other variable changes that reduce results clamp the offset.

## Component animations

Set `animation` on a leaf component to move it from a relative offset and/or initial box scale to its normal position and size:

```yaml
animation:
  duration-ticks: 20
  interval-ticks: 2
  from: {x: -16, y: 0, scale: 0.9}
  easing: ease-out
  on-open: true
```

`duration-ticks` accepts 2..200, `interval-ticks` accepts 2..20, and duration must be a multiple of interval with at most 32 steps. Defaults are 20/2. Relative `x` accepts -512..512, `y` -320..320, and scale 0.25..2. Easing is `linear`, `ease-in`, `ease-out` (default) or `ease-in-out`. Scaling changes the graphic and component box around its center; body text retains the standard 8-unit font. This is movement/box scaling, not alpha fading or a client shader transform.

`on-open` defaults to true. Use false for manual playback through `{type: animate-component, target: component_id}`. Replaying a component replaces its existing motion. Notification animations start when `notify` shows/replaces the message if `on-open` is true. An entrance animation on an offscreen list entry still follows the screen-opening clock; scrolling it into view does not restart it.

Animated controls cannot activate while moving. Other controls remain usable, and the unconditional close control stays fixed and available. The chrome background and close control cannot be animated; animations cannot cover the close control or move outside the canvas/scroll row. Navigation, replacement, reload, disconnect and closing cancel all session motion. No infinite timer is created.

With `shaders.animations: true`, native PNG leaves and body text use a continuous client `GameTime` timeline. Background/overlay graphics and colored/bold text move together, with a stable text wrap and eight-unit font during scaling. Unrelated redraws retain the timeline instead of restarting it. Notifications need one redraw to appear and one to expire, with none for motion frames or the motion endpoint. Other components publish one final redraw to restore ordinary glyphs and enable their controls. Replaying or replacing a motion starts a new timeline. Client/server clock synchronization is approximate, as with image atlas playback.

Shadows, sliders, item slots, autoplay, asset selectors, external/client-sprite graphics and inline sprite text retain discrete motion. Unexpected text colors not present in the compiled palette and text that would exceed the smallest animated box also use that fallback, preserving clipping and viewport bounds. A replacement with supported content can use smooth motion again. The pack supports up to 4096 motion glyphs and 512 configured text colors; adding motion styles or colors requires rebuilding and loading the pack.

Without motion shaders, finite server-driven updates typically run up to 10 times per second. Frames can be skipped when the update budget is exhausted, but the final position is retained. Book resends can flicker. Vanilla Escape closure alone is not observable by the server, so any pending redraw (including notification expiry or a component's final state) can reopen the book; the configured X closes the server session and cancels further updates. There is no automatic periodic refresh after finite updates end.

## In-GUI notifications

A `notification` is a passive text/graphics slot, hidden until an action fills it:

```yaml
notice:
  type: notification
  position: {x: 24, y: 280}
  size: {width: 432, height: 28}
  padding: 6
  layer: 60
  asset: {provider: native, kind: image, id: 'craftcanvas:button'}
```

Show it with `{type: notify, target: notice, text: '<green>Saved!', duration-ticks: 60}`; duration defaults to 60 and accepts 1..1200. Normal colors, variable insertion, alignment, wrapping, graphics and the optional component animation apply. The action's text is resolved when the notification is created. `notify` may also be the final immediate reward action in a `purchase`.

Each target holds one message. A new message replaces its text and expiry; separate notification components can coexist. `{type: dismiss-notification, target: notice}` removes it early. Expiry stops that notification's motion as well. State belongs to the current player's session and is never stored in SQL. Modal text editing and confirmation suppress book redraws; expired messages stay expired when the menu resumes. Notifications cannot contain controls/children or cover the close control.
