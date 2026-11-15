---
title: Variables and formatting
description: Remember a choice, save a player setting and insert values into text.
---

A variable holds a value your screen needs to remember. It might be the selected destination, a counter or a player's notification setting. Declare it under `variables` at the top of the screen, then use its name in components and actions.

## Remember a choice

```yaml
variables:
  destination:
    type: string
    default: spawn
```

Put this on a button to change the choice:

```yaml
actions:
  click:
    - type: set-variable
      name: destination
      value: market
```

Show the current choice on a text component with `text: '<white>Destination: {destination}'`. After the click, it shows `Destination: market`.

The default scope is `session`: each player has their own value while that screen is open. Starting a new session uses the declared default. Going back through screen navigation can restore the previous session values.

## Choose where a value belongs

| Scope | Use it for | How long it lasts |
| --- | --- | --- |
| `session` | A choice or counter in the current menu | The screen session; navigation can restore it |
| `player` | A preference used across that player's screens | Reconnects and restarts too, if marked persistent |
| `global` | One shared value for all players | Until the plugin stops; it is not saved |

Changing a global value does not automatically redraw every player's open screen. Shared player/global variables must use the same type wherever you declare the same name.

## Save a player preference

```yaml
variables:
  notifications:
    type: boolean
    scope: player
    default: true
    persistent: true
```

A toggle bound to `notifications` changes that preference. Keep `persistent-player-data: true` in `config.yml` to save it in the [player database](/configuration/storage/).

The name is yours to choose. A variable called `notifications` does not turn another plugin's notifications on or off; an action or addon must use its value for that behavior.

## Display a value

Use braces in text:

```yaml
text: '<gold>Hello, {player.name}!<newline><white>Destination: {destination}'
overflow: wrap
```

`{player.name}` is the player's Minecraft name. `{destination}` is your variable. `<newline>` starts a new line; the component needs enough height for both lines.

You can include a scope to be explicit: `{session.destination}`, `{player.notifications}` or `{global.announcement}`. Unqualified names such as `{destination}` work for declared variables too.

These names already have a meaning, so do not use them for your own variables:

| Placeholder | Value |
| --- | --- |
| `{player.name}` / `{player.uuid}` | Player name / UUID |
| `{session.selected}` | Selection set by the `select` action |
| `{session.tab}` / `{session.page}` | Current tab / zero-based page |
| `{session.screen}` | Current screen ID |
| `{lang.key}` | Entry from the player's language file |

Missing variable values render as empty text. A missing translation shows its key.

## Numbers, booleans and text

| Type | Example default | Use it for |
| --- | --- | --- |
| `string` | `spawn` | Text or option IDs |
| `integer` | `0` | Whole-number counters |
| `number` | `0.5` | Values that can have decimals |
| `boolean` | `true` | On/off choices |

Names start with a letter and use letters, digits or underscores. Declare a counter before incrementing it:

```yaml
variables:
  count: {type: integer, default: 0}
```

On a button:

```yaml
actions:
  click:
    - {type: increment-variable, name: count, value: 1}
    - {type: message, text: '<green>Count: {count}'}
```

`reset-variable` restores the declared default. When an action expects a typed value, `value: '{count}'` passes the number itself; `value: 'Count: {count}'` produces text.

## Formatting and other plugins

Write labels with MiniMessage colors and bold, such as `<gold><bold>Rewards`. Body text uses prepared fonts; see [screen fonts](/guides/fonts/) and [inline sprites](/guides/sprites/).

PlaceholderAPI `%...%` placeholders work when PlaceholderAPI and the required expansion are installed. Player input and values returned by placeholders are inserted as literal text; they are not parsed again as formatting or commands.

For translated labels, add an entry to `languages/en_US.yml` and use `{lang.key}`. See [translated labels](/configuration/reuse/#translate-labels).

The [variable reference](/reference/yaml/#variables-and-text) lists the exact defaults and limits. ItemStacks in crafting slots are separate from these scalar values; use [item slots](/guides/crafting/) for those.
