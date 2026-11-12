---
title: "Controlling an active GUI"
---

Addons can control declared GUI components without constructing YAML actions. Obtain `CanvasService` through `CanvasApi.whenReady`, open the screen and retain its session ID. These methods do not create components or rebuild the resource pack. Install [interactive_ui.yml](https://github.com/Naimadx123/CraftCanvas/blob/master/examples/interactive_ui.yml) with its bundled frame/assets for the target IDs used below.

| Method | Effect |
| --- | --- |
| `showNotification(player, sessionId, target, text[, durationTicks])` | Replace a notification's content/lifetime and restart its configured entrance animation |
| `dismissNotification(player, sessionId, target)` | Remove a notification and its entrance motion; an already absent message is accepted |
| `playComponentAnimation(player, sessionId, target)` | Replay the visible component's declared `animation`; image-frame `animate` playback is separate |
| `setScrollOffset(player, sessionId, target, offset)` | Set a visible scroll container's zero-based row offset, clamped against its currently visible/filtered entries |

Every method returns a boolean and has an `Async` counterpart returning `CompletableFuture<Boolean>`. Synchronous calls require the player's owner thread (Paper main thread). Async calls can originate on workers and use the existing player scheduler; exceptions fail the future. Cancelling a queued future prevents its operation from starting. Never block a tick thread waiting for completion.

Java, after opening `interactive_ui`:

```java
CanvasOpenResult result = canvas.openDetailed(player, "interactive_ui");
if (result.getOpened()) {
    UUID sessionId = result.getSessionId();
    canvas.showNotification(player, sessionId, "notice",
        Component.text("Saved!", NamedTextColor.GREEN), 60);
    canvas.setScrollOffset(player, sessionId, "results", 0);
    canvas.playComponentAnimation(player, sessionId, "status");
}
```

For asynchronous work, capture the session ID before starting the request. A later response cannot change a replacement screen:

```kotlin
val sessionId = canvas.session(player)?.id ?: return
repository.save().thenCompose {
    canvas.showNotificationAsync(player, sessionId, "notice", Component.text("Saved!"))
}.thenAccept { accepted ->
    if (!accepted) logger.fine("The original GUI is no longer available")
}
```

This is an application workflow using your repository, not an asynchronous CraftCanvas action sequence. Completion callbacks have no guaranteed thread context. Use `withPlayer` for subsequent player operations.

False means that the service/player/session is unavailable, the ID is stale/foreign, the screen is cinematic, or an animation/scroll target is not currently visible. These checks precede target/value validation. A matching session with an unknown component, wrong component kind, missing animation or invalid parameter throws `IllegalArgumentException`. Notification slots are normally hidden until shown, so showing/dismissing them does not require current visibility. Their configured conditions still control whether they render.

Notifications accept immutable Adventure text components with children, colors and bold. Content is literal: MiniMessage-looking strings and placeholders are displayed as text. Per-fragment fonts, shadows, interactions, sprites, translations and baseline decorations such as italic are rejected before mutation. Notifications inherit the screen font; missing body glyphs display a positioned box. See [screen fonts](/guides/fonts/). Text is limited to 2048 UTF-16 units; duration defaults to 60 ticks and accepts 1..1200. Scroll offsets must be nonnegative; large values clamp to the last full viewport. Current search and visibility rules are re-evaluated when setting the offset.

True means accepted, including no-op dismissals and clamped offsets; it does not acknowledge client display. Operations mark the session dirty, obey the update budget and defer rendering during text input/confirmation. Modal tickets remain valid. Expiry continues while a modal is open. Navigation, closure, reload or disconnect discard this state. Static graphics, font metrics and animations must already exist in the loaded resource pack. [Shader motion and discrete fallbacks](/guides/interactive-ui/#component-animations) apply identically to API-triggered motion.

Compiled examples: [Java](https://github.com/Naimadx123/CraftCanvas/blob/master/examples/addon/java/example/CanvasGuiControls.java) and [Kotlin](https://github.com/Naimadx123/CraftCanvas/blob/master/examples/addon/kotlin/example/CanvasGuiControlsKotlin.kt).

For all component declarations and effective presentation, use `uiState(player, sessionId)`. `updateComponent` replaces literal text, tooltip, visibility and appearance for one session. Cooldown, selection, tab and page setters are also available; see [component state and edits](/developers/component-state/). Create the declarations themselves using [code-defined screens](/developers/code-screens/) when no screen YAML is needed.
