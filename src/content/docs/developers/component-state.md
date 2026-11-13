---
title: "Component state and edits"
---

`CanvasService.uiState(player, sessionId)` reads an immutable snapshot of the current session on the player's owning thread. Use `uiStateAsync` from workers or another region. A stale/foreign session ID, offline player or unavailable service returns null. Keep the session ID from `openDetailed` or `session(player)` before asynchronous work.

## Read effective state

`CanvasUiState` contains the `SessionView`, active profile, busy/modal/input flags, first-open status and whether frame animation or a cinematic is active. Its component map includes hidden declarations; `component(id)` returns null for an unknown ID.

| Component property | Meaning |
| --- | --- |
| `visible`, `enabled`, `actionAvailable` | Included in current layout, effective enabled state, and whether ordinary click eligibility currently passes visibility/conditions, busy/motion, modal/input, permission, cooldown and debounce checks |
| `appearance`, `selected`, `locked`, `loading` | Chosen normal/selected/disabled/locked/loading presentation and underlying flags |
| `bounds` | Profile-scaled logical screen box before native book-origin offsets; null outside visible layout |
| `text`, `tooltip`, `itemTooltip`, `item` | Resolved Adventure presentation and item preview; mutable ItemStacks are cloned on access |
| `asset`, `overlay` | Selected resource references, including provider, kind, metrics and fallback |
| `configuredStates`, `overrideState` | All declared appearance variants/conditions and the current session override |
| `value` | Current input-control value or resolved progress/stat value |
| `cooldownRemainingMillis`, `debounceRemainingTicks` | Shared player deadline and short component click debounce |
| `scroll`, `notification`, `motion` | Current viewport, message lifetime and component-motion state |
| `listItems`, `listSelection`, `autoplay` | Addon records, the row's currently bound record and active looping-image status |

Collections are copied and read-only. Earlier snapshots retain their data after later edits. Reads evaluate current conditions, placeholders and layout on the player owner, including search filtering and viewport clamping. Hidden components still expose their declaration and effective presentation with no active bounds. `configuredStates` contains `normal` plus declared variants; it does not invent undeclared appearance assets.

This is server state. Client cursor hover, locally navigated book pages, Escape closure, current shader pixels and unsubmitted keyboard input are not acknowledged by Minecraft. Motion bounds describe the server layout target in shader mode. While a modal/cinematic is active, component declarations describe the underlying book layout; the flags identify the active mode. `actionAvailable` is a current eligibility read, not a reserved action ticket or a guarantee that later addon listeners will accept execution.

```kotlin
val state = canvas.uiState(player, sessionId) ?: return
val button = state.component("claim") ?: return
if (button.actionAvailable) {
    owner.logger.fine("Claim button is available on ${state.profile}")
}
```

## Change one session

`updateComponent(player, sessionId, target, CanvasComponentUpdate(...))` replaces the whole override for that component. It is not a patch merged with the previous override. Null properties use the configured value; passing a null update clears the entire override.

```kotlin
canvas.updateComponent(player, sessionId, "claim", CanvasComponentUpdate(
    appearance = CanvasComponentAppearance.DISABLED,
    text = Component.text("Claimed"),
    tooltip = Component.text("Come back later")
))
// Restore the configured presentation:
canvas.updateComponent(player, sessionId, "claim", null)
```

Use `visible = false` to hide a declaration. `visible = true` still respects configured visibility, parent layout, tab, page and notification rules. Appearance choices are `NORMAL`, `SELECTED`, `DISABLED`, `LOCKED`, `LOADING`; permissions, configured locks and busy state remain authoritative. A missing appearance variant falls back to its configured normal presentation. Protected close controls cannot be hidden, disabled, locked or put into loading; the native background rejects session overrides.

