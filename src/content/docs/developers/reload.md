---
title: "Reloading with a completion result"
---

After registering screens or installing YAML/assets, call `canvas.reload()` once to validate and apply them. It returns a future with a result you can check. You can request it from any thread.

The work includes pack generation. Use it during startup or an administrator-requested change, rather than for every click or variable update.

## Install files, then reload

`bundle` below is a `CanvasAddonBundle`. Start the installation from the global/main thread; its continuation can call `reload()` directly:

```kotlin
canvas.installBundle(this, bundle, CanvasBundleMode.INSTALL).thenAccept { installation ->
    if (installation.successful) {
        canvas.reload().thenAccept { reload ->
            if (reload.successful) {
                logger.info("Loaded ${reload.screenCount} screens; pack SHA-1=${reload.packSha1}")
            }
            reload.diagnostics.forEach { logger.warning("${it.stage}: ${it.message}") }
        }
    } else {
        installation.diagnostics.forEach { logger.warning("${it.path}: ${it.message}") }
    }
}
```

Use reloads in an administrator/startup workflow. The API is for trusted server plugins and does not check a command sender's permissions. Batch installations before reloading when possible. Commands and API calls share one guard: an overlapping request returns BUSY rather than queuing or assuming the existing operation includes its changes. Retry deliberately after the existing reload finishes. An UNCHANGED bundle installation does not prove that its files have been applied; decide whether to reload for your workflow.

## Read the result

| Status | Meaning |
| --- | --- |
| `SUCCESS` | The prepared configuration and generated pack are active. Integration/delivery warnings may be present. |
| `REJECTED` | Preparation or application failed; the prior configuration/catalog remain active. |
| `BUSY` | Another reload owns the operation; this request did no work. |
| `UNAVAILABLE` | CraftCanvas is loading or disabled. |
| `CANCELLED` | CraftCanvas was disabled before an accepted request completed, including queued main-thread requests. |

`CanvasReloadResult.successful` is true only for SUCCESS. `screenCount`, `packSha1` and `packChanged` describe a successful operation; count/hash are null on other statuses. The hash identifies the generated CraftCanvas pack, not Nexo's final merged pack. `packChanged` detects a changed CraftCanvas SHA-1 or effective pack UUID, invalidating player acknowledgements only when needed.

Diagnostics contain `stage`, `message`, `level` (ERROR or WARNING), and optional `file`, `screen`, `component` and YAML `path`. Stages are DEFAULTS, CONFIGURATION, PACK, EXPORT, APPLY, INTEGRATIONS, DELIVERY and LIFECYCLE. Source fields are present when the compiler supplies structured YAML context; other failures retain their message. Inherited definitions use the compiler's resolved-screen context. The first failed preparation stage is returned; reload does not aggregate all possible compiler errors.

Optional `canvas.reload("craftcanvasdaily:daily")` follows the existing command behavior: compile/validate all files, refresh shared settings/assets, and replace only that screen's compiled definition. Other compiled screens remain. Success closes sessions and invalidates old action tickets. Preparation errors preserve active sessions. An application failure may already have closed sessions; retaining configuration does not restore those sessions.

Reload does not undo administrator edits, installed bundles or default-field synchronization. Preparation may write generated artifacts or an export before a later stage fails. The old runtime catalog continues to reference its immutable pack release. This is not a transaction over every file or external integration.

SUCCESS does not confirm hosting, asynchronous Nexo generation/upload, player acceptance or client rendering. Pack senders and SUCCESSFULLY_LOADED acknowledgements remain authoritative. Immediate provider/send failures become warnings after application, allowing other providers and players to proceed. Later asynchronous sender failures use the sender's normal diagnostics.

Never block the server thread with `get()` or `join()`. Continuations attached to completed futures may run on their caller's thread; asynchronous continuations may use workers. Schedule Bukkit operations appropriately. Shutdown completes pending requests without applying late worker results. Cancelling or externally completing the returned future changes only that caller's view; it does not cancel the operation or release its guard. Disabling an addon alone does not cancel a global reload it requested.

## Registered screens

`registerScreen` stages in-memory definitions; use `reload()` without a screen ID after adding, replacing or removing registrations. A selected-screen reload is rejected while the registration revision differs from the published configuration. If registrations change during preparation, application is rejected and the published configuration stays active. Retry a full reload once registration is stable. Closing a screen registration or disabling its owner immediately withdraws that screen and inherited dependents, closing affected sessions even before the next reload.
