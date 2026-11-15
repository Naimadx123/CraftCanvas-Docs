---
title: "Dynamic lists"
---

Use a dynamic list when the rows come from your addon: saved homes, shop results or database records. You declare a few reusable row slots in the screen; your plugin supplies the records they display.

The screen [dynamic_list.yml](/examples/dynamic_list.yml) contains a two-row `results` scroll. It uses `data.source: addon`. Install that example with the shared frame/assets, register its callbacks and open it before filling the list.

## 1. Declare row slots


Declare `data.source: addon` on a `scroll`, with `book.update: resend` (the default). Its children are reusable row slots: declare exactly one child for each visible row. For a viewport of height 52 with row-height 24 and gap 4, declare two slots. Slots may be buttons or groups with nested text/buttons and unique component IDs. Existing component templates can share their appearance and actions. The addon can supply many more records than the number of slots; an empty or not-yet-populated list displays no rows. Ordinary scrolls without `source` retain static YAML entries.

## 2. Supply records

Use a stable ID for each result. Fields such as `name` and `price` become row text. This Java code uses the ID captured when the menu opened:

```java
List<CanvasListItem> items = List.of(
    new CanvasListItem("iron", Map.of("name", "Iron sword", "price", "50"), "iron sword weapon"),
    new CanvasListItem("apple", Map.of("name", "Apple", "price", "5"), "apple food")
);
canvas.setListItemsAsync(player, capturedSessionId, "results", items);
```

Slot text, state text, text tooltips and progress values can use `{item.id}` and `{item.name}` (or another field name). Missing fields produce empty text. Values are inserted literally: MiniMessage tags, placeholder syntax, events, actions and assets cannot be supplied by records. Ordinary font/overflow rules still apply. Assets, geometry, state conditions and actions remain declared in YAML and compiled into the resource pack. This works with shaders disabled; shaders are needed only for features which already require them.

## 3. Search and scroll

`data.search-variable` uses the existing session string query. Search matches each record's `searchText`, which defaults to its field values joined with spaces. It uses the same accent-insensitive, case-insensitive word matching as static scrolls. Record search text is literal and is not expanded as placeholders; slot `search-text` is not used for addon lists. Scroll buttons, API offsets and `{scroll.results.total/offset/visible}` refer to filtered records. Query changes reset the offset; replacement snapshots retain and clamp it. Conditional children may hide content inside a row; keep slot roots visible so each record has a visible row.

## 4. Handle a row click

Row `callback` and `async-callback` handlers receive `session.listSelection`: the container ID and the immutable record captured when the action sequence started. Nested actions, delays, confirmations and subsequent callbacks retain that record even if the list refreshes while an action is running. `ActionExecuteEvent` and selection events carry it too. Ordinary `session(player)` reads, non-row actions and callback conditions have no list selection. YAML callback `data` and typed schemas remain literal administrator configuration; inspect `listSelection.item` in the handler for record data. Row placeholders are for display, not command/action interpolation. Validate application permissions, stock and current prices in your addon before making repository changes.

## Replacing a list safely

Changing snapshots invalidates existing row click tickets. A query/offset change is also checked against the record when a click arrives, even when the row keeps the same position. Confirmation tickets for changed/removed records are rejected and the modal is cleared; running asynchronous work is not cancelled merely because a list changes. Keep a captured session ID for late writes. It rejects responses for a closed/replaced/reloaded screen, but ordering multiple requests within the same session remains the addon's responsibility.

Player methods follow the [GUI thread and lifecycle contract](/developers/gui/): synchronous calls require the player owner, async helpers accept any thread and copy the collection before scheduling. The items copy their field maps at construction. Calls return false for stale/foreign/unavailable sessions or cinematics; unknown targets, static scrolls or invalid snapshots throw for a matching session. Hidden lists may be populated. Validation occurs before replacing any records. Updates request a screen update, respect the render budget and defer display while a modal is open. Records and bindings disappear on close; they are never persisted or migrated.

## Limits

Limits per container: 1000 unique nonblank IDs of at most 128 printable characters; 32 fields per record; field names match `[a-zA-Z][a-zA-Z0-9_-]{0,63}` with `id` reserved; field values at most 2048 characters; search text at most 4096; total snapshot text at most 262144 characters, counting IDs, field names, values and search text. Use backend pagination or narrow your query for larger datasets. The screen catalog exposes `component.listSource == "addon"` so addons can discover compatible targets.

Complete loader examples: [Java](/examples/addon/java/example/CanvasDynamicLists.java) and [Kotlin](/examples/addon/kotlin/example/CanvasDynamicListsKotlin.kt).
