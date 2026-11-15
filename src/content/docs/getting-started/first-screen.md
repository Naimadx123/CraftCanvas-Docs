---
title: Your first screen
description: Make a working menu with the console and button graphics already included in CraftCanvas.
---

This example makes a console app with one button. Clicking it sends the player a message. It uses the artwork that comes with CraftCanvas, so you can try it before making any PNGs.

First [install CraftCanvas and set up pack delivery](/start/installation/). Check that `/cc open home` opens the example launcher after you accept the pack.

## 1. Create the file

Create `plugins/CraftCanvas/screens/welcome.yml` and paste this complete screen:

```yaml title="screens/welcome.yml"
schema-version: 1
id: welcome
extends: frame
title: 'Welcome'
components:
  heading:
    text: '<aqua><bold>WELCOME'
  description:
    type: text
    position: {x: 112, y: 96}
    size: {width: 280, height: 24}
    layer: 20
    text: '<white>Your first CraftCanvas menu.'
    overflow: wrap
  hello:
    type: button
    position: {x: 112, y: 140}
    size: {width: 280, height: 28}
    layer: 20
    align: center
    vertical-align: center
    asset: {provider: native, kind: image, id: 'craftcanvas:button'}
    text: '<gold>Say hello'
    tooltip: ['<gray>Click to send a message.']
    actions:
      click:
        - type: message
          text: '<green>Hello, {player.name}!'
```

Use spaces for indentation, not tabs. Keep the supplied `frame` screen and its assets installed: `extends: frame` reuses its console, heading and physical controls.

## 2. Reload and open it

As an operator, run:

```text
/cc validate
/cc reload
/cc open welcome
```

If Nexo sends your pack, wait for its rebuild and upload, then load the updated pack. If you host the ZIP yourself, replace the hosted file with the new `plugins/CraftCanvas/craftcanvas.zip`. The menu may wait for the player's pack to load.

You should see the console with a WELCOME heading and a Say hello button. Click the button to get the chat message. The console's Exit button closes the menu.

If it does not open, check [pack delivery](/resources/resource-pack/) and run `/cc debug <player>`.

## 3. What each part does

| Part | What to change |
| --- | --- |
| `id: welcome` | The name used by `/cc open welcome` and navigation actions |
| `extends: frame` | The screen whose console layout you reuse |
| `heading.text` | The label drawn at the top of the console display |
| `description` and `hello` | Your component IDs; keep each ID unique in the screen |
| `position` | Where a component starts: `x` from the left, `y` from the top |
| `size` | The width and height of its area |
| `asset` | Which registered graphic to draw |
| `actions.click` | What happens when the player clicks |

`title` names the book; `heading.text` is the visible heading. The coordinates use the frame's 512 by 320 canvas. The client scales the result with Minecraft's GUI Scale and the selected CraftCanvas profile.

## 4. Change what the button does

To open the example settings app instead, replace the button's `actions` block with:

```yaml
actions:
  click:
    - type: open
      screen: settings
```

Keep `settings` installed for this example, or use the ID of another screen you created. Run `/cc reload` after editing, then open the menu again.

## Where to go next

- [Components and layout](/guides/components/) explains text, images, buttons and positions.
- [Actions](/configuration/actions/) covers commands, navigation, messages and confirmation.
- [Variables](/configuration/variables/) lets you remember choices and display values.
- [Add a PNG](/resources/resource-pack/#add-a-png) shows how to use your own artwork.
- [Configuration overview](/configuration/overview/) explains the rest of the files.
