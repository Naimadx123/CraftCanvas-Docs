---
title: "Asynchronous addon actions"
---

Use `registerAsyncAction(owner, id, CanvasAsyncAction)` for YAML sequences that must wait for SQL, HTTP or another asynchronous service. Register globally after `CanvasApi.whenReady(owner)`. The `CanvasRegistration` handle cleans up on owner disable. Async and synchronous IDs have separate registries; `callback` continues to use the synchronous one.

```yaml
actions:
  click:
    - type: async-callback
      id: example:load
      data: {category: profile}
      timeout-ticks: 200
    - type: message
      text: '<green>Loaded: {loaded_label}'
```

The callback receives the player, a detached `SessionView` and the administrator's data map, and returns `CompletionStage<CanvasActionResult>`. Invocation starts on the player's owner (Paper main thread) and must return promptly. CraftCanvas does not automatically offload the invocation or make blocking I/O safe. Use your asynchronous repository/client or your addon's executor. Capture required Bukkit state before leaving that thread.

| Result | Behavior |
| --- | --- |
| `CONTINUE` | Resume the next action on the player's owner |
| `STOP` | Finish the whole current sequence successfully, including enclosing branches/sequences; skip following actions |
| `FAILURE` | Stop through the normal action error handler |

Throwing, exceptionally completed/cancelled stages, null stages/results and timeout also stop through the error handler. The player receives the normal generic action failure; logs contain the source and error. No retry is performed. Completion may originate on any thread. Following actions run once in the original active session; their action conditions/events are evaluated when they execute.

The session stays busy while waiting, blocking other action buttons while the unconditional X remains usable. Each pending invocation consumes one `tasks-per-session` slot. `timeout-ticks` defaults to 200 and accepts integers in 1..1200. It is an owner-scheduled server-tick deadline, including completion dispatch; low TPS can extend wall-clock duration. Existing render budgets and loading states apply.

Closure, navigation, successful reload, disconnect, session timeout and CraftCanvas disable cancel the wait. Unregistering, replacing a registration or disabling its owning addon also cancels pending invocations and releases active sessions through the failure handler. An old handle cannot remove a replacement. Late completion cannot resume the old sequence or change a replacement session through that sequence.

Cancellation requests `cancel(false)` when the returned stage implements `Future`. This is best effort: it does not promise interruption or cancellation of upstream operations. Addon-owned workers, queued API calls and external effects remain the addon's responsibility. Link your repository's cancellation mechanism and clean up jobs on addon disable when needed. Guard delayed UI updates with `session.id`; this prevents replacement-screen updates but is not an addon-lifecycle token. CraftCanvas does not roll back SQL/HTTP effects or arbitrary Bukkit changes.

The compiled [Java](https://github.com/Naimadx123/CraftCanvas/blob/master/examples/addon/java/example/CanvasAsyncLoading.java) and [Kotlin](https://github.com/Naimadx123/CraftCanvas/blob/master/examples/addon/kotlin/example/CanvasAsyncLoadingKotlin.kt) examples take a nonblocking loader and apply its string result through `setVariablesAsync` before returning `CONTINUE`. They use `loaded_label` in [async_callback.yml](https://github.com/Naimadx123/CraftCanvas/blob/master/examples/async_callback.yml). Install that screen with its frame/assets, register `example:load`, then open `async_callback`. This feature does not introduce a custom parameter schema.

`purchase` rejects `async-callback` before execution: external requests cannot participate in the immediate reward snapshot/refund flow. Implement eligibility, idempotency and payment/reward reconciliation in your addon. Cinematic `start`/`covered` also reject waiting callbacks, including nested ones; `uncovering`/`complete` can await them like delays, without pausing the client shader timeline.
