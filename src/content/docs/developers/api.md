---
title: API overview
description: Choose the right API for screens, callbacks, player state and addon resources.
---

The public API lets your plugin open menus, create screens and connect buttons to your own logic. It is the `zone.vao:craftcanvas-api` dependency; the CraftCanvas plugin supplies its implementation on the server.

Start with [your first addon](/developers/quick-start/) for a complete Java or Kotlin plugin. It creates a menu with a working callback button.

## Choose what your addon needs

| I want to... | Use | Guide |
| --- | --- | --- |
| Open an existing menu | `openAsync` or `openDetailed` | [Opening and updating menus](/developers/sessions/) |
| Build a menu in code | `registerScreen` and typed builders | [Code-defined screens](/developers/code-screens/) |
| Change a label or disable a button for one player | `patchComponent` | [Component state and edits](/developers/component-state/) |
| Run my logic when a button is clicked | `registerAction` | [Callbacks](/developers/callbacks/) |
| Validate callback options such as an item count | `CanvasActionSchema` | [Typed parameters](/developers/typed-callbacks/) |
| Wait for a database or HTTP request in a click sequence | `registerAsyncAction` | [Async actions](/developers/async-actions/) |
| Fill a list from my own data | `setListItemsAsync` | [Dynamic lists](/developers/dynamic-lists/) |
| Ship editable YAML and artwork in my addon | `installBundle` | [Addon bundles](/developers/bundles/) |
| Inspect available screens and fields | `screenCatalog` | [Screen catalog](/developers/screen-catalog/) |
| React to opening, selection or closure | Bukkit listeners | [Events](/developers/events/) |

## YAML screens or code-defined screens

Both use the same components, conditions and resource pack. Choose YAML when administrators should edit the layout. Choose code definitions when your addon should construct and register it. You can also inherit an existing YAML screen, such as the shared `frame`, from code.

Declaring an image makes it available to the generated pack. Opening a menu or changing a declared variable does not rebuild that pack. If you add or change definitions or resources, register all changes and request one full `reload()`.

## Startup in order

1. Declare `depend: [CraftCanvas]` and wait for `CanvasApi.whenReady(this)` in `onEnable`.
2. Register your callbacks, conditions, providers and any code-defined screens using your plugin as `owner`.
3. Reload once after staging screen/resource changes. Check the completion result.
4. Open menus and update player state through the service.
5. Release retained registration handles when they are no longer needed. Owner disable also removes them automatically.

A successful reload means the server has applied the definitions. It does not mean the player's pack download is finished. `openAsync` and `openDetailed` report that separately.

## Three reads that answer different questions

| Read | Answers | Thread |
| --- | --- | --- |
| `screenCatalog()` | What screens, variables and component IDs are declared? | Any |
| `session(player)` | Which screen is this player using, and what are its values? | Player thread |
| `uiState(player, sessionId)` | What is visible, enabled or overridden in that session? | Player thread |

Use `sessionAsync` and `uiStateAsync` when calling from another thread. `rendererCapabilities("book-glyph")` tells you whether server features such as `autoplay` are enabled before choosing your screen definitions.

## Keep the session ID

Each opening has its own ID. Capture it before starting background work and pass it to the update method. If the player opens something else before the result arrives, the old ID is rejected instead of changing the new menu.

An ID protects against replacing a different screen. It does not order two background requests for the same screen; track that order in your addon if requests can overlap.

## Where snippets run

In these guides, `canvas` is your `CanvasService`, `owner` or `this` is your enabled addon plugin, and `player` is the player being shown the menu. IDs such as `claim`, `results` and `notice` must exist in the screen used by that example.

Registration runs on Paper's main thread or Folia's global thread. Player reads and edits run on that player's thread. The [threading guide](/developers/threading/) explains which helper to choose; a future completing does not guarantee a particular thread.

Use only the public API package. Keep its classes out of your addon JAR. For signatures and return types, use the [service method reference](/developers/api-reference/).
