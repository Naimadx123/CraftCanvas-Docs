---
title: "Installation"
---

Use Paper or Folia 1.21.11 through 26.3 with a matching vanilla client. CraftCanvas accepts the exact releases 1.21.11, 26.1, 26.1.1, 26.1.2, 26.2 and 26.3; it rejects snapshots, prereleases and versions outside that list. Paper did not publish a 26.1 server release; use stable Paper 26.1.2 for that release family. Do not assume compatibility through ViaVersion.

| Minecraft version | Server Java | Resource-pack format |
| --- | --- | --- |
| 1.21.11 | 21+ | 75.0 |
| 26.1.x | 25+ | 84.0 |
| 26.2 | 25+ | 88.0 |
| 26.3 | 25+ | 97.1 |

The same JAR keeps Java 21 bytecode and compiles against the oldest supported Paper API. The generated pack declares format 75.0 through 97.1. `plugin.yml` retains `api-version: '1.21.11'` as the minimum API version. This is not an upper-version restriction. `/cc info` reports the detected server, Java version and resource-pack format. Check your layouts at the GUI scales used by your players.

Copy the built CraftCanvas JAR into `plugins/` and restart. Startup preparation runs asynchronously; wait for CraftCanvas to report ready. The data directory contains `config.yml`, `storage.yml`, `screens/`, `themes/`, `templates/`, `assets/`, `actions/`, `languages/` and internal files under `.data/`, including `generated-pack/`. Player variables use SQLite in `.data/data.db` by default; `storage.yml` also supports MySQL, MariaDB and PostgreSQL. See [storage configuration](/configuration/storage/).

Internal records use the hidden `.data/` directory (dot prefix plus the Windows hidden attribute where supported). An existing `data/` directory is renamed with its contents preserved. If both names exist, reconcile them before starting the plugin. This directory rename does not import legacy player JSON into SQL.

Startup and `/cc reload` synchronize bundled YAML files outside `screens/`: missing keys are added recursively while existing values, lists, custom keys and comments are preserved. Missing non-screen files are installed; existing binary assets are not replaced. The server log lists files receiving new fields. YAML formatting can be normalized when a file needs new keys; unchanged files are not rewritten. Invalid YAML is reported without replacing it with defaults.

Example screens are copied only on the first installation into an empty data directory. `.data/defaults-installed` records initialization. Existing installations, including older versions without this marker, do not receive new example screens automatically. Files under `screens/` are never merged or updated, and deleting a screen or the entire directory does not restore examples on reload/restart. An empty `screens/` directory may be recreated and is a valid configuration; the plugin stays enabled with zero screens. To restore an example, copy it manually from the JAR or project sources and adjust references from other screens/templates/actions when removing it.

With enabled Nexo and the default `pack.nexo-auto-export: true`, CraftCanvas places its ZIP in `plugins/Nexo/pack/external_packs/craftcanvas.zip` and requests a Nexo rebuild on startup and changed resource builds. Configure Nexo's pack generation/hosting and accept its combined pack. No separate CraftCanvas URL or manual pack UUID is needed.

Without automatic Nexo handling, build and host the pack following [PACK.md](/resources/resource-pack/). Set `pack.sender: craftcanvas`, a direct HTTP(S) `pack.url`, and leave `pack.sha1` empty to use the generated hash. Run `/cc reload`, accept the pack and wait for loading. The supplied missing-pack policy is `wait`; opening waits until the expected pack UUID loads. A manually enabled local pack does not provide a server download-status acknowledgment.

Start with `/cc open home` to browse examples. Use `selection` for generic option buttons and confirmation, `catalog` for pagination and permissions, and `settings` for persistent preferences. `classes` is an optional RPG showcase; `simple` and `variants` demonstrate its icon selection and local page switching. `providers` and `provider_nexo`, `provider_craftengine`, `provider_itemsadder` demonstrate optional item tooltips with vanilla fallbacks. `frame` is a reusable base screen.

## Folia

The same JAR declares `folia-supported: true` and selects an entity/global scheduler adapter. Session polling, rendering, inventory access, clicks, text input and delayed actions execute on the player's owning entity thread and follow entity migration. Startup, configuration publication, resource invalidation, Nexo pack coordination and console commands use the global scheduler. Filesystem preparation stays on the dedicated file executor.


Optional plugins and addons must also support Folia. Player API operations belong on the owning entity region; registration belongs on the global region. The [developer quick start](/developers/quick-start/) shows the scheduling helpers.

Continue with [1st steps: assets and your first screen](/getting-started/first-screen/), [commands](/start/commands/), or [troubleshooting](/resources/troubleshooting/).
