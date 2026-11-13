---
title: "Developer API"
---

Use `zone.vao:craftcanvas-api:1.0.0-SNAPSHOT` as a compile-only dependency and declare `depend: [CraftCanvas]`. The API is included in the CraftCanvas plugin; keep it out of your addon JAR.

Start with the [Java and Kotlin quick start](/developers/quick-start/). Runnable source examples are in [examples/addon](https://github.com/Naimadx123/CraftCanvas/tree/master/examples/addon).

## Availability

| Method | Result |
| --- | --- |
| `CanvasApi.whenReady(owner)` | Future completed when startup publishes the service; call from addon `onEnable` or the global scheduler |
| `CanvasApi.isAvailable()` | Whether a service is registered now |
| `CanvasApi.serviceOrNull()` | Registered service, or null |
| `CanvasApi.service()` | Registered service; throws IllegalStateException while unavailable |

`depend` establishes plugin load order. CraftCanvas also prepares configuration and resources asynchronously, so use `whenReady` during addon startup. Waiting is cancelled when either plugin disables. The helper removes its temporary listeners on success, cancellation or failure. A missing or disabled CraftCanvas fails immediately.

## Player operations

For available screens, variable types/defaults and component targets, use the [screen catalog](/developers/screen-catalog/). `screenCatalog()` is a cached immutable read from any thread and does not require a player or an async helper.

Synchronous methods run on the player's owning entity region, or Paper's main thread. For calls from a worker or another region, use the asynchronous helpers. Each future reports the actual operation result rather than just task submission.

| Synchronous method | Async equivalent | Result |
| --- | --- | --- |
| `openDetailed(player, screen[, data])` | `openAsync(player, screen[, data])` | CanvasOpenResult / future of that result |
| `open(player, screen[, data])` | Use openAsync and inspect opened | Boolean, true only for OPENED |
| `close(player)` | `closeAsync(player)` | Void / completion future |
| `uiState(player, sessionId)` | `uiStateAsync(...)` | Immutable effective component state, or null for a stale session |
| `updateComponent(player, sessionId, target, update)` | `updateComponentAsync(...)` | Replace/clear a complete session component override |
| `patchComponent(player, sessionId, target, patch)` | `patchComponentAsync(...)` | Change supplied override properties and reset selected fields |
| `setComponentCooldown(player, sessionId, target, seconds)` | `setComponentCooldownAsync(...)` | Set/clear a declared shared player cooldown |
| `stopComponentAnimation(player, sessionId, target)` | `stopComponentAnimationAsync(...)` | Settle component motion at its final geometry |
| `setSelection/setTab/setPage(player, sessionId, value)` | Corresponding `Async` method | Update server selection, tab or pagination |
| `session(player)` | `sessionAsync(player)` | SessionView or null |
| `setVariables(player, sessionId, values)` | `setVariablesAsync(player, sessionId, values)` | Boolean acceptance; stale session returns false |
| `setVariable(player, name, value)` | `withPlayer(player, api -> ...)` | Typed mutation; missing session/variable fails |
| `setItemTooltip(player, sessionId, slot, item)` | `withPlayer(player, api -> ...)` | Boolean acceptance |
| `showNotification(player, sessionId, target, text[, durationTicks])` | `showNotificationAsync(...)` | Boolean acceptance |
| `dismissNotification(player, sessionId, target)` | `dismissNotificationAsync(...)` | Boolean acceptance |
| `playComponentAnimation(player, sessionId, target)` | `playComponentAnimationAsync(...)` | Boolean acceptance |
| `setScrollOffset(player, sessionId, target, offset)` | `setScrollOffsetAsync(...)` | Boolean acceptance |
| `setListItems(player, sessionId, target, items)` | `setListItemsAsync(...)` | Atomic session-local list snapshot |

`withPlayer(player, operation)` executes a short function with the service on the correct player thread and returns its result in a CompletableFuture. It is useful for reading a snapshot and changing several presentation values in one scheduled operation. It runs inline when the caller already owns the player. Async opening and variable updates copy the input map before scheduling.

Pending player operations complete exceptionally if CraftCanvas disables or the player becomes unavailable before execution. Operation exceptions are carried by the future. Cancelling a queued player future prevents its function from starting; it cannot interrupt an operation that has already started. Continuations inherit the completing thread and must not assume a player or global context. Never call `join()` or `get()` from a tick thread.

See [opening results, batching and tooltips](/developers/sessions/).

See [GUI controls for addons](/developers/gui/) for literal Adventure notifications, configured component animations, filtered scrolling and session/thread guarantees.

See [dynamic lists](/developers/dynamic-lists/) for addon records, reusable YAML row slots, searchable snapshots and record-aware callbacks.

## Registration and lifecycle

| Method | Thread | Result |
| --- | --- | --- |
| `registerScreen(owner, definition)` | Global / Paper main | Stage an in-memory screen and native resources; full reload publishes it |
| `screenDefinition(id)` | Any | Immutable published schema or null |
| `registerAction(owner, id, action)` | Global / Paper main | CanvasRegistration |
| `registerAsyncAction(owner, id, action)` | Global / Paper main | CanvasRegistration; pending stages cancel on removal |
| `registerAction/registerAsyncAction/registerCondition(owner, id, schema, handler)` | Global / Paper main | CanvasRegistration; validates typed callback data |
| `registerCondition(owner, id, condition)` | Global / Paper main | CanvasRegistration |
| `registerProvider(owner, provider)` | Global / Paper main | CanvasRegistration |
| `installBundle(owner, bundle[, mode])` | Request on global / Paper main | Future of CanvasBundleResult |
| `reload([screen])` | Any | Future of CanvasReloadResult |

Registrations are automatically removed when their owning plugin disables. Handles support early removal. [Callbacks and providers](/developers/callbacks/) includes examples and ownership rules. [Bundles](/developers/bundles/) installs addon-owned YAML and artwork; [reloading](/developers/reload/) compiles them and returns diagnostics.

See [typed definition builders](/developers/definition-builders/) and [code-defined screens](/developers/code-screens/) for creating screens without YAML and [component state](/developers/component-state/) for effective state reads and controlled edits.

## Session snapshots

`SessionView` exposes `id`, `screen`, `revision`, `selected`, `tab`, `page` and a read-only `variables` map. Keys in that map are scope-qualified, such as `session.greeting` or `player.volume`. Keep the session ID with asynchronous repository requests and pass it to batch/tooltip updates. A replaced session rejects the old ID. Multiple requests within one session still need addon-managed ordering.

## Events and platform support

Row action callbacks/events also expose an optional `SessionView.listSelection` with the captured container and immutable record. Ordinary session reads return null for this field; see [dynamic lists](/developers/dynamic-lists/).

[Player events](/developers/events/) expose screen opening/closing, action execution and selection changes. Callbacks and events run on the corresponding player owner and can execute concurrently for different players on Folia. Shared addon state must be thread-safe. Plugin registration and provider invalidation run globally.

Use Paper/Folia 1.21.11 through 26.3 and the Java version required by that server. Compile addons targeting the complete range with Java 21 bytecode. Newer server-only APIs require your own version checks. The graphical renderer uses signed books; the separate native text editor is described in [input controls](/reference/yaml/#native-text-editor).

[Async addon actions](/developers/async-actions/) await a `CompletionStage<CanvasActionResult>` before continuing YAML, with a server-tick deadline and session/registration cancellation.

[Typed callback parameters](/developers/typed-callbacks/) provide immutable schema keys, defaults, bounds, choices and typed reads without casts, with configuration and invocation validation.
