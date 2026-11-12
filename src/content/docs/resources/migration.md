---
title: "Upgrading CraftCanvas"
---

Back up `plugins/CraftCanvas/` before replacing the JAR. Stop the server, replace the plugin and restart. Use `/cc reload` for configuration changes after startup; Bukkit `/reload` and plugin hot-reload managers are unsupported.

## Configuration files

Bundled YAML files outside `screens/` gain missing default keys on startup and reload. Existing values, comments and lists are preserved. This adds defaults; it does not repair invalid values or convert a field to another type. Read the file/component/field diagnostic if reload is rejected.

Screen files are installed only into a fresh empty data directory. Existing screens are never merged, updated or recreated after deletion. New release examples must be copied manually from the JAR's `screens/` directory or project resources. Keep any supporting templates, shared actions and asset manifests with them. `.data/defaults-installed` records initial setup.

Keep variable names, scopes and types stable to retain player preferences. Keep `cooldown.key` stable when a cooldown should continue across updates; changing only the configured seconds affects future activations, not an existing deadline. Keep the player SQL database (`.data/data.db` for SQLite), `storage.yml`, `.data/addon-bundles/`, `.data/generated-pack/characters.yml` and pack-export ownership records in backups. An addon bundle `INSTALL` preserves its existing installation; an explicit administrator-requested `UPDATE` may update unchanged owned files or restore missing files. See [bundle ownership](/developers/bundles/).

## Resource packs

Run `/cc validate`, then `/cc pack build`. Automatic Nexo export requests a combined-pack rebuild. For standalone hosting, replace the hosted ZIP and update or clear a pinned `pack.sha1`. Players must load the new pack before opening screens that require it. Provider-managed merged packs also need their builder to support the target Minecraft version.

Do not delete the character registry during routine upgrades. Current builds preserve active glyph IDs and reclaim obsolete entries automatically, including registries filled by older build history. Do not combine registries from separate installations into the same font namespace.

## Changing Minecraft versions

The same JAR supports Paper/Folia 1.21.11 through 26.3 with matching vanilla clients. Java 21+ is required for 1.21.11; 26.x requires Java 25+. `api-version: '1.21.11'` declares the minimum API, not a maximum version. Check that all optional dependencies support the new server version.

The generated pack declares formats 75.0 through 97.1. If a provider merges it, that provider owns the final pack metadata. Use `/cc info` to inspect detected versions and `/cc debug <player>` to inspect loaded pack identities. Recheck custom layouts at your supported GUI scales after a client-version change.

## Moving to Folia

Keep the same screen YAML and assets. Install Folia-compatible resource, economy, placeholder and addon plugins. Player callbacks and events run on the player's owner and may execute concurrently for different players. Registrations and provider invalidation run globally.

Addons can use `openAsync`, `sessionAsync`, `setVariablesAsync`, `closeAsync` and `withPlayer` instead of writing their own scheduling bridge. Existing synchronous player methods still require the owning entity thread. Registration and bundle requests require the global thread. See the [developer quick start](/developers/quick-start/).

## Updating the handheld examples

The current examples use a transparent handheld chassis, two-page app launcher, flat pixel-art wheel, first-open startup animation/sound and hover outlines inside the screen. Physical console controls opt out of shader hover. The shipped config enables both shader switches, the default outline and `pack.hide-vanilla-book`.

Existing binary assets and screens are not overwritten automatically. Copy the updated screens, supporting templates, manifests, actions and PNG/OGG files from the current JAR or sources, preserving any local changes. Keep each manifest's dimensions in sync with its source images, including the physical console controls. Copy the new `screens/reward_wheel.yml` to enable its shared SPIN/A cooldown; replacing only the JAR does not add it to an existing screen. Keep `persistent-player-data: true` to retain cooldowns across reconnects and restarts.

The startup animation runs once per screen/player connection. Reconnect to replay it. `PACK_SETUP.txt` is installed automatically when missing. Rebuild and distribute the resulting pack after copying artwork or changing layout.

## Adding an example

To add the wheel to an existing installation, copy `screens/reward_wheel.yml` and retain its parent `frame`. Its resources are `actions/wheel_reward.yml`, `assets/wheel.yml`, the 48 wheel PNGs and pointer PNG. Missing bundled non-screen files are installed normally. Reload, load the rebuilt pack and run `/cc open reward_wheel`.

For a reusable animation without reward logic, copy [examples/animation.yml](https://github.com/Naimadx123/CraftCanvas/blob/master/examples/animation.yml). It uses existing UI artwork. Follow the [animation guide](/guides/animations/) to replace its frames with your own.
