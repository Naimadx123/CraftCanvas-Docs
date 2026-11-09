---
title: "Optional integrations"
---

No optional plugin is required. Each resource integration has a separate adapter instantiated only after its plugin is enabled. The adapters use named public API methods through a small reflection bridge, avoiding linked optional classes at startup when dependencies are absent. They do not use NMS or read undocumented glyph internals.

The adapters target the stable public contracts below. Use Java 21+ on Paper 1.21.11 or Java 25+ on Paper 26.x, and install a dependency version that supports your target Minecraft server. CraftCanvas does not make an older dependency compatible with newer Minecraft versions. Use the target API contracts below when selecting dependency versions.

| Provider | Target version | Public contract used | Readiness/reload |
| --- | --- | --- | --- |
| Nexo | 1.29.1 | NexoItems.itemFromId → ItemBuilder.build; NexoPlugin.fontManager → glyphFromID; Glyph.getComponent, hasPermission, height, ascent | NexoItemsLoadedEvent; initial item registry probe |
| CraftEngine | 26.9.1 | Public `bukkit.api.CraftEngineItems.byId(String)` → buildBukkitItem(Player) | CraftEngineReloadEvent; initial loadedItems probe |
| ItemsAdder | 4.0.17 | CustomStack.getInstance/getItemStack/getPermission; FontImageWrapper.exists/getString/getWidth/getHeight | ItemsAdderLoadDataEvent and ItemsAdder.areItemsLoaded |
| PlaceholderAPI | 2.12.3 | setPlaceholders(OfflinePlayer, String) | Enabled plugin at startup; output inserted literally |
| Vault economy | Vault API 1.7.1 contract | Bukkit service registration, balance/withdraw/deposit for OfflinePlayer | Requires an actual registered economy provider |

Nexo and CraftEngine resources are not assumed ready solely because onEnable ran. Resource events schedule adapter invalidation; tickets are invalidated globally and book sessions are closed on their owning player regions. ItemsAdder checks its public loaded status. Graphic/item values are resolved through public APIs during presentation; no filesystem parsing or pack generation occurs per frame. Restart after changing dependency availability.

## Nexo

Automatic pack export is enabled by default when Nexo is enabled. CraftCanvas automatically replaces its reserved `pack/external_packs/craftcanvas.zip`, checks conflicts with other resources and requests regeneration through `packGeneratorOrNull`, `getPackGenFuture` and `regeneratePack`. It listens to NexoPrePackGenerateEvent, NexoPackFinishedEvent and NexoPackUploadEvent; uploaded `packServer().packInfo()` supplies the merged UUID/hash. The adapter targets Nexo 1.29.1. Nexo generation, hosting and sending retain Nexo's settings. See [PACK.md](/resources/resource-pack/) for replacement behavior and opt-out configuration.

Pre-join sending is supported by passively recording resource-pack acknowledgements during Paper's configuration phase. No second CraftCanvas pack request is sent. The observer selects the Paper 1.21.11 or 26.x connection-field mapping to obtain the connection channel; it is separate from Nexo's public API adapter. Game-phase reloads continue to use the standard Bukkit status event. A loaded unrelated pack is never accepted as the Nexo combined pack.

Items use their official builder and are cloned before modification. Glyphs retain the API component and its font, color, namespace and provided metadata. Nexo's own glyph permission check runs first. Explicit width/advance metrics are needed where the API does not supply them. Height and ascent come from the public glyph object. ShaderAnimatedGlyph is not admitted by the default backend.

Inventory/crafting slots use [automatic item models](/guides/item-icons/). The Nexo 1.29.1 post-generation adapter reads the combined pack through Creative 1.15.3's public serializer and adds prepared flat/cuboid model previews before upload. Item model and CustomModelData select the preview without per-item YAML. The import covers supported item definitions from every namespace included in Nexo's combined pack, including resources imported from other providers. Unsupported special renderers and predicates have explicit diagnostics. A Nexo item ID and an item model key remain distinct identifiers. No item metadata is changed by preview generation.

If Nexo loads a pack but CraftCanvas still queues screens, run `/cc info` and check the server log for integration errors. CraftCanvas also needs a successful combined-pack build containing its generated resources; accepting another or an earlier Nexo pack does not satisfy this check. After correcting a generation error, rebuild the Nexo pack and accept the resulting pack. Legacy overlay ranges such as `formats: [34, 41]` are supported and do not require manual changes to `pack.mcmeta`.

```yaml
asset:
  provider: nexo
  kind: glyph
  id: class_archer
  metrics: {width: 16, height: 16, ascent: 12, advance: 17}
```

Use this in a book screen with matching fixed geometry and profile. Book-glyph cannot reposition or rescale a provider font: y=7-ascent, exact width/height and advance are required; changing public metadata refuses rendering or takes an explicit fallback. Font/character overrides are rejected on Nexo glyph references. Missing IDs, readiness or permission failure use an explicit compatible fallback or refuse opening.

