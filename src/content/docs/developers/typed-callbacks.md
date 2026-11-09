---
title: "Typed callback parameters"
---

Use the schema overloads of `registerAction`, `registerAsyncAction` and `registerCondition` to replace manual casts from `Map<String, Any?>` with checked, typed arguments. The YAML forms stay `callback`, `async-callback` and callback conditions; their `data` maps are validated against the registered schema. Raw-map registrations remain available for data that needs an addon-owned format.

Java:

```java
CanvasParameter<String> destination = CanvasParameter.choice("destination", List.of("spawn", "market"));
CanvasParameter<Long> amount = CanvasParameter.integer("amount", 1, 64).optional(1L);
CanvasActionSchema schema = CanvasActionSchema.of(destination, amount);

canvas.registerAction(this, "travel:select", schema, (player, session, args) -> {
    String selected = args.get(destination);
    long count = args.get(amount);
    // Application logic using already checked values.
});
```

Kotlin uses the same keys with `args[amount]` and `args[destination]`. Keep the exact key returned by `.optional(...)`; it is a new immutable key, not a mutation of the original. Register globally after `CanvasApi.whenReady`, as with ordinary callbacks. Schema/handler handles retain the same ownership and disable cleanup.

```yaml
actions:
  click:
    - type: callback
      id: travel:select
      data: {destination: market, amount: 3}
```

| Factory | Typed result | Validation |
| --- | --- | --- |
| `string(name)`, `string(name, maxLength)`, `string(name, minLength, maxLength)` | String | UTF-16 length; defaults 0..4096 |
| `integer(name[, minimum[, maximum]])` | Long | Exact signed 64-bit integer, optionally bounded; rejects floating literals such as 1.0 |
| `number(name[, minimum[, maximum]])` | Double | Finite number, optionally bounded; integer literals are converted to Double |
| `bool(name)` | Boolean | Literal boolean; strings such as `'true'` are rejected |
| `choice(name, values)` | String | One of a copied, nonempty collection of allowed strings |

Factory parameters are required. Calling `.optional(defaultValue)` checks and stores a scalar default used only when the field is absent. An explicit null is invalid. There is no automatic string/number/boolean coercion, placeholder expansion or MiniMessage interpretation. Dynamic values should be read from the session or player inside the callback. `number` uses normal Double precision; use `integer` for exact large integers.

Schemas reject unknown fields even with `strict: false`. Names accept letters followed by letters, digits, underscores or hyphens, at most 64 characters. Duplicate names, invalid ranges and invalid defaults reject the schema before registration. `CanvasActionSchema.of(...)` copies its key list. Schemas, keys, defaults and parsed arguments are immutable and can be shared across Folia players. `args.asMap()` returns normalized values (including defaults) in an unmodifiable map. A key from another schema instance with the same name is not interchangeable unless it is the same key object.

Validation happens at three boundaries:

1. Typed registration validates references in currently loaded screens before replacing the old registration. Errors include file, screen, activating component and `data.<parameter>` path. A failed replacement leaves the previous handle and any pending async work intact.
2. `/cc validate` and reload preparation validate loaded screen definitions against available schemas, without calling addon handlers. Nested actions, action conditions, component/state conditions and cinematic action lists are included. Publication checks schemas again before invalidating sessions, covering registration changes during preparation. A rejected configuration retains the running snapshot and sessions.
3. Every invocation parses its data before calling addon code. Invalid async data fails through the normal async action error path; invalid conditions fail closed. API users can also call `schema.parse(data)` directly and inspect `CanvasParameterException.parameter`.

Addons become ready after CraftCanvas's initial configuration preparation. An unknown/unregistered schema cannot be checked during that first preparation; its registration checks the already loaded screens instead. If that registration fails, fix the YAML and reload it before registering again. Only loaded screen references are checked; unused templates/shared action definitions are checked against the schema when a screen uses them. Removing an addon removes its schema metadata; unavailable callbacks still fail closed at execution.

Schemas validate structure and scalar constraints, not authorization, balances, permissions or repository state. Recheck business rules inside your handler. The schema overload does not change the async timeout/cancellation contract or make purchase callbacks transactional. No files are migrated or rewritten to add defaults.

Compiled examples: [Java](https://github.com/Naimadx123/CraftCanvas/blob/master/examples/addon/java/example/CanvasTypedCallbacks.java), [Kotlin](https://github.com/Naimadx123/CraftCanvas/blob/master/examples/addon/kotlin/example/CanvasTypedCallbacksKotlin.kt) and [screen YAML](https://github.com/Naimadx123/CraftCanvas/blob/master/examples/typed_callback.yml). Register their three handlers, install the screen with its frame/assets and reload to use it. See [async callbacks](/developers/async-actions/) for nonblocking repository work.
