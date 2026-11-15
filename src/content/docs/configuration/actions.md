---
title: Actions
description: Make buttons open menus, run commands, change values or ask for confirmation.
---

Actions are what a button does when clicked. Put them under `actions.click`. They run from top to bottom:

```yaml
actions:
  click:
    - type: sound
      id: minecraft:ui.button.click
    - type: message
      text: '<green>Hello, {player.name}!'
```

This plays the sound, then sends the message. Each entry starts with `type`; the fields below it depend on the action. Both expanded YAML above and the shorter `{type: message, text: 'Hello'}` form work.

The examples below replace a button's `actions` block. For every supported action and field, use the [action reference](/reference/yaml/#action-fields).

## Navigation and selection

Place an action list under a button's `actions.click`. This selects an option and opens another screen:

```yaml
actions:
  click:
    - {type: select, value: market}
    - {type: open, screen: catalog}
```

A navigation button can use `[{type: back}]` to restore the previous screen, or `[{type: close}]` to end the session. Selecting with `select` is independent of variables: use `set-variable` when your screen stores the choice in a declared variable.

For tab buttons, use `[{type: tab, value: equipment}]` and add `data: {tab: equipment}` to the content. For pagination, use `[{type: page, value: next}]`, `previous` or a zero-based integer. The [catalog example](/examples/screens/catalog.yml) includes page filters and disabled navigation.

Local variants require `book.update: local-pages` and declared `book.variants`. A button's entire list must be `[{type: local-page, value: variant_id}]`. See the [complete variants screen](/examples/screens/variants.yml). Local navigation does not send a server action; confirm with a separate callback or button.

## Change variables

Declare `count` as an integer, `saved_count` as an integer and `display_name` as a string in the screen's `variables` map. Use this on a button:

```yaml
actions:
  click:
    - {type: increment-variable, name: count, value: 1}
    - {type: set-variable, name: saved_count, value: '{count}'}
    - {type: message, text: '<green>Count: {count}'}
```

Use `[{type: reset-variable, name: count}]` to restore its declared default. A completed list already updates the screen. `[{type: refresh}]` is useful after a callback changes an external value used by placeholders. The [variables guide](/configuration/variables/) explains scopes and literal substitutions.

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

For item trades or crafting, use `exchange-items` to check all costs and output capacity before changing storage. Separate `take-item` and `give-item` actions do not form a transaction. See the [inventory workbench](/guides/crafting/) for a complete example and exchange limits. Individual `charge` and `refund` actions accept `amount: 25` but do not imply a transaction with later actions. Use `purchase` for immediate paid rewards; see the failure behavior below.

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

## Reuse the same actions

To share a sequence between buttons, put it in an `actions/*.yml` file and reference its ID:

```yaml
actions:
  click:
    - ref: return_home
```

See [shared action lists](/configuration/reuse/#share-an-action-list) for the file itself. The wheel example uses one shared sequence for SPIN and the console A button.

## Authorization and purchases

CraftCanvas checks the current player, screen and permissions before running a click. Old or repeated click links cannot replay the action. Conditions are checked again when an input or confirmation returns.

`purchase` checks and withdraws the price once. If an immediate reward fails, CraftCanvas restores the inventory, variables and selection it saved and attempts to refund the money. A failed refund is logged in `.data/compensation.log` for an administrator to resolve.

An addon callback can change the world or another plugin. Those changes cannot be undone automatically, and a process crash can interrupt a transaction. Use an addon that tracks completed purchases if you need durable, exactly-once rewards. The [execution reference](/reference/yaml/#action-execution) gives the allowed purchase actions and full failure behavior.

## Timing and closing the menu

The menu updates after the action list completes. Delayed work belongs to the open screen: closing, navigating away, disconnecting or reloading cancels it. Use the Exit button or `/cc close` to close explicitly; Escape does not reliably tell the server that the book was closed.

Cinematic effects have their own lifecycle actions. See [cinematics](/guides/animations/#cinematics-inside-an-open-book) for their timing and supported actions.
