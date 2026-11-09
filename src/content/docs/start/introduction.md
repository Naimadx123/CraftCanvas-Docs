---
title: "CraftCanvas documentation"
---

Build graphical menus with signed books, text components and resource-pack glyphs. Screens, artwork, navigation, state and server actions are configured in YAML.

## For server administrators

1. [Install CraftCanvas](/start/installation/) and configure one resource-pack sender.
2. [1st steps: assets and your first screen](/getting-started/first-screen/).
3. Add [graphics and sounds](/resources/resource-pack/), [controls](/guides/components/), [actions](/configuration/actions/) and [conditions](/configuration/conditions/).
4. Reuse [themes and templates](/configuration/reuse/), or add [animations](/guides/animations/).

| Task | Guide |
| --- | --- |
| Find a field, type, default or limit | [YAML reference](/reference/yaml/) |
| Display item icons without copying vanilla PNGs | [Vanilla sprites](/guides/sprites/) |
| Build a crafting board backed by player inventory | [Inventory workbenches](/guides/crafting/) |
| Display item models in slots automatically | [Automatic item models](/guides/item-icons/) |
| Store preferences and insert dynamic text | [Variables and formatting](/configuration/variables/) |
| Choose a screen font or override fragments with MiniMessage | [Screen fonts](/guides/fonts/) |
| Add scrolling, search, component animations and GUI notifications | [Interactive UI](/guides/interactive-ui/) |
| Configure SQLite, MySQL, MariaDB or PostgreSQL | [Player storage](/configuration/storage/) |
| Configure Nexo, CraftEngine or ItemsAdder | [Integrations](/resources/integrations/) |
| Open, validate or inspect a screen | [Commands and permissions](/start/commands/) |
| Diagnose loading, graphics or clicks | [Troubleshooting](/resources/troubleshooting/) |
| Understand native book controls and update behavior | [Renderer limits](/reference/book-renderer/) |
| Upgrade an existing installation | [Upgrading](/resources/migration/) |
| Try optional button hover overlays on supported vanilla clients | [Experimental shader hover](/guides/shader-hover/) |

## For addon developers

Start with the [developer quick start](/developers/quick-start/), then use the [API reference](/developers/api/).

| Task | Guide |
| --- | --- |
| Open screens and update player data | [Sessions and presentation](/developers/sessions/) |
| Register callbacks, conditions and providers | [Extension points](/developers/callbacks/) |
| Install YAML and artwork from your addon JAR | [Addon bundles](/developers/bundles/) |
| Compile configuration and observe the result | [Reloading](/developers/reload/) |
| Listen for opening, closing and selection | [Events](/developers/events/) |
| Build CraftCanvas or publish its API | [Building from source](/developers/building/) |

The examples use schema 1 and the shipped `frame` screen. Whole-file examples can be copied directly; component and action fragments state where they belong. Matching vanilla clients are required. Review [renderer limits](/reference/book-renderer/) when designing layouts.
