---
title: "Discovering screen declarations"
---

`canvas.screenCatalog()` returns an immutable snapshot of one published configuration. Read it on any thread without a player, scheduling, I/O or pack generation. Repeated reads reuse the cached object until configuration publication. Capture it once when inspecting several screens; separate service calls can observe separate reloads. Retained catalogs remain unchanged and readable after later reloads or plugin disable.

```java
CanvasScreenCatalog catalog = canvas.screenCatalog();
CanvasScreenInfo screen = catalog.screen("interactive_ui");
if (catalog.getAvailable() && screen != null) {
    CanvasVariableInfo query = screen.variable("session.query");
    CanvasComponentInfo notice = screen.component("notice");
    if (query != null && query.accepts("diamond") && notice != null) {
        // Use the declared variable/target in your integration.
    }
}
```

`available` is false with an empty map while the service is loading or disabled. A ready plugin with no screens has `available: true` and an empty map. `catalog.screens` is keyed by exact screen ID, including namespaces, in sorted order. Unknown IDs return null. Only successfully compiled, published screens appear; invalid files skipped at startup are absent. Installing a bundle or editing a file does not update the catalog until a configuration is applied.

| Screen field | Meaning |
| --- | --- |
| `id`, `title`, `permission` | Exact ID, original MiniMessage/placeholder title template and optional root permission |
| `font` | Resolved base font, defaulting to minecraft:default; inline MiniMessage font tags may override fragments |
| `renderer`, `bookUpdate` | Resolved `book-glyph`/`cinematic` renderer; `resend`/`local-pages`, or null for cinematic |
| `timeoutTicks` | Declared session timeout |
| `defaultProfile`, `profiles` | Configured default plus allowed profile names/scales; not the profile selected by a particular player |
| `requiredCapabilities` | Explicit and inferred requirements, including nested component features |
| `variables` | Unqualified variable names mapped to immutable declarations |
| `components`, `rootComponents` | All component IDs and ordered root IDs after inheritance/template resolution |

Variables expose `name`, `qualifiedName`, `type`, `scope`, `defaultValue` and `persistent`. Types are `CanvasVariableType.STRING`, `INTEGER`, `NUMBER`, `BOOLEAN`; scopes are `CanvasVariableScope.SESSION`, `PLAYER`, `GLOBAL`. Defaults normalize to String, Long, Double or Boolean. They are declarations, not current player/global/session values. The persistent flag is a declaration; actual persistence also depends on server storage settings.

All variables have defaults, so opening data can omit them. `screen.variable("query")` accepts an unqualified name; `screen.variable("session.query")` accepts only its matching scope-qualified alias. A scope mismatch returns null. `variable.accepts(value)` checks the same value rules as opening: strings up to 4096 UTF-16 units, booleans, finite numbers and exact signed 64-bit integers. It rejects null. Integer **variables** accept integral numeric values such as 1.0, matching the existing variable store; typed callback **parameters** deliberately use stricter integer literals. Numeric metadata normalization does not change runtime variable storage.

Components expose `id`, YAML `type`, `parentId`, immediate `children`, `interactive`, explicit action `triggers`, `hasAnimation`, `hasAutoplay`, `boundVariable`, `searchVariable` and `tooltipItemSlot`. The component map is flattened and includes nested/offscreen/conditionally hidden entries. Parent and child IDs preserve hierarchy without exposing implementation definitions. Built-in controls can be interactive even with no explicit triggers. `hasAnimation` means a configured replayable component animation; autoplay and image-frame timelines are different mechanisms.

`component.listSource` is `"addon"` for a scroll declared with `data.source: addon`, or null for static scrolls and other components. Such a container accepts [dynamic records](/developers/dynamic-lists/) through `setListItems`; its declared children are reusable row slots.

No visibility/enabled conditions, callback conditions, placeholders, permissions or player resource checks run during discovery. A declared interactive component may currently be disabled. A notification can have an animation while its slot is hidden. Use the current session ID with GUI methods and inspect `openDetailed`/`openAsync` results for authoritative acceptance. Reload may remove or alter a screen between preflight and opening; discovery neither reserves nor authorizes it. Source paths, action bodies, mutable internal objects and live session state are not exposed.

Collections are defensively copied and unmodifiable, including nested variable/component maps, profile maps, capability sets, root/child lists and trigger sets. Configuration preparation does not publish metadata; successful application publishes a new catalog. Rejection before application retains the previous catalog. An application rollback can publish an equivalent new snapshot, so do not use object identity as a durable version or session token.

Compiled [Java](https://github.com/Naimadx123/CraftCanvas/blob/master/examples/addon/java/example/CanvasScreenDiscovery.java) and [Kotlin](https://github.com/Naimadx123/CraftCanvas/blob/master/examples/addon/kotlin/example/CanvasScreenDiscoveryKotlin.kt) examples inspect declarations and pass a validated greeting to `openAsync`, retaining its normal runtime checks. [Session snapshots](/developers/sessions/) provide live values on the player's owner; the catalog provides declarations on any thread.

## Code-defined screens

Published code-defined screens appear in the same catalog as YAML screens. `screenDefinition(id)` additionally returns detached schema data; code definitions retain their native resource declarations. A staged registration is not visible until a successful full reload. See [registering code screens](/developers/code-screens/) and [reading effective session state](/developers/component-state/).
