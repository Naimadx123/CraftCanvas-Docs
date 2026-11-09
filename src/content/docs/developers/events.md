---
title: "Player events"
---

Register Bukkit listeners using your addon instance. CraftCanvas fires these events on the corresponding player's owner: Paper main thread or Folia entity region. Different Folia players can invoke listeners concurrently.

| Event | Fields | Cancellable |
| --- | --- | --- |
| ScreenOpenEvent | player, session | Yes |
| ScreenCloseEvent | player, session, reason | No |
| ActionExecuteEvent | player, session, component, action | Yes |
| SelectionChangeEvent | player, session, previous, selected | No |

All events are in `zone.vao.craftcanvas.event`. Session snapshots expose the screen ID and current variables; they are not mutable session handles.

## Restrict an opening

```java
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import zone.vao.craftcanvas.event.ScreenOpenEvent;

public final class MenuAccessListener implements Listener {
    @EventHandler
    public void onOpen(ScreenOpenEvent event) {
        if (event.getSession().getScreen().equals("travel")
                && !event.getPlayer().hasPermission("server.travel")) {
            event.setCancelled(true);
        }
    }
}
```

Register it in `onEnable`:

```java
getServer().getPluginManager().registerEvents(new MenuAccessListener(), this);
```

An opening event runs before completion. Cancellation makes the API return CANCELLED; the event alone does not confirm a successfully rendered screen. Use the opening result for that.

## Observe a selection

Inside a listener class:

```kotlin
@EventHandler
fun selected(event: SelectionChangeEvent) {
    if (event.session.screen != "travel") return
    event.player.sendMessage(Component.text("Selected ${event.selected}"))
}
```

`select` updates the session selection and emits this event. Changing an arbitrary variable does not implicitly emit a selection event. If your addon stores its choice in a variable, observe your callback instead.

## Stop an action

Inside a listener class:

```kotlin
@EventHandler
fun action(event: ActionExecuteEvent) {
    if (event.session.screen == "travel" && event.action == "console-command") {
        event.isCancelled = true
    }
}
```

Use this for addon-specific policy. The event does not bypass normal session tickets, component conditions or permissions.

## Closing and cleanup

`ScreenCloseEvent.reason` describes the server's close operation. Escape and native Done can close a book locally without a reliable immediate server packet; do not use this event as an exact client-window lifecycle signal. Session timeout, gameplay signals and explicit X eventually retire it. During Folia shutdown, regions may already be halted, so plugin cleanup must also run in your addon's `onDisable` rather than relying on per-player close events.

No hover event exists: tooltip and cursor behavior are local client text interactions. For scoped callback cleanup, use [registration ownership](/developers/callbacks/).
