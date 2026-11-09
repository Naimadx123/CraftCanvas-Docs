---
title: "Sessions and presentation"
---

Use `canvas.openAsync(player, screen, data)` from any thread, or `canvas.openDetailed(player, screen, data)` when already on the player's owner (Paper main thread). It uses the same opening pipeline as `open` and `/cc open`. Data keys must name declared variables and values must match their types/scopes. Existing `open(...): Boolean` remains compatible and returns true only for OPENED, including false for queued requests.

```kotlin
val result = canvas.openDetailed(player, "home", mapOf("greeting" to "Welcome"))
when (result.status) {
    CanvasOpenStatus.OPENED -> logger.info("Opened session ${result.sessionId}")
    CanvasOpenStatus.QUEUED -> player.sendMessage(Component.text("Waiting for the resource pack."))
    else -> logger.warning("${result.status}: ${result.message}")
}
```

| CanvasOpenStatus | Meaning |
| --- | --- |
| `OPENED` | The server renderer completed and the session is still current; sessionId is populated. |
| `QUEUED` | The required pack is unavailable and pack.missing is wait; one pending request is stored for this player. |
| `SCREEN_NOT_FOUND` | No compiled screen has this exact ID. |
| `PERMISSION_DENIED` | The player lacks the screen permission; requiredPermission contains its name. |
| `PACK_UNAVAILABLE` | The required pack is unavailable and the configured policy denies opening. |
| `PLAYER_OFFLINE` | The player is offline or disconnected during the opening callbacks. |
| `INVALID_DATA` | A supplied variable is unknown, has a mismatched scope or has an invalid value/type. |
| `CANCELLED` | ScreenOpenEvent was cancelled, another opening/configuration superseded this request, or the session was invalidated during rendering. |
| `RENDER_FAILED` | The renderer threw; CraftCanvas attempts to close and invalidate the failed session. |
| `OPEN_FAILED` | Another opening operation failed, for example closing the previous view; message contains the diagnostic and the server logs the cause. |
| `UNAVAILABLE` | CraftCanvas is loading, disabled or became disabled during opening callbacks. |

`CanvasOpenResult` contains `status`, the requested `screen`, optional human-readable `message`, optional `sessionId` and optional `requiredPermission`. Use status for logic; do not parse message text. sessionId is present only for OPENED and requiredPermission only for PERMISSION_DENIED. `opened` is true only for OPENED, `queued` only for QUEUED, and `packUnavailable` for QUEUED or PACK_UNAVAILABLE. OPENED confirms server-side rendering, not client pixels, hitboxes or an acknowledgement that the book is visible. The returned ID describes that completed opening; a later operation can replace or close it.

QUEUED is an immediate result, not a future guarantee of opening. A matching SUCCESSFULLY_LOADED pack acknowledgement retries the request and rechecks the current screen, permission and data. ACCEPTED or DOWNLOADED alone is insufficient. A later valid queued request replaces the earlier one for that player. Close, disconnect, successful reload and plugin disable discard pending requests. To observe the eventual opening, use ScreenOpenEvent; cancelled/rejected retries send the normal player diagnostic.

Invalid data is checked before queuing. Invalid, queued and event-cancelled openings do not write the supplied values into shared player/global variables or replace an existing session. Once an accepted opening starts replacing the prior screen, it is not a transaction: a later render failure can leave the prior screen closed and shared values applied. Do not automatically repeat reward or command effects on a render failure. Calling from a worker thread remains a programmer error and throws IllegalStateException instead of returning a gameplay status.

## Updating variables as one batch

`canvas.setVariables(player, sessionId, values)` updates declared scalar variables together on the player's owning entity thread (Paper main thread). Capture the session ID before starting an asynchronous read and schedule the complete result back to the owning entity thread. False means CraftCanvas, the player or the session is unavailable, or the ID is stale/foreign. These checks run before inspecting the data. True means the batch was accepted, including empty maps and identical stored values.

```kotlin
val opened = canvas.openDetailed(player, "settings")
if (opened.opened) {
    canvas.setVariables(player, requireNotNull(opened.sessionId), mapOf(
        "player.notifications" to false,
        "player.display_name" to "Explorer",
        "player.volume" to 75.0
    ))
}
```

