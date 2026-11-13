---
title: "Typed definition builders"
---

Use `CanvasComponentDefinition`, `CanvasActions` and `CanvasConditions` to create code-defined screens with IDE completion for schema fields and enum values. These produce the same immutable declarations as the map-based API. They do not register handlers, run actions, access players or publish screens on their own. Register the resulting screen and call a full reload as described in [code-defined screens](/developers/code-screens/).

## Components

```kotlin
val claim = CanvasComponentDefinition.button("claim")
    .position(112, 128).size(160, 28).layer(20).padding(4).centered()
    .text("Claim").tooltip("Claim your reward")
    .asset(CanvasAssets.nativeImage("craftcanvas:button"))
    .enabledWhen(CanvasConditions.variableEquals("claimed", false))
    .cooldown(60, "claim_reward")
    .state(CanvasComponentAppearance.DISABLED, CanvasAppearanceDefinition.builder().text("Claimed"))
    .onClick(CanvasActions.setVariable("claimed", true), CanvasActions.message("Claimed!"))

val screen = CanvasScreenDefinition.builder("example:menu")
    .extendsScreen("frame")
    .variable("claimed", CanvasVariableType.BOOLEAN, false)
    .component(claim)
    .build()
```

In Java the same chain uses static methods, for example `CanvasComponentDefinition.button("claim")`. `CanvasScreenDefinition.Builder.component` accepts either a component builder or its built `CanvasComponentDefinition`. Components and nested values are detached when added/built; changing an earlier builder does not change the built screen. Builders themselves are mutable and should remain confined to their construction thread.

| Methods | Purpose |
| --- | --- |
| `button`, `text`, `image`, `group`, `scroll`, `notification` | Common component factories |
| `toggle`, `choice`, `stepper`, `slider`, `textInput`, `search` | Controls with a bound variable name |
| `builder(id, CanvasComponentType)` | Every supported component type, including panels, pagination, grids and item slots |
| `position`, `offset`, `anchor`, `size`, `layer`, `margin`, `padding` | Integer GUI geometry |
| `text`, `color`, `font`, `shadow`, `tooltip`, `tooltipItem`, `tooltipItemSlot` | Configured content and tooltip resources |
| `align`, `verticalAlign`, `centered`, `overflow` | Layout using typed enums |
| `asset`, `assetSelect`, `autoplay`, `animation` | Graphics, variable-bound graphics and declared motion |
| `hover`, `hoverOverlay`, `clickSound`, `deniedSound` | Hover and interaction presentation |
| `visibleWhen`, `enabledWhen`, `executeWhen` | Typed condition declarations |
| `onClick`, `cooldown`, `debounce` | Typed action lists, player cooldown and short click debounce |
| `state(appearance, definition/builder)` | Configured normal/selected/disabled/locked/loading appearance |
| `child`, `layout` | Nested components with manual/row/column/grid layout |
| `bindVariable`, `options`, `range`, `sliderAssets`, `input`, `placeholder` | Control-specific settings |
| `scrollRows`, `searchVariable`, `addonList`, `searchText` | Scroll viewport, addon row slots and filtering |
| `progress`, `page`, `tab`, `itemSlot` | Meter values, page/tab visibility and workbench slot source |

`CanvasAppearanceDefinition.builder()` provides `text`, `color`, `asset`, `overlay`, `tooltip` and `whenCondition`. `CanvasMotionDefinition` takes duration/interval ticks, starting offsets/scale, `CanvasEasing` and `onOpen`; defaults are 20 ticks, interval 2, zero offsets, scale 1, ease-out and on-open true. Native renderer constraints still apply to each type. Controls must bind a compatible declared variable; choice options must include its default. Addon scrolls still need one fixed child slot per visible row. Inventory slots use indices 0..35; grid indices must fit the declared workbench (up to 5 by 5). Cursor/result slots have no index.

## Actions and conditions

Actions are immutable values returned by typed factories:

```kotlin
val allowed = CanvasConditions.permission("example.claim")
    .and(CanvasConditions.variableEquals("claimed", false))
val reward = CanvasActions.sequence(
    CanvasActions.giveItem(CanvasAssets.vanillaItem(Material.DIAMOND)),
    CanvasActions.setVariable("claimed", true),
    CanvasActions.message("Reward claimed!")
).onlyIf(allowed)
```

`CanvasActions` covers navigation (`open`, `close`, `back`, `refresh`, `select`, `tab`, `page`, `nextPage`, `previousPage`, `localPage`), variables (`setVariable`, `incrementVariable`, `resetVariable`, `randomVariable`), frame/component animations, scroll, input, notifications, messages/titles/sounds, commands, item exchanges, workbench actions, economy, callbacks and action composition. `ref(id)` uses a shared action. Commands omit a leading slash.

`sequence` and `delay` take action varargs. `branch(condition, actions[, otherwise])` uses action lists. `confirm(title, text, actions...)` and `purchase(amount, rewards...)` use varargs; purchases retain their existing restrictions on immediate reward actions. `exchangeItems` takes input/output lists of `CanvasItemAmount`. `animate(variable, frames[, cycles, intervalTicks, slowdownTicks, stopAt])` declares a frame timeline. Callback data remains a copied literal map with addon-defined keys; handlers are registered separately through `registerAction`, `registerAsyncAction` or `registerCondition`.

`onlyIf(condition)` guards execution. For a shared reference or branch it creates an enclosing conditional sequence, keeping the referenced action or branch condition intact. This does not override a component's execution conditions.

`CanvasConditions` supports permission, world, integration, workbench, variable equality/type, comparison, session property, item/balance/asset, placeholder and callback checks. Compose with `all`, `any`, `not`, or the immutable `.and`, `.or`, `.not` methods. `CanvasComparison` provides EQ, NE, GT, GE, LT, LE and CONTAINS. `CanvasSessionField` provides SELECTED, PAGE, TAB, BUSY and SCREEN. Workbench checks use READY or HOLDING. These are declarations evaluated later on the player's owner.

## Assets, validation and schema access

`CanvasAssets.nativeImage`, `vanillaItem(Material)`, `vanillaSprite`, `item(provider, id)` and `sound` create references. `nexoGlyph(id, metrics)` and `itemsAdderGlyph(id, font, metrics, permission)` follow those providers' glyph contracts. Generic/custom providers can use `CanvasAssetReference` or the explicit `glyph` factory. Register native files with the screen builder's `image`/`sound` methods before using their IDs; existing manifest assets can also be referenced.

Typed methods reject invalid local ranges, incompatible method/type combinations, invalid variable defaults and malformed component IDs during construction. References, variable compatibility, full screen layout, resource availability and client renderer constraints still require a successful reload. Constructor success does not mean the screen is published or a client has loaded its pack.

The map API remains supported. `component(id, fields)`, component `.field(name, value)` and `CanvasActionDefinition.fromFields` / `CanvasConditionDefinition.fromFields` provide access to the complete schema and future extensions. These raw fields receive their normal validation during reload. All immutable definition `fields` maps are readable but cannot be modified. Nested typed helpers take precedence over matching raw keys when building a component. Repeated scalar helper calls use their last value.

For current player state and frequent presentation edits, use [uiState, updateComponent and patchComponent](/developers/component-state/). Definition builders change declarations and require registration/reload; patches change only one session and do not rebuild the resource pack.
