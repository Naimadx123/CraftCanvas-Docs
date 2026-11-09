---
title: "Installing addon files"
---

`CanvasAddonBundle` describes an addon ID, a version string and a map of destination paths to resources inside the owning plugin's JAR. Request installation on the global region thread (Paper main thread) after the CraftCanvas service becomes ready. Resource reading, validation and filesystem changes run on CraftCanvas's file executor, serialized with pack builds and configuration preparation. The caller's file map is copied before queuing.

```kotlin
val bundle = CanvasAddonBundle(
    id = "travel",
    version = "1.0.0",
    files = mapOf(
        "screens/menu.yml" to "canvas/screens/menu.yml",
        "assets/images.yml" to "canvas/assets/images.yml",
        "assets/background.png" to "canvas/assets/background.png",
        "assets/close.png" to "canvas/assets/close.png"
    )
)
canvas.installBundle(this, bundle, CanvasBundleMode.INSTALL).thenAccept { result ->
    if (!result.successful) logger.warning(result.diagnostics.joinToString { "${it.path}: ${it.message}" })
}
```

The installer inserts the bundle ID after the destination's top-level directory. The example creates `screens/travel/menu.yml` and `assets/travel/…` inside the CraftCanvas data directory. Supported directories are `screens`, `themes`, `templates`, `actions` and `assets`. The first four accept `.yml`; assets accept `.yml`, `.png` and `.ogg`. Global configuration, language catalogs, generated fonts, ZIPs and shader overrides are not bundle destinations. PNGs used for bitmap glyphs belong in the native asset manifest; there is no arbitrary font-provider installation API.

Bundle IDs match `[a-z0-9][a-z0-9_-]{0,63}`. Destination paths use lowercase letters, digits, underscores, hyphens and dots in slash-separated segments; no empty/dot segments, Windows device names, trailing dots, absolute paths, backslashes or symbolic links are accepted. Paths have at most 512 characters and segments at most 128. JAR resource paths additionally allow uppercase letters. Limits are 512 files, 16 MiB per file and 64 MiB per request; existing files considered for an update also have a 64 MiB total limit. PNG dimensions are limited to 2048 × 2048.

Definition IDs must use the bundle namespace, for example `id: travel:menu`. Native image/sound IDs must start with `craftcanvas:travel/`. Manifest `file` values are relative to CraftCanvas's `assets/` directory and include the inserted bundle directory:

```yaml
schema-version: 1
images:
  craftcanvas:travel/background:
    file: travel/background.png
    width: 640
    height: 300
  craftcanvas:travel/close:
    file: travel/close.png
    width: 24
    height: 24
```

Set dimensions to the actual source PNG size. Screen component asset references use those exact IDs. YAML syntax, schema version, namespace/ID collisions, native manifest fields, file existence, PNG decoding/dimensions and the OGG signature are checked before writes. Other screen semantics, layout constraints, references and pack generation are checked by the normal configuration compiler during `/cc validate` or `/cc reload`. Bundle installation does not compile, apply or send a resource pack.

| Mode | Behavior |
| --- | --- |
| `INSTALL` | Install once; an existing ownership record makes later requests UNCHANGED, including newer versions. Never synchronize, add new files or recreate deleted screens on ordinary addon startup. |
| `UPDATE` | Explicit administrator-requested update. Add new files, replace unchanged owned files, and restore missing files listed in this request. A locally modified file rejects the whole request unless its current bytes already match the requested version. Files omitted from the new request are retained. |

Use `UPDATE` only from an administrator-approved workflow in the addon, not from its startup or player menu. The API is callable by trusted server plugins and does not receive a command sender or perform that permission check. There is no force-overwrite mode. Unknown preexisting files are rejected even if their bytes match. Another plugin cannot claim the bundle ID. Duplicate catalog IDs in another file also reject installation, so renaming an old YAML file may require the administrator to remove its previous copy first.

`CanvasBundleResult` contains `bundleId`, `status` (INSTALLED, UPDATED, UNCHANGED or REJECTED), `installedVersion`, `writtenFiles`, `diagnostics`, `successful` and `filesChanged`. `installedVersion` reports the recorded version, so a skipped automatic install may report an older version than requested. Diagnostics contain a path and message. A failed validation returns REJECTED without changing files. Programmer/lifecycle errors such as an asynchronous request or disabled owner throw immediately. Future continuations are not guaranteed to run on the main thread; schedule Bukkit operations appropriately and do not block the server thread waiting for the future.

Ownership records in `.data/addon-bundles/<id>.json` contain the owning plugin name and SHA-256 hashes. Keep them with server backups. The installer stages bytes, rechecks existing files, commits the ownership record last and rolls back ordinary write failures. File replacements use atomic moves where supported. This is not a filesystem-wide transaction across process crashes or concurrent manual edits. Do not edit files during installation; if rollback cannot safely restore a file, diagnostics identify retained recovery data under `.data/addon-bundles/staging/`. Its `recovery.json` maps numbered old/new files to their destinations. Recovery data from an interrupted installation blocks later requests for that bundle until an administrator reconciles it. A cleanup warning after a completed installation may appear alongside a successful result. Disabling or removing an addon leaves its installed files on disk; its callback registrations are cleaned separately.

After installing or explicitly updating, run `/cc validate` and `/cc reload`, or use the reload API below, to compile the files and build the shared resource pack. Existing Nexo export, standalone hosting and player pack-status checks remain responsible for delivery. Success from `installBundle` proves installation on disk, not a loaded screen, a successful pack build or client readiness.

The parent `.data/` directory is hidden; its `addon-bundles/` subdirectory uses a normal name and has the Windows hidden attribute cleared where supported. An existing `.data/.addon-bundles/` directory is renamed on the next bundle installation request, preserving ownership records and recovery data. If both names exist, the request is rejected until the directories are reconciled.

## Install, then compile

Run the installation request from `onEnable` inside `CanvasApi.whenReady(this).thenAccept { canvas -> ... }`, or from an administrator command on the global scheduler. Installation completion may run on a worker; `reload()` accepts that context:

```kotlin
canvas.installBundle(this, bundle).thenCompose { result ->
    if (result.successful) canvas.reload()
    else java.util.concurrent.CompletableFuture.failedFuture(
        IllegalStateException(result.diagnostics.joinToString { "${it.path}: ${it.message}" })
    )
}.thenAccept { result ->
    result.diagnostics.forEach { logger.warning("${it.stage}: ${it.message}") }
}
```

Use `CanvasBundleMode.UPDATE` only in your administrator-requested update path. Check `result.successful` before advertising the new screen as available. Loaded player resources remain a separate condition; `openAsync` returns QUEUED or PACK_UNAVAILABLE when appropriate.
