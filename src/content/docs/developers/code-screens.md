---
title: "Code-defined screens"
---

`CanvasService.registerScreen(owner, definition)` registers an immutable screen in memory. It creates no screen YAML or asset manifest. Code screens use the same schema, compiler, conditions, actions, layout validation and resource-pack renderer as YAML screens. They appear in `screenCatalog()` after a successful full reload.

## Register and publish

Obtain the service with `CanvasApi.whenReady(owner)`. Register on Paper's main thread or Folia's global thread, then call `canvas.reload()` and inspect the asynchronous result. Future continuations have no fixed thread context; schedule registrations on the global scheduler if a readiness continuation runs on another thread. Never block a tick thread with `join()` or `get()`.

This Kotlin definition inherits the bundled `frame` and artwork:

```kotlin
val definition = CanvasScreenDefinition.builder("example:code-menu")
    .extendsScreen("frame")
    .title("Addon menu")
    .variable("claimed", CanvasVariableType.BOOLEAN, false)
    .component("claim", mapOf(
        "type" to "button",
        "position" to mapOf("x" to 112, "y" to 128),
        "size" to mapOf("width" to 160, "height" to 28),
        "layer" to 20, "align" to "center", "vertical-align" to "center",
        "text" to "Claim",
        "asset" to mapOf("provider" to "native", "kind" to "image", "id" to "craftcanvas:button"),
        "tooltip" to listOf("Claim your reward"),
        "actions" to mapOf("click" to listOf(mapOf("type" to "message", "text" to "Claimed!")))
    ))
    .build()
val registration = canvas.registerScreen(owner, definition)
canvas.reload().thenAccept { result ->
    if (!result.successful) owner.logger.warning(result.toString())
}
```

After successful publication and pack loading, open `example:code-menu` through `openDetailed` on the player's owner or `openAsync` from a worker. Registration and pack generation do not acknowledge that a client has loaded the pack.

Compiled examples: [Java](https://github.com/Naimadx123/CraftCanvas/blob/master/examples/addon/java/example/CanvasCodeScreens.java) and [Kotlin](https://github.com/Naimadx123/CraftCanvas/blob/master/examples/addon/kotlin/example/CanvasCodeScreensKotlin.kt).

## Full schema and native resources

The constructor accepts `CanvasScreenDefinition(id, fields[, images, sounds])`. Maps and lists are copied recursively and made read-only. Values must be literal strings, finite numbers, booleans, null, lists or string-keyed maps. Definitions have a maximum nesting depth of 40 and 100,000 data nodes. IDs accept letters, digits, `_`, `.`, `:`, `-` and at most 128 characters.

The builder provides these helpers:

| Method | Purpose |
| --- | --- |
| `field(name, value)` | Any schema field, including `layout`, `book`, `first-open`, `session`, `theme`, `font` or `cinematic` |
| `extendsScreen(id)` / `title(text)` | Root inheritance and window title |
| `component(id, fields)` | Add or replace a component declaration; nested children use ordinary schema maps |
| `variable(name, type, default[, scope, persistent])` | Typed variable declaration |
| `image(id, path, width, height)` | Native PNG resource; dimensions must match the file and be within 1..2048 |
| `sound(id, path)` | Native OGG resource |
| `build()` / `definition.toBuilder()` | Create a detached definition or edit a copy |

Native resources need a `craftcanvas:` ID, for example `craftcanvas:myaddon/panel`. Supply a real file path, such as a resource extracted into your addon's data folder. No CraftCanvas asset YAML is required. Resources are included in the same generated pack and optional Nexo export. IDs must not collide with YAML resources; repeated identical declarations from code are allowed.

Inheritance is optional. A complete `book-glyph` definition can supply its own `layout`, unconditional root native `background` image and unconditional root close button. Register its background PNG with `.image(...)` and reference it in the component. It can then compile with an empty `screens/` directory. The [book renderer constraints](/reference/book-renderer/) still apply, including the close control, bitmap dimensions/ascent and overlapping controls. Cinematics use their normal schema and shader requirements through `.field("backend", "cinematic")` and `.field("cinematic", ...)`.

Callback actions and conditions registered by addons work exactly as in YAML. Use the regular `callback` / `async-callback` schema data in component actions; Java/Kotlin handlers stay in their normal registration, outside the literal definition map.

## Read, replace and remove

`screenDefinition(id)` returns a detached definition from the published configuration, safe on any thread. For YAML screens it contains the resolved root inheritance/theme schema; component template references still use the ordinary schema. For code screens it returns the registered definition, including resource declarations. Unknown or unpublished IDs return null. Use `screenCatalog()` for normalized declaration metadata and [uiState](/developers/component-state/) for player-specific effective state.

Only the same enabled owner may replace an existing code registration. Register a modified `toBuilder().build()` value, then run a full `reload()`. A YAML screen with the same ID is a configuration conflict. Invalid definitions reject the reload and leave the published configuration active. Registration changes during preparation also reject application; retry after registrations stabilize.

Handles are token-bound: closing an old handle cannot remove a replacement. Closing the current handle or disabling its owner immediately makes the screen unavailable and closes sessions that depend on it, including YAML screens inheriting from it. The published declaration may remain readable until the next successful full reload, but opening it is rejected. Unrelated sessions remain open. Register again after owner re-enable; registrations are not persisted across server restarts.

Geometry, native resources, declared appearance assets and font metrics require definition replacement and a rebuilt/reloaded resource pack. For frequent text/value/visibility changes, use [session component edits](/developers/component-state/) instead.
