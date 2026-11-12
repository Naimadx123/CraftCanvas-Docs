---
title: "Actions"
---

Action lists execute sequentially on the player owner thread (Paper main thread or Folia entity region). Console commands switch to the global scheduler, then continue on the player owner after completion if the session is still current. Every entry declares `type`. An optional `condition` guards each action except branch, which uses `when`; guard a branch with a surrounding sequence. Shared actions expand through `{ref: id}` and cannot accept client arguments.

| Type | Fields besides type/condition | Defaults and behavior |
| --- | --- | --- |
| open | `screen` string | Open a known permitted screen; cancel the old session's work |
| close | none | Close/invalidate active session |
| back | none | Restore previous navigation frame, or close if none |
| refresh | none | Mark the session dirty |
| select | `value` string | Set persistent session selection; emit selection event |
| tab | `value` string | Set tab and reset page to zero |
| page | `value` | next, previous, or integer `0..10000`; clamps navigation |
| set-variable | `name` string, `value` typed scalar or exact variable placeholder | Validate declared scope/type |
| increment-variable | `name` string, numeric `value` | Finite numeric addition; integral amount for integer variables; overflow fails |
| reset-variable | `name` string | Restore declared default |
| random-variable | `name` session string variable, `values` string list | Uniform server-side choice from 2..128 distinct nonempty literals, at most 128 characters each; no client-selected outcome |
| animate | `variable`, `frames`; optional `cycles`, `interval-ticks`, `slowdown-ticks`, `stop-at` | Finite playback: client texture atlas with `shaders.animations` enabled, book resends otherwise; see [animations](/guides/animations/) |
| scroll | `target` scroll component ID, signed integer `rows` | -128..128 except zero; clamp a player's row offset; no wheel events |
| animate-component | `target` component ID | Replay its configured movement/box-scale animation; finite book updates with or without shaders |
| notify | `target` notification component ID, `text`; optional `duration-ticks` | Replace that player's message; duration 1..1200, default 60; text up to 2048 characters |
| dismiss-notification | `target` notification component ID | Remove its message and motion early |
| message | `text` string | Chat message |
| actionbar | `text` string | Native actionbar |
| title | `text` string, optional `subtitle` string | Subtitle defaults empty; native default timing |
| sound | exactly one of `id` string / `asset` sound reference; optional `volume`, `pitch` | Both numeric values default to 1; nonnegative finite |
| player-command | `command` string | Dispatch as the player |
| console-command | `command` string | Dispatch as console; administrator-authored only |
| give-item | `asset` item reference, `amount` integer | `1..2304`; cloned stacks; reject and restore storage on insufficient space |
| take-item | `asset` item reference, `amount` integer | `1..2304`; require sufficient matching storage inventory first |
| exchange-items | required `inputs` and `outputs` item-entry lists | Each list has 1..64 entries with item `asset` and optional integer `amount` (default 1, range 1..2304); commit only when all costs and outputs fit |
| workbench-clear | none | Cancel reserved slots and Hand; requires a workbench screen |
| workbench-craft | none | Craft one permitted matching recipe; requires an empty Hand |
| charge | `amount` positive finite number | Single Vault withdrawal; no implied reward transaction |
| refund | `amount` positive finite number | Single Vault deposit |
| purchase | `amount` positive finite number, `actions` list | Immediate guarded reward transaction, described below |
| confirm | optional `title`, `text`; required `on-confirm` list | Signed-book modal. Defaults Confirm/Continue?; cancel returns to source |
| input | `name` declared string variable; optional `max-length`, `prompt`, `mode` | Prefilled native text dialog by default; mode dialog/chat; maximum 1..256 UTF-16 code units (default 128); cancel leaves value unchanged |
| local-page | `value` variant ID | Sole direct click action; switches precompiled native pages locally |
| sequence | `actions` list | Nested sequential execution |
| delay | `ticks` integer, `actions` list | `1..72000`; one session-owned scheduled task |
| branch | `when` condition, `actions`, optional `otherwise` list | Execute selected branch; otherwise defaults empty |
| callback | `id` string, optional `data` map | Execute an API-registered action; data defaults empty |
| async-callback | `id`, optional `data`, optional `timeout-ticks` | Await an addon stage; timeout defaults to 200, range 1..1200 ticks. See [async callbacks](/developers/async-actions/) |

Completion marks the active session dirty. Immediate sequences are processed before rendering again. Loading state is observable while delayed work remains active. Jobs are always cancelled on session end; detached after-close YAML jobs are not offered. Close, navigation, reload and disable invalidate the old session and stop remaining actions.


See [animations](/guides/animations/) for the animate and random-variable actions.

