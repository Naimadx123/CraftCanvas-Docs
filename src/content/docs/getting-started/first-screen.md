---
title: "1st steps"
---

Create a working menu from PNG files and a screen YAML. This walkthrough explains where each file belongs, how asset IDs connect to components, and when to rebuild and publish the resource pack.

The finished example has an image, two destination buttons, a selected frame, a changing label, confirmation and an X. It uses the supplied `frame` screen as its base.

## 1. Start CraftCanvas once

[Install CraftCanvas](/start/installation/), start the server and wait for configuration loading to finish. Edit files in the server's `plugins/CraftCanvas/` directory. You do not need to edit the plugin JAR or its source project.

Keep these supplied files available:

| File | Used for |
| --- | --- |
| `screens/frame.yml` | Shared panel layout, visible heading and X |
| `assets/book.yml`, `assets/book_panel.png`, `assets/close.png`, `assets/console_*.png` | Frame and physical control artwork |
| `assets/sounds/handheld_startup.ogg` | Sound registered by the shared book manifest |
| `assets/images.yml`, `assets/button.png`, `assets/selected_frame.png` | Standard buttons and selection artwork |

Default screens are installed only once. If you removed `frame.yml`, restore it from the JAR's `screens/frame.yml` or [project resources](https://github.com/Naimadx123/CraftCanvas/blob/master/src/main/resources/screens/frame.yml) before continuing. Reload does not recreate deleted screens.

You will add this structure alongside those files:

```text
plugins/CraftCanvas/
  assets/
    travel.yml
    travel/
      map.png
      route_button.png
  screens/
    travel.yml
```

The two `travel.yml` files serve different purposes. `assets/travel.yml` registers images; `screens/travel.yml` defines the menu.

## 2. Put your PNGs in assets

Create `plugins/CraftCanvas/assets/travel/` and put two PNGs inside:

| PNG | Source dimensions | Purpose |
| --- | --- | --- |
| `map.png` | 36 x 36 pixels | Example icon |
| `route_button.png` | 160 x 24 pixels | Background behind button text |

For a first run, copy the supplied `assets/archer.png` to `assets/travel/map.png` and `assets/button.png` to `assets/travel/route_button.png`. This gives you files with exactly those dimensions. Replace the copies with your own artwork afterward.

You can create artwork with any PNG editor. Export as PNG, preserve transparency, and use lowercase names without spaces. Other dimensions are allowed, but the manifest in the next step must match the actual source file. The screen's component size controls how large the image appears in the GUI.

