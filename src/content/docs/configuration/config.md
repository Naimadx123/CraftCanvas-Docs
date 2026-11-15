---
title: Main configuration
description: Pack delivery, hover effects, animation, menu scales and limits in config.yml.
---

`plugins/CraftCanvas/config.yml` controls settings shared by every screen. Menu text and buttons belong in `screens/`; you do not need to change this file for each menu.

The values below are the ones shipped with a fresh install. Your existing values are kept when you update.

## Resource-pack delivery

Players need the CraftCanvas resource pack to see menus. Choose the setup you use:

| Setup | What to configure |
| --- | --- |
| Nexo generates and sends your pack | Keep `pack.nexo-auto-export: true`; set up hosting and sending in Nexo |
| CraftCanvas sends a ZIP you host | Set `pack.sender: craftcanvas` and give it the direct download URL |
| Another plugin sends a combined pack | Keep `pack.sender: external` and set `pack.id` to that pack's UUID |

With Nexo enabled, the automatic workflow takes priority over `sender`, `id`, `url` and `sha1`. CraftCanvas exports its ZIP to Nexo, and Nexo sends the combined pack.

For standalone hosting:

```yaml title="config.yml"
pack:
  sender: craftcanvas
  url: 'https://example.com/craftcanvas.zip'
  sha1: ''
  missing: wait
```

Replace the example URL with a direct link to your hosted `plugins/CraftCanvas/craftcanvas.zip`. CraftCanvas creates that ZIP; you provide the hosting. Leaving `sha1` empty uses the generated hash. Keep the other `pack` settings already in your file.

`missing: wait` queues a menu opening until the expected pack loads. Set it to `deny` to reject the opening immediately instead. A missing standalone URL is a setup error and cannot be queued.

See [resource packs](/resources/resource-pack/) for each complete workflow and how to check a player's download status.

## Hover and animation

```yaml
shaders:
  hover: true
  animations: true
  default-hover-overlay: craftcanvas:selected_frame
```

`hover` adds an outline when the cursor is over an eligible button. Set `default-hover-overlay: ''` if you want to choose outlines on individual components instead.

`animations` enables smooth movement, image atlases, video and cinematics. With it off, supported frame and component animations use book updates; autoplay images, video and cinematics need it on. The bundled `cinema` screen needs animation shaders. Remove it or keep this option on; validation rejects screens that require a disabled feature.

Both effects need the generated pack and a supported matching vanilla client. After changing these settings, reload and have players load the rebuilt pack.

## Menu size

```yaml
profiles:
  compact: 0.75
  normal: 1.14
  large: 1.25
```

A profile scales the menu artwork and layout. Try `/cc profile compact` if the console is too large, or `/cc profile large` if it is too small. Custom values can be between `0.5` and `1.5`.

Minecraft's GUI Scale still affects the result. Players choose a profile themselves; CraftCanvas cannot read their resolution or GUI Scale.

## Vanilla book background

```yaml
pack:
  hide-vanilla-book: true
```

This makes the vanilla book background texture transparent. It also affects ordinary books while the pack is loaded. Set it to `false` to keep that texture. Native controls and world dimming still exist either way.

## Saved player settings

```yaml
persistent-player-data: true
```

This enables saving for player variables marked `persistent: true`, and for button cooldowns. The default database is SQLite; you do not need a separate database server. See [variables](/configuration/variables/#save-a-player-preference) and [player storage](/configuration/storage/).

## Importing MP4 files

```yaml
media:
  ffmpeg: ffmpeg
  ffprobe: ffprobe
```

These are the executable names or paths used when a screen imports an MP4. Install FFmpeg and FFprobe on the server, or enter their full paths here. They are only needed for MP4 conversion. See [MP4 playback](/guides/animations/#import-an-mp4-directly).

## Limits

| Setting | Shipped value | What it limits |
| --- | --- | --- |
| `limits.components` | `512` | Declared components in one screen, including children |
| `limits.tasks-per-session` | `64` | Pending action tasks for one open screen |
| `limits.updates-per-tick` | `25` | Screen renders shared across the server in one tick |
| `limits.glyphs` | `0` | Optional font-glyph budget; `0` disables this budget |
| `limits.video-atlas-tiles` | `0` | Optional video-atlas budget; `0` disables this budget |

Lowering the render budget can delay screen updates. The two optional resource budgets are yours to set; there is no configured budget when they are `0`.

Keep `strict: true` to catch misspelled or unknown YAML fields. Leave `schema-version: 1` in place. `item-icons` is for optional item-preview overrides; see [automatic item models](/guides/item-icons/).

Run `/cc validate` and `/cc reload` after changing this file. The [YAML reference](/reference/yaml/#main-configuration) lists the accepted types, parser defaults and ranges.
