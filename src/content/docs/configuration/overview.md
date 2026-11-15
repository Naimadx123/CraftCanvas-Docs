---
title: Configuration overview
description: Which file to edit, how screens fit together, and when to reload the pack.
---

Everything lives in `plugins/CraftCanvas/`. Start with the file for the thing you want to change:

| File or folder | What it controls |
| --- | --- |
| `config.yml` | Pack delivery, shader effects, menu scales and limits |
| `screens/*.yml` | The menus players open: text, images, buttons and actions |
| `assets/*.yml` | The names and source files for your images and sounds |
| `assets/` | Your PNG, OGG and MP4 files |
| `themes/*.yml` | Shared colors and screen defaults |
| `templates/*.yml` | Components you want to reuse, such as navigation buttons |
| `actions/*.yml` | Click sequences shared by several buttons |
| `languages/*.yml` | Translated labels and messages |
| `storage.yml` | Where player preferences and cooldowns are saved |

`craftcanvas.zip` is the generated resource pack. Edit the source files above and let CraftCanvas rebuild it. Keep `.data/` in backups; it holds saved player data, resource records and conversion caches.

## If you are just starting

First [install the plugin and set up pack delivery](/start/installation/). Then follow [your first screen](/getting-started/first-screen/): it uses the console and button artwork already included with CraftCanvas, so you can try it without drawing or importing anything.

A screen has three main parts:

- **Components** are what players see: a label, an image, a button or an input.
- **Actions** are what happens when someone clicks, such as opening another screen or running a command.
- **Variables** hold choices and values, such as a selected class or a saved setting. Conditions use those values to decide what to show or allow.

You can add your own artwork once that first menu works.

## What comes with the plugin

A fresh install includes a handheld console layout and example apps. Run `/cc open home` after accepting the pack to try them. The launcher has three pages, including the wheel, settings and Cinema.

The shared `frame` screen supplies the console, heading and physical controls. A screen with `extends: frame` keeps those parts and adds its own content. You can change the shared artwork in `assets/` without copying it into every screen.

Hover highlights and shader animations are enabled in the supplied `config.yml`. [Main configuration](/configuration/config/) explains those switches and the pack settings.

## Apply your changes

After editing YAML, run:

```text
/cc validate
/cc reload
```

`validate` checks the files. `reload` checks them again, rebuilds the pack and applies the changes. If a reload fails, the running configuration stays in place; fix the error shown in the command reply and try again.

When Nexo sends the pack, wait for its rebuild and upload to finish. Players need the updated pack before they can see changed artwork or layouts. If you host the ZIP yourself, replace the hosted file too. See [resource packs](/resources/resource-pack/).

Changing `storage.yml` needs a server restart.

## Updating and keeping your edits

Updates add missing settings to bundled configuration files while keeping your existing values. Existing PNGs are also kept.

Screen examples are installed only into a fresh, empty setup. A screen you deleted stays deleted, and an update does not replace your screens or add new examples to them. Copy a new example yourself if you want to try it.

## Choose what to add next

| I want to... | Read |
| --- | --- |
| Change a label, position a button or add a tooltip | [Components and layout](/guides/components/) |
| Run commands or open another menu | [Actions](/configuration/actions/) |
| Remember a choice or show a player's name | [Variables and formatting](/configuration/variables/) |
| Restrict a button by permission or a value | [Conditions](/configuration/conditions/) |
| Reuse the same layout or button | [Themes and templates](/configuration/reuse/) |
| Import an image, sound or video | [Resource packs](/resources/resource-pack/) and [animations](/guides/animations/) |
| Look up an exact field or limit | [YAML reference](/reference/yaml/) |
