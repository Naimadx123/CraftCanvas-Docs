---
title: "Troubleshooting"
---

## A screen will not open

If the console reports `Screen '<id>' skipped`, that screen failed startup validation. Other valid screens remain available. Use `/cc inspect <id>` for its recorded error, fix the named file and run `/cc reload`. An `inherited-from` field identifies the parent or ancestor supplying the invalid setting; the `screen` field identifies the affected child. A child may remain available if it overrides the invalid parent setting. A failed reload preserves the running configuration instead of applying a partial update.

`open` reports unknown screen IDs, the required screen permission, event cancellation, invalid variable data and rendering failures separately. If the pack is missing, it reports whether the screen is queued, the expected pack UUID, its current status and instructions for the selected sender.

The supplied `pack.sender: external` setting observes another sender; it does not host or send a ZIP. For standalone use, host `plugins/CraftCanvas/craftcanvas.zip`, set `pack.sender: craftcanvas` and its direct `pack.url`, leave `pack.sha1` empty and run `/cc reload`. Accept the server pack, wait for it to load, then use `/cc open home`. Automatic Nexo handling provides the export and merged UUID itself. For a manual merged provider pack, retain `external`, include the exported CraftCanvas assets in that pack and match `pack.id` to the provider's sent UUID. Use `/cc debug <player>` to check identities and download statuses. With effective sender `craftcanvas`, an empty URL now returns `PACK_UNAVAILABLE` immediately; configure `pack.url` before retrying.

## Disabled plugin or zip file closed

If tab completion reports `zip file closed`, the plugin's classloader has already been closed. Paper closes it when disabling the plugin. Inspect earlier startup messages, particularly `Configuration rejected` and `Disabling CraftCanvas`; that earlier error explains why the plugin stopped. Old screen backends or missing-pack policies may require [configuration migration](/resources/migration/).

Fix the reported configuration error and fully restart the server. Stop the server before replacing its plugin JAR. Use `/cc reload` for configuration changes while CraftCanvas is running; server `/reload` and plugin hot-reload managers are unsupported. A stopped plugin cannot be recovered by its configuration reload command.

## Graphics and interaction

- Missing graphics: check the expected UUID/hash and `SUCCESSFULLY_LOADED` with `/cc debug <player>`, then rebuild/host the current pack.
- Pack compilation failure: read the file/screen/component/field in the error; check PNG dimensions, glyph ascent and character registry. Root backgrounds may be transparent; slider-cell PNGs still require opaque pixels.
- Visible book texture: enable `pack.hide-vanilla-book`, rebuild and load the pack. Native control text, hitboxes and keyboard behavior remain.
- Unexpected clickable area: use PNG-backed controls, account for transparency and advance, and reject accidental overlaps.
- Panel too large: select a smaller manual profile; GUI Scale cannot be read by the server.
- Lost local choice: local pages are client-only until custom confirmation; use resend for immediate server state.
- Book reopens: remove external code that calls refresh after Escape; automatic YAML polling is prohibited.

Client appearance, outside-page hitboxes, cursor behavior, keyboard interception and update flicker must be checked in matching vanilla clients for each supported release family after changing resources or layouts. Compilation and unit tests cannot establish those visual results.

## A tiny coloured square replaces an animation

The atlas is being drawn as a normal bitmap glyph instead of being expanded by its text shader. Update CraftCanvas and rebuild the delivered pack, including its client-version overlays. Current builds recognise animation tiles independently of the vertex-buffer offset used by the 26.3 GUI renderer. For Nexo, rebuild/upload the combined pack and accept its new version; the standalone CraftCanvas ZIP alone is not the delivered pack.

Use `/cc debug <player>` to check the actual pack identity and download status. Inspect the ZIP from the URL the client receives, along with client shader errors. Finding `animation.json` and animation PNGs does not establish that the effective text shader can decode them. Shader pixel tests should also cover nonzero vertex-buffer offsets.

## PNG dimensions differ from the manifest

The manifest describes source pixels; a component's `size` describes display units. If `assets/icon.png` is 64 x 64, the manifest must say `width: 64` and `height: 64`, even when the button displays it at 32 x 32. Correct the manifest or PNG, run `/cc validate` and `/cc reload`, and publish the rebuilt pack. Do not edit a generated font JSON to repair the source.

## Bitmap ascent greater than tile height

Glyph height and vertical position must be representable in every allowed profile. Moving a small image toward the top of the book can require more ascent than its scaled height permits. Read the profile and component in the error. Move the image down, use taller artwork with transparent padding above the visible pixels, or restrict `layout.profiles` to the profiles your layout supports.

`layout.profile: normal` only chooses the default; it does not exclude compact or large. Use `layout.profiles: [normal]` to restrict the allowed set, then validate the entire layout. Profile restrictions apply to all generated glyph metrics and close-control layout; backgrounds need not cover native controls.

## Private-use character range exhausted

Older builds retained obsolete glyph assignments until the private-use range filled. Current builds automatically remove unused entries and preserve assignments for active glyph IDs. Update the plugin JAR, restart and run `/cc reload`; keep `.data/generated-pack/characters.yml` in place.

If generation reports a `limits.glyphs` budget error, an administrator-defined budget is enabled. Set `limits.glyphs: 0` to disable it, or choose a different positive budget, then rebuild. Budgets are disabled by default. The font encoder uses all 137468 Unicode private-use codepoints; exhausting those codepoints is a separate encoding error. The previous pack and registry remain intact after this failure. Duplicate or invalid saved codepoints are a separate registry error; restore a known-good backup rather than merging character maps manually.

## A button reports a cooldown

`cooldown.seconds` counts real seconds for each player. Controls with the same `cooldown.key` share a deadline, including SPIN and A on the bundled wheel. Closing or reopening the screen does not reset it. With player persistence enabled, reconnects and restarts do not reset it either. Change the configured duration for future activations or wait for the existing deadline; this cooldown is separate from the short `cooldown-ticks` click debounce. See [button cooldowns](/guides/components/#button-cooldowns).

## A setting does not change Minecraft

The settings example writes CraftCanvas variables such as `player.volume`, `player.difficulty` and `player.notifications`. An addon can use them for its own sounds or gameplay behavior. They do not modify vanilla client audio options or world difficulty. Connect the control's click action to an addon callback when it should change another system.