Each entry is staged and checked before any writes: the variable must exist, an explicit scope must match its declaration, and the value must pass the same type/length/finite-number/integer-range validation as setVariable. Unknown variables throw IllegalStateException; invalid values, nulls, non-string keys and scope mismatches throw IllegalArgumentException. Scoped and unscoped names are accepted, but including both aliases of the same variable rejects the batch even when their values agree. A rejected batch leaves every scope, dirty flag and render revision unchanged. Other omitted variables retain their values.

After validation, writes to session/player/global stores complete within the same synchronous call, without intermediate callbacks or rendering. If at least one stored value differs, the target session is marked dirty once. Empty/identical batches do not request a refresh or clear an already pending refresh. Normal update limits, modal deferral and book resend/local-page behavior still apply; the method does not render immediately or promise flicker-free updates. Global values remain shared, but other sessions are not automatically marked dirty. On Folia, increments and batches are atomic for CraftCanvas global state; an action sequence is not a transaction across player regions or external plugins. Existing persistence saves player variables through its normal lifecycle; this is not an immediate disk transaction or a transaction with external plugins.

For an asynchronous repository workflow, capture Bukkit/player/session information on the owning entity thread and return only the data from the worker:

```kotlin
val sessionId = canvas.session(player)?.id ?: return
val playerId = player.uniqueId
repository.loadVariables(playerId).thenCompose { values ->
    canvas.setVariablesAsync(player, sessionId, values)
}.thenAccept { applied ->
    if (!applied) logger.fine("The original menu is no longer active")
}.exceptionally { error ->
    logger.warning("Could not update menu: ${error.message}")
    null
}
```

Here `repository.loadVariables` is your addon's asynchronous read returning a future of a typed variable map. Capture the session/player IDs on the player owner; the helper schedules only the application back to that owner. Do not mutate that map while applying it. CraftCanvas retains the values, not the caller's map. Calling setVariables directly from a worker throws IllegalStateException; it does not silently enqueue work. Navigation, close, timeout, disconnect, successful reload and disable invalidate the original session. The ID does not order multiple reads belonging to the same still-active session; an addon that permits overlapping reads must track their request order itself. Existing `setVariable(player, name, value)` keeps its original signature and behavior.

## Dynamic graphics and session item tooltips

Configure `asset-select.variable` and a finite map of `asset-select.options` in YAML. Change the declared string variable through `setVariable` or YAML actions. All options are compiled during reload; changing the value only chooses an existing graphic. No pack rebuild occurs. Native images and explicit external glyphs retain their normal metric, permission and fallback checks. See [selector fields and example](/reference/yaml/#selecting-a-compiled-graphic).

Declare `tooltip-item-slot: selection_preview` on a component, then supply its native item preview with `setItemTooltip`. The bundled `selection` screen already has that slot:

```kotlin
val result = canvas.openDetailed(player, "selection")
if (result.opened) {
    canvas.setVariable(player, "selected_option", "option_b")
    canvas.setItemTooltip(player, requireNotNull(result.sessionId), "selection_preview", rewardItem)
}
```

Call on the player owner thread after opening completes. The required session ID prevents a delayed addon response from altering a replacement session. False means CraftCanvas/player/session is unavailable or the ID is stale/foreign; no change is made. A matched session with an undeclared slot or invalid item throws IllegalArgumentException. Pass null to clear the slot and restore its text tooltip. Empty/AIR/non-item stacks and counts outside 1..99 are rejected. Items are cloned and converted immediately through Paper's `ItemStack.asHoverEvent()`, preserving the platform's item data components; later changes to the supplied stack cannot mutate the stored tooltip. Failure to construct the hover event leaves the previous slot intact.

A changed hover event marks the session dirty; identical values leave the dirty flag unchanged. The normal action-driven book update applies it, including resend's possible flicker. A slot has one value shared by its referencing components and local book pages in that session. Slots do not become scalar variables, are not included in SessionView, are not saved, and disappear on close, navigation, reload or disable. Local change_page does not notify the server or choose a different server-side ItemStack. These calls neither give rewards nor turn an arbitrary item model into a displayed bitmap. Obtain items through your addon's/provider's own supported item API.