The shipped `provider_nexo` example uses a Nexo item with a vanilla bow fallback, so it remains runnable without Nexo. Replace its example ID with one present in your installation.

[Nexo API guide](https://docs.nexomc.com/community-guides/api), [Javadoc entry point](https://jd.nexomc.com/), [stable Maven repository](https://repo.nexomc.com/releases/com/nexomc/nexo/1.29.1/).

## CraftEngine

The item path uses the stable bukkit.api facade. Player-aware builds preserve context, and the result is cloned. Its returned builder lives outside the stable API package, so that final build method is signature-sensitive to the targeted 26.9.1 version and isolated in this adapter. No manager or internal glyph data is accessed. Different API signatures require an updated adapter.

The targeted public API does not provide a general glyph-ID resolver with every required metric. Explicit glyph linkage is therefore required:

```yaml
asset:
  provider: craftengine
  kind: glyph
  id: classes:archer_label
  font: classes:labels
  character: "\uE200"
  metrics: {width: 16, height: 16, ascent: 12, advance: 17}
  permission: classes.archer
```

Here the ID is an administrator-declared linkage label. CraftCanvas can check plugin readiness and the declared permission, but cannot prove that this character exists in the client pack through CraftEngine's public item API. A client resource test is required. Do not treat this linkage as an API-confirmed glyph registry entry. Use a supplied native PNG when server-verifiable graphic generation is needed.

The shipped `provider_craftengine` example uses the public item API with a vanilla blaze-rod fallback. [API stability boundaries](https://xiao-momi.github.io/craft-engine-wiki/api/), [public source facade](https://github.com/Xiao-MoMi/craft-engine/tree/main/bukkit/src/main/java/net/momirealms/craftengine/bukkit/api), [repository](https://github.com/Xiao-MoMi/craft-engine).

## ItemsAdder

Items use CustomStack after loaded-data readiness and respect the API's item permission. Font images use the public FontImageWrapper; its string is parsed as legacy formatting, then uses the explicitly declared font. Width/height are obtained from the wrapper; ascent/advance and the intended glyph permission are supplied explicitly because the targeted public wrapper does not expose all attributes.

```yaml
asset:
  provider: itemsadder
  kind: glyph
  id: classes:archer
  font: minecraft:default
  permission: classes.archer
  metrics: {width: 16, height: 16, ascent: 12, advance: 17}
```

Configure the permission that applies to this image in your actual ItemsAdder content. No internalGetUnicode method is called. Do not substitute an item ID for a font-image ID. The adapter targets 4.0.17; use a release compatible with your server.

The shipped `provider_itemsadder` example uses an item with a vanilla iron-sword fallback. [Java API](https://wiki.itemsadder.com/developers/java-api/), [usage examples](https://wiki.itemsadder.com/developers/java-api/examples/), [font-image API guidance](https://wiki.itemsadder.com/developers/java-api/huds-guis-images-and-more/), [API repository](https://github.com/LoneDev6/API-ItemsAdder).

## Resource families

| Provider | Supported kinds |
| --- | --- |
| native | image, glyph, static sprite, generated font, OGG sound |
| vanilla | item, known native font, registered sound |
| nexo | item, glyph |
| craftengine | item, explicitly linked glyph |
| itemsadder | item, font-image glyph |
| custom API provider | Its implemented kinds; visuals need native PNGs or explicit single-character glyph metrics |

Unsupported provider/kind combinations fail validation. External sounds/fonts can be referenced by their explicit namespaced key in supported text/sound settings, using namespaced keys. Check merged pack namespaces for conflicts. Automatic Nexo handling selects Nexo as the responsible sender. Otherwise `pack.sender` selects manual routing; CraftEngine/ItemsAdder adapters do not merge or send separate packs.

`providers` demonstrates Nexo/CraftEngine/ItemsAdder item tooltips on independent native PNG buttons, with explicit vanilla fallbacks. `provider_nexo`, `provider_craftengine` and `provider_itemsadder` preserve the existing item examples. Their example IDs must be replaced with IDs actually installed on your server. Automatic inventory-slot previews use the separate combined-pack model importer described above. Baseline Nexo glyph adapters reject ShaderAnimatedGlyph. External composite/multi-character font images cannot be placed as a single book glyph; provide a PNG or one explicitly mapped character.

## Folia requirements

Install resource/economy/placeholder plugins that independently support your Folia version. The Folia loader can reject dependencies without their own support declaration. CraftCanvas resolves player resources and placeholders on the owning player region. Provider invalidation and Nexo pack generation coordination run globally and exclude active CraftCanvas rendering. Registered addon callbacks and providers can be invoked concurrently for different players; they must protect shared state and avoid accessing other regions. Do not hot-load plugins as a substitute for a restart.
