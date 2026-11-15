---
title: Threads and async work
description: Choose between synchronous calls, async helpers and the global scheduler on Paper or Folia.
---

On Paper, player work runs on the main server thread. On Folia, each player belongs to an entity region. In these guides, **the player thread** means whichever thread currently owns that player.

Use the async helper when you do not know whether you are already on that thread. It schedules the operation and returns a future containing the real result.

## Which thread does a method need?

| Operation | Where to call it |
| --- | --- |
| `openDetailed`, `session`, `uiState`, synchronous player edits | The player's thread |
| `openAsync`, `sessionAsync`, `uiStateAsync`, async player edits | Any thread |
| `withPlayer(player, operation)` | Any thread; the operation runs on the player's thread |
| `whenReady(owner)` | `onEnable` or the global scheduler |
| Register/unregister callbacks, screens or providers; request a bundle install | Paper main / Folia global thread |
| `reload`, `screenCatalog`, `screenDefinition`, `rendererCapabilities` | Any thread |

Call `whenReady` from `onEnable`. Its successful continuation runs there or on the global thread when startup finishes, so you can register your addon in it. Later futures may complete elsewhere.

## Change several things together

```kotlin
canvas.withPlayer(player) { api ->
    val session = api.session(player)
    if (session == null) false
    else api.setVariables(player, session.id, mapOf("status" to "Ready"))
}.exceptionally { error ->
    logger.warning("Could not update menu: ${error.message}")
    false
}
```

This example expects a declared string variable named `status`. Keep the function short: `withPlayer` runs on a tick thread. It does not turn a blocking SQL or HTTP call into background work.

## Load data in the background

Start this from a player callback or command while on the player's thread. `repository.loadVariables` represents your own nonblocking data service:

```kotlin
val sessionId = canvas.session(player)?.id ?: return
val playerId = player.uniqueId
repository.loadVariables(playerId).thenCompose { values ->
    canvas.setVariablesAsync(player, sessionId, values)
}.thenAccept { applied ->
    if (!applied) logger.fine("The original menu is no longer active")
}.exceptionally { error ->
    logger.warning("Could not load menu data: ${error.message}")
    null
}
```

Capture player/session information before leaving the player thread. Return data from the worker, then use an async API edit to apply it. Do not access Bukkit world or player state inside the repository worker.

If a click sequence must wait for that request before its next action, use an [async action](/developers/async-actions/).

## Return to registration work

If a worker result needs to change a registration, schedule that part globally:

```kotlin
Bukkit.getGlobalRegionScheduler().execute(owner) {
    if (owner.isEnabled) {
        canvas.registerScreen(owner, definition)
        canvas.reload()
    }
}
```

`definition` is a completed `CanvasScreenDefinition`. Keep its registration handle if you need to remove it early. This scheduler works for the shared registration step on both Paper and Folia.

## Futures, cancellation and shutdown

Never wait with `join()` or `get()` on a tick thread. Attach a continuation instead. In that continuation, use `withPlayer` or the global scheduler before doing work that requires them; the future may have completed on a worker or immediately on its caller.

Queued player API operations fail if the player or CraftCanvas becomes unavailable. Cancelling a queued player future prevents its function from starting, but cannot interrupt a function already running. Cancelling a reload future does not cancel the shared reload operation.

Folia can run callbacks for different players concurrently. Protect shared addon maps and state, and shut down your own workers in `onDisable`. CraftCanvas registration cleanup does not cancel unrelated jobs you created.
