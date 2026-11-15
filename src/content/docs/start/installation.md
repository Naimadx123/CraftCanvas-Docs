---
title: Installation
description: Install the JAR, send the resource pack and open the example launcher.
---

Use the CraftCanvas JAR supplied for your release or beta. Players need the generated resource pack and a supported vanilla client; they do not need a client mod.

## Requirements

| Server family | Java |
| --- | --- |
| Paper or Folia 1.21.11 | 21 or newer |
| Paper or Folia 26.1.2, 26.2 or 26.3 | 25 or newer |

Use a matching supported client. The supported releases are 1.21.11, 26.1, 26.1.1, 26.1.2, 26.2 and 26.3. Snapshots, prereleases and versions outside that list are rejected. Paper did not publish a 26.1 server release; use Paper 26.1.2 for that family. ViaVersion alone does not guarantee compatible rendering.

## 1. Install the plugin

1. Put the JAR in the server's `plugins/` folder.
2. Restart the server and wait for CraftCanvas to finish preparing its files and pack.
3. Check that `plugins/CraftCanvas/config.yml` and `craftcanvas.zip` exist.

The first start also creates example screens, artwork and other [configuration files](/configuration/overview/). SQLite stores player preferences by default, so a separate database is optional.

## 2. Send the pack

### With Nexo

Keep `pack.nexo-auto-export: true` in CraftCanvas. Configure pack generation, hosting and sending in Nexo as usual.

CraftCanvas exports its graphics into `plugins/Nexo/pack/external_packs/craftcanvas.zip`. Nexo builds and sends the combined pack. Wait for its rebuild and upload to finish, then accept the updated pack. You do not need to copy a CraftCanvas URL or UUID into the automatic Nexo setup.

### Without Nexo

Host `plugins/CraftCanvas/craftcanvas.zip` at a direct download URL, then edit the existing `pack` section:

```yaml title="config.yml"
pack:
  sender: craftcanvas
  url: 'https://example.com/craftcanvas.zip'
  sha1: ''
```

Replace the URL with your own. Keep the other pack fields and run `/cc reload`. Players must accept this pack before menus can open.

If another plugin sends a combined pack, follow [external workflows](/resources/resource-pack/#external-workflows). CraftCanvas creates the ZIP but does not host it. All three delivery options are explained under [resource packs](/resources/resource-pack/).

## 3. Open the launcher

As an operator, run:

```text
/cc info
/cc open home
```

You should see the handheld console with three pages of apps. Try settings, the wheel and Cinema. The wheel grants a real item and has a shared 60-second cooldown on SPIN and A.

The startup animation and sound play once per connection; reconnect to replay them. Hover and animation shaders are enabled in the shipped configuration. `pack.hide-vanilla-book: true` hides the underlying vanilla book background while the pack is loaded, including for ordinary books.

If a menu stays queued, use `/cc debug <player>` to check pack status. A locally selected pack does not confirm that the expected server pack was downloaded. See [troubleshooting](/resources/troubleshooting/).

## Keeping your configuration

Startup and `/cc reload` add missing keys to bundled YAML files outside `screens/`, keeping existing values, lists, comments and custom keys. Missing non-screen files are installed; existing PNGs are kept.

Example screens are installed only into a fresh empty setup. Removed screens stay removed, and an update does not replace your screens or install new examples into an existing setup. Copy an example yourself when you want to add it.

Keep `.data/` in backups along with your configuration and artwork. It holds saved player data and internal resource records. See [updating an existing setup](/resources/migration/) and [player storage](/configuration/storage/).

## Folia

Use Folia-compatible versions of your optional plugins and addons too. CraftCanvas schedules player work on the player's region and shared work on the global scheduler. Addon authors can use the helpers in the [developer quick start](/developers/quick-start/).

Next, [make your first screen](/getting-started/first-screen/) or read the [configuration overview](/configuration/overview/).