The reusable `frame` supplies the panel, so you only need an icon and button image for this example. To replace the entire panel later, follow [transparent backgrounds and native controls](/reference/book-renderer/#layout-and-native-controls).

## 3. Register the images in a manifest

Create `plugins/CraftCanvas/assets/travel.yml`:

```yaml
schema-version: 1
images:
  craftcanvas:travel/map:
    file: travel/map.png
    width: 36
    height: 36
  craftcanvas:travel/route_button:
    file: travel/route_button.png
    width: 160
    height: 24
```

Every field has a specific role:

| Field | Meaning |
| --- | --- |
| `schema-version: 1` | The configuration format used by this file |
| `images` | Map of image IDs to source PNG definitions |
| `craftcanvas:travel/map` | Unique resource ID you will reference in the screen |
| `file: travel/map.png` | Path relative to `plugins/CraftCanvas/assets/` |
| `width`, `height` | Actual PNG dimensions in pixels |

A resource ID is a name, not a filesystem path. This example chooses a matching name for convenience. Native image IDs use the `craftcanvas:` namespace; the part after the colon distinguishes your resources. The manifest filename can be different from the image IDs.

Do not include `assets/` in `file`, and do not point `file` at the generated pack. Keep source PNGs in `assets/`; CraftCanvas generates the fonts and pack entries for them.

## 4. Start the screen file

Create `plugins/CraftCanvas/screens/travel.yml`. Begin with:

```yaml
schema-version: 1
id: travel
extends: frame
backend: book-glyph
title: '<gold>Travel menu'
```

| Field | Meaning |
| --- | --- |
| `schema-version` | Screen configuration format |
| `id: travel` | Screen ID used by `/cc open travel` and navigation actions |
| `extends: frame` | Inherit the background, heading, X and layout from the supplied `frame` screen |
| `backend: book-glyph` | Render with a signed book and resource-pack glyphs |
| `title` | Book metadata; the visible panel heading is a text component |

Use spaces for YAML indentation, not tabs. Map keys such as `components` are followed by indented child fields. Keep component IDs unique throughout the screen, including nested children. Quote formatted text and resource IDs as shown in the examples.

Do not open the file yet: the following steps add the visible content. Step 8 contains the complete final file, ready to copy.

## 5. Add a variable and components

Add a root `variables` map to store the selected destination. Its default is `spawn`:

```yaml
variables:
  destination:
    type: string
    scope: session
    default: spawn
```

`type` restricts the value to text. `scope: session` gives each player's open menu its own value. Closing and opening a fresh session starts from the default. Use [player variables](/configuration/variables/) when a preference should survive between screens or be saved.

Add a root `components` map for visible elements. Here is the image component you will use:

```yaml
components:
  map_icon:
    type: image
    position: {x: 112, y: 88}
    size: {width: 32, height: 32}
    layer: 20
    asset:
      provider: native
      kind: image
      id: craftcanvas:travel/map
```

| Field | Meaning |
| --- | --- |
| `map_icon` | Stable component ID chosen for this screen |
| `type: image` | Draw a graphic |
| `position.x`, `position.y` | Position relative to the parent; root components use the inherited panel's top-left corner |
| `size.width`, `size.height` | Display size in logical GUI units |
| `layer` | Draw order; larger layers draw later, above smaller layers |
| `asset.provider: native` | Resolve artwork registered in CraftCanvas's own asset manifests |
| `asset.kind: image` | Use a PNG-backed image |
| `asset.id` | Exact ID from `assets/travel.yml` |

The PNG is 36 x 36 source pixels but is displayed at 32 x 32 GUI units. These are different measurements. GUI Scale affects the final on-screen size; the server cannot read the player's resolution. The inherited `frame` uses a 512 x 320 reference area and manual profiles. The complete example keeps application content inside the handheld screen, around x=104..400 and y=80..248, leaving A/B and Home/Exit clear.

When adding more components, put them alongside `map_icon` inside the same `components` map. Do not add a second root `components` key.

## 6. Turn an image into a button

A button combines a graphic, text and actions. In the final file, `spawn` and `market` each have:

| Field | Meaning |
| --- | --- |
| `type: button` | Register click actions for this component |
| `asset` | Your `craftcanvas:travel/route_button` graphic |
| `text` | Label drawn over the image |
| `padding: 8` | Inset between the component edges and its text |
| `tooltip` | Lines shown locally by the client when hovering over the component |
| `states.selected.when` | Condition deciding whether this button is selected |
| `states.selected.overlay` | Graphic drawn over the button while selected |
| `actions.click` | Ordered list of server actions to run when clicked |

The action `{type: set-variable, name: destination, value: market}` changes this session's variable. The selected-state condition then compares it with `market`. The label `Destination: {destination}` inserts the current value into the text.

The action list automatically marks the menu for update when it finishes. With the default `resend` update mode, CraftCanvas opens the changed book again. This can flicker; it is not a live HTML-style element update.

The selected frame persists after clicking. Hover tooltips and cursor behavior are handled locally by Minecraft; the shipped shader configuration adds temporary outlines to eligible controls with text tooltips. See [shader hover](/guides/shader-hover/) to opt out or configure an overlay. Transparent areas of a graphic do not create pixel-shaped click holes. See [hitbox limits](/reference/book-renderer/#clicks-and-updates).

## 7. Add confirmation and keep the X

The final `confirm` button opens a confirmation view. Its `on-confirm` actions send the selected destination to chat, then close the session. Cancel returns without running those actions.

The root X comes from `frame`. Leave it unconditional and keep its sole click action as `close`. The inherited `heading` component is also reused: setting only its `text` overrides that label while retaining its position and size.

## 8. Copy the complete screen

Replace `screens/travel.yml` with this complete file. It includes all the steps above; the earlier snippets were parts of this file, not additional files.

```yaml
schema-version: 1
id: travel
extends: frame
backend: book-glyph
title: '<gold>Travel menu'
variables:
  destination:
    type: string
    scope: session
    default: spawn
components:
  heading:
    text: '<gold>TRAVEL'
  map_icon:
    type: image
    position: {x: 112, y: 88}
    size: {width: 32, height: 32}
    layer: 20
    asset: {provider: native, kind: image, id: 'craftcanvas:travel/map'}
  description:
    type: text
    position: {x: 156, y: 88}
    size: {width: 244, height: 32}
    layer: 20
    text: '<white>Choose your next destination.'
    overflow: wrap
  spawn:
    type: button
    position: {x: 112, y: 136}
    size: {width: 136, height: 32}
    padding: 8
    align: center
    vertical-align: center
    layer: 20
    text: '<white>Spawn'
    asset: {provider: native, kind: image, id: 'craftcanvas:travel/route_button'}
    tooltip: ['<gray>Select the server spawn.']
    states:
      selected:
        when: {variable-equals: {name: destination, value: spawn}}
        overlay: {provider: native, kind: image, id: 'craftcanvas:selected_frame'}
    actions:
      click:
        - {type: set-variable, name: destination, value: spawn}
  market:
    type: button
    position: {x: 264, y: 136}
    size: {width: 136, height: 32}
    padding: 8
    align: center
    vertical-align: center
    layer: 20
    text: '<white>Market'
    asset: {provider: native, kind: image, id: 'craftcanvas:travel/route_button'}
    tooltip: ['<gray>Select the market.']
    states:
      selected:
        when: {variable-equals: {name: destination, value: market}}
        overlay: {provider: native, kind: image, id: 'craftcanvas:selected_frame'}
    actions:
      click:
        - {type: set-variable, name: destination, value: market}
  selection:
    type: text
    position: {x: 112, y: 180}
    size: {width: 288, height: 20}
    layer: 20
    text: '<gold>Destination: <white>{destination}'
  confirm:
    type: button
    position: {x: 112, y: 216}
    size: {width: 288, height: 28}
    padding: 8
    align: center
    vertical-align: center
    layer: 20
    text: '<gold>Confirm destination'
    tooltip: ['<gray>Confirm the selected destination.']
    asset: {provider: native, kind: image, id: 'craftcanvas:travel/route_button'}
    actions:
      click:
        - type: confirm
          title: '<gold>Travel'
          text: '<white>Choose {destination}?'
          on-confirm:
            - {type: message, text: '<green>Selected destination: {destination}'}
            - {type: close}
```

This menu selects and confirms a destination. To make it teleport, replace the confirmation message with a command from your server's teleport plugin or a registered addon callback. See [actions](/configuration/actions/).

## 9. Validate and build

Run these commands as an administrator, waiting for each result:

```text
/cc validate travel
/cc reload
```

`validate` checks the configuration without replacing the active screens. `reload` compiles the configuration, generates the required positioned glyphs, builds the resource pack and applies the result. On a rejected reload, the previous valid configuration stays active. Read the file, component and field in the error before trying again.

`/cc pack build` uses the same reload/build operation. You do not need to run both it and `/cc reload` for one edit.

Adding a component, moving it or changing its size can require new glyphs even if the PNG already exists. Rebuild after layout changes as well as artwork changes. Do not edit generated font JSON manually.

## 10. Deliver the resource pack

### With Nexo

Leave `pack.nexo-auto-export: true` in CraftCanvas's `config.yml`. CraftCanvas writes `plugins/Nexo/pack/external_packs/craftcanvas.zip` and requests a merged-pack rebuild. Nexo must already have its generation, hosting and sending configured. Accept its combined pack and wait for loading to finish. If Nexo does not resend to connected players after rebuilding, follow its resend settings or reconnect.

### Without Nexo

1. Take `plugins/CraftCanvas/craftcanvas.zip` from the successful build.
2. Upload it to your own host at a direct HTTP(S) ZIP URL.
3. In `plugins/CraftCanvas/config.yml`, set `pack.sender` to `craftcanvas`, `pack.url` to that URL and `pack.sha1` to an empty string to use the generated hash. Set `pack.nexo-auto-export` to false if you are deliberately bypassing an installed Nexo sender.
4. Run `/cc reload`, accept the pack and wait for loading to finish.

After later edits, replace the hosted ZIP with the newly generated one and resend/reload it. Use one responsible sender. If CraftEngine or ItemsAdder sends a combined pack, follow the [external pack workflow](/resources/resource-pack/#external-workflows) instead.

## 11. Open and check the menu

Run:

```text
/cc open travel
```

The image and buttons should appear inside the inherited frame. Spawn starts selected. Click Market: its selected frame and the destination label should update. Hover each button for its tooltip, use Confirm to send a message, and use the X to close.

Test your configured GUI profiles and the GUI Scale settings your players use. A successful build does not by itself establish the appearance or hitboxes in a client. Book updates can reopen or flicker, and Escape does not provide a reliable immediate server close signal.

If opening is queued or denied, run `/cc debug <player>` and check the expected pack UUID and loaded status. A successful resource-pack build is separate from a player's `SUCCESSFULLY_LOADED` acknowledgement.

## 12. Make it your own

| Change | What to edit |
| --- | --- |
| Replace the icon | Overwrite `assets/travel/map.png`; update manifest dimensions if necessary |
| Use a different button image | Replace `assets/travel/route_button.png` or register another image ID |
| Change the heading | `components.heading.text` |
| Move an element | Its `position`; then rebuild and load the new pack |
| Scale an image | Its `size`, keeping manifest dimensions equal to the actual PNG |
| Restrict opening | Add `permission: server.travel` at the screen root |
| Hide or disable a destination | Add [component conditions](/configuration/conditions/) |
| Save a preference | Use a player-scoped persistent variable and [a supported control](/guides/components/#input-controls) |
| Reuse button definitions | Create a [template](/configuration/reuse/#reuse-a-button-with-parameters) |

Keep source assets, manifests and screen YAML together in backups. Your own screen files are administrator-managed: CraftCanvas does not overwrite them or restore them after deletion. Continue with the [YAML reference](/reference/yaml/) for every field and default, or the [resource-pack guide](/resources/resource-pack/) for more artwork and sounds.
