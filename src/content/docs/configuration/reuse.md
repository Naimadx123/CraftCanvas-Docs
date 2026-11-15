---
title: Themes and templates
description: Reuse a screen frame, button layout, click sequence or translated label.
---

You do not need to copy the whole console into each menu. Use screen inheritance for a shared layout, then add the other kinds of reuse when they help:

| I want to reuse... | Use |
| --- | --- |
| A background and physical controls | A parent screen with `extends` |
| Colors and other defaults | A theme |
| A button with a different label or target each time | A template |
| The same click sequence | A shared action list |
| Labels in several languages | Language entries |

All references use the `id` inside a file, not its filename. Local values override inherited ones. Maps merge by key; a list you supply replaces the inherited list completely.

## Inherit a reusable frame

Create `screens/travel.yml`:

```yaml
schema-version: 1
id: travel
extends: frame
components:
  heading:
    text: '<gold>TRAVEL'
```

The child keeps the inherited console and Exit button. Only `heading.text` is replaced. IDs refer to loaded definitions, so `frame` may live in a nested screen directory without changing `extends`.

If an inherited value fails validation, the error identifies both the affected screen and the declaration that supplied that value. For example, `eyes.yml [screen=curtain, component=-, inherited-from=eyes] $.cinematic.actions.covered[1].type` means the second `covered` action comes from `eyes`, even though validation failed while loading `curtain`. Fix that action in `eyes.yml`, or replace the `covered` list in the child. A local override is reported against the child's own file. At startup, only rejected screens are skipped; a failed reload keeps the previous configuration.

## Apply a theme

Create `themes/server.yml`:

```yaml
schema-version: 1
id: server
component-defaults:
  color: '#E8DFC8'
  shadow: false
screen-defaults:
  session:
    timeout-ticks: 2400
```

Set `theme: server` at the screen root. Component-specific colors override the theme. A theme may set `extends: other_theme` to inherit another theme; parent maps merge before local values.

## Reuse a button with parameters

The supplied `navigation_button` template declares `screen`, `label`, `x` and `y`. Add this to a screen's `components`:

```yaml
open_settings:
  template: navigation_button
  parameters:
    screen: settings
    label: Settings
    x: 112
    y: 128
```

To write your own template, create `templates/menu_link.yml`:

```yaml
schema-version: 1
id: menu_link
parameters:
  target: home
  label: Home
component:
  type: button
  position: {x: 112, y: 128}
  size: {width: 240, height: 32}
  padding: 8
  layer: 20
  text: '<white>{{label}}'
  asset: {provider: native, kind: image, id: 'craftcanvas:button'}
  actions:
    click: [{type: open, screen: '{{target}}'}]
```

Use `template: menu_link` with `parameters: {target: catalog, label: Browse}`. Parameters are expanded at reload, while `{variable}` placeholders are resolved during presentation. List overrides replace the inherited list, including an entire click sequence.

## Share an action list

Create `actions/return_home.yml`:

```yaml
schema-version: 1
id: return_home
actions:
  - {type: sound, id: minecraft:ui.button.click}
  - {type: open, screen: home}
```

Reference it from any button:

```yaml
actions:
  click: [{ref: return_home}]
```

Cycles between parent definitions, template expansion or shared actions reject reload and leave the previous valid configuration active.

## Translate labels

Create or edit `languages/en_US.yml`:

```yaml
schema-version: 1
locale: en_US
entries:
  menu-title: '<gold>SERVER MENU'
  saved: '<green>Your settings have been saved.'
```

Use `text: '{lang.menu-title}'` in a component or message. Add a language file with the corresponding locale and keys for another language. Missing locales fall back to `en_US`; missing keys display the key.


## Parameters and values

`{{label}}` is a template parameter, filled in when the configuration reloads. `{player.name}` and your variable placeholders are filled in for a player when the screen is shown. A template token used as the whole value keeps its type, so a numeric position stays a number.

Templates must declare each parameter they use. Parent and reference cycles are rejected during validation. The [YAML reference](/reference/yaml/#themes-templates-actions-and-languages) lists the fields for these files.
