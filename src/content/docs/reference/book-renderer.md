---
title: "Book renderer and limitations"
---

Every configured CraftCanvas menu uses `book-glyph` on Paper 1.21.11 through 26.3 with a matching vanilla client. `auto` selects this same renderer and rejects unsupported requirements. There is no inventory backend. An optional [experimental core-shader extension](/guides/shader-hover/) adds hover overlays on supported 1.21.11 through 26.3 clients; the default book renderer requires no shader changes. Text input can temporarily open a native Paper dialog as an auxiliary editor; it is not a menu backend.

Noninteractive [cinematic screens](/guides/animations/#fullscreen-cinematics) use a separate title HUD overlay with core shaders on 1.21.11. They do not open the book renderer described below.

## Layout and native controls

The renderer creates a detached `WRITTEN_BOOK` with Adventure pages through `BookMeta` and opens it through public `Player.openBook(ItemStack)` without changing the player's inventory. PNGs become tiled bitmap-font glyphs. Per-position ascent sets vertical placement; positive/negative space glyphs and bidirectional text isolate each element's advance while keeping the logical book line within its 114-unit wrapping budget. Text defaults to Minecraft bitmap glyphs with their native advances; the screen font and MiniMessage font tags select prepared font variants. The panel may extend beyond the old text-page bounds.

The origin is the native book text origin. `layout.anchor: horizontal-center` centers horizontally using that origin; `book-origin` keeps it fixed. `layout.offset-y` is an unscaled fixed offset. The server does not know client resolution or GUI Scale, so vertical centering and automatic profile selection are unavailable. Choose `compact`, `normal` or `large` manually. Confirm each profile on the clients you support.

Every screen requires an unconditional root `background` with a native PNG and a root close control whose only unconditional click action is `close`. The compiler checks all allowed profiles and requires fully opaque pixels over book bounds `(-36,-30,192,192)` and Done bounds `(-40,164,200,20)` relative to the book origin. The background draws over native texture, page number, arrows and Done. Its native no-op `change_page` click consumes mouse activation before the client's widget handler; foreground elements supply their own actions. This covers controls and intercepts their mouse actions; it does not remove the native widgets.

Keyboard focus still belongs to Minecraft. Tab/Enter can activate native Done and Escape closes locally. A resource pack cannot remove widget hitboxes or keyboard behavior. No global vanilla button/book texture is overridden. The world stays visible outside the panel with native dimming. The book screen avoids the blurred-background render path; removing dimming is unsupported.

## Clicks and updates

Native tooltips and cursors operate locally. Action buttons send `ClickEvent.custom` identifiers to Paper `PlayerCustomClickEvent`. Opaque tickets bind owner, session, screen, component and render revision. Tokens are single-use; permissions and visibility/enabled/execution conditions are rechecked. Client payload cannot supply commands, prices or rewards.

A bitmap hitbox is rectangular according to font advance and height. Transparent pixels inside it are still interactive. The measured advance can include a one-unit right edge; `reject-interactive` overlap validation includes that edge. Text-only buttons use text bounds, not the whole declared rectangle. Use a PNG-backed button for a rectangular hit area. Higher layers are visited after lower ones; overlapping active glyphs favor later text entries. Avoid ambiguous overlaps unless they are intentionally mutually exclusive.

`book.update: resend` is the default. Action completion marks the session dirty; the server recompiles changed content and calls `openBook` again. A custom click alone does not update a book. Static layouts are cached. Resends renew single-use tickets even when the visible content is unchanged. Reopening can reset client focus or flicker; do not assume invisible updates.

`local-pages` compiles 1..12 declared variants into native pages. A sole `local-page` action switches locally without a server packet. The server therefore learns a choice only when a custom confirmation/action ticket from that whitelisted page arrives. It validates and applies that variant before action dispatch. A local-page cannot also send a server action or play a server click sound. See `screens/variants.yml`.

Escape has no reliable server book-close event. X invalidates immediately; timeout, disconnect, navigation, reload and resource reload invalidate sessions. Resumed gameplay signals retire sessions after a short opening grace period. Periodic screen/component refresh is rejected because it could reopen a book closed locally. A stale session can remain until a signal or timeout; this is a protocol limitation, not a guaranteed immediate close event.

The `animate` action plays precompiled image frames. With `shaders.animations: true`, the client selects frames from a tiled atlas; the start and final state still resend the book. Otherwise every frame resends it and can flicker. Both paths obey session/task limits. Escape alone does not reliably cancel playback; use X or `/cc close`. See [animation fields and examples](/guides/animations/).

## Controls and effects

Toggle, choice and stepper are server-action buttons. By default, a text-input closes the book and opens a native dialog with a prefilled single-line text field, Save and Cancel. Save rechecks the original session, render revision, component visibility, availability, execution condition and screen permission before writing the string and resuming its action sequence. Cancel resumes the book without writing or running subsequent actions. `/cc cancel` can also return to the book. The editor defines Cancel as its exit action and enables Escape. Returning to the book uses the same resend mechanism as other updates.

This editor uses the shared public Paper Dialog API available in the supported release families. It has native dialog styling, controls and client-controlled background/blur effects; CraftCanvas does not disable the client's dialog blur. The graphical book menu is unchanged. It is not a freely positioned text box inside a bitmap glyph. The editor uses one-line literal input, a limit of 1..256 UTF-16 code units and a fixed 300-unit field width. Input is not parsed as MiniMessage. Requests have private submit/cancel tokens and expire with their session; closing, navigation, reload, disable and disconnect invalidate them. Returning to the book uses its existing resend/update behavior and can flicker.

Set `data.mode: chat` on a text-input, or `mode: chat` on an input action, to keep private chat replies. The plugin cancels matching chat broadcast and inserts the original text literally. Dialog mode does not intercept chat. Other chat plugins may still observe cancelled chat events in chat mode; avoid using these controls for secrets.

A slider is a row of separately clickable PNG glyph cells, with a label, fill and thumb. Clicking a cell sets its precompiled numeric value; the server receives a session-bound token rather than mouse coordinates or a client-supplied number. Values can increase or decrease. Native hover shows each cell's target value. The book is updated through its configured update mode after a click. Dragging is unavailable: holding the mouse does not provide continuous slider input. See the [slider fields and layout requirements](/reference/yaml/#click-slider).

Supported appearance states are normal, selected, disabled, locked and loading. Tooltip text or item preview uses native hover behavior. Selected appearance persists in session state and is rendered after a click. Disabled elements have no server-action ticket.

Hover-image, hover-sound, on-hover, pressed states, drag, scroll, right/shift/double-click and raw keyboard input are unsupported and rejected. Vanilla text does not send hover-enter/leave or cursor coordinates to the server. Frame playback uses repeated book opens unless `shaders.animations` enables atlas playback. The optional `hover-overlay` field requires the explicitly enabled experimental shader extension and a text tooltip; it does not expose server hover events. External glyphs must be one character with explicit fixed font metrics and matching geometry; native PNGs support generated positioning and scaling.

Book body text accepts colors, bold and MiniMessage font tags with measured metrics. Screen `font` defaults to `minecraft:default`. See [screen fonts](/guides/fonts/) for bitmap resources, glyph coverage, supported providers and line-spacing limits. Pages are limited to 32767 serialized component characters; reduce elements, repeated tooltips or variants if that budget is exceeded.

## Troubleshooting

- Missing graphics: check the expected UUID/hash and `SUCCESSFULLY_LOADED` with `/cc debug <player>`, then rebuild/host the current pack.
- Pack compilation failure: read the file/screen/component/field in the error; check PNG dimensions, opacity, ascent and character registry.
- Visible book controls: check every allowed profile's background coverage, layers and pack version.
- Unexpected clickable area: use PNG-backed controls, account for transparency and advance, and reject accidental overlaps.
- Panel too large: select a smaller manual profile; GUI Scale cannot be read by the server.
- Lost local choice: local pages are client-only until custom confirmation; use resend for immediate server state.
- Book reopens: remove external code that calls refresh after Escape; automatic YAML polling is prohibited.

After changing artwork or layout, check the final merged pack in a matching vanilla client. Test your allowed profiles and GUI Scale settings, including clicks near transparent edges, native-control coverage and redraw behavior.

Scrollable row viewports, dialog/chat search, finite component movement/box scaling and in-GUI notifications are available without core shaders. See [interactive UI](/guides/interactive-ui/) for the complete example, fields and the finite resend/Escape limitations.