Text and tooltip overrides are literal Adventure text, up to 4096 UTF-16 units with children, colors and bold. No MiniMessage/placeholder expansion, custom fonts, events, insertions, shadows, translations or non-text components are accepted. Body text also rejects unsupported decorations through the standard text renderer. Controls keep their current value suffix and progress/stat components keep their generated meter. Set `clearTooltip = true` with no tooltip value to remove both text/item tooltips and the component's shader hover marker for this session. Changing body text uses discrete motion for that component when needed.

## Apply a partial change

`patchComponent(player, sessionId, target, CanvasComponentPatch(...))` changes only supplied properties. Omitted properties keep their previous overrides. It is a new operation; `updateComponent` keeps its full replacement behavior.

```kotlin
canvas.patchComponent(player, sessionId, "claim", CanvasComponentPatch(text = Component.text("Claimed")))
canvas.patchComponent(player, sessionId, "claim", CanvasComponentPatch(appearance = CanvasComponentAppearance.DISABLED))
// The second call keeps the text "Claimed" and any previous tooltip/visibility override.
```

Use `resetProperties` to restore selected properties from configuration while preserving other overrides:

```kotlin
canvas.patchComponent(player, sessionId, "claim", CanvasComponentPatch(
    resetProperties = setOf(CanvasComponentProperty.TEXT)
))
```

Java has a fluent builder:

```java
canvas.patchComponent(player, sessionId, "claim", CanvasComponentPatch.builder()
    .text(Component.text("Claimed"))
    .appearance(CanvasComponentAppearance.DISABLED)
    .build());
canvas.patchComponent(player, sessionId, "claim", CanvasComponentPatch.builder()
    .reset(CanvasComponentProperty.TEXT)
    .build());
```

Resettable properties are `VISIBILITY`, `APPEARANCE`, `TEXT`, `TOOLTIP`. Resetting tooltip restores its configured text/item tooltip and hover behavior. `clearTooltip = true` hides the tooltip/hover marker instead. A new tooltip value clears a previous suppression flag. An omitted tooltip and `clearTooltip = false` preserve existing tooltip state. An empty patch preserves all properties. Setting and resetting the same property in one constructor is rejected; a builder's last call for a property takes precedence.

The patch is immutable, including its copied reset set. `patchComponentAsync` schedules the same operation from any thread. The player/session checks run before target/render validation. The entire merged override is validated before mutation, so an invalid text component cannot partially change visibility or appearance. Protected controls, literal-text limits, render budgets and modal behavior are the same as `updateComponent`. To remove the entire override, continue using `updateComponent(..., null)`.

## Other state setters

| Method | Effect |
| --- | --- |
| `setVariables(player, sessionId, values)` | Validated typed variable batch, including control values |
| `setComponentCooldown(player, sessionId, target, seconds)` | Set a declared player cooldown to 0..2592000 seconds; zero clears it; shared keys affect every matching control |
| `playComponentAnimation` / `stopComponentAnimation` | Replay configured component motion / settle it at final geometry |
| `setSelection(player, sessionId, value)` | Set an application selection string, at most 4096 characters; non-null emits SelectionChangeEvent; null clears without an event |
| `setTab(player, sessionId, tab)` | Choose a tab string, at most 4096 characters, and reset page to zero |
| `setPage(player, sessionId, page)` | Set server pagination in 0..10000; independent of client local-page navigation |
| `setItemTooltip`, `showNotification`, `dismissNotification`, `setScrollOffset`, `setListItems` | Existing tooltip, notification, viewport and dynamic-list operations |

Every new player operation has an `Async` equivalent returning a CompletableFuture. For several operations on one owner, use `withPlayer`. Unknown/wrong-kind targets and invalid values throw for a matching session; stale session checks run first and return false. These presentation setters reject cinematic sessions. Reads can still identify their cinematic status.

Accepted edits mark the session dirty, obey rendering budgets and defer while a modal is open. They invalidate ordinary stale action tickets while preserving modal/close controls. State disappears with the session; declared persistent player cooldowns use the configured storage. A true result means accepted by the server, not displayed by the client. Geometry, assets, definitions and font changes go through [code registration and full reload](/developers/code-screens/), which rebuilds the pack.
