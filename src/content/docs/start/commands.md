---
title: "Commands and permissions"
---

Use `/cc` or `/cc help` to see the commands you can use. `/craftcanvas` and `/gs` also work. Angle brackets indicate required arguments and square brackets optional ones.

| Command | Purpose | Permission |
| --- | --- | --- |
| `help` | Show commands allowed by your permissions | Any sender |
| `open <screen> [player]` | Open a permitted screen or queue while its pack loads | craftcanvas.open; craftcanvas.others for another player |
| `close [player]` | Close and invalidate the session | craftcanvas.open; craftcanvas.others for another player |
| `cancel` | Cancel pending private text input | Current player with pending input |
| `profile <profile>` | Choose a manual layout profile | craftcanvas.profile |
| `list` | List screens accessible to the sender | Screen-specific permissions |
| `info` | Version, server, pack summary and integrations | Any sender |
| `reload [screen]` | Recompile configuration and rebuild resources; optionally target one screen's session lifecycle | craftcanvas.admin |
| `validate [screen]` | Compile and validate without publishing | craftcanvas.admin |
| `inspect <screen>` | Layout, renderer capabilities and limitations | craftcanvas.admin |
| `assets <provider>` | Readiness and known asset IDs | craftcanvas.admin |
| `pack build` | Reload and build resources | craftcanvas.admin |
| `pack export <standalone\|nexo\|craftengine\|itemsadder>` | Stage assets without modifying provider files | craftcanvas.admin |
| `debug <player>` | Session, token, task, pack and held-item model/icon diagnostics | craftcanvas.admin |
| `debug capabilities` | Renderer features, limits and core-shader integration status | craftcanvas.admin |

Open/profile permissions default to everyone; admin/others default to operators. Screen and action permissions are administrator-defined. Argument suggestions respect command permissions, screen access and permission to target other players. Console opening/closing requires a player name. Failed reloads retain the last valid configuration and pack.

Reload still reads and validates YAML. Unchanged PNGs share decoded images, and generated PNGs are cached by their pixels. When the compiled configuration, source-file contents, character registry and generated outputs are unchanged, CraftCanvas reuses the existing pack instead of rebuilding it. Changing a PNG is detected even if its size and modification time stay the same. The image caches use at most 64 MiB and reset when the plugin restarts; the first build still prepares all resources. Nexo merging and hosting have their own build times.

`/cc list` shows accessible screen IDs. Click an entry to fill in its open command. For example, `/cc open home` opens the shipped home screen; `/cc profile compact` chooses the compact layout. Extra arguments and unknown targets produce a usage or error message.

Pack loading messages stay short. Administrators can use `/cc debug <player>` for pack UUIDs, load statuses, session state and held-item model details. Unexpected command errors are recorded with their stack trace in the server log; players receive a short failure message. Configuration validation keeps the file, component and field in its error details.

Optional providers are discovered at startup. Restart after installing/removing dependencies; see [integration setup](/resources/integrations/). CraftCanvas must be the sole pack sender, or be configured to observe the UUID sent by your chosen external sender.
