---
title: "Variables and formatting"
---

| Declaration field | Type | Default | Constraints |
| --- | --- | --- | --- |
| type | enum | string | string, integer, number, boolean |
| scope | enum | session | session, player, global |
| default | typed scalar | empty string, 0, 0.0, false | String at most 4096 UTF-16 units; finite numbers; integral integers |
| persistent | boolean | false | Only player scope; main persistence must also be enabled |

Variable names begin with a letter and contain letters, digits or underscores. Shared player/global declarations must agree on type. Player variables span that player's screens; global variables span players and remain in memory. Persistent player data uses asynchronous SQL storage configured in `storage.yml` (SQLite by default). See [storage configuration](/configuration/storage/). Navigation restores compatible session variables, selected element, tab and page.

Text placeholders include `{session.name}`, `{player.name}`, `{global.name}`, unqualified declared variables and `{lang.key}`. Reserved placeholders are `{player.name}`, `{player.uuid}`, `{session.selected}`, `{session.tab}`, `{session.page}`, `{session.screen}`; avoid declaring conflicting names. Missing variables become empty text; missing translations use their key. PlaceholderAPI `%...%` values and player input are inserted literally, without MiniMessage reparsing or action authority.

Administrator body text supports MiniMessage colors, bold and registered [sprite tags](/guides/sprites/). Inline sprites occupy 8 GUI units; graphical vanilla sprite components support larger sizes. Tooltip/message formatting can use the supported MiniMessage decorations and native sprite objects. The screen `font` defaults to `minecraft:default`; MiniMessage `<font:namespace:name>` tags override fragments. See [screen fonts](/guides/fonts/) for resources, measured advances and glyph/provider limits. External visual glyphs use their own explicit font and metrics.

Board items and Hand are session-owned ItemStacks, separate from scalar variables; use [item-slot bindings and workbench actions](/guides/crafting/) to display the full inventory and transfer items.

## Save a player preference

Add to a screen's root `variables` map:

```yaml
variables:
  notifications: {type: boolean, scope: player, default: true, persistent: true}
  visit_count: {type: integer, scope: session, default: 0}
  announcement: {type: string, scope: global, default: Welcome}
```

A [toggle](/reference/yaml/#component-types) bound to `notifications` changes the player's stored preference. Player persistence also requires `persistent-player-data: true` in `config.yml`. Your addon can read that preference to control its own notifications; declaring it does not change other plugins automatically.

`visit_count` belongs to this session. `announcement` is shared in memory across players; setting it does not automatically redraw their screens. Persistent global storage is not provided.

## Display and format a value

Put this on a text component:

```yaml
text: '<gold>Welcome, {player.name}!<newline><gray>Visits: {visit_count}'
overflow: wrap
```

Body text supports prepared bitmap fonts, colors and bold. `{player.name}` is a reserved player placeholder. A player variable named `display_name` is addressed as `{player.display_name}` or `{display_name}`.

For translated text, put a key under `entries` in `languages/en_US.yml`, then use `text: '{lang.saved}'`. PlaceholderAPI `%...%` works when its plugin and expansion are ready. Player input and placeholder results remain literal, so entered MiniMessage tags do not gain formatting or actions.

## Connect a variable to an action

```yaml
actions:
  click:
    - {type: increment-variable, name: visit_count, value: 1}
    - {type: message, text: '<green>Visits: {visit_count}'}
```

Exact variable placeholders preserve their scalar value in actions that accept typed values; surrounding text turns them into a string. Keep names, scopes and types stable when upgrading saved player preferences.