Cinematic screens run lifecycle lists under `cinematic.actions.start`, `covered`, `uncovering` and `complete`. Use `uncovering` for actions while a transition opens, including session-owned delays. Start and covered reject delays and async callbacks; title, input, confirm and animate are unavailable in every cinematic phase. See [fullscreen cinematics](/guides/animations/#fullscreen-cinematics) for timing and examples.

## Authorization and purchases

Book clients activate an opaque key registered for their current owner/session/screen/component/render revision. No client-supplied command, cost, permission, reward or action body is trusted. A token is consumed once; foreign, old-screen, old-revision and repeated tokens are rejected. Availability and execution checks run before dispatch, and again after input submission. `cooldown-ticks` is a short per-component/session debounce. The optional [button cooldown](/guides/components/#button-cooldowns) uses player deadlines, shared keys and persistent storage when enabled. A busy lock still guards the action sequence. Confirmation replaces the prior token set and checks authorization again.

`purchase` permits immediate give-item, callback, set-variable, select, message, notify and sound rewards. It excludes commands, delays, async callbacks and nested transactions. The engine snapshots player storage, declared variables and selection, withdraws once, executes rewards, and restores those snapshots if a reward fails. It attempts a refund; a failed refund appends an explicit reconciliation entry to `.data/compensation.log` and logs a severe error.

External callbacks may change other plugins, the world or send messages before failing. Those effects cannot be rolled back automatically. Vault has no cross-plugin atomic transaction or general crash-safe idempotency API. The implementation prevents repeated execution within a live session and compensates ordinary partial failures; it does not promise atomicity across process crashes or other plugins. Review the compensation log manually with the economy provider. Use an application-owned idempotency key inside external callbacks for durable purchases.

## Navigation and selection

Place an action list under a button's `actions.click`. This selects an option and opens another screen:

```yaml
actions:
  click:
    - {type: select, value: market}
    - {type: open, screen: catalog}
```

A navigation button can use `[{type: back}]` to restore the previous screen, or `[{type: close}]` to end the session. Selecting with `select` is independent of variables: use `set-variable` when your screen stores the choice in a declared variable.

For tab buttons, use `[{type: tab, value: equipment}]` and add `data: {tab: equipment}` to the content. For pagination, use `[{type: page, value: next}]`, `previous` or a zero-based integer. The [catalog example](https://github.com/Naimadx123/CraftCanvas/blob/master/src/main/resources/screens/catalog.yml) includes page filters and disabled navigation.

Local variants require `book.update: local-pages` and declared `book.variants`. A button's entire list must be `[{type: local-page, value: variant_id}]`. See the [complete variants screen](https://github.com/Naimadx123/CraftCanvas/blob/master/src/main/resources/screens/variants.yml). Local navigation does not send a server action; confirm with a separate callback or button.

## Change variables

Declare `count` as an integer, `saved_count` as an integer and `display_name` as a string in the screen's `variables` map. These are alternative action lists:

```yaml
actions:
  click:
    - {type: increment-variable, name: count, value: 1}
    - {type: set-variable, name: saved_count, value: '{count}'}
    - {type: message, text: '<green>Count: {count}'}
```

Use `[{type: reset-variable, name: count}]` to restore its declared default. A completed list already marks the screen dirty. `[{type: refresh}]` is useful after a callback changes an external value used by placeholders. The [variables guide](/configuration/variables/) explains scopes and literal substitutions.

## Ask for text

```yaml
actions:
  click:
    - type: input
      name: display_name
      max-length: 32
      prompt: '<gold>Choose a display name'
    - {type: message, text: '<green>Saved: {display_name}'}
```

This opens a separate native text dialog and resumes after Save. Cancel skips the remaining list. Set `mode: chat` to collect a private chat reply instead. For an always-visible editor button, use [text-input](/reference/yaml/#native-text-editor).

## Feedback and commands

```yaml
actions:
  click:
    - {type: sound, id: minecraft:ui.button.click, volume: 0.7, pitch: 1.0}
    - {type: actionbar, text: '<green>Destination selected'}
    - {type: title, text: '<gold>Ready', subtitle: '<white>Have a good trip'}
    - {type: player-command, command: 'spawn'}
```

`spawn` in this example must be supplied by another installed plugin. To dispatch as console, use `console-command`, for example `command: 'say Welcome {player.name}'`. The first command token must be a literal. Substituted arguments must match `[a-zA-Z0-9_.:+/-]+`; newlines and variable-provided selectors are rejected. Static administrator command text can include selectors.

## Confirm an economy purchase

This button requires Vault and an economy plugin. Put a balance condition in `conditions.enabled` if you also want to gray it out for players without enough coins; the withdrawal still checks again at activation.

```yaml
actions:
  click:
    - type: confirm
      title: '<gold>Buy a bow'
      text: '<white>Spend 25 coins?'
      on-confirm:
        - type: purchase
          amount: 25
          actions:
            - type: give-item
              asset: {provider: vanilla, kind: item, id: BOW}
              amount: 1
            - {type: message, text: '<green>Purchase complete.'}
```

For item trades or crafting, use `exchange-items` to check all costs and output capacity before changing storage. Separate `take-item` and `give-item` actions do not form a transaction. See the [inventory workbench](/guides/crafting/) for a complete example and exchange limits. Individual `charge` and `refund` actions accept `amount: 25` but do not imply a transaction with later actions. Prefer `purchase` for immediate paid rewards and review the rollback boundaries above.

## Delay and branch

```yaml
actions:
  click:
    - type: sequence
      actions:
        - {type: message, text: '<gray>Preparing...'}
        - type: delay
          ticks: 20
          actions:
            - type: branch
              when: {permission: server.vip}
              actions: [{type: message, text: '<gold>VIP destination available.'}]
              otherwise: [{type: message, text: '<white>Standard destinations available.'}]
```

Close, navigation, reload and disconnect cancel delayed work. Use an action's `condition` to guard a single action. A `branch` uses `when` instead. [Animation examples](/guides/animations/) combine `random-variable` and `animate`; [callback examples](/developers/callbacks/) connect server plugin logic.
