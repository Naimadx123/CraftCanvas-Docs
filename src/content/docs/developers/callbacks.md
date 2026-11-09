---
title: "Callbacks and asset providers"
---

Pass your enabled Bukkit `Plugin` instance as `owner`; `this` in the example is the addon's JavaPlugin. Each registration returns a `CanvasRegistration`. Its `isRegistered` property reports whether it is active. Call `unregister()` to remove it early; the result is true only for the first removal. `close()` does the same and supports Java try-with-resources. Register callbacks and access registration handles on the global region thread (Paper main thread).

CraftCanvas removes all three registration kinds when the owner receives `PluginDisableEvent`, and removes every addon registration when CraftCanvas shuts down. Manual cleanup from the addon's `onDisable` is optional and safe to repeat. Removal prevents future lookups from calling the addon; it does not interrupt an invocation already running or erase an already displayed client page. Existing action tickets still recheck conditions and resolve callbacks when clicked. Missing callbacks and providers fail closed or use the configured asset fallback.

An owner may replace its own registration of the same kind and ID. Replacement retires the old handle immediately; calling it later cannot remove the replacement. Another owner cannot overwrite an occupied ID. Action, condition and provider IDs occupy separate registries. Native/vanilla providers and installed CraftCanvas integration adapters cannot be replaced through the addon API. Use unique namespaced action/condition IDs and a unique provider ID.

The previous ownerless overloads remain available with their original JVM signatures for existing addons, but are deprecated. They infer ownership through Paper's `JavaPlugin.getProvidingPlugin` using the callback/provider implementation class, then participate in the same automatic cleanup. If that class is supplied by a shared library/plugin or a non-plugin classloader, migrate to an explicit owner; unknown owners are rejected instead of leaving an unowned callback. The legacy methods do not return handles.

Use `handle.unregister()` or `handle.close()` on the global thread to remove a registration before addon shutdown.

The bundled `home` screen declares `greeting`. For your own screen, declare the variables supplied through `open` or `setVariable` in its YAML. Registered callbacks can be referenced by `callback` actions and conditions; selection values may represent any application-defined option.

Player service operations must run on the player's owning region thread (Paper main thread). Registrations, unregister handles and bundle requests require the global region thread (Paper main thread). Reload requests are accepted from any thread; configuration application runs on the global region. Future callback context can vary, especially on immediate rejection or disable, so schedule player work explicitly. Callbacks, conditions and player events run on the corresponding player owner and can execute concurrently for different players on Folia. Implementations must protect their shared state and only access owned world/entity data. Do not wait on reload/bundle futures from a tick thread. Providers expose ID, readiness, version, resolver, identifiers and invalidation. An AssetRequest distinguishes kind, ID, font, character, metrics and permission. Returned ItemStacks must be treated as provider-owned; CraftCanvas clones before changing presentation. CanvasAsset supports component, item, metrics and resourceKey representations.

## Register an action and condition

For checked scalar parameters and typed reads without casts, use the [schema overloads](/developers/typed-callbacks/). They support synchronous actions, asynchronous actions and conditions.

For SQL/HTTP that must finish before the sequence continues, use [registerAsyncAction and async-callback](/developers/async-actions/). Ordinary CanvasAction callbacks remain synchronous.

Run this from the readiness callback in your addon's `onEnable`. Both IDs belong to your addon; use a unique namespace.

```kotlin
val notify = canvas.registerAction(this, "travel:notify", CanvasAction { player, session, data ->
    val destination = data["destination"] as? String ?: "spawn"
    player.sendMessage(Component.text("Selected $destination in ${session.screen}"))
})
val allowed = canvas.registerCondition(this, "travel:ready", CanvasCondition { player, _, _ ->
    player.hasPermission("server.travel")
})
```

A component can use both:

```yaml
conditions:
  enabled: {callback: {id: 'travel:ready'}}
actions:
  click:
    - type: callback
      id: travel:notify
      data: {destination: market}
```

`data` comes from administrator-authored YAML. A client sends only an opaque registered action ticket. Recheck any addon-owned balance, reward eligibility or database state inside the callback before changing it. Callback exceptions fail the sequence; they do not roll back arbitrary changes to external plugins. Keep callbacks short and send external I/O to a worker, applying its result through session-guarded [batch updates](/developers/sessions/#updating-variables-as-one-batch).

## Provide custom items

This provider exposes a vanilla-backed travel ticket. Register it globally after readiness. Use your own supported item API to build a custom stack instead of changing its model into a bitmap.

```kotlin
val ticketProvider = object : CanvasAssetProvider {
    override val id = "travel"
    override val ready = true
    override val version = "1.0.0"

    override fun resolve(player: Player, request: AssetRequest): CanvasAsset? {
        if (request.kind != "item" || request.id != "ticket") return null
        return CanvasAsset(item = ItemStack(Material.PAPER))
    }

    override fun identifiers(kind: String): Collection<String> =
        if (kind == "item") listOf("ticket") else emptyList()

    override fun invalidate() = Unit
}
canvas.registerProvider(this, ticketProvider)
```

Reference it in an item tooltip or item action:

```yaml
tooltip-item: {provider: travel, kind: item, id: ticket}
```

Provider `resolve` receives the player, resource kind/ID, optional font, character, metrics and permission. Return null for unsupported or unavailable resources. Item stacks are cloned for presentation. Fixed visual glyphs require a single character, font and complete metrics matching the screen geometry; for positioned/scaled artwork, distribute a native PNG through an [addon bundle](/developers/bundles/).

`identifiers(kind)` supports diagnostics. `invalidate()` runs globally on reload: clear your resource cache and update readiness there. Player resolution can run concurrently on different Folia regions, so protect any mutable shared state.
